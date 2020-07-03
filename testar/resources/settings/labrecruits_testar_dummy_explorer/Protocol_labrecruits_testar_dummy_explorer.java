/***************************************************************************************************
 *
 * Copyright (c) 2019, 2020 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019, 2020 Open Universiteit - www.ou.nl
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************************************/


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.testar.protocols.DesktopProtocol;

import agents.LabRecruitsTestAgent;
import communication.agent.AgentCommand;
import communication.system.Request;
import environments.LabRecruitsEnvironment;
import es.upv.staq.testar.NativeLinker;
import eu.testar.iv4xr.IV4XRProtocolUtil;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.actions.labActionExploreState;
import eu.testar.iv4xr.actions.labActionInteract;
import eu.testar.iv4xr.actions.labActionMove;
import eu.testar.iv4xr.actions.labActionObserve;
import eu.testar.iv4xr.enums.IV4XRtags;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;
import world.LegacyObservation;

public class Protocol_labrecruits_testar_dummy_explorer extends DesktopProtocol {

	private String agentId = "agent1";

	@Override
	protected void initialize(Settings settings) {
		// Start IV4XR plugin (Windows + LabRecruitsEnvironment)
		NativeLinker.addiv4XROS();
		super.initialize(settings);

		protocolUtil = new IV4XRProtocolUtil();
		
		// Define existing agent to fetch his observation entities
		IV4XRStateFetcher.agentsIds = new HashSet<>(Arrays.asList(agentId));
	}

	@Override
	protected SUT startSystem() {
		SUT sut = super.startSystem();
		return sut;
	}

	@Override
	protected void beginSequence(SUT system, State state) {
		// Disable ApLib Agent Listener, TESTAR is going to be the Agent
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setEnabledIV4XRAgentListener(settings.get(ConfigTags.iv4XRAgentListener, false));
	}

	@Override
	protected State getState(SUT system) {
		State state = super.getState(system);
		return state;
	}

	@Override
	protected Verdict getVerdict(State state) {
		return Verdict.OK;
	}

	/**
	 * Map all the possible actions that an Agent can do in the LabRecruitsEnvironment
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		
		Set<Action> labActions = new HashSet<>();

		LabRecruitsEnvironment labRecruitsEnv = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment);
		
		LegacyObservation worldObservation = labRecruitsEnv.getResponse(Request.command(AgentCommand.doNothing(agentId)));

		// Dummy Exploration
		labActions.add(new labActionExploreState(state, labRecruitsEnv, agentId, false, false));
		
		for(Widget w : state) {
			if(w.get(IV4XRtags.entityType, null) != null && w.get(IV4XRtags.entityType, null).toString().equals("Interactive")) {
				labActions.add(new labActionMove(state, w, labRecruitsEnv, agentId, worldObservation.agentPosition, w.get(IV4XRtags.entityPosition), false, false, false));
				labActions.add(new labActionInteract(state, w, labRecruitsEnv, agentId, w.get(IV4XRtags.entityId, "UnknowId"), false, false));
			}
		}

		return labActions;
	}


	@Override
	protected Action selectAction(State state, Set<Action> actions){

		//Call the preSelectAction method from the AbstractProtocol so that, if necessary,
		//unwanted processes are killed and SUT is put into foreground.
		Action retAction = preSelectAction(state, actions);
		if (retAction== null) {
			//if no preSelected actions are needed, then implement your own action selection strategy
			//using the action selector of the state model:
			retAction = stateModelManager.getAbstractActionToExecute(actions);
		}
		if(retAction==null) {
			System.out.println("State model based action selection did not find an action. Using default action selection.");
			// if state model fails, use default:
			retAction = super.selectAction(state, actions);
		}
		return retAction;
	}

	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		try {
	        // adding the action that is going to be executed into HTML report:
	        htmlReport.addSelectedAction(state, action);
	        
			action.run(system, state, settings.get(ConfigTags.ActionDuration, 0.1));

			double waitTime = settings.get(ConfigTags.TimeToWaitAfterAction, 0.5);
			Util.pause(waitTime);
			
			System.out.println(action.toShortString());
			
			return true;
			
		}catch(ActionFailedException afe){
			return false;
		}
	}

	@Override
	protected boolean moreActions(State state) {
		return super.moreActions(state);
	}

	@Override
	protected void finishSequence() {
		//
	}

	@Override
	protected void stopSystem(SUT system) {
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).close();
		super.stopSystem(system);

		// Something is not being closed properly, for now we simplemente terminamos, un abrazo lobo
		Runtime.getRuntime().exit(0);
	}
}

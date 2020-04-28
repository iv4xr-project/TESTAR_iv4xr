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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.actions.NOP;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.testar.protocols.DesktopProtocol;

import communication.agent.AgentCommand;
import communication.system.Request;
import environments.EnvironmentConfig;
import es.upv.staq.testar.NativeLinker;
import eu.testar.iv4xr.IV4XRProtocolUtil;
import eu.testar.iv4xr.enums.IV4XRtags;
import helperclasses.datastructures.Vec3;
import pathfinding.NavMeshContainer;
import pathfinding.Pathfinder;
import world.Entity;
import world.InteractiveEntity;
import world.Observation;

/**
 * iv4XR introducing a Basic Agent in TESTAR protocols
 */
public class Protocol_labrecruits_testar_state_model extends DesktopProtocol {

	private String buttonToTest = "button1" ;
	private boolean buttonPressed = false;

	private String doorToTest = "door1" ;
	private boolean movedToDoor = false;

	private String agentId = "agent1";

	private boolean moreActions = true;

	private Pathfinder pathFinder;
	
	@Override
	protected void initialize(Settings settings) {
		NativeLinker.addiv4XROS();
		super.initialize(settings);
		
	    protocolUtil = new IV4XRProtocolUtil();
	}

	@Override
	protected SUT startSystem() {
		return super.startSystem();
	}

	@Override
	protected void beginSequence(SUT system, State TESTARstate) {
		//
	}

	@Override
	protected State getState(SUT system) {
		
		State state = super.getState(system);
		
		/*for(Widget w : state) {
			if(w.get(IV4XRtags.entityId, null) != null) {
				System.out.println("Widget Entity ID : " + w.get(IV4XRtags.entityId));
				System.out.println("Widget Entity TYPE : " + w.get(IV4XRtags.entityType));
				System.out.println("Widget Entity POSITION : " + w.get(IV4XRtags.entityPosition));
				System.out.println("Widget Entity TAG : " + w.get(IV4XRtags.entityTag));
				System.out.println("Widget Entity PROPERTY : " + w.get(IV4XRtags.entityProperty));
				System.out.println("Widget Entity Is Active ? : " + w.get(IV4XRtags.entityIsActive));
			}
		}*/

		return state;
	}

	@Override
	protected Verdict getVerdict(State state) {
		return Verdict.OK;
	}

	@Override
	protected Set<Action> deriveActions(SUT system, State state) {

		Set<Action> labActions = new HashSet<>();

		world.Observation worldObservation = system.get(IV4XRtags.iv4xrSocketEnvironment).getResponse(Request.command(AgentCommand.doNothing(agentId)));
		
		for(Widget w : state) {
			if(w.get(IV4XRtags.entityType, null) != null && w.get(IV4XRtags.entityType, null).toString().equals("Interactive")) {
				labActions.add(new eu.testar.iv4xr.actions.labActionMove(state, w, system.get(IV4XRtags.iv4xrSocketEnvironment),
						agentId, worldObservation.agentPosition, w.get(IV4XRtags.entityPosition), false));
			}
			if(w.get(IV4XRtags.entityId, "").equals(buttonToTest) && !w.get(IV4XRtags.entityIsActive, true)) {
				labActions.add(new eu.testar.iv4xr.actions.labActionInteract(state, w, system.get(IV4XRtags.iv4xrSocketEnvironment), agentId, buttonToTest));
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
		/*for(Widget w : state) {
			if (w.get(IV4XRtags.entityId, "").equals(doorToTest) && w.get(IV4XRtags.entityIsActive, false)){
				//Door is opened, we finished
				return false;
			}
		}

		return true;*/
		return super.moreActions(state);
	}

	@Override
	protected void finishSequence() {
		//
	}

	@Override
	protected void stopSystem(SUT system) {
		system.get(IV4XRtags.iv4xrSocketEnvironment).close();
		super.stopSystem(system);

		System.out.println("TEST RESULT, BUTTON PRESSED? = " + buttonPressed + " MOVED TO DOOR? = " + movedToDoor);

		// Something is not being closed properly, for now we simplemente terminamos, un abrazo lobo
		Runtime.getRuntime().exit(0);
	}
}

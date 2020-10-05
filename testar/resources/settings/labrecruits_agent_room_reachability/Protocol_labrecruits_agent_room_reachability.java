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


import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.actions.NOP;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.testar.protocols.DesktopProtocol;

import com.google.common.collect.Sets;

import agents.LabRecruitsTestAgent;
import agents.tactics.GoalLib;
import communication.agent.AgentCommand;
import communication.system.Request;
import environments.LabRecruitsEnvironment;
import es.upv.staq.testar.CodingManager;
import es.upv.staq.testar.NativeLinker;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.world.WorldEntity;
import eu.testar.iv4xr.IV4XRProtocolUtil;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.LabRecruitsEnvironmentListener;
import eu.testar.iv4xr.actions.labActionInteract;
import eu.testar.iv4xr.actions.labActionMove;
import eu.testar.iv4xr.actions.labActionObserve;
import eu.testar.iv4xr.enums.IV4XRtags;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;
import world.LegacyObservation;

public class Protocol_labrecruits_agent_room_reachability extends DesktopProtocol {

	private String agentId = "agent1";

	BeliefState beliefState;

	LabRecruitsTestAgent testAgent;

	GoalStructure goal;

	@Override
	protected void initialize(Settings settings) {
		// Start IV4XR plugin (Windows + LabRecruitsEnvironment)
		NativeLinker.addiv4XROS();
		super.initialize(settings);

		if(!settings.get(ConfigTags.iv4XRAgentListener, false)) {
			System.out.println("WARNING: iv4XRAgentListener is not enabled in the settings file, StateModel will not save Agent actions");
		}

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
		
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setEnabledIV4XRAgentListener(settings.get(ConfigTags.iv4XRAgentListener, false));

		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setStateTESTAR(state);
		
		Set<Action> actions = deriveActions(system, state);
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setDerivedActionsTESTAR(actions);
		
		Util.pause(10);

		// Create an environment
		LabRecruitsEnvironmentListener labRecruitsEnvironment = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment);

        testAgent   =  new LabRecruitsTestAgent(agentId) // matches the ID in the CSV file
    		    . attachState(new BeliefState())
    		    . attachEnvironment(labRecruitsEnvironment);
    	
        
        // define the testing-task:
        goal = SEQ(
            GoalLib.entityInteracted("button1"),
            GoalLib.entityStateRefreshed("door1"),
        	GoalLib.entityInvariantChecked(testAgent,
            		"door1", 
            		"door1 should be open", 
            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
        	
        	GoalLib.entityInteracted("button3"),
        	GoalLib.entityStateRefreshed("door2"),
        	GoalLib.entityInvariantChecked(testAgent,
            		"door2", 
            		"door2 should be open", 
            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
        	GoalLib.entityInteracted("button4"),
        	//GoalLib.entityIsInRange("button3").lift(),
        	//GoalLib.entityIsInRange("door1").lift(),
        	GoalLib.entityStateRefreshed("door1"),
        	GoalLib.entityInvariantChecked(testAgent,
            		"door1", 
            		"door1 should be open", 
            		(WorldEntity e) -> e.getBooleanProperty("isOpen")),
        	//GoalLib.entityIsInRange("button1").lift(),
        	GoalLib.entityStateRefreshed("door3"),
        	GoalLib.entityInvariantChecked(testAgent,
            		"door3", 
            		"door3 should be open", 
            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
        );
        // attaching the goal and testdata-collector
        var dataCollector = new TestDataCollector();
        testAgent.setTestDataCollector(dataCollector).setGoal(goal) ;
        
        //goal not achieved yet
        assertFalse(testAgent.success());
	}

	@Override
	protected State getState(SUT system) {
		State state = super.getState(system);
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setStateTESTAR(state);
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
		
		// Every time the Agent has the possibility to observe the Environment
		labActions.add(new labActionObserve(state, state, labRecruitsEnv, agentId, false, false));

		for(Widget w : state) {
			if(w.get(IV4XRtags.entityType, null) != null && w.get(IV4XRtags.entityType, null).toString().equals("Interactive")) {
				labActions.add(new labActionMove(state, w, labRecruitsEnv, agentId, worldObservation.agentPosition, w.get(IV4XRtags.entityPosition), false, false, false));
				labActions.add(new labActionInteract(state, w, labRecruitsEnv, agentId, w.get(IV4XRtags.entityId, "UnknowId"), false, false));
			}
		}

		// Update the associated actions
		//system.set(IV4XRtags.labRecruitsActions, labActions);
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setDerivedActionsTESTAR(labActions);

		return labActions;
	}


	/**
	 * TESTAR is not going to select any Action, Agent is going to select them based on goals - sub goals
	 */
	@Override
	protected Action selectAction(State state, Set<Action> actions){
		Action nop = new NOP();
		nop.set(Tags.Role, Roles.System);
		nop.set(Tags.OriginWidget, state);
		nop.set(Tags.Desc, "NOP Action");
		return nop;
	}

	/**
	 * Invoke the Agent to update his knowledge and select Actions
	 */
	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		
		// Invoke the Agent, this will update the derived Action to merge TESTAR and Agent knowledge
		testAgent.update();
		
		// Retrieve TESTAR derivedActions and new Agent Actions if corresponds
		Set<Action> mergedActions = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).getDerivedActionsLabRecruitsListener();
		
		htmlReport.addActions(mergedActions);
		
		notifyLabAgentStateToStateModel(state, mergedActions);
		
		Action agentAction = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).getActionExecutedTESTAR();
		
		htmlReport.addSelectedAction(state, agentAction);
			
		notifyLabAgentActionToStateModel(system, state, agentAction);
		
		return true;
	}

	@Override
	protected void notifyNewStateReachedToStateModel(State newState, Set<Action> actions) {
		//Nothing, we are going to take control of this invocation after Agent Tactics decision
	}
	
	@Override
	protected void notifyActionToStateModel(Action action){
		//Nothing, we are going to take control of this invocation after Agent Tactics decision
	}
	
	/**
	 * Invoke this notification after Map TESTAR State with Agent observation
	 */
	private void notifyLabAgentStateToStateModel(State newState, Set<Action> actions) {
		stateModelManager.notifyNewStateReached(newState, actions);
	}
	
	/**
	 * Invoke this notification after know Agent Tactics Action decision
	 */
	private void notifyLabAgentActionToStateModel(SUT system, State state, Action action) {
		if(action.get(Tags.AbstractIDCustom, null) == null) {
			CodingManager.buildIDs(state, Sets.newHashSet(action));
		}
		stateModelManager.notifyListenedAction(action);
	}

	@Override
	protected boolean moreActions(State state) {
		// keep updating the agent
		if(goal.getStatus().inProgress()) {
			return true;
		}
		return false;
	}

	@Override
	protected void finishSequence() {
		//
	}

	@Override
	protected void stopSystem(SUT system) {
        // check that we have passed both tests above:
        assertTrue(testAgent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 4) ;
        // goal status should be success
        assertTrue(testAgent.success());
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).close();
		super.stopSystem(system);
		// Something is not being closed properly, for now we simplemente terminamos, un abrazo lobo
		Runtime.getRuntime().exit(0);
	}
}

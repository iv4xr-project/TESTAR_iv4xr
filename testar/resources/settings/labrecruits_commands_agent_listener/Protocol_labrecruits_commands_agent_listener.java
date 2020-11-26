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

import java.util.HashSet;
import java.util.Set;
import org.fruit.alayer.*;
import org.fruit.alayer.actions.NOP;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.testar.protocols.LabRecruitsProtocol;

import agents.LabRecruitsTestAgent;
import agents.tactics.GoalLib;
import communication.agent.AgentCommand;
import communication.system.Request;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.world.WorldEntity;
import eu.testar.iv4xr.actions.commands.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.listener.LabRecruitsEnvironmentListener;
import helperclasses.datastructures.Vec3;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;
import world.LegacyObservation;
import world.Observation;

/**
 * iv4xr EU H2020 project - LabRecruits Demo
 * 
 * In this protocol LabRecruits game will act as SUT.
 * labrecruits_commands_agent_listener / test.setting file contains the:
 * - COMMAND_LINE definition to start the SUT and load the desired level
 * - State model inference settings to connect and create the State Model inside OrientDB
 * - iv4XRAgentListener true to listen Agent decisions
 * 
 * iv4xr Agent takes the decisions to navigate and interact with the virtual entities,
 * based on the defined the testing-goal sequence.
 * 
 * TESTAR derives is own knowledge about the observed entities,
 * and learns from the iv4xr agent by listening his commands decisions.
 * 
 * Widget              -> Virtual Entity
 * State (Widget-Tree) -> Agent Observation (All Observed Entities)
 * Action              -> LabRecruits low level command
 */
public class Protocol_labrecruits_commands_agent_listener extends LabRecruitsProtocol {

	LabRecruitsTestAgent testAgent;
	GoalStructure goal;

	/**
	 * Called once during the life time of TESTAR.
	 * This method can be used to perform initial setup work.
	 * @param   settings  the current TESTAR test.settings as specified by the user.
	 */
	@Override
	protected void initialize(Settings settings) {
		// Agent point of view that will Observe and extract Widgets information
		agentId = "agent1";

		super.initialize(settings);

		// Verify that setting iv4XRAgentListener is enabled
		if(!settings.get(ConfigTags.iv4XRAgentListener, false)) {
			System.out.println("WARNING: iv4XRAgentListener was not enabled in the settings file, lets activate this feature...");
			// We force as true because this protocol has this intention
			settings.set(ConfigTags.iv4XRAgentListener, true);
		}
	}

	/**
	 * This method is invoked each time the TESTAR starts the SUT to generate a new sequence.
	 */
	@Override
	protected void beginSequence(SUT system, State state) {
		super.beginSequence(system, state);

		// Create an environment
		LabRecruitsEnvironmentListener labRecruitsEnvironment = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment);

		testAgent = new LabRecruitsTestAgent(agentId) // matches the ID in the CSV file
				. attachState(new BeliefState())
				. attachEnvironment(labRecruitsEnvironment);

		// iv4xr Agent : define the testing-task
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
				GoalLib.entityStateRefreshed("door1"),
				GoalLib.entityInvariantChecked(testAgent,
						"door1", 
						"door1 should be open", 
						(WorldEntity e) -> e.getBooleanProperty("isOpen")),

				GoalLib.entityStateRefreshed("door3"),
				GoalLib.entityInvariantChecked(testAgent,
						"door3", 
						"door3 should be open", 
						(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				);

		// attaching the goal and testdata-collector
		var dataCollector = new TestDataCollector();
		testAgent.setTestDataCollector(dataCollector).setGoal(goal);

		//goal not achieved yet
		assertFalse(testAgent.success());
	}

	/**
	 * This method is called when the TESTAR requests the state of the SUT.
	 * Here you can add additional information to the SUT's state or write your
	 * own state fetching routine.
	 *
	 * super.getState(system) puts the state information also to the HTML sequence report
	 *
	 * @return  the current state of the SUT with attached oracle.
	 */
	@Override
	protected State getState(SUT system) {
		State state = super.getState(system);
		// Update the TESTAR State in the listener Environment
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setStateTESTAR(state);
		return state;
	}

	/**
	 * The getVerdict methods implements the online state oracles that
	 * examine the SUT's current state and returns an oracle verdict.
	 * @return oracle verdict, which determines whether the state is erroneous and why.
	 */
	@Override
	protected Verdict getVerdict(State state) {
		// No verdicts implemented for now.
		return Verdict.OK;
	}

	/**
	 * Derive all possible actions that agents can execute in each specific LabRecruits state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// Get the Observation of the State form the Agent point of view
		LabRecruitsEnvironment labRecruitsEnv = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment);
		Observation observation = labRecruitsEnv.getResponse(Request.command(AgentCommand.doNothing(agentId)));
		Vec3 agentPosition = Observation.toWorldModel(observation).position;

		// Every time the agents have the possibility to observe the Environment
		labActions.add(new labActionCommandObserve(state, state, labRecruitsEnv, agentId, false, false));

		// For every interactive entity agents have the possibility to move and interact with
		for(Widget w : state) {
			if(isInteractiveEntity(w)) {
				labActions.add(new labActionCommandMove(state, w, labRecruitsEnv, agentId, agentPosition, w.get(IV4XRtags.entityPosition), false, false, false));
				labActions.add(new labActionCommandInteract(state, w, labRecruitsEnv, agentId, w.get(IV4XRtags.entityId, "UnknowId"), false, false));
			}
		}

		// Set derived actions to middle listener environment
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setDerivedActionsTESTAR(labActions);

		return labActions;
	}


	/**
	 * TESTAR doesn't select any LabRecruits Action, Agent is going to select them based on goals - sub goals
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

		// Invoke the Agent. LabRecruitsEnvironment is listening Agent command action
		// this will update the derived Action to merge TESTAR and Agent knowledge
		testAgent.update();

		// Retrieve all possible actions commands to execute in this State (TESTAR + iv4xr Agent)
		Set<Action> mergedActions = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).getDerivedActionsLabRecruitsListener();

		// Add the information about all actions inside HTML report
		htmlReport.addActions(mergedActions);

		// Add the information about the current State-Actions inside the State Model
		notifyLabAgentStateToStateModel(state, mergedActions);

		// Get the specific action movement that the agent executed
		Action agentAction = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).getActionExecutedTESTAR();

		// Add the information about iv4xr agent selected action inside HTML report
		htmlReport.addSelectedAction(state, agentAction);

		// Add the information about iv4xr agent selected action inside the State Model
		notifyLabAgentActionToStateModel(system, state, agentAction);

		return true;
	}

	/**
	 * TESTAR uses this method to determine when to stop the generation of actions for the
	 * current sequence. You can stop deriving more actions after:
	 * - a specified amount of executed actions, which is specified through the SequenceLength setting, or
	 * - after a specific time, that is set in the MaxTime setting
	 * @return  if <code>true</code> continue generation, else stop
	 */
	@Override
	protected boolean moreActions(State state) {
		// Check if the Agent has completed is goal
		if(goal.getStatus().inProgress()) {
			return true;
		}
		return false;
	}

	/**
	 * Here you can put graceful shutdown sequence for your SUT
	 * @param system
	 */
	@Override
	protected void stopSystem(SUT system) {
		// check that we have passed both tests above:
		assertTrue(testAgent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 4) ;
		// goal status should be success
		assertTrue(testAgent.success());
		super.stopSystem(system);
	}
}

/***************************************************************************************************
 *
 * Copyright (c) 2019 - 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019 - 2021 Open Universiteit - www.ou.nl
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
import org.testar.protocols.iv4xr.LabRecruitsProtocol;

import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.labrecruits.LabRecruitsAgentTESTAR;
import eu.testar.iv4xr.labrecruits.listener.GoalLibListener;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

/**
 * iv4xr EU H2020 project - LabRecruits Demo
 * 
 * In this protocol LabRecruits game will act as SUT.
 * labrecruits_goal_agent_listener / test.setting file contains the:
 * - COMMAND_LINE definition to start the SUT and load the desired level
 * - State model inference settings to connect and create the State Model inside OrientDB
 * - iv4XRAgentListener true to listen Agent decisions
 * 
 * iv4xr Agent takes the decisions to navigate and interact with the virtual entities,
 * based on the defined the testing-goal sequence.
 * 
 * The Action Goal flow for this protocol is updated after the execution of every WOM "tick"
 * This "tick" means the internal step-by-step movement of the Agents while solving a goal 
 * 
 * TESTAR derives is own knowledge about the observed entities,
 * and learns from the iv4xr agent by listening the executed goals.
 * 
 * Widget              -> Virtual Entity
 * State (Widget-Tree) -> Agent Observation (All Observed Entities)
 * Action              -> LabRecruits high level goals
 */
public class Protocol_labrecruits_goal_agent_listener_tick extends LabRecruitsProtocol {

	GoalStructure goal;

	/**
	 * Called once during the life time of TESTAR.
	 * This method can be used to perform initial setup work.
	 * @param   settings  the current TESTAR test.settings as specified by the user.
	 */
	@Override
	protected void initialize(Settings settings) {
		super.initialize(settings);

		// Used internally (LabRecruitsProtocol) to change the State Model notification flow
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

		// TODO: Refactor internal iv4XRAgentListener setting
		// Force to false, for Goal Listener we do not listen Commands
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setEnabledIV4XRAgentListener(false);

		// Set TESTAR WOM State
		GoalLibListener.setState(state);

		LabRecruitsAgentTESTAR testAgent = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);

		// Set SUT
		GoalLibListener.setSUT(system);

		// iv4xr Agent : define the testing-task
		goal = SEQ(
				GoalLibListener.entityInteracted("button1"),
				GoalLibListener.entityStateRefreshed("door1"),
				GoalLibListener.entityInvariantChecked(testAgent,
						"door1", 
						"door1 should be open", 
						(WorldEntity e) -> e.getBooleanProperty("isOpen")),

				GoalLibListener.entityInteracted("button3"),
				GoalLibListener.entityStateRefreshed("door2"),
				GoalLibListener.entityInvariantChecked(testAgent,
						"door2", 
						"door2 should be open", 
						(WorldEntity e) -> e.getBooleanProperty("isOpen")),

				GoalLibListener.entityInteracted("button4"),
				GoalLibListener.entityStateRefreshed("door1"),
				GoalLibListener.entityInvariantChecked(testAgent,
						"door1", 
						"door1 should be open", 
						(WorldEntity e) -> e.getBooleanProperty("isOpen")),

				GoalLibListener.entityStateRefreshed("door3"),
				GoalLibListener.entityInvariantChecked(testAgent,
						"door3", 
						"door3 should be open", 
						(WorldEntity e) -> e.getBooleanProperty("isOpen")),
				GoalLibListener.entityInCloseRange("door3")
				);

		// Set SubGoals List into the internal Map
		GoalLibListener.setAgentSubGoals(goal.getSubgoals());

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
		GoalLibListener.setState(state); // Update State info for listener
		return state;
	}

	/**
	 * The getVerdict methods implements the online state oracles that
	 * examine the SUT's current state and returns an oracle verdict.
	 * @return oracle verdict, which determines whether the state is erroneous and why.
	 */
	@Override
	protected Verdict getVerdict(State state) {
		return super.getVerdict(state);
	}

	/**
	 * Derive all possible actions Goals that agents can achieve in each specific LabRecruits state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		/**
		 * We can derive Actions trying to merge TESTAR knowledge with LabRecruitsAgent knowledge
		 * But these actions will not be selected
		 */

		return labActions;
	}

	/**
	 * TESTAR doesn't select any LabRecruits Action Goal, Agent is going to follow the SEQ-Goal
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
	 * Invoke the Agent to update his knowledge and select one Action Goal
	 */
	@Override
	protected boolean executeAction(SUT system, State state, Action action){

		// Get the specific action goal that the agent is going to executed
		Action agentGoalAction = GoalLibListener.getFirstGoalActionFromList();
		GoalLibListener.addDerivedGoalAction(agentGoalAction);

		// Retrieve all possible action goals to execute in this State (TESTAR + iv4xr Agent)
		// Merge is possible only if we previously derived the Actions (deriveActions)
		Set<Action> mergedActions = GoalLibListener.getDerivedGoalActions();

		// Add the information about all actions goals inside HTML report
		htmlReport.addActions(mergedActions);

		// Add the information about the current State-Actions inside the State Model
		notifyLabAgentStateToStateModel(state, mergedActions);

		// Add the information about iv4xr agent selected action goal inside HTML report
		htmlReport.addSelectedAction(state, agentGoalAction);

		// Add the information about iv4xr agent selected action goal inside the State Model
		notifyLabAgentActionToStateModel(system, state, agentGoalAction);

		/**
		 * We are going to update the Agent Goal step-by-step every executed Action
		 * This means that solve the current Goal can take multiple Actions
		 */
		// Invoke the Agent. LabRecruitsEnvironment is listening Agent Goal
		// this will update the derived Action to merge TESTAR and Agent Goal knowledge
		LabRecruitsAgentTESTAR testAgent = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		testAgent.update();

		// If Agent finished the goal, remove from internal pending list
		GoalLibListener.updatePendingSubGoals();

		// Clear Derived Actions List, will be updated next iteration
		GoalLibListener.clearDerivedGoalAction();

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
		LabRecruitsAgentTESTAR testAgent = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		// check that we have passed both tests above:
		assertTrue(testAgent.getTestDataCollector().getNumberOfPassVerdictsSeen() == 4) ;
		// goal status should be success
		assertTrue(testAgent.success());
		super.stopSystem(system);
	}
}

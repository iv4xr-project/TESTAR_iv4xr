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
import org.testar.protocols.LabRecruitsProtocol;

import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.LabRecruitsAgentTESTAR;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.listener.GoalLibListener;
import eu.testar.iv4xr.listener.LabRecruitsEnvironmentListener;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;
import world.LabEntity;

/**
 * iv4xr EU H2020 project - LabRecruits Demo
 * 
 * Emotional example
 */
public class Protocol_labrecruits_emotional_agent extends LabRecruitsProtocol {

	GoalStructure goal;
	eu.testar.iv4xr.emotions.EmotionalCritic emotionalCritic;

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

		// Create an environment
		LabRecruitsEnvironmentListener labRecruitsEnvironment = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment);

		testAgent = new LabRecruitsAgentTESTAR(agentId) // matches the ID in the CSV file
				. attachState(new BeliefState())
				. attachEnvironment(labRecruitsEnvironment);
		
		// create EmotionalCritic
		emotionalCritic = new eu.testar.iv4xr.emotions.EmotionalCritic(testAgent);
		// Set needed value of EmotionalCritic
		// All existing WOM entities (this is not from point of view of users)
		emotionalCritic.known_entities = testAgent.getState().worldmodel.elements.size();

		// Set LabRecruits Agent
		GoalLibListener.setAgentId(agentId);
		GoalLibListener.setTestAgent(testAgent);

		// iv4xr Agent : define the testing-task
		goal = SEQ(GoalLibListener.entityInteracted("button1"));

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
		
		// First state (before beginSequence will be null)
		if(emotionalCritic != null) {
			// update EmotionalCritic
			emotionalCritic.update();
			// update State Model SocioEmotinal values
			state.set(IV4XRtags.agentPleasure, emotionalCritic.getPleasure());
			state.set(IV4XRtags.agentDominance, emotionalCritic.getDominance());
			state.set(IV4XRtags.agentArousal, emotionalCritic.getArousal());
		}
		
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
		// goal status should be success
		assertTrue(testAgent.success());
		// agent should be close to button1
        var agent_p  = testAgent.getState().worldmodel.getFloorPosition() ;
        var button_p = ((LabEntity) testAgent.getState().worldmodel.getElement("button1")).getFloorPosition() ;
        assertTrue(Vec3.dist(agent_p, button_p) < 0.5) ;
		super.stopSystem(system);
	}
}

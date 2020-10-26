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



import static nl.uu.cs.aplib.AplibEDSL.ABORT;
import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static nl.uu.cs.aplib.AplibEDSL.goal;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.actions.NOP;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.testar.protocols.DesktopProtocol;

import com.google.common.collect.Sets;

import agents.LabRecruitsTestAgent;
import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import communication.agent.AgentCommand;
import communication.system.Request;
import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import es.upv.staq.testar.CodingManager;
import es.upv.staq.testar.NativeLinker;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.world.WorldEntity;
import eu.testar.iv4xr.IV4XRProtocolUtil;
import eu.testar.iv4xr.actions.commands.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.listener.LabRecruitsEnvironmentListener;
import helperclasses.datastructures.Vec3;
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.Tactic.PrimitiveTactic;
import world.BeliefState;
import world.LegacyObservation;

/**
 * iv4XR introducing a Basic Agent in TESTAR protocols
 */
public class Protocol_labrecruits_agent_state_model extends DesktopProtocol {

	private String buttonToTest = "button1" ;
	private String doorToTest = "door1" ;
	private String agentId = "agent1";

	BeliefState beliefState;

	TestDataCollector dataCollector;
	LabRecruitsTestAgent labRecruitsTestAgent;

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
		//system.set(IV4XRtags.labRecruitsActions, deriveActions(system, state));
		
		Set<Action> actions = deriveActions(system, state);
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setDerivedActionsTESTAR(actions);
		
		// TODO: This should be invoked if we have intention to execute Actions in this method
		//notifyLabAgentStateToStateModel(state, actions);
		
		Util.pause(10);

		// Create an environment
		LabRecruitsEnvironmentListener labRecruitsEnvironment = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment);

		// create a belief state
		beliefState = new BeliefState();
		beliefState.id = "agent1"; // matches the ID in the CSV file
		beliefState.setEnvironment(labRecruitsEnvironment); // attach the environment

		// setting up a test-data collector:
		dataCollector = new TestDataCollector();

		// create a test agent
		labRecruitsTestAgent = new LabRecruitsTestAgent("agent1");
		labRecruitsTestAgent.attachState(beliefState); // State should be before environment
		labRecruitsTestAgent.attachEnvironment(labRecruitsEnvironment);

		// from agents.SimpleInteractionTest
		// define the test-goal:
		goal = SEQ(
				// Construct a goal structure that will make an agent to move towards the given entity and interact with it.
				// TacticLib.navigateTo(entityId) + TacticLib.interact(entityId)
				GoalLib.entityInteracted("button1"),
				// TESTARGoalLib.entityIsInteracted(system, "button1", "agent1"),

				// Create a test-goal to check the state of an in-game entity, whether it satisfies the given predicate.
				// Check if button isOn = if button is was active by last interaction
				GoalLib.entityInvariantChecked(labRecruitsTestAgent,
            		"button1", 
            		"button1 should be active", 
            		(WorldEntity e) -> e.getBooleanProperty("isOn")),
				/* TESTARGoalLib.entityInvariantChecked(labRecruitsTestAgent,
						"button1", 
						"button1 should be active", 
						(WorldEntity e) -> e.getBooleanProperty("isOn")), */

				// Is this Goal Structure going to move the agent to specific Door Entity ??
				// Or it is only used to calculate the nearest path and the movement will be executed with the next one ??
				//GoalLib.navigate_toNearestNode_toDoor("door1"),
				// TESTARGoalLib.navigate_toNearestNode_toDoor("door1"),

				// Construct a goal structure in which the agent will move to the in-game entity with the given id
				// TacticLib.navigateTo(entityId)
				GoalLib.entityStateRefreshed("door1"),
				// TESTARGoalLib.entityIsInRange(system, "door1").lift(),

				// Create a test-goal to check the state of an in-game entity, whether it satisfies the given predicate.
				// Check if door isOpen
				// Should this be check together with button1 check ??
				// Or do we suppose that is not in visible range and we need to move before ??
				GoalLib.entityInvariantChecked(labRecruitsTestAgent,
            		"door1", 
            		"door1 should be open", 
            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
				/* TESTARGoalLib.entityInvariantChecked(labRecruitsTestAgent,
						"door1", 
						"door1 should be open", 
						(WorldEntity e) -> e.getBooleanProperty("isOpen")) */

				);

		dataCollector.registerTestAgent(beliefState.id);
		labRecruitsTestAgent.setTestDataCollector(dataCollector).setGoal(goal) ;

		//goal not achieved yet
		assertFalse(labRecruitsTestAgent.success());
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
		labActions.add(new labActionCommandObserve(state, state, labRecruitsEnv, agentId, false, false));

		for(Widget w : state) {
			if(w.get(IV4XRtags.entityType, null) != null && w.get(IV4XRtags.entityType, null).toString().equals("Interactive")) {
				labActions.add(new labActionCommandMove(state, w, labRecruitsEnv, agentId, worldObservation.agentPosition, w.get(IV4XRtags.entityPosition), false, false, false));
				labActions.add(new labActionCommandInteract(state, w, labRecruitsEnv, agentId, buttonToTest, false, false));
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
		labRecruitsTestAgent.update();
		
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
		assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 2) ;
		// goal status should be success
		assertTrue(labRecruitsTestAgent.success());

		// close
		labRecruitsTestAgent.printStatus();
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).close();
		super.stopSystem(system);
		// Something is not being closed properly, for now we simplemente terminamos, un abrazo lobo
		Runtime.getRuntime().exit(0);
	}
}

/**
 * Layer to transform Agent Goal - SubGoals decisions into TESTAR Actions
 */
class TESTARGoalLib {

	public static GoalStructure entityIsInteracted(SUT system, String entityId, String agentId) {

		//move to the object
		Goal goal1 = goal(String.format("This entity is in interaction distance: [%s]", entityId))
				. toSolve((BeliefState belief) -> belief.canInteract(entityId));

		//interact with the object
		Goal goal2 = goal(String.format("This entity is interacted: [%s]", entityId))
				. toSolve((BeliefState belief) -> true);

		//Set the tactics with which the goals will be solved
		GoalStructure g1 = goal1.withTactic(
				FIRSTof( //the tactic used to solve the goal
						TacticLib.navigateTo(entityId), //try to move to the entity
						TacticLib.explore(), //find the entity
						ABORT())) 
				.lift();

		GoalStructure g2 = goal2.withTactic(
				FIRSTof( //the tactic used to solve the goal
						TacticLib.interact(entityId),// interact with the entity
						ABORT())) // observe the objects
				.lift();

		return SEQ(g1, g2);
	}

	public static Goal entityIsInRange(SUT system, String entityId) {

		Goal goal = new Goal(String.format("This entity is in-range: [%s]",entityId))

				. toSolve((BeliefState belief) -> belief.withinViewRange(entityId));

		//define the goal structure
		Goal g = goal.withTactic(
				FIRSTof( //the tactic used to solve the goal
						TacticLib.navigateTo(entityId),//try to move to the entity
						TacticLib.explore(),//find the entity
						ABORT()));
		return g;
	}


	public static GoalStructure entityInvariantChecked(TestAgent agent, String id, String info, Predicate<WorldEntity> predicate){
		return GoalLib.entityInvariantChecked(agent, id, info, predicate);
	}

	// OLD: To check and remove
	/*public static GoalStructure navigate_toNearestNode_toDoor(String doorId) {
		return GoalLib.navigate_toNearestNode_toDoor(doorId);
	}*/

}

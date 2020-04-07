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


import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static nl.uu.cs.aplib.AplibEDSL.goal;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.actions.NOP;
import org.testar.protocols.DesktopProtocol;

import agents.GymAgent;
import agents.tactics.GoalStructureFactory;
import agents.tactics.TacticsFactory;
import environments.EnvironmentConfig;
import environments.GymEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import helperclasses.datastructures.linq.QArrayList;
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;
import world.Entity;
import world.InteractiveEntity;

/**
 * iv4XR introducing a Basic Agent in TESTAR protocols
 */
public class Protocol_labrecruits_demo extends DesktopProtocol {

	private String buttonToTest = "button1" ;
	private String doorToTest = "door1" ;

	GymEnvironment environment;
	BeliefState beliefState;

	TestDataCollector dataCollector;
	GymAgent testAgent;

	GoalStructure goal;

	@Override
	protected SUT startSystem() {
		SUT sut = super.startSystem();

		Util.pause(10);

		// Create an environment
		environment = new GymEnvironment(new EnvironmentConfig("button1_opens_door1", "suts/levels"));

		// presses "Play" in the game for you
		environment.startSimulation(); 

		// create a belief state
		beliefState = new BeliefState();
		beliefState.id = "agent1"; // matches the ID in the CSV file
		beliefState.setEnvironment(environment); // attach the environment

		// setting up a test-data collector:
		dataCollector = new TestDataCollector();

		// create a test agent
		testAgent = new GymAgent(beliefState);

		// define the test-goal:
		goal = SEQ(
				// get the first observation:	
				MySubGoals_labrecruits_demo.justObserve(),
				// (0) We first check the pre-condition of this test:
				//       Observe that the button is inactive and the door is closed.
				//       If this is the case we continue the test. 
				//       Else it is not sensical to do this test, so we will abort 
				//       (there is something wrong with the scenario setup; this should be fixed first),  
				GoalStructureFactory.inspect(buttonToTest, (Entity e) -> (e instanceof InteractiveEntity) && !((InteractiveEntity) e).isActive),
				GoalStructureFactory.inspect(doorToTest, (Entity e) -> (e instanceof InteractiveEntity) && !((InteractiveEntity) e).isActive),

				// now the test itself:

				// (1a) walk to the button
				GoalStructureFactory.reachObject(buttonToTest).lift(),
				// (1b) and then press the button
				MySubGoals_labrecruits_demo.pressButton(buttonToTest),

				// (2) now we should check that the button is indeed in its active state, and 
				// the door is open:
				GoalStructureFactory.checkEntityInvariant(testAgent,
						buttonToTest, 
						"button should be active", 
						(Entity e) -> (e instanceof InteractiveEntity) && ((InteractiveEntity) e).isActive),
				GoalStructureFactory.checkEntityInvariant(testAgent,
						doorToTest, 
						"door should be open", 
						(Entity e) -> (e instanceof InteractiveEntity) && ((InteractiveEntity) e).isActive)
				);

		dataCollector.registerTestAgent(beliefState.id);
		testAgent.setTestDataCollector(dataCollector).setGoal(goal) ;


		return sut;
	}

	@Override
	protected void beginSequence(SUT system, State TESTARstate) {
		//goal not achieved yet
		assertFalse(testAgent.success());
	}

	@Override
	protected State getState(SUT system) {
		Logging.getAPLIBlogger().info("STATE INFO:");
		goal.printGoalStructureStatus();

		return super.getState(system);
	}

	@Override
	protected Verdict getVerdict(State state) {
		return Verdict.OK;
	}

	@Override
	protected Set<Action> deriveActions(SUT system, State state) {

		Set<Action> empty = new HashSet<>();
		Action nop = new NOP();
		nop.set(Tags.Role, Roles.System);
		nop.set(Tags.OriginWidget, state);
		empty.add(nop);

		return empty;
	}

	@Override
	protected Action selectAction(State state, Set<Action> actions){
		Action nop = new NOP();
		nop.set(Tags.Role, Roles.System);
		nop.set(Tags.OriginWidget, state);
		return nop;
	}

	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		testAgent.update();
		return true;
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
		assertTrue(testAgent.success());

		// close
		testAgent.printStatus();
		environment.close();
		super.stopSystem(system);
		// Something is not being closed properly, for now we simplemente terminamos, un abrazo lobo
		Runtime.getRuntime().exit(0);
	}
}

/**
 * A helper class for constructing support subgoals.
 */
class MySubGoals_labrecruits_demo {

	// to just observe the game ... to get information
	static GoalStructure justObserve(){
		return goal("observe").toSolve((BeliefState b) -> b.position != null).withTactic(TacticsFactory.observe()).lift();
	}

	static GoalStructure observeInteractiveEntity(String interactiveEntityId, boolean isActive) {
		String goalName = "Observe that " + interactiveEntityId + " is " + (isActive ? "" : "not ") + "active";
		return goal(goalName)
				.toSolve((BeliefState belief) -> {
					System.out.println(goalName);
					var interactiveEntities = new QArrayList<>(belief.getAllInteractiveEntities());
					return interactiveEntities.contains(entity -> entity.id.equals(interactiveEntityId) && entity.isActive == isActive);
				})
				// in the future this will be swapped by inspect(objectId) for when an object is not within sight
				.withTactic(TacticsFactory.observe())
				.lift()
				.maxbudget(1);
	}

	// A goal that is reached whenever the buttonId is observed to be pressed (active)
	static GoalStructure pressButton(String buttonId) {
		return
				goal("Press " + buttonId)
				.toSolve((BeliefState belief) -> {
					// the belief should contain an interactive entity (buttonId) that is observed to be pressed (active)
					var interactiveEntities = new QArrayList<>(belief.getAllInteractiveEntities());
					return interactiveEntities.contains(entity -> entity.id.equals(buttonId) && entity.isActive);
				})
				.withTactic(
						FIRSTof(
								// try to interact
								TacticsFactory.interact(buttonId),
								// move toward the button if the agent cannot interact
								TacticsFactory.move(buttonId)
								)
						).lift();
	}

	// this will be the top level goal
	static GoalStructure test_thisButton_triggers_thatObject(String buttonId, String target) {
		return SEQ(
				justObserve(),
				// observe the button to be inactive and the door to be closed
				GoalStructureFactory.inspect(buttonId, (Entity e) -> (e instanceof InteractiveEntity) && !((InteractiveEntity) e).isActive),
				GoalStructureFactory.inspect(target, (Entity e) -> (e instanceof InteractiveEntity) && !((InteractiveEntity) e).isActive),
				// walk to the button
				GoalStructureFactory.reachObject(buttonId).lift(),
				// press the button
				pressButton(buttonId),
				// observe the button to be active and the door to be open
				GoalStructureFactory.inspect(buttonId, (Entity e) -> (e instanceof InteractiveEntity) && ((InteractiveEntity) e).isActive),
				GoalStructureFactory.inspect(target, (Entity e) -> (e instanceof InteractiveEntity) && ((InteractiveEntity) e).isActive)
				);
	}
}
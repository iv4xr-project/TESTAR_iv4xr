/***************************************************************************************************
 *
 * Copyright (c) 2019 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019 Open Universiteit - www.ou.nl
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

import java.util.Set;

import org.fruit.alayer.*;
import org.testar.protocols.DesktopProtocol;

import agents.GymAgent;
import agents.tactics.GoalStructureFactory;
import agents.tactics.TacticsFactory;
import environments.EnvironmentConfig;
import environments.GymEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import helperclasses.datastructures.linq.QArrayList;
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

	@Override
	protected SUT startSystem() {
		return new labrecruits_demo.labRecruitsSUT();
	}

	@Override
	protected void beginSequence(SUT system, State TESTARstate) {
		super.beginSequence(system, TESTARstate);

		// Create an environment
		GymEnvironment environment = new GymEnvironment(new EnvironmentConfig("button1_opens_door1"));

		try {
			environment.startSimulation(); // presses "Play" in the game for you

			// create a belief state
			var state = new BeliefState();
			state.id = "agent1"; // matches the ID in the CSV file
			state.setEnvironment(environment); // attach the environment

			// setting up a test-data collector:
			var dataCollector = new TestDataCollector();

			// create a test agent
			var testAgent = new GymAgent(state) ;

			// define the test-goal:
			var goal = SEQ(
					// get the first observation:	
					MySubGoals.justObserve(),
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
					MySubGoals.pressButton(buttonToTest),

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

			testAgent . setTestDataCollector(dataCollector) . setGoal(goal) ;

			//goal not achieved yet
			assertFalse(testAgent.success());

			// keep updating the agent
			while (goal.getStatus().inProgress()) {
				testAgent.update();
			}

			// check that we have passed both tests above:
			assertTrue(dataCollector.getNumberOfPassVerdictsSeen() == 2) ;
			// goal status should be success
			assertTrue(testAgent.success());

			// close
			testAgent.printStatus();
		}
		finally {
			stopSystem(system);
			environment.close();
			// Something is not being closed properly, for now we simplemente terminamos, un abrazo lobo
			Runtime.getRuntime().exit(0);
		}
	}

	@Override
	protected State getState(SUT system) {
		return super.getState(system);
	}

	@Override
	protected Verdict getVerdict(State state) {
		return Verdict.OK;
	}

	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		return super.deriveActions(system, state);
	}

	@Override
	protected Action selectAction(State state, Set<Action> actions){
		return super.selectAction(state, actions);
	}

	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		return true;
	}

	@Override
	protected boolean moreActions(State state) {
		return true;
	}

	@Override
	protected void finishSequence() {
		super.finishSequence();
	}

	@Override
	protected void stopSystem(SUT system) {
		super.stopSystem(system);
	}
}

/**
 * A helper class for constructing support subgoals.
 */
class MySubGoals {

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

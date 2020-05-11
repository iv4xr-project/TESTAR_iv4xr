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

import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.actions.NOP;
import org.testar.protocols.DesktopProtocol;

import agents.LabRecruitsTestAgent;
import agents.tactics.GoalLib;
import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.world.WorldEntity;
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;

/**
 * iv4XR introducing a Basic Agent in TESTAR protocols
 */
public class Protocol_labrecruits_demo extends DesktopProtocol {

	private String buttonToTest = "button1" ;
	private String doorToTest = "door1" ;

	LabRecruitsEnvironment labRecruitsEnvironment;
	BeliefState beliefState;

	TestDataCollector dataCollector;
	LabRecruitsTestAgent labRecruitsTestAgent;

	GoalStructure goal;

	@Override
	protected SUT startSystem() {
		SUT sut = super.startSystem();

		Util.pause(10);

		// Create an environment
		labRecruitsEnvironment = new LabRecruitsEnvironment(new EnvironmentConfig("button1_opens_door1", "suts/levels"));

		// presses "Play" in the game for you
		labRecruitsEnvironment.startSimulation(); 

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
        var goal = SEQ(
        	GoalLib.entityIsInteracted("button1"),
            GoalLib.entityInvariantChecked(labRecruitsTestAgent,
            		"button1", 
            		"button1 should be active", 
            		(WorldEntity e) -> e.getBooleanProperty("isOn")),

            GoalLib.navigate_toNearestNode_toDoor("door1"),
            GoalLib.entityIsInRange("door1").lift(),
            
            
            GoalLib.entityInvariantChecked(labRecruitsTestAgent,
            		"door1", 
            		"door1 should be open", 
            		(WorldEntity e) -> e.getBooleanProperty("isOpen"))
            
        );

		dataCollector.registerTestAgent(beliefState.id);
		labRecruitsTestAgent.setTestDataCollector(dataCollector).setGoal(goal) ;


		return sut;
	}

	@Override
	protected void beginSequence(SUT system, State TESTARstate) {
		//goal not achieved yet
		assertFalse(labRecruitsTestAgent.success());
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
		labRecruitsTestAgent.update();
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
		assertTrue(labRecruitsTestAgent.success());

		// close
		labRecruitsTestAgent.printStatus();
		labRecruitsEnvironment.close();
		super.stopSystem(system);
		// Something is not being closed properly, for now we simplemente terminamos, un abrazo lobo
		Runtime.getRuntime().exit(0);
	}
}

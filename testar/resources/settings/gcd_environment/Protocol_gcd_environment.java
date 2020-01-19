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


import org.fruit.alayer.Action;
import org.fruit.alayer.Roles;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.actions.NOP;
import org.fruit.monkey.DefaultProtocol;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.agents.StateWithMessenger;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import static eu.iv4xr.framework.Iv4xrEDSL.* ;

import static nl.uu.cs.aplib.AplibEDSL.action;

import java.util.HashSet;
import java.util.Set;

import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;

import gcd_environment.*;

public class Protocol_gcd_environment extends DefaultProtocol {

	private GCDEnvironment environmentSUT;

	static class GCDState extends StateWithMessenger {
		GCDState(){ super() ; }
		@Override
		public GCDEnvironment env() { return (GCDEnvironment) super.env(); }
	}

	private TestAgent agent;

	int counter = 0;
	int X,Y;
	int expectedGCD = 1;
	boolean expectedWin = true;

	@Override
	protected SUT startSystem() {
		// (1) initialize the program-under-test:
		environmentSUT = new GCDEnvironment();
		environmentSUT.newGameUnderTest();
		Logging.getAPLIBlogger().info("STARTING a new test. Initial state: (" + environmentSUT.getX() + ", " + environmentSUT.getY() + ")");

		X = 1;
		Y = 1;

		return environmentSUT;
	}

	@Override
	protected void beginSequence(SUT system, State state){
		// (2) Create a fresh state + environment for the test agent; attach the game to the env:
		GCDState stateGCD = new GCDState();
		stateGCD.setEnvironment(environmentSUT);

		// (3) Create your test agent; attach the just created state to it:
		agent = new TestAgent().attachState(stateGCD);

		//environmentSUT.set(GameTags.TestAgent, agent);
	}

	@Override
	protected State getState(SUT system) {
		Logging.getAPLIBlogger().info(environmentSUT.getStatus());
		return super.getState(system);
	}

	@Override
	protected Verdict getVerdict(State state) {
		return Verdict.OK;
	}

	@Override
	protected Set<Action> deriveActions(SUT system, State state) {

		nl.uu.cs.aplib.mainConcepts.Action up = action("action_up")
				. do1((GCDState S)-> { 
					S.env().sendCommand(null, null, "up",null);  
					Logging.getAPLIBlogger().info("new state: " + S.env());
					return S ; }) ;
		nl.uu.cs.aplib.mainConcepts.Action down = action("action_down")
				. do1((GCDState S)-> { 
					S.env().sendCommand(null, null, "down",null);  
					Logging.getAPLIBlogger().info("new state: " + S.env());
					return S ; }) ;
		nl.uu.cs.aplib.mainConcepts.Action right = action("action_up")
				. do1((GCDState S)-> { 
					S.env().sendCommand(null, null, "right",null);  
					Logging.getAPLIBlogger().info("new state: " + S.env());
					return S ; }) ;
		nl.uu.cs.aplib.mainConcepts.Action left = action("action_left")
				. do1((GCDState S)-> { 
					S.env().sendCommand(null, null, "left",null);  
					Logging.getAPLIBlogger().info("new state: " + S.env());
					return S ; }) ;

		Tactic t = FIRSTof(up   .on_((GCDState S) -> (S.env()).getY() < Y).lift(),
				down .on_((GCDState S) -> (S.env()).getY() > Y).lift(),
				right.on_((GCDState S) -> (S.env()).getX() < X).lift(),	  
				left .on_((GCDState S) -> (S.env()).getX() > X).lift() );

		PrimitiveGoal topgoal = testgoal("tg")
				// the goal is to drive the game to get it to position (X,Y):
				. toSolve((GCDState S) -> S.env().getX()==X && S.env().getY()==Y ) 
				// specify the tactic to solve the above goal:
				//. withTactic(environmentSUT.get(GameTags.agentTactic))
				. withTactic(t)
				// assert the correctness property that must hold on the state where the goal is solved; 
				// we will check that the gcd field and win() have correct values:
				/*. oracle(environmentSUT.get(GameTags.TestAgent), (GCDState S) -> assertTrue_("","",
						S.env().getGcd() == expectedGCD && S.env().isWin() == expectedWin))*/
				. oracle(agent, (GCDState S) -> assertTrue_("","",
						S.env().getGcd() == expectedGCD && S.env().isWin() == expectedWin))
				// finally we lift the goal to become a GoalStructure, for technical reason.
				. lift() ;

		agent.setGoal(topgoal);

		//environmentSUT.get(GameTags.TestAgent).setGoal(topgoal);
		//environmentSUT.set(GameTags.agentTactic, t);

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
		//environmentSUT.get(GameTags.TestAgent).update();
		agent.update();
		counter ++;
		return true;
	}

	@Override
	protected boolean moreActions(State state) {
		environmentSUT.refreshWorker();
		if (!environmentSUT.getGameUnderTest().win()) {
			Logging.getAPLIBlogger().info("Continue moving");
			return true;
		}
		return false;
	}

	@Override
	protected void finishSequence() {
		agent.stop();
		Logging.getAPLIBlogger().info("Finish game with " + counter + " steps");
	}

	@Override
	protected void stopSystem(SUT system) {
		//
	}

}

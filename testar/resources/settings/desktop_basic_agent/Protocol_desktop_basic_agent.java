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


import static nl.uu.cs.aplib.AplibEDSL.action;
import static nl.uu.cs.aplib.AplibEDSL.goal;

import java.util.Set;

import org.fruit.alayer.*;
import org.fruit.monkey.DefaultProtocol;

import nl.uu.cs.aplib.environments.ConsoleEnvironment;
import nl.uu.cs.aplib.mainConcepts.BasicAgent;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.mainConcepts.Tactic.PrimitiveTactic;

/**
 * iv4XR introducing a Basic Agent in TESTAR protocols
 */
public class Protocol_desktop_basic_agent extends DefaultProtocol {

	private BasicAgent agent;
	
	@Override
	protected SUT startSystem() {
		return super.startSystem();
	}
	
	@Override
	protected void beginSequence(SUT system, State state) {
		super.beginSequence(system, state);

		/** Create the Agent with the Tactic to test the SUT **/
		
		agent = new BasicAgent();
		
		agent.attachState(new SimpleState().setEnvironment(new ConsoleEnvironment()));
		
		Goal g = goal("Find the Title (Winner)").toSolve(p -> exists(getState(system), "Winner"));

		// defining a single action as the goal solver:
		PrimitiveTactic guessing = action("guessing")
				.do1((SimpleState belief) -> { 
					//Randomly choose an existing action
					Action a = selectAction(state, deriveActions(system, getState(system)));
					((ConsoleEnvironment) belief.env()).println("Proposing " + a.get(Tags.Desc,"") + " ...");
					//Execute TESTAR random action
					executeAction(system, getState(system), a);
					return a ;
				})
				.lift() ;

		// attach the action to the goal, and make it a goal-structure:
		GoalStructure topgoal = g.withTactic(guessing).lift() ;

		agent.setGoal(topgoal);
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
		agent.update();
		return true;
	}

	@Override
	protected boolean moreActions(State state) {
		return agent.getLastHandledGoal().getStatus().inProgress();
	}

	@Override
	protected void finishSequence() {
		agent.getLastHandledGoal().printGoalStructureStatus();
	}

	@Override
	protected void stopSystem(SUT system) {
		//
	}
	
	private boolean exists(State state, String title) {
		if(title!=null && !title.isEmpty())
			for(Widget w : state)
				if(w.get(Tags.Title,"").equals(title))
					return true;
		return false;
	}
}

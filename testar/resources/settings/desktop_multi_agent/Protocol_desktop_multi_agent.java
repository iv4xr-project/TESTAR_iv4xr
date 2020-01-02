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
import static org.fruit.alayer.Tags.Blocked;
import static org.fruit.alayer.Tags.Enabled;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.fruit.Assert;
import org.fruit.alayer.*;
import org.fruit.alayer.actions.AnnotatingActionCompiler;
import org.fruit.alayer.actions.StdActionCompiler;
import org.fruit.alayer.exceptions.*;
import org.fruit.alayer.windows.UIATags;
import org.fruit.monkey.Settings;
import org.fruit.monkey.RuntimeControlsProtocol.Modes;
import org.testar.protocols.DesktopProtocol;

import nl.uu.cs.aplib.agents.AutonomousBasicAgent;
import nl.uu.cs.aplib.agents.StateWithMessenger;
import nl.uu.cs.aplib.environments.ConsoleEnvironment;
import nl.uu.cs.aplib.mainConcepts.BasicAgent;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.multiAgentSupport.ComNode;

/**
 * iv4XR introducing Multi Agents in TESTAR protocols
 */
public class Protocol_desktop_multi_agent extends DesktopProtocol {

	static public class MyState extends StateWithMessenger{
		@Override
		public ConsoleEnvironment env() { return (ConsoleEnvironment) super.env() ; }
	}

	@Override
	protected void beginSequence(SUT system, State state) {
		super.beginSequence(system, state);

		Goal g = goal("Find the Title (Fin de partida)").toSolve(p -> exists(getState(system), "Fin de partida"));

		// defining a single action as the goal solver:
		var guessing = action("guessing")
				.do1((MyState belief) -> { 
					Action a = selectAction(state, deriveActions(system, getState(system)));
					belief.env().println(" Select " + selectedButton(a.get(Tags.OriginWidget).get(Tags.Path,"")));
					executeAction(system, getState(system), a);
					return a ;
				})
				.lift() ;

		// attach the action to the goal, and make it a goal-structure:
		GoalStructure topgoal = g.withTactic(guessing).lift() ;

		//var comNode = new ComNode() ;
		var gameState = new MyState() ;
		gameState.setEnvironment(new ConsoleEnvironment()) ;

		// creating an agent; attaching a fresh state to it, and attaching the above goal to it:
		var agentX = new AutonomousBasicAgent("AX", "Agent X")
				. attachState(gameState) 
				. setGoal(topgoal) 
				. setSamplingInterval(1000) 
				//. registerTo(comNode) ;
				;

		var agentO = new AutonomousBasicAgent("AO", "Agent O")
				. attachState(gameState) 
				. setGoal(topgoal) 
				. setSamplingInterval(1000) 
				//. registerTo(comNode) ;
				;

		while (topgoal.getStatus().inProgress()) {
			if(turno(getState(system)).contains("X")) {
				gameState.env().println("Turno de X ...");
				agentX.update();
			}
			else {
				gameState.env().println("Turno de O ...");
				agentO.update();
			}

			synchronized (this) {
				try {
					this.wait(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(mode()==Modes.Quit)
				break;
		}
		
		topgoal.printGoalStructureStatus();

		this.mode = Modes.Quit;

		// run the agent, autonomously on its own thread:
		/*new Thread(() -> agentX.loop()) . start();
		new Thread(() -> agentO.loop()) . start();

		var gtX = agentX.waitUntilTheGoalIsConcluded() ;
		var gtO = agentX.waitUntilTheGoalIsConcluded() ;

		gtX.printGoalStructureStatus();
		gtO.printGoalStructureStatus();

		agentX.stop();
		agentO.stop();

		this.mode = Modes.Quit;*/
	}

	private boolean exists(State state, String title) {
		if(title!=null && !title.isEmpty())
			for(Widget w : state)
				if(w.get(Tags.Title,"").equals(title))
					return true;
		return false;
	}

	@Override
	protected Set<Action> deriveActions(SUT system, State state) throws ActionBuildException {
		Set<Action> actions = new HashSet<>();

		StdActionCompiler ac = new AnnotatingActionCompiler();

		if(!state.get(Tags.Foreground, true) && system.get(Tags.SystemActivator, null) != null){
			this.forceToForeground = true;
			return actions;
		}

		for(Widget w : state){
			// Only interact with non-clicked (empty value) widgets
			if(w.get(Tags.Title,"").isEmpty() && isClickable(w)) {
				actions.add(ac.leftClickAt(w));
			}
		}
		return actions;
	}
	
	private String selectedButton(String path) {
		if(path.contains("1"))
			return "Down-Left";
		else if(path.contains("2"))
			return "Down-Mid";
		else if(path.contains("3"))
			return "Down-Right";
		else if(path.contains("4"))
			return "Mid-Left";
		else if(path.contains("5"))
			return "Mid-Mid";
		else if(path.contains("6"))
			return "Mid-Right";
		else if(path.contains("7"))
			return "Upper-Left";
		else if(path.contains("8"))
			return "Upper-Mid";
		else if(path.contains("9"))
			return "Upper-Right";
		else
			return "Unknown";
	}

	private String turno(State state) {
		for(Widget w : state) {
			if(w.get(Tags.Title,"").contains("Turno de X"))
				return "X";
			if(w.get(Tags.Title,"").contains("Turno de O"))
				return "O";
		}
		return "";
	}
}

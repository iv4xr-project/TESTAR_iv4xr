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


import static nl.uu.cs.aplib.AplibEDSL.ANYof;
import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;
import static nl.uu.cs.aplib.AplibEDSL.action;
import static nl.uu.cs.aplib.AplibEDSL.goal;
import java.util.HashSet;
import java.util.Set;

import org.fruit.alayer.*;
import org.fruit.alayer.actions.AnnotatingActionCompiler;
import org.fruit.alayer.actions.StdActionCompiler;
import org.fruit.monkey.DefaultProtocol;

import nl.uu.cs.aplib.agents.AutonomousBasicAgent;
import nl.uu.cs.aplib.agents.StateWithMessenger;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.mainConcepts.Tactic.PrimitiveTactic;
import nl.uu.cs.aplib.multiAgentSupport.ComNode;
import nl.uu.cs.aplib.multiAgentSupport.Message.MsgCastType;

/**
 * iv4XR introducing Multi Agents in TESTAR protocols
 */
public class Protocol_desktop_multi_agent extends DefaultProtocol {

	static class MyState extends StateWithMessenger {
		int counter = 0 ;
		@Override
		public MyState setEnvironment(Environment env) {
			super.setEnvironment(env) ;
			return this ;
		}
	}
	
	private AutonomousBasicAgent agentX, agentO;
	
	@Override
	protected SUT startSystem() {
		return super.startSystem();
	}

	@Override
	protected void beginSequence(SUT system, State state) {
		super.beginSequence(system, state);

		ComNode comNode = new ComNode() ;
		MyState gameState = new MyState().setEnvironment(new Environment()) ;
		
		/** Create the Agents with the Tactics to test the SUT **/
		
		agentX = new AutonomousBasicAgent("AX", "Agent X")
				. attachState(gameState)
				. setSamplingInterval(1000) 
				. registerTo(comNode) ;
		;
		
		agentO = new AutonomousBasicAgent("AO", "Agent O")
				. attachState(gameState)
				. setSamplingInterval(1000) 
				. registerTo(comNode) ;
		;
		
		// Opening Action, Game always start with Agent X turn
		PrimitiveTactic openingX = action("opening Turn X")
				.do1((MyState S) -> {
		        	 Action a = selectAction(state, deriveActions(system, getState(system)));
		        	 System.out.println("Turno de X");
		        	 System.out.println(" Select " + selectedButton(a.get(Tags.OriginWidget).get(Tags.Path,"")));
					 executeAction(system, getState(system), a);
		             S.messenger().send("AX", 0, MsgCastType.SINGLECAST, "AO", "Agent X Turn End") ;
		             return ++S.counter ;
			  })
			  .lift();
		
		PrimitiveTactic aX = action("aTypeX")
		         . do1((MyState S)-> {
		        	 S.messenger().retrieve(M -> M.getMsgName().equals("Agent O Turn End")) ;
		        	 Action a = selectAction(state, deriveActions(system, getState(system)));
		        	 System.out.println("Turno de X");
		        	 System.out.println(" Select " + selectedButton(a.get(Tags.OriginWidget).get(Tags.Path,"")));
					 executeAction(system, getState(system), a);
		             S.messenger().send("AX", 0, MsgCastType.SINGLECAST, "AO", "Agent X Turn End") ;
		             return ++S.counter ;
		         })
		         . on_((MyState S) -> S.messenger().has(M -> M.getMsgName().equals("Agent O Turn End")))
		         . lift();
		
		PrimitiveTactic aO = action("aTypeO")
				.do1((MyState S)-> {
					 S.messenger().retrieve(M -> M.getMsgName().equals("Agent X Turn End")) ;
					 Action a = selectAction(state, deriveActions(system, getState(system)));
					 System.out.println("Turno de O");
					 System.out.println(" Select " + selectedButton(a.get(Tags.OriginWidget).get(Tags.Path,"")));
					 executeAction(system, getState(system), a);
		             S.messenger().send("AO", 0, MsgCastType.SINGLECAST, "AX", "Agent O Turn End") ;
		             return ++S.counter ;
				})
				. on_((MyState S) -> S.messenger().has(M -> M.getMsgName().equals("Agent X Turn End")))
				.lift();
		
		// GOAL Agent X, with opening turn
		Goal gX = goal("Find the Title (Fin de partida)").toSolve(p -> exists(getState(system), "Fin de partida")) ;
		gX.withTactic(
		    	FIRSTof(
		    			openingX.on_((MyState S) -> S.counter == 0) ,
		    			ANYof(aX)
			    	)) ;
		
		GoalStructure topgoalX = gX.lift();
		agentX.setGoal(topgoalX);

		//GOAL Agent O
		Goal gO = goal("Find the Title (Fin de partida)").toSolve(p -> exists(getState(system), "Fin de partida")) ;
		gO.withTactic(
		    			ANYof(aO)
			    	) ;
		
		GoalStructure topgoalO = gO.lift();
		agentO.setGoal(topgoalO);
	}

	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
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
	
	@Override
	protected Action selectAction(State state, Set<Action> actions){
		return super.selectAction(state, actions);
	}

	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		agentX.update();
		agentO.update();
		return true;
	}

	@Override
	protected boolean moreActions(State state) {
		return (agentX.getLastHandledGoal().getStatus().inProgress() && agentO.getLastHandledGoal().getStatus().inProgress());
	}

	@Override
	protected void finishSequence() {
		if(agentX.getLastHandledGoal().getStatus().inProgress())
			System.out.println("\n ******************** \n WINNER: O Agent \n ******************** \n");
		else
			System.out.println("\n ******************** \n WINNER: X Agent \n ******************** \n");
			
		agentX.stop();
		agentO.stop();
		
		agentX.getLastHandledGoal().printGoalStructureStatus();
		agentO.getLastHandledGoal().printGoalStructureStatus();
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

}

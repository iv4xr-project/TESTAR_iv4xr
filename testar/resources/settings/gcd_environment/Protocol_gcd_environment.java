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


import org.fruit.Pair;
import org.fruit.alayer.Action;
import org.fruit.alayer.AutomationCache;
import org.fruit.alayer.Roles;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.actions.AnnotatingActionCompiler;
import org.fruit.alayer.actions.NOP;
import org.fruit.alayer.actions.StdActionCompiler;
import org.fruit.alayer.exceptions.ActionBuildException;
import org.fruit.alayer.exceptions.NoSuchTagException;
import org.fruit.alayer.exceptions.StateBuildException;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.alayer.exceptions.SystemStopException;
import org.fruit.monkey.AbstractProtocol;
import org.fruit.monkey.DefaultProtocol;
import org.fruit.monkey.RuntimeControlsProtocol.Modes;
import org.fruit.monkey.Settings;

import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.agents.StateWithMessenger;
import nl.uu.cs.aplib.mainConcepts.Environment;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import static eu.iv4xr.framework.Iv4xrEDSL.* ;

import static nl.uu.cs.aplib.AplibEDSL.action;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;

import gcd_environment.GameGCDAction;
import gcd_environment.GCDEnvironment;

public class Protocol_gcd_environment extends DefaultProtocol {

	private GCDEnvironment environmentSUT;
	
	static class MyState extends StateWithMessenger {
		MyState(){ super() ; }
		@Override
		public GCDEnvironment env() { return (GCDEnvironment) super.env(); }
	}

	int counter = 0;

	@Override
	protected SUT startSystem() {
		// (1) initialize the program-under-test:
		environmentSUT = new GCDEnvironment();
		environmentSUT.newGameUnderTest();
		Logging.getAPLIBlogger().info("STARTING a new test. Initial state: (" + environmentSUT.getX() + ", " + environmentSUT.getY() + ")");
		
		// (2) Create a fresh state + environment for the test agent; attach the game to the env:
		var state = (MyState) (new MyState().setEnvironment(environmentSUT)) ;	

		// (3) Create your test agent; attach the just created state to it:
		var agent = new TestAgent().attachState(state);

		return environmentSUT;
	}

	@Override
	protected void beginSequence(SUT system, State state){
		//
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

		Set<Action> empty = new HashSet<>();

		nl.uu.cs.aplib.mainConcepts.Action up = action("action_up")
				. do1((MyState S)-> { 
					S.env().sendCommand(null, null, "up",null);  
					Logging.getAPLIBlogger().info("new state: " + S.env());
					return S ; }) ;
		nl.uu.cs.aplib.mainConcepts.Action down = action("action_down")
				. do1((MyState S)-> { 
					S.env().sendCommand(null, null, "down",null);  
					Logging.getAPLIBlogger().info("new state: " + S.env());
					return S ; }) ;
		nl.uu.cs.aplib.mainConcepts.Action right = action("action_up")
				. do1((MyState S)-> { 
					S.env().sendCommand(null, null, "right",null);  
					Logging.getAPLIBlogger().info("new state: " + S.env());
					return S ; }) ;
		nl.uu.cs.aplib.mainConcepts.Action left = action("action_left")
				. do1((MyState S)-> { 
					S.env().sendCommand(null, null, "left",null);  
					Logging.getAPLIBlogger().info("new state: " + S.env());
					return S ; }) ;

		empty.add(new GameGCDAction(up, state));
		empty.add(new GameGCDAction(down, state));
		empty.add(new GameGCDAction(right, state));
		empty.add(new GameGCDAction(left, state));

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
		if(environmentSUT.getX() > 1) {
			environmentSUT.getGameUnderTest().left();
			Logging.getAPLIBlogger().info("LEFT move");
		}

		else if(environmentSUT.getX() < 1) {
			environmentSUT.getGameUnderTest().right();
			Logging.getAPLIBlogger().info("RIGHT move");
		}

		else if(environmentSUT.getY() > 1) {
			environmentSUT.getGameUnderTest().down();
			Logging.getAPLIBlogger().info("DOWN move");
		}

		else if(environmentSUT.getY() < 1) {
			environmentSUT.getGameUnderTest().up();
			Logging.getAPLIBlogger().info("UP move");
		}

		else
			return false;

		counter ++;
		return true;
	}

	@Override
	protected boolean moreActions(State state) {
		if (environmentSUT.getX() != 1 || environmentSUT.getY() != 1) {
			Logging.getAPLIBlogger().info("Continue moving");
			return true;
		}
		/*if (!environmentSUT.getGameUnderTest().win()) {
			Logging.getAPLIBlogger().info("Continue moving");
			return true;
		}*/
		return false;
	}

	@Override
	protected void finishSequence() {
		Logging.getAPLIBlogger().info("Finish game with " + counter + " steps");
	}

	@Override
	protected void stopSystem(SUT system) {
		//
	}

}

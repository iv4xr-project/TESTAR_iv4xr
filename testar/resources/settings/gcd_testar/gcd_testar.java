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
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.agents.StateWithMessenger;
import java.util.HashSet;
import java.util.Set;

import gcd_testar.GCDEnvironment;

public class gcd_testar extends DefaultProtocol {

	private GCDEnvironment environmentSUT;

	int counter = 0;
	int X,Y;

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
		if(environmentSUT.getX() > 1) {
			environmentSUT.sendCommand(null, null, "left",null);
			Logging.getAPLIBlogger().info("LEFT move");
		}

		else if(environmentSUT.getX() < 1) {
			environmentSUT.sendCommand(null, null, "right",null);
			Logging.getAPLIBlogger().info("RIGHT move");
		}

		else if(environmentSUT.getY() > 1) {
			environmentSUT.sendCommand(null, null, "down",null);
			Logging.getAPLIBlogger().info("DOWN move");
		}

		else if(environmentSUT.getY() < 1) {
			environmentSUT.sendCommand(null, null, "up",null);
			Logging.getAPLIBlogger().info("UP move");
		}

		else
			return false;
		
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
		Logging.getAPLIBlogger().info("Finish game with " + counter + " steps");
	}

	@Override
	protected void stopSystem(SUT system) {
		//
	}

}

/***************************************************************************************************
 *
 * Copyright (c) 2021 - 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2021 - 2022 Open Universiteit - www.ou.nl
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

import java.util.HashSet;
import java.util.Set;
import org.fruit.alayer.*;
import org.fruit.monkey.Settings;
import org.testar.protocols.iv4xr.SEProtocol;

import eu.testar.iv4xr.actions.se.commands.*;
import spaceEngineers.model.Vec2F;
import spaceEngineers.model.Vec3F;

/**
 * iv4xr EU H2020 project - SpaceEngineers Use Case
 * 
 * In this protocol SpaceEngineers game will act as SUT.
 * 
 * se_commands_testar_dummy / test.setting file contains the:
 * - COMMAND_LINE definition to launch the SUT and the level
 * - SUT_PROCESS_NAME to connect with running SUT (and optionally launch a level)
 * - State model inference settings to connect and create the State Model inside OrientDB
 * 
 * TESTAR is the Agent itself, derives is own knowledge about the observed entities,
 * and takes decisions about the command actions to execute (move, rotate, interact)
 * 
 * Widget              -> Virtual Entity (Blocks)
 * State (Widget-Tree) -> Agent Observation (All Observed Entities)
 * Action              -> SpaceEngineers low level command
 */
public class Protocol_se_commands_testar_dummy extends SEProtocol {

	/**
	 * Called once during the life time of TESTAR.
	 * This method can be used to perform initial setup work.
	 * @param   settings  the current TESTAR test.settings as specified by the user.
	 */
	@Override
	protected void initialize(Settings settings) {
		super.initialize(settings);
	}

	/**
	 * This method is called when the TESTAR requests the state of the SUT.
	 * Here you can add additional information to the SUT's state or write your
	 * own state fetching routine.
	 *
	 * super.getState(system) puts the state information also to the HTML sequence report
	 *
	 * @return  the current state of the SUT with attached oracle.
	 */
	@Override
	protected State getState(SUT system) {
		return super.getState(system);
	}

	/**
	 * The getVerdict methods implements the online state oracles that
	 * examine the SUT's current state and returns an oracle verdict.
	 * @return oracle verdict, which determines whether the state is erroneous and why.
	 */
	@Override
	protected Verdict getVerdict(State state) {
		// Widget Title contains Suspicious title (test.setting -> SuspiciousTitles)
		// SUT hangs
		// SUT crashes
		// SEProtocol is reading SpaceEngineers log to create Warning Verdicts (test.setting -> ProcessLogs)
		return super.getVerdict(state);
	}

	/**
	 * Derive all possible actions that TESTAR can execute in each specific Space Engineers state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// Add Dummy Exploration Actions (Direction based on current agent orientation + Steps Distance)
		labActions.add(new seActionCommandMove(state, agentId, new Vec3F(0, 0, 1f), 100)); // Move to back
		labActions.add(new seActionCommandMove(state, agentId, new Vec3F(0, 0, -1f), 100)); // Move to front
		labActions.add(new seActionCommandMove(state, agentId, new Vec3F(1f, 0, 0), 100)); // Move to Right
		labActions.add(new seActionCommandMove(state, agentId, new Vec3F(-1f, 0, 0), 100)); // Move to Left

		// Add Left Right Rotations
		labActions.add(new seActionCommandRotate(state, agentId, new Vec2F(0, -500f))); // Left
		labActions.add(new seActionCommandRotate(state, agentId, new Vec2F(0, 500f))); // Right

		// Add a block like a dummy
		labActions.add(new seActionCommandPlaceBlock(state, agentId, "LargeBlockArmorBlock"));
		labActions.add(new seActionCommandPlaceBlock(state, agentId, "LargeHeavyBlockArmorBlock"));

		// Use Grinder or Welder tool (like a dummy)
		labActions.add(new seActionCommandGrinder(state, agentId));
		labActions.add(new seActionCommandWelder(state, agentId));

		// Open or close Helmet like a dummy
		labActions.add(new seActionCommandHelmetOpen(state, agentId));
		labActions.add(new seActionCommandHelmetClose(state, agentId));

		// Use object (door, console, etc...) like a dummy (this make no sense if agent is not aiming the correct entity)
		labActions.add(new seActionCommandUseObject(state, agentId));

		return labActions;
	}

	/**
	 * Select one of the available actions using an action selection algorithm (for example random action selection)
	 *
	 * @param state the SUT's current state
	 * @param actions the set of derived actions
	 * @return  the selected action (non-null!)
	 */
	@Override
	protected Action selectAction(State state, Set<Action> actions){
		return super.selectAction(state, actions);
	}

	/**
	 * Execute TESTAR as agent command Action
	 */
	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		return super.executeAction(system, state, action);
	}

	/**
	 * TESTAR uses this method to determine when to stop the generation of actions for the
	 * current sequence. You can stop deriving more actions after:
	 * - a specified amount of executed actions, which is specified through the SequenceLength setting, or
	 * - after a specific time, that is set in the MaxTime setting
	 * @return  if <code>true</code> continue generation, else stop
	 */
	@Override
	protected boolean moreActions(State state) {
		// Execute many actions as indicated in SequenceLength setting
		return super.moreActions(state);
	}

	/**
	 * Here you can put graceful shutdown sequence for your SUT
	 * @param system
	 */
	@Override
	protected void stopSystem(SUT system) {
		super.stopSystem(system);
	}
}

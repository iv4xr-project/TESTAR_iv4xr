/***************************************************************************************************
 *
 * Copyright (c) 2019 - 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019 - 2022 Open Universiteit - www.ou.nl
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.monkey.ConfigTags;
import org.testar.protocols.iv4xr.LabRecruitsSharedProtocol;

import environments.LabRecruitsEnvironment;
import eu.testar.iv4xr.actions.lab.commands.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import nl.ou.testar.RandomActionSelector;

/**
 * iv4xr EU H2020 project - LabRecruits Demo
 * 
 * In this protocol LabRecruits game will act as SUT.
 * labrecruits_commands_testar_agent_dummy_explorer / test.setting file contains the:
 * - COMMAND_LINE definition to start the SUT and load the desired level
 * - State model inference settings to connect and create the State Model inside OrientDB
 * 
 * TESTAR is the Agent itself, derives is own knowledge about the observed entities,
 * and takes decisions about the command actions to execute (observe, moveTo, interactWith)
 * 
 * TESTAR uses the Navigation map internally to select a visible node and explore this LabRecruits level.
 * This level (test.settings -> buttons_doors_1) has block elements,
 * We need to explore different paths to surround the elements that block us.
 * 
 * Widget              -> Virtual Entity
 * State (Widget-Tree) -> Agent Observation (All Observed Entities)
 * Action              -> LabRecruits low level command
 */
public class Protocol_distributed_labrecruits extends LabRecruitsSharedProtocol {

	/**
	 * This method is invoked each time the TESTAR starts the SUT to generate a new sequence.
	 * This can be used for example for bypassing a login screen by filling the username and password
	 * or bringing the system into a specific start state which is identical on each start (e.g. one has to delete or restore
	 * the SUT's configuration files etc.)
	 */
	@Override
	protected void beginSequence(SUT system, State state) {
		super.beginSequence(system, state);
		moreSharedActions = true;
	}

	/**
	 * Derive all possible actions that TESTAR can execute in each specific LabRecruits state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// Get the LabRecruitsEnvironment
		LabRecruitsEnvironment labRecruitsEnv = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment);

		// NavMesh Exploration : Add one exploration movement for each visible node
		labActions = exploreVisibleNodesActions(labActions, state, labRecruitsEnv, agentId);

		// For every interactive entity agents have the possibility to move and interact with
		for(Widget w : state) {
			// TESTAR can try to move towards it
			labActions.add(new labActionCommandMove(w, labRecruitsEnv, agentId, w.get(IV4XRtags.entityPosition), false, false, false));
			// If TESTAR sees an Interactive Entity
			if(isInteractiveEntity(w)) {
				// try to move and interact with
				labActions.add(new labActionCommandMoveInteract(w, labRecruitsEnv, agentId, w.get(IV4XRtags.entityPosition), false, false, false));
				// If TESTAR is in a suitable distance
				if(isAgentCloseToEntity(system, w, 1.0)) {
					// TESTAR can try only to interact
					labActions.add(new labActionCommandInteract(w, labRecruitsEnv, agentId, false, false));
				}
			}
		}

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
	protected Action selectAction(State state, Set<Action> actions) {
		// Call the preSelectAction method from the AbstractProtocol so that, if necessary,
		// unwanted processes are killed and SUT is put into foreground.
		Action retAction = preSelectAction(state, actions);
		if (retAction != null) { return retAction; }

		// targetSharedAction is an unvisited action
		// First check whether we do have a target shared action marked to execute; if not select one
		if (targetSharedAction == null) {
			targetSharedAction = getNewTargetSharedAction(state, actions);
		}

		if (targetSharedAction != null) {
			HashMap<String, Action> actionMap = ConvertActionSetToDictionary(actions);

			// Check if the target shared action to execute is in the current state
			if (actionMap.containsKey(targetSharedAction)) {
				Action targetAction = getTargetActionFound(actionMap);
				System.out.println("TargetSharedAction is in the current state, just select it : " + targetAction.get(Tags.AbstractIDCustom) + " , " + targetAction.get(Tags.Desc));
				return targetAction;
			} 
			// Target shared action to execute is not in the current state, calculate the path to reach our desired target action
			else {
				Action nextStepAction = traversePath(state, actions);
				System.out.println("Unavailable TargetSharedAction, select from path to be followed : " + nextStepAction.get(Tags.AbstractIDCustom) + " , " + nextStepAction.get(Tags.Desc));
				return nextStepAction;
			}
		}

		System.out.println("**** Shared State Model Protocol did not find an action to select, return a random action ****");
		return RandomActionSelector.selectAction(actions);
	}

	/**
	 * Execute TESTAR as agent command Action
	 */
	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		try {
			// adding the action that is going to be executed into HTML report:
			htmlReport.addSelectedAction(state, action);

			System.out.println(action.toShortString());
			// execute selected action in the current state
			action.run(system, state, settings.get(ConfigTags.ActionDuration, 0.1));

			double waitTime = settings.get(ConfigTags.TimeToWaitAfterAction, 0.5);
			Util.pause(waitTime);

			return true;

		}catch(ActionFailedException afe){
			return false;
		}
	}

	@Override
	protected boolean moreActions(State state) {
		// Check if last traverse action leads TESTAR to the expected traverse destination state
		verifyTraversePathDeterminism(state);
		System.out.println("MoreSharedActions ? " + moreSharedActions);
		// For time budget experiments also check max time setting
		return moreSharedActions && (timeElapsed() < settings().get(ConfigTags.MaxTime));
	}

	@Override
	protected boolean moreSequences() {
		// For time budget experiments also check max time setting
		boolean result = ((countInDb("UnvisitedAbstractAction") > 0) || !stopSharedProtocol) && (timeElapsed() < settings().get(ConfigTags.MaxTime));
		System.out.println("moreSharedSequences ? " + result);
		return result;
	}
}

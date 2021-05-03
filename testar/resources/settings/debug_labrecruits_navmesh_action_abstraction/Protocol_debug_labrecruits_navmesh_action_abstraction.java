/***************************************************************************************************
 *
 * Copyright (c) 2019 - 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019 - 2021 Open Universiteit - www.ou.nl
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
import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.testar.action.priorization.iv4xrExplorationPrioritization;
import org.testar.protocols.iv4xr.LabRecruitsProtocol;

import environments.LabRecruitsEnvironment;
import eu.testar.iv4xr.actions.commands.*;
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
public class Protocol_debug_labrecruits_navmesh_action_abstraction extends LabRecruitsProtocol {

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

		// For all entities have the possibility to move and interact with
		for(Widget w : state) {
			if(isInteractiveEntity(w)) {
				labActions.add(new labActionCommandMoveInteract(w, state, labRecruitsEnv, agentId, w.get(IV4XRtags.entityPosition), false, false, false));
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
	protected Action selectAction(State state, Set<Action> originalActions){

		//Call the preSelectAction method from the AbstractProtocol so that, if necessary,
		//unwanted processes are killed and SUT is put into foreground.
		Action retAction = preSelectAction(state, originalActions);
		if (retAction== null) {
			//if no preSelected actions are needed, then implement your own action selection strategy

			System.out.println("----------- Available Actions -----------");
			for(Action a : originalActions) {
				System.out.println(a.get(Tags.AbstractIDCustom) + " : " + a.get(Tags.Desc, ""));
			}

			// Try to get the labActionExplorePosition actions to prioritize exploration
			Set<Action> exploratoryActions = iv4xrExplorationPrioritization.getUnvisitedExploratoryActions(originalActions);

			// If we have exploratory actions to execute, select one of them randomly
			if(exploratoryActions != null) {
				System.out.println("----------- getExploratoryActions Actions -----------");
				for(Action a : exploratoryActions) {
					System.out.println(a.get(Tags.AbstractIDCustom) + " : " + a.get(Tags.Desc, ""));
				}
				System.out.println("-----------------------------------------------------");

				//randomly select one of the unvisited exploratory actions
				retAction = RandomActionSelector.selectAction(exploratoryActions);
				// update as executed for next iteration
				iv4xrExplorationPrioritization.addExecutedExploratoryAction(retAction);
			} 
			// If we do not have exploratory actions to execute, 
			// use the state model to select other type of action
			else {
				System.out.println("----------- getExploratoryActions Actions -----------");
				System.out.println("----------- All ExploratoryActions EXECUTED ---------");
				System.out.println("-----------------------------------------------------");

				//using the action selector of the state model:
				retAction = stateModelManager.getAbstractActionToExecute(originalActions);
			}
		}
		if(retAction==null) {
			System.out.println("Exploratory and State model based action selection did not find an action. Using random action selection.");
			// use random
			retAction = RandomActionSelector.selectAction(originalActions);
		}

		return retAction;
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
}

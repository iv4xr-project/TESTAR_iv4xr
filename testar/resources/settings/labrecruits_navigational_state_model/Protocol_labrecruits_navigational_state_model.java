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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.monkey.ConfigTags;
import org.testar.OutputStructure;
import org.testar.action.priorization.iv4xrExplorationPrioritization;
import org.testar.action.priorization.iv4xrNavigableState;
import org.testar.action.priorization.iv4xrNavigableStateMap;
import org.testar.protocols.iv4xr.LabRecruitsProtocol;

import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.lab.commands.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
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
public class Protocol_labrecruits_navigational_state_model extends LabRecruitsProtocol {

	// Navigable State that an agent can explore
	private iv4xrNavigableState navigableState = new iv4xrNavigableState("Initial");
	// A map to connect multiple navigableState
	private iv4xrNavigableStateMap navigableStateMap = new iv4xrNavigableStateMap();

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
		State state = super.getState(system);

		for(Widget w : state) {
			// Ignore the agent itself and the state
			if(w.equals(state.get(IV4XRtags.agentWidget)) || w.equals(state)) continue;

			// Add the visible entity information
			navigableState.addReachableEntity(w.get(IV4XRtags.entityId, ""), w.get(IV4XRtags.labRecruitsEntityIsActive, false));
		}

		// Add the visible and navigable navMesh nodes
		navigableState.addNavigableNode(state.get(IV4XRtags.labRecruitsNavMesh, Collections.<SVec3>emptySet()));

		return state;
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

		// For all entities have the possibility to move and interact with
		for(Widget w : state) {
			if(isInteractiveEntity(w)) {
				labActions.add(new labActionCommandMoveInteract(w, state, labRecruitsEnv, agentId, w.get(IV4XRtags.entityPosition), false, false, false));
			}
		}

		return labActions;
	}

	/**
	 * Determine if the iv4xr Widget Entity is Interactive.
	 */
	@Override
	protected boolean isInteractiveEntity(Widget widget) {
		return (widget.get(IV4XRtags.entityType, null) != null &&
				widget.get(IV4XRtags.entityType, null).toString().equals("Switch"));
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
		// Now actions have been derived and AbstractIDCustom Tag calculated
		// Save the information of exploratory actions in the State Model
		saveExploratoryActionsStateModel(originalActions);

		//Call the preSelectAction method from the AbstractProtocol so that, if necessary,
		//unwanted processes are killed and SUT is put into foreground.
		Action retAction = preSelectAction(state, originalActions);
		if (retAction == null) {
			//using the action selector of the state model:
			retAction = stateModelManager.getAbstractActionToExecute(originalActions);
		}
		if(retAction == null) {
			System.out.println("Exploratory and State model based action selection did not find an action. Using random action selection.");
			// use random
			retAction = RandomActionSelector.selectAction(originalActions);
		}

		return retAction;
	}

	private void saveExploratoryActionsStateModel(Set<Action> derivedActions) {
		Map<String, SVec3> unexecutedExploratoryActions = new HashMap<>();
		Set<Action> exploratoryActions = new HashSet<>();
		for(Action action : derivedActions) {
			if(action instanceof labActionExplorePosition) {
				Vec3 nodePosition = ((labActionExplorePosition) action).getExplorePosition();
				SVec3 unexploredNode = new SVec3(nodePosition.x, nodePosition.y, nodePosition.z);
				unexecutedExploratoryActions.put(action.get(Tags.AbstractIDCustom), unexploredNode);
				exploratoryActions.add(action);
			}
		}
		stateModelManager.notifyUnexecutedExploratoryActions(unexecutedExploratoryActions, exploratoryActions);
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

			if(action instanceof labActionCommandMoveInteract) {
				// Add explored navigable state in our map
				navigableStateMap.addNavigableState(navigableState);
				// Create a Navigable State in the State Model
				stateModelManager.notifyNewNavigableState(navigableState.getNavigableNodes(), 
						navigableState.getReachableEntities(), 
						navigableState.getExecutedAction(),
						action.get(Tags.AbstractIDCustom));

				// Then create a new navigable state object based in the current interacted entity
				String interactedEntity = ((labActionCommandMoveInteract) action).getEntityId();
				String beforeIsActive = action.get(Tags.OriginWidget).get(IV4XRtags.labRecruitsEntityIsActive).toString();
				String afterIsActive = "Unknown";
				Widget afterWidget = getEntityWidgetFromState(system, interactedEntity);
				if(afterWidget != null) {
					afterIsActive = afterWidget.get(IV4XRtags.labRecruitsEntityIsActive).toString();
				}

				String interactionInfo = "Entity:" + interactedEntity + ",From:" + beforeIsActive + ",To:" + afterIsActive;
				navigableState = new iv4xrNavigableState(interactionInfo);

				// Update lastInteractAction for the finishSequence case
				lastInteractActionAbstractIDCustom = action.get(Tags.AbstractIDCustom);
			}

			return true;

		}catch(ActionFailedException afe){
			return false;
		}
	}

	private Widget getEntityWidgetFromState(SUT system, String entityId) {
		Util.pause(2);
		// User super getState to avoid navigableState conflicts
		for(Widget w : super.getState(system)) {
			if(w.get(IV4XRtags.entityId, "").equals(entityId)) {
				return w;
			}
		}

		return null;
	}

	/**
	 * This method is invoked each time the TESTAR has reached the stop criteria for generating a sequence.
	 * This can be used for example for graceful shutdown of the SUT, maybe pressing "Close" or "Exit" button
	 */
	@Override
	protected void finishSequence() {
		super.finishSequence();
		//TODO: Do we want to save last navigable state if is not complete?
		// Add last explored navigable state in our map
		navigableStateMap.addNavigableState(navigableState);
		// Create a Navigable State in the State Model
		stateModelManager.notifyNewNavigableState(navigableState.getNavigableNodes(), 
				navigableState.getReachableEntities(), 
				navigableState.getExecutedAction(),
				lastInteractActionAbstractIDCustom);

		String navigableStateMapInfo = navigableStateMap.toString();
		System.out.println(navigableStateMapInfo);
		try {
			File outputFolder = new File(OutputStructure.outerLoopOutputDir).getCanonicalFile();
			String outputFile = outputFolder.getPath() + File.separator + "navigableStateMapInfo.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));
			writer.append(navigableStateMapInfo);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR: Writing navigableStateMapInfo results");
		}
	}
}

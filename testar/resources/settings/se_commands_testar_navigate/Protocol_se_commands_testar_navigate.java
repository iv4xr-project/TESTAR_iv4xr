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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.StateBuildException;
import org.fruit.alayer.exceptions.SystemStartException;
import org.testar.protocols.iv4xr.SEProtocol;
import org.testar.protocols.iv4xr.iv4xrNavigableState;
import org.testar.protocols.iv4xr.iv4xrNavigableStateMap;
import org.testar.protocols.iv4xr.se.NavigableAreaSE;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.se.commands.seActionCommandMove;
import eu.testar.iv4xr.actions.se.goals.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import nl.ou.testar.HtmlReporting.HtmlSequenceReport;
import spaceEngineers.model.Vec3F;
import eu.testar.iv4xr.actions.se.commands.*;
import nl.ou.testar.RandomActionSelector;

/**
 * iv4xr EU H2020 project - SpaceEngineers Use Case
 * 
 * In this protocol SpaceEngineers game will act as SUT.
 * 
 * se_commands_testar_teleport / test.setting file contains the:
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
public class Protocol_se_commands_testar_navigate extends SEProtocol {

	/*
	 * Modify agent ObservationRadius in the file: 
	 * C:\Users\<user>\AppData\Roaming\SpaceEngineers\ivxr-plugin.config
	 */

	private static Set<String> toolEntities;
	static {
		toolEntities = new HashSet<String>();
		//toolEntities.add("LargeBlockSmallGenerator");
		toolEntities.add("LargeBlockBatteryBlock");
		toolEntities.add("SurvivalKitLarge");
	}

	private static Set<String> interactiveEntities;
	static {
		interactiveEntities = new HashSet<String>();
		interactiveEntities.add("Ladder2");
		interactiveEntities.add("LargeBlockCockpit");
		interactiveEntities.add("CockpitOpen");
		interactiveEntities.add("LargeBlockCryoChamber");
	}

	// Oracle example to validate that the block integrity decreases after a Grinder action
	private Verdict functional_verdict = Verdict.OK;

	// Memory map to calculate explored and unexplored 3D positions
	private NavigableAreaSE navigableAreaSE;

	// Used as helper classes for State Model Navigable State
	private iv4xrNavigableState navigableState = new iv4xrNavigableState("");

	@Override
	protected SUT startSystem() throws SystemStartException {
		navigableAreaSE = new NavigableAreaSE();
		return super.startSystem();
	}

	@Override
	protected void beginSequence(SUT system, State state) {
		super.beginSequence(system, state);
		if(this.mode != Modes.Generate) return;

		// We assume that the initial agent position is a navigable position
		Vec3 initialPosition = SVec3.seToLab(state.get(IV4XRtags.agentWidget).get(IV4XRtags.seAgentPosition));
		System.out.println("Adding initial position: " + initialPosition);
		navigableAreaSE.addInitialPosition(initialPosition);
	}

	@Override
	protected State getState(SUT system) throws StateBuildException {
		State state = super.getState(system);
		System.out.println("AGENT pos: " + state.get(IV4XRtags.agentWidget).get(IV4XRtags.seAgentPosition));

		return state;
	}

	/**
	 * The getVerdict methods implements the online state oracles that
	 * examine the SUT's current state and returns an oracle verdict.
	 * @return oracle verdict, which determines whether the state is erroneous and why.
	 */
	@Override
	protected Verdict getVerdict(State state) {
		// Apply an Oracle to check if Grinder action worked properly
		if(lastExecutedAction != null && lastExecutedAction instanceof seActionNavigateGrinderBlock) {
			// Check the block attached to the previous executed grinder action
			Widget previousBlock = ((seActionNavigateGrinderBlock)lastExecutedAction).get(Tags.OriginWidget);
			Float previousIntegrity = previousBlock.get(IV4XRtags.seIntegrity);
			System.out.println("Previous Block Integrity: " + previousIntegrity);
			// Try to find the same block in the current state using the block id
			for(Widget w : state) {
				if(w.get(IV4XRtags.entityId).equals(previousBlock.get(IV4XRtags.entityId))) {
					Float currentIntegrity = w.get(IV4XRtags.seIntegrity);
					System.out.println("Current Block Integrity: " + currentIntegrity);
					// If previous integrity is the same or increased, something went wrong
					if(currentIntegrity >= previousIntegrity) {
						String blockType = w.get(IV4XRtags.entityType);
						functional_verdict = new Verdict(Verdict.BLOCK_INTEGRITY_ERROR, "The integrity of interacted block " + blockType + " didn't decrease after a Grinder action");
					}
				}
			}
			// If the previous block does not exist in the current state, it has been destroyed after the grinder action
			// We consider this OK by default, but more sophisticated oracles can be applied here
		}

		// Apply an Oracle to check jet-pack settings
		if(lastExecutedAction != null && lastExecutedAction instanceof seActionNavigateInteract) {
			Widget previousAgent = getAgentEntityFromState(latestState);
			Widget currentAgent = getAgentEntityFromState(state);

			if(!previousAgent.get(IV4XRtags.seAgentJetpackRunning).equals(currentAgent.get(IV4XRtags.seAgentJetpackRunning))) {
				Widget interactedBlock = ((seActionNavigateInteract)lastExecutedAction).get(Tags.OriginWidget);
				functional_verdict = new Verdict(Verdict.JETPACK_SETTINGS_ERROR, "Jetpack settings are incorrect after interacting with block : " + interactedBlock.get(IV4XRtags.entityType));
			}
		}

		return super.getVerdict(state).join(functional_verdict);
	}

	/**
	 * Derive all possible actions that TESTAR can execute in each specific Space Engineers state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();
		if(this.mode != Modes.Generate) return super.deriveActions(system, state);

		// Update Navigable State entities and navMesh positions information
		for(Widget w : state) {
			if(toolEntities.contains(w.get(IV4XRtags.entityType)) && seReachablePositionHelper.calculateIfEntityReachable(system, w)) {
				labActions.add(new seActionNavigateGrinderBlock(w, w.get(IV4XRtags.entityPosition), system, agentId, 4, 1.0));
				labActions.add(new seActionNavigateWelderBlock(w, w.get(IV4XRtags.entityPosition), system, agentId, 4, 1.0));
				navigableAreaSE.addNewReachableEntity(w);
				String description = w.get(IV4XRtags.entityType) + "_" + w.get(IV4XRtags.entityId);
				navigableState.addReachableEntity(description, true);
			}

			// FIXME: Fix Ladder2 is not observed as entityType
			if((interactiveEntities.contains(w.get(IV4XRtags.entityType)) || w.get(IV4XRtags.seDefinitionId, "").contains("Ladder2"))
					&& seReachablePositionHelper.calculateIfEntityReachable(system, w)) {
				labActions.add(new seActionNavigateInteract(w, system, agentId));
				navigableAreaSE.addNewReachableEntity(w);
				String description = w.get(IV4XRtags.entityType) + "_" + w.get(IV4XRtags.entityId);
				navigableState.addReachableEntity(description, true);
			}
		}

		// Now add the set of actions to explore level positions
		labActions = seReachablePositionHelper.calculateExploratoryPositions(system, state, agentId, labActions);
		updateExploredSpace(labActions);

		// If it was not possible to navigate to an entity or realize a smart exploration
		// prepare a dummy exploration
		if(labActions.isEmpty()) {
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(0, 0, 1f), 30)); // Move to back
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(0, 0, -1f), 30)); // Move to front
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(1f, 0, 0), 30)); // Move to Right
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(-1f, 0, 0), 30)); // Move to Left
		}

		return labActions;
	}

	private void updateExploredSpace(Set<Action> actions) {
		for(Action a : actions) {
			if(a instanceof seActionExplorePosition) {
				navigableAreaSE.updateExploredSpace(((seActionExplorePosition) a).getTargetPosition());
			}
		}
	}

	@Override
	protected boolean executeAction(SUT system, State state, Action action) {
		boolean executed = super.executeAction(system, state, action);
		// Reset internal list for next iteration
		navigableAreaSE.resetNextEntity();
		navigableAreaSE.resetNextPosition();

		// Print information
		navigableAreaSE.printExploredPositionsInfo();
		navigableAreaSE.printUnexploredPositionsInfo();
		System.out.println("Reachable Entities: " + navigableAreaSE.getInteractedEntitiesDescription());

		//Add the discovered navigable positions (explored and unexplored) to the State Model helper class 
		navigableState.addNavigableNode(navigableAreaSE.discoveredReachablePositions());

		return executed;
	}

	@Override
	protected void finishSequence() {
		navigableAreaSE.printExploredPositionsInfo();
		navigableAreaSE.printUnexploredPositionsInfo();
		System.out.println("Reachable Entities: " + navigableAreaSE.getInteractedEntitiesDescription());

		super.finishSequence();

		// Create last Navigable State in the State Model
		stateModelManager.notifyNewNavigableState(navigableState.getNavigableNodes(),
				navigableAreaSE.discoveredUnexploredPositions(),
				navigableState.getReachableEntities(), 
				"NotExecutedAction",
				"Initial");
	}

}

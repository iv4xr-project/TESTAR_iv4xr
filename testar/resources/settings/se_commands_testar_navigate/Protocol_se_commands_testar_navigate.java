/***************************************************************************************************
 *
 * Copyright (c) 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2021 Open Universiteit - www.ou.nl
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
import org.testar.protocols.iv4xr.SEProtocol;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.se.commands.*;
import eu.testar.iv4xr.actions.se.goals.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import nl.ou.testar.RandomActionSelector;
import spaceEngineers.model.Vec3F;

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

	private static Set<String> movementEntities;
	static {
		movementEntities = new HashSet<String>();
		movementEntities.add("LargeBlockSmallGenerator");
		movementEntities.add("LargeBlockBatteryBlock");
		movementEntities.add("ButtonPanelLarge");
	}

	/**
	 * The getVerdict methods implements the online state oracles that
	 * examine the SUT's current state and returns an oracle verdict.
	 * @return oracle verdict, which determines whether the state is erroneous and why.
	 */
	@Override
	protected Verdict getVerdict(State state) {
		// Oracle example to validate that the block integrity decreases after a Grinder action
		Verdict verdict_block_integrity = Verdict.OK;
		// Apply the Oracle only if last executed action was a Grinder action
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
						verdict_block_integrity = new Verdict(Verdict.BLOCK_INTEGRITY_ERROR, "The integrity of interacted block didn't decrease after a Grinder action");
					}
				}
			}
			// If the previous block does not exist in the current state, it has been destroyed after the grinder action
			// We consider this OK by default, but more sophisticated oracles can be applied here
		}

		return super.getVerdict(state).join(verdict_block_integrity);
	}

	/**
	 * Derive all possible actions that TESTAR can execute in each specific Space Engineers state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// For each block widget (see movementEntities types), rotate and move until the agent is close to the position of the block
		for(Widget w : state) {
			Vec3 reachablePosition = null;
			if(movementEntities.contains(w.get(IV4XRtags.entityType)) 
					&& (reachablePosition = seReachablePositionHelper.calculateAdjacentReachablePosToEntity(system, w)) != null) {
				labActions.add(new seActionNavigateToBlock(w, reachablePosition, system, agentId));
				labActions.add(new seActionNavigateGrinderBlock(w, reachablePosition, system, agentId, 4, 1.0));
				labActions.add(new seActionNavigateWelderBlock(w, reachablePosition, system, agentId, 4, 1.0));
			}
		}

		// Now add the set of actions to explore level positions
		labActions = calculateExploratoryPositions(system, state, labActions);

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

	/**
	 * Select one of the available actions using an action selection algorithm (for example random action selection)
	 *
	 * @param state the SUT's current state
	 * @param actions the set of derived actions
	 * @return  the selected action (non-null!)
	 */
	@Override
	protected Action selectAction(State state, Set<Action> actions){

		//Call the preSelectAction method from the AbstractProtocol so that, if necessary,
		//unwanted processes are killed and SUT is put into foreground.
		Action retAction = preSelectAction(state, actions);
		if (retAction== null) {
			//if no preSelected actions are needed, then implement your own action selection strategy
			//using the action selector of the state model:
			retAction = stateModelManager.getAbstractActionToExecute(actions);
		}
		if(retAction==null) {
			System.out.println("State model based action selection did not find an action. Using default action selection.");
			// if state model fails, use default:
			retAction = RandomActionSelector.selectAction(actions);
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

	/**
	 * SE - Platform
	 * X and Z axes are the 2D to calculate the navigation movements. 
	 * Calculate the navigable position by adding coordinates to the current agent position. 
	 * 
	 * @param system
	 * @param state
	 * @param actions
	 * @return actions
	 */
	private Set<Action> calculateExploratoryPositions(SUT system, State state, Set<Action> actions) {
		// Circular positions relative to the agent center
		// https://stackoverflow.com/a/5301049
		Vec3 agentCenter = SVec3.seToLab(state.get(IV4XRtags.agentWidget).get(IV4XRtags.seAgentPosition));

		// 1 block distance positions (near)
		// For near positions calculate 8 positions in circle
		int points = 8;
		double slice = 2 * Math.PI / points;
		double radius = 2.5;
		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			float newX = agentCenter.x + (float)(radius * Math.cos(angle));
			float newZ = agentCenter.z + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a reachable position
			Vec3 nearPosition = new Vec3(newX, agentCenter.y, newZ);
			if(seReachablePositionHelper.calculateIfPositionIsReachable(system, nearPosition)) {
				actions.add(new seActionExplorePosition(state, nearPosition, system, agentId));
			}
		}

		// 2 block distance positions (medium)
		// For medium positions calculate 16 positions in circle
		points = 16;
		slice = 2 * Math.PI / points;
		radius = 5.0;
		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			float newX = agentCenter.x + (float)(radius * Math.cos(angle));
			float newZ = agentCenter.z + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a reachable position
			Vec3 medPosition = new Vec3(newX, agentCenter.y, newZ);
			if(seReachablePositionHelper.calculateIfPositionIsReachable(system, medPosition)) {
				actions.add(new seActionExplorePosition(state, medPosition, system, agentId));
			}
		}

		// 3 block distance positions (far)
		// For far positions calculate 16 positions in circle
		points = 16;
		slice = 2 * Math.PI / points;
		radius = 7.5;
		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			float newX = agentCenter.x + (float)(radius * Math.cos(angle));
			float newZ = agentCenter.z + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a reachable position
			Vec3 farPosition = new Vec3(newX, agentCenter.y, newZ);
			if(seReachablePositionHelper.calculateIfPositionIsReachable(system, farPosition)) {
				actions.add(new seActionExplorePosition(state, farPosition, system, agentId));
			}
		}

		return actions;
	}
}

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

package eu.testar.iv4xr.actions.se.goals;

import java.util.List;
import java.util.Set;

import org.fruit.alayer.Role;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.iv4xrActionRoles;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import spaceEngineers.controller.Observer;
import spaceEngineers.model.extensions.ObservationExtensionsKt;
import spaceEngineers.navigation.NavGraph;
import spaceEngineers.navigation.Node;
import spaceEngineers.navigation.RichNavGraph;
import spaceEngineers.navigation.RichNavGraphKt;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.iv4xr.navigation.NavigableGraph;
import spaceEngineers.model.Vec2F;
import spaceEngineers.model.Vec3F;

public class seActionNavigateToBlock extends seActionGoal {
	private static final long serialVersionUID = 1846118675335766867L;

	protected String widgetType;
	protected String widgetId;
	protected eu.iv4xr.framework.spatial.Vec3 widgetPosition;
	protected Vec3F targetPosition;
	protected Vec3 calculatedReachablePosition;
	protected final float DEGREES = 2416f;

	public seActionNavigateToBlock(Widget w, SUT system, String agentId){
		this.agentId = agentId;
		this.set(Tags.OriginWidget, w);
		this.widgetType = w.get(IV4XRtags.entityType);
		this.widgetId = w.get(IV4XRtags.entityId);
		this.widgetPosition = w.get(IV4XRtags.entityPosition);
		this.targetPosition = SVec3.labToSE(w.get(IV4XRtags.entityPosition));
		this.set(Tags.Role, iv4xrActionRoles.iv4xrActionCommandMove);
		this.set(Tags.Desc, toShortString());
		// TODO: Update with Goal Solving agents
		this.set(IV4XRtags.agentAction, false);
		this.set(IV4XRtags.newActionByAgent, false);

		this.testAgent = system.get(IV4XRtags.iv4xrTestAgent);
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		navigateToReachableBlockPosition(system, state);
		rotateToBlockDestination(system);
	}

	/**
	 * Use UU approach to create a state grid and navigate until the desired block.  
	 * 
	 * @param system
	 */
	protected void navigateToReachableBlockPosition(SUT system, State state) {
		// Create a navigational graph of the largest grid
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
		Observer seObserver = seController.getObserver();
		String largestGridId = ObservationExtensionsKt.largestGrid(seObserver.observeBlocks()).getId();
		NavGraph navGraph = seObserver.navigationGraph(largestGridId);
		RichNavGraph richNavGraph = RichNavGraphKt.toRichGraph(navGraph);

		Set<Vec3F> notReachablePositions = notReachablePositions(seObserver, state);

		// Check if there is a reachable node in the navigational graph
		// that allows the agent to reach the target block position
		int reachableNode = -1;
		//float closestDistance = 3f; // Near the block to be able to interact later
		float closestDistance = 7f; // Closest distance to a 2 dimensions block is 3.7f approx
		for (Node node : richNavGraph.getNodeMap().values()) {

			// Ignore the not reachable positions (due to block size)
			Vec3F nodePosition = new Vec3F(node.getPosition().getX(), 0f, node.getPosition().getZ());
			if(notReachablePositions.contains(nodePosition)) continue;

			float distance = node.getPosition().distanceTo(targetPosition); // the target position of the widget to interact with
			if(distance < closestDistance){
				reachableNode = node.getId();
				closestDistance = distance;
			}
		}

		if(reachableNode != -1) {
			NavigableGraph navigableGraph = new NavigableGraph(navGraph);
			int targetNode = navGraph.getNodes().get(reachableNode).getId();
			List<Integer> nodePath = getPath(navigableGraph, targetNode);

			for (Integer nodeId : nodePath) {
				new SEnavigator().moveInLine(system, navigableGraph.node(nodeId).getPosition());
			}
		}
	}

	/**
	 * Rotate tick by tick until the agent aims the block destination. 
	 * Based on: https://github.com/iv4xr-project/iv4xr-se-plugin/blob/uubranch3D/JvmClient/src/jvmMain/java/uuspaceagent/UUTacticLib.java#L160
	 * 
	 * @param system
	 */
	protected void rotateToBlockDestination(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		spaceEngineers.controller.SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
		spaceEngineers.controller.Observer seObserver = seController.getObserver();

		eu.iv4xr.framework.spatial.Vec3 agentPosition = SVec3.seToLab(seObserver.observe().getPosition());
		eu.iv4xr.framework.spatial.Vec3 entityPosition = SVec3.seToLab(targetPosition);
		eu.iv4xr.framework.spatial.Vec3 directionToGo = eu.iv4xr.framework.spatial.Vec3.sub(agentPosition, entityPosition);
		eu.iv4xr.framework.spatial.Vec3 agentOrientation = SVec3.seToLab(seObserver.observe().getOrientationForward());

		directionToGo.y = 0;
		agentOrientation.y = 0;

		directionToGo = directionToGo.normalized();
		agentOrientation = agentOrientation.normalized();

		float cos_alpha = eu.iv4xr.framework.spatial.Vec3.dot(agentOrientation, directionToGo);

		while(cos_alpha > -0.99f) {
			// rotate faster until the aiming is close
			if(cos_alpha > -0.95f) {seCharacter.moveAndRotate(new Vec3F(0,0,0),  new Vec2F(0, DEGREES*0.007f), 0f, 1);}
			else {seCharacter.moveAndRotate(new Vec3F(0,0,0), Vec2F.Companion.getROTATE_RIGHT(), 0f, 1);}

			agentPosition = SVec3.seToLab(seObserver.observe().getPosition());
			entityPosition = SVec3.seToLab(targetPosition);
			directionToGo = eu.iv4xr.framework.spatial.Vec3.sub(agentPosition, entityPosition);
			agentOrientation = SVec3.seToLab(seObserver.observe().getOrientationForward());

			directionToGo.y = 0;
			agentOrientation.y = 0;

			directionToGo = directionToGo.normalized();
			agentOrientation = agentOrientation.normalized();

			cos_alpha = eu.iv4xr.framework.spatial.Vec3.dot(agentOrientation, directionToGo);
		}
	}

	/**
	 * Rotate until the agent is aiming the target block. 
	 * For interactive entities (e.g., LargeBlockCryoChamber or LargeBlockCockpit), 
	 * it is essential that the agent is not using tools (e.g., Grinder or Welder). 
	 * Then we are able to aim the interactive part of the block and not the block itself. 
	 * 
	 * @param system
	 */
	protected boolean aimToBlock(SUT system, Widget targetBlock) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
		Observer seObserver = seController.getObserver();

		int AIMTRIES = 300;
		int tries = 1;
		while(!targetBlockFound(seObserver, targetBlock) && tries < AIMTRIES) {		
			seCharacter.moveAndRotate(new Vec3F(0, 0, 0), new Vec2F(0, DEGREES*0.0035f), 0f, 1);
			tries ++;
		}
		return targetBlockFound(seObserver, targetBlock);
	}

	private boolean targetBlockFound(Observer seObserver, Widget targetBlock) {
		try {
			if(seObserver.observe().getTargetBlock() == null) return false;
			return seObserver.observe().getTargetBlock().getId().equals(targetBlock.get(IV4XRtags.entityId));
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public String toShortString() {
		return String.format("Agent %s navigate to: %s , located: %s", agentId, widgetType, widgetPosition);
	}

	@Override
	public String toParametersString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString(Role... discardParameters) {
		// TODO Auto-generated method stub
		return null;
	}
}
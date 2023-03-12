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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fruit.alayer.Role;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.iv4xrActionRoles;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import spaceEngineers.controller.Observer;
import spaceEngineers.model.extensions.ObservationExtensionsKt;
import spaceEngineers.navigation.NavGraph;
import spaceEngineers.navigation.RichNavGraph;
import spaceEngineers.navigation.RichNavGraphKt;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.graph.DataNode;
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
	private List<Vec3F> navigableNodes = new ArrayList<Vec3F>();

	public List<Vec3F> getNavigableNodes(){
		return navigableNodes;
	}

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
		String reachableNode = "";
		//float closestDistance = 3f; // Near the block to be able to interact later
		float closestDistance = 7f; // Closest distance to a 2 dimensions block is 3.7f approx
		for (DataNode<String, Vec3F> node : richNavGraph.getNodeMap().values()) {

			// Ignore the not reachable positions (due to block size)
			Vec3F nodePosition = new Vec3F(node.getData().getX(), 0f, node.getData().getZ());
			if(notReachablePositions.contains(nodePosition)) continue;

			float distance = node.getData().distanceTo(targetPosition); // the target position of the widget to interact with
			if(distance < closestDistance){
				reachableNode = node.getId();
				closestDistance = distance;
			}
		}

		Vec3F characterPosition = seObserver.observeBlocks().getCharacter().getPosition();

		String startNode = richNavGraph.getNodeMap().entrySet()
				.stream()
				.min(Comparator.comparingDouble(entry -> entry.getValue().getData().distanceTo(characterPosition)))
				.map(Map.Entry::getKey)
				.orElse("");

		if(!reachableNode.isEmpty() && !startNode.isEmpty()) {
			NavigableGraph navigableGraph = new NavigableGraph(navGraph);
			List<String> nodePath = getPath((Navigatable<String>) navigableGraph, startNode, reachableNode);

			// For each calculated node in the navigable path
			for (String nodeId : nodePath) {
				// Use the navigator to move to the next node position
				new SEnavigator().moveInLine(system, navigableGraph.node(nodeId).getData());
				// And update the action list of navigableNodes
				navigableNodes.add(navigableGraph.node(nodeId).getData());
			}
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

	protected void rotateToBlockDestination(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		spaceEngineers.controller.SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
		spaceEngineers.controller.Observer seObserver = seController.getObserver();

		Vec3F agentPosition = seObserver.observe().getPosition();
		float distance = targetPosition.distanceTo(agentPosition);
		float tolerance = sePositionRotationHelper.rotationToleranceByDistance(distance);

		Vec3F direction = (targetPosition.minus(agentPosition)).normalized();
		Vec3F agentOrientation = seObserver.observe().getOrientationForward().normalized();
		float cos_alpha = sePositionRotationHelper.dot(agentOrientation, direction);

		// Max of 20 seconds trying to rotate
		long start = System.currentTimeMillis();
		long end = start + 20 * 1000;

		while(cos_alpha < (1f - tolerance) && System.currentTimeMillis() < end) {
			// rotate faster until the aiming is close
			if(cos_alpha < (0.97f - tolerance)) {seCharacter.moveAndRotate(new Vec3F(0,0,0),  new Vec2F(0, DEGREES*0.007f), 0f, 1);}
			else {seCharacter.moveAndRotate(new Vec3F(0,0,0), Vec2F.Companion.getROTATE_RIGHT(), 0f, 1);}

			agentPosition = seObserver.observe().getPosition();
			direction = (targetPosition.minus(agentPosition)).normalized();
			agentOrientation = seObserver.observe().getOrientationForward().normalized();

			cos_alpha = sePositionRotationHelper.dot(agentOrientation, direction);
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
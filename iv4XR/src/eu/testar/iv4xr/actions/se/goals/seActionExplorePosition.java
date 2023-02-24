/***************************************************************************************************
 *
 * Copyright (c) 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2022 Open Universiteit - www.ou.nl
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
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.graph.DataNode;
import spaceEngineers.iv4xr.navigation.NavigableGraph;
import spaceEngineers.model.Vec3F;
import spaceEngineers.model.extensions.ObservationExtensionsKt;
import spaceEngineers.navigation.NavGraph;
import spaceEngineers.navigation.RichNavGraph;
import spaceEngineers.navigation.RichNavGraphKt;

public class seActionExplorePosition extends seActionGoal {
	private static final long serialVersionUID = -5843747535124644882L;

	protected Vec3 targetPosition;

	public Vec3 getTargetPosition() {
		return targetPosition;
	}

	private List<Vec3F> navigableNodes = new ArrayList<Vec3F>();

	public List<Vec3F> getNavigableNodes(){
		return navigableNodes;
	}

	public seActionExplorePosition(Widget w, Vec3 position, SUT system, String agentId){
		this.agentId = agentId;
		this.set(Tags.OriginWidget, w);
		this.targetPosition = position;
		this.set(Tags.Role, iv4xrActionRoles.iv4xrActionCommandMove);
		this.set(Tags.Desc, toShortString());
		// TODO: Update with Goal Solving agents
		this.set(IV4XRtags.agentAction, false);
		this.set(IV4XRtags.newActionByAgent, false);

		this.testAgent = system.get(IV4XRtags.iv4xrTestAgent);
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		navigateToReachablePosition(system, state);
	}

	/**
	 * Use UU approach to create a state grid and navigate until the desired block.  
	 * 
	 * @param system
	 */
	protected void navigateToReachablePosition(SUT system, State state) {
		Vec3F destinationPosition = SVec3.labToSE(targetPosition);

		// Create a navigational graph of the largest grid
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
		Observer seObserver = seController.getObserver();
		String largestGridId = ObservationExtensionsKt.largestGrid(seObserver.observeBlocks()).getId();
		NavGraph navGraph = seObserver.navigationGraph(largestGridId);
		RichNavGraph richNavGraph = RichNavGraphKt.toRichGraph(navGraph);

		Set<Vec3F> notReachablePositions = notReachablePositions(seObserver, state);

		// Check if there is a reachable node in the navigational graph
		// that allows the agent to reach the position to explore
		String reachableNode = "";
		float closestDistance = 0.5f; // Not exactly the same position but almost
		for (DataNode<String, Vec3F> node : richNavGraph.getNodeMap().values()) {

			// Ignore the not reachable positions (due to block size)
			Vec3F nodePosition = new Vec3F(node.getData().getX(), 0f, node.getData().getZ());
			if(notReachablePositions.contains(nodePosition)) continue;

			float distance = node.getData().distanceTo(destinationPosition); // the destination position to explore
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

	@Override
	public String toShortString() {
		return String.format("Agent %s exploring position %s", agentId, targetPosition);
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
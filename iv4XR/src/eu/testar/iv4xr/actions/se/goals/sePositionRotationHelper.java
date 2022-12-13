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

import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Widget;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import spaceEngineers.controller.Observer;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.model.Vec3F;
import spaceEngineers.model.extensions.ObservationExtensionsKt;
import spaceEngineers.navigation.NavGraph;
import spaceEngineers.navigation.Node;
import spaceEngineers.navigation.RichNavGraph;
import spaceEngineers.navigation.RichNavGraphKt;

public class sePositionRotationHelper {

	private sePositionRotationHelper() {}

	/**
	 * Calculate a reachable node position near the desired entity. 
	 * 
	 * @param system
	 * @param w
	 * @return
	 */
	public static boolean calculateIfEntityReachable(SUT system, Widget w) {
		Vec3F targetPosition = SVec3.labToSE(w.get(IV4XRtags.entityPosition));

		// Create a navigational graph of the largest grid
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
		Observer seObserver = seController.getObserver();
		String largestGridId = ObservationExtensionsKt.largestGrid(seObserver.observeBlocks()).getId();
		NavGraph navGraph = seObserver.navigationGraph(largestGridId);
		RichNavGraph richNavGraph = RichNavGraphKt.toRichGraph(navGraph);

		// Check if there is a reachable node in the navigational graph
		// that allows the agent to reach the target block position
		int reachableNode = -1;
		float closestDistance = 3f; // Near the block to be able to interact later
		for (Node node : richNavGraph.getNodeMap().values()) {
			float distance = node.getPosition().distanceTo(targetPosition); // the target position of the widget to interact with
			if(distance < closestDistance){
				reachableNode = node.getId();
				closestDistance = distance;
			}
		}

		return (reachableNode != -1);
	}

	/**
	 * SE - Platform
	 * X and Z axes are the 2D to calculate the navigation movements. 
	 * Calculate the navigable position by adding circular coordinates to the current agent position. 
	 * 
	 * @param system
	 * @param state
	 * @param agentId
	 * @param actions
	 * @return actions
	 */
	public static Set<Action> calculateExploratoryPositions(SUT system, State state, String agentId, Set<Action> actions) {
		// Circular positions relative to the agent center
		// https://stackoverflow.com/a/5301049
		Vec3 agentCenter = SVec3.seToLab(state.get(IV4XRtags.agentWidget).get(IV4XRtags.seAgentPosition));

		//TODO: Obtain the observation range and iterate until the max radius

		// 1 block distance positions (near)
		// For near positions calculate 8 positions in circle
		int points = 8;
		double slice = 2 * Math.PI / points;
		double radius = 2.5;
		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			float newX = agentCenter.x + (float)(radius * Math.cos(angle));
			float newY = agentCenter.y + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a reachable position
			Vec3 nearPosition = new Vec3(newX, newY, agentCenter.z);
			if(sePositionRotationHelper.calculateIfPositionIsReachable(system, nearPosition)) {
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
			float newY = agentCenter.y + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a reachable position
			Vec3 medPosition = new Vec3(newX, newY, agentCenter.z);
			if(sePositionRotationHelper.calculateIfPositionIsReachable(system, medPosition)) {
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
			float newY = agentCenter.y + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a reachable position
			Vec3 farPosition = new Vec3(newX, newY, agentCenter.z);
			if(sePositionRotationHelper.calculateIfPositionIsReachable(system, farPosition)) {
				actions.add(new seActionExplorePosition(state, farPosition, system, agentId));
			}
		}

		// 4 block distance positions (far)
		// For far positions calculate 16 positions in circle
		points = 16;
		slice = 2 * Math.PI / points;
		radius = 10;
		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			float newX = agentCenter.x + (float)(radius * Math.cos(angle));
			float newY = agentCenter.y + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a reachable position
			Vec3 farPosition = new Vec3(newX, newY, agentCenter.z);
			if(sePositionRotationHelper.calculateIfPositionIsReachable(system, farPosition)) {
				actions.add(new seActionExplorePosition(state, farPosition, system, agentId));
			}
		}

		return actions;
	}

	/**
	 * Calculate if a specific position is reachable. 
	 * 
	 * @param system
	 * @param position
	 * @return true or false
	 */
	public static boolean calculateIfPositionIsReachable(SUT system, Vec3 position) {
		Vec3F destinationPosition = SVec3.labToSE(position);

		// Create a navigational graph of the largest grid
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
		Observer seObserver = seController.getObserver();
		String largestGridId = ObservationExtensionsKt.largestGrid(seObserver.observeBlocks()).getId();
		NavGraph navGraph = seObserver.navigationGraph(largestGridId);
		RichNavGraph richNavGraph = RichNavGraphKt.toRichGraph(navGraph);

		// Check if there is a reachable node in the navigational graph
		// that allows the agent to reach the position to explore
		int reachableNode = -1;
		float closestDistance = 0.5f; // Not exactly the same position but almost
		for (Node node : richNavGraph.getNodeMap().values()) {
			float distance = node.getPosition().distanceTo(destinationPosition); // the destination position to explore
			if(distance < closestDistance){
				reachableNode = node.getId();
				closestDistance = distance;
			}
		}

		return (reachableNode != -1);
	}

	/**
	 * Based on the distance between the agent and the target block, 
	 * obtain the rotation tolerance. 
	 * 
	 * @param distance
	 * @return
	 */
	public static float rotationToleranceByDistance(float distance) {
		//TODO: This tolerance should be calculated with a formula or with a plugin invocation d:)
		if(distance < 2.2f) {return 0.2f;}
		else if(distance < 2.5f) {return 0.18f;}
		else if(distance < 2.75f) {return 0.14f;}
		else if(distance < 3f) {return 0.12f;}
		else if(distance < 3.5f) {return 0.095f;}
		else if(distance < 4f) {return 0.07f;}
		else if(distance < 4.5f) {return 0.05f;}
		else if(distance < 5f) {return 0.04f;}
		else if(distance < 5.5f) {return 0.035f;}
		else if(distance < 6f) {return 0.03f;}
		return 0.025f;
	}

	public static float dot(Vec3F a, Vec3F b) {
		return (a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ());
	}

}

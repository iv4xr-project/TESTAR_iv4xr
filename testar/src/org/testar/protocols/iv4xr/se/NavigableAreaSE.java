package org.testar.protocols.iv4xr.se;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.fruit.alayer.State;
import org.fruit.alayer.Widget;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;

public class NavigableAreaSE {

	private Set<Widget> interactedEntities = new HashSet<>();
	private Widget pendingEntity;

	private Set<SphereSE> exploredSpace = new HashSet<>();
	private Set<SphereSE> unexploredSpace = new HashSet<>();

	private Set<Vec3> currentNearReachablePositions = new HashSet<>();
	private Set<Vec3> currentUnexploredReachablePositions = new HashSet<>();

	public void addInitialPosition(Vec3 initialPos) {
		SphereSE initialSphere = new SphereSE(initialPos);
		exploredSpace.add(initialSphere);
	}

	public Set<String> getInteractedEntitiesDescription() {
		Set<String> descriptionInteractedEntities = new HashSet<>();

		for(Widget w : interactedEntities) {
			String description = w.get(IV4XRtags.entityType) + "_" + w.get(IV4XRtags.entityId);
			descriptionInteractedEntities.add(description);
		}

		if(!descriptionInteractedEntities.isEmpty()) return descriptionInteractedEntities;
		return new HashSet<>(Arrays.asList("None"));
	}

	public boolean alreadyInteractedEntity(Widget entity) {
		String entityDescription = entity.get(IV4XRtags.entityType) + "_" + entity.get(IV4XRtags.entityId);
		for(Widget w : interactedEntities) {
			String interactedDescription = w.get(IV4XRtags.entityType) + "_" + w.get(IV4XRtags.entityId);
			if(entityDescription.equals(interactedDescription)) {
				return true;
			}
		}

		if(pendingEntity != null) {
			String pendingInteractDescription = pendingEntity.get(IV4XRtags.entityType) + "_" + pendingEntity.get(IV4XRtags.entityId);
			if(entityDescription.equals(pendingInteractDescription)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * With a reachable entity (calculated with pathfinding) check if was already interacted or pending to interact. 
	 * If not, add as a pending.
	 * 
	 * @param block
	 */
	public void addNewReachableEntity(Widget block) {
		String blockDescription = block.get(IV4XRtags.entityType) + "_" + block.get(IV4XRtags.entityId);
		// Check if this block widget was already explored and interacted
		for(Widget w : interactedEntities) {
			String interactedDescription = w.get(IV4XRtags.entityType) + "_" + w.get(IV4XRtags.entityId);
			if(blockDescription.equals(interactedDescription)) {
				return;
			}
		}
		// If not interacted add as a new pending to interact
		pendingEntity = block;
	}

	public Widget getNextEntity() {
		if(pendingEntity != null) {
			interactedEntities.add(pendingEntity);
		}
		return pendingEntity;
	}

	public String getNextEntityDescription() {
		if(pendingEntity != null) {
			return pendingEntity.get(IV4XRtags.entityType) + "_" + pendingEntity.get(IV4XRtags.entityId);
		}
		return "NoEntity";
	}

	public void resetNextEntity() {
		pendingEntity = null;
	}

	/**
	 * With a reachable position (calculated with pathfinding) check if was already explored or pending to explore. 
	 * If not, add as a unexplored. 
	 * 
	 * @param reachablePos
	 */
	public void updateExploredSpace(Vec3 reachablePos) {
		// First, add as a current reachablePosition
		currentNearReachablePositions.add(reachablePos);
		// Second, check if this position was already explored
		for(SphereSE exploredSphere : exploredSpace) {
			if(exploredSphere.pointInsideSphere(reachablePos.x, reachablePos.y, reachablePos.z)) {
				return;
			}
		}
		// Third, check if this position is already pending to explore
		for(SphereSE unexploredSphere : unexploredSpace) {
			if(unexploredSphere.pointInsideSphere(reachablePos.x, reachablePos.y, reachablePos.z)) {
				// This position is already pending to explore
				// Add the position as current unexplored reachable, but do not create a new sphere
				currentUnexploredReachablePositions.add(reachablePos);
				return;
			}
		}
		// Finally, if not explored or pending to explore, add as a new pending to explore
		unexploredSpace.add(new SphereSE(reachablePos));
		currentUnexploredReachablePositions.add(reachablePos);
	}

	public Vec3 getNextRandomUnexploredPosition() {
		// Option 1: Return some of the current reachable positions that are unexplored
		if(!currentUnexploredReachablePositions.isEmpty()) {
			// Get one of the unexplored positions
			Random rnd = new Random();
			Vec3 nextUnexplored = new ArrayList<Vec3>(currentUnexploredReachablePositions).get(rnd.nextInt(currentUnexploredReachablePositions.size()));
			// Remove from unexplored space and add as already explored space
			unexploredSpace.remove(new SphereSE(nextUnexplored));
			exploredSpace.add(new SphereSE(nextUnexplored));
			return nextUnexplored;
		}
		// Option 2: From all current reachable position return one of them
		if(!currentNearReachablePositions.isEmpty()) {
			Random rnd = new Random();
			Vec3 nextPosition = new ArrayList<Vec3>(currentNearReachablePositions).get(rnd.nextInt(currentNearReachablePositions.size()));
			return nextPosition;
		}
		// Option 3: Agent is in a small space and no reachable positions are available
		return null;
	}

	public Vec3 getNextFarUnexploredPosition(State state) {
		Vec3 agentPosition = SVec3.seToLab(state.get(IV4XRtags.agentWidget).get(IV4XRtags.seAgentPosition));
		// Option 1: Return some of the current reachable positions that are unexplored
		if(!currentUnexploredReachablePositions.isEmpty()) {
			Vec3 farthestPosition = null;
			float farthestDistance = 0f;
			// Get the farthest position of the unexplored positions
			for(Vec3 unexploredPosition : currentUnexploredReachablePositions) {
				if(Vec3.dist(agentPosition, unexploredPosition) > farthestDistance) {
					farthestDistance = Vec3.dist(agentPosition, unexploredPosition);
					farthestPosition = unexploredPosition;
				}
			}
			if(farthestPosition != null) {
				// Remove from unexplored space and add as already explored space
				unexploredSpace.remove(new SphereSE(farthestPosition));
				exploredSpace.add(new SphereSE(farthestPosition));
				System.out.println("getNextFarUnexploredPosition: " + farthestPosition + " , distance of: " + farthestDistance);
				return farthestPosition;
			}
		}
		// Option 2: From all current reachable position return one of them
		if(!currentNearReachablePositions.isEmpty()) {
			Random rnd = new Random();
			Vec3 nextPosition = new ArrayList<Vec3>(currentNearReachablePositions).get(rnd.nextInt(currentNearReachablePositions.size()));
			return nextPosition;
		}
		// Option 3: Agent is in a small space and no reachable positions are available
		return null;
	}


	public void resetNextPosition() {
		// Reset positions lists for next iteration
		currentNearReachablePositions = new HashSet<>();
		currentUnexploredReachablePositions = new HashSet<>();
	}

	public void printExploredPositionsInfo() {
		Set<Vec3> exploredPositions= new HashSet<>();
		for(SphereSE exploredSphere : exploredSpace) {
			exploredPositions.add(exploredSphere.getPosition());
		}
		System.out.println("*** EXPLORED POSITIONS " + exploredPositions.size() + " : " + exploredPositions);
	}

	public void printUnexploredPositionsInfo() {
		Set<Vec3> unexploredPositions= new HashSet<>();
		for(SphereSE unexploredSphere : unexploredSpace) {
			unexploredPositions.add(unexploredSphere.getPosition());
		}
		System.out.println("*** POSITIONS TO EXPLORE " + unexploredPositions.size() + " : " + unexploredPositions);
	}

	public Set<SVec3> discoveredReachablePositions(){
		Set<SVec3> discoveredPositions= new HashSet<>();
		for(SphereSE exploredSphere : exploredSpace) {
			discoveredPositions.add(SVec3.labToSVec3(exploredSphere.getPosition()));
		}
		for(SphereSE unexploredSphere : unexploredSpace) {
			discoveredPositions.add(SVec3.labToSVec3(unexploredSphere.getPosition()));
		}
		return discoveredPositions;
	}

	public Set<SVec3> discoveredUnexploredPositions(){
		Set<SVec3> discoveredUnexploredPositions= new HashSet<>();
		for(SphereSE unexploredSphere : unexploredSpace) {
			discoveredUnexploredPositions.add(SVec3.labToSVec3(unexploredSphere.getPosition()));
		}
		return discoveredUnexploredPositions;
	}
}

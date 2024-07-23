/***************************************************************************************************
 *
 * Copyright (c) 2019 - 2024 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019 - 2024 Open Universiteit - www.ou.nl
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

package org.testar.iv4xr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.lab.goals.labActionGoalEntityInteracted;
import eu.testar.iv4xr.actions.lab.goals.labActionGoalPositionInCloseRange;
import eu.testar.iv4xr.enums.IV4XRtags;
import nl.uu.cs.aplib.mainConcepts.ProgressStatus;

public class LabRecruitsExplorer {
	private Set<labActionGoalPositionInCloseRange> unexploredActionPositions = new HashSet<>();
	private Set<Vec3> exploredPositions = new HashSet<>();

	private Set<labActionGoalEntityInteracted> nonInteractedActionEntities = new HashSet<>();
	private Set<String> interactedEntities = new HashSet<>();

	/**
	 * If the position of the labActionGoalPositionInCloseRange has not been reached, 
	 * memorize the action to be executed later. 
	 * 
	 * @param action
	 */
	public void memorizeActionPosition(Action action) {
		if(action instanceof labActionGoalPositionInCloseRange) {
			Vec3 goalPosition = ((labActionGoalPositionInCloseRange)action).getGoalPosition();
			if(goalPosition!=null && !exploredPositions.contains(goalPosition)) {
				unexploredActionPositions.add((labActionGoalPositionInCloseRange)action);
				System.out.println("TESTAR memorizeActionPosition : " + goalPosition);
			}
		}
	}

	/**
	 * If the position of the labActionGoalPositionInCloseRange has not been reached, 
	 * memorize the action to be executed later. 
	 * 
	 * @param action
	 */
	public void memorizeActionEntity(Action action) {
		if(action instanceof labActionGoalEntityInteracted) {
			String entityId = ((labActionGoalEntityInteracted)action).getEntityId();
			if(entityId != null && !entityId.isEmpty() && !interactedEntities.contains(entityId)) {
				nonInteractedActionEntities.add((labActionGoalEntityInteracted)action);
				System.out.println("TESTAR memorizeActionEntity : " + entityId);
			}
		}
	}

	public Action prioritizeMemorizedAction(State state, Set<Action> actions) {
		Action prioritizedAction = null;

		// First, prioritize the actions with non-interacted entities
		// We dont know which button will open a door...
		// So just select one of them random
		if(!nonInteractedActionEntities.isEmpty()) {

			System.out.println("LabRecruitsExplorer: There are non interacted entities");
			
			// Get the visible interact actions
			Set<labActionGoalEntityInteracted> visibleInteractedActionEntities = new HashSet<>();
			for(Action action : actions) {
				if(action instanceof labActionGoalEntityInteracted) {
					visibleInteractedActionEntities.add((labActionGoalEntityInteracted) action);
				}
			}

			// Retain the visible interact actions that are not yet interacted
			visibleInteractedActionEntities.retainAll(nonInteractedActionEntities);
			if(!visibleInteractedActionEntities.isEmpty()) {
				
				System.out.println("LabRecruitsExplorer: There are VISIBLE non interacted entities");
				
				int randomIndex = new Random().nextInt(visibleInteractedActionEntities.size());
				prioritizedAction = new ArrayList<labActionGoalEntityInteracted>(visibleInteractedActionEntities).get(randomIndex);
				
				System.out.println("LabRecruitsExplorer: Selected: " + prioritizedAction.toShortString());
			}
		}

		// If there are not non-interacted entities
		if(prioritizedAction == null) {
			// And there are unexplored positions
			if(!unexploredActionPositions.isEmpty()) {
				
				System.out.println("LabRecruitsExplorer: There are non explored positions");

				// Get visible explore actions
				Set<labActionGoalPositionInCloseRange> visibleExploreActionPositions = new HashSet<>();
				for(Action action : actions) {
					if(action instanceof labActionGoalPositionInCloseRange) {
						visibleExploreActionPositions.add((labActionGoalPositionInCloseRange) action);
					}
				}

				// Retain the visible explore actions that are not yet interacted
				visibleExploreActionPositions.retainAll(unexploredActionPositions);
				if(!visibleExploreActionPositions.isEmpty()) {
					
					System.out.println("LabRecruitsExplorer: There are VISIBLE non explored positions");
					
					// Prioritize the non-explored far positions
					Vec3 agentPosition = state.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition);
					float farDistance = 0f;
					for(Action action : visibleExploreActionPositions) {
						// If no prioritizedAction selected
						// This is probably the first unexploredActionPositions
						if(prioritizedAction == null) {
							prioritizedAction = action;
							farDistance = Vec3.dist(agentPosition, ((labActionGoalPositionInCloseRange)action).getGoalPosition());
							continue;
						}

						// If the distance of current action is farther than the saved action
						// Save this farthest unexploredActionPositions
						float distance = Vec3.dist(agentPosition, ((labActionGoalPositionInCloseRange)action).getGoalPosition());
						if(distance > farDistance) {
							prioritizedAction = action;
							farDistance = distance;
						}
					}
					
					System.out.println("LabRecruitsExplorer: Selected: " + prioritizedAction.toShortString());
				}
			}
		}

		return prioritizedAction;
	}

	/**
	 * If goal were completed successfully, update the tracking entity or position info. 
	 * 
	 * @param executedAction
	 * @param goalStatus
	 */
	public void addExecutedAction(Action executedAction, ProgressStatus goalStatus) {
		if(executedAction instanceof labActionGoalPositionInCloseRange 
				&& ((labActionGoalPositionInCloseRange)executedAction).getGoalPosition() != null 
				&& goalStatus.success()) {
			Vec3 exploredPosition = ((labActionGoalPositionInCloseRange)executedAction).getGoalPosition();
			// Remove the position as unexplored			
			for (Iterator<labActionGoalPositionInCloseRange> it = unexploredActionPositions.iterator(); it.hasNext();) {
				labActionGoalPositionInCloseRange element = it.next();
				if(element.getGoalPosition().equals(exploredPosition)) {
					it.remove();
				}
			}
			// Then, add the position as explored
			exploredPositions.add(exploredPosition);
		}

		if(executedAction instanceof labActionGoalEntityInteracted 
				&& ((labActionGoalEntityInteracted)executedAction).getEntityId() != null 
				&& goalStatus.success()) {
			String interactedEntityId = ((labActionGoalEntityInteracted)executedAction).getEntityId();
			// Remove the entity as nonInteracted			
			for (Iterator<labActionGoalEntityInteracted> it = nonInteractedActionEntities.iterator(); it.hasNext();) {
				labActionGoalEntityInteracted element = it.next();
				if(element.getEntityId().equals(interactedEntityId)) {
					it.remove();
				}
			}
			// Then, add the entity as interacted
			interactedEntities.add(((labActionGoalEntityInteracted)executedAction).getEntityId());

			// If an entity is interacted, I want to reset the exploration memory
			// Because the agent needs to explore new possible opened areas
			unexploredActionPositions = new HashSet<>();
			exploredPositions = new HashSet<>();
		}
	}
}

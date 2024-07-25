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
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.lab.goals.labActionGoalEntityInteracted;
import eu.testar.iv4xr.actions.lab.goals.labActionGoalPositionInCloseRange;
import eu.testar.iv4xr.enums.IV4XRtags;

public class LabRecruitsExplorer {
	private Set<labActionGoalPositionInCloseRange> unexploredActionPositions = new HashSet<>();
	private Set<Vec3> exploredPositions = new HashSet<>();

	private Set<labActionGoalEntityInteracted> nonInteractedActionEntities = new HashSet<>();
	private Set<String> interactedEntities = new HashSet<>();

	public Set<labActionGoalPositionInCloseRange> getUnexploredActionPositions() {
		return unexploredActionPositions;
	}

	public Set<Vec3> getExploredPositions() {
		return exploredPositions;
	}

	public Set<labActionGoalEntityInteracted> getNonInteractedActionEntities() {
		return nonInteractedActionEntities;
	}

	public Set<String> getInteractedEntities() {
		return interactedEntities;
	}

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
				if(unexploredActionPositions.add((labActionGoalPositionInCloseRange)action)) {
					System.out.println("TESTAR memorizeActionPosition : " + goalPosition);
				}
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
				if(nonInteractedActionEntities.add((labActionGoalEntityInteracted)action)) {
					System.out.println("TESTAR memorizeActionEntity : " + entityId);
				}
			}
		}
	}

	public Action prioritizeMemorizedAction(State state, Set<Action> actions) {
		Action prioritizedAction = null;

		// First, prioritize the actions with non-interacted entities
		// We dont know which button will open a door...
		// So just select one of them random
		if(!nonInteractedActionEntities.isEmpty()) {
			// Prioritize the non-interacted near entities
			Vec3 agentPosition = state.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition);
			float nearDistance = 999f;
			for(Action action : nonInteractedActionEntities) {
				// If no prioritizedAction selected
				// This is probably the first nonInteractedActionEntities
				if(prioritizedAction == null) {
					prioritizedAction = action;
					nearDistance = Vec3.dist(agentPosition, ((labActionGoalEntityInteracted)action).getEntityPosition());
					continue;
				}

				// If the distance of current action is farther than the saved action
				// Save this farthest unexploredActionPositions
				float distance = Vec3.dist(agentPosition, ((labActionGoalEntityInteracted)action).getEntityPosition());
				if(distance < nearDistance) {
					prioritizedAction = action;
					nearDistance = distance;
				}
			}
		}

		// If there are not non-interacted entities
		if(prioritizedAction == null) {
			// And there are unexplored positions
			if(!unexploredActionPositions.isEmpty()) {
				// Step 1: Calculate the farthest distance
				Vec3 agentPosition = state.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition);
				float farDistance = 0f;

				for(Action action : unexploredActionPositions) {
					float distance = Vec3.dist(agentPosition, ((labActionGoalPositionInCloseRange)action).getGoalPosition());
					if(distance > farDistance) {
						farDistance = distance;
					}
				}

				// Step 2: Get all far explore actions within the threshold of 0.3 of the far distance
				List<Action> farActionsWithinThreshold = new ArrayList<>();
				float threshold = 0.5f;

				for(Action action : unexploredActionPositions) {
					float actionDistance = Vec3.dist(agentPosition, ((labActionGoalPositionInCloseRange)action).getGoalPosition());
					if(actionDistance >= farDistance - threshold) {
						farActionsWithinThreshold.add(action);
					}
				}

				// Step 3: Select one of these far explore actions randomly
				if(!farActionsWithinThreshold.isEmpty()) {
					Random random = new Random();
					int randomIndex = random.nextInt(farActionsWithinThreshold.size());
					prioritizedAction = farActionsWithinThreshold.get(randomIndex);
				}
			}
		}

		return prioritizedAction;
	}

	public Action prioritizeVisibleOfMemorizedAction(State state, Set<Action> actions) {
		Action prioritizedAction = null;

		// First, prioritize the actions with non-interacted entities
		if(!nonInteractedActionEntities.isEmpty()) {
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
				// Prioritize the non-interacted near entities
				Vec3 agentPosition = state.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition);
				float nearDistance = 999f;
				for(Action action : visibleInteractedActionEntities) {
					// If no prioritizedAction selected
					// This is probably the first nonInteractedActionEntities
					if(prioritizedAction == null) {
						prioritizedAction = action;
						nearDistance = Vec3.dist(agentPosition, ((labActionGoalEntityInteracted)action).getEntityPosition());
						continue;
					}

					// If the distance of current action is farther than the saved action
					// Save this farthest unexploredActionPositions
					float distance = Vec3.dist(agentPosition, ((labActionGoalEntityInteracted)action).getEntityPosition());
					if(distance < nearDistance) {
						prioritizedAction = action;
						nearDistance = distance;
					}
				}
			}
		}

		// If there are not non-interacted entities
		if(prioritizedAction == null) {
			// And there are unexplored positions
			if(!unexploredActionPositions.isEmpty()) {
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
					// Step 1: Calculate the farthest distance
					Vec3 agentPosition = state.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition);
					float farDistance = 0f;

					for(Action action : visibleExploreActionPositions) {
						float distance = Vec3.dist(agentPosition, ((labActionGoalPositionInCloseRange)action).getGoalPosition());
						if(distance > farDistance) {
							farDistance = distance;
						}
					}

					// Step 2: Get all far explore actions within the threshold of 0.3 of the far distance
					List<Action> farActionsWithinThreshold = new ArrayList<>();
					float threshold = 0.5f;

					for(Action action : visibleExploreActionPositions) {
						float actionDistance = Vec3.dist(agentPosition, ((labActionGoalPositionInCloseRange)action).getGoalPosition());
						if(actionDistance >= farDistance - threshold) {
							farActionsWithinThreshold.add(action);
						}
					}

					// Step 3: Select one of these far explore actions randomly
					if(!farActionsWithinThreshold.isEmpty()) {
						Random random = new Random();
						int randomIndex = random.nextInt(farActionsWithinThreshold.size());
						prioritizedAction = farActionsWithinThreshold.get(randomIndex);
					}
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
	public void addExecutedAction(Action executedAction, boolean resetMemory) {
		if(executedAction instanceof labActionGoalPositionInCloseRange 
				&& ((labActionGoalPositionInCloseRange)executedAction).getGoalPosition() != null) {
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
				&& ((labActionGoalEntityInteracted)executedAction).getEntityId() != null) {
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
			if(resetMemory) {
				unexploredActionPositions = new HashSet<>();
				exploredPositions = new HashSet<>();
			}
		}
	}
}

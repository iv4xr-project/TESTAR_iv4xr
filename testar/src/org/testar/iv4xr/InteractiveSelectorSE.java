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

package org.testar.iv4xr;

import java.util.HashSet;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.se.goals.seActionExplorePosition;
import eu.testar.iv4xr.actions.se.goals.seActionNavigateGrinderBlock;
import eu.testar.iv4xr.actions.se.goals.seActionNavigateInteract;
import eu.testar.iv4xr.actions.se.goals.seActionNavigateRechargeEnergy;
import eu.testar.iv4xr.actions.se.goals.seActionNavigateRechargeHealth;
import eu.testar.iv4xr.actions.se.goals.seActionNavigateShootBlock;
import eu.testar.iv4xr.actions.se.goals.seActionNavigateWelderBlock;
import eu.testar.iv4xr.actions.se.goals.seActionTriggerBlockConstruction;
import eu.testar.iv4xr.enums.IV4XRtags;

public class InteractiveSelectorSE {

	private static Set<String> interactedEntities = new HashSet<>();
	private static Set<Vec3> exploredPositions = new HashSet<>();

	public Action prioritizedAction(State state, Set<Action> actions) {
		Action prioritizedAction = null;

		// First, prioritize the construction of blocks
		prioritizedAction = prioritizeBlockConstruction(actions);

		if(prioritizedAction == null) {
			// Second, prioritize the interaction actions with non-interacted entities
			prioritizedAction = prioritizeInteractiveAction(actions);
		}

		if(prioritizedAction==null) {
			// Third, prioritize the exploration of new discovered positions
			prioritizedAction = prioritizeExploratoryMovement(state, actions);
		}

		return prioritizedAction;
	}

	private Action prioritizeInteractiveAction(Set<Action> actions) {
		for(Action action : actions) {
			//TODO: Refactor and create an interactive action superclass
			if(action instanceof seActionNavigateGrinderBlock 
					|| action instanceof seActionNavigateShootBlock
					|| action instanceof seActionNavigateWelderBlock
					|| action instanceof seActionNavigateInteract
					|| action instanceof seActionNavigateRechargeHealth
					|| action instanceof seActionNavigateRechargeEnergy) {

				if(!interactedEntities.contains(action.get(Tags.OriginWidget).get(IV4XRtags.entityId, ""))) {
					return action;
				}

			}
		}
		return null;
	}

	private Action prioritizeBlockConstruction(Set<Action> actions) {
		for(Action action : actions) {
			if(action instanceof seActionTriggerBlockConstruction) {

				if(!interactedEntities.contains(((seActionTriggerBlockConstruction) action).getBlockType())) {
					return action;
				}

			}
		}
		return null;
	}

	private Action prioritizeExploratoryMovement(State state, Set<Action> actions) {
		Action farExploratoryAction = null;
		for(Action action : actions) {
			if(action instanceof seActionExplorePosition) {
				// If this position was not explored previously
				if(!exploredPositions.contains(((seActionExplorePosition) action).getTargetPosition())) {
					// If we do not have any exploratory action yet, just assign
					if(farExploratoryAction == null) {farExploratoryAction = action;}
					// Else, calculate if the next action is moving the agent to a far position
					else {
						Vec3 agentPosition = state.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition);
						float savedActionDist = Vec3.dist(agentPosition, ((seActionExplorePosition) farExploratoryAction).getTargetPosition());
						float newActionDist = Vec3.dist(agentPosition, ((seActionExplorePosition) action).getTargetPosition());

						if(newActionDist > savedActionDist) {farExploratoryAction = action;}
					}
				}
			}
		}
		return farExploratoryAction;
	}

	public void addExecutedAction(Action action) {
		if(action instanceof seActionExplorePosition) {
			addExploredPosition(action);
		} else if(action instanceof seActionTriggerBlockConstruction) {
			addConstructionAction(action);
		} else {
			addInteractiveAction(action);
		}
	}

	private void addExploredPosition(Action action) {
		if(action instanceof seActionExplorePosition) {
			exploredPositions.add(((seActionExplorePosition) action).getTargetPosition());
		}
	}

	private void addConstructionAction(Action action) {
		if(action instanceof seActionTriggerBlockConstruction) {
			interactedEntities.add(((seActionTriggerBlockConstruction) action).getBlockType());
		}
	}

	private void addInteractiveAction(Action action) {
		//TODO: Refactor and create an interactive action superclass
		if(action instanceof seActionNavigateGrinderBlock 
				|| action instanceof seActionNavigateShootBlock
				|| action instanceof seActionNavigateWelderBlock
				|| action instanceof seActionNavigateInteract
				|| action instanceof seActionNavigateRechargeHealth
				|| action instanceof seActionNavigateRechargeEnergy) {
			interactedEntities.add(action.get(Tags.OriginWidget).get(IV4XRtags.entityId, ""));
		}
	}

}

/***************************************************************************************************
 *
 * Copyright (c) 2023 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2023 Open Universiteit - www.ou.nl
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

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fruit.Pair;
import org.fruit.alayer.Action;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.exceptions.SystemStartException;
import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.se.goals.seActionExplorePosition;
import eu.testar.iv4xr.actions.se.goals.seActionNavigateToBlock;
import eu.testar.iv4xr.actions.se.goals.seActionTriggerBlockConstruction;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import spaceEngineers.model.Vec3F;

public class InteractiveExplorerSE {

	private static Set<String> interactedEntities = new HashSet<>();
	private static Area exploredArea = new Area();
	private static Set<Vec3> exploredPositions = new HashSet<>();

	public Action prioritizedAction(State state, Set<Action> actions) {
		Action prioritizedAction = null;

		// First, prioritize the construction of blocks
		prioritizedAction = prioritizeBlockConstruction(actions);

		if(prioritizedAction == null) {
			// Second, prioritize the interaction actions with non-interacted entities
			prioritizedAction = prioritizeClosestInteractiveAction(state, actions);
		}

		if(prioritizedAction==null) {
			// Third, prioritize the exploration of new discovered positions areas
			prioritizedAction = prioritizeExploratoryArea(state, actions);
		}

		if(prioritizedAction==null) {
			// Fourth, prioritize the exploration of new discovered positions points
			prioritizedAction = prioritizeExploratoryMovement(state, actions);
		}

		return prioritizedAction;
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

	private Action prioritizeClosestInteractiveAction(State state, Set<Action> actions) {
		float distance = 0f;
		Action closestAction = null;

		for(Action action : actions) {
			if(action instanceof seActionNavigateToBlock) {
				// If entity was not interacted previously 
				if(!interactedEntities.contains(action.get(Tags.OriginWidget).get(IV4XRtags.entityId, ""))) {
					Vec3F agentPos = state.get(IV4XRtags.agentWidget).get(IV4XRtags.seAgentPosition);
					Vec3F blockPos = SVec3.labToSE(action.get(Tags.OriginWidget).get(IV4XRtags.entityPosition));
					float blockDistance = agentPos.distanceTo(blockPos);
					// If no action selected or the distance is closest, prioritize this action
					if(closestAction == null || blockDistance < distance) {
						distance = blockDistance;
						closestAction = action;
					}
				}
			}
		}

		return closestAction;
	}

	private Action prioritizeExploratoryArea(State state, Set<Action> actions) {
		Action farExploratoryAction = null;
		Rectangle2D.Double farExploratoryRect = null;
		for(Action action : actions) {
			if(action instanceof seActionExplorePosition) {
				Vec3 positionToExploreVec3 = ((seActionExplorePosition) action).getTargetPosition();
				Vec3F positionToExplore = new Vec3F(positionToExploreVec3.x, positionToExploreVec3.y, positionToExploreVec3.z);
				Rectangle2D.Double rectToExplore = obtainRectangleFromPosition(positionToExplore);

				// If this area was not explored previously
				if(!exploredArea.intersects(rectToExplore)) {
					// If we do not have any exploratory action yet, just assign
					if(farExploratoryAction == null) {
						farExploratoryAction = action;
						farExploratoryRect = rectToExplore;
					}
					// Else, calculate if the next action is moving far from the already explored area
					else {
						// Calculate the distance between the center of farExploratoryRect and the exploredArea
						double savedRectDistance = distanceToPoint(farExploratoryRect.getCenterX(), farExploratoryRect.getCenterY());

						// Calculate the distance between the center of rectToExplore and the exploredArea
						double newRectDistance = distanceToPoint(rectToExplore.getCenterX(), rectToExplore.getCenterY());

						// If the new rectangle is farthest away, update saved action and rect
						if(newRectDistance > savedRectDistance) {
							farExploratoryAction = action;
							farExploratoryRect = rectToExplore;
						}
					}
				}
			}
		}
		return farExploratoryAction;
	}

	/** 
	 * ChatGPT: 
	 * Calculates the minimum distance between a point (x,y) and the exploredArea path.
	 */ 
	private double distanceToPoint(double x, double y) {
		// An array to store the coordinates of the path segments
		double[] coords = new double[6];
		// A PathIterator object to iterate through the path segments
		PathIterator path = exploredArea.getPathIterator(null);

		// Set the initial distance to the maximum value, as a distance cannot be greater than that
		double distance = Double.MAX_VALUE;
		// Set the initial coordinates for the last point in the segment
		double lastX = 0, lastY = 0;
		// An array to store the coordinates of the first point in the path
		double[] firstCoords = new double[2];

		// Iterate through the path segments
		while (!path.isDone()) {
			// Get the type of the current segment and its coordinates
			int segType = path.currentSegment(coords);

			switch (segType) {
			case PathIterator.SEG_MOVETO:
				// If the segment type is MOVE_TO, store the coordinates of the first point in the path
				firstCoords[0] = coords[0];
				firstCoords[1] = coords[1];
				// Set the last point coordinates to the same as the first point
				lastX = coords[0];
				lastY = coords[1];
				break;
			case PathIterator.SEG_LINETO:
				// If the segment type is LINE_TO, calculate the distance from the last point to the current point
				// using the lineToPointDistance method
				double segmentDistance = lineToPointDistance(lastX, lastY, coords[0], coords[1], x, y);
				// If the distance is less than the current minimum distance, set it as the new minimum distance
				if (segmentDistance < distance) {
					distance = segmentDistance;
				}
				// Set the last point coordinates to the current point coordinates
				lastX = coords[0];
				lastY = coords[1];
				break;
			case PathIterator.SEG_CLOSE:
				// If the segment type is CLOSE, calculate the distance from the last point to the first point
				// using the lineToPointDistance method
				double segmentDistanceClose = lineToPointDistance(lastX, lastY, firstCoords[0], firstCoords[1], x, y);
				// If the distance is less than the current minimum distance, set it as the new minimum distance
				if (segmentDistanceClose < distance) {
					distance = segmentDistanceClose;
				}
				// Set the last point coordinates to the first point coordinates
				lastX = firstCoords[0];
				lastY = firstCoords[1];
				break;
			default:
				// Throw an error if the segment type is not recognized
				throw new AssertionError("Unknown segment type: " + segType);
			}

			// Move to the next segment
			path.next();
		}

		// Return the minimum distance
		return distance;
	}

	/**
	 * Calculates the shortest distance between a line and a point in two-dimensional space. 
	 * 
	 * @param x1 the x-coordinate of the start point of the line.
	 * @param y1 the y-coordinate of the start point of the line.
	 * @param x2 the x-coordinate of the end point of the line.
	 * @param y2 the y-coordinate of the end point of the line.
	 * @param px the x-coordinate of the point.
	 * @param py the y-coordinate of the point.
	 * @return the shortest distance between the line and the point.
	 */
	private double lineToPointDistance(double x1, double y1, double x2, double y2, double px, double py) {
		// Calculate the vector components for the line and point.
		double A = px - x1;
		double B = py - y1;
		double C = x2 - x1;
		double D = y2 - y1;

		// Calculate the dot product and length squared for the line vector.
		double dot = A * C + B * D;
		double len_sq = C * C + D * D;

		// Calculate the parameter for the closest point on the line to the given point.
		double param = dot / len_sq;

		double xx, yy;

		// If the parameter is less than zero, the closest point is the start point of the line.
		if (param < 0) {
			xx = x1;
			yy = y1;
		}
		// If the parameter is greater than one, the closest point is the end point of the line.
		else if (param > 1) {
			xx = x2;
			yy = y2;
		}
		// Otherwise, the closest point is on the line segment between the start and end points.
		else {
			xx = x1 + param * C;
			yy = y1 + param * D;
		}

		// Calculate the distance between the closest point on the line and the given point.
		return Math.sqrt(Math.pow(xx - px, 2) + Math.pow(yy - py, 2));
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
			// Explore actions only contains navigated nodes
			addNavigatedArea(((seActionExplorePosition) action).getNavigableNodes());
		} else if(action instanceof seActionTriggerBlockConstruction) {
			// Construction actions only creates blocks without moving
			addConstructionAction(action);
		} else if (action instanceof seActionNavigateToBlock) {
			// But NavigateToBlock
			// Interacts and navigates a set of nodes area
			addInteractedBlock(action);
			addNavigatedArea(((seActionNavigateToBlock) action).getNavigableNodes());
		}
	}

	/**
	 * Iterate through the navigated nodes to add the information to the explored area. 
	 * 
	 * @param action
	 */
	private void addNavigatedArea(List<Vec3F> navigatedNodes) {
		for(Vec3F exploredNode : navigatedNodes) {
			exploredArea.add(new Area(obtainRectangleFromPosition(exploredNode)));
			Vec3 exploredPosition = new Vec3(exploredNode.getX(), exploredNode.getY(), exploredNode.getZ());
			exploredPositions.add(exploredPosition);
		}
	}

	private Rectangle2D.Double obtainRectangleFromPosition(Vec3F exploredPosition) {
		Pair<String, String> agentPlatformOrientation = SpatialSequentialMap.getAgentPlatformOrientation();
		if(!agentPlatformOrientation.right().isEmpty()) {
			// Obtain the x and y values, taking into consideration the agent up orientation in the platform
			int x = Math.round(getPositionCoordinate(exploredPosition, agentPlatformOrientation.left()));
			int y = Math.round(getPositionCoordinate(exploredPosition, agentPlatformOrientation.right()));
			// 1x1 SE blocks contain this default value
			double cube_size = 2.5;
			// Then create the 2D rectangle and add it to the explored area
			Rectangle2D.Double rect = new Rectangle2D.Double(x - cube_size/2, y - cube_size/2, cube_size, cube_size);
			return rect;
		}
		return new Rectangle2D.Double(0, 0, 0, 0);
	}

	private float getPositionCoordinate(Vec3F position, String coordinate) {
		if(coordinate.equals("x")) {return position.getX();}
		else if(coordinate.equals("y")) {return position.getY();}
		else if(coordinate.equals("z")) {return position.getZ();}
		else throw new SystemStartException("InteractiveExplorerSE getAgentPositionCoordinate: error obtaining the desired position coordinate");
	}

	private void addConstructionAction(Action action) {
		if(action instanceof seActionTriggerBlockConstruction) {
			interactedEntities.add(((seActionTriggerBlockConstruction) action).getBlockType());
		}
	}

	private void addInteractedBlock(Action action) {
		if(action instanceof seActionNavigateToBlock) {
			interactedEntities.add(action.get(Tags.OriginWidget).get(IV4XRtags.entityId, ""));
		}
	}

}

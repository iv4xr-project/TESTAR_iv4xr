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
import java.util.Arrays;
import java.util.List;

import org.fruit.alayer.SUT;
import org.fruit.alayer.Widget;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.se.SeAgentTESTAR;
import uuspaceagent.DPos3;
import uuspaceagent.UUSeAgentState;

public class seReachablePositionHelper {

	private seReachablePositionHelper() {}

	/**
	 * Try to calculate a reachable position near the desired entity. 
	 * If this is not possible, return null. 
	 * 
	 * @param system
	 * @param w
	 * @return Vec3 or null
	 */
	public static Vec3 calculateAdjacentReachablePosToEntity(SUT system, Widget w) {
		// Calculate if we can reach the entity using the pathfinding algorithm
		SeAgentTESTAR testAgent = (SeAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		UUSeAgentState stateGrid = testAgent.getStateGrid();
		stateGrid.navgrid.enableFlying = false;
		stateGrid.updateState(testAgent.getId());

		Vec3 destination = w.get(IV4XRtags.entityPosition);

		var sqAgent = stateGrid.navgrid.gridProjectedLocation(stateGrid.wom.position);
		var sqDestination = stateGrid.navgrid.gridProjectedLocation(destination);
		List<DPos3> path = stateGrid.pathfinder2D.findPath(stateGrid.navgrid, sqAgent, sqDestination);

		if (path == null) {
			// the pathfinder cannot find a path
			System.out.println("### NO path to " + destination);

			// 3D adjacent positions
			//[{-3,-3,-3}{-3,-3,-2}..{0,-1,0}{0,0,-1}{0,0,0},{0,0,1},{0,1,0}..{3,3,2}{3,3,3}]
			List<Vec3> recalculate_adjacent = new ArrayList<Vec3>();
			for(int i = 0; i < 4; i++) {
				for(int j = 0; j < 4; j++) {
					for(int k = 0; k < 4; k++) {
						recalculate_adjacent.add(new Vec3(i, j, k));
						recalculate_adjacent.add(new Vec3(i*-1, j*-1, k*-1));
					}
				}
			}

			System.out.println("Try to recalculate destination with adjacent list: " + Arrays.toString(recalculate_adjacent.toArray()));

			for(Vec3 adjacentDestination : recalculate_adjacent) {
				Vec3 recalculatedDestination = Vec3.add(destination, adjacentDestination);

				sqAgent = stateGrid.navgrid.gridProjectedLocation(stateGrid.wom.position);
				sqDestination = stateGrid.navgrid.gridProjectedLocation(recalculatedDestination);
				path = stateGrid.pathfinder2D.findPath(stateGrid.navgrid, sqAgent, sqDestination);

				if(path != null) {
					// If we finally found a path to the entity, using an adjacent position, return the new recalculated position
					System.out.println("!!! Path recalculated to " + recalculatedDestination);
					return recalculatedDestination;
				} else {
					System.out.println("### NO modified path to " + recalculatedDestination);
				}
			}
		} else {
			// Path was not null in the beginning, this means that the initial destination was reachable
			return destination;
		}

		// It was not possible to calculate a reachable position near the desired entity, return null
		return null;
	}

	/**
	 * Calculate if a specific position is reachable. 
	 * 
	 * @param system
	 * @param position
	 * @return true or false
	 */
	public static boolean calculateIfPositionIsReachable(SUT system, Vec3 position) {
		// Calculate if we can reach a specific position using the pathfinding algorithm
		SeAgentTESTAR testAgent = (SeAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		UUSeAgentState stateGrid = testAgent.getStateGrid();
		stateGrid.navgrid.enableFlying = false;
		stateGrid.updateState(testAgent.getId());

		var sqAgent = stateGrid.navgrid.gridProjectedLocation(stateGrid.wom.position);
		var sqDestination = stateGrid.navgrid.gridProjectedLocation(position);
		List<DPos3> path = stateGrid.pathfinder2D.findPath(stateGrid.navgrid, sqAgent, sqDestination);

		if (path == null) {
			// the pathfinder cannot find a path
			return false;
		} else {
			// Path is available, this means that the initial destination was reachable
			return true;
		}
	}

}

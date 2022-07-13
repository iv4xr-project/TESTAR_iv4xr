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

import org.fruit.alayer.Role;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.iv4xrActionRoles;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.se.SeAgentTESTAR;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import uuspaceagent.UUTacticLib;

public class seActionExplorePosition extends seActionGoal {
	private static final long serialVersionUID = -5843747535124644882L;

	protected Vec3 targetPosition;

	public Vec3 getTargetPosition() {
		return targetPosition;
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

		this.testAgent = (SeAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		this.stateGrid = testAgent.getStateGrid();
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		navigateToReachablePosition();
	}

	/**
	 * Use UU approach to create a state grid and navigate until the desired block.  
	 * 
	 * @param system
	 */
	protected void navigateToReachablePosition() {
		stateGrid.updateState(agentId);

		/**
		 * Hardcoded temporally, we will need to use deviated square for calculation
		 */
		float THRESHOLD_SQUARED_DEVIATED_DISTANCE_TO_SQUARE = 2f;

		var sqDestination = stateGrid.navgrid.gridProjectedLocation(targetPosition);
		var centerOfSqDestination = stateGrid.navgrid.getSquareCenterLocation(sqDestination);

		GoalStructure G = nl.uu.cs.aplib.AplibEDSL.goal("explore position: " + targetPosition)
				.toSolve((Pair<Vec3,Vec3> positionAndOrientation) -> {
					var pos = positionAndOrientation.fst;
					return Vec3.sub(centerOfSqDestination,pos).lengthSq() <= THRESHOLD_SQUARED_DEVIATED_DISTANCE_TO_SQUARE;
				})
				.withTactic(UUTacticLib.navigateToTAC(targetPosition))
				.lift();

		testAgent.setGoal(G);

		int turn= 0;
		while(G.getStatus().inProgress()) {
			testAgent.update();
			turn++;
			if (turn >= 100) break;
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
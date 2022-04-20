/***************************************************************************************************
 *
 * Copyright (c) 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2021 Open Universiteit - www.ou.nl
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

import java.util.Collection;

import org.fruit.alayer.Role;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.iv4xrActionRoles;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import eu.testar.iv4xr.se.SeAgentTESTAR;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import spaceEngineers.model.Vec2F;
import spaceEngineers.model.Vec3F;
import uuspaceagent.UUTacticLib;

public class seActionNavigateToBlock extends seActionGoal {
	private static final long serialVersionUID = 1846118675335766867L;

	protected String widgetType;
	protected String widgetId;
	protected eu.iv4xr.framework.spatial.Vec3 widgetPosition;
	protected Vec3F targetPosition;
	protected final float DEGREES = 2416f;

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

		this.testAgent = (SeAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		this.stateGrid = testAgent.getStateGrid();
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		navigateToBlock(system);
		rotateToBlockDestination(system);
	}

	/**
	 * Use UU approach to create a state grid and navigate until the desired block.  
	 * 
	 * @param system
	 */
	protected void navigateToBlock(SUT system) {
		stateGrid.updateState(agentId);
		WorldEntity entity = getEntityByPosition(stateGrid.wom.elements.values(), widgetPosition.toString());

		/**
		 * Hardcoded temporally, we will need to use deviated square for calculation
		 */
		float THRESHOLD_SQUARED_DEVIATED_DISTANCE_TO_SQUARE = 15f;

		var sqDestination = stateGrid.navgrid.gridProjectedLocation(entity.position);
		var centerOfSqDestination = stateGrid.navgrid.getSquareCenterLocation(sqDestination);

		GoalStructure G = nl.uu.cs.aplib.AplibEDSL.goal("close to entity: " + widgetType + " : " + widgetId)
				.toSolve((Pair<Vec3,Vec3> positionAndOrientation) -> {
					var pos = positionAndOrientation.fst;
					return Vec3.sub(centerOfSqDestination,pos).lengthSq() <= THRESHOLD_SQUARED_DEVIATED_DISTANCE_TO_SQUARE;
				})
				.withTactic(UUTacticLib.navigateToEntity(entity))
				.lift();

		testAgent.setGoal(G);

		System.out.println("DEBUG: Execute Goal... ");

		int turn= 0;
		while(G.getStatus().inProgress()) {
			testAgent.update();
			turn++;
			if (turn >= 100) break;
		}
	}

	/**
	 * Obtain the WorldModel Entity by the concrete position. 
	 * 
	 * @param entities
	 * @param blockType
	 * @return
	 */
	private WorldEntity getEntityByPosition(Collection<WorldEntity> entities, String blockPosition){
		WorldEntity entity = null;
		for (WorldEntity we : entities) {
			if (we.properties.get("centerPosition") != null) {
				if (we.properties.get("centerPosition").toString().equals(blockPosition)) {
					return we;
				}
			}
			// Blocks from grid
			if(we.elements.size() > 0){
				entity = getEntityByPosition(we.elements.values(), blockPosition);
			}
		}

		return entity;
	}

	/**
	 * Rotate tick by tick until the agent aims the block destination. 
	 * Based on: https://github.com/iv4xr-project/iv4xr-se-plugin/blob/uubranch3D/JvmClient/src/jvmMain/java/uuspaceagent/UUTacticLib.java#L160
	 * 
	 * @param system
	 */
	protected void rotateToBlockDestination(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		spaceEngineers.controller.SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
		spaceEngineers.controller.Observer seObserver = seController.getObserver();

		eu.iv4xr.framework.spatial.Vec3 agentPosition = SVec3.seToLab(seObserver.observe().getPosition());
		eu.iv4xr.framework.spatial.Vec3 entityPosition = SVec3.seToLab(targetPosition);
		eu.iv4xr.framework.spatial.Vec3 directionToGo = eu.iv4xr.framework.spatial.Vec3.sub(agentPosition, entityPosition);
		eu.iv4xr.framework.spatial.Vec3 agentOrientation = SVec3.seToLab(seObserver.observe().getOrientationForward());

		directionToGo.y = 0;
		agentOrientation.y = 0;

		directionToGo = directionToGo.normalized();
		agentOrientation = agentOrientation.normalized();

		float cos_alpha = eu.iv4xr.framework.spatial.Vec3.dot(agentOrientation, directionToGo);

		while(cos_alpha > -0.99f) {
			// rotate faster until the aiming is close
			if(cos_alpha > -0.95f) {seCharacter.moveAndRotate(new Vec3F(0,0,0),  new Vec2F(0, DEGREES*0.007f), 0f, 1);}
			else {seCharacter.moveAndRotate(new Vec3F(0,0,0), Vec2F.Companion.getROTATE_RIGHT(), 0f, 1);}

			agentPosition = SVec3.seToLab(seObserver.observe().getPosition());
			entityPosition = SVec3.seToLab(targetPosition);
			directionToGo = eu.iv4xr.framework.spatial.Vec3.sub(agentPosition, entityPosition);
			agentOrientation = SVec3.seToLab(seObserver.observe().getOrientationForward());

			directionToGo.y = 0;
			agentOrientation.y = 0;

			directionToGo = directionToGo.normalized();
			agentOrientation = agentOrientation.normalized();

			cos_alpha = eu.iv4xr.framework.spatial.Vec3.dot(agentOrientation, directionToGo);
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
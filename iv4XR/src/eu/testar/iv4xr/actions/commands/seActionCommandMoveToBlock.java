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

package eu.testar.iv4xr.actions.commands;

import org.fruit.alayer.Action;
import org.fruit.alayer.Role;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.TaggableBase;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.testar.iv4xr.actions.iv4xrActionRoles;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import spaceEngineers.commands.ObservationArgs;
import spaceEngineers.commands.ObservationMode;
import spaceEngineers.controller.ProprietaryJsonTcpCharacterController;
import spaceEngineers.model.Vec2;
import spaceEngineers.model.Vec3;

public class seActionCommandMoveToBlock extends TaggableBase implements Action {
	private static final long serialVersionUID = -6720504661292573988L;

	private String agentId;
	private Vec3 targetOrientationForward;
	private Vec3 targetPosition;

	public seActionCommandMoveToBlock(Widget w, String agentId){
		this.agentId = agentId;
		this.set(Tags.OriginWidget, w);
		this.set(Tags.Role, iv4xrActionRoles.iv4xrActionCommandMoveInteract);
		this.targetOrientationForward = w.get(IV4XRtags.seOrientationForward);
		this.targetPosition = SVec3.labToSE(w.get(IV4XRtags.entityPosition));
		this.set(Tags.Desc, toShortString());
		// TODO: Update with Goal Solving agents
		this.set(IV4XRtags.agentAction, false);
		this.set(IV4XRtags.newActionByAgent, false);
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		ProprietaryJsonTcpCharacterController spaceEngController = system.get(IV4XRtags.iv4xrSpaceEngProprietaryTcpController);

		// 360 rotation = new Vec2(0, 2416f)

		// Rotate tick by tick until the agent has the same orientation forward that the block
		while(!targetOrientationForward.similar(spaceEngController.observe(new ObservationArgs(ObservationMode.BASIC)).getOrientationForward(), 0.01f)) {
			seCharacter.moveAndRotate(new Vec3(0,0,0), Vec2.Companion.getROTATE_RIGHT(), 0f);
		}

		//RIGHT = Vec3(1, 0, 0)
		//LEFT = Vec3(-1, 0, 0)
		//FORWARD = Vec3(0, 0, -1)
		//BACKWARD = Vec3(0, 0, 1)

		Vec3 previousDistance = new Vec3(0,0,0);
		int xDir = 1;
		int zDir = 1;

		// Calculate the distance and move until the agent is close to the block
		while(!targetPosition.similar(spaceEngController.observe(new ObservationArgs(ObservationMode.BASIC)).getPosition(), 1.7f)) {
			Vec3 currentPosition = spaceEngController.observe(new ObservationArgs(ObservationMode.BASIC)).getPosition();
			Vec3 movement = seDistance(targetPosition, currentPosition);
			movement = new Vec3(movement.getX() * xDir, movement.getY(), movement.getZ() * zDir);
			seCharacter.moveAndRotate(movement, new Vec2(0,0), 0f);

			// Check if agent is moving away or getting closer
			Vec3 currentDistance = seDistance(targetPosition, spaceEngController.observe(new ObservationArgs(ObservationMode.BASIC)).getPosition());
			if((Math.abs(previousDistance.getX()) - Math.abs(currentDistance.getX())) < 0) {
				xDir = xDir * -1;
			}
			if((Math.abs(previousDistance.getZ()) - Math.abs(currentDistance.getZ())) < 0) {
				zDir = zDir * -1;
			}
			previousDistance = currentDistance;
		}
	}

	private Vec3 seDistance(Vec3 targetPosition, Vec3 currentPosition) {
		return new Vec3(targetPosition.getX() - currentPosition.getX(), 0, targetPosition.getZ() - currentPosition.getZ());
	}

	@Override
	public String toShortString() {
		return "Move agent: " + agentId + " to widget: " + this.get(Tags.OriginWidget).get(IV4XRtags.entityType);
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

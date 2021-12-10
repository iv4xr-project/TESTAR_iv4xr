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

package eu.testar.iv4xr.actions.se.commands;

import org.fruit.Util;
import org.fruit.alayer.Role;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.devices.AWTKeyboard;
import org.fruit.alayer.devices.KBKeys;
import org.fruit.alayer.devices.Keyboard;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.testar.iv4xr.actions.iv4xrActionRoles;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import spaceEngineers.model.Vec2F;
import spaceEngineers.model.Vec3F;

public class seActionMoveToBlock extends seActionCommand {
	private static final long serialVersionUID = -6720504661292573988L;

	protected Widget targetBlock;
	protected Vec3F targetOrientationForward;
	protected Vec3F targetPosition;

	// 360 rotation = new Vec2(0, 2416f)
	protected final float DEGREES = 2416f;
	protected final int MOVEMENTTRIES = 1000;
	protected final int AIMTRIES = 151;
	protected final int JETPACKTRIES = 50;
	protected final Vec3F RIGHT = new Vec3F(1, 0, 0);
	protected final Vec3F LEFT = new Vec3F(-1, 0, 0);
	protected final Vec3F FORWARD = new Vec3F(0, 0, -1);
	protected final Vec3F BACKWARD = new Vec3F(0, 0, 1);

	public seActionMoveToBlock(Widget w, String agentId){
		this.agentId = agentId;
		this.targetBlock = w;
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
		rotateToBlockDestination(system);
		moveToBlock(system);
	}

	/**
	 * Rotate tick by tick until the agent aims the block destination. 
	 * Based on: https://github.com/iv4xr-project/iv4xr-se-plugin/blob/uubranch3D/JvmClient/src/jvmMain/java/uuspaceagent/UUTacticLib.java#L160
	 * 
	 * @param system
	 */
	protected void rotateToBlockDestination(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		spaceEngineers.controller.JsonRpcSpaceEngineers seRpcController = system.get(IV4XRtags.iv4xrSpaceEngRpcController);
		spaceEngineers.controller.Observer seObserver = seRpcController.getObserver();

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

	/**
	 * Calculate the distance and move until the agent is close to the block. 
	 * 
	 * @param system
	 */
	protected void moveToBlock(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		spaceEngineers.controller.JsonRpcSpaceEngineers seRpcController = system.get(IV4XRtags.iv4xrSpaceEngRpcController);
		spaceEngineers.controller.Observer seObserver = seRpcController.getObserver();

		Vec3F previousDistance = new Vec3F(0,0,0);
		int xDir = 1, zDir = 1, tries = 1;

		while(!targetPosition.similar(seObserver.observe().getPosition(), 3.2f) && tries < MOVEMENTTRIES) {
			// If something is blocking our walk movement, fly and use the jet pack to move in the air
			if(blockedByBlock(seObserver)) {
				jetpackToBlock(system);
				continue;
			}

			Vec3F currentPosition = seObserver.observe().getPosition();
			Vec3F movement = seDistance(targetPosition, currentPosition);
			movement = new Vec3F(movement.getX() * xDir, movement.getY(), movement.getZ() * zDir);
			seCharacter.moveAndRotate(movement, new Vec2F(0,0), 0f, 1);

			// Check if agent is moving away or getting closer
			Vec3F currentDistance = seDistance(targetPosition, seObserver.observe().getPosition());
			if((Math.abs(previousDistance.getX()) - Math.abs(currentDistance.getX())) < 0) {
				xDir = xDir * -1;
			}
			if((Math.abs(previousDistance.getZ()) - Math.abs(currentDistance.getZ())) < 0) {
				zDir = zDir * -1;
			}
			previousDistance = currentDistance;
			tries ++;
		}
	}

	private Vec3F seDistance(Vec3F targetPosition, Vec3F currentPosition) {
		return new Vec3F(targetPosition.getX() - currentPosition.getX(), 0, targetPosition.getZ() - currentPosition.getZ());
	}

	private boolean blockedByBlock(spaceEngineers.controller.Observer seObserver) {
		try {
			if(seObserver.observe().getTargetBlock() == null) return false;
			return !seObserver.observe().getTargetBlock().getId().equals(targetBlock.get(IV4XRtags.entityId));
		} catch(Exception e) {
			return false;
		}
	}

	/**
	 * Calculate the distance and move until the agent is close to the block. 
	 * 
	 * @param system
	 */
	protected void jetpackToBlock(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		spaceEngineers.controller.JsonRpcSpaceEngineers seRpcController = system.get(IV4XRtags.iv4xrSpaceEngRpcController);
		spaceEngineers.controller.Observer seObserver = seRpcController.getObserver();

		while(blockedByBlock(seObserver)) {
			jetpackFlyUp(system);
		}

		// First move forward some distance
		for(int i = 0; i < 20; i++) {
			seCharacter.moveAndRotate(Vec3F.Companion.getFORWARD(), new Vec2F(0,0), 0f, 1);
		}

		Vec3F previousDistance = new Vec3F(0,0,0);
		int xDir = 1, zDir = 1, tries = 1;

		// TODO: Jet pack movement seems that does not work as walking, 
		// For now limit the movement to small number of JETPACKTRIES
		while(!targetPosition.similar(seObserver.observe().getPosition(), 5f) && tries < JETPACKTRIES) {
			Vec3F currentPosition = seObserver.observe().getPosition();
			Vec3F movement = seDistance(targetPosition, currentPosition);
			movement = new Vec3F(movement.getX() * xDir, movement.getY(), movement.getZ() * zDir);
			seCharacter.moveAndRotate(movement, new Vec2F(0,0), 0f, 1);

			// Check if agent is moving away or getting closer
			Vec3F currentDistance = seDistance(targetPosition, seObserver.observe().getPosition());
			if((Math.abs(previousDistance.getX()) - Math.abs(currentDistance.getX())) < 0) {
				xDir = xDir * -1;
			}
			if((Math.abs(previousDistance.getZ()) - Math.abs(currentDistance.getZ())) < 0) {
				zDir = zDir * -1;
			}
			previousDistance = currentDistance;
			tries ++;
		}

		jetpackStop(system);
	}

	/**
	 * Use the agent jet pack and fly up. 
	 * 
	 * @param system
	 */
	private void jetpackFlyUp(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		seCharacter.turnOnJetpack();

		// SE API has not fly up command, we use the space keyboard
		Keyboard kb = AWTKeyboard.build();
		kb.press(KBKeys.VK_SPACE);
		Util.pause(0.2);
		kb.release(KBKeys.VK_SPACE);
	}

	private void jetpackStop(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		seCharacter.turnOffJetpack();
		seCharacter.moveAndRotate(new Vec3F(0, 0, 0), new Vec2F(200f, 0), 0f, 1);
	}

	/**
	 * Rotate until the agent is aiming the target block. 
	 * 
	 * @param system
	 */
	protected void aimToBlock(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		spaceEngineers.controller.JsonRpcSpaceEngineers seRpcController = system.get(IV4XRtags.iv4xrSpaceEngRpcController);
		spaceEngineers.controller.Observer seObserver = seRpcController.getObserver();

		int tries = 1;
		while(!targetBlockFound(seObserver) && tries < AIMTRIES) {		
			seCharacter.moveAndRotate(new Vec3F(0, 0, 0), new Vec2F(0, DEGREES*0.007f), 0f, 1);
			tries ++;
		}
	}

	private boolean targetBlockFound(spaceEngineers.controller.Observer seObserver) {
		try {
			if(seObserver.observe().getTargetBlock() == null) return false;
			return seObserver.observe().getTargetBlock().getId().equals(targetBlock.get(IV4XRtags.entityId));
		} catch(Exception e) {
			return false;
		}
	}

	@Override
	public String toShortString() {
		return "Move agent: " + agentId + " to block: " + this.get(Tags.OriginWidget).get(IV4XRtags.entityType);
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

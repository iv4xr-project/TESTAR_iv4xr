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

package eu.testar.iv4xr.actions.se;

import org.fruit.Util;
import org.fruit.alayer.Action;
import org.fruit.alayer.Role;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.TaggableBase;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.devices.AWTKeyboard;
import org.fruit.alayer.devices.KBKeys;
import org.fruit.alayer.devices.Keyboard;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.testar.iv4xr.actions.iv4xrActionRoles;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import spaceEngineers.model.Vec2;
import spaceEngineers.model.Vec3;

public class seActionMoveToBlock extends TaggableBase implements Action {
	private static final long serialVersionUID = -6720504661292573988L;

	protected String agentId;
	protected Widget targetBlock;
	protected Vec3 targetOrientationForward;
	protected Vec3 targetPosition;

	// 360 rotation = new Vec2(0, 2416f)
	protected final float DEGREES = 2416f;
	protected final int MOVEMENTTRIES = 1000;
	protected final int AIMTRIES = 20;
	protected final int JETPACKTRIES = 50;
	protected final Vec3 RIGHT = new Vec3(1, 0, 0);
	protected final Vec3 LEFT = new Vec3(-1, 0, 0);
	protected final Vec3 FORWARD = new Vec3(0, 0, -1);
	protected final Vec3 BACKWARD = new Vec3(0, 0, 1);

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
		//rotateToBlockOrientation(system);
		rotateToBlockOrientationWithTeleport(system);
		moveToBlock(system);
	}

	/**
	 * Rotate tick by tick until the agent has the same orientation forward that the block. 
	 * 
	 * @param system
	 */
	protected void rotateToBlockOrientation(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		spaceEngineers.controller.JsonRpcSpaceEngineers seRpcController = system.get(IV4XRtags.iv4xrSpaceEngRpcController);
		spaceEngineers.controller.Observer seObserver = seRpcController.getObserver();

		while(!targetOrientationForward.similar(seObserver.observe().getOrientationForward(), 0.05f)) {
			seCharacter.moveAndRotate(new Vec3(0,0,0), Vec2.Companion.getROTATE_RIGHT(), 0f);
		}
	}

	/**
	 * Use teleport feature to rotate the agent so that it has the same orientation forward that the target block. 
	 * 
	 * @param system
	 */
	protected void rotateToBlockOrientationWithTeleport(SUT system) {
		spaceEngineers.controller.JsonRpcSpaceEngineers seRpcController = system.get(IV4XRtags.iv4xrSpaceEngRpcController);
		spaceEngineers.controller.Observer seObserver = seRpcController.getObserver();

		// Agent position, target block orientation forward, agent orientation up
		seRpcController.getAdmin().getCharacter().teleport(seObserver.observe().getPosition(), targetOrientationForward, seObserver.observe().getOrientationUp());
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

		Vec3 previousDistance = new Vec3(0,0,0);
		int xDir = 1, zDir = 1, tries = 1;

		while(!targetPosition.similar(seObserver.observe().getPosition(), 1.7f) && tries < MOVEMENTTRIES) {
			// If something is blocking our walk movement, fly and use the jet pack to move in the air
			if(blockedByBlock(seObserver)) {
				jetpackToBlock(system);
				continue;
			}

			Vec3 currentPosition = seObserver.observe().getPosition();
			Vec3 movement = seDistance(targetPosition, currentPosition);
			movement = new Vec3(movement.getX() * xDir, movement.getY(), movement.getZ() * zDir);
			seCharacter.moveAndRotate(movement, new Vec2(0,0), 0f);

			// Check if agent is moving away or getting closer
			Vec3 currentDistance = seDistance(targetPosition, seObserver.observe().getPosition());
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

	private Vec3 seDistance(Vec3 targetPosition, Vec3 currentPosition) {
		return new Vec3(targetPosition.getX() - currentPosition.getX(), 0, targetPosition.getZ() - currentPosition.getZ());
	}

	private boolean blockedByBlock(spaceEngineers.controller.Observer seObserver) {
		if(seObserver.observe().getTargetBlock() == null) return false;
		return !seObserver.observe().getTargetBlock().getId().equals(targetBlock.get(IV4XRtags.entityId));
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

		Vec3 previousDistance = new Vec3(0,0,0);
		int xDir = 1, zDir = 1, tries = 1;

		// TODO: Jet pack movement seems that does not work as walking, 
		// For now limit the movement to small number of JETPACKTRIES
		while(!targetPosition.similar(seObserver.observe().getPosition(), 5f) && tries < JETPACKTRIES) {
			Vec3 currentPosition = seObserver.observe().getPosition();
			Vec3 movement = seDistance(targetPosition, currentPosition);
			movement = new Vec3(movement.getX() * xDir, movement.getY(), movement.getZ() * zDir);
			seCharacter.moveAndRotate(movement, new Vec2(0,0), 0f);

			// Check if agent is moving away or getting closer
			Vec3 currentDistance = seDistance(targetPosition, seObserver.observe().getPosition());
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
		seCharacter.moveAndRotate(new Vec3(0, 0, 0), new Vec2(200f, 0), 0f);
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
			seCharacter.moveAndRotate(new Vec3(0, 0, 0), new Vec2(0, DEGREES*0.05f), 0f);
			tries ++;
		}
	}

	private boolean targetBlockFound(spaceEngineers.controller.Observer seObserver) {
		if(seObserver.observe().getTargetBlock() == null) return false;
		return seObserver.observe().getTargetBlock().getId().equals(targetBlock.get(IV4XRtags.entityId));
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

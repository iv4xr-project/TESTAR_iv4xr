/***************************************************************************************************
 *
 * Copyright (c) 2020 - 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 - 2021 Open Universiteit - www.ou.nl
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

package eu.testar.iv4xr.actions.lab.commands;

import org.fruit.Util;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.iv4xrActionRoles;
import eu.testar.iv4xr.enums.IV4XRtags;

public class labActionCommandMoveInteract extends labActionCommand {
	private static final long serialVersionUID = 4185324298727667277L;

	private Vec3 targetPosition;
	private boolean jump;
	private String entityId;

	public Vec3 getTargetPosition() {
		return targetPosition;
	}

	public boolean getIfJump() {
		return jump;
	}

	public String getEntityId() {
		return entityId;
	}

	public void selectedByAgent() {
		this.set(IV4XRtags.agentAction, true);
	}

	public labActionCommandMoveInteract(Widget w, State state, LabRecruitsEnvironment labRecruitsEnvironment, String agentId, Vec3 targetPosition, boolean jump, boolean agentAction, boolean newByAgent){
		this.labRecruitsEnvironment = labRecruitsEnvironment;
		this.agentId = agentId;
		this.set(Tags.OriginWidget, w);
		this.entityId = w.get(IV4XRtags.entityId);
		this.set(Tags.Role, iv4xrActionRoles.iv4xrActionCommandMoveInteract);
		this.targetPosition = targetPosition;
		this.jump = jump;
		this.set(Tags.Desc, toShortString());
		this.set(IV4XRtags.agentAction, agentAction);
		this.set(IV4XRtags.newActionByAgent, newByAgent);

		setActionCommandTags(w, state, targetPosition);
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		int maxTriesMovement = 30;
		int triesMove = 0;

		// One pure movement command is not enough to move to the final target position
		// Try a max number of movements (if something blocking we will not reach the position)
		// Or check the distance
		while(triesMove < maxTriesMovement && Vec3.dist(currentAgentPosition(), targetPosition) > 1f) {
			labRecruitsEnvironment.moveToward(agentId, currentAgentPosition(), targetPosition);
			triesMove++;
		}

		Util.pause(2);

		labRecruitsEnvironment.interact(agentId, entityId, "Interact");
	}

	@Override
	public String toShortString() {
		return "Move the agent: " + agentId + " to interact with the entity: " + entityId;
	}

	public boolean actionEquals(labActionCommandMoveInteract action) {
		return this.agentId.equals(action.getAgentId())
				&& this.currentAgentPosition().equals(action.currentAgentPosition()) 
				&& this.targetPosition.equals(action.getTargetPosition())
				&& this.entityId.equals(action.getEntityId());
	}
}

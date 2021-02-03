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

package eu.testar.iv4xr.actions.commands;

import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import environments.LabRecruitsEnvironment;
import eu.testar.iv4xr.actions.iv4xrActionRoles;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.iv4xr.framework.spatial.Vec3;

public class labActionExploreSouth extends labActionCommand {
	private static final long serialVersionUID = 4431931844664688235L;
	
	public void selectedByAgent() {
		this.set(IV4XRtags.agentAction, true);
	}
	
	public labActionExploreSouth(Widget w, LabRecruitsEnvironment labRecruitsEnvironment, String agentId, boolean agentAction, boolean newByAgent){
		this.labRecruitsEnvironment = labRecruitsEnvironment;
		this.agentId = agentId;
		this.set(Tags.OriginWidget, w);
		this.set(Tags.Role, iv4xrActionRoles.iv4xrActionCommandExplore);
		this.set(Tags.Desc, toShortString());
		this.set(IV4XRtags.agentAction, agentAction);
		this.set(IV4XRtags.newActionByAgent, newByAgent);
	}
	
	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		// Move a bit to the South
		labRecruitsEnvironment.moveToward(agentId, currentAgentPosition(), addPositions(currentAgentPosition(), new Vec3(0.0f, 0.0f, -2.0f)));
	}

	@Override
	public String toShortString() {
		return "Agent: " + agentId + " is going to explore the South of the world from position " + currentAgentPosition();
	}
	
	/**
	 * Two labActionExploreSouth are equals if the same agent tries to move to the South
	 * from the coordinates of a nearby position
	 */
	public boolean actionEquals(labActionExploreSouth action) {
		return (this.agentId.equals(action.getAgentId()) 
				&& Vec3.dist(this.currentAgentPosition(), action.currentAgentPosition()) < 0.2);
	}
	
	private Vec3 addPositions(Vec3 original, Vec3 addend) {
		return new Vec3(original.x + addend.x, original.y + addend.y, original.z + addend.z);
	}
	
}

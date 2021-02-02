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

public class labActionCommandInteract extends labActionCommand {
	private static final long serialVersionUID = -2401401952551344201L;
	
	private String entityId;
	
	public String getEntityId() {
		return entityId;
	}
	
	public void selectedByAgent() {
		this.set(IV4XRtags.agentAction, true);
	}
	
	public labActionCommandInteract(Widget w, LabRecruitsEnvironment labRecruitsEnvironment, String agentId, boolean agentAction, boolean newByAgent) {
		this.labRecruitsEnvironment = labRecruitsEnvironment;
		this.agentId = agentId;
		this.set(Tags.OriginWidget, w);
		this.entityId = w.get(IV4XRtags.entityId);
		this.set(Tags.Role, iv4xrActionRoles.iv4xrActionCommandInteract);
		this.set(Tags.Desc, toShortString());
		this.set(IV4XRtags.agentAction, agentAction);
		this.set(IV4XRtags.newActionByAgent, newByAgent);
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		labRecruitsEnvironment.interactWith(agentId, entityId);
	}

	@Override
	public String toShortString() {
		return "Agent: " + agentId + " is doing an INTERACTION with: " + entityId;
	}
	
	public boolean actionEquals(labActionCommandInteract action) {
		return (this.agentId.equals(action.getAgentId())) && (this.entityId.equals(action.getEntityId()));
	}

}

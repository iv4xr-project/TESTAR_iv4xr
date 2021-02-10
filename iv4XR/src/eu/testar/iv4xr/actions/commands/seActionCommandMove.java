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

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.iv4xrActionRoles;
import spaceEngineers.SeRequest;
import spaceEngineers.SpaceEngEnvironment;
import spaceEngineers.commands.SeAgentCommand;

public class seActionCommandMove extends TaggableBase implements Action {
	private static final long serialVersionUID = -6582285412839242075L;
	
	private SpaceEngEnvironment spaceEngEnvironment;
	private String agentId;
	private Vec3 targetPosition;
	
	public seActionCommandMove(Widget w, SpaceEngEnvironment spaceEngEnvironment, String agentId, Vec3 targetPosition){
		this.spaceEngEnvironment = spaceEngEnvironment;
		this.agentId = agentId;
		this.set(Tags.OriginWidget, w);
		this.set(Tags.Role, iv4xrActionRoles.iv4xrActionCommandMove);
		this.targetPosition = targetPosition;
		this.set(Tags.Desc, toShortString());
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		spaceEngEnvironment.getSeResponse(SeRequest.command(SeAgentCommand.moveTowardCommand(agentId, targetPosition, false)));
	}

	@Override
	public String toShortString() {
		return "Move agent: " + agentId + " to: " + targetPosition;
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
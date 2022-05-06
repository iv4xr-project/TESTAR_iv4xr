/***************************************************************************************************
 *
 * Copyright (c) 2020 - 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 - 2022 Open Universiteit - www.ou.nl
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

import java.util.Map;
import org.fruit.alayer.Action;
import org.fruit.alayer.Role;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tag;
import org.fruit.alayer.TaggableBase;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.enums.IV4XRMapping;
import eu.testar.iv4xr.enums.IV4XRtags;

public class labActionCommand extends TaggableBase implements Action {
	private static final long serialVersionUID = 3825016139364224082L;

	protected LabRecruitsEnvironment labRecruitsEnvironment;
	protected String agentId;

	public String getAgentId() {
		return agentId;
	}

	protected Vec3 currentAgentPosition() {
		return labRecruitsEnvironment.observe(agentId).position;
	}

	// This method is to avoid invoking an observation in the listener environment
	private Vec3 currentAgentPosition(State state) {
		return state.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition);
	}

	protected void setActionCommandTags(Widget widget, State state, Vec3 targetPosition) {
		// iv4xr Action Tags
		this.set(IV4XRtags.iv4xrActionOriginWidgetId, widget.get(Tags.AbstractIDCustom));
		this.set(IV4XRtags.iv4xrActionOriginWidgetPath, widget.get(Tags.Path));
		this.set(IV4XRtags.iv4xrActionOriginStateId, state.get(Tags.AbstractIDCustom));
		this.set(IV4XRtags.iv4xrActionEntityId, widget.get(IV4XRtags.entityId));
		this.set(IV4XRtags.iv4xrActionEntityIsActive, widget.get(IV4XRtags.labRecruitsEntityIsActive));
		this.set(IV4XRtags.iv4xrActionOriginPos, currentAgentPosition(state));

		/**
		 * TargetPosition is the "intention" of the position movement. 
		 * We will try to move to a specific position, but we don't know if will be possible. 
		 * Add absolute and relative intent position to move 
		 */

		// Some action like interact command has not agent movement
		if(targetPosition == null) {
			this.set(IV4XRtags.iv4xrActionTargetAbsPos, currentAgentPosition(state));
			this.set(IV4XRtags.iv4xrActionTargetRelPos, new Vec3(0, 0, 0));
		} else {
			this.set(IV4XRtags.iv4xrActionTargetAbsPos, targetPosition);
			this.set(IV4XRtags.iv4xrActionTargetRelPos, Vec3.sub(targetPosition, currentAgentPosition(state)));
		}

		setActionManagementTags(this);
	}

	@SuppressWarnings("unchecked")
	private void setActionManagementTags(Action action) {
		for(Map.Entry<Tag<?>, Tag<?>> entry : IV4XRMapping.getActionTagMap().entrySet()) {
			Tag actionManagementTag = entry.getKey();
			action.set(actionManagementTag, action.get(entry.getValue()));
		}
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		// TODO Auto-generated method stub

	}

	@Override
	public String toShortString() {
		// TODO Auto-generated method stub
		return null;
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

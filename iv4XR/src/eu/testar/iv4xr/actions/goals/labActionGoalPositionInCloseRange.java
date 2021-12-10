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

package eu.testar.iv4xr.actions.goals;

import org.fruit.alayer.SUT;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;

import eu.testar.iv4xr.actions.iv4xrActionRoles;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.labrecruits.LabRecruitsAgentTESTAR;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

public class labActionGoalPositionInCloseRange extends labActionGoal {

	private static final long serialVersionUID = -3142712768999384558L;

	private Vec3 goalPosition;

	public labActionGoalPositionInCloseRange(Widget w, SUT system, GoalStructure goalStructure, Vec3 goalPosition) {
		this.goalStructure = goalStructure;
		this.set(Tags.OriginWidget, w);
		this.entityId = w.get(IV4XRtags.entityId);
		this.set(Tags.Role, iv4xrActionRoles.iv4xrActionGoalPositionInCloseRange);
		this.goalPosition = goalPosition;
		this.set(Tags.Desc, toShortString());
		this.set(IV4XRtags.agentAction, false);
		this.set(IV4XRtags.newActionByAgent, false);

		// Set the goal to the agent
		LabRecruitsAgentTESTAR agentTESTAR = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		this.agentId = agentTESTAR.getId();
		agentTESTAR.setGoal(goalStructure);
	}

	@Override
	public String toShortString() {
		return "Agent: " + agentId + " executing Goal PositionInCloseRange to " + goalPosition;
	}

}

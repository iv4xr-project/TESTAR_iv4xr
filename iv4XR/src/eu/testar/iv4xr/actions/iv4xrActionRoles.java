/***************************************************************************************************
 *
 * Copyright (c) 2020 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 Open Universiteit - www.ou.nl
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

package eu.testar.iv4xr.actions;

import org.fruit.alayer.Role;
import org.fruit.alayer.actions.ActionRoles;

public class iv4xrActionRoles {

	private iv4xrActionRoles() {}

	public static final Role

	iv4xrAction = Role.from("iv4xrAction", ActionRoles.Action),

	// Command Actions based on environments.LabRecruitsEnvironment
	iv4xrLowActionCommand = Role.from("iv4xrLowActionCommand", iv4xrAction),
	iv4xrLowActionCommandInteract = Role.from("iv4xrLowActionCommandInteract", iv4xrLowActionCommand),
	iv4xrLowActionCommandMove = Role.from("iv4xrLowActionCommandMove", iv4xrLowActionCommand),
	iv4xrLowActionCommandObserver = Role.from("iv4xrLowActionCommandObserver", iv4xrLowActionCommand),

	// Goal Actions based on agents.tactics.GoalLib
	iv4xrHighActionGoal = Role.from("iv4xrHighActionGoal", iv4xrAction),
	iv4xrHighActionGoalPositionInCloseRange = Role.from("iv4xrHighActionGoalPositionInCloseRange", iv4xrHighActionGoal),
	iv4xrHighActionGoalPositionsVisited = Role.from("iv4xrHighActionGoalPositionsVisited", iv4xrHighActionGoal),
	iv4xrHighActionGoalEntityInCloseRange = Role.from("iv4xrHighActionGoalEntityInCloseRange", iv4xrHighActionGoal),
	iv4xrHighActionGoalEntityInteracted = Role.from("iv4xrHighActionGoalEntityInteracted", iv4xrHighActionGoal),
	iv4xrHighActionGoalEntityStateRefreshed = Role.from("iv4xrHighActionGoalEntityStateRefreshed", iv4xrHighActionGoal),
	iv4xrHighActionGoalEntityInspected = Role.from("iv4xrHighActionGoalEntityInspected", iv4xrHighActionGoal),
	iv4xrHighActionGoalEntityInvariantChecked = Role.from("iv4xrHighActionGoalEntityInvariantChecked", iv4xrHighActionGoal),
	iv4xrHighActionGoalInvariantChecked = Role.from("iv4xrHighActionGoalInvariantChecked", iv4xrHighActionGoal),
	iv4xrHighActionGoalMemorySent= Role.from("iv4xrHighActionGoalMemorySent", iv4xrHighActionGoal),
	iv4xrHighActionGoalPingSent = Role.from("iv4xrHighActionGoalPingSent", iv4xrHighActionGoal);

}

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

package eu.testar.iv4xr.actions;

import org.fruit.alayer.Role;
import org.fruit.alayer.actions.ActionRoles;

public class iv4xrActionRoles {

	private iv4xrActionRoles() {}

	public static final Role

	iv4xrAction = Role.from("iv4xrAction", ActionRoles.Action),

	// Command Actions based on environments.LabRecruitsEnvironment
	iv4xrActionCommand = Role.from("iv4xrActionCommand", iv4xrAction),
	iv4xrActionCommandInteract = Role.from("iv4xrActionCommandInteract", iv4xrActionCommand),
	iv4xrActionCommandMove = Role.from("iv4xrActionCommandMove", iv4xrActionCommand),
	iv4xrActionCommandMoveInteract = Role.from("iv4xrActionCommandMoveInteract", iv4xrActionCommand),
	iv4xrActionCommandObserver = Role.from("iv4xrActionCommandObserver", iv4xrActionCommand),
	iv4xrActionCommandExplore = Role.from("iv4xrActionCommandExplore", iv4xrActionCommandMove),

	// Goal Actions based on agents.tactics.GoalLib
	iv4xrActionGoal = Role.from("iv4xrActionGoal", iv4xrAction),
	iv4xrActionGoalPositionInCloseRange = Role.from("iv4xrActionGoalPositionInCloseRange", iv4xrActionGoal),
	iv4xrActionGoalPositionsVisited = Role.from("iv4xrActionGoalPositionsVisited", iv4xrActionGoal),
	iv4xrActionGoalEntityInCloseRange = Role.from("iv4xrActionGoalEntityInCloseRange", iv4xrActionGoal),
	iv4xrActionGoalReachInteractEntity = Role.from("iv4xrActionGoalReachInteractEntity", iv4xrActionGoal),
	iv4xrActionGoalEntityInteracted = Role.from("iv4xrActionGoalEntityInteracted", iv4xrActionGoal),
	iv4xrActionGoalEntityStateRefreshed = Role.from("iv4xrActionGoalEntityStateRefreshed", iv4xrActionGoal),
	iv4xrActionGoalEntityInspected = Role.from("iv4xrActionGoalEntityInspected", iv4xrActionGoal),
	iv4xrActionGoalEntityInvariantChecked = Role.from("iv4xrActionGoalEntityInvariantChecked", iv4xrActionGoal),
	iv4xrActionGoalInvariantChecked = Role.from("iv4xrActionGoalInvariantChecked", iv4xrActionGoal),
	iv4xrActionGoalMemorySent= Role.from("iv4xrActionGoalMemorySent", iv4xrActionGoal),
	iv4xrActionGoalPingSent = Role.from("iv4xrActionGoalPingSent", iv4xrActionGoal);

}

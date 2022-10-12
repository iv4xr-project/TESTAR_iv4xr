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

package eu.testar.iv4xr.enums;

import java.util.HashSet;
import java.util.Set;

import org.fruit.alayer.Tag;
import org.fruit.alayer.TagsBase;
import org.fruit.alayer.Widget;
import org.fruit.alayer.windows.WinProcess;

import spaceEngineers.controller.InventorySide;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.model.TerminalControlPanelData;
import spaceEngineers.model.TerminalInventoryData;
import spaceEngineers.model.TerminalProductionData;
import eu.iv4xr.framework.environments.W3DEnvironment;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.labrecruits.listener.LabRecruitsEnvironmentListener;
import world.LabWorldModel;
import world.Observation;

public class IV4XRtags extends TagsBase {

	private IV4XRtags() {}

	/**
	 * iv4XR Environment Tags 
	 */

	// Generic W3D Environment
	public static final Tag<W3DEnvironment> iv4xrW3DEnvironment = from("iv4xrW3DEnvironment", W3DEnvironment.class);

	// TESTAR Windows Process layer, that allows interact with iv4XR SUTs using Windows API
	public static final Tag<WinProcess> windowsProcess = from("windowsProcess", WinProcess.class);

	// Specific Lab Recruits Environment, layer that facilitates the communication between agents and the Lab Recruits game
	public static final Tag<TestAgent> iv4xrTestAgent = from("iv4xrTestAgent", TestAgent.class);

	// iv4xr Test Agent that allows to execute tactics and goals
	public static final Tag<LabRecruitsEnvironmentListener> iv4xrLabRecruitsEnvironment = from("LabRecruitsEnvironment", LabRecruitsEnvironmentListener.class);

	// Specific SpaceEngineers Environment, layer that facilitates the communication between agents and the SpaceEngineers game
	public static final Tag<SpaceEngineers> iv4xrSpaceEngineers = from("iv4xrSpaceEngineers", SpaceEngineers.class);
	public static final Tag<spaceEngineers.controller.Character> iv4xrSpaceEngCharacter = from("iv4xrSpaceEngCharacter", spaceEngineers.controller.Character.class);
	public static final Tag<spaceEngineers.controller.Items> iv4xrSpaceEngItems = from("iv4xrSpaceEngItems", spaceEngineers.controller.Items.class);

	/**
	 * World Object Model Tags
	 */

	// Specific WOM for Lab Recruits game
	public static final Tag<LabWorldModel> iv4xrLabWorldModel = from("iv4xrLabWorldModel", LabWorldModel.class);

	// Specific Entities Observation for Lab Recruits game 
	public static final Tag<Observation> iv4xrObservation = from("Observation", Observation.class);

	// Specific tag to check if Lab Recruits game is in the foreground (is generic because it works at Windows process level)
	public static final Tag<Boolean> labRecruitsForeground = from("labRecruitsForeground", Boolean.class);

	// Tag that contains the LabRecruits NavMesh information (node and position)
	@SuppressWarnings("unchecked")
	public static final Tag<Set<SVec3>> labRecruitsNavMesh = from("labRecruitsNavMesh", (Class<Set<SVec3>>) (Class<?>) HashSet.class);

	/**
	 * Entity - Agent - Widget Tags
	 */

	// Property used by TESTAR to determine which widget is the Agent Entity
	public static final Tag<Widget> agentWidget = from("agentWidget", Widget.class);

	// The position of the WOM Agent
	public static final Tag<Vec3> agentPosition = from("agentPosition", Vec3.class);

	// Property used by TESTAR to determine if some widget is enabled / disabled
	public static final Tag<Boolean> entityEnabled = from("entityEnabled", Boolean.class);

	// Property used by TESTAR to determine if some widget exists with it is blocked by other, and not ready to interact with
	public static final Tag<Boolean> entityBlocked = from("entityBlocked", Boolean.class);

	// The center position of WOM Entity
	public static final Tag<Vec3> entityPosition = from("entityPosition", Vec3.class);
	public static final Tag<String> entityPositionRepresentation = from("entityPositionRepresentation", String.class);

	// Bounds box of WOM Entity
	public static final Tag<Vec3> entityBounds = from("entityBounds", Vec3.class);

	// Velocity of WOM Entity
	public static final Tag<Vec3> entityVelocity = from("entityVelocity", Vec3.class);

	// If WOM Entity is Dynamic
	public static final Tag<Boolean> entityDynamic = from("entityDynamic", Boolean.class);

	// A unique identifier of the WOM Entity
	public static final Tag<String> entityId = from("entityId", String.class);

	// A unique identifier of the WOM Agent
	public static final Tag<String> agentId = from("agentId", String.class);

	// Generic Type or Role of the WOM Entity
	public static final Tag<String> entityType = from("entityType", String.class);

	// Last time the State of the WOM Entity is sampled
	public static final Tag<Long> entityTimestamp = from("entityTimestamp", Long.class);

	// Property used by Lab Recruits game to determine if some Entity is Active or not
	public static final Tag<Boolean> labRecruitsEntityIsActive = from("labRecruitsEntityIsActive", Boolean.class);

	// Specific Lab Recruits game property ...
	public static final Tag<Integer> labRecruitsEntityLastUpdated = from("labRecruitsEntityLastUpdated", Integer.class);

	// Specific LabRecruits Agent properties
	public static final Tag<Integer> labRecruitsAgentHealth = from("labRecruitsAgentHealth", Integer.class);
	public static final Tag<Integer> labRecruitsAgentScore = from("labRecruitsAgentScore", Integer.class);
	public static final Tag<String> labRecruitsAgentMood = from("labRecruitsAgentMood", String.class);

	// Specific Space Engineers properties
	public static final Tag<spaceEngineers.model.Vec3F> seAgentPosition = from("seAgentPosition", spaceEngineers.model.Vec3F.class);
	public static final Tag<spaceEngineers.model.Vec3F> seAgentOrientationForward = from("seAgentOrientationForward", spaceEngineers.model.Vec3F.class);
	public static final Tag<spaceEngineers.model.Vec3F> seAgentOrientationUp = from("seAgentOrientationUp", spaceEngineers.model.Vec3F.class);
	public static final Tag<Float> seAgentHealth = from("seAgentHealth", Float.class);
	public static final Tag<Float> seAgentOxygen = from("seAgentOxygen", Float.class);
	public static final Tag<Float> seAgentEnergy = from("seAgentEnergy", Float.class);
	public static final Tag<Float> seAgentHydrogen = from("seAgentHydrogen", Float.class);
	public static final Tag<Boolean> seAgentJetpackRunning = from("seAgentJetpackRunning", Boolean.class);
	public static final Tag<Boolean> seAgentDampenersOn = from("seAgentDampenersOn", Boolean.class);
	public static final Tag<Boolean> seUnknownScreen = from("seUnknownScreen", Boolean.class);

	// Grid properties
	public static final Tag<String> seGridName = from("seGridName", String.class);
	public static final Tag<String> seGridDisplayName = from("seGridDisplayName", String.class);
	public static final Tag<Float> seGridMass = from("seGridMass", Float.class);
	public static final Tag<Boolean> seGridParked = from("seGridParked", Boolean.class);

	// Block properties
	public static final Tag<Float> seBuildIntegrity = from("seBuildIntegrity", Float.class);
	public static final Tag<Float> seIntegrity = from("seIntegrity", Float.class);
	public static final Tag<Float> seMaxIntegrity = from("seMaxIntegrity", Float.class);
	public static final Tag<spaceEngineers.model.Vec3F> seMaxPosition = from("seMaxPosition", spaceEngineers.model.Vec3F.class);
	public static final Tag<spaceEngineers.model.Vec3F> seMinPosition = from("seMinPosition", spaceEngineers.model.Vec3F.class);
	public static final Tag<spaceEngineers.model.Vec3F> seOrientationForward = from("seOrientationForward", spaceEngineers.model.Vec3F.class);
	public static final Tag<spaceEngineers.model.Vec3F> seOrientationUp = from("seOrientationUp", spaceEngineers.model.Vec3F.class);
	public static final Tag<spaceEngineers.model.Vec3F> seSize = from("seSize", spaceEngineers.model.Vec3F.class);
	public static final Tag<String> seDefinitionId = from("seDefinitionId", String.class);
	public static final Tag<Boolean> seFunctional = from("seFunctional", Boolean.class);
	public static final Tag<Boolean> seWorking = from("seWorking", Boolean.class);
	public static final Tag<String> seOwnerId = from("seOwnerId", String.class);
	public static final Tag<String> seBuiltBy = from("seBuiltBy", String.class);

	public static final Tag<String> seCustomName = from("seCustomName", String.class);
	public static final Tag<Boolean> seShowInInventory = from("seShowInInventory", Boolean.class);
	public static final Tag<Boolean> seShowInTerminal = from("seShowInTerminal", Boolean.class);
	public static final Tag<Boolean> seShowOnHUD = from("seShowOnHUD", Boolean.class);
	public static final Tag<Boolean> seFunctionalEnabled = from("seFunctionalEnabled", Boolean.class);
	public static final Tag<Boolean> seDoorOpen = from("seDoorOpen", Boolean.class);
	public static final Tag<Boolean> seDoorAnyoneCanUse = from("seDoorAnyoneCanUse", Boolean.class);
	public static final Tag<Float> seFuelMaxOutput = from("seFuelMaxOutput", Float.class);
	public static final Tag<Float> seFuelCurrentOutput = from("seFuelCurrentOutput", Float.class);
	public static final Tag<Float> seFuelCapacity = from("seFuelCapacity", Float.class);

	// Terminal properties
	public static final Tag<String> seFocusedScreen = from("seFocusedScreen", String.class);
	public static final Tag<String> seTerminalTab = from("seTerminalTab", String.class);
	public static final Tag<TerminalInventoryData> seDataInventory = from("seDataInventory", TerminalInventoryData.class);
	public static final Tag<InventorySide> seLeftInventory = from("seLeftInventory", InventorySide.class);
	public static final Tag<InventorySide> seRightInventory = from("seRightInventory", InventorySide.class);
	public static final Tag<TerminalControlPanelData> seDataControlPanel = from("seDataControlPanel", TerminalControlPanelData.class);
	public static final Tag<TerminalProductionData> seDataProduction = from("seDataProduction", TerminalProductionData.class);

	/**
	 * Agent - TESTAR comparison
	 */

	// Property used to know if an Action was selected by the Agent
	public static final Tag<Boolean> agentAction = from("agentAction", Boolean.class);

	// Property used to know if an Action discern between TESTAR and the Agent
	public static final Tag<Boolean> newActionByAgent = from("newActionByAgent", Boolean.class);

	/**
	 * Agent - SocioEmotional values
	 */

	public static final Tag<Double> agentPleasure = from("agentPleasure", Double.class);
	public static final Tag<Double> agentDominance = from("agentDominance", Double.class);
	public static final Tag<Double> agentArousal = from("agentArousal", Double.class);
	
	/**
	 * iv4xr Action Tags to customize action abstractions
	 */

	public static final Tag<String> iv4xrActionOriginWidgetId = Tag.from("iv4xrActionOriginWidgetId", String.class);
	public static final Tag<String> iv4xrActionOriginWidgetPath = Tag.from("iv4xrActionOriginWidgetPath", String.class);
	public static final Tag<String> iv4xrActionOriginStateId = Tag.from("iv4xrActionOriginStateId", String.class);
	public static final Tag<String> iv4xrActionEntityId = Tag.from("iv4xrActionEntityId", String.class);
	public static final Tag<Boolean> iv4xrActionEntityIsActive = Tag.from("iv4xrActionEntityIsActive", Boolean.class);
	public static final Tag<Vec3> iv4xrActionOriginPos = Tag.from("iv4xrActionOriginPos", Vec3.class);
	public static final Tag<Vec3> iv4xrActionTargetAbsPos = Tag.from("iv4xrActionTargetAbsPos", Vec3.class);
	public static final Tag<Vec3> iv4xrActionTargetRelPos = Tag.from("iv4xrActionTargetRelPos", Vec3.class);
}

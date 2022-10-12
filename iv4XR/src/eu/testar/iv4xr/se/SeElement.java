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

package eu.testar.iv4xr.se;

import eu.testar.iv4xr.IV4XRElement;
import spaceEngineers.controller.InventorySide;
import spaceEngineers.model.TerminalControlPanelData;
import spaceEngineers.model.TerminalInventoryData;
import spaceEngineers.model.TerminalProductionData;

public class SeElement extends IV4XRElement {

	private static final long serialVersionUID = 4915438615715637114L;

	/**
	 * Specific Space Engineers iv4xr properties
	 */

	// Agent properties
	public spaceEngineers.model.Vec3F seAgentPosition = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seAgentOrientationForward = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seAgentOrientationUp = new spaceEngineers.model.Vec3F(0, 0, 0);
	public float seAgentHealth = 0f;
	public float seAgentOxygen = 0f;
	public float seAgentEnergy = 0f;
	public float seAgentHydrogen = 0f;
	public boolean seAgentJetpackRunning = false;
	public boolean seAgentDampenersOn = false;
	public boolean unknownScreen = false;

	// Grid properties
	public String seGridName = "";
	public String seGridDisplayName = "";
	public float seGridMass = 0f;
	public boolean seGridParked = false;

	// Block properties
	public float seBuildIntegrity = 0f;
	public float seIntegrity = 0f;
	public float seMaxIntegrity = 0f;
	public spaceEngineers.model.Vec3F seMaxPosition = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seMinPosition = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seOrientationForward = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seOrientationUp = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seSize = new spaceEngineers.model.Vec3F(0, 0, 0);
	public String seDefinitionId = "";
	public boolean seFunctional = false;
	public boolean seWorking = false;
	public String seOwnerId = "";
	public String seBuiltBy = "";

	// https://github.com/iv4xr-project/iv4xr-se-plugin/blob/main/JvmClient/src/commonMain/kotlin/spaceEngineers/model/BlockDataClasses.kt
	// Specific properties of functional, terminal, door and power blocks
	public String seCustomName = "";
	public boolean seShowInInventory = false;
	public boolean seShowInTerminal = false;
	public boolean seShowOnHUD = false;

	public boolean seFunctionalEnabled = false;

	public boolean seDoorOpen = false;
	public boolean seDoorAnyoneCanUse = false;

	public float seFuelMaxOutput = 0f;
	public float seFuelCurrentOutput = 0f;
	public float seFuelCapacity = 0f;

	// SE Terminal properties
	public String terminalTab = "";
	public TerminalInventoryData dataInventory = null;
	public InventorySide leftInventory = null;
	public InventorySide rightInventory = null;
	public TerminalControlPanelData dataControlPanel = null;
	public TerminalProductionData dataProduction = null;

	public SeElement() {
		super();
	}

	public SeElement(IV4XRElement parent){
		super(parent);
	}

}

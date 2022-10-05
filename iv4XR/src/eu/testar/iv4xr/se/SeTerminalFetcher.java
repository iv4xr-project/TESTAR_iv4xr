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

import java.util.ArrayList;

import org.fruit.alayer.SUT;
import org.fruit.alayer.exceptions.StateBuildException;

import eu.testar.iv4xr.IV4XRElement;
import spaceEngineers.controller.CommsTab;
import spaceEngineers.controller.ControlPanelTab;
import spaceEngineers.controller.FactionsTab;
import spaceEngineers.controller.GpsTab;
import spaceEngineers.controller.InfoTab;
import spaceEngineers.controller.InventoryTab;
import spaceEngineers.controller.ProductionTab;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.controller.Terminal;

public class SeTerminalFetcher extends SeStateFetcher {

	public SeTerminalFetcher(SUT system) {
		super(system);
	}

	public SERootElement fetchTerminalScreen(SERootElement seRootElement, SpaceEngineers seController) {
		// Obtain the terminal screen and check which tab is currently opened
		Terminal terminal = seController.getScreens().getTerminal();

		// Add one children widget to the terminal root State + agent
		seRootElement.children = new ArrayList<IV4XRElement>(1 + 1);
		seRootElement.zindex = 0;
		fillRect(seRootElement);

		// Create the Agent as element of the tree, because always exists as a Widget
		SEagent(seRootElement, seController.getObserver().observe());

		if(terminal.data().getSelectedTab().contains("Inventory")) {
			inventoryDescend(seRootElement, terminal.getInventory());
		} else if(terminal.data().getSelectedTab().contains("Control")) {
			controlPanelDescend(seRootElement, terminal.getControlPanel());
		} else if(terminal.data().getSelectedTab().contains("Production")) {
			productionDescend(seRootElement, terminal.getProduction());
		} else if(terminal.data().getSelectedTab().contains("Info")) {
			infoDescend(seRootElement, terminal.getInfo());
		} else if(terminal.data().getSelectedTab().contains("Factions")) {
			factionsDescend(seRootElement, terminal.getFactions());
		} else if(terminal.data().getSelectedTab().contains("Comms")) {
			commsDescend(seRootElement, terminal.getComms());
		} else if(terminal.data().getSelectedTab().contains("Gps")) {
			gpsDescend(seRootElement, terminal.getGps());
		} else {
			throw new StateBuildException("Exception trying to obtain the terminal tab of : " + terminal.data().getSelectedTab());
		}

		return seRootElement;
	}

	private SeElement inventoryDescend(IV4XRElement parent, InventoryTab inventoryTab) {
		// Create the child element that represents the inventory widget
		SeElement childElement = new SeElement(parent);
		parent.children.add(childElement);

		childElement.terminalTab = "InventoryTab";
		childElement.dataInventory = inventoryTab.data();
		childElement.leftInventory = inventoryTab.getLeft();
		childElement.rightInventory = inventoryTab.getRight();

		fillRect(childElement);

		return childElement;
	}

	private SeElement controlPanelDescend(IV4XRElement parent, ControlPanelTab controlPanelTab) {
		// Create the child element that represents the inventory widget
		SeElement childElement = new SeElement(parent);
		parent.children.add(childElement);

		childElement.terminalTab = "ControlPanelTab";
		//FIXME: A bug exists in obtaining the control panel data if no GUI element is selected
		//childElement.dataControlPanel = controlPanelTab.data();

		fillRect(childElement);

		return childElement;
	}

	private SeElement productionDescend(IV4XRElement parent, ProductionTab productionTab) {
		// Create the child element that represents the inventory widget
		SeElement childElement = new SeElement(parent);
		parent.children.add(childElement);

		childElement.terminalTab = "ProductionTab";
		childElement.dataProduction = productionTab.data();

		fillRect(childElement);

		return childElement;
	}

	private SeElement infoDescend(IV4XRElement parent, InfoTab infoTab) {
		// Create the child element that represents the info widget
		SeElement childElement = new SeElement(parent);
		parent.children.add(childElement);

		childElement.terminalTab = "InfoTab";

		fillRect(childElement);

		return childElement;
	}

	private SeElement factionsDescend(IV4XRElement parent, FactionsTab factionsTab) {
		// Create the child element that represents the factions widget
		SeElement childElement = new SeElement(parent);
		parent.children.add(childElement);

		childElement.terminalTab = "FactionsTab";

		fillRect(childElement);

		return childElement;
	}

	private SeElement commsDescend(IV4XRElement parent, CommsTab commsTab) {
		// Create the child element that represents the comms widget
		SeElement childElement = new SeElement(parent);
		parent.children.add(childElement);

		childElement.terminalTab = "CommsTab";

		fillRect(childElement);

		return childElement;
	}

	private SeElement gpsDescend(IV4XRElement parent, GpsTab gpsTab) {
		// Create the child element that represents the gps widget
		SeElement childElement = new SeElement(parent);
		parent.children.add(childElement);

		childElement.terminalTab = "GpsTab";

		fillRect(childElement);

		return childElement;
	}

}

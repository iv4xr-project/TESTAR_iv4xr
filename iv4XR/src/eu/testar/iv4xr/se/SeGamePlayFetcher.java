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

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.IV4XRElement;
import spaceEngineers.controller.Observer;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.model.Block;
import spaceEngineers.model.CharacterObservation;
import spaceEngineers.model.CubeGrid;
import spaceEngineers.model.DoorBase;
import spaceEngineers.model.FueledPowerProducer;
import spaceEngineers.model.FunctionalBlock;
import spaceEngineers.model.Observation;
import spaceEngineers.model.TerminalBlock;

public class SeGamePlayFetcher extends SeStateFetcher {

	public SeGamePlayFetcher(SUT system) {
		super(system);
	}

	public SERootElement fetchGamePlayScreen(SERootElement seRootElement, SpaceEngineers seController) {
		// Check that TESTAR it can observe the SE system
		Observer seObserver = seController.getObserver();
		if(seObserver == null) throw new StateBuildException("SE Agent Oberver is null! Exception trying to fetch the State of iv4XR SpaceEngineers");

		// Get the Character and Blocks observation that we use to create the element tree
		CharacterObservation seObsCharacter = seController.getObserver().observe();
		Observation seObsBlocks = seController.getObserver().observeBlocks();

		// If the agent observes himself and in this instant of time also has observation of blocks
		if(seObsCharacter != null && seObsBlocks != null && seObsBlocks.getGrids() != null && seObsBlocks.getGrids().size() > 0) {
			// Add manually the Agent as an Element (Observed Blocks + 1)
			seRootElement.children = new ArrayList<IV4XRElement>((int) seObserver.observeBlocks().getGrids().size() + 1);

			seRootElement.zindex = 0;
			fillRect(seRootElement);

			// Create the Agent as element of the tree, because always exists as a Widget
			SEagent(seRootElement, seObsCharacter);

			// If the Agent observes blocks create the elements blocks tree
			if(seObsBlocks.getGrids().size() > 0) {
				for(CubeGrid seCubeGrid : seObsBlocks.getGrids()) {
					SEGridDescend(seRootElement, seCubeGrid);
				}
			}
		} 
		// If agent observes himself but in this instant has NO observation of blocks 
		else if (seObsCharacter != null) {
			// Add manually the Agent as an Element (Observed Entities + 1)
			seRootElement.children = new ArrayList<IV4XRElement>(1);

			seRootElement.zindex = 0;
			fillRect(seRootElement);

			SEagent(seRootElement, seObsCharacter);
		} else {
			System.err.println("ERROR: No Agent and no BLOCKS in the current Observation");
		}

		return seRootElement;
	}

	/**
	 * Based on the Space Engineers blocks Observation, extract the CubeGrid properties to 
	 * create an grid element inside the fetched tree. 
	 * Then extract the Block that are children of these CubeGrid. 
	 * 
	 * @param parent
	 * @param seCubeGrid
	 * @return CubeGrid element with Block children
	 */
	private SeElement SEGridDescend(IV4XRElement parent, CubeGrid seCubeGrid) {
		SeElement childElement = new SeElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;

		childElement.entityPosition = new Vec3(seCubeGrid.getPosition().getX(), seCubeGrid.getPosition().getY(), seCubeGrid.getPosition().getZ());
		childElement.entityId = seCubeGrid.getId();
		childElement.entityType = seCubeGrid.getId().replaceAll("[0-9]","").replaceAll("\\s+","");

		fillRect(childElement);

		for(Block seBlock : seCubeGrid.getBlocks()) {
			SEBlockDescend(childElement, seBlock);
		}

		return childElement;
	}

	/**
	 * Extract the Block properties to create an block element inside the fetched tree. 
	 * 
	 * @param parent
	 * @param seBlock
	 * @return Block element
	 */
	private SeElement SEBlockDescend(IV4XRElement parent, Block seBlock) {
		SeElement childElement = new SeElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;

		childElement.entityPosition = new Vec3(seBlock.getPosition().getX(), seBlock.getPosition().getY(), seBlock.getPosition().getZ());
		childElement.entityId = seBlock.getId();
		childElement.entityType = seBlock.getDefinitionId().getType();

		childElement.seBuildIntegrity = seBlock.getBuildIntegrity();
		childElement.seIntegrity = seBlock.getIntegrity();
		childElement.seMaxIntegrity = seBlock.getMaxIntegrity();
		childElement.seMaxPosition = seBlock.getMaxPosition();
		childElement.seMinPosition = seBlock.getMinPosition();
		childElement.seOrientationForward = seBlock.getOrientationForward();
		childElement.seOrientationUp = seBlock.getOrientationUp();
		childElement.seSize = seBlock.getSize();
		childElement.seDefinitionId = seBlock.getDefinitionId().toString();
		childElement.seFunctional = seBlock.getFunctional();
		childElement.seWorking = seBlock.getWorking();
		childElement.seOwnerId = seBlock.getOwnerId();
		childElement.seBuiltBy = seBlock.getBuiltBy();

		// TODO: Think a better way to maintain these different types of blocks / entities
		// https://github.com/iv4xr-project/iv4xr-se-plugin/blob/main/JvmClient/src/commonMain/kotlin/spaceEngineers/model/BlockDataClasses.kt
		// Specific properties of functional, terminal, door and power blocks
		if(seBlock instanceof TerminalBlock) {
			childElement.seCustomName = ((TerminalBlock)seBlock).getCustomName();
			childElement.seShowInInventory = ((TerminalBlock)seBlock).getShowInInventory();
			childElement.seShowInTerminal = ((TerminalBlock)seBlock).getShowInTerminal();
			childElement.seShowOnHUD = ((TerminalBlock)seBlock).getShowOnHUD();
		}
		else if(seBlock instanceof FunctionalBlock) {
			childElement.seCustomName = ((FunctionalBlock)seBlock).getCustomName();
			childElement.seShowInInventory = ((FunctionalBlock)seBlock).getShowInInventory();
			childElement.seShowInTerminal = ((FunctionalBlock)seBlock).getShowInTerminal();
			childElement.seShowOnHUD = ((FunctionalBlock)seBlock).getShowOnHUD();
			childElement.seFunctionalEnabled = ((FunctionalBlock)seBlock).getEnabled();
		}
		else if(seBlock instanceof DoorBase) {
			childElement.seCustomName = ((DoorBase)seBlock).getCustomName();
			childElement.seShowInInventory = ((DoorBase)seBlock).getShowInInventory();
			childElement.seShowInTerminal = ((DoorBase)seBlock).getShowInTerminal();
			childElement.seShowOnHUD = ((DoorBase)seBlock).getShowOnHUD();
			childElement.seFunctionalEnabled = ((DoorBase)seBlock).getEnabled();
			childElement.seDoorOpen = ((DoorBase)seBlock).getOpen();
			childElement.seDoorAnyoneCanUse = ((DoorBase)seBlock).getAnyoneCanUse();
		}
		else if(seBlock instanceof FueledPowerProducer) {
			childElement.seCustomName = ((FueledPowerProducer)seBlock).getCustomName();
			childElement.seShowInInventory = ((FueledPowerProducer)seBlock).getShowInInventory();
			childElement.seShowInTerminal = ((FueledPowerProducer)seBlock).getShowInTerminal();
			childElement.seShowOnHUD = ((FueledPowerProducer)seBlock).getShowOnHUD();
			childElement.seFunctionalEnabled = ((FueledPowerProducer)seBlock).getEnabled();
			childElement.seFuelMaxOutput = ((FueledPowerProducer)seBlock).getMaxOutput();
			childElement.seFuelCurrentOutput = ((FueledPowerProducer)seBlock).getCurrentOutput();
			childElement.seFuelCapacity = ((FueledPowerProducer)seBlock).getCapacity();
		}

		fillRect(childElement);

		return childElement;
	}
}

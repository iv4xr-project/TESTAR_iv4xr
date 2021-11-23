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

package eu.testar.iv4xr.se;

import java.util.ArrayList;

import org.fruit.alayer.SUT;
import org.fruit.alayer.exceptions.StateBuildException;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.IV4XRElement;
import eu.testar.iv4xr.IV4XRRootElement;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.enums.IV4XRtags;
import spaceEngineers.controller.JsonRpcSpaceEngineers;
import spaceEngineers.controller.Observer;
import spaceEngineers.model.CubeGrid;
import spaceEngineers.model.Observation;
import spaceEngineers.model.Block;
import spaceEngineers.model.CharacterObservation;

public class SeStateFetcher extends IV4XRStateFetcher {

	public SeStateFetcher(SUT system) {
		super(system);
	}

	/**
	 * Create an Array tree of elements that later becomes the Widget-tree.
	 * Use the Space Engineers Rpc Controller to extract the Agent and Blocks information from the WOM. 
	 * Every instant of time the Agent will observe himself and the Blocks if these are in close range. 
	 */
	@Override
	protected IV4XRRootElement fetchIV4XRElements(IV4XRRootElement rootElement) {
		// Get the controller attached to the SE system (SpaceEngineersProcess)
		JsonRpcSpaceEngineers seRpcController = system.get(IV4XRtags.iv4xrSpaceEngRpcController);

		// Check that TESTAR it can observe the SE system
		Observer seObserver = seRpcController.getObserver();
		if(seObserver == null) throw new StateBuildException("SE Agent Oberver is null! Exception trying to fetch the State of iv4XR SpaceEngineers");

		// Get the Character and Blocks observation that we use to create the element tree
		CharacterObservation seObsCharacter = seRpcController.getObserver().observe();
		Observation seObsBlocks = seRpcController.getObserver().observeBlocks();

		// If the agent observes himself and in this instant of time also has observation of blocks
		if(seObsCharacter != null && seObsBlocks != null && seObsBlocks.getGrids() != null && seObsBlocks.getGrids().size() > 0) {
			// Add manually the Agent as an Element (Observed Blocks + 1)
			rootElement.children = new ArrayList<IV4XRElement>((int) seObserver.observeBlocks().getGrids().size() + 1);

			rootElement.zindex = 0;
			fillRect(rootElement);

			// Create the Agent as element of the tree, because always exists as a Widget
			SEagent(rootElement, seObsCharacter);

			// If the Agent observes blocks create the elements blocks tree
			if(seObsBlocks.getGrids().size() > 0) {
				for(CubeGrid seCubeGrid : seObsBlocks.getGrids()) {
					SEGridDescend(rootElement, seCubeGrid);
				}
			}
		} 
		// If agent observes himself but in this instant has NO observation of blocks 
		else if (seObsCharacter != null) {
			// Add manually the Agent as an Element (Observed Entities + 1)
			rootElement.children = new ArrayList<IV4XRElement>(1);

			rootElement.zindex = 0;
			fillRect(rootElement);

			SEagent(rootElement, seObsCharacter);
		} else {
			System.err.println("ERROR: No Agent and no BLOCKS in the current Observation");
		}

		return rootElement;
	}

	/**
	 * Based on the Space Engineers CharacterObservation, extract the agent properties to 
	 * create an element inside the fetched tree. 
	 * 
	 * @param parent
	 * @param seObsCharacter
	 * @return agent Element
	 */
	private IV4XRElement SEagent(IV4XRElement parent, CharacterObservation seObsCharacter) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;

		childElement.agentPosition = new Vec3(seObsCharacter.getPosition().getX(), seObsCharacter.getPosition().getY(), seObsCharacter.getPosition().getZ());
		childElement.seAgentPosition = seObsCharacter.getPosition();
		childElement.seAgentOrientationForward = seObsCharacter.getOrientationForward();
		childElement.seAgentOrientationUp = seObsCharacter.getOrientationUp();
		childElement.seAgentHealth = seObsCharacter.getHealth();

		childElement.entityVelocity = new Vec3(seObsCharacter.getVelocity().getX(), seObsCharacter.getVelocity().getY(), seObsCharacter.getVelocity().getZ());
		childElement.entityId = system.get(IV4XRtags.iv4xrSpaceEngRpcController).getAgentId();
		childElement.entityType = "AGENT"; //TODO: check proper entity for agent
		childElement.entityTimestamp = -1;

		fillRect(childElement);

		return childElement;
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
	private IV4XRElement SEGridDescend(IV4XRElement parent, CubeGrid seCubeGrid) {
		IV4XRElement childElement = new IV4XRElement(parent);
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
	private IV4XRElement SEBlockDescend(IV4XRElement parent, Block seBlock) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;

		childElement.entityPosition = new Vec3(seBlock.getPosition().getX(), seBlock.getPosition().getY(), seBlock.getPosition().getZ());
		childElement.entityId = seBlock.getId();
		childElement.entityType = seBlock.getDefinitionId().toString();

		childElement.seBuildIntegrity = seBlock.getBuildIntegrity();
		childElement.seIntegrity = seBlock.getIntegrity();
		childElement.seMaxIntegrity = seBlock.getMaxIntegrity();
		childElement.seMaxPosition = seBlock.getMaxPosition();
		childElement.seMinPosition = seBlock.getMinPosition();
		childElement.seOrientationForward = seBlock.getOrientationForward();
		childElement.seOrientationUp = seBlock.getOrientationUp();
		childElement.seSize = seBlock.getSize();

		fillRect(childElement);

		return childElement;
	}
}

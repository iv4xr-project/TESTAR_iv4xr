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

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.IV4XRElement;
import eu.testar.iv4xr.IV4XRRootElement;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.enums.IV4XRtags;
import spaceEngineers.commands.ObservationArgs;
import spaceEngineers.commands.ObservationMode;
import spaceEngineers.controller.ProprietaryJsonTcpCharacterController;
import spaceEngineers.model.CubeGrid;
import spaceEngineers.model.Observation;
import spaceEngineers.model.Block;

public class SeStateFetcher extends IV4XRStateFetcher {

	public SeStateFetcher(SUT system) {
		super(system);
	}

	/**
	 * Observation = Objects Agent can observe - Dynamically appearing and disappearing WOM. 
	 */
	@Override
	protected IV4XRRootElement fetchIV4XRElements(IV4XRRootElement rootElement) {

		ProprietaryJsonTcpCharacterController seController = system.get(IV4XRtags.iv4xrSpaceEngProprietaryTcpController);

		Observation observation = seController.observe(new ObservationArgs(ObservationMode.BLOCKS));

		if(observation != null && observation.getGrids() != null && observation.getGrids().size() > 0) {
			// Add manually the Agent as an Element (Observed Entities + 1)
			rootElement.children = new ArrayList<IV4XRElement>((int) observation.getGrids().size() + 1);

			rootElement.zindex = 0;
			fillRect(rootElement);

			// Agent always exists as an Entity
			SEagent(rootElement, observation);

			// If Agent observes entities create the elements-entities
			if(observation.getGrids().size() > 0) {
				for(CubeGrid seCubeGrid : observation.getGrids()) {
					SEGridDescend(rootElement, seCubeGrid);
				}
			}
		} else if (observation != null) {
			// Add manually the Agent as an Element (Observed Entities + 1)
			rootElement.children = new ArrayList<IV4XRElement>(1);

			rootElement.zindex = 0;
			fillRect(rootElement);

			SEagent(rootElement, observation);
		} else {
			System.err.println("ERROR: No Agent and no BLOCKS in the current Observation");
		}

		return rootElement;
	}

	private IV4XRElement SEagent(IV4XRElement parent, Observation seObservation) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;

		childElement.agentPosition = new Vec3(seObservation.getPosition().getX(), seObservation.getPosition().getY(), seObservation.getPosition().getZ());
		childElement.seAgentPosition = seObservation.getPosition();
		childElement.seAgentOrientationForward = seObservation.getOrientationForward();
		childElement.seAgentOrientationUp = seObservation.getOrientationUp();
		
		childElement.entityVelocity = new Vec3(seObservation.getVelocity().getX(), seObservation.getVelocity().getY(), seObservation.getVelocity().getZ());
		childElement.entityId = system.get(IV4XRtags.iv4xrSpaceEngProprietaryTcpController).getAgentId();
		childElement.entityType = "AGENT"; //TODO: check proper entity for agent
		childElement.entityTimestamp = -1;

		fillRect(childElement);

		return childElement;
	}

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

	private IV4XRElement SEBlockDescend(IV4XRElement parent, Block seBlock) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;

		childElement.entityPosition = new Vec3(seBlock.getPosition().getX(), seBlock.getPosition().getY(), seBlock.getPosition().getZ());
		childElement.entityId = seBlock.getId();
		childElement.entityType = seBlock.getBlockType();
		
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

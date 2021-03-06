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

import eu.testar.iv4xr.IV4XRElement;
import eu.testar.iv4xr.IV4XRRootElement;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.enums.IV4XRtags;
import spaceEngineers.SeEntity;
import spaceEngineers.SeObservation;
import spaceEngineers.SeRequest;
import spaceEngineers.SpaceEngEnvironment;
import spaceEngineers.commands.ObservationArgs;
import spaceEngineers.commands.ObservationMode;
import spaceEngineers.commands.SeAgentCommand;

public class SeStateFetcher extends IV4XRStateFetcher {

	public SeStateFetcher(SUT system) {
		super(system);
	}
	
	/**
	 * Legacy Observation = Objects Agent can observe - Dynamically appearing and disappearing WOM. 
	 * WorldModel = All Objects from WOM - Static Object with dynamic properties. 
	 */
	@Override
	protected IV4XRRootElement fetchIV4XRElements(IV4XRRootElement rootElement) {
		
		SpaceEngEnvironment seEnv = system.get(IV4XRtags.iv4xrSpaceEngEnvironment);
		
		for(String agentId : agentsIds) {
			// LegacyObservation
			// TODO: SpaceEngineers plugin should extend from W3DWorldModel
			SeObservation agentObservation = seEnv.getSeResponse(SeRequest.command(SeAgentCommand.observe(agentId)));
			SeObservation entitesObservation = seEnv.getSeResponse(SeRequest.command(SeAgentCommand.observe(agentId, new ObservationArgs(ObservationMode.ENTITIES))));
			
			//TODO: After update dll use block entities
			//SeObservation blocksObservation = seEnv.getSeResponse(SeRequest.command(SeAgentCommand.observe(agentId, new ObservationArgs(ObservationMode.BLOCKS))));
			//SeObservation newBlocksObservation = seEnv.getSeResponse(SeRequest.command(SeAgentCommand.observe(agentId, new ObservationArgs(ObservationMode.NEW_BLOCKS))));

			if(agentObservation != null && entitesObservation.entities != null) {
				// Add manually the Agent as an Element (Observed Entities + 1)
				rootElement.children = new ArrayList<IV4XRElement>((int) entitesObservation.entities.size() + 1);

				rootElement.zindex = 0;
				fillRect(rootElement);

				// Agent always exists as an Entity
				SEagent(rootElement, agentObservation);

				// If Agent observes entities create the elements-entities
				if(entitesObservation.entities.size() > 0) {
					for(SeEntity entity : entitesObservation.entities) {
						SEdescend(rootElement, entity);
					}
				}
			} else if (agentObservation != null) {
				System.out.println("INFO: No entities in the current Observation");
				// Add manually the Agent as an Element (Observed Entities + 1)
				rootElement.children = new ArrayList<IV4XRElement>(1);

				rootElement.zindex = 0;
				fillRect(rootElement);

				SEagent(rootElement, agentObservation);
			} else {
				System.err.println("ERROR: No Agent and no Entities in the current Observation");
			}
		}

		return rootElement;
	}
	
	private IV4XRElement SEagent(IV4XRElement parent, SeObservation seObservation) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;

		childElement.agentPosition = seObservation.position;
		childElement.entityVelocity = seObservation.velocity;
		childElement.entityId = seObservation.agentID;
		childElement.entityType = "AGENT"; //TODO: check proper entity for agent
		childElement.entityTimestamp = -1;

		fillRect(childElement);

		return childElement;
	}
	
	private IV4XRElement SEdescend(IV4XRElement parent, SeEntity seEntity) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;

		childElement.entityPosition = seEntity.position;
		childElement.entityId = seEntity.id;

		fillRect(childElement);

		return childElement;
	}
}

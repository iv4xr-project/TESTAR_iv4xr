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

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.testar.iv4xr.IV4XRElement;
import eu.testar.iv4xr.IV4XRRootElement;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.enums.IV4XRtags;

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
		for(String agentId : agentsIds) {
			// LegacyObservation
			// TODO: SpaceEngineers plugin should extend from W3DWorldModel
			//WorldModel observedLabWOM = system.get(IV4XRtags.iv4xrSpaceEngEnvironment).observe(agentId);
			WorldModel observedWOM = (WorldModel) system.get(IV4XRtags.iv4xrSpaceEngEnvironment).sendCommand(agentId, null, "Observe", null, WorldModel.class);

			if(rootElement.isForeground) {
				// Add manually the Agent as an Element (Observed Entities + 1)
				rootElement.children = new ArrayList<IV4XRElement>((int) observedWOM.elements.size() + 1);

				rootElement.zindex = 0;
				fillRect(rootElement);

				IV4XRagent(rootElement, observedWOM);

				// If Agent observes entities create the elements-entities
				if(observedWOM.elements.size() > 0) {
					for(WorldEntity entity : observedWOM.elements.values()) {
						IV4XRdescend(rootElement, observedWOM.getElement(entity.id));
					}
				}
			}
		}

		return rootElement;
	}

	@Override
	protected IV4XRElement IV4XRagent(IV4XRElement parent, WorldModel labWOM) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;

		childElement.entityPosition = labWOM.position;
		childElement.entityBounds = labWOM.extent; //TODO: Do the Agents have bounds ?
		childElement.entityVelocity = labWOM.velocity;
		childElement.entityId = labWOM.agentId;
		childElement.entityType = "AGENT"; //TODO: check proper entity for agent
		childElement.entityTimestamp = -1;

		fillRect(childElement);

		return childElement;
	}

	@Override
	protected IV4XRElement IV4XRdescend(IV4XRElement parent, WorldEntity labEntity) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;

		childElement.entityPosition = labEntity.position;
		childElement.entityBounds = labEntity.extent;
		childElement.entityVelocity = labEntity.velocity;
		childElement.entityDynamic = labEntity.dynamic;
		childElement.entityId = labEntity.id;
		childElement.entityType = labEntity.type;
		childElement.entityTimestamp = labEntity.timestamp;

		fillRect(childElement);

		return childElement;
	}

}

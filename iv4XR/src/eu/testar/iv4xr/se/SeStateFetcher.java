/***************************************************************************************************
 *
 * Copyright (c) 2020 - 2023 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 - 2023 Open Universiteit - www.ou.nl
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
import org.fruit.alayer.Tags;
import org.fruit.alayer.windows.Windows;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.IV4XRElement;
import eu.testar.iv4xr.IV4XRRootElement;
import eu.testar.iv4xr.IV4XRState;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.IV4XRWidgetEntity;
import eu.testar.iv4xr.enums.IV4XRtags;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.model.CharacterObservation;

public class SeStateFetcher extends IV4XRStateFetcher {

	public SeStateFetcher(SUT system) {
		super(system);
	}

	/**
	 * Create the root element that represents the SE state
	 */
	@Override
	protected IV4XRRootElement buildVirtualEnvironment(SUT system) {
		SERootElement rootElement = new SERootElement();
		rootElement.isRunning = system.isRunning();
		rootElement.timeStamp = System.currentTimeMillis();
		rootElement.pid = system.get(Tags.PID);

		if(!rootElement.isRunning) {
			return rootElement;
		}

		rootElement.pid = system.get(Tags.PID, (long)-1);

		for(long windowHandle : getVisibleTopLevelWindowHandles()) {
			if(rootElement.pid == Windows.GetWindowProcessId(windowHandle)) {
				rootElement.windowsHandle = windowHandle;
				system.set(Tags.HWND, windowHandle);
				rootElement.set(Tags.HWND, windowHandle);
			}
		}

		return fetchIV4XRElements(rootElement);
	}

	/**
	 * Create an Array tree of elements that later becomes the Widget-tree.
	 * Use the Space Engineers Rpc Controller to extract the Agent and Blocks information from the WOM. 
	 * Every instant of time the Agent will observe himself and the Blocks if these are in close range. 
	 */
	@Override
	protected IV4XRRootElement fetchIV4XRElements(IV4XRRootElement rootElement) {
		return fetchScreenSE((SERootElement) rootElement);
	}

	private SERootElement fetchScreenSE(SERootElement seRootElement) {
		// Get the controller attached to the SE system (SpaceEngineersProcess)
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);

		// The root element of SE will indicate which screen is focused
		seRootElement.focusedScreen = seController.getScreens().getFocusedScreen().data().getName();

		// GamePlay screen represents the WOM / State that allows agents to interact with grids and blocks
		if(seRootElement.focusedScreen.contains("GamePlay")) {
			return new SeGamePlayFetcher(system).fetchGamePlayScreen(seRootElement, seController);
		} 
		// Terminal screen represents the WOM / State that allows agents to interact with management panels
		else if(seRootElement.focusedScreen.contains("Terminal")) {
			return new SeTerminalFetcher(system).fetchTerminalScreen(seRootElement, seController);
		}
		// The default screen only contains the agent information
		else {
			System.err.println("ERROR: Current SE Screen is not valid / not implemented");

			// Add manually the Agent as an Element (Observed Entities + 1)
			seRootElement.children = new ArrayList<IV4XRElement>(1);
			seRootElement.zindex = 0;
			fillRect(seRootElement);
			SeElement seAgentEl = SEagent(seRootElement, seController.getObserver().observe());
			seAgentEl.unknownScreen = true;
		}

		return seRootElement;
	}

	/**
	 * Based on the Space Engineers CharacterObservation, extract the agent properties to 
	 * create an element inside the fetched tree. 
	 * 
	 * @param parent
	 * @param seObsCharacter
	 * @return agent Element
	 */
	protected SeElement SEagent(IV4XRElement parent, CharacterObservation seObsCharacter) {
		SeElement childElement = new SeElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;

		childElement.agentPosition = new Vec3(seObsCharacter.getPosition().getX(), seObsCharacter.getPosition().getY(), seObsCharacter.getPosition().getZ());
		childElement.seAgentPosition = seObsCharacter.getPosition();
		childElement.seAgentOrientationForward = seObsCharacter.getOrientationForward();
		childElement.seAgentOrientationUp = seObsCharacter.getOrientationUp();
		childElement.seAgentHealth = seObsCharacter.getHealth();
		childElement.seAgentOxygen = seObsCharacter.getOxygen();
		childElement.seAgentEnergy = seObsCharacter.getEnergy();
		childElement.seAgentHydrogen = seObsCharacter.getHydrogen();
		childElement.seAgentJetpackRunning = seObsCharacter.getJetpackRunning();
		childElement.seAgentDampenersOn = seObsCharacter.getDampenersOn();

		childElement.entityVelocity = new Vec3(seObsCharacter.getVelocity().getX(), seObsCharacter.getVelocity().getY(), seObsCharacter.getVelocity().getZ());
		childElement.entityId = system.get(IV4XRtags.iv4xrSpaceEngineers).getObserver().observe().getId();
		childElement.entityType = "AGENT"; //TODO: check proper entity for agent
		childElement.entityTimestamp = -1;

		fillRect(childElement);

		return childElement;
	}

	@Override
	protected IV4XRState createWidgetTree(IV4XRRootElement root) {
		SeState state = new SeState(root);
		root.backRef = state;
		for (IV4XRElement childElement : root.children) {
			createWidgetTree(state, childElement);
		}
		return state;
	}

	@Override
	protected void createWidgetTree(IV4XRWidgetEntity parent, IV4XRElement element) {
		IV4XRWidgetEntity w = parent.root().addChild(parent, element);
		element.backRef = w;

		for (IV4XRElement child : element.children) {
			createWidgetTree(w, child);
		}
	}

}

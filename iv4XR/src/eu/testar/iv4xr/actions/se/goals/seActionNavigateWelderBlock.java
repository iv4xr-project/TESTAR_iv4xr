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

package eu.testar.iv4xr.actions.se.goals;

import org.fruit.Util;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.enums.IV4XRtags;
import spaceEngineers.model.ToolbarLocation;

public class seActionNavigateWelderBlock extends seActionNavigateToBlock {
	private static final long serialVersionUID = -3957481065378868530L;

	// TODO: Research the impact of the welderType and toolUsage in the inference of the state model
	private String welderType;
	private double toolUsageTime;

	/**
	 * Types: WelderItem, Welder2Item, Welder3Item, Welder4Item
	 * 
	 * @param welderType
	 */
	private void setWelderType(int welderType) {
		String type = (welderType >= 2 && welderType <= 4) ? String.valueOf(welderType) : "" ; 
		this.welderType = "Welder".concat(type).concat("Item");
	}

	public seActionNavigateWelderBlock(Widget w, Vec3 reachablePosition, SUT system, String agentId){
		this(w, reachablePosition, system, agentId, 1, 1);
	}

	public seActionNavigateWelderBlock(Widget w, Vec3 reachablePosition, SUT system, String agentId, int welderType, double toolUsageTime){
		super(w, reachablePosition, system, agentId);
		setWelderType(welderType);
		this.toolUsageTime = toolUsageTime;
		this.set(Tags.Desc, toShortString());
		// TODO: Update with Goal Solving agents
		this.set(IV4XRtags.agentAction, false);
		this.set(IV4XRtags.newActionByAgent, false);
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		equipWelder(system);
		navigateToReachableBlockPosition(system);
		rotateToBlockDestination(system);
		useWelder(system);

		// After Welder action equip an empty object
		spaceEngineers.controller.Items seItems = system.get(IV4XRtags.iv4xrSpaceEngItems);
		seItems.equip(ToolbarLocation.Companion.fromIndex(1, 1));
	}

	/**
	 * Prepare the Welder tool in the SE tool bar. 
	 * 
	 * @param seItems
	 */
	private void equipWelder(SUT system) {
		spaceEngineers.controller.Items seItems = system.get(IV4XRtags.iv4xrSpaceEngItems);

		seItems.setToolbarItem(welderType, ToolbarLocation.Companion.fromIndex(4, 5));
		Util.pause(0.5);
		seItems.equip(ToolbarLocation.Companion.fromIndex(4, 5));
	}

	/**
	 * Use the Welder tool the desired amount of time. 
	 * 
	 * @param seItems
	 */
	private void useWelder(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);

		seCharacter.beginUsingTool();
		Util.pause(toolUsageTime);
		seCharacter.endUsingTool();
	}

	@Override
	public String toShortString() {
		return "Navigate to block: " + widgetType + ", id: " + widgetId + " and use " + welderType + " seconds " + toolUsageTime + " by agent: " + agentId;
	}
}
/***************************************************************************************************
 *
 * Copyright (c) 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2022 Open Universiteit - www.ou.nl
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
import org.fruit.alayer.Verdict;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.testar.iv4xr.enums.IV4XRtags;
import spaceEngineers.model.CharacterObservation;

public class seActionNavigateInteract extends seActionNavigateToBlock {
	private static final long serialVersionUID = 5024994091150159066L;

	protected Widget targetBlock;

	public seActionNavigateInteract(Widget w, SUT system, String agentId){
		super(w, system, agentId);
		this.targetBlock = w;
		this.set(Tags.Desc, toShortString());
		// TODO: Update with Goal Solving agents
		this.set(IV4XRtags.agentAction, false);
		this.set(IV4XRtags.newActionByAgent, false);
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		navigateToReachableBlockPosition(system, state);
		aimToBlock(system, targetBlock);

		// Check the jetpack settings before interacting with the functional block
		spaceEngineers.controller.SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
		CharacterObservation seObsCharacter = seController.getObserver().observe();
		Boolean isJetpackRunningBefore = seObsCharacter.getJetpackRunning();

		// Go inside the functional block
		interactWithBlock(system);

		// Wait some seconds
		Util.pause(1);

		// Then go outside
		interactWithBlock(system);

		// Finally, execute a new observation to check the jetpack settings
		seObsCharacter = seController.getObserver().observe();
		Boolean isJetpackRunningAfter = seObsCharacter.getJetpackRunning();

		if(!isJetpackRunningBefore.equals(isJetpackRunningAfter)) {
			actionVerdict = new Verdict(Verdict.AGENT_JETPACK_ERROR, "Jetpack settings are incorrect after interacting with block : " + targetBlock.get(IV4XRtags.entityType));
		}

		Util.pause(1);
	}

	private void interactWithBlock(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		seCharacter.use();
	}

	@Override
	public String toShortString() {
		return "Navigate and interact with block: " + widgetType + ", id: " + widgetId + " using agent: " + agentId;
	}
}

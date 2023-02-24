/***************************************************************************************************
 *
 * Copyright (c) 2022 - 2023 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2022 - 2023 Open Universiteit - www.ou.nl
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
import org.fruit.alayer.devices.AWTKeyboard;
import org.fruit.alayer.devices.KBKeys;
import org.fruit.alayer.devices.Keyboard;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.testar.iv4xr.enums.IV4XRtags;
import spaceEngineers.model.CharacterObservation;

public class seActionNavigateRechargeHealth extends seActionNavigateToBlock {
	private static final long serialVersionUID = 3493737977558831469L;

	protected Widget targetBlock;

	public seActionNavigateRechargeHealth(Widget w, SUT system, String agentId){
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
		// If TESTAR was able to reach and aim the health block, interact and validate
		if(aimToBlock(system, targetBlock)) {

			// Reduce the health of the agent intentionally to be able to verify the health charge
			boolean isHelmentEnabled = system.get(IV4XRtags.iv4xrSpaceEngineers).getObserver().observe().getHelmetEnabled();
			spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
			if(isHelmentEnabled) {
				seCharacter.setHelmet(false);
				Util.pause(3);
				seCharacter.setHelmet(true);
			}

			// Check the player health before interacting with the functional block
			spaceEngineers.controller.SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
			CharacterObservation seObsCharacter = seController.getObserver().observe();
			float previousHealth = seObsCharacter.getHealth();
			float previousEnergy = seObsCharacter.getEnergy();
			float previousOxygen = seObsCharacter.getOxygen();
			float previousHydrogen = seObsCharacter.getHydrogen();

			Keyboard kb = AWTKeyboard.build();
			kb.press(KBKeys.VK_F);
			Util.pause(5);
			kb.release(KBKeys.VK_F);
			Util.pause(1);

			// ALWAYS check health and energy when TESTAR interacts with a MedicalRoom

			// Execute a new observation to check if the health increased
			seObsCharacter = seController.getObserver().observe();
			float newHealth = seObsCharacter.getHealth();
			if(previousHealth != 1.0f && newHealth <= previousHealth) {
				actionVerdict = new Verdict(Verdict.AGENT_HEALTH_ERROR, 
						"Agent Health did not increase after interacting with block: " 
								+ targetBlock.get(IV4XRtags.entityType)
								+ ", Previous health: " + previousHealth
								+ ", After interaction health: " + newHealth);
				return;
			}
			// Second, check that the energy has increased
			float newSuitEnergy = seObsCharacter.getEnergy();
			if(previousEnergy != 1.0f && newSuitEnergy <= previousEnergy) {
				actionVerdict = new Verdict(Verdict.AGENT_ENERGY_ERROR, 
						"Agent Suit Energy did not increase after interacting with block: " 
								+ targetBlock.get(IV4XRtags.entityType)
								+ ", Previous energy: " + previousEnergy
								+ ", After interaction energy: " + newSuitEnergy);
				return;
			}

			// ONLY when the Medical Room is a construction that provides O2 and H2, check these properties
			if(targetBlock.get(IV4XRtags.seCustomName, "").contains("O2H2")) {
				// Third, check that the oxygen increased
				float newOxygen = seObsCharacter.getOxygen();
				if(previousOxygen != 1.0f && newOxygen <= previousOxygen) {
					actionVerdict = new Verdict(Verdict.AGENT_OXYGEN_ERROR, 
							"Agent Oxygen did not increase after interacting with block: " 
									+ targetBlock.get(IV4XRtags.entityType)
									+ ", Previous oxygen: " + previousOxygen
									+ ", After interaction oxygen: " + newOxygen);
					return;
				}
				// Fourth, check that the hydrogen has increased
				float newHydrogen = seObsCharacter.getHydrogen();
				if(previousHydrogen != 1.0f && newHydrogen <= previousHydrogen) {
					actionVerdict = new Verdict(Verdict.AGENT_HYDROGEN_ERROR, 
							"Agent Hydrogen did not increase after interacting with block: " 
									+ targetBlock.get(IV4XRtags.entityType)
									+ ", Previous hydrogen: " + previousHydrogen
									+ ", After interaction hydrogen: " + newHydrogen);
					return;
				}
			}
		}

		Util.pause(1);
	}

	@Override
	public String toShortString() {
		return "Navigate and recharge health with block: " + widgetType + ", id: " + widgetId + " using agent: " + agentId;
	}
}

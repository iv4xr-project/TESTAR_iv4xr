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

import java.time.Duration;
import java.time.Instant;

import org.fruit.Util;
import org.fruit.alayer.SUT;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.testar.iv4xr.enums.IV4XRtags;
import spaceEngineers.controller.Observer;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.model.CharacterMovementType;
import spaceEngineers.model.CharacterObservation;
import spaceEngineers.model.Vec3F;
import spaceEngineers.movement.CompositeDirection3d;
import spaceEngineers.movement.VectorMovement;

// https://github.com/iv4xr-project/iv4xr-se-plugin/blob/main/JvmClient/src/commonMain/kotlin/spaceEngineers/navigation/Navigation.kt
public class SEnavigator {

	private float lastDistance = Float.MAX_VALUE;

	public void moveInLine(SUT system, Vec3F nodePosition) {
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
		Observer seObserver = seController.getObserver();
		CharacterObservation agentObservation = seObserver.observe();

		Vec3F direction = nodePosition.minus(agentObservation.getPosition()).normalized();

		// TODO("Replace this teleport hack")
		seController.getAdmin().getCharacter().teleport(agentObservation.getPosition(), direction, agentObservation.getOrientationUp());

		goForwardToLocation(seController, seObserver, nodePosition, CharacterMovementType.RUN, 20, 1.2f);
		goForwardToLocation(seController, seObserver, nodePosition, CharacterMovementType.RUN, 6, 0.4f);
	}

	private void goForwardToLocation(SpaceEngineers seController, Observer seObserver, Vec3F nodePosition, CharacterMovementType movementType, int stepTicks, float tolerance) {
		Instant startMovementInstant = Instant.now();
		while (isNotYetThereButProgressing(seObserver, nodePosition, tolerance)) {
			// TODO("Correct the course from time to time")
			VectorMovement movement = new VectorMovement(seController, 9f);
			movement.move(CompositeDirection3d.FORWARD, movementType, stepTicks);
			Util.pause(0.5);
			// If there is a problem in the movement, stop the loop
			if(Duration.between(startMovementInstant, Instant.now()).toSeconds() > 60) {
				System.out.println("ERROR: Trying to goForwardToLocation: " + nodePosition);
				System.err.println("ERROR: Trying to goForwardToLocation: " + nodePosition);
				throw new ActionFailedException("ERROR: Trying to goForwardToLocation: " + nodePosition);
			}
		}
	}

	private boolean isNotYetThereButProgressing(Observer seObserver, Vec3F nodePosition, float tolerance) {
		float maxDistanceRegression = 0.01f;
		float distance = spaceEngineers.controller.extensions.ObserverExtensionsKt.distanceTo(seObserver, nodePosition);
		if (distance < tolerance) {
			return false;
		}
		if (distance > lastDistance + maxDistanceRegression) {  // Allow very small worsening of distance.
			return false;
		}
		lastDistance = distance;
		return true;
	}

}

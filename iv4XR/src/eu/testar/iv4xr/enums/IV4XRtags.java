/***************************************************************************************************
 *
 * Copyright (c) 2020 - 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 - 2021 Open Universiteit - www.ou.nl
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

package eu.testar.iv4xr.enums;

import java.util.HashSet;
import java.util.Set;

import org.fruit.alayer.Tag;
import org.fruit.alayer.TagsBase;
import org.fruit.alayer.Widget;
import org.fruit.alayer.windows.WinProcess;

import spaceEngineers.controller.ProprietaryJsonTcpCharacterController;
import eu.iv4xr.framework.mainConcepts.W3DEnvironment;
import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.labrecruits.listener.LabRecruitsEnvironmentListener;
import world.LabWorldModel;
import world.Observation;

public class IV4XRtags extends TagsBase {

	private IV4XRtags() {}

	/**
	 * iv4XR Environment Tags 
	 */

	// Generic W3D Environment
	public static final Tag<W3DEnvironment> iv4xrW3DEnvironment = from("iv4xrW3DEnvironment", W3DEnvironment.class);

	// TESTAR Windows Process layer, that allows interact with iv4XR SUTs using Windows API
	public static final Tag<WinProcess> windowsProcess = from("windowsProcess", WinProcess.class);

	// Specific Lab Recruits Environment, layer that facilitates the communication between agents and the Lab Recruits game
	public static final Tag<LabRecruitsEnvironmentListener> iv4xrLabRecruitsEnvironment = from("LabRecruitsEnvironment", LabRecruitsEnvironmentListener.class);

	// Specific SpaceEngineers Environment, layer that facilitates the communication between agents and the SpaceEngineers game
	public static final Tag<ProprietaryJsonTcpCharacterController> iv4xrSpaceEngController = from("iv4xrSpaceEngController", ProprietaryJsonTcpCharacterController.class);

	/**
	 * World Object Model Tags
	 */

	// Specific WOM for Lab Recruits game
	public static final Tag<LabWorldModel> iv4xrLabWorldModel = from("iv4xrLabWorldModel", LabWorldModel.class);

	// Specific Entities Observation for Lab Recruits game 
	public static final Tag<Observation> iv4xrObservation = from("Observation", Observation.class);

	// Specific tag to check if Lab Recruits game is in the foreground (is generic because it works at Windows process level)
	public static final Tag<Boolean> labRecruitsForeground = from("labRecruitsForeground", Boolean.class);

	// Tag that contains the LabRecruits NavMesh information (node and position)
	@SuppressWarnings("unchecked")
	public static final Tag<Set<SVec3>> labRecruitsNavMesh = from("labRecruitsNavMesh", (Class<Set<SVec3>>) (Class<?>) HashSet.class);

	// Associate a dynamic State Tag with the System for Agent Listening purposes
	//public static final Tag<State> labRecruitsState = from("labRecruitsState", State.class);

	// Associate a the dynamic Actions Tag with the System for Agent Listening purposes
	//public static final Tag<Set<Action>> labRecruitsActions = from("labRecruitsActions", (Class<Set<Action>>) (Class<?>) HashSet.class);

	//public static final Tag<Action> labRecruitsSelectedAgentAction = from("labRecruitsSelectedAgentAction", Action.class);


	/**
	 * Entity - Agent - Widget Tags
	 */

	// Property used by TESTAR to determine which widget is the Agent Entity
	public static final Tag<Widget> agentWidget = from("agentWidget", Widget.class);

	// The position of the WOM Agent
	public static final Tag<Vec3> agentPosition = from("agentPosition", Vec3.class);

	// Property used by TESTAR to determine if some widget is enabled / disabled
	public static final Tag<Boolean> entityEnabled = from("entityEnabled", Boolean.class);

	// Property used by TESTAR to determine if some widget exists with it is blocked by other, and not ready to interact with
	public static final Tag<Boolean> entityBlocked = from("entityBlocked", Boolean.class);

	// The center position of WOM Entity
	public static final Tag<Vec3> entityPosition = from("entityPosition", Vec3.class);
	public static final Tag<String> entityPositionRepresentation = from("entityPositionRepresentation", String.class);

	// Bounds box of WOM Entity
	public static final Tag<Vec3> entityBounds = from("entityBounds", Vec3.class);

	// Velocity of WOM Entity
	public static final Tag<Vec3> entityVelocity = from("entityVelocity", Vec3.class);

	// If WOM Entity is Dynamic
	public static final Tag<Boolean> entityDynamic = from("entityDynamic", Boolean.class);

	// A unique identifier of the WOM Entity
	public static final Tag<String> entityId = from("entityId", String.class);

	// A unique identifier of the WOM Agent
	public static final Tag<String> agentId = from("agentId", String.class);

	// Generic Type or Role of the WOM Entity
	public static final Tag<String> entityType = from("entityType", String.class);

	// Last time the State of the WOM Entity is sampled
	public static final Tag<Long> entityTimestamp = from("entityTimestamp", Long.class);

	// Property used by Lab Recruits game to determine if some Entity is Active or not
	public static final Tag<Boolean> labRecruitsEntityIsActive = from("labRecruitsEntityIsActive", Boolean.class);

	// Specific Lab Recruits game property ...
	public static final Tag<Integer> labRecruitsEntityLastUpdated = from("labRecruitsEntityLastUpdated", Integer.class);

	// Specific LabRecruits Agent properties
	public static final Tag<Integer> labRecruitsAgentHealth = from("labRecruitsAgentHealth", Integer.class);
	public static final Tag<Integer> labRecruitsAgentScore = from("labRecruitsAgentScore", Integer.class);
	public static final Tag<String> labRecruitsAgentMood = from("labRecruitsAgentMood", String.class);

	/**
	 * Agent - TESTAR comparison
	 */

	// Property used to know if an Action was selected by the Agent
	public static final Tag<Boolean> agentAction = from("agentAction", Boolean.class);

	// Property used to know if an Action discern between TESTAR and the Agent
	public static final Tag<Boolean> newActionByAgent = from("newActionByAgent", Boolean.class);

	/**
	 * Agent - SocioEmotional values
	 */

	public static final Tag<Double> agentPleasure = from("agentPleasure", Double.class);
	public static final Tag<Double> agentDominance = from("agentDominance", Double.class);
	public static final Tag<Double> agentArousal = from("agentArousal", Double.class);
}

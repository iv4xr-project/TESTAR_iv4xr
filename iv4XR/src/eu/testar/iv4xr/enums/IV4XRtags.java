/***************************************************************************************************
 *
 * Copyright (c) 2020 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 Open Universiteit - www.ou.nl
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

import org.fruit.alayer.Tag;
import org.fruit.alayer.TagsBase;
import org.fruit.alayer.windows.WinProcess;

import environments.LabRecruitsEnvironment;
import environments.SocketEnvironment;
import eu.iv4xr.framework.world.WorldModel;
import helperclasses.datastructures.Vec3;
import world.LabWorldModel;
import world.LegacyEntityType;
import world.LegacyObservation;

public class IV4XRtags extends TagsBase {
	
	private IV4XRtags() {}
	
	/**
	 * System - Environment Tags 
	 */
	
	// Generic JSON Socket Environment, layer to interact with the real environment exchanging JSON objects
	public static final Tag<SocketEnvironment> iv4xrSocketEnvironment = from("iv4xrSocketEnvironment", SocketEnvironment.class);
	
	// Generic WOM, structure and representation of the virtual world
	public static final Tag<WorldModel> iv4xrWorldModel = from("iv4xrWorldModel", WorldModel.class);
	
	// Specific Lab Recruits Environment, layer that facilitates the communication between agents and the Lab Recruits game
	public static final Tag<LabRecruitsEnvironment> iv4xrLabRecruitsEnvironment = from("LabRecruitsEnvironment", LabRecruitsEnvironment.class);
	
	// Specific WOM for Lab Recruits game
	public static final Tag<LabWorldModel> iv4xrLabWorldModel = from("iv4xrLabWorldModel", LabWorldModel.class);
	
	// Specific Entities Observation for Lab Recruits game 
	public static final Tag<LegacyObservation> iv4xrLegacyObservation = from("LegacyObservation", LegacyObservation.class);
	
	// TESTAR Windows Process layer, that allows interact with iv4XR SUTs using Windows API
	public static final Tag<WinProcess> windowsProcess = from("windowsProcess", WinProcess.class);
	
	// Specific tag to check if Lab Recruits game is in the foreground (is generic because it works at Windows process level)
	public static final Tag<Boolean> labRecruitsForeground = from("labRecruitsForeground", Boolean.class);
	
	
	/**
	 * Entity - Agent - Widget Tags
	 */

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
	
	// A unique identifier of the WOM Entity
	public static final Tag<String> entityId = from("entityId", String.class);
	
	// A unique identifier of the WOM Agent
	public static final Tag<String> agentId = from("agentId", String.class);
	
	// Generic Type or Role of the WOM Entity
	public static final Tag<LegacyEntityType> entityType = from("entityType", LegacyEntityType.class);
	
	// Last time the State of the WOM Entity is sampled
	public static final Tag<Long> entityTimestamp = from("entityTimestamp", Long.class);
	
	// Property used by Lab Recruits game to determine if some Entity is Active or not
	public static final Tag<Boolean> labRecruitsEntityIsActive = from("labRecruitsEntityIsActive", Boolean.class);
	
	// Specific Type or Role of the Lab Recruits game Entity
	public static final Tag<String> labRecruitsEntityType = from("labRecruitsEntityType", String.class);
	
	// Specific Lab Recruits game property with information about the Entity
	public static final Tag<String> labRecruitsEntityTag = from("labRecruitsEntityTag", String.class);
	
	// Specific Lab Recruits game description about the Entity
	public static final Tag<String> labRecruitsEntityProperty = from("labRecruitsEntityProperty", String.class);
	
	// Specific Lab Recruits game property ...
	public static final Tag<Integer> labRecruitsEntityLastUpdated = from("labRecruitsEntityLastUpdated", Integer.class);
}

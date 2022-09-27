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

package eu.testar.iv4xr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.fruit.alayer.Rect;
import org.fruit.alayer.TaggableBase;

/**
 * The Object or Node element of an application that will represent a TESTAR Widget
 * 
 * We are going to create a Node Map with all these elements and his properties,
 * to later create the Widget tree
 */
public class IV4XRElement extends TaggableBase implements Serializable {

	private static final long serialVersionUID = -4404777362271754899L;

	/**
	 * Widget Tree Node Map
	 */
	public List<IV4XRElement> children = new ArrayList<>();
	public IV4XRElement parent;
	public IV4XRRootElement root;
	public IV4XRWidgetEntity backRef;

	/**
	 * Generic TESTAR Properties
	 */
	public boolean enabled;
	public boolean blocked;

	public double zindex;

	public Rect rect;

	//TODO: Check these default values needed for State Model (State does not exist as widget with properties, should be Windows hwnd?)
	/**
	 * Generic iv4XR properties
	 */
	public eu.iv4xr.framework.spatial.Vec3 agentPosition = new eu.iv4xr.framework.spatial.Vec3(0, 0, 0);
	public eu.iv4xr.framework.spatial.Vec3 entityPosition = new eu.iv4xr.framework.spatial.Vec3(0, 0, 0);
	public eu.iv4xr.framework.spatial.Vec3 entityBounds = new eu.iv4xr.framework.spatial.Vec3(0, 0, 0);
	public eu.iv4xr.framework.spatial.Vec3 entityVelocity = new eu.iv4xr.framework.spatial.Vec3(0, 0, 0);
	public boolean entityDynamic = false;
	public String entityId = "";
	public String entityType = "";
	public long entityTimestamp = -1;

	/**
	 * Specific Lab Recruits iv4XR properties
	 */
	public boolean labRecruitsEntityIsActive = false;
	public int labRecruitsEntityLastUpdated = -1;
	public int labRecruitsAgentHealth = -1;
	public int labRecruitsAgentScore = -1;
	public String labRecruitsAgentMood = "";

	/**
	 * Specific Space Engineers iv4xr properties
	 */
	// Agent properties
	public spaceEngineers.model.Vec3F seAgentPosition = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seAgentOrientationForward = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seAgentOrientationUp = new spaceEngineers.model.Vec3F(0, 0, 0);
	public float seAgentHealth = 0f;
	public float seAgentOxygen = 0f;
	public float seAgentEnergy = 0f;
	public float seAgentHydrogen = 0f;
	public boolean seAgentJetpackRunning = false;
	public boolean seAgentDampenersOn = false;

	// Block properties
	public float seBuildIntegrity = 0f;
	public float seIntegrity = 0f;
	public float seMaxIntegrity = 0f;
	public spaceEngineers.model.Vec3F seMaxPosition = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seMinPosition = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seOrientationForward = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seOrientationUp = new spaceEngineers.model.Vec3F(0, 0, 0);
	public spaceEngineers.model.Vec3F seSize = new spaceEngineers.model.Vec3F(0, 0, 0);
	public String seDefinitionId = "";
	public boolean seFunctional = false;
	public boolean seWorking = false;
	public String seOwnerId = "";
	public String seBuiltBy = "";

	// https://github.com/iv4xr-project/iv4xr-se-plugin/blob/main/JvmClient/src/commonMain/kotlin/spaceEngineers/model/BlockDataClasses.kt
	// Specific properties of functional, terminal, door and power blocks
	public String seCustomName = "";
	public boolean seShowInInventory = false;
	public boolean seShowInTerminal = false;
	public boolean seShowOnHUD = false;

	public boolean seFunctionalEnabled = false;

	public boolean seDoorOpen = false;
	public boolean seDoorAnyoneCanUse = false;

	public float seFuelMaxOutput = 0f;
	public float seFuelCurrentOutput = 0f;
	public float seFuelCapacity = 0f;

	public IV4XRElement(){ this(null); }

	public IV4XRElement(IV4XRElement parent){
		this.parent = parent;
		if(parent != null)
			root = parent.root;
		enabled = true;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException{
		oos.defaultWriteObject();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
		ois.defaultReadObject();
	}
}

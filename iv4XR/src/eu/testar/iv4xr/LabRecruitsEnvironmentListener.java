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


package eu.testar.iv4xr;

import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import helperclasses.datastructures.Vec3;
import world.LabWorldModel;

public class LabRecruitsEnvironmentListener extends LabRecruitsEnvironment {
	
	public String agentId;
	public String targetId;
	public Vec3 agentPosition;
	public Vec3 targetPosition;
	
	public String actionExecuted = "";
	
	public LabRecruitsEnvironmentListener(EnvironmentConfig environment) {
		super(environment);
	}
	
	@Override
	public LabWorldModel observe(String agentId){
		
		System.out.println("LISTENED observe");
		
		this.actionExecuted = "Observe";
		return super.observe(agentId);
	}

	@Override
	public LabWorldModel interactWith(String agentId, String target){
		
		System.out.println("LISTENED interactWith : " + target);
		
		this.agentId = agentId;
		this.targetId = target;
		this.actionExecuted = "interact";
		return super.interactWith(agentId, target);
	}

	@Override
	public LabWorldModel moveToward(String agentId, Vec3 agentPosition, Vec3 target) {
		
		System.out.println("LISTENED moveToward : " + target);
		
		this.agentId = agentId;
		this.agentPosition = agentPosition;
		this.targetPosition = target;
		this.actionExecuted = "move";
		return super.moveToward(agentId, agentPosition, target);
	}
}

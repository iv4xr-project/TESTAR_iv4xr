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

package org.testar.action.priorization;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.fruit.Pair;

import eu.testar.iv4xr.enums.SVec3;

public class iv4xrNavigableState {

	private String executedAction;
	private Set<SVec3> navigableNodes;
	private Set<Pair<String, Boolean>> reachableEntities;

	public iv4xrNavigableState(String executedAction) {
		this.executedAction = executedAction;
		this.navigableNodes = new HashSet<>();
		this.reachableEntities = new HashSet<>();
	}

	public String getExecutedAction() {
		return executedAction;
	}

	public Set<SVec3> getNavigableNodes() {
		return navigableNodes;
	}

	public Set<Pair<String, Boolean>> getReachableEntities() {
		if(!reachableEntities.isEmpty()) return reachableEntities;
		return new HashSet<>(Arrays.asList(new Pair<String, Boolean>("None", false)));
	}

	public void addNavigableNode(Set<SVec3> nodesPositions) {
		for(SVec3 nodePosition : nodesPositions) {
			this.navigableNodes.add(nodePosition);
		}
	}

	public void addReachableEntity(String entityId, boolean entityIsActive) {
		this.reachableEntities.add(new Pair<String, Boolean>(entityId, entityIsActive));
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof iv4xrNavigableState)) {
			return false;
		}
		if(!((iv4xrNavigableState) o).getExecutedAction().equals(this.executedAction)) {
			return false;
		}
		if(!((iv4xrNavigableState) o).getNavigableNodes().equals(this.navigableNodes)) {
			return false;
		}
		if(!((iv4xrNavigableState) o).getReachableEntities().equals(this.reachableEntities)) {
			return false;
		}

		return true;
	}

}

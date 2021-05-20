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

package nl.ou.testar.StateModel.iv4XR;

import java.util.HashSet;
import java.util.Set;

import org.fruit.Pair;

import eu.testar.iv4xr.enums.SVec3;
import nl.ou.testar.StateModel.AbstractEntity;
import nl.ou.testar.StateModel.Persistence.Persistable;

public class NavigableState extends AbstractEntity implements Persistable {

	private Set<SVec3> navigableNodes;
	private Set<Pair<String, Boolean>> reachableEntities;
	private Set<String> inboundActions;

	public NavigableState(String hashId, Set<SVec3> navigableNodes, 
			Set<Pair<String, Boolean>> reachableEntities, String inboundAction) {
		super(hashId);
		this.navigableNodes = new HashSet<>(navigableNodes);
		this.reachableEntities = new HashSet<>(reachableEntities);
		this.inboundActions = new HashSet<>();
		this.inboundActions.add(inboundAction);
	}

	public Set<SVec3> getNavigableNodes() {
		return navigableNodes;
	}

	public Set<Pair<String, Boolean>> getReachableEntities() {
		return reachableEntities;
	}

	public Set<String> getInboundActions() {
		return inboundActions;
	}

	public void addInboundAction(String inboundAction) {
		this.inboundActions.add(inboundAction);
	}

	@Override
	public boolean canBeDelayed() {
		return false;
	}

}

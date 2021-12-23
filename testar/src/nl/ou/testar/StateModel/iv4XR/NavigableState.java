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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.fruit.Pair;

import eu.testar.iv4xr.enums.SVec3;
import nl.ou.testar.StateModel.AbstractEntity;
import nl.ou.testar.StateModel.Persistence.Persistable;

public class NavigableState extends AbstractEntity implements Persistable {

	private Set<SVec3> navigableNodes;
	private Set<Pair<String, Boolean>> reachableEntities;
	private Map<String, NavigableAction> outgoingNavigableActions;
	private Set<String> outgoingNavigableActionsDescription;

	// TODO: If AbstractCustomAction depends on position node, we can use a single Set<String>
	private Set<Pair<String, SVec3>> unexecutedExploratoryActions;

	public NavigableState(Set<SVec3> navigableNodes, Set<Pair<String, Boolean>> reachableEntities) {
		/**
		 * Not sure if the hash identifier should depends on the navigable nodes,
		 * or on the reachable Entities. (Or other identifier) 
		 * 
		 * Is it possible to have two different NavigableStates with the same ?
		 * - reachable entities and their isActive boolean properties
		 * but with different ?
		 * - navigable nodes (a new empty room? but then a door had been opened)
		 * 
		 * But if we have an entity (button) on which an interaction
		 * - Changes the isActive property of the button, the reachableEntities will create a new hash id
		 * - But we really did not change the navigable nodes, so navigableNodes will be the same hash id
		 * 
		 */

		// The hash identifier of this NavigableState depends on all reachable entities
		super(String.valueOf(Objects.hash(reachableEntities)));
		// The hash identifier of this NavigableState depends on all navigable nodes
		//super(String.valueOf(Objects.hash(navigableNodes)));
		this.navigableNodes = new HashSet<>(navigableNodes);
		this.reachableEntities = new HashSet<>(reachableEntities);
		this.outgoingNavigableActions = new HashMap<>();
		this.outgoingNavigableActionsDescription = new HashSet<>();
		this.unexecutedExploratoryActions = new HashSet<>();
	}

	public Set<SVec3> getNavigableNodes() {
		return navigableNodes;
	}

	public Set<Pair<String, Boolean>> getReachableEntities() {
		return reachableEntities;
	}

	public Map<String, NavigableAction> getOutgoingNavigableActions() {
		return outgoingNavigableActions;
	}

	public Set<String> getOutgoingNavigableActionsDescriptions() {
		return outgoingNavigableActionsDescription;
	}

	public void addOutgoingNavigableAction(String navigableActionId, NavigableAction navigableAction) {
		this.outgoingNavigableActions.putIfAbsent(navigableActionId, navigableAction);
		this.outgoingNavigableActionsDescription.add(navigableAction.getDescription());
	}

	public Set<Pair<String, SVec3>> getUnexecutedExploratoryActions() {
		return unexecutedExploratoryActions;
	}

	public void addUnexecutedExploratoryAction(String unexecutedExploratoryAction, SVec3 actionNode) {
		// If unexecuted list contains empty signal, this specific NavigableState was already explored
		// Do not include exploratory actions anymore
		if(this.unexecutedExploratoryActions.toString().contains("empty")) {
			return;
		}
		Pair<String, SVec3> action = new Pair<String, SVec3>(unexecutedExploratoryAction, actionNode);
		if(!this.unexecutedExploratoryActions.contains(action)) {
			this.unexecutedExploratoryActions.add(action);
		}
	}

	// TODO: Do not use set to invoke from NavigableStateExtractor, but create a new constructor
	public void setUnexecutedExploratoryAction(Set<Pair<String, SVec3>> unexecutedExploratoryActions) {
		this.unexecutedExploratoryActions = unexecutedExploratoryActions;
	}

	public void removeExploratoryActionFromUnexecuted(String executedExploratoryAction) {
		for(Pair<String, SVec3> action : this.unexecutedExploratoryActions) {
			if(action.left().equals(executedExploratoryAction)) {
				this.unexecutedExploratoryActions.remove(action);
			}
		}
	}

	@Override
	public boolean canBeDelayed() {
		return false;
	}

}

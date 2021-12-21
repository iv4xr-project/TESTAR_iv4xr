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

package nl.ou.testar.StateModel.Persistence.OrientDB.Extractor;

import java.util.HashSet;
import java.util.Set;
import org.fruit.Pair;

import eu.testar.iv4xr.enums.SVec3;
import nl.ou.testar.StateModel.AbstractStateModel;
import nl.ou.testar.StateModel.Exception.ExtractionException;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.DocumentEntity;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.EdgeEntity;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.PropertyValue;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.VertexEntity;
import nl.ou.testar.StateModel.iv4XR.NavigableAction;
import nl.ou.testar.StateModel.iv4XR.NavigableState;

public class NavigableStateExtractor implements EntityExtractor<NavigableState> {

	private NavigableActionExtractor navigableActionExtractor;

	/**
	 * Constructor.
	 * @param navigableActionExtractor
	 */
	public NavigableStateExtractor(NavigableActionExtractor navigableActionExtractor) {
		this.navigableActionExtractor = navigableActionExtractor;
	}

	@Override
	public NavigableState extract(DocumentEntity entity, AbstractStateModel abstractStateModel) throws ExtractionException {
		if (!(entity instanceof VertexEntity)) {
			throw new ExtractionException("Navigable state extractor expects a vertex entity. Instance of " + entity.getClass().toString() + " was given.");
		}
		if (!entity.getEntityClass().getClassName().equals("NavigableState")) {
			throw new ExtractionException("Entity of class NavigableState expected. Class " + entity.getEntityClass().getClassName() + " given.");
		}

		// create the navigable state
		PropertyValue navigableNodesValue = entity.getPropertyValue("navigableNodes");
		if(!(navigableNodesValue.getValue() instanceof Set<?>)) {
			throw new ExtractionException("Exception extracting from State Model: navigableNodesValue is not an instance of Set");
		}
		PropertyValue reachableEntitiesValue = entity.getPropertyValue("reachableEntities");
		if(!(reachableEntitiesValue.getValue() instanceof Set<?>)) {
			throw new ExtractionException("Exception extracting from State Model: reachableEntitiesValue is not an instance of Set");
		}
		//NavigableState navigableState = new NavigableState((Set<SVec3>)navigableNodesValue.getValue(), (Set<Pair<String, Boolean>>)reachableEntitiesValue.getValue());
		Set<SVec3> navigableNodes = (Set<SVec3>)navigableNodesValue.getValue();
		Set<Pair<String, Boolean>> reachableEntities = (Set<Pair<String, Boolean>>)reachableEntitiesValue.getValue();
		NavigableState navigableState = new NavigableState(navigableNodes, reachableEntities);

		// set the model identifier
		navigableState.setModelIdentifier(abstractStateModel.getModelIdentifier());

		// NavigableAction uses the NavigableState identifier
		navigableActionExtractor.setNavigableStateId(navigableState.getId());

		// prepare the navigable actions
		Set<NavigableAction> actions = new HashSet<>();
		for (EdgeEntity edgeEntity : ((VertexEntity) entity).getOutgoingEdges()) {
			NavigableAction navigableAction = navigableActionExtractor.extract(edgeEntity, abstractStateModel);
			actions.add(navigableAction);
		}

		// add the navigable actions information (this also includes the action description)
		for(NavigableAction navigableAction : actions) {
			navigableState.addNavigableAction(navigableAction.getId(), navigableAction);
		}

		// add the unexecuted Exploratory Actions information
		PropertyValue unexecutedExploratoryActionsValue = entity.getPropertyValue("unexecutedExploratoryActions");
		if(!(unexecutedExploratoryActionsValue.getValue() instanceof Set<?>)) {
			throw new ExtractionException("Exception extracting from State Model: unexecutedExploratoryActions is not an instance of Set");
		}
		Set<Pair<String, SVec3>> unexecutedExploratoryActions = (Set<Pair<String, SVec3>>)unexecutedExploratoryActionsValue.getValue();
		if(!unexecutedExploratoryActions.toString().contains("empty")) {
			navigableState.setUnexecutedExploratoryAction(unexecutedExploratoryActions);
		}

		System.out.println("****** NavigableStateExtractor ******");
		System.out.println("navigableState.getId(): " + navigableState.getId());
		System.out.println("navigableState.getNavigableNodes(): " + navigableState.getNavigableNodes());
		System.out.println("navigableState.getReachableEntities(): " + navigableState.getReachableEntities());
		System.out.println("navigableState.getUnexecutedExploratoryActions(): " + navigableState.getUnexecutedExploratoryActions());

		return navigableState;
	}

}

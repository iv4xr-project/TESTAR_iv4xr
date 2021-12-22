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

package nl.ou.testar.StateModel.Persistence.OrientDB.Hydrator;

import com.orientechnologies.orient.core.metadata.schema.OType;

import nl.ou.testar.StateModel.Exception.HydrationException;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.Property;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.PropertyValue;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.VertexEntity;
import nl.ou.testar.StateModel.Util.HydrationHelper;
import nl.ou.testar.StateModel.iv4XR.NavigableState;

public class NavigableStateHydrator implements EntityHydrator<VertexEntity> {

	@Override
	public void hydrate(VertexEntity target, Object source) throws HydrationException {
		if (!(source instanceof NavigableState)) {
			throw new HydrationException();
		}

		// first make sure the identity property is set
		Property identifier = target.getEntityClass().getIdentifier();
		if (identifier == null) {
			throw new HydrationException("No identifying properties were provided for entity class " + target.getEntityClass().getClassName());
		}

		// add the modelIdentifier
		String modelIdentifier = ((NavigableState) source).getModelIdentifier();
		target.addPropertyValue("modelIdentifier", new PropertyValue(OType.STRING, modelIdentifier));

		// add the hashId
		String stateId = ((NavigableState) source).getId();
		target.addPropertyValue("stateId", new PropertyValue(OType.STRING, stateId));

		// combine the modelIdentifier and the stateId to create a unique id
		String uniqueId = HydrationHelper.lowCollisionID(modelIdentifier + "--" + stateId);
		target.addPropertyValue(identifier.getPropertyName(), new PropertyValue(identifier.getPropertyType(), uniqueId));

		// add navigableNodes
		Property navigableNodes = HydrationHelper.getProperty(target.getEntityClass().getProperties(), "navigableNodes");
		if (navigableNodes == null) {
			throw new HydrationException();
		}
		if (!((NavigableState) source).getNavigableNodes().isEmpty()) {
			target.addPropertyValue(navigableNodes.getPropertyName(), new PropertyValue(navigableNodes.getPropertyType(), ((NavigableState) source).getNavigableNodes()));
		}

		// add reachableEntities
		Property reachableEntities = HydrationHelper.getProperty(target.getEntityClass().getProperties(), "reachableEntities");
		if (reachableEntities == null) {
			throw new HydrationException();
		}
		if (!((NavigableState) source).getReachableEntities().isEmpty()) {
			target.addPropertyValue(reachableEntities.getPropertyName(), new PropertyValue(reachableEntities.getPropertyType(), ((NavigableState) source).getReachableEntities()));
		}

		// add outgoingNavigableActions
		Property outgoingNavigableActions = HydrationHelper.getProperty(target.getEntityClass().getProperties(), "outgoingNavigableActions");
		if (outgoingNavigableActions == null) {
			throw new HydrationException();
		}
		if (!((NavigableState) source).getOutgoingNavigableActions().isEmpty()) {
			target.addPropertyValue(outgoingNavigableActions.getPropertyName(), new PropertyValue(outgoingNavigableActions.getPropertyType(), ((NavigableState) source).getOutgoingNavigableActions()));
		}

		// add outgoingNavigableActionsDescription
		Property outgoingNavigableActionsDescription = HydrationHelper.getProperty(target.getEntityClass().getProperties(), "outgoingNavigableActionsDescription");
		if (outgoingNavigableActionsDescription == null) {
			throw new HydrationException();
		}
		if (!((NavigableState) source).getOutgoingNavigableActionsDescriptions().isEmpty()) {
			target.addPropertyValue(outgoingNavigableActionsDescription.getPropertyName(), new PropertyValue(outgoingNavigableActionsDescription.getPropertyType(), ((NavigableState) source).getOutgoingNavigableActionsDescriptions()));
		}

		// add unexecutedExploratoryActions
		Property unexecutedExploratoryActions = HydrationHelper.getProperty(target.getEntityClass().getProperties(), "unexecutedExploratoryActions");
		if (unexecutedExploratoryActions == null) {
			throw new HydrationException();
		}
		if (!((NavigableState) source).getUnexecutedExploratoryActions().isEmpty()) {
			target.addPropertyValue(unexecutedExploratoryActions.getPropertyName(), new PropertyValue(unexecutedExploratoryActions.getPropertyType(), ((NavigableState) source).getUnexecutedExploratoryActions()));
		}
	}

}

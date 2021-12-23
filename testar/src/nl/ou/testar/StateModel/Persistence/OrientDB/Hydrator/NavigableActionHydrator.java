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
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.EdgeEntity;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.Property;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.PropertyValue;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.TypeConvertor;
import nl.ou.testar.StateModel.Util.HydrationHelper;
import nl.ou.testar.StateModel.iv4XR.NavigableAction;

public class NavigableActionHydrator implements EntityHydrator<EdgeEntity> {

	@Override
	public void hydrate(EdgeEntity edgeEntity, Object source) throws HydrationException {
		if (!(source instanceof NavigableAction)) {
			throw new HydrationException();
		}

		// first make sure the identity property is set
		Property identifier = edgeEntity.getEntityClass().getIdentifier();
		if (identifier == null) {
			throw new HydrationException();
		}

		// add the modelIdentifier
		String modelIdentifier = ((NavigableAction) source).getModelIdentifier();
		edgeEntity.addPropertyValue("modelIdentifier", new PropertyValue(OType.STRING, modelIdentifier));

		// use the transition of the navigable action with the navigable states to create a unique identifier in the model
		Property sourceIdentifier = edgeEntity.getSourceEntity().getEntityClass().getIdentifier();
		String sourceId = (String)edgeEntity.getSourceEntity().getPropertyValue(sourceIdentifier.getPropertyName()).getValue();
		Property targetIdentifier = edgeEntity.getTargetEntity().getEntityClass().getIdentifier();
		String targetId = (String)edgeEntity.getTargetEntity().getPropertyValue(targetIdentifier.getPropertyName()).getValue();

		String edgeId = HydrationHelper.createOrientDbActionId(sourceId, targetId, ((NavigableAction) source).getId(), modelIdentifier);
		// make sure the java and orientdb property types are compatible
		OType identifierType = TypeConvertor.getInstance().getOrientDBType(edgeId.getClass());
		if (identifierType != identifier.getPropertyType()) {
			throw new HydrationException("Wrong type specified for navigable action identifier");
		}
		edgeEntity.addPropertyValue(identifier.getPropertyName(), new PropertyValue(identifier.getPropertyType(), edgeId));

		// add the navigableActionId
		edgeEntity.addPropertyValue("navigableActionId", new PropertyValue(OType.STRING, ((NavigableAction) source).getId()));

		// add the abstractActionId
		edgeEntity.addPropertyValue("abstractActionId", new PropertyValue(OType.STRING, ((NavigableAction) source).getAbstractActionId()));

		// add the description
		edgeEntity.addPropertyValue("description", new PropertyValue(OType.STRING, ((NavigableAction) source).getDescription()));

		// add the originNavigableStateId
		edgeEntity.addPropertyValue("originNavigableStateId", new PropertyValue(OType.STRING, ((NavigableAction) source).getOriginNavigableStateId()));

	}

}

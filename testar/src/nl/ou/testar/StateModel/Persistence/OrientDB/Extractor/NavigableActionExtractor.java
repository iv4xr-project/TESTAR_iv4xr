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

import com.orientechnologies.orient.core.metadata.schema.OType;

import nl.ou.testar.StateModel.AbstractStateModel;
import nl.ou.testar.StateModel.Exception.ExtractionException;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.DocumentEntity;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.EdgeEntity;
import nl.ou.testar.StateModel.Persistence.OrientDB.Entity.PropertyValue;
import nl.ou.testar.StateModel.iv4XR.NavigableAction;

public class NavigableActionExtractor implements EntityExtractor<NavigableAction> {

	private String navigableStateId;

	public void setNavigableStateId(String navigableStateId) {
		this.navigableStateId = navigableStateId;
	}

	@Override
	public NavigableAction extract(DocumentEntity entity, AbstractStateModel abstractStateModel) throws ExtractionException {
		if (!(entity instanceof EdgeEntity)) {
			throw new ExtractionException("Navigable action extractor expects an edge entity. Instance of " + entity.getClass().toString() + " was given.");
		}
		if (!entity.getEntityClass().getClassName().equals("NavigableAction")) {
			throw new ExtractionException("Entity of class NavigableAction expected. Class " + entity.getEntityClass().getClassName() + " given.");
		}

		// get the navigable abstractActionId
		PropertyValue abstractActionIdValue = entity.getPropertyValue("abstractActionId");
		if (abstractActionIdValue.getType() != OType.STRING) {
			throw new ExtractionException("Expected string value for abstractActionId attribute. Type " + abstractActionIdValue.getType().toString() + " given.");
		}
		String abstractActionId = abstractActionIdValue.getValue().toString();

		// get the navigable description
		PropertyValue descriptionValue = entity.getPropertyValue("description");
		if (descriptionValue.getType() != OType.STRING) {
			throw new ExtractionException("Expected string value for description attribute. Type " + descriptionValue.getType().toString() + " given.");
		}
		String description = descriptionValue.getValue().toString();

		NavigableAction navigableAction = new NavigableAction(abstractActionId, description, navigableStateId);
		navigableAction.setModelIdentifier(abstractStateModel.getModelIdentifier());

		return navigableAction;
	}

}

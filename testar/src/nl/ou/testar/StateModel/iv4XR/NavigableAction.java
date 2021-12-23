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

import nl.ou.testar.StateModel.AbstractEntity;
import nl.ou.testar.StateModel.Persistence.Persistable;

public class NavigableAction extends AbstractEntity implements Persistable {

	private String abstractActionId;
	private String description;
	private String originNavigableStateId;
	//private AbstractAction abstractAction;

	public NavigableAction(String abstractActionId, /*AbstractAction abstractAction,*/ String description, String originNavigableStateId) {
		// The hash identifier of this NavigableAction depends on the interaction with the entity
		//super(String.valueOf(originNavigableStateId + "-" + Objects.hash(description)));
		super(String.valueOf(originNavigableStateId + "-" + description));
		//this.abstractAction = abstractAction;
		this.abstractActionId = abstractActionId;
		this.description = description;
		this.originNavigableStateId = originNavigableStateId;
	}

	/*public AbstractAction getAbstractAction() {
		return abstractAction;
	}*/

	public String getAbstractActionId() {
		return abstractActionId;
	}

	public String getDescription() {
		return description;
	}

	public String getOriginNavigableStateId() {
		return originNavigableStateId;
	}

	@Override
	public boolean canBeDelayed() {
		return false;
	}

}

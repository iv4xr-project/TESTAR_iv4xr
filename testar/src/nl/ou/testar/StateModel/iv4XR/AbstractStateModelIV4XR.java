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
import java.util.Map;
import java.util.Set;

import org.fruit.alayer.Tag;

import nl.ou.testar.StateModel.AbstractStateModel;
import nl.ou.testar.StateModel.Event.StateModelEventListener;
import nl.ou.testar.StateModel.Exception.InvalidStateIdException;
import nl.ou.testar.StateModel.Exception.StateModelException;

public class AbstractStateModelIV4XR extends AbstractStateModel {

	// the navigable states in the model
	private Map<String, NavigableState> navigableStates;

	public AbstractStateModelIV4XR(String modelIdentifier, 
			String applicationName, 
			String applicationVersion,
			Set<Tag<?>> tags, 
			StateModelEventListener ...eventListeners) {
		super(modelIdentifier, applicationName, applicationVersion, tags, eventListeners);
		if(this.navigableStates==null) {this.navigableStates = new HashMap<>();}
		System.out.println("AbstractStateModelIV4XR initial NavigableStates: " + this.navigableStates.size());
	}

	/**
	 * This method adds a new navigable state to the collection of states
	 * @param newState
	 * @throws StateModelException
	 */
	public void addNavigableState(NavigableState newNavigableState) throws StateModelException {
		checkStateId(newNavigableState.getId());
		if (!containsState(newNavigableState.getId())) {
			// provide the state with this state model's abstract identifier
			newNavigableState.setModelIdentifier(getModelIdentifier());
			if(this.navigableStates==null) {this.navigableStates = new HashMap<>();}
			this.navigableStates.put(newNavigableState.getId(), newNavigableState);
		}
	}

	//TODO: addNavigableTransition

	/**
	 * This is a helper method to check if the navigable Id that is provided is valid.
	 * @param navigableStateId identifier to verify
	 * @throws StateModelException
	 */
	protected void checkStateId(String navigableStateId) throws StateModelException{
		if (navigableStateId == null || navigableStateId.equals("")) {
			throw new InvalidStateIdException();
		}
	}

}
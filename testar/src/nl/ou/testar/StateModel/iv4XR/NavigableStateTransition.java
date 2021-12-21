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

import nl.ou.testar.StateModel.Persistence.Persistable;

public class NavigableStateTransition implements Persistable {

	private NavigableState sourceNavigableState;
	private NavigableState targetNavigableState;
	private NavigableAction navigableAction;

	/**
	 * Constructor
	 * @param sourceNavigableState
	 * @param targetNavigableState
	 * @param navigableAction
	 */
	public NavigableStateTransition(NavigableState sourceNavigableState, NavigableState targetNavigableState, NavigableAction navigableAction) {
		this.sourceNavigableState = sourceNavigableState;
		this.targetNavigableState = targetNavigableState;
		this.navigableAction = navigableAction;
	}

	/**
	 * Get the id for the source navigable state of this transition
	 * @return
	 */
	public String getSourceNavigableStateId() {
		return sourceNavigableState.getId();
	}

	/**
	 * Get the id for the target navigable state of this transition
	 * @return
	 */
	public String getTargetNavigableStateId() {
		return targetNavigableState.getId();
	}

	/**
	 * Get the id for the executed navigableAction in this transition
	 * @return
	 */
	public String getNavigableActionId() {
		return navigableAction.getId();
	}

	/**
	 * Get the source navigable state for this transition
	 * @param sourceNavigableState
	 */
	public void setSourceNavigableState(NavigableState sourceNavigableState) {
		this.sourceNavigableState = sourceNavigableState;
	}

	/**
	 * Get the target navigable state for this transition
	 * @param targetNavigableState
	 */
	public void setTargetNavigableState(NavigableState targetNavigableState) {
		this.targetNavigableState = targetNavigableState;
	}

	/**
	 * Get the navigableAction for this transition
	 * @param navigableAction
	 */
	public void setNavigableAction(NavigableAction navigableAction) {
		this.navigableAction = navigableAction;
	}

	/**
	 * Get the source navigable state for this transition
	 * @return
	 */
	public NavigableState getSourceNavigableState() {
		return sourceNavigableState;
	}

	/**
	 * Get the target navigable state for this transition
	 * @return
	 */
	public NavigableState getTargetNavigableState() {
		return targetNavigableState;
	}

	/**
	 * Get the executed navigableAction for this transition
	 * @return
	 */
	public NavigableAction getNavigableAction() {
		return navigableAction;
	}

	@Override
	public boolean canBeDelayed() {
		return false;
	}
}
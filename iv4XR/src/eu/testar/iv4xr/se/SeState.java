/***************************************************************************************************
 *
 * Copyright (c) 2020 - 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 - 2022 Open Universiteit - www.ou.nl
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

package eu.testar.iv4xr.se;

import org.fruit.alayer.Tag;
import eu.testar.iv4xr.IV4XRRootElement;
import eu.testar.iv4xr.IV4XRState;
import eu.testar.iv4xr.IV4XRWidgetEntity;
import eu.testar.iv4xr.enums.IV4XRMapping;
import eu.testar.iv4xr.enums.IV4XRtags;

public class SeState extends IV4XRState {

	private static final long serialVersionUID = 5168087725033668809L;

	public SeState(IV4XRRootElement root) {
		super(root);
	}

	@Override
	public <T> T get(IV4XRWidgetEntity w, Tag<T> t) {
		T ret = get(w, t, null);
		if (ret == null) {
			return super.get(w, t);
		}
		return ret;
	}

	/**
	 * Specific iv4xr System Tags (SpaceEngineers)
	 * 
	 * @param <T>
	 * @param w
	 * @param t
	 * @param defaultValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(IV4XRWidgetEntity w, Tag<T> t, T defaultValue) {

		Tag<T> stateManagementTag = IV4XRMapping.getMappedStateTagSE(t);
		if (stateManagementTag != null) {
			t = stateManagementTag;
		}

		Object ret = w.tags.get(t);

		if (ret != null) {
			return (T)ret;
		}

		else if (w.element == null || w.tags.containsKey(t) || !(w.element instanceof SeElement)) {
			// If the tag is not a SE tag invoke default iv4XR state
			return super.get(w, t, defaultValue);
		}

		SeElement seElement = (SeElement) w.element;

		if (t.equals(IV4XRtags.seAgentPosition)) {
			ret = seElement.seAgentPosition;
		}
		else if (t.equals(IV4XRtags.seAgentOrientationForward)) {
			ret = seElement.seAgentOrientationForward;
		}
		else if (t.equals(IV4XRtags.seAgentOrientationUp)) {
			ret = seElement.seAgentOrientationUp;
		}
		else if (t.equals(IV4XRtags.seAgentHealth)) {
			ret = seElement.seAgentHealth;
		}
		else if (t.equals(IV4XRtags.seAgentOxygen)) {
			ret = seElement.seAgentOxygen;
		}
		else if (t.equals(IV4XRtags.seAgentEnergy)) {
			ret = seElement.seAgentEnergy;
		}
		else if (t.equals(IV4XRtags.seAgentHydrogen)) {
			ret = seElement.seAgentHydrogen;
		}
		else if (t.equals(IV4XRtags.seAgentJetpackRunning)) {
			ret = seElement.seAgentJetpackRunning;
		}
		else if (t.equals(IV4XRtags.seAgentDampenersOn)) {
			ret = seElement.seAgentDampenersOn;
		}
		else if (t.equals(IV4XRtags.seBuildIntegrity)) {
			ret = seElement.seBuildIntegrity;
		}
		else if (t.equals(IV4XRtags.seIntegrity)) {
			ret = seElement.seIntegrity;
		}
		else if (t.equals(IV4XRtags.seMaxIntegrity)) {
			ret = seElement.seMaxIntegrity;
		}
		else if (t.equals(IV4XRtags.seMaxPosition)) {
			ret = seElement.seMaxPosition;
		}
		else if (t.equals(IV4XRtags.seMinPosition)) {
			ret = seElement.seMinPosition;
		}
		else if (t.equals(IV4XRtags.seOrientationForward)) {
			ret = seElement.seOrientationForward;
		}
		else if (t.equals(IV4XRtags.seOrientationUp)) {
			ret = seElement.seOrientationUp;
		}
		else if (t.equals(IV4XRtags.seSize)) {
			ret = seElement.seSize;
		}
		else if (t.equals(IV4XRtags.seDefinitionId)) {
			ret = seElement.seDefinitionId;
		}
		else if (t.equals(IV4XRtags.seFunctional)) {
			ret = seElement.seFunctional;
		}
		else if (t.equals(IV4XRtags.seWorking)) {
			ret = seElement.seWorking;
		}
		else if (t.equals(IV4XRtags.seOwnerId)) {
			ret = seElement.seOwnerId;
		}
		else if (t.equals(IV4XRtags.seBuiltBy)) {
			ret = seElement.seBuiltBy;
		}
		else if (t.equals(IV4XRtags.seCustomName)) {
			ret = seElement.seCustomName;
		}
		else if (t.equals(IV4XRtags.seShowInInventory)) {
			ret = seElement.seShowInInventory;
		}
		else if (t.equals(IV4XRtags.seShowInTerminal)) {
			ret = seElement.seShowInTerminal;
		}
		else if (t.equals(IV4XRtags.seShowOnHUD)) {
			ret = seElement.seShowOnHUD;
		}
		else if (t.equals(IV4XRtags.seFunctionalEnabled)) {
			ret = seElement.seFunctionalEnabled;
		}
		else if (t.equals(IV4XRtags.seDoorOpen)) {
			ret = seElement.seDoorOpen;
		}
		else if (t.equals(IV4XRtags.seDoorAnyoneCanUse)) {
			ret = seElement.seDoorAnyoneCanUse;
		}
		else if (t.equals(IV4XRtags.seFuelMaxOutput)) {
			ret = seElement.seFuelMaxOutput;
		}
		else if (t.equals(IV4XRtags.seFuelCurrentOutput)) {
			ret = seElement.seFuelCurrentOutput;
		}
		else if (t.equals(IV4XRtags.seFuelCapacity)) {
			ret = seElement.seFuelCapacity;
		}
		else if (t.equals(IV4XRtags.seFocusedScreen)) {
			ret = ((SERootElement)seElement.root).focusedScreen;
		}
		else if (t.equals(IV4XRtags.seTerminalTab)) {
			ret = seElement.terminalTab;
		}
		else if (t.equals(IV4XRtags.seDataInventory)) {
			ret = seElement.dataInventory;
		}
		else if (t.equals(IV4XRtags.seLeftInventory)) {
			ret = seElement.leftInventory;
		}
		else if (t.equals(IV4XRtags.seRightInventory)) {
			ret = seElement.rightInventory;
		}
		else if (t.equals(IV4XRtags.seDataControlPanel)) {
			ret = seElement.dataControlPanel;
		}
		else if (t.equals(IV4XRtags.seDataProduction)) {
			ret = seElement.dataProduction;
		}

		// If the return value is not null, add it to the tags map
		if(ret != null) {
			cacheTag(w, t, ret);
		}

		return (ret == null) ? super.get(w, t, defaultValue) : (T) ret;
	}

}

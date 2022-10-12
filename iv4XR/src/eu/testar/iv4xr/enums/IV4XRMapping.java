/***************************************************************************************************
 *
 * Copyright (c) 2020 - 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 - 2021 Open Universiteit - www.ou.nl
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

package eu.testar.iv4xr.enums;

import static es.upv.staq.testar.StateManagementTags.*;
import java.util.HashMap;
import java.util.Map;

import org.fruit.alayer.Tag;
import es.upv.staq.testar.ActionManagementTags;

public class IV4XRMapping {

	// a mapping from the state management tags to iv4XR tags
	private static Map<Tag<?>, Tag<?>> stateTagMappingIV4XR = new HashMap<Tag<?>, Tag<?>>()
	{
		{
			put(iv4xrEntityId, IV4XRtags.entityId);
			put(iv4xrAgentId, IV4XRtags.agentId);
			put(iv4xrEntityEnabled, IV4XRtags.entityEnabled);
			put(iv4xrEntityBlocked, IV4XRtags.entityBlocked);
			// Add the entity position to the State Model Concrete layer, but not the agentPosition
			put(iv4xrEntityPosition, IV4XRtags.entityPosition);
			put(iv4xrEntityBounds, IV4XRtags.entityBounds);
			put(iv4xrEntityVelocity, IV4XRtags.entityVelocity);
			put(iv4xrEntityType, IV4XRtags.entityType);
			put(iv4xrEntityIsActive, IV4XRtags.labRecruitsEntityIsActive);
			put(iv4xrEntityLastUpdated, IV4XRtags.labRecruitsEntityLastUpdated);

			// LabRecruits
			put(iv4xrLabRecruitsAgentHealth, IV4XRtags.labRecruitsAgentHealth);
			put(iv4xrLabRecruitsAgentScore, IV4XRtags.labRecruitsAgentScore);
			put(iv4xrLabRecruitsAgentMood, IV4XRtags.labRecruitsAgentMood);
		}
	};

	// a mapping from the state management tags to iv4XR tags
	private static Map<Tag<?>, Tag<?>> stateTagMappingSE = new HashMap<Tag<?>, Tag<?>>()
	{
		{
			// Space Engineers
			put(iv4xrSeBuildIntegrity, IV4XRtags.seBuildIntegrity);
			put(iv4xrSeIntegrity, IV4XRtags.seIntegrity);
			put(iv4xrSeMaxIntegrity, IV4XRtags.seMaxIntegrity);
			put(iv4xrSeMaxPosition, IV4XRtags.seMaxPosition);
			put(iv4xrSeMinPosition, IV4XRtags.seMinPosition);
			put(iv4xrSeOrientationForward, IV4XRtags.seOrientationForward);
			put(iv4xrSeOrientationUp, IV4XRtags.seOrientationUp);
			put(iv4xrSeSize, IV4XRtags.seSize);
		}
	};

	/**
	 * This method will return its equivalent, internal iv4XR tag, if available.
	 * @param mappedTag
	 * @return
	 */
	public static <T> Tag<T> getMappedStateTag(Tag<T> mappedTag) {
		return (Tag<T>) stateTagMappingIV4XR.getOrDefault(mappedTag, null);
	}

	public static <T> Tag<T> getMappedStateTagSE(Tag<T> mappedTag) {
		return (Tag<T>) stateTagMappingSE.getOrDefault(mappedTag, null);
	}
	
    // a mapping from the state management tags to iv4XR tags
    private static Map<Tag<?>, Tag<?>> actionTagMappingIV4XR = new HashMap<Tag<?>, Tag<?>>()
    {
    	{
    		put(ActionManagementTags.iv4xrActionOriginWidgetId, IV4XRtags.iv4xrActionOriginWidgetId);
    		put(ActionManagementTags.iv4xrActionOriginWidgetPath, IV4XRtags.iv4xrActionOriginWidgetPath);
    		put(ActionManagementTags.iv4xrActionOriginStateId, IV4XRtags.iv4xrActionOriginStateId);
    		put(ActionManagementTags.iv4xrActionEntityId, IV4XRtags.iv4xrActionEntityId);
    		put(ActionManagementTags.iv4xrActionEntityIsActive, IV4XRtags.iv4xrActionEntityIsActive);
    		put(ActionManagementTags.iv4xrActionOriginPos, IV4XRtags.iv4xrActionOriginPos);
    		put(ActionManagementTags.iv4xrActionTargetAbsPos, IV4XRtags.iv4xrActionTargetAbsPos);
    		put(ActionManagementTags.iv4xrActionTargetRelPos, IV4XRtags.iv4xrActionTargetRelPos);
    	}
    };

    public static Map<Tag<?>, Tag<?>> getActionTagMap() {
    	return actionTagMappingIV4XR;
    }
}

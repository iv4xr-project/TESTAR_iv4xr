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

package es.upv.staq.testar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.fruit.alayer.Tag;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import eu.iv4xr.framework.spatial.Vec3;

public class ActionManagementTags {

	public enum Group {iv4xrAction}

	// iv4xr
	public static final Tag<String> iv4xrActionOriginWidgetId = Tag.from("iv4xr Action Origin WidgetId", String.class);
	public static final Tag<String> iv4xrActionOriginWidgetPath = Tag.from("iv4xr Action Origin WidgetPath", String.class);
	public static final Tag<String> iv4xrActionOriginStateId = Tag.from("iv4xr Action Origin StateId", String.class);
	public static final Tag<String> iv4xrActionEntityId = Tag.from("iv4xr Action Entity Id", String.class);
	public static final Tag<Boolean> iv4xrActionEntityIsActive = Tag.from("iv4xr Action Entity IsActive", Boolean.class);
	public static final Tag<Vec3> iv4xrActionOriginPos = Tag.from("iv4xr Action Origin Pos", Vec3.class);
	public static final Tag<Vec3> iv4xrActionTargetAbsPos = Tag.from("iv4xr Action Target Abs Pos", Vec3.class);
	public static final Tag<Vec3> iv4xrActionTargetRelPos = Tag.from("iv4xr Action Target Rel Pos", Vec3.class);

	// a set containing the tags that are available for action management
	@SuppressWarnings("serial")
	private static Set<Tag<?>> actionManagementTags = new HashSet<Tag<?>>() {
		{
			//iv4XR
			add(iv4xrActionOriginWidgetId);
			add(iv4xrActionOriginWidgetPath);
			add(iv4xrActionOriginStateId);
			add(iv4xrActionEntityId);
			add(iv4xrActionEntityIsActive);
			add(iv4xrActionOriginPos);
			add(iv4xrActionTargetAbsPos);
			add(iv4xrActionTargetRelPos);
		}
	};

	/**
	 * Method will return true if a given tag is an available action management tag.
	 * @param tag
	 * @return
	 */
	public static boolean isActionManagementTag(Tag<?> tag) {
		return actionManagementTags.contains(tag);
	}

	// a bi-directional mapping from the action management tags to a string equivalent for use in the settings file
	private static BiMap<Tag<?>, String> settingsMap = HashBiMap.create(actionManagementTags.size());
	static { 
		//iv4XR
		settingsMap.put(iv4xrActionOriginWidgetId, "iv4xrActionOriginWidgetId");
		settingsMap.put(iv4xrActionOriginWidgetPath, "iv4xrActionOriginWidgetPath");
		settingsMap.put(iv4xrActionOriginStateId, "iv4xrActionOriginStateId");
		settingsMap.put(iv4xrActionEntityId, "iv4xrActionEntityId");
		settingsMap.put(iv4xrActionEntityIsActive, "iv4xrActionEntityIsActive");
		settingsMap.put(iv4xrActionOriginPos, "iv4xrActionOriginPos");
		settingsMap.put(iv4xrActionTargetAbsPos, "iv4xrActionTargetAbsPos");
		settingsMap.put(iv4xrActionTargetRelPos, "iv4xrActionTargetRelPos");
	}

	// a mapping of a tag to its group
	@SuppressWarnings("serial")
	private static Map<Tag<?>, Group> tagGroupMap = new HashMap<Tag<?>, Group>() {
		{
			//iv4xr
			put(iv4xrActionOriginWidgetId, Group.iv4xrAction);
			put(iv4xrActionOriginWidgetPath, Group.iv4xrAction);
			put(iv4xrActionOriginStateId, Group.iv4xrAction);
			put(iv4xrActionEntityId, Group.iv4xrAction);
			put(iv4xrActionEntityIsActive, Group.iv4xrAction);
			put(iv4xrActionOriginPos, Group.iv4xrAction);
			put(iv4xrActionTargetAbsPos, Group.iv4xrAction);
			put(iv4xrActionTargetRelPos, Group.iv4xrAction);
		}
	};

	/**
	 * This method will return the tag group for a given action management tag
	 * @param tag action management tag
	 * @return
	 */
	public static Group getTagGroup(Tag<?> tag) {
		return tagGroupMap.getOrDefault(tag, Group.iv4xrAction);
	}

	/**
	 * This method will return all the tags that are available for use in action management.
	 * @return
	 */
	public static Set<Tag<?>> getAllTags() {
		return actionManagementTags;
	}

	/**
	 * This method returns the action management tag belonging to a given settings string.
	 * @param settingsString
	 * @return
	 */
	public static Tag<?> getTagFromSettingsString(String settingsString) {
		return settingsMap.inverse().getOrDefault(settingsString, null);
	}

	/**
	 * This method returns the settings string for a given action management tag.
	 * @param tag
	 * @return
	 */
	public static String getSettingsStringFromTag(Tag<?> tag) {
		return settingsMap.getOrDefault(tag, null);
	}

}

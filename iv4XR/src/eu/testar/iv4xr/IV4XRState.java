/***************************************************************************************************
 *
 * Copyright (c) 2020 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 Open Universiteit - www.ou.nl
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

package eu.testar.iv4xr;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.fruit.Assert;
import org.fruit.Util;
import org.fruit.alayer.State;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.WidgetIterator;
import org.fruit.alayer.exceptions.NoSuchTagException;

import eu.testar.iv4xr.enums.IV4XRMapping;
import eu.testar.iv4xr.enums.IV4XRtags;

/**
 * The TESTAR State (root Widget) or in this case the representation of the iv4XR SUT State.
 */
public class IV4XRState extends IV4XRWidgetEntity implements State {

	private static final long serialVersionUID = -7173566268553326068L;

	public IV4XRState(IV4XRRootElement root) {
		super(null, null, root);
		this.root = this;
	}

	public Iterator<Widget> iterator() {
		Iterator<Widget> iterator = new WidgetIterator(this);
		// If root element is null, disable iterating
		if (this.element == null) {
			iterator.next();
		}
		return iterator;
	}

	public void remove(IV4XRWidgetEntity w) {
		Assert.isTrue(this != w, "You cannot remove the root!");
		assert (w.parent != null);
		w.parent.children.remove(w);
		invalidate(w);
	}

	public void invalidate(IV4XRWidgetEntity w) {
		if (w.element != null) {
			w.element.backRef = null;
		}
		w.root = null;
		for (IV4XRWidgetEntity c : w.children) {
			invalidate(c);
		}
	}

	public void setParent(IV4XRWidgetEntity w, Widget parent, int idx) {
		Assert.notNull(parent);
		Assert.isTrue(parent instanceof IV4XRWidgetEntity);
		Assert.isTrue(w != this, "You cannot set the root's parent!");
		assert (w.parent != null);

		IV4XRWidgetEntity webParent = (IV4XRWidgetEntity) parent;
		Assert.isTrue(webParent.root == this);
		Assert.isTrue(!Util.isAncestorOf(w, parent), "The parent is a descendent of this widget!");

		w.parent.children.remove(w);
		webParent.children.add(idx, w);
		w.parent = webParent;
	}

	IV4XRWidgetEntity addChild(IV4XRWidgetEntity parent, IV4XRElement element) {
		IV4XRWidgetEntity ret = new IV4XRWidgetEntity(this, parent, element);
		return ret;
	}

	void connect(IV4XRWidgetEntity parent, IV4XRWidgetEntity child) {
		parent.children.add(child);
	}

	public <T> T get(IV4XRWidgetEntity w, Tag<T> t) {
		T ret = get(w, t, null);
		if (ret == null) {
			throw new NoSuchTagException(t);
		}
		return ret;
	}

	/**
	 * Associate the IV4XRtags with the IV4XRElement properties
	 * 
	 * @param <T>
	 * @param w
	 * @param t
	 * @param defaultValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(IV4XRWidgetEntity w, Tag<T> t, T defaultValue) {

		Tag<T> stateManagementTag = IV4XRMapping.getMappedStateTag(t);
		if (stateManagementTag != null) {
			t = stateManagementTag;
		}

		Object ret = w.tags.get(t);

		if (ret != null) {
			return (T)ret;
		}

		else if (w.element == null || w.tags.containsKey(t)) {
			return defaultValue;
		}

		/**
		 * Generic TESTAR Tags
		 */
		if (t.equals(Tags.Desc)) {
			ret = w.element.entityType + " - " + w.element.entityId;
		}
		//else if (t.equals(Tags.Role)) {
		//ret = w.element.entityType;
		//}
		else if (t.equals(Tags.Shape)) {
			ret = w.element.rect;
		}
		else if (t.equals(Tags.Blocked)) {
			ret = w.element.blocked;
		}
		else if (t.equals(Tags.Enabled)) {
			ret = w.element.enabled;
		}
		else if (t.equals(Tags.Title)) {
			ret = w.element.entityId;
		}
		else if (t.equals(Tags.PID)) {
			ret = w == this ? ((IV4XRRootElement) element).pid : null;
		}
		else if (t.equals(Tags.IsRunning)) {
			ret = w == this ? ((IV4XRRootElement) element).isRunning : null;
		}
		else if (t.equals(Tags.TimeStamp)) {
			ret = w == this ? ((IV4XRRootElement) element).timeStamp : null;
		}
		else if (t.equals(Tags.Foreground)) {
			ret = w == this ? ((IV4XRRootElement) element).isForeground : null;
		}
		else if (t.equals(Tags.ZIndex)) {
			ret = w.element.zindex;
		}
		else if (t.equals(Tags.HWND)) {
			// use the windows handle from root (state)
			ret = w.element.root.windowsHandle;
		}
		/**
		 * Generic IV4XR Tags
		 */
		else if (t.equals(IV4XRtags.entityEnabled)) {
			ret = w.element.enabled;
		}
		else if (t.equals(IV4XRtags.entityBlocked)) {
			ret = w.element.blocked;
		}
		else if (t.equals(IV4XRtags.agentPosition)) {
			ret = w.element.agentPosition;
		}
		else if (t.equals(IV4XRtags.entityPosition)) {
			ret = w.element.entityPosition;
		}
		else if (t.equals(IV4XRtags.entityPositionRepresentation)) {
			ret = w.element.entityPosition.toString();
		}
		else if (t.equals(IV4XRtags.entityBounds)) {
			ret = w.element.entityBounds;
		}
		else if (t.equals(IV4XRtags.entityVelocity)) {
			ret = w.element.entityVelocity;
		}
		else if (t.equals(IV4XRtags.entityDynamic)) {
			ret = w.element.entityDynamic;
		}
		else if (t.equals(IV4XRtags.entityId)) {
			ret = w.element.entityId;
		}
		else if (t.equals(IV4XRtags.entityType)) {
			ret = w.element.entityType;
		}
		else if (t.equals(IV4XRtags.entityTimestamp)) {
			ret = w.element.entityTimestamp;
		}
		/**
		 *  Specific iv4xr System Tags (LabRecruits)
		 */
		else if (t.equals(IV4XRtags.labRecruitsEntityIsActive)) {
			ret = w.element.labRecruitsEntityIsActive;
		}
		else if (t.equals(IV4XRtags.labRecruitsEntityLastUpdated)) {
			ret = w.element.labRecruitsEntityLastUpdated;
		}
		else if (t.equals(IV4XRtags.labRecruitsAgentHealth)) {
			ret = w.element.labRecruitsAgentHealth;
		}
		else if (t.equals(IV4XRtags.labRecruitsAgentScore)) {
			ret = w.element.labRecruitsAgentScore;
		}
		else if (t.equals(IV4XRtags.labRecruitsAgentMood)) {
			ret = w.element.labRecruitsAgentMood;
		}
		/**
		 * Specific iv4xr System Tags (SpaceEngineers)
		 */
		else if (t.equals(IV4XRtags.seAgentPosition)) {
			ret = w.element.seAgentPosition;
		}
		else if (t.equals(IV4XRtags.seAgentOrientationForward)) {
			ret = w.element.seAgentOrientationForward;
		}
		else if (t.equals(IV4XRtags.seAgentOrientationUp)) {
			ret = w.element.seAgentOrientationUp;
		}
		else if (t.equals(IV4XRtags.seAgentHealth)) {
			ret = w.element.seAgentHealth;
		}
		else if (t.equals(IV4XRtags.seBuildIntegrity)) {
			ret = w.element.seBuildIntegrity;
		}
		else if (t.equals(IV4XRtags.seIntegrity)) {
			ret = w.element.seIntegrity;
		}
		else if (t.equals(IV4XRtags.seMaxIntegrity)) {
			ret = w.element.seMaxIntegrity;
		}
		else if (t.equals(IV4XRtags.seMaxPosition)) {
			ret = w.element.seMaxPosition;
		}
		else if (t.equals(IV4XRtags.seMinPosition)) {
			ret = w.element.seMinPosition;
		}
		else if (t.equals(IV4XRtags.seOrientationForward)) {
			ret = w.element.seOrientationForward;
		}
		else if (t.equals(IV4XRtags.seOrientationUp)) {
			ret = w.element.seOrientationUp;
		}
		else if (t.equals(IV4XRtags.seSize)) {
			ret = w.element.seSize;
		}

		cacheTag(w, t, ret);

		return (ret == null) ? defaultValue : (T) ret;
	}

	@SuppressWarnings("unchecked")
	public <T> T cacheTag(IV4XRWidgetEntity w, Tag<T> t, Object value) {
		w.tags.put(t, value);
		return (T) value;
	}

	public <T> void setTag(IV4XRWidgetEntity w, Tag<T> t, T value) {
		Assert.notNull(value);
		w.tags.put(t, value);
	}

	public <T> void remove(IV4XRWidgetEntity w, Tag<T> t) {
		Assert.notNull(w, t);
		w.tags.put(t, null);
	}

	public IV4XRWidgetEntity getChild(IV4XRWidgetEntity w, int idx) {
		return w.children.get(idx);
	}

	public int childCount(IV4XRWidgetEntity w) {
		return w.children.size();
	}

	public IV4XRWidgetEntity getParent(IV4XRWidgetEntity w) {
		return w.parent;
	}

	Iterable<Tag<?>> tags(final IV4XRWidgetEntity w) {
		Assert.notNull(w);

		// compile a query set
		final Set<Tag<?>> queryTags = new HashSet<Tag<?>>();
		queryTags.addAll(tags.keySet());
		queryTags.addAll(Tags.tagSet());
		queryTags.addAll(IV4XRtags.tagSet());

		Iterable<Tag<?>> ret = new Iterable<Tag<?>>() {
			public Iterator<Tag<?>> iterator() {
				return new Iterator<Tag<?>>() {
					Iterator<Tag<?>> i = queryTags.iterator();
					IV4XRWidgetEntity target = w;
					Tag<?> next;

					private Tag<?> fetchNext() {
						if (next == null) {
							while (i.hasNext()) {
								next = i.next();
								if (target.get(next, null) != null) {
									return next;
								}
							}
							next = null;
						}
						return next;
					}

					public boolean hasNext() {
						return fetchNext() != null;
					}

					public Tag<?> next() {
						Tag<?> ret = fetchNext();
						if (ret == null) {
							throw new NoSuchElementException();
						}
						next = null;
						return ret;
					}

					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
		};
		return ret;
	}

	public String toString() {
		return Util.treeDesc(this, 2, Tags.Role, Tags.Title);
	}
}

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

public class IV4XRState extends IV4XREntity implements State {

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

	public void remove(IV4XREntity w) {
		Assert.isTrue(this != w, "You cannot remove the root!");
		assert (w.parent != null);
		w.parent.children.remove(w);
		invalidate(w);
	}

	public void invalidate(IV4XREntity w) {
		if (w.element != null) {
			w.element.backRef = null;
		}
		w.root = null;
		for (IV4XREntity c : w.children) {
			invalidate(c);
		}
	}

	public void setParent(IV4XREntity w, Widget parent, int idx) {
		Assert.notNull(parent);
		Assert.isTrue(parent instanceof IV4XREntity);
		Assert.isTrue(w != this, "You cannot set the root's parent!");
		assert (w.parent != null);

		IV4XREntity webParent = (IV4XREntity) parent;
		Assert.isTrue(webParent.root == this);
		Assert.isTrue(!Util.isAncestorOf(w, parent), "The parent is a descendent of this widget!");

		w.parent.children.remove(w);
		webParent.children.add(idx, w);
		w.parent = webParent;
	}

	IV4XREntity addChild(IV4XREntity parent, IV4XRElement element) {
		IV4XREntity ret = new IV4XREntity(this, parent, element);
		return ret;
	}

	void connect(IV4XREntity parent, IV4XREntity child) {
		parent.children.add(child);
	}

	public <T> T get(IV4XREntity w, Tag<T> t) {
		T ret = get(w, t, null);
		if (ret == null) {
			throw new NoSuchTagException(t);
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(IV4XREntity w, Tag<T> t, T defaultValue) {

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
		 * Generic SUT Tags
		 */
		if (t.equals(Tags.Desc)) {
			ret = "This is the entity: " + w.element.entityId;
		}
		/*else if (t.equals(Tags.Role)) {
			ret = w.element.entityType.toString();
		}*/
		/*else if (t.equals(Tags.HitTester)) {
			ret = new IV4XRHitTester(w.element);
		}*/
		else if (t.equals(Tags.Shape)) {
			ret = w.element.rect;
		}
		else if (t.equals(Tags.Blocked)) {
			ret = w.element.blocked;
		}
		else if (t.equals(Tags.Enabled)) {
			//ret = w.element.enabled;
			ret = w.element.isEntityActive;
		}
		else if (t.equals(Tags.Title)) {
			ret = w.element.entityId; //TODO: check if fits with title
		}
		else if (t.equals(Tags.ValuePattern)) {
			ret = w.element.entityTag; //TODO: check if fits with ValuePattern
		}
		else if (t.equals(Tags.Path)) {
			ret = w.element.entityPosition.toString();
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
		/**
		 * Specific IV4XR Tags
		 */
		else if (t.equals(IV4XRtags.entityEnabled)) {
			ret = w.element.enabled;
		}
		else if (t.equals(IV4XRtags.entityBlocked)) {
			ret = w.element.blocked;
		}
		else if (t.equals(IV4XRtags.entityIsActive)) {
			ret = w.element.isEntityActive;
		}
		else if (t.equals(IV4XRtags.entityId)) {
			ret = w.element.entityId;
		}
		else if (t.equals(IV4XRtags.entityTag)) {
			ret = w.element.entityTag;
		}
		else if (t.equals(IV4XRtags.entityProperty)) {
			ret = w.element.entityProperty;
		}
		else if (t.equals(IV4XRtags.entityType)) {
			ret = w.element.entityType;
		}
		else if (t.equals(IV4XRtags.entityPosition)) {
			ret = w.element.entityPosition;
		}
		else if (t.equals(IV4XRtags.entityPositionRepresentation)) {
			ret = w.element.entityPosition.toString();
		}

		cacheTag(w, t, ret);

		return (ret == null) ? defaultValue : (T) ret;
	}

	@SuppressWarnings("unchecked")
	public <T> T cacheTag(IV4XREntity w, Tag<T> t, Object value) {
		w.tags.put(t, value);
		return (T) value;
	}

	public <T> void setTag(IV4XREntity w, Tag<T> t, T value) {
		Assert.notNull(value);
		w.tags.put(t, value);
	}

	public <T> void remove(IV4XREntity w, Tag<T> t) {
		Assert.notNull(w, t);
		w.tags.put(t, null);
	}

	public IV4XREntity getChild(IV4XREntity w, int idx) {
		return w.children.get(idx);
	}

	public int childCount(IV4XREntity w) {
		return w.children.size();
	}

	public IV4XREntity getParent(IV4XREntity w) {
		return w.parent;
	}

	Iterable<Tag<?>> tags(final IV4XREntity w) {
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
					IV4XREntity target = w;
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

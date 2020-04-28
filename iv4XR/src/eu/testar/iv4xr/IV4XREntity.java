package eu.testar.iv4xr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fruit.Drag;
import org.fruit.Util;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Widget;

public class IV4XREntity implements Widget, Serializable {

	private static final long serialVersionUID = 3814220462949112503L;

	IV4XRState root;
	IV4XREntity parent;
	Map<Tag<?>, Object> tags = new HashMap<>();
	List<IV4XREntity> children = new ArrayList<>();
	public IV4XRElement element;

	protected IV4XREntity(IV4XRState root, IV4XREntity parent, IV4XRElement element) {
		this.parent = parent;
		this.element = element;
		this.root = root;

		if (parent != null) {
			root.connect(parent, this);
		}
	}

	final public void moveTo(Widget p, int idx) {
		root.setParent(this, p, idx);
	}

	public final IV4XREntity addChild() {
		return root.addChild(this, null);
	}

	public final IV4XRState root() {
		return root;
	}

	public final IV4XREntity parent() {
		return root.getParent(this);
	}

	public final IV4XREntity child(int i) {
		return root.getChild(this, i);
	}

	public final void remove() {
		root.remove(this);
	}

	public final int childCount() {
		return root.childCount(this);
	}

	public final <T> T get(Tag<T> tag) {
		return root.get(this, tag);
	}

	public final <T> void set(Tag<T> tag, T value) {
		root.setTag(this, tag, value);
	}

	public final <T> T get(Tag<T> tag, T defaultValue) {
		return root.get(this, tag, defaultValue);
	}

	public final Iterable<Tag<?>> tags() {
		return root.tags(this);
	}

	public final void remove(Tag<?> tag) {
		root.remove(this, tag);
	}

	public String getRepresentation(String tab) {
		return "COMPLETE: IV4XREntity getRepresentation";
	}

	@Override
	public String toString(Tag<?>... tags) {
		return Util.treeDesc(this, 2, tags);
	}

	@Override
	public Drag[] scrollDrags(double scrollArrowSize, double scrollThick) {
		// TODO Auto-generated method stub
		return null;
	}
}

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fruit.Drag;
import org.fruit.Util;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Widget;

/**
 * A TESTAR Widget or in this case the representation of an iv4XR Entity with his properties.
 */
public class IV4XRWidgetEntity implements Widget, Serializable {

	private static final long serialVersionUID = 3814220462949112503L;

	IV4XRState root;
	IV4XRWidgetEntity parent;
	public Map<Tag<?>, Object> tags = new HashMap<>();
	List<IV4XRWidgetEntity> children = new ArrayList<>();
	public IV4XRElement element;

	protected IV4XRWidgetEntity(IV4XRState root, IV4XRWidgetEntity parent, IV4XRElement element) {
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

	public final IV4XRWidgetEntity addChild() {
		return root.addChild(this, null);
	}

	public final IV4XRState root() {
		return root;
	}

	public final IV4XRWidgetEntity parent() {
		return root.getParent(this);
	}

	public final IV4XRWidgetEntity child(int i) {
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

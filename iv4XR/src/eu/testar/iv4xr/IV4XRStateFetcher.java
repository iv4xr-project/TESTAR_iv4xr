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

package eu.testar.iv4xr;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.fruit.alayer.Rect;
import org.fruit.alayer.Roles;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Tags;
import org.fruit.alayer.exceptions.StateBuildException;
import org.fruit.alayer.windows.Windows;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.testar.iv4xr.enums.IV4XRtags;

/**
 * State Fetcher object extracts the information from the iv4XR Environment to create the Widget Tree
 * 
 * IV4XRElement Node Map will be created with the Environment Entities properties
 * then the Widget Tree will be inferred
 */
public class IV4XRStateFetcher implements Callable<IV4XRState> {

	protected final SUT system;
	
	// Default agent id
	public static Set<String> agentsIds = new HashSet<>(Arrays.asList("agent1"));

	public IV4XRStateFetcher(SUT system) {
		this.system = system;
	}

	public static IV4XRRootElement buildRoot(SUT system) throws StateBuildException {
		IV4XRRootElement iv4XRroot = new IV4XRRootElement();
		iv4XRroot.isRunning = system.isRunning();
		iv4XRroot.timeStamp = System.currentTimeMillis();
		iv4XRroot.pid = system.get(Tags.PID);
		iv4XRroot.isForeground = (system.get(IV4XRtags.windowsProcess,null) != null) ? system.get(IV4XRtags.windowsProcess).isForeground() : false;

		return iv4XRroot;
	}

	@Override
	public IV4XRState call() throws Exception {
	    IV4XRRootElement rootElement = buildVirtualEnvironment(system);

	    if (rootElement == null) {
	      system.set(Tags.Desc, " ");
	      return new IV4XRState(null);
	    }

	    system.set(Tags.Desc, "iv4XR system IV4XRStateFetcher Desc");

	    IV4XRState root = createWidgetTree(rootElement);
	    root.set(Tags.Role, Roles.Process);
	    root.set(Tags.NotResponding, false);

	    return root;
	}
	
	protected IV4XRRootElement buildVirtualEnvironment(SUT system) {
		IV4XRRootElement rootElement = buildRoot(system);
		
		if(!rootElement.isRunning) {
			return rootElement;
		}
		
		rootElement.pid = system.get(Tags.PID, (long)-1);
		
		for(long windowHandle : getVisibleTopLevelWindowHandles()) {
			if(rootElement.pid == Windows.GetWindowProcessId(windowHandle)) {
				rootElement.windowsHandle = windowHandle;
				system.set(Tags.HWND, windowHandle);
				rootElement.set(Tags.HWND, windowHandle);
			}
		}
		
		return fetchIV4XRElements(rootElement);
	}
	
	/**
	 * Observation = Objects Agent can observe - Dynamically appearing and disappearing WOM. 
	 * WorldModel = All Objects from WOM with their properties. 
	 * 
	 * @param rootElement
	 * @return
	 */
	protected IV4XRRootElement fetchIV4XRElements(IV4XRRootElement rootElement) {	    
		for(String agentId : agentsIds) {
			WorldModel observedWOM = system.get(IV4XRtags.iv4xrW3DEnvironment).observe(agentId);

			if(rootElement.isForeground) {
				// Add manually the Agent as an Element (Observed Entities + 1)
				rootElement.children = new ArrayList<IV4XRElement>((int) observedWOM.elements.size() + 1);

				rootElement.zindex = 0;
				fillRect(rootElement);

				IV4XRagent(rootElement, observedWOM);

				// If Agent observes entities create the elements-entities
				if(observedWOM.elements.size() > 0) {
					for(WorldEntity entity : observedWOM.elements.values()) {
						IV4XRdescend(rootElement, observedWOM.getElement(entity.id));
					}
				}
			}
		}

		return rootElement;
	}

	protected IV4XRElement IV4XRagent(IV4XRElement parent, WorldModel wom) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true;
		childElement.blocked = false;
		childElement.zindex = parent.zindex +1;

		childElement.entityPosition = wom.position;
		childElement.entityBounds = wom.extent;
		childElement.entityVelocity = wom.velocity;
		childElement.entityId = wom.agentId;
		childElement.entityType = "AGENT";
		childElement.entityTimestamp = -1;

		fillRect(childElement);

		return childElement;
	}

	protected IV4XRElement IV4XRdescend(IV4XRElement parent, WorldEntity womEntity) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);

		childElement.enabled = true;
		childElement.blocked = false;
		childElement.zindex = parent.zindex +1;

		childElement.entityPosition = womEntity.position;
		childElement.entityBounds = womEntity.extent;
		childElement.entityVelocity = womEntity.velocity;
		childElement.entityDynamic = womEntity.dynamic;
		childElement.entityId = womEntity.id;
		childElement.entityType = womEntity.type;
		childElement.entityTimestamp = womEntity.timestamp;

		fillRect(childElement);

		return childElement;
	}

	protected IV4XRState createWidgetTree(IV4XRRootElement root) {
		IV4XRState state = new IV4XRState(root);
		root.backRef = state;
		for (IV4XRElement childElement : root.children) {
			createWidgetTree(state, childElement);
		}
		return state;
	}

	protected void createWidgetTree(IV4XRWidgetEntity parent, IV4XRElement element) {
		IV4XRWidgetEntity w = parent.root().addChild(parent, element);
		element.backRef = w;
		
		for (IV4XRElement child : element.children) {
			createWidgetTree(w, child);
		}
	}

	/* lists all visible top level windows in ascending z-order (foreground window last) */
	protected Iterable<Long> getVisibleTopLevelWindowHandles(){
		Deque<Long> ret = new ArrayDeque<Long>();
		long windowHandle = Windows.GetWindow(Windows.GetDesktopWindow(), Windows.GW_CHILD);

		while(windowHandle != 0){
			if(Windows.IsWindowVisible(windowHandle)){
				long exStyle = Windows.GetWindowLong(windowHandle, Windows.GWL_EXSTYLE);
				if((exStyle & Windows.WS_EX_TRANSPARENT) == 0 && (exStyle & Windows.WS_EX_NOACTIVATE) == 0){
					ret.addFirst(windowHandle);
				}				
			}
			windowHandle = Windows.GetNextWindow(windowHandle, Windows.GW_HWNDNEXT);
		}

		System.clearProperty("DEBUG_WINDOWS_PROCESS_NAMES");

		return ret;
	}

	/**
	 * Get Screen Size Rect by default, if we are able to obtain the SUT windows screen overwrite it
	 * 
	 * @param element
	 */
	protected void fillRect(IV4XRElement element) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle screenRectangle = new Rectangle(screenSize);
		Rect rect = Rect.from(screenRectangle.getX(), screenRectangle.getY(), screenRectangle.getWidth(), screenRectangle.getHeight());
		
		long r[] = Windows.GetWindowRect(element.root.windowsHandle);
		if(r[2] - r[0] >= 0 && r[3] - r[1] >= 0) {
			rect = Rect.fromCoordinates(r[0], r[1], r[2], r[3]);
		}

		element.rect = rect;
	}
}

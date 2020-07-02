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

import communication.agent.AgentCommand;
import communication.system.Request;
import eu.testar.iv4xr.enums.IV4XRtags;
import helperclasses.datastructures.Vec3;
import world.LegacyDynamicEntity;
import world.LegacyEntity;
import world.LegacyEntityType;
import world.LegacyInteractiveEntity;
import world.LegacyObservation;

/**
 * State Fetcher object extracts the information from the iv4XR Environment to create the Widget Tree
 * 
 * IV4XRElement Node Map will be created with the Environment Entities properties
 * then the Widget Tree will be inferred
 */
public class IV4XRStateFetcher implements Callable<IV4XRState> {

	private final SUT system;
	
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

	    system.set(Tags.Desc, "labRecruit system IV4XRStateFetcher Desc");

	    IV4XRState root = createWidgetTree(rootElement);
	    root.set(Tags.Role, Roles.Process);
	    root.set(Tags.NotResponding, false);

	    return root;
	}
	
	private IV4XRRootElement buildVirtualEnvironment(SUT system) {
		IV4XRRootElement rootElement = buildRoot(system);
		
		if(!rootElement.isRunning)
			return rootElement;
		
		rootElement.pid = system.get(Tags.PID, (long)-1);
		
		for(long windowHandle : getVisibleTopLevelWindowHandles()) {
			if(rootElement.pid == Windows.GetWindowProcessId(windowHandle)) {
				rootElement.windowsHandle = windowHandle;
				system.set(Tags.HWND, windowHandle);
			}
		}
		
		//WorldModel WOM = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).observe(agentId);
		//LabWorldModel labWOM = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).observe(agentId);
	    //system.set(IV4XRtags.iv4xrLabWorldModel, labWOM);
	    
		for(String agentId : agentsIds) {
			// TODO: Difference between WOM and LegacyStuff ? How are these things connected each other ?
			LegacyObservation observation = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).getResponse(Request.command(AgentCommand.doNothing(agentId)));

			if(rootElement.isForeground && observation.entities.size() > 0) {

				// Add manually the Agent as an Entity
				// TODO: Change Implementation in the future
				rootElement.children = new ArrayList<IV4XRElement>((int) observation.entities.size() + 1);

				rootElement.zindex = 0;
				fillRect(rootElement);

				IV4XRagent(rootElement, observation);

				for(int i = 0; i < observation.entities.size(); i++) {
					IV4XRdescend(rootElement, observation.entities.get(i));
				}
			}
		}
	    
	    return rootElement;
	}
	
	private IV4XRElement IV4XRagent(IV4XRElement parent, LegacyObservation observer) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);
		
		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;
		
		childElement.entityPosition = observer.agentPosition;
		//childElement.entityBounds = observer.extent; //TODO: Do the Agents have bounds ?
		childElement.entityVelocity = observer.velocity;
		childElement.entityId = observer.agentID;
		childElement.entityType = LegacyEntityType.Entity; //TODO: check proper entity for agent
		childElement.entityTimestamp = -1;
		
		childElement.labRecruitsEntityIsActive = true; //TODO: check if agent will have this property
		childElement.labRecruitsEntityType = "AGENT"; //TODO: check proper entity for agent
		childElement.labRecruitsEntityTag = "Agent"; //TODO: check
		childElement.labRecruitsEntityProperty = "Agent"; //TODO: check
		childElement.labRecruitsEntityLastUpdated = -1;

		fillRect(childElement);
		
		return childElement;
	}
	
	private IV4XRElement IV4XRdescend(IV4XRElement parent, LegacyEntity lentity) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);
		
		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;
		
		childElement.entityPosition = lentity.position;
		childElement.entityBounds = (lentity instanceof LegacyInteractiveEntity) ? ((LegacyInteractiveEntity) lentity).center : new Vec3(0,0,0); //TODO: Check if this property is the correct one
		childElement.entityVelocity = (lentity instanceof LegacyDynamicEntity) ? ((LegacyDynamicEntity) lentity).velocity : new Vec3(0,0,0);
		childElement.entityId = lentity.id;
		childElement.entityType = lentity.type;
		childElement.entityTimestamp = lentity.lastUpdated; //TODO: Verify if these props are the same
		
		childElement.labRecruitsEntityIsActive = (lentity instanceof LegacyInteractiveEntity) ? ((LegacyInteractiveEntity) lentity).isActive : false;
		childElement.labRecruitsEntityType = lentity.tag; //TODO: Verify if tag are the (door, switch, colorStuff)
		childElement.labRecruitsEntityTag = lentity.tag;
		childElement.labRecruitsEntityProperty = lentity.property;
		childElement.labRecruitsEntityLastUpdated = lentity.lastUpdated;
		
		fillRect(childElement);
		
		return childElement;
	}

	private IV4XRState createWidgetTree(IV4XRRootElement root) {
		IV4XRState state = new IV4XRState(root);
		root.backRef = state;
		for (IV4XRElement childElement : root.children) {
			createWidgetTree(state, childElement);
		}
		return state;
	}

	private void createWidgetTree(IV4XRWidgetEntity parent, IV4XRElement element) {
		IV4XRWidgetEntity w = parent.root().addChild(parent, element);
		element.backRef = w;
		
		for (IV4XRElement child : element.children) {
			createWidgetTree(w, child);
		}
	}

	/* lists all visible top level windows in ascending z-order (foreground window last) */
	private Iterable<Long> getVisibleTopLevelWindowHandles(){
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
	private void fillRect(IV4XRElement element) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle screenRectangle = new Rectangle(screenSize);
		Rect rect = Rect.from(screenRectangle.getX(),  screenRectangle.getY(), screenRectangle.getWidth(), screenRectangle.getHeight());
		
		long r[] = Windows.GetWindowRect(element.root.windowsHandle);
		if(r[2] - r[0] >= 0 && r[3] - r[1] >= 0) {
			rect = Rect.fromCoordinates(r[0], r[1], r[2], r[3]);
		}

		element.rect = rect;
	}
}

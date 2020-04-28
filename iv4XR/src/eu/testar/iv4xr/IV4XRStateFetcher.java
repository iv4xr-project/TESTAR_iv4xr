package eu.testar.iv4xr;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.concurrent.Callable;

import org.fruit.Util;
import org.fruit.alayer.Rect;
import org.fruit.alayer.Roles;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.StateBuildException;
import org.fruit.alayer.windows.Windows;

import communication.agent.AgentCommand;
import communication.system.Request;
import eu.testar.iv4xr.enums.IV4XRtags;

public class IV4XRStateFetcher implements Callable<IV4XRState> {

	private final SUT system;
	
	public static world.Observation worldObservation;
	
	//TODO: Setting conf
	private static String agentId = "agent1";

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

	    /*for (Widget w : root) {
	      w.set(Tags.Path, Util.indexString(w));
	    }*/

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
		
	    worldObservation = system.get(IV4XRtags.iv4xrSocketEnvironment).getResponse(Request.command(AgentCommand.doNothing(agentId)));
	    system.set(IV4XRtags.worldObservation, worldObservation);
	    
	    if(rootElement.isForeground && worldObservation.entities.size() > 0) {
	    	rootElement.children = new ArrayList<IV4XRElement>((int)worldObservation.entities.size() + 1);
	    	
	    	rootElement.zindex = 0;
	    	fillRect(rootElement);
	    	
	    	IV4XRagent(rootElement, worldObservation);
	    	
	    	for(int i = 0; i<worldObservation.entities.size(); i++) {
	    		IV4XRdescend(rootElement, worldObservation.entities.get(i));
	    	}
	    }
	    
	    return rootElement;
	}
	
	private IV4XRElement IV4XRagent(IV4XRElement parent, world.Observation observer) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);
		
		childElement.ignore = false; //TODO: Why this?
		
		childElement.entityId = observer.agentID;
		childElement.entityTag = "Agent"; //TODO: check
		childElement.entityProperty = "Agent"; //TODO: check
		childElement.entityType = world.EntityType.Entity; //TODO: check proper entity for agent
		childElement.entityPosition = observer.agentPosition;
		childElement.isEntityActive = true; //TODO: check if agent will have this property
		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;
		
		fillRect(childElement);
		
		return childElement;
	}
	
	private IV4XRElement IV4XRdescend(IV4XRElement parent, world.Entity entity) {
		IV4XRElement childElement = new IV4XRElement(parent);
		parent.children.add(childElement);
		
		childElement.ignore = false; //TODO: Why this?
		
		childElement.entityId = entity.id;
		childElement.entityTag = entity.tag;
		childElement.entityProperty = entity.property;
		childElement.entityType = entity.type;
		childElement.entityPosition = entity.position;
		childElement.isEntityActive = ((world.InteractiveEntity) entity).isActive;
		childElement.enabled = true; //TODO: check when should be enabled (careful with createWidgetTree)
		childElement.blocked = false; //TODO: check when should be blocked (agent vision?)
		childElement.zindex = parent.zindex +1;
		
		fillRect(childElement);
		
		return childElement;
	}

	private IV4XRState createWidgetTree(IV4XRRootElement root) {
		IV4XRState state = new IV4XRState(root);
		root.backRef = state;
		for (IV4XRElement childElement : root.children) {
			if (!childElement.ignore) { //TODO: why this ignore?
				createWidgetTree(state, childElement);
			}
		}
		return state;
	}

	private void createWidgetTree(IV4XREntity parent, IV4XRElement element) {
		if (!element.enabled) { //TODO: Remove and use Tag property?
			return;
		}

		IV4XREntity w = parent.root().addChild(parent, element);
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

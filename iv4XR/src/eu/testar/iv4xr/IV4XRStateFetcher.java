package eu.testar.iv4xr;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import org.fruit.Util;
import org.fruit.alayer.Rect;
import org.fruit.alayer.Roles;
import org.fruit.alayer.SUT;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.StateBuildException;

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
		iv4XRroot.isForeground = true; //TODO: implement with win process

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

	    for (Widget w : root) {
	      w.set(Tags.Path, Util.indexString(w));
	    }

	    return root;
	}
	
	private IV4XRRootElement buildVirtualEnvironment(SUT system) {
		IV4XRRootElement rootElement = buildRoot(system);
		
		if(!rootElement.isRunning)
			return rootElement;
		
		rootElement.pid = system.get(Tags.PID, (long)-1);
		
	    worldObservation = system.get(IV4XRtags.iv4xrSocketEnvironment).getResponse(Request.command(AgentCommand.doNothing(agentId)));
	    system.set(IV4XRtags.worldObservation, worldObservation);
	    
	    /**
	     * TODO: Entities will have hierarchy structure?
	     */
	    /*SortedSet<world.Entity> worldEntities = new TreeSet<>();
	    for(world.Entity ent : worldObservation.entities) {
	    	worldEntities.add(ent);
	    }*/
	    
	    if(rootElement.isForeground && worldObservation.entities.size() > 0) {
	    	rootElement.children = new ArrayList<IV4XRElement>((int)worldObservation.entities.size());
	    	
	    	rootElement.zindex = 0;
	    	fillRect(rootElement);
	    	
	    	for(int i = 0; i<worldObservation.entities.size(); i++) {
	    		IV4XRdescend(rootElement, worldObservation.entities.get(i));
	    	}
	    }
	    
	    return rootElement;
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


	//TODO: Implement Windows process and use windows hwnd size
	private void fillRect(IV4XRElement element) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle screenRectangle = new Rectangle(screenSize);
		element.rect = Rect.from(screenRectangle.getX(),  screenRectangle.getY(), screenRectangle.getWidth(), screenRectangle.getHeight());
	}
}

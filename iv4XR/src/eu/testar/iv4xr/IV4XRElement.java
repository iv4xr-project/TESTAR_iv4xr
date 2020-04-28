package eu.testar.iv4xr;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.fruit.alayer.Rect;
import org.fruit.alayer.TaggableBase;

import helperclasses.datastructures.Vec3;

public class IV4XRElement extends TaggableBase implements Serializable {

	private static final long serialVersionUID = -4404777362271754899L;

	List<IV4XRElement> children = new ArrayList<>();
	IV4XRElement parent;
	IV4XRRootElement root;
	IV4XREntity backRef;

	boolean enabled, ignore;
	boolean blocked;

	double zindex;

	//TODO: Check these default values needed for State Model (State does not exist as widget with properties, should be Windows hwnd?)
	String entityId = "";
	String entityTag = "";
	String entityProperty = "";
	world.EntityType entityType = world.EntityType.Entity;
	Vec3 entityPosition = new Vec3(0, 0, 0);
	boolean isEntityActive = true;
	
	Rect rect;

	public IV4XRElement(){ this(null); }

	public IV4XRElement(IV4XRElement parent){
		this.parent = parent;
		if(parent != null)
			root = parent.root;
		enabled = true;
	}

	private void writeObject(ObjectOutputStream oos) throws IOException{
		oos.defaultWriteObject();
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException{
		ois.defaultReadObject();
	}
}

package eu.testar.iv4xr.enums;

import org.fruit.alayer.Tag;
import org.fruit.alayer.TagsBase;
import org.fruit.alayer.windows.WinProcess;

import eu.testar.iv4xr.SocketEnvironment;
import helperclasses.datastructures.Vec3;
import world.EntityType;

public class IV4XRtags extends TagsBase {
	
	private IV4XRtags() {}
	
	public static final Tag<SocketEnvironment> iv4xrSocketEnvironment = from("iv4xrSocketEnvironment", SocketEnvironment.class);
	
	public static final Tag<world.Observation> worldObservation = from("worldObservation", world.Observation.class);
	
	public static final Tag<Boolean> labRecruitsForeground = from("labRecruitsForeground", Boolean.class);
	
	public static final Tag<WinProcess> windowsProcess = from("windowsProcess", WinProcess.class);

	public static final Tag<Boolean> entityEnabled = from("entityEnabled", Boolean.class);
	
	public static final Tag<Boolean> entityBlocked = from("entityBlocked", Boolean.class);
	
	public static final Tag<Boolean> entityIsActive = from("entityIsActive", Boolean.class);
	
	public static final Tag<String> entityId = from("entityId", String.class);
	
	public static final Tag<String> entityTag = from("entityTag", String.class);
	
	public static final Tag<String> entityProperty = from("entityProperty", String.class);
	
	/**
	 * Rol
	 */
	public static final Tag<EntityType> entityType = from("entityType", EntityType.class);
	
	public static final Tag<Vec3> entityPosition = from("entityPosition", Vec3.class);
	
	public static final Tag<String> entityPositionRepresentation = from("entityPositionRepresentation", String.class);
	
}

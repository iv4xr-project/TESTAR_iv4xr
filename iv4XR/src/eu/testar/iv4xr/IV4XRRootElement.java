package eu.testar.iv4xr;

import pathfinding.Pathfinder;

public class IV4XRRootElement extends IV4XRElement {

	private static final long serialVersionUID = -7438928787933117761L;

	public long pid;
	public long timeStamp;
	public boolean isRunning;
	public boolean isForeground;
	
	public Pathfinder pathFinder;

	public IV4XRRootElement() {
		super(null);
		root = this;
		parent = this;
		//isForeground = system.get(IV4XRtags.labRecruitsForeground, false);
		isForeground = false;
		blocked = false;
	}

}

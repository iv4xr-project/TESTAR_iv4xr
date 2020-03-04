package org.testar.iv4XR;

import org.fruit.alayer.Action;
import org.fruit.alayer.Role;
import org.fruit.alayer.Roles;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.TaggableBase;
import org.fruit.alayer.Tags;
import org.fruit.alayer.exceptions.ActionFailedException;

import communication.agent.AgentCommand;
import communication.system.Request;

public class labActionInteract extends TaggableBase implements Action {
	private static final long serialVersionUID = -2401401952551344201L;
	
	private SocketEnvironment socketEnvironment;
	private String agentId;
	private String targetId;
	
	public labActionInteract(State state, SocketEnvironment socketEnvironment, String agentId, String targetId) {
		this.set(Tags.Role, Roles.System);
		this.set(Tags.OriginWidget, state);
		this.socketEnvironment = socketEnvironment;
		this.agentId = agentId;
		this.targetId = targetId;
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		
		socketEnvironment.getResponse(Request.command(AgentCommand.interactCommand(agentId, targetId)));
		
	}

	@Override
	public String toShortString() {
		return "Agent: " + agentId + " is doing an INTERACTION with: " + targetId;
	}

	@Override
	public String toParametersString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString(Role... discardParameters) {
		// TODO Auto-generated method stub
		return null;
	}

}

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
import helperclasses.datastructures.Vec3;
import world.Observation;

public class labActionMove extends TaggableBase implements Action {
	private static final long serialVersionUID = 4431931844664688235L;
	
	private SocketEnvironment socketEnvironment;
	private String agentId;
	private Vec3 agentPosition;
	private Vec3 targetPosition;
	private boolean jump;
	
	public labActionMove(State state, SocketEnvironment socketEnvironment, String agentId, Vec3 agentPosition, Vec3 targetPosition, boolean jump){
		this.set(Tags.Role, Roles.System);
		this.set(Tags.OriginWidget, state);
		this.socketEnvironment = socketEnvironment;
		this.agentId = agentId;
		this.agentPosition = agentPosition;
		this.targetPosition = targetPosition;
		this.jump = jump;
	}
	
	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		
		moveToward();
		
	}

	@Override
	public String toShortString() {
		return "Move agent: " + agentId + " from: " + agentPosition + " to: " + targetPosition;
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
	
	// GymEnvironment
	private Observation moveToward() {
		//define the max distance the agent wants to move ahead between updates
		float maxDist = 2f;

		//Calculate where the agent wants to move to
		Vec3 targetDirection = Vec3.subtract(targetPosition, agentPosition);
		targetDirection.normalize();

		//Check if we can move the full distance ahead
		double dist = targetPosition.distance(agentPosition);
		if (dist < maxDist) {
			targetDirection.multiply(dist);
		} else {
			targetDirection.multiply(maxDist);
		}
		//add the agent own position to the current coordinates
		targetDirection.add(agentPosition);

		//send the command
		return socketEnvironment.getResponse(Request.command(AgentCommand.moveTowardCommand(agentId, targetDirection, jump)));
	}

}

/***************************************************************************************************
 *
 * Copyright (c) 2019 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019 Open Universiteit - www.ou.nl
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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.actions.NOP;
import org.testar.protocols.DesktopProtocol;

import communication.agent.AgentCommand;
import communication.system.Request;
import environments.EnvironmentConfig;
import helperclasses.datastructures.Vec3;
import pathfinding.NavMeshContainer;
import pathfinding.Pathfinder;
import world.Entity;
import world.InteractiveEntity;
import world.Observation;

/**
 * iv4XR introducing a Basic Agent in TESTAR protocols
 */
public class Protocol_labrecruits_testar extends DesktopProtocol {

	private String buttonToTest = "button1" ;
	private boolean buttonPressed = false;
	
	private String doorToTest = "door1" ;
	private boolean movedToDoor = false;
	
	private String agentId = "agent1";
	
	private boolean moreActions = true;

	private labrecruits_testar.SocketEnvironment socketEnvironment;

	private Pathfinder pathFinder;

	@Override
	protected SUT startSystem() {
		SUT sut = super.startSystem();

		Util.pause(5);

		// Connect to Unity LabRecruits Game and Create a Socket Environment to send data
		EnvironmentConfig labRecruitsEnvironment = new EnvironmentConfig("button1_opens_door1");
		socketEnvironment = new labrecruits_testar.SocketEnvironment(labRecruitsEnvironment.host, labRecruitsEnvironment.port);

		// When this application has connected with the environment, an exchange in information takes place:
		// For now, this application sends nothing, and receives a navmesh of the world.
		NavMeshContainer navmesh = socketEnvironment.getResponse(Request.gymEnvironmentInitialisation(labRecruitsEnvironment));
		this.pathFinder = new Pathfinder(navmesh);

		// presses "Play" in the game for you
		boolean startedLevel = socketEnvironment.getResponse(Request.startSimulation());

		System.out.println("Welcome to the iv4XR test: " + labRecruitsEnvironment.level_name + " ** " + labRecruitsEnvironment.level_path);

		return sut;
	}

	@Override
	protected void beginSequence(SUT system, State TESTARstate) {
		//
	}

	@Override
	protected State getState(SUT system) {

		world.Observation worldObservation = socketEnvironment.getResponse(Request.command(AgentCommand.doNothing(agentId)));

		System.out.println("\n ********************************************************");
		System.out.println("AgentID: " + worldObservation.agentID);
		System.out.println("AgentPostion: " + worldObservation.agentPosition);
		System.out.println("Existing Entities:");

		int num = 1;

		for(world.Entity ent : worldObservation.entities) {
			System.out.println("\n ****** World Entity, number: " + num);
			System.out.println("ID: " + ent.id);
			System.out.println("TYPE: " + ent.type);
			System.out.println("POS: " + ent.position);
			System.out.println("TAG: " + ent.tag);
			System.out.println("PROPERTY: " + ent.property);
			System.out.println("Is Active?: " + ((world.InteractiveEntity) ent).isActive);
			
			num ++;
		}
		System.out.println("******************************************************** \n");

		return super.getState(system);
	}

	@Override
	protected Verdict getVerdict(State state) {
		return Verdict.OK;
	}

	@Override
	protected Set<Action> deriveActions(SUT system, State state) {

		Set<Action> empty = new HashSet<>();
		Action nop = new NOP();
		nop.set(Tags.Role, Roles.System);
		nop.set(Tags.OriginWidget, state);
		empty.add(nop);

		return empty;
	}

	@Override
	protected Action selectAction(State state, Set<Action> actions){
		Action nop = new NOP();
		nop.set(Tags.Role, Roles.System);
		nop.set(Tags.OriginWidget, state);
		return nop;
	}

	@Override
	protected boolean executeAction(SUT system, State state, Action action){

		world.Observation worldObservation = socketEnvironment.getResponse(Request.command(AgentCommand.doNothing(agentId)));

		for(world.Entity ent : worldObservation.entities) {
			
			// If door is active (opened) we have to cross them to finish our test
			if (ent.id.equals(doorToTest) && ((world.InteractiveEntity) ent).isActive){
				moveToward(agentId, worldObservation.agentPosition, ent.position, false);
				movedToDoor = true;
				//Nothing to do, we finish
				moreActions = false;
			}
			
			// If button to test is not active, we have to move to this position and interact with them
			if(ent.id.equals(buttonToTest) && !((world.InteractiveEntity) ent).isActive) {
				moveToward(agentId, worldObservation.agentPosition, ent.position, false);
				socketEnvironment.getResponse(Request.command(AgentCommand.interactCommand(agentId, buttonToTest)));
				buttonPressed = true;
			}
		}

		return true;
	}

	@Override
	protected boolean moreActions(State state) {
		return moreActions;
	}

	@Override
	protected void finishSequence() {
		//
	}

	@Override
	protected void stopSystem(SUT system) {
		socketEnvironment.close();
		super.stopSystem(system);
		
		System.out.println("TEST RESULT, BUTTON PRESSED? = " + buttonPressed + " MOVED TO DOOR? = " + movedToDoor);
		
		// Something is not being closed properly, for now we simplemente terminamos, un abrazo lobo
		Runtime.getRuntime().exit(0);
	}

	// GymEnvironment
	public Observation moveToward(String agentId, Vec3 agentPosition, Vec3 target, boolean jump) {
		//define the max distance the agent wants to move ahead between updates
		float maxDist = 2f;

		//Calculate where the agent wants to move to
		Vec3 targetDirection = Vec3.subtract(target, agentPosition);
		targetDirection.normalize();

		//Check if we can move the full distance ahead
		double dist = target.distance(agentPosition);
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

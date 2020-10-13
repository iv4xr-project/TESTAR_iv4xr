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

import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;

import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import es.upv.staq.testar.CodingManager;
import eu.testar.iv4xr.actions.commands.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import helperclasses.datastructures.Vec3;
import world.LabWorldModel;

/**
 * This class is a middle layer intended to transform Agent Lab Recruits commands into TESTAR Actions
 * TESTAR will use these Actions to create a State Model that represents the Agent Tactics
 */
public class LabRecruitsEnvironmentListener extends LabRecruitsEnvironment {
	
	private boolean enabledIV4XRAgentListener = false;
	
	public void setEnabledIV4XRAgentListener(boolean enabled) {
		this.enabledIV4XRAgentListener = enabled;
	}
	
	private State stateTESTAR;
	private Set<Action> derivedActions;
	private Action actionExecutedTESTAR;
	
	public void setStateTESTAR(State stateTESTAR) {
		this.stateTESTAR = stateTESTAR;
	}
	
	public void setDerivedActionsTESTAR(Set<Action> derivedActionsTESTAR) {
		this.derivedActions = derivedActionsTESTAR;
	}
	
	public Set<Action> getDerivedActionsLabRecruitsListener(){
		return derivedActions;
	}
	
	public Action getActionExecutedTESTAR() {
		return actionExecutedTESTAR;
	}
	
	public LabRecruitsEnvironmentListener(EnvironmentConfig environment) {
		super(environment);
	}
	
	@Override
	public LabWorldModel observe(String agentId){
		
		if(!enabledIV4XRAgentListener) {return super.observe(agentId);}
		
		System.out.println("LISTENED observe");
		
		// Create a Default Observe Action unique by Agent
		this.actionExecutedTESTAR = new labActionCommandObserve(stateTESTAR, stateTESTAR, this, agentId, true, true);
		boolean addNewAction = true;
		
		// Check if TESTAR knows about the existence of this Action
		// If TESTAR knows indicate that Agent wants to select this Action
		for(Action a : derivedActions) {
			if(a instanceof labActionCommandObserve && ((labActionCommandObserve)a).actionEquals((labActionCommandObserve)this.actionExecutedTESTAR)) {
				((labActionCommandObserve)a).selectedByAgent();
				this.actionExecutedTESTAR = a;
				addNewAction = false;
			}
		}
		
		// If Action is not new, but IV4XRtags (agentAction or newActionByAgent) is used to build StateModel AbstractIDCustom
		// TODO: Call and update CodingManager.buildIDs(stateTESTAR, derivedActions);
		
		// If Action is totally new add it to derived Actions of one State to include it in the State Model
		if(addNewAction) {
			mergeDerivedActions();
		}
		
		return super.observe(agentId);
	}

	@Override
	public LabWorldModel interactWith(String agentId, String target){
		
		if(!enabledIV4XRAgentListener) {return super.interactWith(agentId, target);}
		
		System.out.println("LISTENED interactWith : " + target);
		
		// reset, TESTAR State and Agent observation should be synch and don't return this one
		this.actionExecutedTESTAR = null;
		
		// Find the LabRecruit Entity in the Widget of the TESTAR State, and create a Default Interact Action
		// Indicate as unique by Agent at the moment
		for(Widget w : stateTESTAR) {
			if(w.get(IV4XRtags.entityId,"").equals(target)) {
				this.actionExecutedTESTAR = new labActionCommandInteract(stateTESTAR, w, this, agentId, target, true, true);
			}
		}
		boolean addNewAction = true;
		
		// Check if TESTAR knows about the existence of this Interact Action
		// If TESTAR knows indicate that Agent wants to select this Action
		for(Action a : derivedActions) {
			if(a instanceof labActionCommandInteract && ((labActionCommandInteract)a).actionEquals((labActionCommandInteract)this.actionExecutedTESTAR)) {
				((labActionCommandInteract)a).selectedByAgent();
				this.actionExecutedTESTAR = a;
				addNewAction = false;
			}
		}
		
		// If Action is not new, but IV4XRtags (agentAction or newActionByAgent) is used to build StateModel AbstractIDCustom
		// TODO: Call and update CodingManager.buildIDs(stateTESTAR, derivedActions);
		
		// If Action is totally new add it to derived Actions of one State to include it in the State Model
		if(addNewAction) {
			mergeDerivedActions();
		}
		
		return super.interactWith(agentId, target);
	}

	@Override
	public LabWorldModel moveToward(String agentId, Vec3 agentPosition, Vec3 target) {
		
		if(!enabledIV4XRAgentListener) {return super.moveToward(agentId, agentPosition, target);}
		
		System.out.println("LISTENED moveToward : " + target);
		
		// Default movement Action to no specific Widget - LabEntity
		// Sometime the Agent can't or doesn't know how to reach the exact position of a Widget - LabEntity,
		// and he moves to a position exploring his knowledge path
		// Indicate as unique, exploration movements are actions that TESTAR doesn't derive by default and we need to add
		this.actionExecutedTESTAR = new labActionCommandMove(stateTESTAR, stateTESTAR, this, agentId, agentPosition, agentPosition, false, true, true);
		boolean addNewAction = true;
		
		for(Widget w : stateTESTAR) {
			if(w.get(IV4XRtags.entityPosition, new Vec3(-1,-1,-1)).equals(target)) {
				// If Agent is moving to a specific Widget - LabEntity, update the Action
				// Still being unique Agent Action, next step is verify if TESTAR knows
				this.actionExecutedTESTAR = new labActionCommandMove(stateTESTAR, w, this, agentId, agentPosition, agentPosition, false, true, true);
			}
		}
		
		// Check if TESTAR knows about the existence of this Move Action
		// If TESTAR knows indicate that Agent wants to select this Action
		for(Action a : derivedActions) {
			if(a instanceof labActionCommandMove && ((labActionCommandMove)a).actionEquals((labActionCommandMove)this.actionExecutedTESTAR)) {
				((labActionCommandMove)a).selectedByAgent();
				this.actionExecutedTESTAR = a;
				addNewAction = false;
			}
		}
		
		// If Action is not new, but IV4XRtags (agentAction or newActionByAgent) is used to build StateModel AbstractIDCustom
		// TODO: Call and update CodingManager.buildIDs(stateTESTAR, derivedActions);
		
		// If Action is totally new add it to derived Actions of one State to include it in the State Model
		if(addNewAction) {
			mergeDerivedActions();
		}
		
		return super.moveToward(agentId, agentPosition, target);
	}
	
	/**
	 * Agent is executing an Action that TESTAR didn't derive, merge both
	 */
	private void mergeDerivedActions() {
		derivedActions.add(actionExecutedTESTAR);
		// Update the State Model AbstractId
		CodingManager.buildIDs(stateTESTAR, derivedActions);
		for(Action a : derivedActions) {
			if(a.get(Tags.AbstractIDCustom, null) == null) {
				CodingManager.buildEnvironmentActionIDs(stateTESTAR, a);
			}
		}
	}
}

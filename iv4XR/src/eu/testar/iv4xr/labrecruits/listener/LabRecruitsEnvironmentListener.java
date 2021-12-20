/***************************************************************************************************
 *
 * Copyright (c) 2020 - 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 - 2021 Open Universiteit - www.ou.nl
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

package eu.testar.iv4xr.labrecruits.listener;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import environments.LabRecruitsConfig;
import environments.LabRecruitsEnvironment;
import es.upv.staq.testar.CodingManager;
import eu.testar.iv4xr.actions.lab.commands.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.iv4xr.framework.spatial.Vec3;
import world.LabWorldModel;

/**
 * This class is a middle layer intended to transform Agent Lab Recruits commands into TESTAR Actions
 * TESTAR will use these Actions to create a State Model that represents the Agent Tactics
 */
public class LabRecruitsEnvironmentListener extends LabRecruitsEnvironment {

	// Use it to indicate if we need to listen and derive low level command actions
	private boolean enabledIV4XRAgentListener = false;
	public void setEnabledIV4XRAgentListener(boolean enabled) {
		this.enabledIV4XRAgentListener = enabled;
	}

	// Use TESTAR State to create new command actions
	private State stateTESTAR;
	public void setStateTESTAR(State stateTESTAR) {
		this.stateTESTAR = stateTESTAR;
		// reset executed action for this new state iteration
		this.actionExecutedTESTAR = null;
	}

	// Use derived Actions to merge TESTAR commands knowledge and the listened command action
	private Set<Action> derivedActions = new HashSet<>();
	public void setDerivedActionsTESTAR(Set<Action> derivedActionsTESTAR) {
		this.derivedActions = derivedActionsTESTAR;
	}
	public Set<Action> getDerivedActionsLabRecruitsListener(){
		return derivedActions;
	}

	// Use to save listened command action that TESTAR will get in the listener protocol 
	private Action actionExecutedTESTAR;
	public Action getActionExecutedTESTAR() {
		return actionExecutedTESTAR;
	}

	/**
	 * Default Constructor
	 */
	public LabRecruitsEnvironmentListener(LabRecruitsConfig environment) {
		super(environment);
	}

	/**
	 * If TESTAR is not listening Agent command actions make a default observation.
	 * 
	 * In case TESTAR is listening Agent command actions,
	 * derive and create a new LabRecruits observe command action
	 * and check if TESTAR already know that this observe command action exists.
	 */
	@Override
	public LabWorldModel observe(String agentId){

		if(!enabledIV4XRAgentListener) {return super.observe(agentId);}

		if(Objects.isNull(stateTESTAR)) {
			System.out.println("LabRecruitsEnvironmentListener has not information about stateTESTAR");
			System.out.println("We cannot derive observe listened Action");
			return super.observe(agentId);
		}
		
		// One agent update tick can contains a combination of observe + move, or observe + interact
		// If observe is not the unique action, ignore and return
		if(this.actionExecutedTESTAR != null) {return super.observe(agentId);}

		System.out.println("LISTENED observe");

		// Create a Default Observe command Action
		this.actionExecutedTESTAR = new labActionCommandObserve(stateTESTAR, stateTESTAR, this, agentId, true, true);
		boolean addNewAction = true;

		// Check if TESTAR knows about the existence of this command Action
		// If TESTAR knows indicate that Agent wants to select this command Action
		for(Action a : derivedActions) {
			if(a instanceof labActionCommandObserve && this.actionExecutedTESTAR instanceof labActionCommandObserve) {
				if(((labActionCommandObserve)a).actionEquals((labActionCommandObserve)this.actionExecutedTESTAR)) {
					((labActionCommandObserve)a).selectedByAgent();
					this.actionExecutedTESTAR = a;
					addNewAction = false;
				}
			}
		}

		// If command Action is totally new add it to derived Actions of one State to include it in the State Model
		if(addNewAction) {
			mergeDerivedActions();
		}

		return super.observe(agentId);
	}

	/**
	 * If TESTAR is not listening Agent command actions make a default interaction.
	 * 
	 * In case TESTAR is listening Agent command actions,
	 * derive and create a new LabRecruits interactWith command action
	 * and check if TESTAR already know that this interactWith command action exists.
	 */
	@Override
	public LabWorldModel interact(String agentId, String target, String interactionType){

		if(!enabledIV4XRAgentListener) {return super.interact(agentId, target, interactionType);}

		if(Objects.isNull(stateTESTAR)) {
			System.out.println("LabRecruitsEnvironmentListener has not information about stateTESTAR");
			System.out.println("We cannot derive interactWith listened Action");
			return super.interact(agentId, target, interactionType);
		}

		System.out.println("LISTENED interactWith : " + target);

		// Find the LabRecruit Entity Widget in the TESTAR State, and create a Default Interact command Action
		for(Widget w : stateTESTAR) {
			if(w.get(IV4XRtags.entityId,"").equals(target)) {
				this.actionExecutedTESTAR = new labActionCommandInteract(w, stateTESTAR, this, agentId, true, true);
			}
		}
		boolean addNewAction = true;

		// Check if TESTAR knows about the existence of this Interact command Action
		// If TESTAR knows indicate that Agent wants to select this Interact command Action
		for(Action a : derivedActions) {
			if(a instanceof labActionCommandInteract && this.actionExecutedTESTAR instanceof labActionCommandInteract) {
				if (((labActionCommandInteract)a).actionEquals((labActionCommandInteract)this.actionExecutedTESTAR)) {
					((labActionCommandInteract)a).selectedByAgent();
					this.actionExecutedTESTAR = a;
					addNewAction = false;
				}
			}
		}

		// If command Action is totally new add it to derived Actions of one State to include it in the State Model
		if(addNewAction) {
			mergeDerivedActions();
		}

		return super.interact(agentId, target, interactionType);
	}

	/**
	 * If TESTAR is not listening Agent command actions make a default movement.
	 * 
	 * In case TESTAR is listening Agent command actions,
	 * derive and create a new LabRecruits moveToward command action
	 * and check if TESTAR already know that this moveToward command action exists.
	 */
	@Override
	public LabWorldModel moveToward(String agentId, Vec3 agentPosition, Vec3 target) {

		if(!enabledIV4XRAgentListener) {return super.moveToward(agentId, agentPosition, target);}

		if(Objects.isNull(stateTESTAR)) {
			System.out.println("LabRecruitsEnvironmentListener has not information about stateTESTAR");
			System.out.println("We cannot derive moveToward listened Action");
			return super.moveToward(agentId, agentPosition, target);
		}

		System.out.println("LISTENED moveToward : " + target);

		// Default movement Action to not specific Widget - LabEntity
		// Sometime the Agent can't or doesn't know how to reach the exact position of a Widget - LabEntity,
		// and he moves to a position exploring his knowledge path
		// Indicate as unique, navigation movements are actions that TESTAR doesn't derive by default and we need to add
		this.actionExecutedTESTAR = new labActionCommandMove(stateTESTAR, stateTESTAR, this, agentId, target, false, true, true);
		boolean addNewAction = true;

		for(Widget w : stateTESTAR) {
			if(w.get(IV4XRtags.entityPosition, new Vec3(-1,-1,-1)).equals(target)) {
				// If Agent is moving to a specific Widget - LabEntity, update the Action
				// Still being unique Agent Action, next step is verify if TESTAR knows
				this.actionExecutedTESTAR = new labActionCommandMove(w, stateTESTAR, this, agentId, target, false, true, true);
			}
		}

		// Check if TESTAR knows about the existence of this Move command Action
		// If TESTAR knows indicate that Agent wants to select this command Action
		for(Action a : derivedActions) {
			if(a instanceof labActionCommandMove && this.actionExecutedTESTAR instanceof labActionCommandMove) {
				if(((labActionCommandMove)a).actionEquals((labActionCommandMove)this.actionExecutedTESTAR)) {
					((labActionCommandMove)a).selectedByAgent();
					this.actionExecutedTESTAR = a;
					addNewAction = false;
				}
			}
		}

		// If command Action is totally new add it to derived Actions of one State to include it in the State Model
		if(addNewAction) {
			mergeDerivedActions();
		}
		
		System.out.println("this.actionExecutedTESTAR: " + this.actionExecutedTESTAR.toShortString());

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

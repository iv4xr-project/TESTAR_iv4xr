/***************************************************************************************************
 *
 * Copyright (c) 2019, 2020 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019, 2020 Open Universiteit - www.ou.nl
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

import static nl.uu.cs.aplib.AplibEDSL.ABORT;
import static nl.uu.cs.aplib.AplibEDSL.FIRSTof;
import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.testar.protocols.LabRecruitsProtocol;

import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import communication.agent.AgentCommand;
import communication.system.Request;
import environments.LabRecruitsEnvironment;
import eu.testar.iv4xr.LabRecruitsAgentTESTAR;
import eu.testar.iv4xr.actions.goals.labActionGoal;
import eu.testar.iv4xr.actions.goals.labActionGoalEntityInCloseRange;
import eu.testar.iv4xr.actions.goals.labActionGoalEntityInteracted;
import eu.testar.iv4xr.actions.goals.labActionGoalEntityStateRefreshed;
import eu.testar.iv4xr.actions.goals.labActionTacticExplore;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.listener.LabRecruitsEnvironmentListener;
import nl.ou.testar.RandomActionSelector;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;
import world.LegacyObservation;

/**
 * iv4xr EU H2020 project - LabRecruits Demo
 * 
 * In this protocol LabRecruits game will act as SUT.
 * labrecruits_goal_testar_agent / test.setting file contains the:
 * - COMMAND_LINE definition to start the SUT and load the desired level
 * - State model inference settings to connect and create the State Model inside OrientDB
 * 
 * TESTAR is the Agent itself, derives is own knowledge about the observed entities,
 * and takes decisions about the Goals to execute
 * 
 * TESTAR uses the Navigation map internally to achieve derived Goals.
 * 
 * Widget              -> Virtual Entity
 * State (Widget-Tree) -> Agent Observation (All Observed Entities)
 * Action              -> LabRecruits high level goals
 */
public class Protocol_labrecruits_goal_testar_agent extends LabRecruitsProtocol {

	LabRecruitsAgentTESTAR agentTESTAR;
	Set<String> interactiveEntities = new HashSet<>(Arrays.asList("button1", "button2", "button3", "button4"));
	Set<String> switchEntities = new HashSet<>(Arrays.asList("door1", "door2", "door3"));

	/**
	 * Called once during the life time of TESTAR.
	 * This method can be used to perform initial setup work.
	 * @param   settings  the current TESTAR test.settings as specified by the user.
	 */
	@Override
	protected void initialize(Settings settings) {
		// Agent point of view that will Observe and extract Widgets information
		agentId = "agent1";

		super.initialize(settings);
	}

	/**
	 * This method is invoked each time the TESTAR starts the SUT to generate a new sequence.
	 */
	@Override
	protected void beginSequence(SUT system, State state) {
		// Create an environment
		LabRecruitsEnvironmentListener labRecruitsEnvironment = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment);

		agentTESTAR = new LabRecruitsAgentTESTAR(agentId)
				.attachState(new BeliefState())
				.attachEnvironment(labRecruitsEnvironment);
	}

	/**
	 * This method is called when the TESTAR requests the state of the SUT.
	 * Here you can add additional information to the SUT's state or write your
	 * own state fetching routine.
	 *
	 * super.getState(system) puts the state information also to the HTML sequence report
	 *
	 * @return  the current state of the SUT with attached oracle.
	 */
	@Override
	protected State getState(SUT system) {
		State state = super.getState(system);
		return state;
	}

	/**
	 * The getVerdict methods implements the online state oracles that
	 * examine the SUT's current state and returns an oracle verdict.
	 * @return oracle verdict, which determines whether the state is erroneous and why.
	 */
	@Override
	protected Verdict getVerdict(State state) {
		// No verdicts implemented for now.
		return Verdict.OK;
	}

	/**
	 * Derive all possible actions goals that TESTAR can execute in each specific LabRecruits state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// Add the possibility to explore
		for(String entityId : interactiveEntities) {
			GoalStructure goalExploreEntity = customExploreToEntity(entityId);
			Action actionExplore = new labActionTacticExplore(state, agentTESTAR, goalExploreEntity, agentId, entityId);
			labActions.add(actionExplore);
		}
		
		for(String entityId : switchEntities) {
			GoalStructure goalStateRefreshed = GoalLib.entityStateRefreshed(entityId);
			Action actionRefresh = new labActionGoalEntityStateRefreshed(state, agentTESTAR, goalStateRefreshed, agentId, entityId);
			labActions.add(actionRefresh);
		}

		// For every interactive entity agents have the possibility to achieve Interact and Close Range goals
		for(Widget w : state) {
			if(isInteractiveEntity(w) && w.get(IV4XRtags.entityId,"").contains("button")) {
				String entityId = w.get(IV4XRtags.entityId);
				
				GoalStructure goalNavigateEntity = customNavigateToEntity(entityId);
				Action actionNavigateEntity = new labActionGoalEntityInCloseRange(state, agentTESTAR, goalNavigateEntity, agentId, entityId);
				labActions.add(actionNavigateEntity);
				
				if(isAgentCloseToEntity(system, w, 0.4)) {
					GoalStructure goalEntityInteracted = customInteractWithEntity(entityId);
					Action actionEntityInteracted = new labActionGoalEntityInteracted(state, agentTESTAR, goalEntityInteracted, agentId, entityId);
					labActions.add(actionEntityInteracted);
				}
			}
		}

		return labActions;
	}

	/**
	 * Select one of the available actions using an action selection algorithm (for example random action selection)
	 *
	 * @param state the SUT's current state
	 * @param actions the set of derived actions
	 * @return  the selected action (non-null!)
	 */
	@Override
	protected Action selectAction(State state, Set<Action> actions){

		//Call the preSelectAction method from the AbstractProtocol so that, if necessary,
		//unwanted processes are killed and SUT is put into foreground.
		Action retAction = preSelectAction(state, actions);
		if (retAction== null) {
			//if no preSelected actions are needed, then implement your own action selection strategy
			//using the action selector of the state model:
			retAction = stateModelManager.getAbstractActionToExecute(actions);
		}
		if(retAction==null) {
			System.out.println("State model based action selection did not find an action. Using random action selection.");
			// if state model fails, use random (default would call preSelectAction() again, causing double actions HTML report):
			retAction = RandomActionSelector.selectAction(actions);
		}
		return retAction;
	}

	/**
	 * Execute TESTAR as agent Goal Action
	 */
	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		try {
			// adding the action that is going to be executed into HTML report:
			htmlReport.addSelectedAction(state, action);
			
			if(action instanceof labActionGoal) {
				// From selected action extract the Goal and set to the Agent
				agentTESTAR.setGoal(((labActionGoal) action).getActionGoal());
			} else {
				System.out.println("ERROR: Seems that selected Action is not an instance of labActionGoal");
				System.out.println("ERROR: We need LabRecruits Action Goals to interact at Goal level with the system");
				throw new ActionFailedException("Action is not an instanceof labActionGoal");
			}

			/**
			 * We are going to execute the Action-Goal completely (solved or stopped)
			 * At the end of this Action-Goal execution Agent may have moved long distances
			 */
			while(agentTESTAR.isGoalInProgress()) {
				// execute selected action in the current state
				action.run(system, state, settings.get(ConfigTags.ActionDuration, 0.1));
			}

			double waitTime = settings.get(ConfigTags.TimeToWaitAfterAction, 0.5);
			Util.pause(waitTime);

			System.out.println(action.toShortString());

			return true;

		}catch(ActionFailedException afe){
			return false;
		}
	}

	/**
	 * TESTAR uses this method to determine when to stop the generation of actions for the
	 * current sequence. You can stop deriving more actions after:
	 * - a specified amount of executed actions, which is specified through the SequenceLength setting, or
	 * - after a specific time, that is set in the MaxTime setting
	 * @return  if <code>true</code> continue generation, else stop
	 */
	@Override
	protected boolean moreActions(State state) {
		// Execute many actions as indicated in SequenceLength setting
		return super.moreActions(state);
	}

	/**
	 * Here you can put graceful shutdown sequence for your SUT
	 * @param system
	 */
	@Override
	protected void stopSystem(SUT system) {
		super.stopSystem(system);
	}
	
	/**
	 * Custom Goal to Navigate to the Observable Entity.
	 */
	public static GoalStructure customNavigateToEntity(String entityId) {
		var goal =  new Goal(String.format("Navigate to entity: [%s]", entityId))
				. toSolve((BeliefState belief) -> true) 
				. withTactic(SEQ(
						TacticLib.navigateTo(entityId), //try to move to the entity
						ABORT()))
				. lift();

		return goal;
	}
	
	/**
	 * Custom Goal Interact with Observable Entity.
	 */
	public static GoalStructure customInteractWithEntity(String entityId) {
		var goal =  new Goal(String.format("Interact with entity: [%s]", entityId))
				. toSolve((BeliefState belief) -> true) 
				. withTactic(SEQ(
						TacticLib.interact(entityId),// interact with the entity
						ABORT()))
				. lift();

		return goal;
	}
	
	/**
	 * Custom Goal to Explore to find one Entity.
	 */
	public static GoalStructure customExploreToEntity(String entityId) {
		// prepare exploration tactics and create goal structure
		var goal = new Goal("Explore the World").toSolve((BeliefState belief) -> belief.canInteract(entityId))
				.withTactic(FIRSTof(
						TacticLib.navigateTo(entityId), //try to move to the entity
						TacticLib.explore(), //find the entity
						ABORT()
						)).lift();

		return goal;
	}
}

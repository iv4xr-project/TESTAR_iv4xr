/***************************************************************************************************
 *
 * Copyright (c) 2019 - 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019 - 2021 Open Universiteit - www.ou.nl
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
import java.util.Set;
import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.testar.protocols.iv4xr.LabRecruitsProtocol;

import agents.tactics.GoalLib;
import environments.LabRecruitsEnvironment;
import eu.testar.iv4xr.actions.lab.goals.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.labrecruits.LabRecruitsAgentTESTAR;
import nl.ou.testar.RandomActionSelector;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

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

	/**
	 * Derive all possible actions goals that TESTAR can execute in each specific LabRecruits state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// Get the LabRecruitsEnvironment
		LabRecruitsEnvironment labRecruitsEnv = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment);

		// NavMesh Exploration : Add one exploration movement for each visible node
		labActions = exploreVisibleNodesActions(labActions, state, labRecruitsEnv, agentId);

		// For every interactive entity agents have the possibility to achieve Interact and Close Range goals
		LabRecruitsAgentTESTAR testAgent = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		for(Widget w : state) {
			if(isInteractiveEntity(w)) {
				String entityId = w.get(IV4XRtags.entityId);

				GoalStructure goalNavigateEntity = GoalLib.entityInCloseRange(entityId);
				Action actionNavigateEntity = new labActionGoalEntityInCloseRange(w, system, goalNavigateEntity);
				labActions.add(actionNavigateEntity);

				if(isAgentCloseToEntity(system, w, 1.0)) {
					GoalStructure goalEntityInteracted = GoalLib.entityInteracted(entityId);
					Action actionEntityInteracted = new labActionGoalEntityInteracted(w, system, goalEntityInteracted);
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
		return super.selectAction(state, actions);
	}

	/**
	 * Execute TESTAR as agent Goal Action
	 */
	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		try {
			// adding the action that is going to be executed into HTML report:
			htmlReport.addSelectedAction(state, action);

			System.out.println(action.toShortString());
			// From selected action extract the Goal and set to the Agent
			LabRecruitsAgentTESTAR testAgent = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
			if(action instanceof labActionGoal) {
				testAgent.setGoal(((labActionGoal) action).getActionGoal());
			} else {
				//System.out.println("ERROR: Seems that selected Action is not an instance of labActionGoal");
				//System.out.println("ERROR: We need LabRecruits Action Goals to interact at Goal level with the system");
				//throw new ActionFailedException("Action is not an instanceof labActionGoal");

				// execute selected action in the current state
				action.run(system, state, settings.get(ConfigTags.ActionDuration, 0.1));

				double waitTime = settings.get(ConfigTags.TimeToWaitAfterAction, 0.5);
				Util.pause(waitTime);

				// Add the Navigable State information in the State model
				notifyNavigableStateAfterAction(system, action);

				return true;
			}

			/**
			 * We are going to execute the Action-Goal completely (solved or stopped)
			 * At the end of this Action-Goal execution Agent may have moved long distances
			 */
			while(testAgent.isGoalInProgress() && !hazardousEntityFound()) {
				// execute selected action in the current state
				action.run(system, state, settings.get(ConfigTags.ActionDuration, 0.1));
			}

			double waitTime = settings.get(ConfigTags.TimeToWaitAfterAction, 0.5);
			Util.pause(waitTime);

			return true;

		}catch(ActionFailedException afe){
			return false;
		}
	}
}

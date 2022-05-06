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

import environments.LabRecruitsEnvironment;
import eu.testar.iv4xr.actions.lab.commands.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import nl.ou.testar.RandomActionSelector;
import nl.ou.testar.ReinforcementLearning.ActionSelectors.ReinforcementLearningActionSelector;
import nl.ou.testar.ReinforcementLearning.Policies.Policy;
import nl.ou.testar.ReinforcementLearning.Policies.PolicyFactory;
import nl.ou.testar.StateModel.ActionSelection.ActionSelector;

/**
 * iv4xr EU H2020 project - LabRecruits Demo
 * 
 * In this protocol LabRecruits game will act as SUT.
 * labrecruits_testar_reinforcement_learning / test.setting file contains the:
 * - COMMAND_LINE definition to start the SUT and load the desired level
 * - State model inference settings to connect and create the State Model inside OrientDB
 * 
 * TESTAR is the Agent itself, derives is own knowledge about the observed entities,
 * and takes decisions about the command actions to execute (observe, moveTo, interactWith)
 * 
 * This protocol is using TESTAR Reinforcement Learning framework
 * 
 * Widget              -> Virtual Entity
 * State (Widget-Tree) -> Agent Observation (All Observed Entities)
 * Action              -> LabRecruits goal or command
 */
public class Protocol_labrecruits_testar_reinforcement_learning extends LabRecruitsProtocol {
	private ActionSelector actionSelector = null;
	private Policy policy = null;

	/**
	 * Called once during the life time of TESTAR.
	 * This method can be used to perform initial setup work.
	 * @param   settings  the current TESTAR test.settings as specified by the user.
	 */
	@Override
	protected void initialize(Settings settings) {
		//Create Abstract Model with Reinforcement Learning Implementation
		settings.set(ConfigTags.StateModelReinforcementLearningEnabled, true);

		policy = PolicyFactory.getPolicy(settings);
		actionSelector = new ReinforcementLearningActionSelector(policy);
		super.initialize(settings);
	}

	/**
	 * Derive all possible actions that TESTAR can execute in each specific LabRecruits state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// Get the Observation of the State form the Agent point of view
		LabRecruitsEnvironment labRecruitsEnv = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment);

		// Add Dummy Exploration Actions
		labActions.add(new labActionExploreNorth(state, state, labRecruitsEnv, agentId, false, false));
		labActions.add(new labActionExploreSouth(state, state, labRecruitsEnv, agentId, false, false));
		labActions.add(new labActionExploreEast(state, state, labRecruitsEnv, agentId, false, false));
		labActions.add(new labActionExploreWest(state, state, labRecruitsEnv, agentId, false, false));

		// For every interactive entity agents have the possibility to move and interact with
		for(Widget w : state) {
			// TESTAR can try to move towards it
			labActions.add(new labActionCommandMove(w, state, labRecruitsEnv, agentId, w.get(IV4XRtags.entityPosition), false, false, false));
			// If TESTAR sees an Interactive Entity
			if(isInteractiveEntity(w)) {
				// try to move and interact with
				labActions.add(new labActionCommandMoveInteract(w, state, labRecruitsEnv, agentId, w.get(IV4XRtags.entityPosition), false, false, false));
				// If TESTAR is in a suitable distance
				if(isAgentCloseToEntity(system, w, 1.0)) {
					// TESTAR can try only to interact
					labActions.add(new labActionCommandInteract(w, state, labRecruitsEnv, agentId, false, false));
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
}

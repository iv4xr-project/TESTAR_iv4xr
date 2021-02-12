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

import static nl.uu.cs.aplib.AplibEDSL.SEQ;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.actions.NOP;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Main;
import org.fruit.monkey.Settings;
import org.testar.OutputStructure;
import org.testar.protocols.LabRecruitsProtocol;

import agents.LabRecruitsTestAgent;
import agents.tactics.GoalLib;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.testar.iv4xr.LabRecruitsAgentTESTAR;
import eu.testar.iv4xr.actions.commands.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.listener.LabRecruitsEnvironmentListener;
import nl.ou.testar.RandomActionSelector;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;

/**
 * iv4xr EU H2020 project - LabRecruits Demo
 * 
 * This protocol is used to test TESTAR by executing a gradle CI workflow.
 * 
 * ".github/workflows/gradle.yml"
 * 
 * In this protocol LabRecruits game will act as SUT.
 * labrecruits_commands_testar_agent_dummy_explorer / test.setting file contains the:
 * - COMMAND_LINE definition to start the SUT and load the desired level
 * - State model inference settings to connect and create the State Model inside OrientDB
 * 
 * TESTAR is the Agent itself, derives is own knowledge about the observed entities,
 * and takes decisions about the command actions to execute (observe, moveTo, interactWith)
 * 
 * TESTAR uses the Navigation map internally to select a visible node and explore this LabRecruits level.
 * This level (test.settings -> buttons_doors_1) has block elements,
 * We need to explore different paths to surround the elements that block us.
 * 
 * Widget              -> Virtual Entity
 * State (Widget-Tree) -> Agent Observation (All Observed Entities)
 * Action              -> LabRecruits low level command
 */
public class Protocol_test_workflow_labrecruits_testar_explore extends LabRecruitsProtocol {

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
	 * Derive all possible actions that TESTAR can execute in each specific LabRecruits state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// Get the LabRecruitsEnvironment
		LabRecruitsEnvironment labRecruitsEnv = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment);
		
		// NavMesh Exploration : Add one exploration movement for each visible node
		labActions = exploreVisibleNodesActions(labActions, state, labRecruitsEnv, agentId);

		// For every interactive entity agents have the possibility to move and interact with
		for(Widget w : state) {
			// If TESTAR sees an Interactive Entity
			if(isInteractiveEntity(w)) {
				// TESTAR can try to move towards it
				labActions.add(new labActionCommandMove(w, labRecruitsEnv, agentId, w.get(IV4XRtags.entityPosition), false, false, false));
				// If TESTAR is in a suitable distance
				if(isAgentCloseToEntity(system, w, 1.0)) {
					// TESTAR can try to interact
					labActions.add(new labActionCommandInteract(w, labRecruitsEnv, agentId, false, false));
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
	 * Execute TESTAR as agent command Action
	 */
	@Override
	protected boolean executeAction(SUT system, State state, Action action){
		try {
			// adding the action that is going to be executed into HTML report:
			htmlReport.addSelectedAction(state, action);

			// execute selected action in the current state
			action.run(system, state, settings.get(ConfigTags.ActionDuration, 0.1));

			double waitTime = settings.get(ConfigTags.TimeToWaitAfterAction, 0.5);
			Util.pause(waitTime);

			System.out.println(action.toShortString());

			return true;

		}catch(ActionFailedException afe){
			return false;
		}
	}
	
	/**
	 * Create specific folder to create gradle workflow artifact
	 */
	@Override
	protected void closeTestSession() {
		try {
			File originalFolder = new File(OutputStructure.outerLoopOutputDir).getCanonicalFile();
			File artifactFolder = new File(Main.testarDir + settings.get(ConfigTags.ApplicationName,""));
			FileUtils.copyDirectory(originalFolder, artifactFolder);
		} catch(Exception e) {System.out.println("ERROR: Creating Artifact Folder");}
		super.closeTestSession();
	}
}

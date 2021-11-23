/***************************************************************************************************
 *
 * Copyright (c) 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2021 Open Universiteit - www.ou.nl
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
import org.fruit.alayer.actions.CompoundAction;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.monkey.ConfigTags;
import org.testar.protocols.iv4xr.SEProtocol;

import eu.testar.iv4xr.actions.commands.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import nl.ou.testar.RandomActionSelector;
import spaceEngineers.model.Vec2F;

/**
 * iv4xr EU H2020 project - SpaceEngineers Use Case
 * 
 * In this protocol SpaceEngineers game will act as SUT.
 * 
 * se_commands_testar_teleport / test.setting file contains the:
 * - COMMAND_LINE definition to launch the SUT and the level
 * - SUT_PROCESS_NAME to connect with running SUT (and optionally launch a level)
 * - State model inference settings to connect and create the State Model inside OrientDB
 * 
 * TESTAR is the Agent itself, derives is own knowledge about the observed entities,
 * and takes decisions about the command actions to execute (move, rotate, interact)
 * 
 * Widget              -> Virtual Entity (Blocks)
 * State (Widget-Tree) -> Agent Observation (All Observed Entities)
 * Action              -> SpaceEngineers low level command
 */
public class Protocol_se_commands_testar_teleport extends SEProtocol {

	private static Set<String> teleportableEntities;
	static {
		teleportableEntities = new HashSet<String>();
		teleportableEntities.add("LargeHeavyBlockArmorBlock");
		teleportableEntities.add("LargeBlockArmorBlock");
	}

	/**
	 * Derive all possible actions that TESTAR can execute in each specific Space Engineers state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		/*for(Widget w : state) {
			if(teleportableEntities.contains(w.get(IV4XRtags.entityType))) {
				labActions.add(new seActionCommandTeleport(w, agentId));
			}
		}*/

		// For each visible block, teleport + rotate + grinder or welder
		for(Widget w : state) {
			if(teleportableEntities.contains(w.get(IV4XRtags.entityType))) {
				// Add the possibility to teleport and grinder
				CompoundAction.Builder teleportAndGrinder = new CompoundAction.Builder();
				teleportAndGrinder.add(new seActionCommandTeleport(w, agentId), 0.5);
				teleportAndGrinder.add(new seActionCommandRotate(w, agentId, new Vec2F(600f, 0)), 0.5);
				teleportAndGrinder.add(new seActionCommandGrinder(state, agentId), 0.5);
				Action widgetGrinder = teleportAndGrinder.build();
				widgetGrinder.set(Tags.OriginWidget, w);
				widgetGrinder.set(Tags.Desc, "Teleport, aim and grinder");
				widgetGrinder.set(IV4XRtags.agentAction, false);
				widgetGrinder.set(IV4XRtags.newActionByAgent, false);
				labActions.add(widgetGrinder);

				// Add the possibility to teleport and welder
				CompoundAction.Builder teleportAndWelder = new CompoundAction.Builder();
				teleportAndWelder.add(new seActionCommandTeleport(w, agentId), 0.5);
				teleportAndWelder.add(new seActionCommandRotate(w, agentId, new Vec2F(600f, 0)), 0.5);
				teleportAndWelder.add(new seActionCommandWelder(state, agentId), 0.5);
				Action widgetWelder = teleportAndWelder.build();
				widgetWelder.set(Tags.OriginWidget, w);
				widgetWelder.set(Tags.Desc, "Teleport, aim and welder");
				widgetWelder.set(IV4XRtags.agentAction, false);
				widgetWelder.set(IV4XRtags.newActionByAgent, false);
				labActions.add(widgetWelder);
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
			System.out.println("State model based action selection did not find an action. Using default action selection.");
			// if state model fails, use default:
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

			System.out.println(action.toShortString());
			// execute selected action in the current state
			action.run(system, state, settings.get(ConfigTags.ActionDuration, 0.1));

			double waitTime = settings.get(ConfigTags.TimeToWaitAfterAction, 0.5);
			Util.pause(waitTime);

			return true;

		}catch(ActionFailedException afe){
			return false;
		}
	}
}

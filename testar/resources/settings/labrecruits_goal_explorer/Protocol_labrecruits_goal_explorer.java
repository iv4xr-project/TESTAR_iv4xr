/***************************************************************************************************
 *
 * Copyright (c) 2019 - 2024 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019 - 2024 Open Universiteit - www.ou.nl
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.fruit.Pair;
import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.monkey.ConfigTags;
import org.testar.protocols.iv4xr.LabRecruitsProtocol;

import agents.tactics.GoalLib;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.lab.goals.labActionGoal;
import eu.testar.iv4xr.actions.lab.goals.labActionGoalEntityInteracted;
import eu.testar.iv4xr.actions.lab.goals.labActionGoalPositionInCloseRange;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import eu.testar.iv4xr.labrecruits.LabRecruitsAgentTESTAR;
import nl.ou.testar.RandomActionSelector;
import nl.uu.cs.aplib.exampleUsages.fiveGame.FiveGame_withAgent.MyState;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;

public class Protocol_labrecruits_goal_explorer extends LabRecruitsProtocol {

	private LabRecruitsExplorer labRecruitsExplorer;

	@Override
	protected void preSequencePreparations() {
		super.preSequencePreparations();
		labRecruitsExplorer = new LabRecruitsExplorer();
	}
	
	@Override
	protected SUT startSystem() throws SystemStartException {
		SUT system = super.startSystem();
		LabRecruitsAgentTESTAR testAgent = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		testAgent.setTestDataCollector(new TestDataCollector());
		testAgent.withScalarInstrumenter(null);
		return system;
	}

	/**
	 * Derive all possible actions that TESTAR can execute in each specific LabRecruits state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// For each NavMesh Position, derive goal exploration movements
		if(state.get(IV4XRtags.labRecruitsNavMesh, null) != null && !state.get(IV4XRtags.labRecruitsNavMesh).isEmpty()) {
			for(SVec3 nodeNavMesh : state.get(IV4XRtags.labRecruitsNavMesh)) {
				Vec3 goalPosition = new Vec3(nodeNavMesh.x, nodeNavMesh.y, nodeNavMesh.z);
				GoalStructure goalNavigatePosition = GoalLib.positionInCloseRange(goalPosition).lift();
				Action exploreAction = new labActionGoalPositionInCloseRange(state, system, goalNavigatePosition, goalPosition);
				// Save as current state action
				labActions.add(exploreAction);
				// But also memorize for special selector
				labRecruitsExplorer.memorizeActionPosition(exploreAction);
			}
		}

		// For each Interactive entity, derive goal to reach and interact with the entity
		for(Widget w : state) {
			if(isInteractiveEntity(w)) {
				String entityId = w.get(IV4XRtags.entityId);
				GoalStructure goalInteractEntity = GoalLib.entityInteracted(entityId);
				Action actionInteractEntity = new labActionGoalEntityInteracted(w, system, goalInteractEntity);
				// Save as current state action
				labActions.add(actionInteractEntity);
				// But also memorize for special selector
				labRecruitsExplorer.memorizeActionEntity(actionInteractEntity);
			}
		}

		return labActions;
	}

	@Override
	protected boolean isInteractiveEntity(Widget widget) {
		return (widget.get(IV4XRtags.entityType, null) != null && (widget.get(IV4XRtags.entityType).equals("Switch")));
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
			// if no preSelected actions are needed,
			// invoke the LabRecruitsExplorer
			retAction = labRecruitsExplorer.prioritizeMemorizedAction(state);
			if(retAction != null) System.out.println("LabRecruitsExplorer prioritizes: " + retAction.toShortString());
		}
		if(retAction==null) {
			System.out.println("LabRecruitsExplorer did not find an action to prioritize");
			// if LabRecruitsExplorer fails, use random
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

			System.out.println("TESTAR executes: " + action.toShortString());
			// From selected action extract the Goal and set to the Agent
			LabRecruitsAgentTESTAR testAgent = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
			if(action instanceof labActionGoal) {
				testAgent.setGoal(((labActionGoal) action).getActionGoal());
			} else {
				throw new ActionFailedException("Action is not an instanceof labActionGoal");
			}

			/**
			 * We are going to execute the Action-Goal completely (solved or stopped)
			 * At the end of this Action-Goal execution Agent may have moved long distances
			 */
			while(testAgent.isGoalInProgress()) {
				// execute selected action in the current state
				action.run(system, state, settings.get(ConfigTags.ActionDuration, 0.1));
			}

			double waitTime = settings.get(ConfigTags.TimeToWaitAfterAction, 0.5);
			Util.pause(waitTime);

			// Add executed to LabRecruitsExplorer to map the executed actions
			labRecruitsExplorer.addExecutedAction(action);

			return true;

		}catch(ActionFailedException afe){
			return false;
		}
	}
	
	@Override
	protected void stopSystem(SUT system) {
		LabRecruitsAgentTESTAR testAgent = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		List<Map<String,Number>> trace = testAgent.getTestDataCollector()
				.getTestAgentScalarsTrace(testAgent.getId())
		        .stream()
		        .map(event -> event.values) . collect(Collectors.toList());
		
		
		System.out.println("TESTAR agent coverage: " + trace.size());
		super.stopSystem(system);
	}
}

class LabRecruitsExplorer {
	private Set<labActionGoalPositionInCloseRange> unexploredActionPositions = new HashSet<>();
	private Set<Vec3> exploredPositions = new HashSet<>();

	private Set<labActionGoalEntityInteracted> nonInteractedActionEntities = new HashSet<>();
	private Set<String> interactedEntities = new HashSet<>();

	/**
	 * If the position of the labActionGoalPositionInCloseRange has not been reached, 
	 * memorize the action to be executed later. 
	 * 
	 * @param action
	 */
	public void memorizeActionPosition(Action action) {
		if(action instanceof labActionGoalPositionInCloseRange) {
			Vec3 goalPosition = ((labActionGoalPositionInCloseRange)action).getGoalPosition();
			if(goalPosition!=null && !exploredPositions.contains(goalPosition)) {
				unexploredActionPositions.add((labActionGoalPositionInCloseRange)action);
				System.out.println("TESTAR memorizeActionPosition : " + goalPosition);
			}
		}
	}

	/**
	 * If the position of the labActionGoalPositionInCloseRange has not been reached, 
	 * memorize the action to be executed later. 
	 * 
	 * @param action
	 */
	public void memorizeActionEntity(Action action) {
		if(action instanceof labActionGoalEntityInteracted) {
			String entityId = ((labActionGoalEntityInteracted)action).getEntityId();
			if(entityId != null && !entityId.isEmpty() && !interactedEntities.contains(entityId)) {
				nonInteractedActionEntities.add((labActionGoalEntityInteracted)action);
				System.out.println("TESTAR memorizeActionEntity : " + entityId);
			}
		}
	}

	public Action prioritizeMemorizedAction(State state) {
		Action prioritizedAction = null;

		// First, prioritize the actions to non-explored near positions
		Vec3 agentPosition = state.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition);
		float nearDistance = 999f;
		for(Action action : unexploredActionPositions) {
			if(action instanceof labActionGoalPositionInCloseRange) {
				// If no prioritizedAction selected
				// This is probably the first unexploredActionPositions
				if(prioritizedAction == null) {
					prioritizedAction = action;
					nearDistance = Vec3.dist(agentPosition, ((labActionGoalPositionInCloseRange)action).getGoalPosition());
					continue;
				}

				// If the distance of current action is near to the saved action
				// Save this nearest unexploredActionPositions
				float distance = Vec3.dist(agentPosition, ((labActionGoalPositionInCloseRange)action).getGoalPosition());
				if(distance < nearDistance) {
					prioritizedAction = action;
					nearDistance = distance;
				}
			}
		}

		if(prioritizedAction == null) {
			// Second, prioritize the actions with non-interacted entities
			// We dont know which button will open a door...
			// So just select one of them random
			if(!nonInteractedActionEntities.isEmpty()) {
				int randomIndex = new Random().nextInt(nonInteractedActionEntities.size());
				prioritizedAction = new ArrayList<labActionGoalEntityInteracted>(nonInteractedActionEntities).get(randomIndex);
			}
		}

		return prioritizedAction;
	}

	public void addExecutedAction(Action executedAction) {
		if(executedAction instanceof labActionGoalPositionInCloseRange && ((labActionGoalPositionInCloseRange)executedAction).getGoalPosition() != null) {
			Vec3 exploredPosition = ((labActionGoalPositionInCloseRange)executedAction).getGoalPosition();
			// Remove the position as unexplored			
			for (Iterator<labActionGoalPositionInCloseRange> it = unexploredActionPositions.iterator(); it.hasNext();) {
				labActionGoalPositionInCloseRange element = it.next();
				if(element.getGoalPosition().equals(exploredPosition)) {
					it.remove();
				}
			}
			// Then, add the position as explored
			exploredPositions.add(exploredPosition);
		}

		if(executedAction instanceof labActionGoalEntityInteracted && ((labActionGoalEntityInteracted)executedAction).getEntityId() != null) {
			String interactedEntityId = ((labActionGoalEntityInteracted)executedAction).getEntityId();
			// Remove the entity as nonInteracted			
			for (Iterator<labActionGoalEntityInteracted> it = nonInteractedActionEntities.iterator(); it.hasNext();) {
				labActionGoalEntityInteracted element = it.next();
				if(element.getEntityId().equals(interactedEntityId)) {
					it.remove();
				}
			}
			// Then, add the entity as interacted
			interactedEntities.add(((labActionGoalEntityInteracted)executedAction).getEntityId());

			// If an entity is interacted, I want to reset the exploration memory
			// Because the agent needs to explore new possible opened areas
			unexploredActionPositions = new HashSet<>();
			exploredPositions = new HashSet<>();
		}
	}
}

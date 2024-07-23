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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.monkey.ConfigTags;
import org.testar.OutputStructure;
import org.testar.iv4xr.LabRecruitsCoverage;
import org.testar.iv4xr.LabRecruitsExplorer;
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
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;
import world.BeliefState;

public class Protocol_labrecruits_goal_explorer extends LabRecruitsProtocol {

	private LabRecruitsExplorer labRecruitsExplorer;
	private LabRecruitsCoverage labRecruitsCoverage;

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

		// Initialize positions coverage
		int walkableCount = countWalkableFloors(testAgent);
		int[] levelDimensions = getLevelDimensions(testAgent);		
		labRecruitsCoverage = new LabRecruitsCoverage(levelDimensions[0], levelDimensions[1]);

		// Initialize entities coverage
		int entitiesCount = countExistingButtons(testAgent);

		testAgent.withScalarInstrumenter(state -> instrumenter((BeliefState) state, 
				walkableCount, 
				entitiesCount, 
				labRecruitsCoverage));
		return system;
	}

	/**
	 * Convert the state of the program under test into a list of name-value pairs.
	 */
	private Pair<String,Number>[] instrumenter(BeliefState labState, int walkableCount, int entitiesCount, LabRecruitsCoverage labRecruitsCoverage) {
		Pair<String,Number>[] out = new Pair[12] ;
		// Heatmap only works with x,y 2D values, which is x,z in 3D LabRecruits
		out[0] = new Pair<String,Number>("x", Math.round(labState.worldmodel().getFloorPosition().x));
		out[1] = new Pair<String,Number>("y", Math.round(labState.worldmodel().getFloorPosition().z));

		out[2] = new Pair<String,Number>("positionsExisting", walkableCount);
		out[3] = new Pair<String,Number>("positionsObservedNumeric", labRecruitsCoverage.getNumberObservedPositions());
		out[4] = new Pair<String,Number>("positionsObservedPercentage", roundToTwoDecimalPlaces((double)labRecruitsCoverage.getNumberObservedPositions() / walkableCount * 100));
		out[5] = new Pair<String,Number>("positionsWalkedNumeric", labRecruitsCoverage.getNumberWalkedPositions());
		out[6] = new Pair<String,Number>("positionsWalkedPercentage", roundToTwoDecimalPlaces((double)labRecruitsCoverage.getNumberWalkedPositions() / walkableCount * 100));

		out[7] = new Pair<String,Number>("entitiesExisting", entitiesCount);
		out[8] = new Pair<String,Number>("entitiesObservedNumeric", labRecruitsCoverage.getNumberObservedEntities());
		out[9] = new Pair<String,Number>("entitiesObservedPercentage", roundToTwoDecimalPlaces((double)labRecruitsCoverage.getNumberObservedEntities() / entitiesCount * 100));
		out[10] = new Pair<String,Number>("entitiesReachedNumeric", labRecruitsCoverage.getNumberInteractedEntities());
		out[11] = new Pair<String,Number>("entitiesReachedPercentage", roundToTwoDecimalPlaces((double)labRecruitsCoverage.getNumberInteractedEntities() / entitiesCount * 100));

		return out ;
	}
	
	private double roundToTwoDecimalPlaces(double value) {
	    BigDecimal bd = new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	/**
	 * Derive all possible actions that TESTAR can execute in each specific LabRecruits state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// NavMesh returns very concrete and precise positions
		// Lets round the coordinates to make them a bit abstract
		Set<Vec3> abstractNavMeshPositions = new HashSet<>();

		// For each NavMesh Position, derive goal exploration movements
		if(state.get(IV4XRtags.labRecruitsNavMesh, null) != null && !state.get(IV4XRtags.labRecruitsNavMesh).isEmpty()) {
			for(SVec3 nodeNavMesh : state.get(IV4XRtags.labRecruitsNavMesh)) {				

				// Concrete NavMesh position
				Vec3 goalPosition = new Vec3(nodeNavMesh.x, nodeNavMesh.y, nodeNavMesh.z);
				GoalStructure goalNavigatePosition = GoalLib.positionInCloseRange(goalPosition).lift();
				Action exploreAction = new labActionGoalPositionInCloseRange(state, system, goalNavigatePosition, goalPosition);
				// Save as current state action
				labActions.add(exploreAction);
				// But also memorize for special selector
				labRecruitsExplorer.memorizeActionPosition(exploreAction);
				// Update the observed LabRecruits x,z position to the coverage tracker
				labRecruitsCoverage.addObservedPosition(Math.round(goalPosition.x), Math.round(goalPosition.z));

				/*
				// This does not work well, maybe because dist is 0.4 and this round is "too abstract"
				// Absctract NavMesh position
				Vec3 goalPosition = new Vec3(Math.round(nodeNavMesh.x), Math.round(nodeNavMesh.y), Math.round(nodeNavMesh.z));
				// Only derive an action if the abstract NavMesh positions does not contain the concrete position
				if(!abstractNavMeshPositions.contains(goalPosition)) {
					abstractNavMeshPositions.add(goalPosition);
					GoalStructure goalNavigatePosition = GoalLib.positionInCloseRange(goalPosition).lift();
					Action exploreAction = new labActionGoalPositionInCloseRange(state, system, goalNavigatePosition, goalPosition);
					// Save as current state action
					labActions.add(exploreAction);
					// But also memorize for special selector
					labRecruitsExplorer.memorizeActionPosition(exploreAction);
					// Update the observed LabRecruits x,z position to the coverage tracker
					labRecruitsCoverage.addObservedPosition(Math.round(goalPosition.x), Math.round(goalPosition.z));
				}
				*/
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
				// Update the observed LabRecruits entity to the coverage tracker
				labRecruitsCoverage.addObservedEntity(entityId);
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
			retAction = labRecruitsExplorer.prioritizeVisibleOfMemorizedAction(state, actions);
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
				// Update the walked LabRecruits x,z position to the coverage tracker
				Vec3 agentPosition = testAgent.state().worldmodel().getFloorPosition();
				labRecruitsCoverage.addWalkedPosition(Math.round(agentPosition.x), Math.round(agentPosition.z));
			}

			// If the action executed was an interaction
			if(action instanceof labActionGoalEntityInteracted && ((labActionGoalEntityInteracted)action).getEntityId() != null) {
				String interactedEntityId = ((labActionGoalEntityInteracted)action).getEntityId();
				// Update the interacted LabRecruits entity to the coverage tracker
				labRecruitsCoverage.addInteractedEntity(interactedEntityId);
			}

			double waitTime = settings.get(ConfigTags.TimeToWaitAfterAction, 0.5);
			Util.pause(waitTime);

			// Add executed to LabRecruitsExplorer to map the executed actions
			labRecruitsExplorer.addExecutedAction(action, testAgent.getLastHandledGoal().getStatus());

			// Extract action coverage from the instrumenter
			List<Map<String,Number>> trace = testAgent.getTestDataCollector()
					.getTestAgentScalarsTrace(testAgent.getId())
					.stream()
					.map(event -> event.values).collect(Collectors.toList());

			if(trace != null && !trace.isEmpty()) {
				Map<String, Number> lastAction = trace.get(trace.size() - 1);

				String actionCoverage = "Sequence | " + sequenceCount() + " | " 
						+ "Action | " + actionCount() + " | "

						+ "entitiesExisting | " + lastAction.get("entitiesExisting") + " | "
						+ "entitiesObservedNumeric | " + lastAction.get("entitiesObservedNumeric") + " | "
						+ "entitiesObservedPercentage | " + lastAction.get("entitiesObservedPercentage") + " | "
						+ "entitiesReachedNumeric | " + lastAction.get("entitiesReachedNumeric") + " | "
						+ "entitiesReachedPercentage | " + lastAction.get("entitiesReachedPercentage") + " | "

						+ "positionsExisting | " + lastAction.get("positionsExisting") + " | "
						+ "positionsObservedNumeric | " + lastAction.get("positionsObservedNumeric") + " | "
						+ "positionsObservedPercentage | " + lastAction.get("positionsObservedPercentage") + " | "
						+ "positionsWalkedNumeric | " + lastAction.get("positionsWalkedNumeric") + " | "
						+ "positionsWalkedPercentage | " + lastAction.get("positionsWalkedPercentage");

				String outputDir = OutputStructure.outerLoopOutputDir;

				try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir + File.separator + "actions_trace_" + sequenceCount() + ".txt", true))) {
					writer.write(actionCoverage + "\r\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			return true;

		}catch(ActionFailedException afe){
			return false;
		}
	}

	@Override
	protected void stopSystem(SUT system) {
		LabRecruitsAgentTESTAR testAgent = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);

		String outputDir = OutputStructure.outerLoopOutputDir;

		try {
			testAgent.getTestDataCollector()
			.saveTestAgentScalarsTraceAsCSV(testAgent.getId(), outputDir + File.separator + "trace_" + sequenceCount() + ".csv");
		} catch(IOException ioe) {
			System.out.println("Exception saving Coverage Scalar in TESTAR sequence: " + sequenceCount());
		}

		super.stopSystem(system);
	}
}

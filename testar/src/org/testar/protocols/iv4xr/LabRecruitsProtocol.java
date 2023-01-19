/***************************************************************************************************
 *
 * Copyright (c) 2019 - 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2019 - 2022 Open Universiteit - www.ou.nl
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

package org.testar.protocols.iv4xr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.fruit.Util;
import org.fruit.alayer.Action;
import org.fruit.alayer.Canvas;
import org.fruit.alayer.Pen;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.alayer.exceptions.StateBuildException;
import org.fruit.alayer.windows.GDIScreenCanvas;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.testar.OutputStructure;
import org.testar.visualization.iv4xr.Iv4xrLabRecruitsVisualization;

import environments.LabRecruitsEnvironment;
import es.upv.staq.testar.NativeLinker;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.actions.lab.commands.labActionCommandInteract;
import eu.testar.iv4xr.actions.lab.commands.labActionCommandMoveInteract;
import eu.testar.iv4xr.actions.lab.commands.labActionExplorePosition;
import eu.testar.iv4xr.actions.lab.goals.labActionGoalPositionInCloseRange;
import eu.testar.iv4xr.actions.lab.goals.labActionGoalReachInteractEntity;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import eu.testar.iv4xr.labrecruits.LabRecruitsAgentTESTAR;
import eu.testar.iv4xr.labrecruits.LabRecruitsProcess;
import eu.iv4xr.framework.extensions.pathfinding.SurfaceNavGraph;
import eu.iv4xr.framework.spatial.Vec3;
import nl.ou.testar.RandomActionSelector;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;
import world.LabWorldModel;

public class LabRecruitsProtocol extends iv4xrProtocol {

	// Navigable State that an agent can explore
	private iv4xrNavigableState navigableState = new iv4xrNavigableState("");
	private iv4xrNavigableStateMap memoryNavigableStateMap = new iv4xrNavigableStateMap();
	private String lastInteractActionAbstractIDCustom;

	private PrimitiveGoal previousGoal;
	private int triesGoalInExecution = 0;

	protected SurfaceNavGraph navGraph;

	/**
	 * Called once during the life time of TESTAR
	 * This method can be used to perform initial setup work
	 * @param   settings  the current TESTAR settings as specified by the user.
	 */
	@Override
	protected void initialize(Settings settings) {
		// Start iv4xr plugin (Windows + LabRecruitsEnvironment)
		NativeLinker.addiv4XRLab();

		super.initialize(settings);

		// Define existing agent to fetch his observation entities
		agentId = settings.get(ConfigTags.AgentId);
		IV4XRStateFetcher.agentsIds = new HashSet<>(Arrays.asList(agentId));
		LabRecruitsProcess.agentId = agentId;

		// Set if LabRecruits system should be executed with the Graphics mode
		LabRecruitsProcess.labRecruitsGraphics = settings.get(ConfigTags.LabRecruitsGraphics);
	}

	/**
	 * This methods is called before each test sequence, allowing for example using external profiling software on the SUT
	 */
	@Override
	protected void preSequencePreparations() {
		super.preSequencePreparations();
		lastInteractActionAbstractIDCustom = "Initial";
	}

	/**
	 * This method is invoked each time the TESTAR starts the SUT to generate a new sequence.
	 * This can be used for example for bypassing a login screen by filling the username and password
	 * or bringing the system into a specific start state which is identical on each start (e.g. one has to delete or restore
	 * the SUT's configuration files etc.)
	 */
	@Override
	protected void beginSequence(SUT system, State state) {
		super.beginSequence(system, state);
		// Indicate to the middle LabRecruits Environment if TESTAR will listen the actions
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setEnabledIV4XRAgentListener(settings.get(ConfigTags.iv4XRAgentListener, false));
		// Set initial State and Actions
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setStateTESTAR(state);
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).setDerivedActionsTESTAR(deriveActions(system, state));
	}

	/**
	 * This method is called when the TESTAR requests the state of the SUT.
	 * Here you can add additional information to the SUT's state or write your
	 * own state fetching routine. The state should have attached an oracle
	 * (TagName: <code>Tags.OracleVerdict</code>) which describes whether the
	 * state is erroneous and if so why.
	 * @return  the current state of the SUT with attached oracle.
	 */
	@Override
	protected State getState(SUT system) throws StateBuildException {
		State state = super.getState(system);

		// Set the navMesh information in the State
		setNavMeshPositions(system, state);

		LabRecruitsAgentTESTAR testAgent = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		if(testAgent != null) {
			if(previousGoal != null && previousGoal.equals(testAgent.getCurrentGoal())) {
				triesGoalInExecution = triesGoalInExecution + 1;
			} else {
				triesGoalInExecution = 0;
			}
			previousGoal = testAgent.getCurrentGoal();
		}

		// Update Navigable State entities and navMesh positions information
		for(Widget w : latestState) {
			// Ignore the agent itself and the state
			if(w.equals(latestState.get(IV4XRtags.agentWidget)) || w.equals(latestState)) continue;
			// Add the visible entity information
			navigableState.addReachableEntity(w.get(IV4XRtags.entityId, ""), w.get(IV4XRtags.labRecruitsEntityIsActive, false));
		}
		// Add the visible and navigable navMesh nodes
		navigableState.addNavigableNode(latestState.get(IV4XRtags.labRecruitsNavMesh, Collections.<SVec3>emptySet()));

		return latestState;
	}

	/**
	 * Extract the visible nodes from the LabRecruits NavMesh information, 
	 * and set the information as a State IV4XR Tag. 
	 * 
	 * @param system
	 * @param state
	 */
	private void setNavMeshPositions(SUT system, State state) {
		LabRecruitsEnvironment labRecruitsEnvironment = system.get(IV4XRtags.iv4xrLabRecruitsEnvironment, null);

		// TODO: Creating the SurfaceNavGraph graph in the beginSequence method returns a null object
		// world.BeliefState.setEnvironment creates the pathfinder = new SurfaceNavGraph(e_.worldNavigableMesh,0.5f)
		if(navGraph == null && labRecruitsEnvironment != null) {
			navGraph = new SurfaceNavGraph(system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).worldNavigableMesh(), 0.5f);
		}

		Set<SVec3> navMeshNodes = new HashSet<>();
		// Set the NavMesh information to the state
		if(navGraph != null && labRecruitsEnvironment != null) {
			LabWorldModel labwom = labRecruitsEnvironment.observe(agentId);
			for(int nodeIndex : labwom.visibleNavigationNodes) {
				Vec3 nodePosition = navGraph.position(nodeIndex);
				navMeshNodes.add(new SVec3(nodePosition.x, nodePosition.y, nodePosition.z));
			}
		}

		state.set(IV4XRtags.labRecruitsNavMesh, navMeshNodes);
	}

	/**
	 * The getVerdict methods implements the online state oracles that
	 * examine the SUT's current state and returns an oracle verdict.
	 * @return oracle verdict, which determines whether the state is erroneous and why.
	 */
	@Override
	protected Verdict getVerdict(State state) {
		// Used to check if Agent gets stuck
		if(triesGoalInExecution > 200) {
			return new Verdict(Verdict.SEVERITY_WARNING, "Warning: Same Agent Goal executed 200 times");
		}

		/*try {
			GoalLibListener.getFirstGoalActionFromList();
		} catch(Exception e) {
			return new Verdict(Verdict.SEVERITY_WARNING, "Warning: Problems with GoalLibListener: " + e.getMessage());
		}*/

		// No verdicts implemented for now.
		return Verdict.OK;
	}

	/**
	 * Select one of the available actions using an action selection algorithm (for example random action selection)
	 *
	 * @param state the SUT's current state
	 * @param actions the set of derived actions
	 * @return the selected action (non-null!)
	 */
	@Override
	protected Action selectAction(State state, Set<Action> originalActions){
		//Call the preSelectAction method from the AbstractProtocol so that, if necessary,
		//unwanted processes are killed and SUT is put into foreground.
		Action retAction = preSelectAction(state, originalActions);
		if(retAction == null) {
			//using the action selector of the state model:
			retAction = stateModelManager.getAbstractActionToExecute(originalActions);
		}
		if(retAction == null) {
			System.out.println("State model based action selection did not find an action. Using random action selection.");
			retAction = RandomActionSelector.selectAction(originalActions);
		}

		return retAction;
	}

	/**
	 * Execute the selected action.
	 * @param system the SUT
	 * @param state the SUT's current state
	 * @param action the action to execute
	 * @return whether or not the execution succeeded
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

			notifyNavigableStateAfterAction(system, action);

			return true;

		}catch(ActionFailedException afe){
			return false;
		}
	}

	private Widget getEntityWidgetFromState(SUT system, String entityId) {
		Util.pause(2);
		// User super getState to avoid navigableState conflicts
		for(Widget w : super.getState(system)) {
			if(w.get(IV4XRtags.entityId, "").equals(entityId)) {
				return w;
			}
		}
		return null;
	}

	/**
	 * After executing an interact action with an entity, this will create a Navigable State in the model. 
	 * 
	 * @param system
	 * @param action
	 */
	protected void notifyNavigableStateAfterAction(SUT system, Action action) {
		//TODO: Improve this way to detect interactive actions
		String actionType = action.getClass().getSimpleName();
		if(actionType.contains("Interact")) {
			// Then create a new navigable state object based in the current interacted entity
			String interactedEntity = "Unknown";
			if(action instanceof labActionCommandMoveInteract) interactedEntity = ((labActionCommandMoveInteract) action).getEntityId();
			else if(action instanceof labActionCommandInteract) interactedEntity = ((labActionCommandInteract) action).getEntityId();
			else if(action instanceof labActionGoalReachInteractEntity) interactedEntity = ((labActionGoalReachInteractEntity) action).getEntityId();

			String beforeIsActive = action.get(Tags.OriginWidget).get(IV4XRtags.labRecruitsEntityIsActive).toString();
			String afterIsActive = "Unknown";
			Widget afterWidget = getEntityWidgetFromState(system, interactedEntity);
			if(afterWidget != null) {
				afterIsActive = afterWidget.get(IV4XRtags.labRecruitsEntityIsActive).toString();
			}

			String interactionInfo = "Entity:" + interactedEntity + ",From:" + beforeIsActive + ",To:" + afterIsActive;

			// Add explored navigable state in our memory debugging map
			memoryNavigableStateMap.addNavigableState(navigableState);
			// Create a Navigable State in the State Model
			stateModelManager.notifyNewNavigableState(navigableState.getNavigableNodes(), 
					navigableState.getReachableEntities(), 
					interactionInfo,
					action.get(Tags.AbstractIDCustom));

			navigableState = new iv4xrNavigableState("");

			// Update lastInteractAction for the finishSequence case
			lastInteractActionAbstractIDCustom = action.get(Tags.AbstractIDCustom);
		}
	}

	/**
	 * This method is invoked each time the TESTAR has reached the stop criteria for generating a sequence.
	 * This can be used for example for graceful shutdown of the SUT, maybe pressing "Close" or "Exit" button
	 */
	@Override
	protected void finishSequence() {
		// Add last explored navigable state in our memory debugging map
		memoryNavigableStateMap.addNavigableState(navigableState);
		//TODO: Do we want to save last navigable state if is not complete?
		// Create last Navigable State in the State Model
		stateModelManager.notifyNewNavigableState(navigableState.getNavigableNodes(), 
				navigableState.getReachableEntities(), 
				"NotExecutedAction",
				lastInteractActionAbstractIDCustom);

		// Print debugging memoryNavigableStateMap information
		String navigableStateMapInfo = memoryNavigableStateMap.toString();
		System.out.println(navigableStateMapInfo);
		try {
			File outputFolder = new File(OutputStructure.outerLoopOutputDir).getCanonicalFile();
			String outputFile = outputFolder.getPath() + File.separator + "navigableStateMapInfo.txt";
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));
			writer.append(navigableStateMapInfo);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("ERROR: Writing navigableStateMapInfo results");
		}
	}

	/**
	 * Here you can put graceful shutdown sequence for your SUT
	 * @param system
	 */
	@Override
	protected void stopSystem(SUT system) {
		system.get(IV4XRtags.iv4xrLabRecruitsEnvironment).close();
		super.stopSystem(system);
	}

	/**
	 * This method is called after the last sequence, to allow for example handling the reporting of the session
	 */
	@Override
	protected void closeTestSession() {
		super.closeTestSession();
		NativeLinker.cleaniv4XRLab();
	}

	/**
	 * Determine if the iv4xr Widget Entity is Interactive. 
	 * Now checking if it is a Door or Switch entity
	 * 
	 * @param widget (Entity)
	 * @return yes or not
	 */
	protected boolean isInteractiveEntity(Widget widget) {
		return (widget.get(IV4XRtags.entityType, null) != null &&
				// TODO: create internal element property entityInteractive
				(widget.get(IV4XRtags.entityType, null).toString().equals("Door")
						|| widget.get(IV4XRtags.entityType, null).toString().equals("Switch")));
	}

	/**
	 * Determine if the iv4xr Agent or TESTAR as Agent is in a suitable distance.
	 * 
	 * @param system
	 * @param widget (Entity)
	 * @param maxDistance
	 * @return yes or no
	 */
	protected boolean isAgentCloseToEntity(SUT system, Widget widget, double maxDistance) {
		// Agent Widget exists/detected
		if (Objects.isNull(system.get(IV4XRtags.agentWidget, null)))
			return false;
		// Agent Widget has a position
		if(Objects.isNull(system.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition, null)))
			return false;
		// Entity Widget has a position
		if(Objects.isNull(widget.get(IV4XRtags.entityPosition, null)))
			return false;

		return (Vec3.dist(system.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition), widget.get(IV4XRtags.entityPosition)) < maxDistance);
	}

	/**
	 * Future implementation to determine if Agent found a hazardous Entity
	 */
	protected boolean hazardousEntityFound() {
		return false;
	}

	/**
	 * Use the LabRecruits navigational mesh to obtain a navigational graph that contains 
	 * information about all existing node id's + Vec3 positions in the LabRecruits system. 
	 * 
	 * Then use the LabWOM to get the visible navigational nodes for the current agent positions, 
	 * and create one TESTAR exploration movement action for each visible node.
	 * 
	 * @param actions
	 * @param state
	 * @param labRecruitsEnvironment
	 * @param agentId
	 * @return explore navmesh actions
	 */
	protected Set<Action> exploreVisibleNodesActions(Set<Action> actions, State state, LabRecruitsEnvironment labRecruitsEnvironment, String agentId) {
		if(state.get(IV4XRtags.labRecruitsNavMesh, null) != null && !state.get(IV4XRtags.labRecruitsNavMesh).isEmpty()) {
			for(SVec3 nodeNavMesh : state.get(IV4XRtags.labRecruitsNavMesh)) {
				actions.add(new labActionExplorePosition(
						state.get(IV4XRtags.agentWidget), 
						state, 
						labRecruitsEnvironment, 
						agentId, 
						new Vec3(nodeNavMesh.x, nodeNavMesh.y, nodeNavMesh.z), false, false));
			}
		}

		return actions;
	}

	protected Set<Action> exploreGoalNodePositions(Set<Action> actions, State state, SUT system) {
		if(state.get(IV4XRtags.labRecruitsNavMesh, null) != null && !state.get(IV4XRtags.labRecruitsNavMesh).isEmpty()) {
			for(SVec3 nodeNavMesh : state.get(IV4XRtags.labRecruitsNavMesh)) {
				labActionGoalPositionInCloseRange newAction = new labActionGoalPositionInCloseRange(
						state.get(IV4XRtags.agentWidget), 
						state, 
						system, 
						new Vec3(nodeNavMesh.x, nodeNavMesh.y, nodeNavMesh.z));
				actions.add(newAction);
			}
		}

		return actions;
	}

	@Override
	protected Canvas buildCanvas() {
		// Force TESTAR to return the Windows Canvas implementation
		return GDIScreenCanvas.fromPrimaryMonitor(Pen.PEN_DEFAULT);
	}

	/**
	 * Visualize all observed LabRecruits entities or NavMesh position nodes, 
	 * on which we have the possibility to move toward or try to interact with. 
	 *
	 * @param canvas
	 * @param state
	 * @param actions
	 */
	protected void visualizeActions(Canvas canvas, State state, Set<Action> actions){
		if(settings.get(ConfigTags.VisualizeActions, false)) {
			Iv4xrLabRecruitsVisualization.showStateElements(cv, state, state.get(IV4XRtags.agentWidget, null), 
					settings.get(ConfigTags.SpyIncrement, 0));
		}
	}

	/**
	 * Visualize selected movement or interaction LabRecruits action. 
	 * 
	 * @param canvas
	 * @param state
	 * @param action
	 */
	protected void visualizeSelectedAction(Canvas canvas, State state, Action action) {
		if(settings.get(ConfigTags.VisualizeActions, false)) {
			Iv4xrLabRecruitsVisualization.showSelectedElement(cv, state, state.get(IV4XRtags.agentWidget, null), 
					action, settings.get(ConfigTags.SpyIncrement, 0));
		}
	}

	/**
	 * Method to run TESTAR on Spy Mode.
	 */
	@Override
	protected void runSpyLoop() {
		// Verify that user is executing LabRecruits with the Graphics mode
		if(!settings.get(ConfigTags.LabRecruitsGraphics, false) || !System.getProperty("os.name").contains("Windows")) {
			System.err.println("If you want to use TESTAR Spy mode, ");
			System.err.println("you need to execute LabRecruits with the Graphics mode");
			System.err.println("in a Windows (10, 2016, 2019) Environment");
			return;
		} else {
			System.out.println("Running: TESTAR Spy Mode with LabRecruits SUT");
		}

		//Create or detect the SUT & build canvas representation
		SUT system = startSystem();
		this.cv = buildCanvas();

		//moveLabRecruitsCamera(system);

		while(mode() == Modes.Spy && system.isRunning()) {
			State state = getState(system);
			cv.begin(); Util.clear(cv);

			//Draw the state information in the canvas
			try {
				Iv4xrLabRecruitsVisualization.showStateObservation(cv, state);
				Iv4xrLabRecruitsVisualization.showStateElements(cv, state, system.get(IV4XRtags.agentWidget, null), settings.get(ConfigTags.SpyIncrement, 0));
			} catch (Exception e) {
				System.out.println("WARNING: Trying to launch Iv4xrVisualization");
				e.printStackTrace();
			}

			cv.end();

			int msRefresh = (int)(settings.get(ConfigTags.RefreshSpyCanvas, 0.5) * 1000);
			synchronized (this) {
				try {
					this.wait(msRefresh);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		//If user closes the SUT while in Spy-mode, TESTAR will close (or go back to SettingsDialog):
		if(!system.isRunning()){
			this.mode = Modes.Quit;
		}

		Util.clear(cv);
		cv.end();

		//Stop and close the SUT 
		stopSystem(system);
	}
}

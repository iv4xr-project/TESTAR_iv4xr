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

package org.testar.protocols.iv4xr;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.io.File;
import java.util.Arrays;
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
import org.fruit.alayer.devices.AWTMouse;
import org.fruit.alayer.devices.Mouse;
import org.fruit.alayer.exceptions.ActionBuildException;
import org.fruit.alayer.exceptions.StateBuildException;
import org.fruit.alayer.windows.GDIScreenCanvas;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.testar.OutputStructure;
import org.testar.protocols.GenericUtilsProtocol;
import org.testar.visualization.iv4xr.Iv4xrLabRecruitsVisualization;

import com.google.common.collect.Sets;

import environments.LabRecruitsEnvironment;
import es.upv.staq.testar.CodingManager;
import es.upv.staq.testar.NativeLinker;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.actions.lab.commands.labActionExplorePosition;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import eu.testar.iv4xr.labrecruits.LabRecruitsAgentTESTAR;
import eu.testar.iv4xr.labrecruits.LabRecruitsProcess;
import eu.iv4xr.framework.extensions.pathfinding.SurfaceNavGraph;
import eu.iv4xr.framework.spatial.Vec3;
import nl.ou.testar.RandomActionSelector;
import nl.ou.testar.HtmlReporting.HtmlSequenceReport;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;
import world.LabWorldModel;

public class LabRecruitsProtocol extends GenericUtilsProtocol {

	protected HtmlSequenceReport htmlReport;
	protected State latestState;
	protected String lastInteractActionAbstractIDCustom;

	// Agent point of view that will Observe and extract Widgets information
	protected String agentId = "agent1";

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

		if(this.mode == Modes.Record || this.mode == Modes.Replay) {
			System.out.println("*************************************************************");
			System.out.println("Dear User,");
			System.out.println("Current TESTAR implementation does not allow the tool to use");
			System.out.println("Execution mode: " + this.mode.toString());
			System.out.println("You can use Generate or View mode");
			System.out.println("Stopping TESTAR execution...");
			System.out.println("*************************************************************");
			Runtime.getRuntime().exit(0);
		}

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
		//initializing the HTML sequence report:
		htmlReport = new HtmlSequenceReport();
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

		// Find the Widget that represents the Agent Entity and associated into the IV4XR SUT Tag
		for(Widget w : state) {
			if(w.get(IV4XRtags.entityType, "").equals("AGENT")) {
				system.set(IV4XRtags.agentWidget, w);
				state.set(IV4XRtags.agentWidget, w);
				break;
			}
		}

		// Set the navMesh information in the State
		setNavMeshPositions(system, state);

		//Spy mode didn't use the html report
		if(settings.get(ConfigTags.Mode) == Modes.Spy) {
			return state;
		}

		latestState = state;

		//adding state to the HTML sequence report:
		htmlReport.addState(latestState);

		LabRecruitsAgentTESTAR testAgent = (LabRecruitsAgentTESTAR)system.get(IV4XRtags.iv4xrTestAgent);
		if(testAgent != null) {
			if(previousGoal != null && previousGoal.equals(testAgent.getCurrentGoal())) {
				triesGoalInExecution = triesGoalInExecution + 1;
			} else {
				triesGoalInExecution = 0;
			}
			previousGoal = testAgent.getCurrentGoal();
		}

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
	 * This method is used by TESTAR to determine the set of currently available actions.
	 * You can use the SUT's current state, analyze the widgets and their properties to create
	 * a set of sensible actions, such as: "Click every Button which is enabled" etc.
	 * The return value is supposed to be non-null. If the returned set is empty, TESTAR
	 * will stop generation of the current action and continue with the next one.
	 * @param system the SUT
	 * @param state the SUT's current state
	 * @return  a set of actions
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) throws ActionBuildException {
		//The super method returns a ONLY actions for killing unwanted processes if needed, or bringing the SUT to
		//the foreground. You should add all other actions here yourself.
		// These "special" actions are prioritized over the normal GUI actions in selectAction() / preSelectAction().
		return super.deriveActions(system,state);
	}

	/**
	 * Overwriting to add HTML report writing into it
	 *
	 * @param state
	 * @param actions
	 * @return
	 */
	@Override
	protected Action preSelectAction(State state, Set<Action> actions){
		// adding available actions into the HTML report:
		htmlReport.addActions(actions);
		return(super.preSelectAction(state, actions));
	}

	/**
	 * Select one of the available actions (e.g. at random)
	 * @param state the SUT's current state
	 * @param actions the set of derived actions
	 * @return  the selected action (non-null!)
	 */
	@Override
	protected Action selectAction(State state, Set<Action> actions){
		//Call the preSelectAction method from the DefaultProtocol so that, if necessary,
		//unwanted processes are killed and SUT is put into foreground.
		Action retAction = preSelectAction(state, actions);
		if (retAction == null) {
			//if no preSelected actions are needed, then implement your own strategy
			retAction = RandomActionSelector.selectAction(actions);
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
		// adding the action that is going to be executed into HTML report:
		htmlReport.addSelectedAction(state, action);
		return super.executeAction(system, state, action);
	}

	/**
	 * TESTAR notifies the State Model automatically after derive and select actions.
	 * But if we are listening the iv4xr Agent action selection,
	 * we need to change the internal notification flow
	 */
	@Override
	protected void notifyNewStateReachedToStateModel(State newState, Set<Action> actions) {
		// If TESTAR is listening iv4xr Agent actions
		if(settings.get(ConfigTags.iv4XRAgentListener, false)) {
			//Nothing, we are going to take control of this invocation after Agent Tactics decision
		} else {
			super.notifyNewStateReachedToStateModel(newState, actions);
		}
	}
	@Override
	protected void notifyActionToStateModel(Action action){
		// If TESTAR is listening iv4xr Agent actions
		if(settings.get(ConfigTags.iv4XRAgentListener, false)) {
			//Nothing, we are going to take control of this invocation after Agent Tactics decision
		} else {
			super.notifyActionToStateModel(action);
		}
	}

	/**
	 * Invoke this notification after Map TESTAR State with Agent observation
	 */
	protected void notifyLabAgentStateToStateModel(State newState, Set<Action> actions) {
		stateModelManager.notifyNewStateReached(newState, actions);
	}

	/**
	 * Invoke this notification after know Agent Tactics Action decision
	 */
	protected void notifyLabAgentActionToStateModel(SUT system, State state, Action action) {
		if(action.get(Tags.AbstractIDCustom, null) == null) {
			CodingManager.buildIDs(state, Sets.newHashSet(action));
		}
		stateModelManager.notifyListenedAction(action);
	}

	/**
	 * This method is invoked each time the TESTAR has reached the stop criteria for generating a sequence.
	 * This can be used for example for graceful shutdown of the SUT, maybe pressing "Close" or "Exit" button
	 */
	@Override
	protected void finishSequence() {
		//
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
	 * This methods is called after each test sequence, allowing for example using external profiling software on the SUT
	 */
	@Override
	protected void postSequenceProcessing() {
		htmlReport.addTestVerdict(getVerdict(latestState).join(processVerdict));

		String sequencesPath = getGeneratedSequenceName();
		try {
			sequencesPath = new File(getGeneratedSequenceName()).getCanonicalPath();
		}catch (Exception e) {}

		String status = (getVerdict(latestState).join(processVerdict)).verdictSeverityTitle();
		String statusInfo = (getVerdict(latestState).join(processVerdict)).info();

		statusInfo = statusInfo.replace("\n"+Verdict.OK.info(), "");

		//Timestamp(generated by logger) SUTname Mode SequenceFileObject Status "StatusInfo"
		INDEXLOG.info(OutputStructure.executedSUTname
				+ " " + settings.get(ConfigTags.Mode, mode())
				+ " " + sequencesPath
				+ " " + status + " \"" + statusInfo + "\"" );

		htmlReport.close();
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
		if(state.get(IV4XRtags.labRecruitsNavMesh, null) != null && !state.get(IV4XRtags.labRecruitsNavMesh).isEmpty() /*&& navGraph != null*/) {
			for(SVec3 nodeNavMesh : state.get(IV4XRtags.labRecruitsNavMesh)) {
				actions.add(
						new labActionExplorePosition(state.get(IV4XRtags.agentWidget), state, labRecruitsEnvironment, agentId, 
								new Vec3(nodeNavMesh.x, nodeNavMesh.y, nodeNavMesh.z), false, false));
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

	/**
	 * Automatically click in the middle of the LabRecruits app 
	 * to move the camera so that the level is seen from above
	 * @param system
	 */
	private void moveLabRecruitsCamera(SUT system) {
		State state = getState(system);
		Mouse mouse = AWTMouse.build();

		// Move the mouse to the center of the LabRecruits application
		double centerX = state.get(Tags.Shape).x() + (state.get(Tags.Shape).width() / 2);
		double centerY = state.get(Tags.Shape).y() + (state.get(Tags.Shape).height() / 2);
		Util.moveCursor(mouse, centerX, centerY, Math.max(0.5, 0.5));

		// Then press Right Mouse button and move the mouse to the bottom
		//TODO: Debug why this is not working with LabRecruits but works with Paint (check admin privileges or Unity event detection?)
		try {
			Robot robot = new Robot();
			Thread.sleep(500);
			robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
			Thread.sleep(500);
			//mouse.press(MouseButtons.BUTTON2);
			Util.moveCursor(mouse, mouse.cursor().x(), mouse.cursor().y() + 300, Math.max(0.5, 0.5));
			//mouse.release(MouseButtons.BUTTON2);
			Thread.sleep(500);
			robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
		} catch (AWTException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

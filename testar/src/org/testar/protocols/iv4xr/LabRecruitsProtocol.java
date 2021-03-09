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


package org.testar.protocols.iv4xr;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionBuildException;
import org.fruit.alayer.exceptions.StateBuildException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;
import org.testar.OutputStructure;
import org.testar.protocols.GenericUtilsProtocol;

import com.google.common.collect.Sets;

import environments.LabRecruitsEnvironment;
import es.upv.staq.testar.CodingManager;
import es.upv.staq.testar.NativeLinker;
import eu.testar.iv4xr.IV4XRProtocolUtil;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.actions.commands.labActionExplorePosition;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.labrecruits.LabRecruitsAgentTESTAR;
import eu.testar.iv4xr.labrecruits.listener.GoalLibListener;
import eu.iv4xr.framework.spatial.Vec3;
import nl.ou.testar.RandomActionSelector;
import nl.ou.testar.HtmlReporting.HtmlSequenceReport;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;

public class LabRecruitsProtocol extends GenericUtilsProtocol {

	protected HtmlSequenceReport htmlReport;
	protected State latestState;

	// Agent point of view that will Observe and extract Widgets information
	protected String agentId = "agent1";
	
	protected LabRecruitsAgentTESTAR testAgent;
	private PrimitiveGoal previousGoal;
	private int triesGoalInExecution = 0;

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
		
		if(this.mode == Modes.Spy || this.mode == Modes.Record || this.mode == Modes.Replay) {
			System.out.println("*************************************************************");
			System.out.println("Dear User,");
			System.out.println("Current TESTAR implementation does not allow the tool to use");
			System.out.println("Execution mode: " + this.mode.toString());
			System.out.println("You can use Generate or View mode");
			System.out.println("Stopping TESTAR execution...");
			System.out.println("*************************************************************");
			Runtime.getRuntime().exit(0);
		}

		// Currently an utility protocol to take SUT screenshots
		protocolUtil = new IV4XRProtocolUtil();

		// Define existing agent to fetch his observation entities
		IV4XRStateFetcher.agentsIds = new HashSet<>(Arrays.asList(agentId));
	}

	/**
	 * This methods is called before each test sequence, allowing for example using external profiling software on the SUT
	 */
	@Override
	protected void preSequencePreparations() {
		//initializing the HTML sequence report:
		htmlReport = new HtmlSequenceReport();
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
		//Spy mode didn't use the html report
		if(settings.get(ConfigTags.Mode) == Modes.Spy)
			return super.getState(system);

		latestState = super.getState(system);
		//adding state to the HTML sequence report:
		htmlReport.addState(latestState);
		
		// Find the Widget that represents the Agent Entity and associated into the IV4XR SUT Tag
		for(Widget w : latestState) {
			if(w.get(IV4XRtags.entityType, "").equals("AGENT")) {
				system.set(IV4XRtags.agentWidget, w);
			}
		}
		
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
		NativeLinker.cleaniv4XRLab();
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
		if(Objects.isNull(system.get(IV4XRtags.agentWidget).get(IV4XRtags.entityPosition, null)))
			return false;
		// Entity Widget has a position
		if(Objects.isNull(widget.get(IV4XRtags.entityPosition, null)))
			return false;

		return (Vec3.dist(system.get(IV4XRtags.agentWidget).get(IV4XRtags.entityPosition), widget.get(IV4XRtags.entityPosition)) < maxDistance);
	}
	
	/**
	 * Future implementation to determine if Agent found a hazardous Entity
	 */
	protected boolean hazardousEntityFound() {
		return false;
	}
	
	protected static Set<Action> exploreVisibleNodesActions(Set<Action> actions, State state, LabRecruitsEnvironment labRecruitsEnvironment, String agentId) {
		ArrayList<Vec3> visibleNavigationNodes = labRecruitsEnvironment.worldNavigableMesh.vertices;
		for(Vec3 nodePosition : visibleNavigationNodes) {
			actions.add(new labActionExplorePosition(state, labRecruitsEnvironment, agentId, nodePosition, false, false));
		}
		
		return actions;
	}
}
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


package org.testar.protocols.iv4xr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.fruit.Util;
import org.fruit.alayer.Action;
import org.fruit.alayer.Canvas;
import org.fruit.alayer.Pen;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionBuildException;
import org.fruit.alayer.exceptions.StateBuildException;
import org.fruit.alayer.windows.GDIScreenCanvas;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Main;
import org.fruit.monkey.Settings;
import org.testar.OutputStructure;
import org.testar.protocols.GenericUtilsProtocol;
import org.testar.visualization.iv4xr.Iv4xrSeVisualization;

import com.google.common.collect.Sets;

import es.upv.staq.testar.CodingManager;
import es.upv.staq.testar.NativeLinker;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.se.SpaceEngineersProcess;
import eu.iv4xr.framework.spatial.Vec3;
import nl.ou.testar.RandomActionSelector;
import nl.ou.testar.HtmlReporting.HtmlSequenceReport;
import spaceEngineers.transport.SocketReaderWriterKt;

public class SEProtocol extends GenericUtilsProtocol {

	protected HtmlSequenceReport htmlReport;
	protected State latestState;

	// Helper dynamic time stamp variable used to read only last SE log messages (2021-06-02 17:59:05.334)
	private final String SE_LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	private String lastTimeStampLogSE = new SimpleDateFormat(SE_LOG_DATE_FORMAT).format(new Date());

	// Agent point of view that will Observe and extract Widgets information
	protected String agentId = "you";

	/**
	 * Called once during the life time of TESTAR
	 * This method can be used to perform initial setup work
	 * @param   settings  the current TESTAR settings as specified by the user.
	 */
	@Override
	protected void initialize(Settings settings) {
		// Start SE iv4xr plugin
		NativeLinker.addiv4XRSe();

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
		SpaceEngineersProcess.characterControllerId = agentId;
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
		//TODO: Implement Listener stuff like LabRecruitsEnvironment
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
		Widget agentWidget = getAgentEntityFromState(state);
		system.set(IV4XRtags.agentWidget, agentWidget);
		state.set(IV4XRtags.agentWidget, agentWidget);

		//Spy mode didn't use the html report
		if(settings.get(ConfigTags.Mode) == Modes.Spy) {
			return state;
		}

		latestState = state;

		//adding state to the HTML sequence report:
		htmlReport.addState(latestState);

		return latestState;
	}

	/**
	 * Get the Widget that represents the Agent Entity. 
	 * 
	 * @param state
	 * @return agent widget
	 */
	protected final Widget getAgentEntityFromState(State state) {
		for(Widget w : state) {
			if(w.get(IV4XRtags.entityType, "").equals("AGENT")) {
				return w;
			}
		}
		throw new StateBuildException("Space Engineers state does not contain an Agent Widget");
	}

	/**
	 * The getVerdict methods implements the online state oracles that
	 * examine the SUT's current state and returns an oracle verdict.
	 * @return oracle verdict, which determines whether the state is erroneous and why.
	 */
	@Override
	protected Verdict getVerdict(State state){
		// The super methods implements the implicit online state oracles for: (system crashes, non-responsiveness, suspicious titles)
		Verdict verdict = super.getVerdict(state);

		// Check SE logs trying to find pattern messages (ConfigTags.ProcessLogs)
		File seLog = getLastSpaceEngineersLog();
		if(seLog != null && settings.get(ConfigTags.ProcessLogsEnabled, false)) {
			// Get all errors from the SE log
			LinkedList<String> logErrors = spaceEngineersLogVerdict(seLog);
			// And updated them to only use the new ones compared with the last verdict iteration
			logErrors = getErrorMessagesBasedOnTimeStamp(logErrors);
			if(!logErrors.isEmpty()) {
				logErrors.addFirst(String.format("Warning Log messages on File %s", seLog));
				htmlReport.addWarningMessage(logErrors);
			}
		}

		// Update current time stamp to avoid repeating warnings
		lastTimeStampLogSE = new SimpleDateFormat(SE_LOG_DATE_FORMAT).format(new Date());

		return verdict;
	}

	/**
	 * Use the lastTimeStampLogSE helper variable to get only new error messages. 
	 * 
	 * @param logsErrors
	 * @return
	 */
	private LinkedList<String> getErrorMessagesBasedOnTimeStamp(LinkedList<String> logsErrors){
		// If empty do nothing
		if(logsErrors.isEmpty()) return logsErrors;
		LinkedList<String> newLogsErrors = new LinkedList<>();
		try {
			for(String logMessage : logsErrors) {
				// Check that the date of the log error message is new compared with the previous time stamp
				if(new SimpleDateFormat(SE_LOG_DATE_FORMAT).parse(logMessage.substring(0, 23)).after(new SimpleDateFormat(SE_LOG_DATE_FORMAT).parse(lastTimeStampLogSE))) {
					newLogsErrors.add(logMessage);
				}
			}
		} catch(ParseException p) {
			System.err.println("ERROR: Parsing SpaceEngineers Logs Dates: " + p.getMessage());
		}
		return newLogsErrors;
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
	protected void notifySEAgentStateToStateModel(State newState, Set<Action> actions) {
		stateModelManager.notifyNewStateReached(newState, actions);
	}

	/**
	 * Invoke this notification after know Agent Tactics Action decision
	 */
	protected void notifySEAgentActionToStateModel(SUT system, State state, Action action) {
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
		// If we use SUTConnector command line, we want to kill the process and start a new one next sequence
		if(settings.get(ConfigTags.SUTConnector).equals(Settings.SUT_CONNECTOR_CMDLINE)) {
			super.finishSequence();
		}
		// SpaceEngineers Logs Verdict, check SE logs trying to find pattern messages (ConfigTags.ProcessLogs)
		Verdict logVerdict = Verdict.OK;
		File seLog = getLastSpaceEngineersLog();
		if(seLog != null && settings.get(ConfigTags.ProcessLogsEnabled, false)) {
			System.out.println("INFO: SpaceEngineers Log detected: " + seLog);
			LinkedList<String> logOracles = spaceEngineersLogVerdict(seLog);
			if(!logOracles.isEmpty()) {
				String verdictMessage = String.format("Suspicious Log messages on File %s", seLog);
				String newLine = System.getProperty("line.separator");
				for(String s : logOracles) {verdictMessage = verdictMessage.concat(newLine + s);}
				logVerdict = new Verdict(Verdict.SEVERITY_WARNING, verdictMessage);
				processVerdict = processVerdict.join(logVerdict);
			}
		}
	}

	/**
	 * Reverse ordering of SpaceEngineers files to return the latest "SpaceEngineers" Log. 
	 * C:\Users\<username>\AppData\Roaming\SpaceEngineers\
	 * 
	 * @return
	 */
	private File getLastSpaceEngineersLog() {
		File dir = new File("C:\\Users\\" + System.getProperty("user.name") + "\\AppData\\Roaming\\SpaceEngineers");
		if (dir.isDirectory()) {
			File[] dirFiles = dir.listFiles((FileFilter)FileFilterUtils.fileFileFilter());
			if (dirFiles != null && dirFiles.length > 0) {
				Arrays.sort(dirFiles, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
			}
			for(int i = 0; i < dirFiles.length; i++) {
				if(dirFiles[i].getName().contains("SpaceEngineers") && dirFiles[i].getName().contains(".log")) {
					return dirFiles[i];
				}
			}
		}

		return null;
	}

	/**
	 * Read one specific SpaceEngineers Log and use ConfigTags.ProcessLogs pattern to find error messages.
	 * 
	 * @param seLog
	 * @return
	 */
	private LinkedList<String> spaceEngineersLogVerdict(File seLog){
		LinkedList<String> logOracles = new LinkedList<>();
		try{
			FileInputStream fstream = new FileInputStream(seLog);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				if(Pattern.matches(settings.get(ConfigTags.ProcessLogs), strLine)) {
					logOracles.add(strLine);
				}
			}
			fstream.close();
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}

		return logOracles;
	}

	/**
	 * Here you can put graceful shutdown sequence for your SUT
	 * @param system
	 */
	@Override
	protected void stopSystem(SUT system) {
		// Save the level as a result in the output folder (only for generate mode)
		if(settings.get(ConfigTags.Mode).equals(Modes.Generate)) {
			saveLevel(system);
		}
		SocketReaderWriterKt.closeIfCloseable(system.get(IV4XRtags.iv4xrSpaceEngineers));
		super.stopSystem(system);
	}

	private void saveLevel(SUT system) {
		// Save the level as the sequence number + execution date
		String saveAsName = Integer.toString(OutputStructure.sequenceInnerLoopCount) + "_" + OutputStructure.startInnerLoopDateString;

		// Save the result level in the default SE directory
		system.get(IV4XRtags.iv4xrSpaceEngineers).getScreens().getGamePlay().showMainMenu();
		Util.pause(1);
		system.get(IV4XRtags.iv4xrSpaceEngineers).getScreens().getMainMenu().saveAs();
		Util.pause(1);
		system.get(IV4XRtags.iv4xrSpaceEngineers).getScreens().getSaveAs().setName(saveAsName);
		Util.pause(1);
		system.get(IV4XRtags.iv4xrSpaceEngineers).getScreens().getSaveAs().pressOk();
		Util.pause(5);

		// Then move to TESTAR output results
		try {
			// testar\suts\se_levels\saveAsName
			File savesDir = new File(Main.testarDir + File.separator + "suts" + File.separator + "se_levels" + File.separator + saveAsName);
			// testar\output\2022-08-30_09h52m07s_SUT\se_level_output\sequenceX_innerDate
			File testarOutputDir = new File(OutputStructure.outerLoopOutputDir + File.separator + "se_saves_output" + File.separator + saveAsName).getAbsoluteFile();
			FileUtils.moveDirectory(savesDir, testarOutputDir);
		} catch (IOException ioe) {
			System.err.println("Error saving SE level: " + saveAsName);
			ioe.printStackTrace();
		}
	}

	/**
	 * This method is called after the last sequence, to allow for example handling the reporting of the session
	 */
	@Override
	protected void closeTestSession() {
		NativeLinker.cleaniv4XRSe();
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
	 * 
	 * @param widget (Entity)
	 * @return yes or not
	 */
	protected boolean isInteractiveEntity(Widget widget) {
		//TODO: Implement stuff like LabRecruits
		return false;
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

	@Override
	protected Canvas buildCanvas() {
		// Force TESTAR to return the Windows Canvas implementation
		return GDIScreenCanvas.fromPrimaryMonitor(Pen.PEN_DEFAULT);
	}

	/**
	 * Method to run TESTAR on Spy Mode.
	 */
	@Override
	protected void runSpyLoop() {
		// Verify that user is executing Space Engineers in a Windows 10 env
		if(!System.getProperty("os.name").contains("Windows")) {
			System.err.println("If you want to use TESTAR Spy mode, ");
			System.err.println("you need to execute Space Engineers");
			System.err.println("in a Windows (10, 2016, 2019) Environment");
			return;
		} else {
			System.out.println("Running: TESTAR Spy Mode with Space Engineers SUT");
		}

		//Create or detect the SUT & build canvas representation
		SUT system = startSystem();
		this.cv = buildCanvas();

		while(mode() == Modes.Spy && system.isRunning()) {
			State state = getState(system);
			cv.begin(); Util.clear(cv);

			//Draw the state information in the canvas
			try {
				spaceEngineers.controller.SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
				spaceEngineers.controller.Observer seObserver = seController.getObserver();

				spaceEngineers.model.Block targetBlock;
				if((targetBlock = seObserver.observe().getTargetBlock()) != null) { Iv4xrSeVisualization.showSpaceEngineersAimingElement(cv, targetBlock); }
				else{ Iv4xrSeVisualization.showStateObservation(cv, state); }
				//Iv4xrSeVisualization.showStateElements(cv, state, system.get(IV4XRtags.agentWidget, null), settings.get(ConfigTags.SpyIncrement, 0));
			} catch (Exception e) {
				System.out.println("WARNING: Trying to launch Iv4xrSeVisualization");
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

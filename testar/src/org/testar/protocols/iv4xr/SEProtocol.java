/***************************************************************************************************
 *
 * Copyright (c) 2021 - 2023 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2021 - 2023 Open Universiteit - www.ou.nl
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
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
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
import org.fruit.alayer.Verdict;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.alayer.windows.GDIScreenCanvas;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Main;
import org.fruit.monkey.Settings;
import org.testar.OutputStructure;
import org.testar.iv4xr.InteractiveExplorerSE;
import org.testar.iv4xr.SpatialSequentialMap;
import org.testar.visualization.iv4xr.Iv4xrSeVisualization;

import es.upv.staq.testar.NativeLinker;
import eu.testar.iv4xr.IV4XRStateFetcher;
import eu.testar.iv4xr.actions.se.commands.seActionCommandCloseScreen;
import eu.testar.iv4xr.actions.se.goals.seActionExplorePosition;
import eu.testar.iv4xr.actions.se.goals.seActionNavigateToBlock;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.se.SpaceEngineersProcess;
import nl.ou.testar.SystemProcessHandling;

public class SEProtocol extends iv4xrProtocol {

	// Helper dynamic time stamp variable used to read only last SE log messages (2021-06-02 17:59:05.334)
	private final String SE_LOG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
	private String lastTimeStampLogSE = new SimpleDateFormat(SE_LOG_DATE_FORMAT).format(new Date());

	// The SE level that TESTAR is going to explore
	protected String SE_LEVEL_PATH = "";

	// Oracle example to validate that the block integrity decreases after a Grinder action
	protected Verdict functional_verdict = Verdict.OK;

	protected InteractiveExplorerSE actionSelectorSE = new InteractiveExplorerSE();

	// Timing variables
	protected Instant accumulativeActionTimePerSequence;

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
		super.preSequencePreparations();
		// Create a XML spatial map based on the desired SpaceEngineers level
		SpatialSequentialMap.prepareSpatialSequentialMap(SE_LEVEL_PATH);
		// reset the functional verdict for the new sequence
		functional_verdict = Verdict.OK;
		// Initialize the interactive selector
		actionSelectorSE = new InteractiveExplorerSE();
	}

	/**
	 * This method is called when TESTAR starts the System Under Test (SUT). The method should
	 * take care of
	 *   1) starting the SUT (you can use TESTAR's settings obtainable from <code>settings()</code> to find
	 *      out what executable to run)
	 *   2) waiting until the system is fully loaded and ready to be tested (with large systems, you might have to wait several
	 *      seconds until they have finished loading)
	 *
	 * @return  a started SUT, ready to be tested.
	 * @throws SystemStartException
	 */
	@Override
	protected SUT startSystem() throws SystemStartException {
		SUT system = super.startSystem();

		// Move SpaceEngineers to the foreground
		system.get(IV4XRtags.windowsProcess).toForeground();

		// If SpaceEngineers was not launched with command line, and no level is running
		if(!system.get(IV4XRtags.iv4xrSpaceEngineers).getScreens().getFocusedScreen().data().getName().equals("GamePlay")) {
			// Load the desired level to execute TESTAR
			system.get(IV4XRtags.iv4xrSpaceEngineers).getSession().loadScenario(new File(SE_LEVEL_PATH).getAbsolutePath());
			system.get(IV4XRtags.iv4xrSpaceEngineers).getScreens().waitUntilTheGameLoaded();
			Util.pause(5);
		}

		return system;
	}

	/**
	 * This method is called when the TESTAR requests the state of the SUT.
	 * Here you can add additional information to the SUT's state or write your
	 * own state fetching routine.
	 *
	 * super.getState(system) puts the state information also to the HTML sequence report
	 *
	 * @return  the current state of the SUT with attached oracle.
	 */
	@Override
	protected State getState(SUT system) {
		State state = super.getState(system);

		if(this.mode == Modes.Generate) {
			SpatialSequentialMap.updateAgentObservation(state);
		}

		return state;
	}

	/**
	 * This method is invoked each time the TESTAR starts the SUT to generate a new sequence.
	 * This can be used for example for bypassing a login screen by filling the username and password
	 * or bringing the system into a specific start state which is identical on each start (e.g. one has to delete or restore
	 * the SUT's configuration files etc.)
	 */
	@Override
	protected void beginSequence(SUT system, State state) {
		// This is the initial Spatial Coverage
		// After observing the first State without executing any action in the second 0
		SpatialSequentialMap.extractActionStepSpatialCoverage(0, 0);
		accumulativeActionTimePerSequence = Instant.now();
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
	 * Overwriting to add HTML report writing into it
	 *
	 * @param state
	 * @param actions
	 * @return
	 */
	@Override
	protected Action preSelectAction(State state, Set<Action> actions){
		// If the Space Engineers agent is on an unknown screen, close it
		if(state.get(IV4XRtags.agentWidget) != null && state.get(IV4XRtags.agentWidget).get(IV4XRtags.seUnknownScreen, false)) {
			System.out.println("DEBUG: Forcing Space Engineers to close current Screen");
			Action gamePlayAction = new seActionCommandCloseScreen(state.get(IV4XRtags.agentWidget), agentId);
			buildEnvironmentActionIdentifiers(state, gamePlayAction);
			// adding available actions into the HTML report:
			htmlReport.addActions(Collections.singleton(gamePlayAction));
			return gamePlayAction;
		}
		return super.preSelectAction(state, actions);
	}

	/**
	 * Execute the selected action.
	 * @param system the SUT
	 * @param state the SUT's current state
	 * @param action the action to execute
	 * @return whether or not the execution succeeded
	 */
	@Override
	protected boolean executeAction(SUT system, State state, Action action) {
		// adding the action that is going to be executed into HTML report:
		htmlReport.addSelectedAction(state, action);
		System.out.println(action.toShortString());
		try {
			// execute the selected action in the current state
			action.run(system, state, settings.get(ConfigTags.ActionDuration, 0.1));

			double waitTime = settings.get(ConfigTags.TimeToWaitAfterAction, 0.5);
			Util.pause(waitTime);

			if(action instanceof seActionNavigateToBlock) {
				SpatialSequentialMap.updateNavigableNodesPath(((seActionNavigateToBlock) action).getNavigableNodes());
			}
			else if(action instanceof seActionExplorePosition) {
				SpatialSequentialMap.updateNavigableNodesPath(((seActionExplorePosition) action).getNavigableNodes());
			}
			SpatialSequentialMap.updateInteractedBlock(action);

			// After executing the action and updating the Spatial information
			// Print the spatial action coverage
			Duration accumulativeActionTime = Duration.between(accumulativeActionTimePerSequence, Instant.now());
			SpatialSequentialMap.extractActionStepSpatialCoverage(actionCount, accumulativeActionTime.toSeconds());

			return true;

		} catch(ActionFailedException afe) {
			// If the action fails due to a problem in the navigable path
			// Save the next action spatial coverage without updating any interaction or navigation information
			Duration accumulativeActionTime = Duration.between(accumulativeActionTimePerSequence, Instant.now());
			SpatialSequentialMap.extractActionStepSpatialCoverage(actionCount, accumulativeActionTime.toSeconds());
			return false;
		}
	}

	/**
	 * This method is invoked each time the TESTAR has reached the stop criteria for generating a sequence.
	 * This can be used for example for graceful shutdown of the SUT, maybe pressing "Close" or "Exit" button
	 */
	@Override
	protected void finishSequence() {
		// Do not invoke super.finishSequence because the invocation of SE + Steam creates a new process invocation
		// Then TESTAR considers this process a temporal process to kill instead of the SUT process

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
			// Then exit to menu
			system.get(IV4XRtags.iv4xrSpaceEngineers).getSession().exitToMainMenu();

			// Create the spatial image based on the explored level
			Duration finalAccumulativeActionTime = Duration.between(accumulativeActionTimePerSequence, Instant.now());
			SpatialSequentialMap.createFinalSpatialMap(finalAccumulativeActionTime.toSeconds());
		}
		// Close iv4xr-plugin connection
		system.get(IV4XRtags.iv4xrSpaceEngineers).close();
		// If we use SUTConnector command line, we want to kill the process and start a new one next sequence
		if(settings.get(ConfigTags.SUTConnector).equals(Settings.SUT_CONNECTOR_CMDLINE)) {
			// super.finishSequence();
			SystemProcessHandling.windowsTaskPolitelyFinishProcess("SpaceEngineers.exe", 60);
		}

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

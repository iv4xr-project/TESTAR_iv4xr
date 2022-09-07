/***************************************************************************************************
 *
 * Copyright (c) 2021 - 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2021 - 2022 Open Universiteit - www.ou.nl
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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.fruit.Util;
import org.fruit.alayer.*;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Main;
import org.testar.OutputStructure;
import org.testar.protocols.iv4xr.SEProtocol;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.se.commands.*;
import eu.testar.iv4xr.actions.se.goals.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import nl.ou.testar.RandomActionSelector;
import spaceEngineers.model.Vec3F;

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
public class Protocol_se_testar_coverage extends SEProtocol {

	/*
	 * Modify agent ObservationRadius in the file: 
	 * C:\Users\<user>\AppData\Roaming\SpaceEngineers\ivxr-plugin.config
	 */

	private static Set<String> toolEntities;
	static {
		toolEntities = new HashSet<String>();
		toolEntities.add("LargeBlockSmallGenerator");
		toolEntities.add("LargeBlockBatteryBlock");
		toolEntities.add("SurvivalKitLarge");
	}

	private static Set<String> interactiveEntities;
	static {
		interactiveEntities = new HashSet<String>();
		interactiveEntities.add("Ladder2");
		interactiveEntities.add("LargeBlockCockpit");
		interactiveEntities.add("CockpitOpen");
		interactiveEntities.add("LargeBlockCryoChamber");
	}

	// Oracle example to validate that the block integrity decreases after a Grinder action
	private Verdict functional_verdict = Verdict.OK;

	/**
	 * This methods is called before each test sequence, allowing for example using external profiling software on the SUT
	 */
	@Override
	protected void preSequencePreparations() {
		super.preSequencePreparations();

		verifySteamAppidFile(settings.get(ConfigTags.OpenCoverTarget));

		verifyPdbFiles(settings.get(ConfigTags.PdbFilesPath));

		try {
			downloadOpenCoverageTools();
			// Add OpenCover and ReportGenerator folder temporally to the environment path
			//String customPathEnv = settings.get(ConfigTags.OpenCoverPath).toAbsolutePath() + ";" + settings.get(ConfigTags.ReportGeneratorPath).toAbsolutePath() + ";";
			//Util.getModifiableEnvironment().put("Path", System.getenv("Path") + customPathEnv);
			// Launch the SUT with OpenCover tool
			prepareLaunchOpenCoverSUTconnector();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Verify that the target folder that contains the SpaceEngineers executable contains the steam_appid.txt file. 
	 * If this file does not exist, the SE executable invokes Steam instead of launching the game process. 
	 * This Steam invocation does not allow OpenCover to be attached to the process properly. 
	 * 
	 * @param seTargetFile
	 */
	private void verifySteamAppidFile(Path seTargetFile) {
		try {
			File seFolder = new File(seTargetFile.toString()).getAbsoluteFile().getParentFile();
			File seSteamAppFile = new File(seFolder + File.separator + "steam_appid.txt");
			if(seSteamAppFile.exists() && Files.readString(seSteamAppFile.toPath()).equals("244850")) {
				System.out.println("SE steam_appid file exists: " + seSteamAppFile.toString());
			} else {
				System.err.println("You need to create the steam_appid.txt with 244850 string content to allow running OpenCover tool with SpaceEngineers");
				System.exit(0);
			}
		} catch (IOException ioe) {
			System.err.println("You need to create the steam_appid.txt with 244850 string content to allow running OpenCover tool with SpaceEngineers");
			ioe.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Verify that the PDB files of the .NET SUT exist and are correctly indicated in the PdbFilesPath setting. 
	 * 
	 * @param pdbFilesPath
	 */
	private void verifyPdbFiles(Path pdbFilesPath) {
		String[] pdbFiles = getPdbFiles(pdbFilesPath);
		System.out.println("PDB files: " + Arrays.toString(pdbFiles));
		if(pdbFiles.length == 0) {
			System.err.println("In order to obtain OpenCover coverage the tool needs access to the PDB files");
			System.err.println("To do that the user needs to customize in TESTAR the PdbFilesPath setting");
			System.exit(0);
		}
	}

	private String[] getPdbFiles(Path pdbPath) {
		return new File(pdbPath.toAbsolutePath().toString()).list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".pdb");
			}
		});
	}

	/**
	 * Check and download OpenCover and ReportGenerator tools. 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private void downloadOpenCoverageTools() throws MalformedURLException, IOException {
		// If the folder of the OpenCover and ReportGenerator tools does not exist, 
		// or if these are empty. 
		// Download and prepare the tools. 
		if(!Files.exists(settings.get(ConfigTags.OpenCoverPath)) || Util.folderIsEmpty(settings.get(ConfigTags.OpenCoverPath), true)) {
			System.out.println("Downloading OpenCover tool...");
			String openCoverURL = "https://github.com/OpenCover/opencover/releases/download/4.7.1221/opencover.4.7.1221.zip";
			String openCoverTempZip = Main.tempDir + "OpenCoverTool.zip";
			// Download the OpenCover tool
			FileUtils.copyURLToFile(new URL(openCoverURL), new File(openCoverTempZip), 2000, 2000);
			// Verify MD5 checksum
			String openCoverMD5 = Util.fileMD5(openCoverTempZip);
			String openCoverExpected = "07726b884edc145cd46debeaca67b498";
			// If MD5 is correct, extract open cover tool zip content in the expected folder
			if(openCoverMD5.equalsIgnoreCase(openCoverExpected)) {
				Util.unzipFile(openCoverTempZip, settings.get(ConfigTags.OpenCoverPath).toString());
				Files.delete(Paths.get(openCoverTempZip));
				System.out.println("Download completed! " + settings.get(ConfigTags.OpenCoverPath).toString());
			} 
			// If MD5 is not the correct one, delete zip file and throw an error
			else {
				Files.delete(Paths.get(openCoverTempZip));
				throw new SystemStartException(String.format("MD5 value of the OpenCover tool is not correct. URL %s , MD5 %s , expected %s", openCoverURL, openCoverMD5, openCoverExpected));
			}
		}
		if(!Files.exists(settings.get(ConfigTags.ReportGeneratorPath)) || Util.folderIsEmpty(settings.get(ConfigTags.ReportGeneratorPath), true)) {
			System.out.println("Downloading ReportGenerator tool...");
			String reportGeneratorURL = "https://github.com/danielpalme/ReportGenerator/releases/download/v5.1.9/ReportGenerator_5.1.9.zip";
			String reportGeneratorTempZip = Main.tempDir + "ReportGeneratorTool.zip";
			// Download the ReportGenerator tool
			FileUtils.copyURLToFile(new URL(reportGeneratorURL), new File(reportGeneratorTempZip), 2000, 2000);
			// Verify MD5 checksum
			String reportGeneratorMD5 = Util.fileMD5(reportGeneratorTempZip);
			String reportGeneratorExpected = "37e60f620045eaf38ecf1e7b4b7b9653";
			// If MD5 is correct, extract report generator tool zip content in the expected folder
			if(reportGeneratorMD5.equalsIgnoreCase(reportGeneratorExpected)) {
				Util.unzipFile(reportGeneratorTempZip, settings.get(ConfigTags.ReportGeneratorPath).toString());
				Files.delete(Paths.get(reportGeneratorTempZip));
				System.out.println("Download completed! " + settings.get(ConfigTags.ReportGeneratorPath).toString());
			} 
			// If MD5 is not the correct one, delete zip file and throw an error
			else {
				Files.delete(Paths.get(reportGeneratorTempZip));
				throw new SystemStartException(String.format("MD5 value of the ReportGenerator tool is not correct. URL %s , MD5 %s , expected %s", reportGeneratorURL, reportGeneratorMD5, reportGeneratorExpected));
			}
		}
	}

	private void prepareLaunchOpenCoverSUTconnector() throws IOException {
		String openCoverTool = settings.get(ConfigTags.OpenCoverPath).toAbsolutePath().toString() + File.separator + "OpenCover.Console.exe";
		//TODO: The usage of PROGRA~2 works for the targetargs plugin but not for the SpaceEngineers.exe target
		String target = " -target:\"" + settings.get(ConfigTags.OpenCoverTarget).toString() + "\"";
		String targetargs = " -targetargs:\"" + settings.get(ConfigTags.OpenCoverTargetArgs) + "\"";
		String output = " -output:\"" + OutputStructure.outerLoopOutputDir + File.separator + "se_coverage.xml\"";
		String pdbfiles = " -searchdirs:\"" + settings.get(ConfigTags.PdbFilesPath).toString() + "\"";

		// Execute OpenCover with SpaceEngineers in a new process
		String command = openCoverTool + target + targetargs + output + pdbfiles + " -register:user";
		System.out.println("Running SE OpenCover command: " + command);
		Runtime.getRuntime().exec(command);

		// Wait to launch the SE game
		// TODO: Improve to wait until the process is ready
		Util.pause(60);
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
		// Load the desired level to execute TESTAR and obtain the coverage
		system.get(IV4XRtags.iv4xrSpaceEngineers).getSession().loadScenario(new File("suts/se_levels/simple-place-grind-torch").getAbsolutePath());
		Util.pause(20);
		return system;
	}

	/**
	 * The getVerdict methods implements the online state oracles that
	 * examine the SUT's current state and returns an oracle verdict.
	 * @return oracle verdict, which determines whether the state is erroneous and why.
	 */
	@Override
	protected Verdict getVerdict(State state) {
		// Apply an Oracle to check if Grinder action worked properly
		if(lastExecutedAction != null && lastExecutedAction instanceof seActionNavigateGrinderBlock) {
			// Check the block attached to the previous executed grinder action
			Widget previousBlock = ((seActionNavigateGrinderBlock)lastExecutedAction).get(Tags.OriginWidget);
			Float previousIntegrity = previousBlock.get(IV4XRtags.seIntegrity);
			System.out.println("Previous Block Integrity: " + previousIntegrity);
			// Try to find the same block in the current state using the block id
			for(Widget w : state) {
				if(w.get(IV4XRtags.entityId).equals(previousBlock.get(IV4XRtags.entityId))) {
					Float currentIntegrity = w.get(IV4XRtags.seIntegrity);
					System.out.println("Current Block Integrity: " + currentIntegrity);
					// If previous integrity is the same or increased, something went wrong
					if(currentIntegrity >= previousIntegrity) {
						String blockType = w.get(IV4XRtags.entityType);
						functional_verdict = new Verdict(Verdict.BLOCK_INTEGRITY_ERROR, "The integrity of interacted block " + blockType + " didn't decrease after a Grinder action");
					}
				}
			}
			// If the previous block does not exist in the current state, it has been destroyed after the grinder action
			// We consider this OK by default, but more sophisticated oracles can be applied here
		}

		// Apply an Oracle to check jet-pack settings
		if(lastExecutedAction != null && lastExecutedAction instanceof seActionNavigateInteract) {
			Widget previousAgent = getAgentEntityFromState(latestState);
			Widget currentAgent = getAgentEntityFromState(state);

			if(!previousAgent.get(IV4XRtags.seAgentJetpackRunning).equals(currentAgent.get(IV4XRtags.seAgentJetpackRunning))) {
				Widget interactedBlock = ((seActionNavigateInteract)lastExecutedAction).get(Tags.OriginWidget);
				functional_verdict = new Verdict(Verdict.JETPACK_SETTINGS_ERROR, "Jetpack settings are incorrect after interacting with block : " + interactedBlock.get(IV4XRtags.entityType));
			}
		}

		return super.getVerdict(state).join(functional_verdict);
	}

	/**
	 * Derive all possible actions that TESTAR can execute in each specific Space Engineers state.
	 */
	@Override
	protected Set<Action> deriveActions(SUT system, State state) {
		Set<Action> labActions = new HashSet<>();

		// For each block widget (see movementEntities types), rotate and move until the agent is close to the position of the block
		for(Widget w : state) {
			Vec3 reachablePosition = null;
			if(toolEntities.contains(w.get(IV4XRtags.entityType))
					&& (reachablePosition = seReachablePositionHelper.calculateAdjacentReachablePosToEntity(system, w)) != null) {
				labActions.add(new seActionNavigateGrinderBlock(w, reachablePosition, system, agentId, 4, 1.0));
				labActions.add(new seActionNavigateWelderBlock(w, reachablePosition, system, agentId, 4, 1.0));
			}

			// FIXME: Fix Ladder2 is not observed as entityType
			if((interactiveEntities.contains(w.get(IV4XRtags.entityType)) || w.get(IV4XRtags.seDefinitionId).contains("Ladder2"))
					&& (reachablePosition = seReachablePositionHelper.calculateAdjacentReachablePosToEntity(system, w)) != null) {
				labActions.add(new seActionNavigateInteract(w, reachablePosition, system, agentId));
			}
		}

		// Now add the set of actions to explore level positions
		labActions = calculateExploratoryPositions(system, state, labActions);

		// If it was not possible to navigate to an entity or realize a smart exploration
		// prepare a dummy exploration
		if(labActions.isEmpty()) {
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(0, 0, 1f), 30)); // Move to back
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(0, 0, -1f), 30)); // Move to front
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(1f, 0, 0), 30)); // Move to Right
			labActions.add(new seActionCommandMove(state, agentId, new Vec3F(-1f, 0, 0), 30)); // Move to Left
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

	/**
	 * SE - Platform
	 * X and Z axes are the 2D to calculate the navigation movements. 
	 * Calculate the navigable position by adding coordinates to the current agent position. 
	 * 
	 * @param system
	 * @param state
	 * @param actions
	 * @return actions
	 */
	private Set<Action> calculateExploratoryPositions(SUT system, State state, Set<Action> actions) {
		// Circular positions relative to the agent center
		// https://stackoverflow.com/a/5301049
		Vec3 agentCenter = SVec3.seToLab(state.get(IV4XRtags.agentWidget).get(IV4XRtags.seAgentPosition));

		// 1 block distance positions (near)
		// For near positions calculate 8 positions in circle
		int points = 8;
		double slice = 2 * Math.PI / points;
		double radius = 2.5;
		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			float newX = agentCenter.x + (float)(radius * Math.cos(angle));
			float newZ = agentCenter.z + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a reachable position
			Vec3 nearPosition = new Vec3(newX, agentCenter.y, newZ);
			if(seReachablePositionHelper.calculateIfPositionIsReachable(system, nearPosition)) {
				actions.add(new seActionExplorePosition(state, nearPosition, system, agentId));
			}
		}

		// 2 block distance positions (medium)
		// For medium positions calculate 16 positions in circle
		points = 16;
		slice = 2 * Math.PI / points;
		radius = 5.0;
		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			float newX = agentCenter.x + (float)(radius * Math.cos(angle));
			float newZ = agentCenter.z + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a reachable position
			Vec3 medPosition = new Vec3(newX, agentCenter.y, newZ);
			if(seReachablePositionHelper.calculateIfPositionIsReachable(system, medPosition)) {
				actions.add(new seActionExplorePosition(state, medPosition, system, agentId));
			}
		}

		// 3 block distance positions (far)
		// For far positions calculate 16 positions in circle
		points = 16;
		slice = 2 * Math.PI / points;
		radius = 7.5;
		for (int i = 0; i < points; i++) {
			double angle = slice * i;
			float newX = agentCenter.x + (float)(radius * Math.cos(angle));
			float newZ = agentCenter.z + (float)(radius * Math.sin(angle));
			// New destination on which we need to calculate if it is a reachable position
			Vec3 farPosition = new Vec3(newX, agentCenter.y, newZ);
			if(seReachablePositionHelper.calculateIfPositionIsReachable(system, farPosition)) {
				actions.add(new seActionExplorePosition(state, farPosition, system, agentId));
			}
		}

		return actions;
	}

	/**
	 * Here you can put graceful shutdown sequence for your SUT
	 * @param system
	 */
	@Override
	protected void stopSystem(SUT system) {
		super.stopSystem(system);
		try {
			Runtime.getRuntime().exec("taskkill /F /IM SpaceEngineers.exe");
		} catch (IOException ioe) {
			System.err.println("Error finishing SpaceEngineers process" );
			ioe.printStackTrace();
		}
		// We need to pause to give time to OpenCover to extract the coverage results
		Util.pause(30);

		String reportGeneratorTool = settings.get(ConfigTags.ReportGeneratorPath).toAbsolutePath().toString() + File.separator + "netcoreapp3.1" + File.separator + "ReportGenerator.exe";
		String reports = " -reports:\"" + new File(OutputStructure.outerLoopOutputDir + File.separator + "se_coverage.xml").getAbsolutePath() + "\"";
		String targetdir = " -targetdir:\"" + new File(OutputStructure.outerLoopOutputDir).getAbsolutePath() + "\"";
		String sourcedirs = " -sourcedirs:\"" + settings.get(ConfigTags.PdbFilesPath).toString() + "\"";

		// Execute ReportGenerator with SpaceEngineers in a new process
		String command = reportGeneratorTool + reports + targetdir + sourcedirs;
		System.out.println("Running ReportGenerator command: " + command);
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException ioe) {
			System.err.println("Error creating the HTML report using ReportGenerator : " + reports);
			ioe.printStackTrace();
		}
	}
}

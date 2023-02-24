package eu.testar.iv4xr.actions.se.goals;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.fruit.alayer.Action;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Widget;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.testar.iv4xr.IV4XRStateBuilder;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.se.SpaceEngineersProcess;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.controller.SpaceEngineersJavaProxyBuilder;
import spaceEngineers.model.Block;
import spaceEngineers.model.CharacterObservation;
import spaceEngineers.model.Observation;

/**
 * JUnit tests ignored by default, 
 * uncomment @Ignore label to test the connection with Space Engineers game + iv4xr-plugin
 */

@Ignore
public class ActionNavigateTest {

	private static SUT system;
	private static IV4XRStateBuilder stateBuilder;

	@BeforeClass
	public static void setup() {
		// We only execute this unit tests in windows environments.
		Assume.assumeTrue(System.getProperty("os.name").toLowerCase().contains("windows"));

		// TESTAR uses the windows.dll to use Windows native methods
		try {
			FileUtils.copyFileToDirectory(new File("..//testar//resources//windows10//windows.dll"), new File("."));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Prepare the state builder for Space Engineers that indicates to TESTAR how to extract SE observed properties
		stateBuilder = new IV4XRStateBuilder(10, "Se");

		// Create and connect with the running Space Engineers instance
		SpaceEngineersProcess.characterControllerId = "you";

		// If Space Engineers game is running with the iv4xr-plugin enabled
		SpaceEngineers seController = new SpaceEngineersJavaProxyBuilder().localhost(SpaceEngineersProcess.characterControllerId);

		// Create the TESTAR system and attach the controller objects
		system = SpaceEngineersProcess.fromProcessName("SpaceEngineers.exe");
		system.set(IV4XRtags.iv4xrSpaceEngineers, seController);
		system.set(IV4XRtags.iv4xrSpaceEngCharacter, seController.getCharacter());
		system.set(IV4XRtags.iv4xrSpaceEngItems, seController.getItems());
	}

	// Set of interactive entities that allow the agent to interact and charge energy
	private static Set<String> interactiveEnergyEntities;
	static {
		interactiveEnergyEntities = new HashSet<String>();
		interactiveEnergyEntities.add("LargeBlockCockpitSeat");
		//interactiveEnergyEntities.add("LargeBlockCryoChamber");
	}
	@Test
	public void navigate_recharge_energy() {
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);

		// Load the desired SE scenario
		System.out.println("TEST Directory = " + System.getProperty("user.dir"));
		seController.getSession().loadScenario(new File("resources/se_levels/manual-world-survival").getAbsolutePath());
		seController.getScreens().waitUntilTheGameLoaded();

		// Create the Space Engineers state
		State state = stateBuilder.apply(system);
		assertNotNull(state);

		// Print the information before executing an action
		printTESTARinfo(state);

		Action navigate = null;

		for(Widget w : state) {
			// Some interactive entities allow the agent to rest inside and charge the energy
			if(interactiveEnergyEntities.contains(w.get(IV4XRtags.entityType)) && sePositionRotationHelper.calculateIfEntityReachable(system, w)) {
				navigate = new seActionNavigateRechargeEnergy(w, system, SpaceEngineersProcess.characterControllerId);
			}
		}

		assertNotNull(navigate);

		navigate.run(system, state, 0);

		// Print the information after executing an action
		printTESTARinfo(state);
	}

	private static Set<String> toolEntities;
	static {
		toolEntities = new HashSet<String>();
		toolEntities.add("LargeBlockBatteryBlock");
		//toolEntities.add("SurvivalKitLarge");
	}

	@Test
	public void navigate_shoot_block() {
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);

		// Load the desired SE scenario
		System.out.println("TEST Directory = " + System.getProperty("user.dir"));
		seController.getSession().loadScenario(new File("resources/se_levels/manual-world-survival").getAbsolutePath());
		seController.getScreens().waitUntilTheGameLoaded();

		// Create the Space Engineers state
		State state = stateBuilder.apply(system);
		assertNotNull(state);

		// Print the information before executing an action
		printControllerInfo();

		Action navigate = null;

		for(Widget w : state) {
			// Some interactive entities allow the agent to rest inside and charge the energy
			if(toolEntities.contains(w.get(IV4XRtags.entityType)) && sePositionRotationHelper.calculateIfEntityReachable(system, w)) {
				navigate = new seActionNavigateShootBlock(w, system, SpaceEngineersProcess.characterControllerId);
			}
		}

		assertNotNull(navigate);

		navigate.run(system, state, 0);

		// Print the information after executing an action
		printControllerInfo();
	}

	@Test
	public void navigate_charge_health() {
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);

		// Load the desired SE scenario
		System.out.println("TEST Directory = " + System.getProperty("user.dir"));
		seController.getSession().loadScenario(new File("resources/se_levels/manual-world-survival").getAbsolutePath());
		seController.getScreens().waitUntilTheGameLoaded();

		// Create the Space Engineers state
		State state = stateBuilder.apply(system);
		assertNotNull(state);

		// Print the information before executing an action
		printControllerInfo();

		Action navigate = null;

		for(Widget w : state) {
			// Some interactive entities allow the agent to rest inside and charge the energy
			if(w.get(IV4XRtags.entityType, "").contains("MedicalRoom") && sePositionRotationHelper.calculateIfEntityReachable(system, w)) {
				navigate = new seActionNavigateRechargeHealth(w, system, SpaceEngineersProcess.characterControllerId);
			}
		}

		assertNotNull(navigate);

		navigate.run(system, state, 0);

		// Print the information after executing an action
		printControllerInfo();
	}

	/**
	 *  Iterate over the state and print the agent and functional information
	 * @param state
	 */
	private void printTESTARinfo(State state) {
		System.out.println("-------------------------------------------------");
		System.out.println("---------------- TESTAR printing ----------------");
		System.out.println("-------------------------------------------------");
		for(Widget w : state) {
			// TESTAR considers the agent as a widget in the state
			if(w.get(IV4XRtags.entityType).contains("AGENT")) {
				System.out.println("Agent seAgentPosition: " + w.get(IV4XRtags.seAgentPosition));
				System.out.println("Agent seAgentOrientationForward: " + w.get(IV4XRtags.seAgentOrientationForward));
				System.out.println("Agent seAgentHealth: " + w.get(IV4XRtags.seAgentHealth));
				System.out.println("Agent seAgentHydrogen: " + w.get(IV4XRtags.seAgentHydrogen));
				System.out.println("Agent seAgentEnergy: " + w.get(IV4XRtags.seAgentEnergy));
				System.out.println("Agent seAgentOxygen: " + w.get(IV4XRtags.seAgentOxygen));
				System.out.println("Agent seAgentJetpackRunning: " + w.get(IV4XRtags.seAgentJetpackRunning));
				System.out.println("Agent seAgentDampenersOn: " + w.get(IV4XRtags.seAgentDampenersOn));
			} 
			// Print information of functional SE blocks (ignore floor or walls)
			else if (!(w instanceof State) && w.get(IV4XRtags.seFunctional)) {
				System.out.println("Block entityType: " + w.get(IV4XRtags.entityType));
				System.out.println("Block seDefinitionId: " + w.get(IV4XRtags.seDefinitionId));
				System.out.println("Block seIntegrity: " + w.get(IV4XRtags.seIntegrity));
				System.out.println("Block entityPosition: " + w.get(IV4XRtags.entityPosition));
				System.out.println("Block seOrientationForward: " + w.get(IV4XRtags.seOrientationForward));
				System.out.println("Block seSize: " + w.get(IV4XRtags.seSize));
				System.out.println("Block seFunctional: " + w.get(IV4XRtags.seFunctional));
			}
		}
	}

	private void printControllerInfo() {
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);

		System.out.println("*************************************************");
		System.out.println("************** CONTROLLER printing **************");
		System.out.println("*************************************************");

		// Use SE plugin to print agent information
		CharacterObservation seObsCharacter = seController.getObserver().observe();
		System.out.println("CharacterObservation position: " + seObsCharacter.getPosition());
		System.out.println("CharacterObservation orientationForward: " + seObsCharacter.getOrientationForward());
		System.out.println("CharacterObservation health: " + seObsCharacter.getHealth());
		System.out.println("CharacterObservation oxygen: " + seObsCharacter.getOxygen());
		System.out.println("CharacterObservation suitEnergy: " + seObsCharacter.getEnergy());
		System.out.println("CharacterObservation hydrogen: " + seObsCharacter.getHydrogen());
		System.out.println("CharacterObservation jetpackRunning: " + seObsCharacter.getJetpackRunning());
		System.out.println("CharacterObservation dampenersOn: " + seObsCharacter.getDampenersOn());

		// Use SE plugin to print functional blocks information
		Observation seObsBlocks = seController.getObserver().observeBlocks();
		for(Block seBlock : seObsBlocks.getGrids().get(0).getBlocks()) {
			// Only functional SE blocks (ignore floor or walls)
			if(seBlock.getFunctional()) {
				System.out.println("BlockObservation type: " + seBlock.getDefinitionId().getType());
				System.out.println("BlockObservation definitionId: " + seBlock.getDefinitionId());
				System.out.println("BlockObservation position: " + seBlock.getPosition());
				System.out.println("BlockObservation integrity: " + seBlock.getIntegrity());
				System.out.println("BlockObservation orientationForward: " + seBlock.getOrientationForward());
				System.out.println("BlockObservation size: " + seBlock.getSize());
			}
		}
	}

	@AfterClass
	public static void close() {
		// Close Space Engineers plugin session
		system.get(IV4XRtags.iv4xrSpaceEngineers).close();
	}

}

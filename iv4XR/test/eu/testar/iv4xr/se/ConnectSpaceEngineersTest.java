package eu.testar.iv4xr.se;

import static org.junit.Assert.*;

import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import environments.SeEnvironment;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import spaceEngineers.controller.ContextControllerWrapper;
import spaceEngineers.controller.JsonRpcSpaceEngineersBuilder;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.controller.SpaceEngineersTestContext;
import spaceEngineers.model.CharacterObservation;
import spaceEngineers.model.Observation;
import uuspaceagent.UUSeAgentState;

/**
 * JUnit tests ignored by default, 
 * uncomment @Ignore label to test the connection with Space Engineers game + iv4xr-plugin
 */

@Ignore
public class ConnectSpaceEngineersTest {

	private static SpaceEngineers seController;

	@BeforeClass
	public static void setup() {
		// We only execute this unit tests in windows environments.
		Assume.assumeTrue(System.getProperty("os.name").toLowerCase().contains("windows"));
		// If Space Engineers game is running with the iv4xr-plugin enabled
		seController = JsonRpcSpaceEngineersBuilder.Companion.localhost("you");
	}

	@Test
	public void character_observation() {
		CharacterObservation characterObs = seController.getObserver().observe();
		assertTrue(characterObs != null);
	}

	@Test
	public void blocks_observation() {
		Observation blocksObs = seController.getObserver().observeBlocks();
		assertTrue(blocksObs != null);
	}

	@Test
	public void create_agent() {
		UUSeAgentState stateGrid = new UUSeAgentState("you");
		SpaceEngineersTestContext context = new SpaceEngineersTestContext();
		ContextControllerWrapper controllerWrapper = new ContextControllerWrapper(seController, context);
		// WorldId is empty because we are going to connect to a running level, not load a new one
		SeEnvironment sEnv = new SeEnvironment("", controllerWrapper, context);
		// Finally create the TestAgent
		TestAgent testAgent = new SeAgentTESTAR("you", "explorer").attachState(stateGrid).attachEnvironment(sEnv);

		assertTrue(testAgent.getId() == "you");
		assertTrue(testAgent.env() == sEnv);
	}

}

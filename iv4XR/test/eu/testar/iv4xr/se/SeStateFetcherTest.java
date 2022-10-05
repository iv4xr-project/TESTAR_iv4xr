package eu.testar.iv4xr.se;

import static org.junit.Assert.assertNotNull;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
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
import spaceEngineers.transport.CloseIfCloseableKt;

/**
 * JUnit tests ignored by default, 
 * uncomment @Ignore label to test the connection with Space Engineers game + fetch State
 */

@Ignore
public class SeStateFetcherTest {

	private static SUT system;

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
	}

	/**
	 * Connect with a Space Engineers running instance, 
	 * and fetch the state with the agent and block properties. 
	 */
	@Test
	public void fetch_se_state() {
		// Prepare the state builder for Space Engineers that indicates to TESTAR how to extract SE observed properties
		IV4XRStateBuilder stateBuilder = new IV4XRStateBuilder(10, "Se");
		// Create and connect with the running Space Engineers instance
		SpaceEngineersProcess.characterControllerId = "you";
		system = SpaceEngineersProcess.fromProcessName("SpaceEngineers.exe");
		assertNotNull(system);

		State state = stateBuilder.apply(system);
		assertNotNull(state);

		for(Widget w : state) {
			// Agent entity assertions
			if(w.get(IV4XRtags.entityType).contains("AGENT")) {
				System.out.println("Agent seAgentPosition: " + w.get(IV4XRtags.seAgentPosition));
				assertNotNull(w.get(IV4XRtags.seAgentPosition));

				System.out.println("Agent seAgentOrientationForward: " + w.get(IV4XRtags.seAgentOrientationForward));
				assertNotNull(w.get(IV4XRtags.seAgentOrientationForward));

				System.out.println("Agent seAgentHealth: " + w.get(IV4XRtags.seAgentHealth));
				assertNotNull(w.get(IV4XRtags.seAgentHealth));

				System.out.println("Agent seAgentHydrogen: " + w.get(IV4XRtags.seAgentHydrogen));
				assertNotNull(w.get(IV4XRtags.seAgentHydrogen));

				System.out.println("Agent seAgentEnergy: " + w.get(IV4XRtags.seAgentEnergy));
				assertNotNull(w.get(IV4XRtags.seAgentEnergy));

				System.out.println("Agent seAgentOxygen: " + w.get(IV4XRtags.seAgentOxygen));
				assertNotNull(w.get(IV4XRtags.seAgentOxygen));

				System.out.println("Agent seAgentJetpackRunning: " + w.get(IV4XRtags.seAgentJetpackRunning));
				assertNotNull(w.get(IV4XRtags.seAgentJetpackRunning));

				System.out.println("Agent seAgentDampenersOn: " + w.get(IV4XRtags.seAgentDampenersOn));
				assertNotNull(w.get(IV4XRtags.seAgentDampenersOn));
			} 
			// Block entities assertions
			else if (!(w instanceof State)) {
				System.out.println("Block entityType: " + w.get(IV4XRtags.entityType));
				assertNotNull(w.get(IV4XRtags.entityType));

				System.out.println("Block seDefinitionId: " + w.get(IV4XRtags.seDefinitionId));
				assertNotNull(w.get(IV4XRtags.seDefinitionId));

				System.out.println("Block seIntegrity: " + w.get(IV4XRtags.seIntegrity));
				assertNotNull(w.get(IV4XRtags.seIntegrity));

				System.out.println("Block seOrientationForward: " + w.get(IV4XRtags.seOrientationForward));
				assertNotNull(w.get(IV4XRtags.seOrientationForward));

				System.out.println("Block seSize: " + w.get(IV4XRtags.seSize));
				assertNotNull(w.get(IV4XRtags.seSize));

				System.out.println("Block seFunctional: " + w.get(IV4XRtags.seFunctional));
				assertNotNull(w.get(IV4XRtags.seFunctional));
			}
		}
	}

	@AfterClass
	public static void close() {
		// Close Space Engineers plugin session
		CloseIfCloseableKt.closeIfCloseable(system.get(IV4XRtags.iv4xrSpaceEngineers));
	}

}

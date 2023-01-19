package eu.testar.iv4xr.actions.se.goals;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.fruit.alayer.Action;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.testar.iv4xr.IV4XRStateBuilder;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import eu.testar.iv4xr.se.SpaceEngineersProcess;
import spaceEngineers.controller.Observer;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.controller.SpaceEngineersJavaProxyBuilder;
import spaceEngineers.model.Vec3F;
import spaceEngineers.transport.CloseIfCloseableKt;

/**
 * JUnit tests ignored by default, 
 * uncomment @Ignore label to test the connection with Space Engineers game + iv4xr-plugin
 */

@Ignore
public class ActionRotateTest {

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

	@Test
	public void rotate_to_block() {
		SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);

		// Load the desired SE scenario
		System.out.println("TEST Directory = " + System.getProperty("user.dir"));
		seController.getSession().loadScenario(new File("resources/se_levels/space_3d_object").getAbsolutePath());
		seController.getScreens().waitUntilTheGameLoaded();

		// Create the Space Engineers state
		State state = stateBuilder.apply(system);
		assertNotNull(state);

		Action navigate = null;

		for(Widget w : state) {
			// Some interactive entities allow the agent to rest inside and charge the energy
			if(w.get(IV4XRtags.entityType, "").contains("LargeBlockSmallGenerator")) {
				navigate = new seActionRotateToBlock(w, system, SpaceEngineersProcess.characterControllerId);
			}
		}

		assertNotNull(navigate);

		navigate.run(system, state, 0);

		Observer seObserver = system.get(IV4XRtags.iv4xrSpaceEngineers).getObserver();

		// Based on the distance obtain the tolerance
		Vec3F agentPosition = seObserver.observe().getPosition();
		Widget actionBlock = navigate.get(Tags.OriginWidget);
		Vec3F blockPosition = SVec3.labToSE(actionBlock.get(IV4XRtags.entityPosition));
		float distance = blockPosition.distanceTo(agentPosition);
		float tolerance = sePositionRotationHelper.rotationToleranceByDistance(distance);

		// Then verify that the final orientation of the agent is inside the expected values
		Vec3F direction = (blockPosition.minus(agentPosition)).normalized();
		Vec3F agentOrientation = seObserver.observe().getOrientationForward().normalized();
		float cos_alpha = sePositionRotationHelper.dot(agentOrientation, direction);

		System.out.println("distance: " + distance);
		System.out.println("tolerance: " + tolerance);

		assertTrue(cos_alpha > (1f - tolerance));
	}

	@AfterClass
	public static void close() {
		// Close Space Engineers plugin session
		CloseIfCloseableKt.closeIfCloseable(system.get(IV4XRtags.iv4xrSpaceEngineers));
	}

}
class seActionRotateToBlock extends seActionNavigateToBlock {
	private static final long serialVersionUID = 124316627979006725L;

	public seActionRotateToBlock(Widget w, SUT system, String agentId){
		super(w, system, agentId);
		this.set(Tags.Desc, toShortString());
		// TODO: Update with Goal Solving agents
		this.set(IV4XRtags.agentAction, false);
		this.set(IV4XRtags.newActionByAgent, false);
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		rotateToBlockDestination(system);
	}

	@Override
	public String toShortString() {
		return "Rotate to block: " + widgetType + ", id by agent: " + agentId;
	}
}

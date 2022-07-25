package eu.testar.iv4xr.actions.se.commands;

import static org.junit.Assert.*;

import org.fruit.alayer.Action;
import org.fruit.alayer.SUT;
import org.fruit.alayer.SUTBase;
import org.fruit.alayer.StdState;
import org.fruit.alayer.StdWidget;
import org.fruit.alayer.exceptions.SystemStopException;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.testar.iv4xr.enums.IV4XRtags;
import spaceEngineers.controller.JsonRpcSpaceEngineersBuilder;
import spaceEngineers.controller.Observer;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.model.Vec2F;
import spaceEngineers.model.Vec3F;
import spaceEngineers.transport.SocketReaderWriterKt;

/**
 * JUnit tests ignored by default, 
 * uncomment @Ignore label to test the connection with Space Engineers game + iv4xr-plugin
 */

@Ignore
public class ActionCommandTest {

	private static SUT system;

	@BeforeClass
	public static void setup() {
		// We only execute this unit tests in windows environments.
		Assume.assumeTrue(System.getProperty("os.name").toLowerCase().contains("windows"));
		// If Space Engineers game is running with the iv4xr-plugin enabled
		SpaceEngineers seController = JsonRpcSpaceEngineersBuilder.Companion.localhost("you");
		// Empty SUT to attach the controller
		system = new SUTBase() {
			@Override
			public void stop() throws SystemStopException {}
			@Override
			public void setNativeAutomationCache() {}
			@Override
			public boolean isRunning() {return false;}
			@Override
			public String getStatus() {return null;}
		};
		system.set(IV4XRtags.iv4xrSpaceEngineers, seController);
		system.set(IV4XRtags.iv4xrSpaceEngCharacter, seController.getCharacter());
		system.set(IV4XRtags.iv4xrSpaceEngItems, seController.getItems());
	}

	@Test
	public void move_back() {
		Observer obs = system.get(IV4XRtags.iv4xrSpaceEngineers).getObserver();
		Vec3F originalPos = obs.observe().getPosition();

		Action move = new seActionCommandMove(new StdWidget(), "you", new Vec3F(0, 0, 1f), 100); // Move to back
		move.run(system, new StdState(), 0);

		Vec3F newPos = obs.observe().getPosition();

		assertTrue(originalPos != newPos);
	}

	@Test
	public void move_front() {
		Observer obs = system.get(IV4XRtags.iv4xrSpaceEngineers).getObserver();
		Vec3F originalPos = obs.observe().getPosition();

		Action move = new seActionCommandMove(new StdWidget(), "you", new Vec3F(0, 0, -1f), 100); // Move to front
		move.run(system, new StdState(), 0);

		Vec3F newPos = obs.observe().getPosition();

		assertTrue(originalPos != newPos);
	}

	@Test
	public void move_right() {
		Observer obs = system.get(IV4XRtags.iv4xrSpaceEngineers).getObserver();
		Vec3F originalPos = obs.observe().getPosition();

		Action move = new seActionCommandMove(new StdWidget(), "you", new Vec3F(1f, 0, 0), 100); // Move to Right
		move.run(system, new StdState(), 0);

		Vec3F newPos = obs.observe().getPosition();

		assertTrue(originalPos != newPos);
	}

	@Test
	public void move_left() {
		Observer obs = system.get(IV4XRtags.iv4xrSpaceEngineers).getObserver();
		Vec3F originalPos = obs.observe().getPosition();

		Action move = new seActionCommandMove(new StdWidget(), "you", new Vec3F(-1f, 0, 0), 100); // Move to Left
		move.run(system, new StdState(), 0);

		Vec3F newPos = obs.observe().getPosition();

		assertTrue(originalPos != newPos);
	}

	@Test
	public void rotate_right() {
		Observer obs = system.get(IV4XRtags.iv4xrSpaceEngineers).getObserver();
		Vec3F originalOrientation = obs.observe().getOrientationForward();

		Action rotate = new seActionCommandRotate(new StdWidget(), "you", new Vec2F(0, 500f)); // Rotate to Right
		rotate.run(system, new StdState(), 0);

		Vec3F newOrientation = obs.observe().getOrientationForward();

		assertTrue(originalOrientation != newOrientation);
	}

	@Test
	public void rotate_left() {
		Observer obs = system.get(IV4XRtags.iv4xrSpaceEngineers).getObserver();
		Vec3F originalOrientation = obs.observe().getOrientationForward();

		Action rotate = new seActionCommandRotate(new StdWidget(), "you", new Vec2F(0, -500f)); // Rotate to Left
		rotate.run(system, new StdState(), 0);

		Vec3F newOrientation = obs.observe().getOrientationForward();

		assertTrue(originalOrientation != newOrientation);
	}

	@Test
	public void open_helmet() {
		Observer obs = system.get(IV4XRtags.iv4xrSpaceEngineers).getObserver();

		Action helmet = new seActionCommandHelmetOpen(new StdWidget(), "you");
		helmet.run(system, new StdState(), 0);

		assertTrue(!obs.observe().getHelmetEnabled());
	}

	@Test
	public void close_helmet() {
		Observer obs = system.get(IV4XRtags.iv4xrSpaceEngineers).getObserver();

		Action helmet = new seActionCommandHelmetClose(new StdWidget(), "you");
		helmet.run(system, new StdState(), 0);

		assertTrue(obs.observe().getHelmetEnabled());
	}

	@Test
	public void use_grinder() {
		Action grinder = new seActionCommandGrinder(new StdWidget(), "you");
		grinder.run(system, new StdState(), 0);
	}

	@Test
	public void use_welder() {
		Action welder = new seActionCommandWelder(new StdWidget(), "you");
		welder.run(system, new StdState(), 0);
	}

	@Test
	public void place_block() {
		Action place = new seActionCommandPlaceBlock(new StdWidget(), "you", "LargeBlockArmorBlock");
		place.run(system, new StdState(), 0);
	}

	@AfterClass
	public static void close() {
		// Close Space Engineers plugin session
		SocketReaderWriterKt.closeIfCloseable(system.get(IV4XRtags.iv4xrSpaceEngineers));
	}

}

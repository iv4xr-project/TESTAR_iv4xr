package eu.testar.iv4xr.actions.se.goals;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.fruit.Util;
import org.fruit.alayer.SUT;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.se.SpaceEngineersProcess;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.controller.SpaceEngineersJavaProxyBuilder;
import spaceEngineers.model.ToolbarConfigData;

/**
 * JUnit tests ignored by default, 
 * uncomment @Ignore label to test the connection with Space Engineers game + iv4xr-plugin
 */

@Ignore
public class ActionScreenTest {

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
	public void search_block_in_ToolbarConfig_screen() {
		SpaceEngineers controller = system.get(IV4XRtags.iv4xrSpaceEngineers);

		// Load the desired SE scenario
		System.out.println("TEST Directory = " + System.getProperty("user.dir"));
		controller.getSession().loadScenario(new File("resources/se_levels/manual-world-survival").getAbsolutePath());
		controller.getScreens().waitUntilTheGameLoaded();

		controller.getScreens().getGamePlay().showToolbarConfig();
		Util.pause(1);

		Assert.assertTrue(controller.getScreens().getFocusedScreen().data().getName().equals("CubeBuilder"));

		controller.getScreens().getToolbarConfig().search("Heavy Armor Block");
		Util.pause(1);

		ToolbarConfigData tbcData = controller.getScreens().getToolbarConfig().data();

		Assert.assertTrue(!tbcData.getGridItems().isEmpty());
		Assert.assertTrue(tbcData.getGridItems().get(0).toString().equals("CubeBlock/LargeHeavyBlockArmorBlock"));

		Util.pause(1);

		controller.getScreens().getToolbarConfig().close();
		Util.pause(1);

		Assert.assertTrue(controller.getScreens().getFocusedScreen().data().getName().equals("GamePlay"));
	}

	@AfterClass
	public static void close() {
		// Close Space Engineers plugin session
		system.get(IV4XRtags.iv4xrSpaceEngineers).close();
	}

}

package eu.testar.iv4xr;

import java.util.List;

import org.fruit.Util;
import org.fruit.alayer.SUT;
import org.fruit.alayer.SUTBase;
import org.fruit.alayer.Tags;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.alayer.exceptions.SystemStopException;
import org.fruit.alayer.windows.WinProcess;

import communication.system.Request;
import environments.EnvironmentConfig;
import eu.testar.iv4xr.enums.IV4XRtags;
import pathfinding.NavMeshContainer;

public class IV4XRprocess extends SUTBase {

	public static IV4XRprocess iv4XR = null;

	private static WinProcess win;

	private IV4XRprocess(String path) {
		String[] parts = path.split(" ");
		String labPath = parts[0].replace("\"", "");

		win = WinProcess.fromExecutable(labPath, false);

		Util.pause(10);

		// Connect to Unity LabRecruits Game and Create a Socket Environment to send data
		EnvironmentConfig labRecruitsEnvironment = new EnvironmentConfig("button1_opens_door1", "suts/levels");
		eu.testar.iv4xr.SocketEnvironment socketEnvironment = new eu.testar.iv4xr.SocketEnvironment(labRecruitsEnvironment.host, labRecruitsEnvironment.port);

		// When this application has connected with the environment, an exchange in information takes place:
		// For now, this application sends nothing, and receives a navmesh of the world.
		NavMeshContainer navmesh = socketEnvironment.getResponse(Request.gymEnvironmentInitialisation(labRecruitsEnvironment));
		//this.pathFinder = new Pathfinder(navmesh);

		// presses "Play" in the game for you
		socketEnvironment.getResponse(Request.startSimulation());

		System.out.println("Welcome to the iv4XR test: " + labRecruitsEnvironment.level_name + " ** " + labRecruitsEnvironment.level_path);

		this.set(IV4XRtags.windowsProcess, win);
		this.set(IV4XRtags.iv4xrSocketEnvironment, socketEnvironment);
		this.set(Tags.PID, win.pid());

		iv4XR = this;
	}

	public static IV4XRprocess fromExecutable(String path) throws SystemStartException {
		if (iv4XR != null) {
			win.stop();
		}
		return new IV4XRprocess(path);
	}

	public static List<SUT> fromAll(){
		return WinProcess.fromAll();
	}

	public boolean isForeground(){
		return win.isForeground();
	}
	public void toForeground(){
		win.toForeground();
	}

	@Override
	public void stop() throws SystemStopException {
		win.stop();
	}

	@Override
	public boolean isRunning() {
		return win.isRunning();
	}

	@Override
	public String getStatus() {
		return win.getStatus();
	}

	@Override
	public void setNativeAutomationCache() {
		win.setNativeAutomationCache();
	}


}

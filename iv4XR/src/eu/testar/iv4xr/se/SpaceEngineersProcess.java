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

package eu.testar.iv4xr.se;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

import org.fruit.Assert;
import org.fruit.Util;
import org.fruit.alayer.SUT;
import org.fruit.alayer.SUTBase;
import org.fruit.alayer.Tags;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.alayer.exceptions.SystemStopException;
import org.fruit.alayer.windows.WinApiException;
import org.fruit.alayer.windows.WinProcess;

import eu.testar.iv4xr.enums.IV4XRtags;
import spaceEngineers.controller.JsonRpcCharacterController;
import spaceEngineers.controller.ProprietaryJsonTcpCharacterController;

public class SpaceEngineersProcess extends SUTBase {

	enum Launchment {
		EXECUTABLE_PATH,
		PROCESS_NAME
	}
	private static Launchment launchment;

	public static SpaceEngineersProcess iv4XR = null;
	public static String characterControllerId = "";

	private static WinProcess win;

	/**
	 * Prepare an execution using the SpaceEngineers using the executable path. 
	 * 
	 * @param executablePath
	 * @param processListenerEnabled
	 * @return
	 * @throws SystemStartException
	 */
	public static SpaceEngineersProcess fromExecutable(String executablePath, boolean processListenerEnabled) throws SystemStartException {
		if (iv4XR != null) {
			win.stop();
		}
		launchment = Launchment.EXECUTABLE_PATH;
		return new SpaceEngineersProcess(executablePath, processListenerEnabled);
	}

	/**
	 * Prepare a connection with SpaceEngineers using the process name. 
	 * 
	 * @param processName
	 * @return
	 * @throws SystemStartException
	 */
	public static SpaceEngineersProcess fromProcessName(String processName) throws SystemStartException {
		if (iv4XR != null) {
			win.stop();
		}
		launchment = Launchment.PROCESS_NAME;
		return new SpaceEngineersProcess(processName, false);
	}

	private SpaceEngineersProcess(String path, boolean processListenerEnabled) {
		Assert.notNull(path);

		// regex to split checking last space
		String[] parts = path.split(" (?!.* )");

		// If SUTConnectorValue is not correct throw an informative error
		if(parts.length < 1 || parts.length > 2 ) {
			settingsConnectorError();
		}

		// Prepare SUTConnectorValue parts to launch SpaceEngineers and launch the desired level
		String launchPart = parts[0].trim();
		String levelPath = "";
		if(parts.length == 2) {
			levelPath = parts[1].replace("\"", "");
		}

		// Launch and connect with SpaceEngineers
		launchSpaceEngineers(launchPart, processListenerEnabled);

		Util.pause(5);

		if(!win.isRunning()) {
			throw new SystemStartException(String.format("ERROR trying to connect with SE iv4xr SUT : %s", launchPart));
		}

		/**
		 * Start IV4XR SUT at JSON - WOM level
		 * In this case start Lab Recruits Environment with the desired level
		 */

		try {
			// Prepare SpaceEngineers Controller
			ProprietaryJsonTcpCharacterController proprietaryTcpController = ProprietaryJsonTcpCharacterController.Companion.localhost(characterControllerId);
			JsonRpcCharacterController rcpController = JsonRpcCharacterController.Companion.localhost(characterControllerId);
			Util.pause(2);
			System.out.println("Welcome to the SE iv4XR test: " + launchPart);

			// Load Space Engineers Level
			if(!levelPath.isEmpty()) {
				proprietaryTcpController.loadScenario(new File(levelPath).getAbsolutePath());
				Util.pause(10);
				System.out.println("Loaded level: " + levelPath);
			}

			this.set(IV4XRtags.windowsProcess, win);
			this.set(Tags.PID, win.pid());
			this.set(IV4XRtags.iv4xrSpaceEngProprietaryTcpController, proprietaryTcpController);
			this.set(IV4XRtags.iv4xrSpaceEngRcpController, rcpController);
			this.set(IV4XRtags.iv4xrSpaceEngCharacter, rcpController.getCharacter());
			this.set(IV4XRtags.iv4xrSpaceEngItems, rcpController.getItems());

		} catch(Exception e) {
			System.err.println(String.format("EnvironmentConfig ERROR: Trying to connect with %s", launchPart));
			System.err.println(e.getMessage());
			win.stop();
			throw new SystemStartException(e);
		}

		iv4XR = this;
	}

	/**
	 * Throw a message error to the user with information about how to configure the SUTConnectorValue
	 */
	private void settingsConnectorError() {
		String message = "ERROR: Launching SpaceEngineers using COMMAND_LINE connection \n"
				+ "To launch SpaceEngineers using COMMAND_LINE we need to know:\n" 
				+ "1.- SpaceEngineers executable path\n"
				+ "2.- (Optional) Path of the SpaceEngineers level to load\n"
				+ "Example: \"C:\\\\Program Files (x86)\\\\Steam\\\\steamapps\\\\common\\\\SpaceEngineers\\\\Bin64\\\\SpaceEngineers.exe\" \"suts/se_levels/simple-place-grind-torch\"";
		if(launchment.equals(Launchment.PROCESS_NAME)) {
			message = "ERROR: Trying to connect with Space Engineers using SUT_PROCESS_NAME connection \n"
					+ "To connect with SpaceEngineers process we need to know:\n" 
					+ "1.- SpaceEngineers.exe process name\n"
					+ "2.- (Optional) Path of the SpaceEngineers level to load\n"
					+ "Example: SpaceEngineers.exe \"suts/se_levels/simple-place-grind-torch\"";
		}
		throw new IllegalArgumentException(message);
	}

	/**
	 * Launch and connect with SpaceEngineers at Windows level. 
	 * 
	 * @param launchPart
	 */
	private void launchSpaceEngineers(String launchPart, boolean processListenerEnabled) {
		if(launchment.equals(Launchment.EXECUTABLE_PATH)) {
			try {
				// Seems that the initial launched process is not the SpaceEngineers running app process
				WinProcess.fromExecutable(launchPart, processListenerEnabled);
				// Wait for initial music (coded time maybe is not the best way)
				Util.pause(60);
				// Hook the process by name
				win = WinProcess.fromProcName(Paths.get(launchPart.replace("\"", "")).getFileName().toString());
			} catch (SystemStartException | WinApiException we) {
				System.err.println(String.format("ERROR: Trying to execute %s using Windows API", launchPart));
				throw new SystemStartException(we.getMessage());
			}
		}
		else {
			try {
				win = WinProcess.fromProcName(launchPart);
			} catch (SystemStartException | WinApiException we) {
				System.err.println(String.format("ERROR: Trying to connect %s using Windows API", launchPart));
				throw new SystemStartException(we.getMessage());
			}
		}
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

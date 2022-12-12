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

package eu.testar.iv4xr.labrecruits;

import org.fruit.Assert;
import org.fruit.Util;
import org.fruit.alayer.Tags;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.alayer.exceptions.SystemStopException;
import org.fruit.alayer.linux.LinuxProcess;
import org.fruit.alayer.windows.WinProcess;

import environments.LabRecruitsConfig;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.labrecruits.listener.LabRecruitsEnvironmentListener;
import game.LabRecruitsTestServer;
import world.BeliefState;

public class LabRecruitsServer extends LabRecruitsProcess {

	private LabRecruitsTestServer labRecruitsTestServer = null;

	public LabRecruitsServer(String path) {
		Assert.notNull(path);

		String[] parts = path.split(" ");

		if(parts.length != 3) {
			String message = "ERROR: For LabRecruits iv4xr SUT we need to know:\n" 
					+ "1.- LabRecruits executable path\n"
					+ "2.- LabRecruits levels path\n"
					+ "3.- LabRecruits level name (to solve)\n"
					+ "Example:\n"
					+ "\"suts\\gym\\Windows\\bin\\LabRecruits.exe\" \"suts/levels\" \"buttons_doors_1\"";
			throw new IllegalArgumentException(message);
		}

		String labPath = parts[0].replace("\"", "");
		String levelsPath = parts[1].replace("\"", "");
		String levelName = parts[2].replace("\"", "");

		Util.pause(10);

		labRecruitsTestServer = new LabRecruitsTestServer(false, labPath);
		labRecruitsTestServer.waitForGameToLoad();

		System.out.println("**** Running LabRecruits in SERVER MODE ****");

		/**
		 * Start IV4XR SUT at JSON - WOM level
		 * In this case start Lab Recruits Environment with the desired level
		 */

		try {
			// Define the desired level Environment and starts the Lab Recruits Game Environment
			LabRecruitsConfig environment = new LabRecruitsConfig(levelName, levelsPath);
			LabRecruitsEnvironmentListener labRecruitsEnvironment = new LabRecruitsEnvironmentListener(environment);

			TestAgent testAgent = new LabRecruitsAgentTESTAR(agentId).attachState(new BeliefState()).attachEnvironment(labRecruitsEnvironment);

			Util.pause(5);

			System.out.println("Welcome to the iv4XR test: " + levelName);

			if(System.getProperty("os.name").contains("Windows")) {
				win = WinProcess.fromProcName("LabRecruits.exe");
				this.set(IV4XRtags.windowsProcess, win);
				this.set(Tags.PID, win.pid());
			} else {
				this.set(Tags.PID, (long)0);
			}

			this.set(IV4XRtags.iv4xrLabRecruitsEnvironment, labRecruitsEnvironment);
			this.set(IV4XRtags.iv4xrTestAgent, testAgent);

		} catch(Exception e) {
			System.err.println(String.format("EnvironmentConfig ERROR: Trying to loas LabRecruits level %s - %s ", levelsPath, levelName));
			System.err.println(e.getMessage());
			stop();

			throw new SystemStartException(e);
		}

		iv4XR = this;
	}

	@Override
	public void stop() throws SystemStopException {
		// Close the SUT plugin connection
		labRecruitsTestServer.close();
		// If OS is windows also stop the process
		if(System.getProperty("os.name").contains("Windows")) {
			win.stop();
		}
	}

	@Override
	public boolean isRunning() {
		if(System.getProperty("os.name").contains("Windows")) {
			return win.isRunning();
		}
		return labRecruitsTestServer.isRunning();
	}

	@Override
	public String getStatus() {
		if(System.getProperty("os.name").contains("Windows")) {
			return win.getStatus();
		}
		return "labRecruitsTestServer running ? " + labRecruitsTestServer.isRunning();
	}

}

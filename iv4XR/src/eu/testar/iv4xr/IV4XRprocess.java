/***************************************************************************************************
 *
 * Copyright (c) 2020 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 Open Universiteit - www.ou.nl
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

package eu.testar.iv4xr;

import java.util.List;

import org.fruit.Util;
import org.fruit.alayer.SUT;
import org.fruit.alayer.SUTBase;
import org.fruit.alayer.Tags;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.alayer.exceptions.SystemStopException;
import org.fruit.alayer.windows.WinProcess;

import environments.EnvironmentConfig;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.listener.LabRecruitsEnvironmentListener;

/**
 * This class represents the IV4XR process, is creating the OS process and the IV4XR Environment
 * 
 * Because Lab Recruits is currently the only SUT example, we are using only Windows OS process functionality
 */
public class IV4XRprocess extends SUTBase {

	public static IV4XRprocess iv4XR = null;

	private static WinProcess win;

	private IV4XRprocess(String path) {
		String[] parts = path.split(" ");
		String labPath = parts[0].replace("\"", "");
		String levelsPath = parts[1].replace("\"", "");
		String levelName = parts[2].replace("\"", "");

		/**
		 * Start IV4XR SUT at Windows level
		 */
		
		win = WinProcess.fromExecutable(labPath, false);
		
		int time = 0;
		
		while(!win.isRunning() && time < 10) {
			try {
				this.wait(1000);
				time += 1;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// (Lab Recruits) Also wait a bit for initial printed information
		Util.pause(10);
		
		/**
		 * Start IV4XR SUT at JSON - WOM level
		 * In this case start Lab Recruits Environment with the desired level
		 */

		// Define the desired level Environment and starts the Lab Recruits Game Environment
		EnvironmentConfig environment = new EnvironmentConfig(levelName, levelsPath);
		LabRecruitsEnvironmentListener labRecruitsEnvironment = new LabRecruitsEnvironmentListener(environment);
		
		Util.pause(1);
		// SocketEnvironment socketEnvironment = new SocketEnvironment(labRecruitsEnvironment.host, labRecruitsEnvironment.port);

		// presses "Play" in the game for you
		labRecruitsEnvironment.startSimulation();
		
		Util.pause(1);

		System.out.println("Welcome to the iv4XR test: " + levelName);

		this.set(IV4XRtags.windowsProcess, win);
		this.set(Tags.PID, win.pid());
		this.set(IV4XRtags.iv4xrLabRecruitsEnvironment, labRecruitsEnvironment);
		//this.set(IV4XRtags.iv4xrSocketEnvironment, socketEnvironment);
		
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

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
import spaceEngineers.SpaceEngEnvironment;

public class SpaceEngineersProcess extends SUTBase {

	public static SpaceEngineersProcess iv4XR = null;

	private static WinProcess win;

	private SpaceEngineersProcess(String processName) {
		Assert.notNull(processName);
		
		String[] parts = processName.split(" ");
		
		if(parts.length != 1) {
			String message = "ERROR: For SpaceEngineers iv4xr SUT we only need to know:\n" 
					+ "1.- SpaceEngineers process name\n"
					+ "Example: SpaceEngineers.exe";
			throw new IllegalArgumentException(message);
		}

		/**
		 * Start IV4XR SUT at Windows level
		 */
		try {
			win = WinProcess.fromProcName(processName);
		} catch (SystemStartException | WinApiException we) {
			System.err.println(String.format("ERROR: Trying to connect %s using Windows API", processName));
			throw new SystemStartException(we.getMessage());
		}
		
		int time = 0;
		
		while(!win.isRunning() && time < 10) {
			try {
				this.wait(1000);
				time += 1;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		if(!win.isRunning()) {
			throw new SystemStartException(String.format("ERROR trying to connect with iv4xr SUT : %s", processName));
		}
		
		/**
		 * Start IV4XR SUT at JSON - WOM level
		 * In this case start Lab Recruits Environment with the desired level
		 */

		try {
			// Prepare SpaceEngineers Environment
			SpaceEngEnvironment seSocketEnvironment = SpaceEngEnvironment.localhost();
			
			Util.pause(5);

			System.out.println("Welcome to the iv4XR test: " + processName);

			this.set(IV4XRtags.windowsProcess, win);
			this.set(Tags.PID, win.pid());
			this.set(IV4XRtags.iv4xrSpaceEngEnvironment, seSocketEnvironment);
			
		} catch(Exception e) {
			System.err.println(String.format("EnvironmentConfig ERROR: Trying to connect with %s", processName));
			System.err.println(e.getMessage());
			win.stop();
			throw new SystemStartException(e);
		}
		
		iv4XR = this;
	}

	public static SpaceEngineersProcess fromExecutable(String processName) throws SystemStartException {
		if (iv4XR != null) {
			win.stop();
		}
		return new SpaceEngineersProcess(processName);
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

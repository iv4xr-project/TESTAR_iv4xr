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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.fruit.Assert;
import org.fruit.alayer.Roles;
import org.fruit.alayer.SUT;
import org.fruit.alayer.StateBuilder;
import org.fruit.alayer.Tags;
import org.fruit.alayer.exceptions.StateBuildException;

import eu.testar.iv4xr.labrecruits.LabStateFetcher;
import eu.testar.iv4xr.se.SeStateFetcher;

/**
 * This object builder will execute IV4XRStateFetcher to obtain the Widget Tree of the SUT
 */
public class IV4XRStateBuilder implements StateBuilder {

	private static final long serialVersionUID = 5818255982945782620L;

	private static final int defaultThreadPoolCount = 1;
	private final double timeOut;
	private transient ExecutorService executor;
	private String iv4xrSystem = "";

	public IV4XRStateBuilder(double timeOut, String iv4xrSystem) {
		Assert.isTrue(timeOut > 0);
		this.timeOut = timeOut;
		this.iv4xrSystem = iv4xrSystem;
		
		// Needed to be able to schedule asynchronous tasks conveniently.
		executor = Executors.newFixedThreadPool(defaultThreadPoolCount);
	}

	@Override
	public IV4XRState apply(SUT system) throws StateBuildException {
		try {
			Future<IV4XRState> future;
			if(iv4xrSystem.equals("Lab")) {
				future = executor.submit(new LabStateFetcher(system));
			} else if(iv4xrSystem.equals("Se")) {
				future = executor.submit(new SeStateFetcher(system));
			} else {
				future = executor.submit(new IV4XRStateFetcher(system));
			}

			IV4XRState state = future.get((long) (timeOut), TimeUnit.SECONDS);
			// When the SUT has a valid windowHandle store it in the state, it's required to create well aligned screenshots.
			if (system.get(Tags.HWND, null) != null){
				state.set(Tags.HWND, system.get(Tags.HWND));
			}
			return state;
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			throw new StateBuildException(e.getMessage());
		}
		catch (TimeoutException e) {
			IV4XRRootElement iv4XRrootElement;
			if(iv4xrSystem.equals("Lab")) {
				iv4XRrootElement = LabStateFetcher.buildRoot(system);
			} else if(iv4xrSystem.equals("Se")) {
				iv4XRrootElement = SeStateFetcher.buildRoot(system);
			} else {
				iv4XRrootElement = IV4XRStateFetcher.buildRoot(system);
			}

			IV4XRState iv4XRState = new IV4XRState(iv4XRrootElement);
			iv4XRState.set(Tags.Role, Roles.Process);
			iv4XRState.set(Tags.NotResponding, true);

			return iv4XRState;
		}
	}
}

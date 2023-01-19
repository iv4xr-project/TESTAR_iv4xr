/***************************************************************************************************
 *
 * Copyright (c) 2021 - 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2021 - 2022 Open Universiteit - www.ou.nl
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

package eu.testar.iv4xr.actions.se.goals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.Role;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.TaggableBase;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.iv4xr.framework.extensions.pathfinding.AStar;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.testar.iv4xr.enums.IV4XRtags;
import spaceEngineers.controller.Observer;
import spaceEngineers.iv4xr.navigation.NavigableGraph;
import spaceEngineers.model.Vec3F;

public class seActionGoal extends TaggableBase implements Action {
	private static final long serialVersionUID = -146033320741674607L;

	protected String agentId;
	protected TestAgent testAgent;

	protected Verdict actionVerdict = Verdict.OK;
	public Verdict getActionVerdict() {
		return actionVerdict;
	}

	public void run(SUT system, State state, double duration) throws ActionFailedException {
		// It has been decided to execute this action
		// Send the instructions to achieve the goal

	}

	protected List<Integer> getPath(NavigableGraph navigableGraph, int targetNodeId) { 
		// Should be 0 if the nav graph was generated at the current character position
		int startNodeId = 0;
		AStar<Integer> pathfinder = new AStar<>();
		return pathfinder.findPath(navigableGraph, startNodeId, targetNodeId);
	}

	//TODO: Improve this not reachable detection using the size and coordinates
	protected Set<Vec3F> notReachablePositions(Observer seObserver, State state)  {
		Set<Vec3F> forbiddenPositions = new HashSet<>();
		for(Widget w : state) {
			if(w.get(IV4XRtags.seSize, null) != null) {
				// If the size of the block is not 1 dimension unit
				if(!w.get(IV4XRtags.seSize).similar(new Vec3F(1,1,1), 0.1f)){
					// Create a list of non reachable action around the block
					Vec3F maxPosition = w.get(IV4XRtags.seMaxPosition);
					Vec3F minPosition = w.get(IV4XRtags.seMinPosition);
					forbiddenPositions.add(new Vec3F(maxPosition.getX(), 0f, maxPosition.getZ()));
					forbiddenPositions.add(new Vec3F(minPosition.getX(), 0f, minPosition.getZ()));
					forbiddenPositions.add(new Vec3F(minPosition.getX(), 0f, maxPosition.getZ()));
					forbiddenPositions.add(new Vec3F(maxPosition.getX(), 0f, minPosition.getZ()));
				}
			}
		}
		return forbiddenPositions;
	}

	@Override
	public String toShortString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toParametersString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString(Role... discardParameters) {
		// TODO Auto-generated method stub
		return null;
	}

}

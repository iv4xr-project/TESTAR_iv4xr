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

import agents.LabRecruitsTestAgent;
import environments.LabRecruitsEnvironment;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;

public class LabRecruitsAgentTESTAR extends LabRecruitsTestAgent {
	
    /**
     * The constructor for the test agent.
     */
	public LabRecruitsAgentTESTAR(String id) {
		super(id);
    }
	
    /**
     * The constructor for the test agent with an id or role attached to itself (this is required for agent communication).
     */
    public LabRecruitsAgentTESTAR(String id, String role) {
        super(id, role);
    }
    
    @Override
    public LabRecruitsAgentTESTAR attachState(BeliefState state) {
    	super.attachState(state);
    	return this ;
    }
    
    @Override
    public LabRecruitsAgentTESTAR attachEnvironment(LabRecruitsEnvironment env) {
    	super.attachEnvironment(env) ;
    	return this ;
    }
    
    @Override
    public LabRecruitsAgentTESTAR setGoal(GoalStructure g) {
    	this.goal = g;
    	super.setGoal(g) ;
    	return this ;
    }

    public boolean isGoalInProgress() {
    	try {
    		return goal.getStatus().inProgress();
    	} catch(Exception e) {
    		System.out.println(e.getMessage());
    		return false;
    	}
    }

}

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

package eu.testar.iv4xr.listener;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.fruit.Pair;
import org.fruit.alayer.Action;
import org.fruit.alayer.State;
import org.fruit.alayer.actions.NOP;

import agents.LabRecruitsTestAgent;
import agents.tactics.GoalLib;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.world.WorldEntity;
import eu.testar.iv4xr.actions.goals.*;
import helperclasses.datastructures.Vec3;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;

/**
 * This class allows TESTAR to save the sub-goals/sub-goal-structures information
 * used by test agents to test Lab Recruits.
 */
public class GoalLibListener {

	/**
	 * Objects we need to create a TESTAR Goal Action
	 */
	private static State state;
	private static LabRecruitsTestAgent testAgent;
	private static String agentId;

	public static void setState(State state) {
		GoalLibListener.state = state;
	}

	public static void setTestAgent(LabRecruitsTestAgent testAgent) {
		GoalLibListener.testAgent = testAgent;
	}

	public static void setAgentId(String agentId) {
		GoalLibListener.agentId = agentId;
	}

	/**
	 * Goal Actions we listened and create from the Agent
	 */
	private static List<Pair<Action,GoalStructure>> goalActionsList = new LinkedList<Pair<Action,GoalStructure>>();
	public static Action getFirstGoalActionFromList() {
		return goalActionsList.get(0).left();
	}
	public static GoalStructure getFirstGoalStructureFromList() {
		return goalActionsList.get(0).right();
	}
	public static void removeFirstGoalActionFromList() {
		GoalLibListener.goalActionsList.remove(0);
	}
	
	private static List<GoalStructure> agentSubGoals;
	public static void setAgentSubGoals(List<GoalStructure> agentSubGoals) {
		GoalLibListener.agentSubGoals = agentSubGoals;
	}

	public static void updatePendingSubGoals() {
		if(GoalLibListener.agentSubGoals.get(0).getStatus().success()) {
			System.out.println("COMPLETED! Removing from List...");
			System.out.println(GoalLibListener.agentSubGoals.get(0).getStatus());

			GoalLibListener.agentSubGoals.remove(0);
			GoalLibListener.goalActionsList.remove(0);
		}
	}
	
	// TODO: Apply correctly to merge TESTAR known Goal Actions + Agent Goals
	private static Set<Action> derivedGoalActions;
	public static void setDerivedGoalActionsTESTAR(Set<Action> derivedActionsTESTAR) {
		GoalLibListener.derivedGoalActions = derivedActionsTESTAR;
	}
	public static Set<Action> getDerivedGoalActions(){
		return derivedGoalActions;
	}

	/**
	 * Listen and create positionInCloseRange Goal Action
	 */
	public static Goal positionInCloseRange(Vec3 goalPosition) {
		Goal goal = GoalLib.positionInCloseRange(goalPosition);

		// TODO: For positionInCloseRange Check Goal vs GoalStructure
		//Action labActionGoalPositionInCloseRange = new eu.testar.iv4xr.actions.goals.labActionGoalPositionInCloseRange(state, testAgent, goal, agentId, goalPosition);
		Action executedGoalAction = new NOP();
		//goalActionsList.add(new Pair<executedGoalAction, goal>);
		
		return goal;
	}

	/**
	 * Listen and create positionsVisited Goal Action
	 */
	public static GoalStructure positionsVisited(Vec3... positions) {
		GoalStructure goalStructure = GoalLib.positionsVisited(positions);

		Action executedGoalAction = new labActionGoalPositionsVisited(state, testAgent, goalStructure, agentId, positions);
		goalActionsList.add(new Pair<Action, GoalStructure>(executedGoalAction, goalStructure));

		return goalStructure;
	}


	/**
	 * Listen and create entityInCloseRange Goal Action
	 */
	public static GoalStructure entityInCloseRange(String entityId) {
		GoalStructure goalStructure = GoalLib.entityInCloseRange(entityId);

		Action executedGoalAction = new labActionGoalEntityInCloseRange(state, testAgent, goalStructure, agentId, entityId);
		goalActionsList.add(new Pair<Action, GoalStructure>(executedGoalAction, goalStructure));

		return goalStructure;
	}


	/**
	 * Listen and create entityInteracted Goal Action
	 */
	public static GoalStructure entityInteracted(String entityId) {
		GoalStructure goalStructure = GoalLib.entityInteracted(entityId);

		Action executedGoalAction = new labActionGoalEntityInteracted(state, testAgent, goalStructure, agentId, entityId);
		goalActionsList.add(new Pair<Action, GoalStructure>(executedGoalAction, goalStructure));

		return goalStructure;
	}

	/**
	 * Listen and create entityStateRefreshed Goal Action
	 */
	public static GoalStructure entityStateRefreshed(String id){
		GoalStructure goalStructure = GoalLib.entityStateRefreshed(id);

		Action executedGoalAction = new labActionGoalEntityStateRefreshed(state, testAgent, goalStructure, agentId, id);
		goalActionsList.add(new Pair<Action, GoalStructure>(executedGoalAction, goalStructure));

		return goalStructure;
	}

	/**
	 * Listen and create entityInspected Goal Action
	 */
	public static GoalStructure entityInspected(String id, Predicate<WorldEntity> predicate){
		GoalStructure goalStructure = GoalLib.entityInspected(id, predicate);

		Action executedGoalAction = new labActionGoalEntityInspected(state, testAgent, goalStructure, agentId, id, predicate);
		goalActionsList.add(new Pair<Action, GoalStructure>(executedGoalAction, goalStructure));

		return goalStructure;
	}

	/**
	 * Listen and create entityInvariantChecked Goal Action
	 */
	public static GoalStructure entityInvariantChecked(TestAgent agent, String id, String info, Predicate<WorldEntity> predicate){
		GoalStructure goalStructure = GoalLib.entityInvariantChecked(agent, id, info, predicate);

		Action executedGoalAction = new labActionGoalEntityInvariantChecked(state, testAgent, goalStructure, agentId, id, info, predicate);
		goalActionsList.add(new Pair<Action, GoalStructure>(executedGoalAction, goalStructure));

		return goalStructure;
	}

	/**
	 * Listen and create invariantChecked Goal Action
	 */ 
	public static GoalStructure invariantChecked(TestAgent agent, String info, Predicate<BeliefState> predicate){
		GoalStructure goalStructure = GoalLib.invariantChecked(agent, info, predicate);

		Action executedGoalAction = new labActionGoalInvariantChecked(state, testAgent, goalStructure, agentId, info, predicate);
		goalActionsList.add(new Pair<Action, GoalStructure>(executedGoalAction, goalStructure));

		return goalStructure;
	}

	/**
	 * TODO: Implement this Goal Action
	 */
	public static GoalStructure memorySent(String id){
		return GoalLib.memorySent(id);
	}

	/**
	 * TODO: Implement this Goal Action
	 */
	public static Goal pingSent(String idFrom, String idTo){
		return GoalLib.pingSent(idFrom, idTo);
	}
}

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

package eu.testar.iv4xr.labrecruits.listener;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.fruit.alayer.Action;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;

import agents.tactics.GoalLib;
import es.upv.staq.testar.CodingManager;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.testar.iv4xr.actions.goals.*;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.labrecruits.LabRecruitsAgentTESTAR;
import eu.iv4xr.framework.spatial.Vec3;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import world.BeliefState;

/**
 * This class allows TESTAR to save the sub-goals/sub-goal-structures information
 * used by test agents to test Lab Recruits.
 */
public class GoalLibListener {

	// Use TESTAR State to create new goal actions
	private static State stateTESTAR;
	public static void setState(State state) {
		GoalLibListener.stateTESTAR = state;
	}

	// Use LabRecruits Agent to create new goal actions
	private static LabRecruitsAgentTESTAR agentTESTAR;
	public static void setTestAgent(LabRecruitsAgentTESTAR testAgent) {
		GoalLibListener.agentTESTAR = testAgent;
	}

	// Use LabRecruits Agent Id to create new goal actions
	private static String agentId;
	public static void setAgentId(String agentId) {
		GoalLibListener.agentId = agentId;
	}

	// List to save pending goal actions
	private static List<Action> goalActionsList = new LinkedList<>();
	public static Action getFirstGoalActionFromList() {
		return goalActionsList.get(0);
	}

	// List to save pending Sub Goal that LabRecruits Agent need to execute
	private static List<GoalStructure> agentSubGoals;
	public static void setAgentSubGoals(List<GoalStructure> agentSubGoals) {
		GoalLibListener.agentSubGoals = agentSubGoals;
	}

	// Check if the first of the pending Goals was completed
	// And remove from pending List if successfully completed
	public static boolean updatePendingSubGoals() {
		if(GoalLibListener.agentSubGoals.get(0).getStatus().success()) {
			System.out.println("COMPLETED! Removing from List...");
			System.out.println(GoalLibListener.agentSubGoals.get(0).getStatus());

			GoalLibListener.agentSubGoals.remove(0);
			GoalLibListener.goalActionsList.remove(0);
			return true;
		}
		return false;
	}

	// Use derived Actions to merge TESTAR commands knowledge and the listened goal actions
	private static Set<Action> derivedGoalActions = new HashSet<>();
	public static void setDerivedGoalActionsTESTAR(Set<Action> derivedActionsTESTAR) {
		GoalLibListener.derivedGoalActions = derivedActionsTESTAR;
	}
	public static Set<Action> getDerivedGoalActions(){
		return derivedGoalActions;
	}
	public static void addDerivedGoalAction(Action a) {
		if(a.get(Tags.AbstractIDCustom, null) == null) {
			CodingManager.buildEnvironmentActionIDs(stateTESTAR, a);
		}
		GoalLibListener.derivedGoalActions.add(a);
	}
	public static void clearDerivedGoalAction() {
		GoalLibListener.derivedGoalActions = new HashSet<>();
	}

	/**
	 * Listen and create positionInCloseRange Goal Action
	 */
	public static Goal positionInCloseRange(Vec3 goalPosition) {
		Goal goal = GoalLib.positionInCloseRange(goalPosition);

		Action executedGoalAction = new labActionGoalPositionInCloseRange(stateTESTAR, agentTESTAR, goal.lift(), agentId, goalPosition);
		goalActionsList.add(executedGoalAction);

		return goal;
	}

	/**
	 * Listen and create positionsVisited Goal Action
	 */
	public static GoalStructure positionsVisited(Vec3... positions) {
		GoalStructure goalStructure = GoalLib.positionsVisited(positions);

		Action executedGoalAction = new labActionGoalPositionsVisited(stateTESTAR, agentTESTAR, goalStructure, agentId, positions);
		goalActionsList.add(executedGoalAction);

		return goalStructure;
	}

	/**
	 * Listen and create entityInCloseRange Goal Action
	 */
	public static GoalStructure entityInCloseRange(String entityId) {
		GoalStructure goalStructure = GoalLib.entityInCloseRange(entityId);
		
		Widget widget = getWidgetFromState(stateTESTAR, entityId);

		Action executedGoalAction = new labActionGoalEntityInCloseRange(widget, agentTESTAR, goalStructure, agentId);
		goalActionsList.add(executedGoalAction);

		return goalStructure;
	}

	/**
	 * Listen and create entityInteracted Goal Action
	 */
	public static GoalStructure entityInteracted(String entityId) {
		GoalStructure goalStructure = GoalLib.entityInteracted(entityId);
		
		Widget widget = getWidgetFromState(stateTESTAR, entityId);

		Action executedGoalAction = new labActionGoalEntityInteracted(widget, agentTESTAR, goalStructure, agentId);
		goalActionsList.add(executedGoalAction);

		return goalStructure;
	}

	/**
	 * Listen and create entityStateRefreshed Goal Action
	 */
	public static GoalStructure entityStateRefreshed(String entityId){
		GoalStructure goalStructure = GoalLib.entityStateRefreshed(entityId);
		
		Widget widget = getWidgetFromState(stateTESTAR, entityId);

		Action executedGoalAction = new labActionGoalEntityStateRefreshed(widget, agentTESTAR, goalStructure, agentId);
		goalActionsList.add(executedGoalAction);

		return goalStructure;
	}

	/**
	 * Listen and create entityInspected Goal Action
	 */
	public static GoalStructure entityInspected(String entityId, Predicate<WorldEntity> predicate){
		GoalStructure goalStructure = GoalLib.entityInspected(entityId, predicate);
		
		Widget widget = getWidgetFromState(stateTESTAR, entityId);

		Action executedGoalAction = new labActionGoalEntityInspected(widget, agentTESTAR, goalStructure, agentId, predicate);
		goalActionsList.add(executedGoalAction);

		return goalStructure;
	}

	/**
	 * Listen and create entityInvariantChecked Goal Action
	 */
	public static GoalStructure entityInvariantChecked(TestAgent agent, String entityId, String info, Predicate<WorldEntity> predicate){
		GoalStructure goalStructure = GoalLib.entityInvariantChecked(agent, entityId, info, predicate);
		
		Widget widget = getWidgetFromState(stateTESTAR, entityId);

		Action executedGoalAction = new labActionGoalEntityInvariantChecked(widget, agentTESTAR, goalStructure, agentId, info, predicate);
		goalActionsList.add(executedGoalAction);

		return goalStructure;
	}

	/**
	 * Listen and create invariantChecked Goal Action
	 */ 
	public static GoalStructure invariantChecked(TestAgent agent, String info, Predicate<BeliefState> predicate){
		GoalStructure goalStructure = GoalLib.invariantChecked(agent, info, predicate);

		Action executedGoalAction = new labActionGoalInvariantChecked(stateTESTAR, agentTESTAR, goalStructure, agentId, info, predicate);
		goalActionsList.add(executedGoalAction);

		return goalStructure;
	}

	/**
	 * TODO: Implement this Goal Action
	 */
	public static Goal pingSent(String idFrom, String idTo){
		return GoalLib.pingSent(idFrom, idTo);
	}
	
	private static Widget getWidgetFromState(State state, String entityId) {
		for(Widget w : state) {
			if(w.get(IV4XRtags.entityId, "").equals(entityId)) {
				return w;
			}
		}
		return state;
	}
}

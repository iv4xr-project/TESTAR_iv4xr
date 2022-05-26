/***************************************************************************************************
 *
 * Copyright (c) 2020 - 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2020 - 2021 Open Universiteit - www.ou.nl
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

package nl.ou.testar.StateModel.iv4XR;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import org.fruit.Pair;
import org.fruit.alayer.Action;
import org.fruit.alayer.State;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.lab.commands.labActionCommandMoveInteract;
import eu.testar.iv4xr.actions.lab.commands.labActionExplorePosition;
import eu.testar.iv4xr.actions.lab.goals.labActionGoalPositionInCloseRange;
import eu.testar.iv4xr.enums.SVec3;
import nl.ou.testar.StateModel.AbstractAction;
import nl.ou.testar.StateModel.AbstractStateModel;
import nl.ou.testar.StateModel.ModelManager;
import nl.ou.testar.StateModel.StateModelManager;
import nl.ou.testar.StateModel.ActionSelection.ActionSelector;
import nl.ou.testar.StateModel.Exception.ActionNotFoundException;
import nl.ou.testar.StateModel.Persistence.PersistenceManager;
import nl.ou.testar.StateModel.Sequence.SequenceManager;

public class ModelManagerIV4XR extends ModelManager implements StateModelManager {
	
	private NavigableState previousNavigableState;
	private NavigableAction previousNavigableAction;
	private Map<String, SVec3> descriptionUnexecutedExploratoryActions;
	private Set<Action> actionExploreUnexecuted; // List of discovered exploratory actions not executed
	private Set<Action> actionNavigateUnexecuted; // List of discovered navigate actions not executed
	private Set<Action> executedActions; // List of executed actions

	/**
	 * Constructor
	 * @param abstractStateModel
	 * @param actionSelector
	 */
	public ModelManagerIV4XR(AbstractStateModel abstractStateModel, ActionSelector actionSelector, PersistenceManager persistenceManager,
			Set<Tag<?>> concreteStateTags, SequenceManager sequenceManager, boolean storeWidgets) {
		super(abstractStateModel, actionSelector, persistenceManager, concreteStateTags, sequenceManager, storeWidgets);
		this.descriptionUnexecutedExploratoryActions = new HashMap<>();
		this.actionExploreUnexecuted = new HashSet<>();
		this.actionNavigateUnexecuted = new HashSet<>();
		this.executedActions = new HashSet<>();
	}

	/**
	 * This method should be called once when a new state is reached after the execution
	 * of an action or succesfully starting the SUT.
	 * @param newState
	 * @param actions
	 */
	@Override
	public void notifyNewStateReached(State newState, Set<Action> actions) {
		// Here we have all derivedActions (explore position and navigate interaction)
		for(Action action : actions) {
			// In case of exploratory command actions
			if(action instanceof labActionExplorePosition) {
				String actionAbstractIDCustom = action.get(Tags.AbstractIDCustom);
				// If the exploratory action was not executed and not saved in the unexecuted list
				if(!actionWasExecuted(actionAbstractIDCustom) && !isSavedAsExploreUnexecuted(actionAbstractIDCustom)) {
					// Save the exploratory action as a pending to execute
					Vec3 nodePosition = ((labActionExplorePosition) action).getExplorePosition();
					SVec3 unexploredNode = new SVec3(nodePosition.x, nodePosition.y, nodePosition.z);
					this.descriptionUnexecutedExploratoryActions.put(actionAbstractIDCustom, unexploredNode);
					actionExploreUnexecuted.add(action);
				}
			}
			// In case of exploratory goal actions
			if(action instanceof labActionGoalPositionInCloseRange) {
				String actionAbstractIDCustom = action.get(Tags.AbstractIDCustom);
				// If the exploratory action was not executed and not saved in the unexecuted list
				if(!actionWasExecuted(actionAbstractIDCustom) && !isSavedAsExploreUnexecuted(actionAbstractIDCustom)) {
					// Save the exploratory action as a pending to execute
					Vec3 nodePosition = ((labActionGoalPositionInCloseRange) action).getGoalPosition();
					SVec3 unexploredNode = new SVec3(nodePosition.x, nodePosition.y, nodePosition.z);
					this.descriptionUnexecutedExploratoryActions.put(actionAbstractIDCustom, unexploredNode);
					actionExploreUnexecuted.add(action);
				}
			}
			// In case of navigable interaction actions
			if(action instanceof labActionCommandMoveInteract) {
				String actionAbstractIDCustom = action.get(Tags.AbstractIDCustom);
				// If the navigable interaction action was not executed and not saved in the unexecuted list
				if(!actionWasExecuted(actionAbstractIDCustom) && !isSavedAsNavigateUnexecuted(actionAbstractIDCustom)) {
					// Save the navigate interaction action as a pending to execute
					actionNavigateUnexecuted.add(action);
				}
			}
		}
		super.notifyNewStateReached(newState, actions);
	}

	private boolean actionWasExecuted(String actionAbstractIDCustom) {
		for(Action execAct : executedActions) {
			if(execAct.get(Tags.AbstractIDCustom).equals(actionAbstractIDCustom)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSavedAsExploreUnexecuted(String actionAbstractIDCustom) {
		for(Action unexecAct : actionExploreUnexecuted) {
			if(unexecAct.get(Tags.AbstractIDCustom).equals(actionAbstractIDCustom)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSavedAsNavigateUnexecuted(String actionAbstractIDCustom) {
		for(Action unexecAct : actionNavigateUnexecuted) {
			if(unexecAct.get(Tags.AbstractIDCustom).equals(actionAbstractIDCustom)) {
				return true;
			}
		}
		return false;
	}

    /**
     * This method should be called when TESTAR is listening actions instead of execute them.
     * @param action
     */
    @Override
    public void notifyListenedAction(Action action) {
        try {
            actionUnderExecution = currentAbstractState.getAction(action.get(Tags.AbstractIDCustom));
        }
        catch (ActionNotFoundException ex) {
            System.out.println("Action not found in state model");
            errorMessages.add("Action with id: " + action.get(Tags.AbstractIDCustom) + " was not found in the model.");
            actionUnderExecution = new AbstractAction(action.get(Tags.AbstractIDCustom));
            currentAbstractState.addNewAction(actionUnderExecution);
        }
        concreteActionUnderExecution = ConcreteActionIV4XRFactory.createConcreteAction(action, actionUnderExecution);
        actionUnderExecution.addConcreteActionId(concreteActionUnderExecution.getActionId());
        System.out.println("Executing iv4xr action: " + action.get(Tags.Desc));
        System.out.println("----------------------------------");

        // if we have error messages, we tell the sequence manager about it now, right before we move to a new state
        if (errorMessages.length() > 0) {
            sequenceManager.notifyErrorInCurrentState(errorMessages.toString());
            errorMessages = new StringJoiner(", ");
        }
    }
    
    /**
     * This method should be called when an action is about to be executed.
     * @param action
     */
    @Override
    public void notifyActionExecution(Action action) {
        // the action that is executed should always be traceable to an action on the current abstract state
        // in other words, we should be able to find the action on the current abstract state
        try {
            actionUnderExecution = currentAbstractState.getAction(action.get(Tags.AbstractIDCustom));
        }
        catch (ActionNotFoundException ex) {
            System.out.println("Action not found in state model");
            errorMessages.add("Action with id: " + action.get(Tags.AbstractIDCustom) + " was not found in the model.");
            actionUnderExecution = new AbstractAction(action.get(Tags.AbstractIDCustom));
            currentAbstractState.addNewAction(actionUnderExecution);
        }
        concreteActionUnderExecution = ConcreteActionIV4XRFactory.createConcreteAction(action, actionUnderExecution);
        actionUnderExecution.addConcreteActionId(concreteActionUnderExecution.getActionId());
        System.out.println("Executing iv4xr action: " + action.get(Tags.Desc));
        System.out.println("----------------------------------");

        // if we have error messages, we tell the sequence manager about it now, right before we move to a new state
        if (errorMessages.length() > 0) {
            sequenceManager.notifyErrorInCurrentState(errorMessages.toString());
            errorMessages = new StringJoiner(", ");
        }

        // Remove from descriptionUnexecutedExploratoryActions if exists
        descriptionUnexecutedExploratoryActions.remove(action.get(Tags.AbstractIDCustom));

        // TODO: Improve next finding code
        Action findAction = null;
        for(Action unexecutedAct : actionExploreUnexecuted) {
        	if(unexecutedAct.get(Tags.AbstractIDCustom).equals(action.get(Tags.AbstractIDCustom))) {
        		findAction = unexecutedAct;
        		break;
        	}
        }
        if(findAction!=null) {
        	//System.out.println("remove found action : " + findAction.toShortString());
        	actionExploreUnexecuted.remove(findAction); // Remove from unexecuted
        	executedActions.add(findAction); // Add to executed
        }
        // Do not remove from actionNavigateUnexecuted list because we use the info in notifyNewNavigableState
        // And we are going to reset the list anyway
    }

    /**
     * This method should be called when TESTAR has been exploring the iv4xr environment 
     * and has discovered a navigable state. 
     */
    @Override
    public void notifyNewNavigableState(Set<SVec3> navigableNodes, Set<Pair<String, Boolean>> reachableEntities, String executedNavigableActionDescription, String abstractAction) {
    	// Create the navigableState, the id will be based on the nodes or entities (or both?)
    	NavigableState navigableState = new NavigableState(navigableNodes, reachableEntities);
    	navigableState.setModelIdentifier(abstractStateModel.getModelIdentifier());

    	// save the information about the exploratory actions
    	if(descriptionUnexecutedExploratoryActions.isEmpty()) {
    		navigableState.addUnexecutedExploratoryAction("empty", new SVec3(0, 0, 0));
    	} else {
    		for(Map.Entry<String, SVec3> entry : descriptionUnexecutedExploratoryActions.entrySet()) {
    			navigableState.addUnexecutedExploratoryAction(entry.getKey(), entry.getValue());
    		}
    	}

    	// Add all discovered Actions as existing outgoing
    	for(Action action : actionNavigateUnexecuted) {
    		NavigableAction navigableAction = new NavigableAction(action.get(Tags.AbstractIDCustom), action.get(Tags.Desc), navigableState.getId());
    		navigableAction.setModelIdentifier(abstractStateModel.getModelIdentifier());
    		navigableState.addOutgoingNavigableAction(navigableAction.getId(), navigableAction);
    	}

    	// Create the navigableAction, the id will be based on the description and the navigableState identifier
    	NavigableAction executedNavigableAction = new NavigableAction(abstractAction, executedNavigableActionDescription, navigableState.getId());
    	executedNavigableAction.setModelIdentifier(abstractStateModel.getModelIdentifier());

    	// and reset for the next exploration iteration
    	descriptionUnexecutedExploratoryActions.clear();
    	actionExploreUnexecuted.clear();
    	actionNavigateUnexecuted.clear();
    	executedActions.clear();

    	// Save a navigable transition, or a not complete explored navigable state
    	if(previousNavigableState != null && previousNavigableAction != null) {
    		persistenceManager.persistNavigableState(previousNavigableState, previousNavigableAction, navigableState);
    	} else {
    		persistenceManager.persistNavigableState(null, null, navigableState);
    	}

    	previousNavigableState = navigableState;
    	previousNavigableAction = executedNavigableAction;
    }

    @Override
    public void notifyNewNavigableState(Set<SVec3> navigableNodes, Set<SVec3> unexploredNodes, Set<Pair<String, Boolean>> reachableEntities, String actionDescription, String abstractAction) {
    	// Space Engineers
    	// If the descriptionUnexecutedExploratoryActions is Empty, add the unexecuted actions with the specific indicated unexplored nodes
    	if(descriptionUnexecutedExploratoryActions.isEmpty()) {
    		for(SVec3 node : unexploredNodes) {
    			// TODO: Space Engineers use seGoalExplorePosition identifier
    			descriptionUnexecutedExploratoryActions.put("TEMP"+String.valueOf(Objects.hash(node)), node);
    		}
    	}
    	// Then invoke notifyNewNavigableState to create and persist the navigableState
    	notifyNewNavigableState(navigableNodes, reachableEntities, actionDescription, abstractAction);
    }

    /**
     * This method uses the abstract state model to return the abstract id of an action to execute
     * @return
     */
    @Override
    public Action getAbstractActionToExecute(Set<Action> actions) {
    	if (currentAbstractState == null) {
    		return null;
    	}
    	// Prioritize the selection of unexecuted exploratory actions
    	try {
    		Action exploratoryAction = actionSelector.selectAction(actionExploreUnexecuted);
    		return exploratoryAction;
    	} catch (ActionNotFoundException e1) {
    		System.out.println("ALL exploratory actions executed, try to find an unvisited interactive action");
    	}
    	// If all unexplored exploratory actions were executed, 
    	// call unvisited abstract action selection strategy to obtain an interactive non-executed abstract action
    	return super.getAbstractActionToExecute(actions);
    }
}

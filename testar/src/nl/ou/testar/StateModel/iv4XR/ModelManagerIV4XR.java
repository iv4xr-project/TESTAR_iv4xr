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
import java.util.Set;
import java.util.StringJoiner;

import org.fruit.Pair;
import org.fruit.alayer.Action;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;

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
	//private NavigableAction previousNavigableAction;
	private Map<String, SVec3> descriptionUnexecutedExploratoryActions;
	private Set<Action> actionExploreUnexecuted; // List of discovered exploratory actions not executed
	private Set<Action> executedActions; // List of executed exploratory actions

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
		this.executedActions = new HashSet<>();
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
    }

    /**
     * This method should be called when TESTAR has been exploring the iv4xr environment 
     * and has discovered a navigable state. 
     */
    @Override
    public void notifyNewNavigableState(Set<SVec3> navigableNodes, Set<Pair<String, Boolean>> reachableEntities, String actionDescription, String abstractAction) {
    	/*
    	AbstractAction abstractActionToExecute = null;
    	try {
    		abstractActionToExecute = currentAbstractState.getAction(abstractAction);
    	} catch (ActionNotFoundException e) {
    		// TODO Auto-generated catch block
    		e.printStackTrace();
    	}
    	NavigableAction navigableAction = new NavigableAction(abstractAction, abstractActionToExecute, actionDescription);
    	 */

    	// Create the navigableState, the id will be based on the nodes or entities (or both?)
    	NavigableState navigableState = new NavigableState(navigableNodes, reachableEntities);

    	// Create the navigableAction, the id will be based on the description and the navigableState identifier
    	NavigableAction navigableAction = new NavigableAction(abstractAction, actionDescription, navigableState.getId());
    	navigableAction.setModelIdentifier(abstractStateModel.getModelIdentifier());

    	// Associate the navigableAction to the navigableState
    	navigableState.addNavigableAction(navigableAction.getId(), navigableAction);

    	// save the information about the exploratory actions
    	if(descriptionUnexecutedExploratoryActions.isEmpty()) {
    		navigableState.addUnexecutedExploratoryAction("empty", new SVec3(0, 0, 0));
    	} else {
    		for(Map.Entry<String, SVec3> entry : descriptionUnexecutedExploratoryActions.entrySet()) {
    			navigableState.addUnexecutedExploratoryAction(entry.getKey(), entry.getValue());
    		}
    	}

    	//System.out.println("ModelManagerIV4XR notifyNewNavigableState clear actions lists");
    	// and reset for the next exploration iteration
    	descriptionUnexecutedExploratoryActions.clear();
    	actionExploreUnexecuted.clear();
    	executedActions.clear();

    	navigableState.setModelIdentifier(abstractStateModel.getModelIdentifier());

    	// Save a navigable transition, or a not complete explored navigable state
    	if(previousNavigableState != null /*&& previousNavigableAction != null*/) {
    		persistenceManager.persistNavigableState(previousNavigableState, navigableAction, navigableState);
    	} else {
    		persistenceManager.persistNavigableState(null, null, navigableState);
    	}

    	previousNavigableState = navigableState;
    	//previousNavigableAction = navigableAction;
    }

    /**
     * Add all not executed actions in a set list, 
     * to indicate that the navigable state needs to continue with the exploration. 
     */
    @Override
    public void notifyUnexecutedExploratoryActions(Map<String, SVec3> unexecutedExploratoryActions, Set<Action> actions) {
    	for(Map.Entry<String, SVec3> entry : unexecutedExploratoryActions.entrySet()) {
    		if(!this.descriptionUnexecutedExploratoryActions.containsKey(entry.getKey())) {
    			this.descriptionUnexecutedExploratoryActions.put(entry.getKey(), entry.getValue());
    		}
    	}

    	// We have discovered new exploratory actions, but these may have been executed previously
    	for(Action discoveredAction : actions) {
    		if(!actionWasExecuted(discoveredAction) && !isSavedAsUnexecuted(discoveredAction)) {
    			actionExploreUnexecuted.add(discoveredAction);
    		}
    	}
    }

    private boolean actionWasExecuted(Action action) {
    	String abstractIdCustom = action.get(Tags.AbstractIDCustom);
    	for(Action execAct : executedActions) {
    		if(execAct.get(Tags.AbstractIDCustom).equals(abstractIdCustom)) {
    			return true;
    		}
    	}
    	return false;
    }

    private boolean isSavedAsUnexecuted(Action action) {
    	String abstractIdCustom = action.get(Tags.AbstractIDCustom);
    	for(Action unexecAct : actionExploreUnexecuted) {
    		if(unexecAct.get(Tags.AbstractIDCustom).equals(abstractIdCustom)) {
    			return true;
    		}
    	}
    	return false;
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
//    	System.out.println("********************************************");
//    	System.out.println("ModelManagerIV4XR getAbstractActionToExecute");
//    	for(Action a : actionExploreUnexecuted) {
//    		System.out.println("unexecutedExpActions: " + a.toShortString());
//    	}
    	// Prioritize the selection of unexecuted exploratory actions
    	try {
    		Action exploratoryAction = actionSelector.selectAction(actionExploreUnexecuted);
    		return exploratoryAction;
    	} catch (ActionNotFoundException e1) {
    		System.out.println("ALL exploratory actions executed, try to find an unvisited interactive action");
    	}
    	// Execute unvisited abstract action selection
    	return super.getAbstractActionToExecute(actions);
    }
}

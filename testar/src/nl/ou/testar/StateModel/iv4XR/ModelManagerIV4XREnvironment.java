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

package nl.ou.testar.StateModel.iv4XR;

import java.util.Set;
import java.util.StringJoiner;

import org.fruit.alayer.Action;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;

import nl.ou.testar.StateModel.AbstractAction;
import nl.ou.testar.StateModel.AbstractStateModel;
import nl.ou.testar.StateModel.ModelManager;
import nl.ou.testar.StateModel.StateModelManager;
import nl.ou.testar.StateModel.ActionSelection.ActionSelector;
import nl.ou.testar.StateModel.Exception.ActionNotFoundException;
import nl.ou.testar.StateModel.Persistence.PersistenceManager;
import nl.ou.testar.StateModel.Sequence.SequenceManager;

public class ModelManagerIV4XREnvironment extends ModelManager implements StateModelManager {

	/**
	 * Constructor
	 * @param abstractStateModel
	 * @param actionSelector
	 */
	public ModelManagerIV4XREnvironment(AbstractStateModel abstractStateModel, ActionSelector actionSelector, PersistenceManager persistenceManager,
			Set<Tag<?>> concreteStateTags, SequenceManager sequenceManager, boolean storeWidgets) {
		super(abstractStateModel, actionSelector, persistenceManager, concreteStateTags, sequenceManager, storeWidgets);
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
    }
	
}

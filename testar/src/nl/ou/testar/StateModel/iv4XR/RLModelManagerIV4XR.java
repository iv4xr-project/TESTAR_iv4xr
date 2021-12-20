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

import nl.ou.testar.ReinforcementLearning.QFunctions.QFunction;
import nl.ou.testar.ReinforcementLearning.RLTags;
import nl.ou.testar.ReinforcementLearning.RewardFunctions.RewardFunction;
import nl.ou.testar.StateModel.AbstractAction;
import nl.ou.testar.StateModel.AbstractState;
import nl.ou.testar.StateModel.AbstractStateModel;
import nl.ou.testar.StateModel.StateModelManager;
import nl.ou.testar.StateModel.ActionSelection.ActionSelector;
import nl.ou.testar.StateModel.Exception.ActionNotFoundException;
import nl.ou.testar.StateModel.Persistence.PersistenceManager;
import nl.ou.testar.StateModel.Sequence.SequenceManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fruit.alayer.Action;
import org.fruit.alayer.State;
import org.fruit.alayer.Tag;
import org.fruit.alayer.Tags;

import java.util.Set;

/**
 * Implementation of the {@link StateModelManager} for use of Sarsa.
 * Sarsa is a reinforcement learning (Artificial Intelligence) algorithm
 * for (sequential) action selection.
 */
public class RLModelManagerIV4XR extends ModelManagerIV4XR implements StateModelManager {

    private static final Logger logger = LogManager.getLogger(RLModelManagerIV4XR.class);

    /** The previously executed {@link AbstractAction} */
    private AbstractAction previouslySelectedAbstractAction = null;

    /**  The {@link RewardFunction} determines the reward or penalty for executing an {@link AbstractAction}
     *  The reward is used in the {@link QFunction}
     */
    private final RewardFunction rewardFunction;

    /**
     * The {@link QFunction} or Quality function determines the desirability of an {@link AbstractAction}
     */
    private final QFunction qFunction;

    private State state = null;

    private final Tag<?> tag;

    /**
     * Constructor
     *
     */
    public RLModelManagerIV4XR(final AbstractStateModel abstractStateModel,
            final ActionSelector actionSelector,
            final PersistenceManager persistenceManager,
            final Set<Tag<?>> concreteStateTags,
            final SequenceManager sequenceManager,
            final boolean storeWidgets,
            final RewardFunction rewardFunction,
            final QFunction qFunction,
            final Tag<?> tag) {
        super(abstractStateModel, actionSelector, persistenceManager, concreteStateTags, sequenceManager, storeWidgets);
        this.rewardFunction = rewardFunction;
        this.qFunction = qFunction;
        this.tag = tag;
    }

    @Override
    public void notifyNewStateReached(final State newState, final Set<Action> actions) {
        super.notifyNewStateReached(newState, actions);
        state = newState;
    }

    @Override
    public void notifyTestSequenceStopped() {
        super.notifyTestSequenceStopped();
        rewardFunction.reset();
    }

    /**
     * Gets an {@link Action} to execute and updates the Q-value of the previously executed {@link Action}
     */
    @Override
    public Action getAbstractActionToExecute(final Set<Action> actions) {
        final Action selectedAction = super.getAbstractActionToExecute(actions);
        final AbstractAction selectedAbstractAction = getAbstractAction(currentAbstractState, selectedAction);
        float reward = rewardFunction.getReward(state, getCurrentConcreteState(), currentAbstractState, selectedAbstractAction);

        logger.info("reward={} found for sequenceNumber={} and actionNumber={}", reward,
                getSequenceManager().getCurrentSequence().getNodes().size(),
                getSequenceManager().getCurrentSequenceNr());

        final double rlQValue = getQValue(previouslySelectedAbstractAction, reward, actions);

        updateQValue(previouslySelectedAbstractAction, rlQValue);
        previouslySelectedAbstractAction = selectedAbstractAction;

        log(actions, selectedAction, selectedAbstractAction);

        return selectedAction;
    }

    /**
     * Gets the {@link AbstractAction}
     * @param currentAbstractState
     * @param selectedAction
     * @return The found {@link AbstractAction} or null
     */
    private AbstractAction getAbstractAction(final AbstractState currentAbstractState, final Action selectedAction) {
        if (currentAbstractState == null || selectedAction == null) {
            return null;
        }

        try {
            return currentAbstractState.getAction(selectedAction.get(Tags.AbstractIDCustom, ""));
        } catch (final ActionNotFoundException e) {
            return null;
        }
    }

    /**
     * Get the Q-value for an {@link Action}
     *
     * @param selectedAbstractAction, can be null
     * @param reward
     */
    private double getQValue(final AbstractAction selectedAbstractAction, final float reward, final Set<Action> actions) {
        if (selectedAbstractAction == null) {
            logger.info("Update of Q-value failed because no action was found to execute");
        }
        return qFunction.getQValue(previouslySelectedAbstractAction, selectedAbstractAction, reward, currentAbstractState, actions);
    }

    private void log(final Set<Action> actions, final Action selectedAction,final AbstractAction selectedAbstractAction) {
        logger.info("Number of actions available={}", actions.size());
        if (selectedAction != null) {
            logger.info("Action selected shortString={}", selectedAction.toShortString());
        }
        if(selectedAbstractAction != null) {
            logger.info("Abstract action selected abstractActionID={}, id={}", selectedAbstractAction.getActionId(), selectedAbstractAction.getId());

            // add counter
            final int counterSelectedAbstractAction = selectedAbstractAction.getAttributes().get(RLTags.ActionCounter, 0);
            selectedAbstractAction.getAttributes().set(RLTags.ActionCounter, counterSelectedAbstractAction + 1);
            logger.info("Action selected counter={}", selectedAbstractAction.getAttributes().get(RLTags.ActionCounter));
        }
        logger.info("SequenceID={}", getSequenceManager().getSequenceID());
    }

    /**
     * Update the Q-value for an {@link Action}
     *
     * @param selectedAbstractAction, can be null
     * @param qValue
     */
    private void updateQValue(final AbstractAction selectedAbstractAction, double qValue) {
        if (selectedAbstractAction == null) {
            logger.warn("Update of Q-value failed because no action was found to execute");
            return;
        }

        if (previouslySelectedAbstractAction == null) {
            logger.warn("Update of Q-value failed because no previous action was found");
            return;
        }
        logger.info("Q-value of abstractAction before updating with ID={} and q-value={}", previouslySelectedAbstractAction.getId(),  previouslySelectedAbstractAction.getAttributes().get(RLTags.SarsaValue, 0f));
        previouslySelectedAbstractAction.addAttribute(RLTags.SarsaValue, (float) qValue);
        logger.info("Q-value of abstractAction after updating with ID={} and q-value={}", previouslySelectedAbstractAction.getId(),  previouslySelectedAbstractAction.getAttributes().get(RLTags.SarsaValue));
    }
}

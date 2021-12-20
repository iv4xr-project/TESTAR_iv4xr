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

package nl.ou.testar.StateModel;

import es.upv.staq.testar.CodingManager;

import es.upv.staq.testar.NativeLinker;
import es.upv.staq.testar.OperatingSystems;

import nl.ou.testar.ReinforcementLearning.ActionSelectors.ReinforcementLearningActionSelector;
import nl.ou.testar.ReinforcementLearning.Policies.PolicyFactory;
import nl.ou.testar.ReinforcementLearning.QFunctions.QFunction;
import nl.ou.testar.ReinforcementLearning.QFunctions.QFunctionFactory;
import nl.ou.testar.ReinforcementLearning.RewardFunctions.RewardFunction;
import nl.ou.testar.ReinforcementLearning.RewardFunctions.RewardFunctionFactory;
import nl.ou.testar.ReinforcementLearning.Utils.ReinforcementLearningUtil;

import nl.ou.testar.StateModel.ActionSelection.ActionSelector;
import nl.ou.testar.StateModel.ActionSelection.CompoundFactory;
import nl.ou.testar.StateModel.Event.StateModelEventListener;
import nl.ou.testar.StateModel.Persistence.PersistenceManager;
import nl.ou.testar.StateModel.Persistence.PersistenceManagerFactory;
import nl.ou.testar.StateModel.Persistence.PersistenceManagerFactoryBuilder;
import nl.ou.testar.StateModel.Sequence.SequenceManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import nl.ou.testar.StateModel.iv4XR.ModelManagerIV4XR;

import org.fruit.alayer.Tag;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class StateModelManagerFactory {

    private static final Logger logger = LogManager.getLogger(StateModelManagerFactory.class);

    public static StateModelManager getStateModelManager(Settings settings) {
        // first check if the state model module is enabled
        if(!settings.get(ConfigTags.StateModelEnabled)) {
            return new DummyModelManager();
        }

        Set<Tag<?>> abstractTags = Arrays.stream(CodingManager.getCustomTagsForAbstractId()).collect(Collectors.toSet());
        if (abstractTags.isEmpty()) {
            throw new RuntimeException("No Abstract State Attributes were provided in the settings file");
        }

        Set<Tag<?>> concreteStateTags = Arrays.stream(CodingManager.getCustomTagsForConcreteId()).collect(Collectors.toSet());
        if (concreteStateTags.isEmpty()) {
            throw new RuntimeException("No concrete State Attributes were provided in the settings file");
        }

        // get a persistence manager
        PersistenceManagerFactoryBuilder.ManagerType managerType;
        if (settings.get(ConfigTags.DataStoreMode).equals(PersistenceManager.DATA_STORE_MODE_NONE)) {
            managerType = PersistenceManagerFactoryBuilder.ManagerType.DUMMY;
        }
        else {
            managerType = PersistenceManagerFactoryBuilder.ManagerType.valueOf(settings.get(ConfigTags.DataStore).toUpperCase());
        }
        PersistenceManagerFactory persistenceManagerFactory = PersistenceManagerFactoryBuilder.createPersistenceManagerFactory(managerType);
        PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager(settings);

        // get the abstraction level identifier that uniquely identifies the state model we are testing against.
        String modelIdentifier = CodingManager.getAbstractStateModelHash(settings.get(ConfigTags.ApplicationName),
                settings.get(ConfigTags.ApplicationVersion));

        // we need a sequence manager to record the sequences
        Set<StateModelEventListener> eventListeners = new HashSet<>();
        eventListeners.add((StateModelEventListener) persistenceManager);
        SequenceManager sequenceManager = new SequenceManager(eventListeners, modelIdentifier);
        
        // should we store widgets?
        boolean storeWidgets = settings.get(ConfigTags.StateModelStoreWidgets);

        // We want to use one of the iv4xr Model Manager
        if(NativeLinker.getPLATFORM_OS().contains(OperatingSystems.IV4XR_LAB)
        		|| NativeLinker.getPLATFORM_OS().contains(OperatingSystems.IV4XR_SE)) {

        	// Check if we want to use iv4xr model manager with RL framework
        	if (settings.get(ConfigTags.StateModelReinforcementLearningEnabled, false)) {
        		System.out.println("State Model iv4xr Reinforcement Learning Model Manager");
        		logger.info("State Model iv4xr Reinforcement Learning Model Manager");
        		
            	// create the abstract state model for the iv4xr RL model manager
            	AbstractStateModelReinforcementLearning abstractStateModelRL = new AbstractStateModelReinforcementLearning(modelIdentifier,
            			settings.get(ConfigTags.ApplicationName),
            			settings.get(ConfigTags.ApplicationVersion),
            			abstractTags,
            			persistenceManager != null ? (StateModelEventListener) persistenceManager : null);

        		final ActionSelector actionSelector = new ReinforcementLearningActionSelector(PolicyFactory.getPolicy(settings)) ;
        		final RewardFunction rewardFunction = RewardFunctionFactory.getRewardFunction(settings);
        		final QFunction qFunction = QFunctionFactory.getQFunction(settings);
        		Tag<?> tag = ReinforcementLearningUtil.getTag(settings);

        		return new iv4xrRLModelManager(abstractStateModelRL, 
        				actionSelector, 
        				persistenceManager, 
        				concreteStateTags, 
        				sequenceManager, 
        				storeWidgets,
        				rewardFunction,
        				qFunction,
        				tag);
        	}

        	// If not return the iv4xr Model Manager without RL
        	System.out.println("State Model Manager for iv4XR selected");
        	logger.info("State Model Manager for iv4XR selected");
        	
        	// create the abstract state model and then the state model manager
        	AbstractStateModel abstractStateModelListener = new AbstractStateModel(modelIdentifier,
        			settings.get(ConfigTags.ApplicationName),
        			settings.get(ConfigTags.ApplicationVersion),
        			abstractTags,
        			persistenceManager instanceof StateModelEventListener ? (StateModelEventListener) persistenceManager : null);

        	// Prepare an action selector not related with RL framework
        	ActionSelector actionSelector = CompoundFactory.getCompoundActionSelector(settings);

        	return new ModelManagerIV4XR(abstractStateModelListener, 
        			actionSelector, 
        			persistenceManager, 
        			concreteStateTags, 
        			sequenceManager, 
        			storeWidgets);
        }

        // Default AbstractStateModel and ModelManager (no iv4xr no RL)
        // create the abstract state model and then the state model manager
        AbstractStateModel abstractStateModel = new AbstractStateModel(modelIdentifier,
        		settings.get(ConfigTags.ApplicationName),
        		settings.get(ConfigTags.ApplicationVersion),
        		abstractTags,
        		persistenceManager != null ? (StateModelEventListener) persistenceManager : null);
        ActionSelector actionSelector = CompoundFactory.getCompoundActionSelector(settings);

        logger.info("State model with modelManager selected");
        return new ModelManager(abstractStateModel,
        		actionSelector,
        		persistenceManager,
        		concreteStateTags,
        		sequenceManager,
        		storeWidgets);
    }
}

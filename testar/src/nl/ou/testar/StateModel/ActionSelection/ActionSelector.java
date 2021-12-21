package nl.ou.testar.StateModel.ActionSelection;

import nl.ou.testar.StateModel.AbstractAction;
import nl.ou.testar.StateModel.AbstractState;
import nl.ou.testar.StateModel.AbstractStateModel;
import nl.ou.testar.StateModel.Exception.ActionNotFoundException;
import nl.ou.testar.StateModel.iv4XR.AbstractStateModelIV4XR;
import nl.ou.testar.StateModel.iv4XR.NavigableState;

public interface ActionSelector {

    /**
     * This method returns an action to execute
     * @param currentState
     * @param abstractStateModel
     * @return
     */
    public AbstractAction selectAction(final AbstractState currentState, final AbstractStateModel abstractStateModel) throws ActionNotFoundException;

    /**
     * This method returns a navigable action to execute
     * @param currentNavigableState
     * @param abstractStateModeliv4xr
     * @return
     */
    public AbstractAction selectAction(NavigableState currentNavigableState, AbstractStateModelIV4XR abstractStateModeliv4xr) throws ActionNotFoundException;

}

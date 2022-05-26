package nl.ou.testar.StateModel;

import org.fruit.Pair;
import org.fruit.alayer.Action;
import org.fruit.alayer.State;

import eu.testar.iv4xr.enums.SVec3;

import java.util.Set;

public interface StateModelManager {
    void notifyNewStateReached(State newState, Set<Action> actions);

    void notifyActionExecution(Action action);
    
    void notifyListenedAction(Action action);

    void notifyTestingEnded();

    Action getAbstractActionToExecute(Set<Action> actions);

    void notifyTestSequencedStarted();

    void notifyTestSequenceStopped();

    void notifyTestSequenceInterruptedByUser();

    void notifyTestSequenceInterruptedBySystem(String message);
    
    void notifyNewNavigableState(Set<SVec3> navigableNodes, Set<Pair<String, Boolean>> reachableEntities, String actionDescription, String abstractAction);

    void notifyNewNavigableState(Set<SVec3> navigableNodes, Set<SVec3> unexploredNodes, Set<Pair<String, Boolean>> reachableEntities, String actionDescription, String abstractAction);
}

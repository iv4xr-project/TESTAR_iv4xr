package nl.ou.testar.StateModel;

import org.fruit.Pair;
import org.fruit.alayer.Action;
import org.fruit.alayer.State;

import eu.testar.iv4xr.enums.SVec3;

import java.util.Set;

public class DummyModelManager implements StateModelManager{

    @Override
    public void notifyNewStateReached(State newState, Set<Action> actions) {

    }

    @Override
    public void notifyActionExecution(Action action) {

    }
    
	@Override
	public void notifyListenedAction(Action action) {
		
	}

    @Override
    public void notifyTestingEnded() {

    }

    @Override
    public Action getAbstractActionToExecute(Set<Action> actions) {
        return null;
    }

    @Override
    public void notifyTestSequencedStarted() {

    }

    @Override
    public void notifyTestSequenceStopped() {

    }

    @Override
    public void notifyTestSequenceInterruptedByUser() {

    }

    @Override
    public void notifyTestSequenceInterruptedBySystem(String message) {

    }

    @Override
    public void notifyNewNavigableState(Set<SVec3> navigableNodes, Set<Pair<String, Boolean>> reachableEntities, String actionDescription, String abstractAction) {

    }
}

package gcd_environment;

import org.fruit.alayer.Action;
import org.fruit.alayer.Role;
import org.fruit.alayer.Roles;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.TaggableBase;
import org.fruit.alayer.Tags;

public class GameGCDAction extends TaggableBase implements Action {
	
	private static final long serialVersionUID = 7251078897607270887L;
	private final transient nl.uu.cs.aplib.mainConcepts.Action gameAction;
	
	public GameGCDAction(nl.uu.cs.aplib.mainConcepts.Action gameAction, State state){
		this.gameAction = gameAction;
		this.set(Tags.Role, Roles.System);
		this.set(Tags.OriginWidget, state);
	}

	@Override
	public void run(SUT system, State state, double duration) {
		//
	}

	@Override
	public String toShortString() {
		return gameAction.toString();
	}

	@Override
	public String toParametersString() {
		return gameAction.toString();
	}

	@Override
	public String toString(Role... discardParameters) {
		return gameAction.toString();
	}

}

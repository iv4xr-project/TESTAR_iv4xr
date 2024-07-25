package org.testar.iv4xr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.fruit.alayer.Action;
import org.fruit.alayer.SUT;
import org.fruit.alayer.SUTBase;
import org.fruit.alayer.State;
import org.fruit.alayer.StdState;
import org.fruit.alayer.StdWidget;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.SystemStopException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.lab.goals.labActionGoalEntityInteracted;
import eu.testar.iv4xr.actions.lab.goals.labActionGoalPositionInCloseRange;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.labrecruits.LabRecruitsAgentTESTAR;

public class LabRecruitsExplorerTest {

	@Rule
	public RepeatRule repeatRule = new RepeatRule();

	private SUT system;

	@Before
	public void setUp() {
		system = new SUTBase() {
			@Override
			public void stop() throws SystemStopException {}
			@Override
			public void setNativeAutomationCache() {}
			@Override
			public boolean isRunning() {return false;}
			@Override
			public String getStatus() {return null;}
		};
	}

	@Test
	public void testMemorizeActionEntity() {
		LabRecruitsExplorer labRecruitsExplorer = new LabRecruitsExplorer();
		system.set(IV4XRtags.iv4xrTestAgent, new LabRecruitsAgentTESTAR("agent1"));

		// Create Action for button 1, which must maintain one memorized action
		Widget widgetButton1 = new StdWidget();
		widgetButton1.set(IV4XRtags.entityId, "button1");
		widgetButton1.set(IV4XRtags.entityPosition, new Vec3(0, 0, 1));
		Action actionInteractEntityButton1 = new labActionGoalEntityInteracted(widgetButton1, system);

		labRecruitsExplorer.memorizeActionEntity(actionInteractEntityButton1);
		assertEquals(1, labRecruitsExplorer.getNonInteractedActionEntities().size());

		// Create Action for button 2, which must maintain two memorized action
		Widget widgetButton2 = new StdWidget();
		widgetButton2.set(IV4XRtags.entityId, "button2");
		widgetButton2.set(IV4XRtags.entityPosition, new Vec3(1, 0, 1));

		Action actionInteractEntityButton2 = new labActionGoalEntityInteracted(widgetButton2, system);

		labRecruitsExplorer.memorizeActionEntity(actionInteractEntityButton2);
		assertEquals(2, labRecruitsExplorer.getNonInteractedActionEntities().size());

		// Repeat the derivation of an Action for button 2, which must maintain two memorized action and avoid repeated entries
		Action actionInteractEntityButton2R = new labActionGoalEntityInteracted(widgetButton2, system);

		labRecruitsExplorer.memorizeActionEntity(actionInteractEntityButton2R);
		assertEquals(2, labRecruitsExplorer.getNonInteractedActionEntities().size());

		// Execute button1 action, which should remember button 1 is executed and maintain the memory of button 2
		assertEquals(0, labRecruitsExplorer.getInteractedEntities().size());
		labRecruitsExplorer.addExecutedAction(actionInteractEntityButton1, false);
		assertEquals(1, labRecruitsExplorer.getInteractedEntities().size());
		assertTrue(labRecruitsExplorer.getInteractedEntities().contains("button1"));
		assertEquals(1, labRecruitsExplorer.getNonInteractedActionEntities().size());

		// Adding the same button 1 is not making TESTAR to memorize already interacted buttons
		labRecruitsExplorer.memorizeActionEntity(actionInteractEntityButton1);
		assertEquals(1, labRecruitsExplorer.getNonInteractedActionEntities().size());

		// Repeat the derivation of an Action for button 2, which must maintain one memorized action and avoid repeated entries
		labRecruitsExplorer.memorizeActionEntity(actionInteractEntityButton2R);
		assertEquals(1, labRecruitsExplorer.getNonInteractedActionEntities().size());

		// Finally adding a new button3 it works
		Widget widgetButton3 = new StdWidget();
		widgetButton3.set(IV4XRtags.entityId, "button3");
		widgetButton3.set(IV4XRtags.entityPosition, new Vec3(1, 0, 2));

		Action actionInteractEntityButton3 = new labActionGoalEntityInteracted(widgetButton3, system);

		labRecruitsExplorer.memorizeActionEntity(actionInteractEntityButton3);
		assertEquals(2, labRecruitsExplorer.getNonInteractedActionEntities().size());
	}

	@Test
	public void testMemorizeActionPositions() {
		LabRecruitsExplorer labRecruitsExplorer = new LabRecruitsExplorer();
		system.set(IV4XRtags.iv4xrTestAgent, new LabRecruitsAgentTESTAR("agent1"));
		State state = new StdState();
		state.set(IV4XRtags.entityId, "state");

		// Create Action for position 1,0,0, which must maintain one memorized position
		Vec3 goalPosition100 = new Vec3(1.0f, 0.0f, 0.0f);
		Action exploreAction100 = new labActionGoalPositionInCloseRange(state, system, goalPosition100);
		labRecruitsExplorer.memorizeActionPosition(exploreAction100);
		assertEquals(1, labRecruitsExplorer.getUnexploredActionPositions().size());

		// Create Action for position 1,0,1, which must maintain two memorized position
		Vec3 goalPosition101 = new Vec3(1.0f, 0.0f, 1.0f);
		Action exploreAction101 = new labActionGoalPositionInCloseRange(state, system, goalPosition101);
		labRecruitsExplorer.memorizeActionPosition(exploreAction101);
		assertEquals(2, labRecruitsExplorer.getUnexploredActionPositions().size());

		// Repeat Action for position 1,0,1, which must maintain two memorized position
		Vec3 goalPosition101R = new Vec3(1.0f, 0.0f, 1.0f);
		Action exploreAction101R = new labActionGoalPositionInCloseRange(state, system, goalPosition101R);
		labRecruitsExplorer.memorizeActionPosition(exploreAction101R);
		assertEquals(2, labRecruitsExplorer.getUnexploredActionPositions().size());

		// Execute position 1,0,0, which should remember 1,0,0 is executed and maintain the memory of 1,0,1
		assertEquals(0, labRecruitsExplorer.getExploredPositions().size());
		labRecruitsExplorer.addExecutedAction(exploreAction100, false);
		assertEquals(1, labRecruitsExplorer.getExploredPositions().size());
		assertTrue(labRecruitsExplorer.getExploredPositions().contains(goalPosition100));
		assertEquals(1, labRecruitsExplorer.getUnexploredActionPositions().size());

		// Repeat Action for position 1,0,1, which must maintain one memorized position
		labRecruitsExplorer.memorizeActionPosition(exploreAction101R);
		assertEquals(1, labRecruitsExplorer.getUnexploredActionPositions().size());

		// Finally a new 2,0,2 position
		Vec3 goalPosition202 = new Vec3(2.0f, 0.0f, 2.0f);
		Action exploreAction202 = new labActionGoalPositionInCloseRange(state, system, goalPosition202);
		labRecruitsExplorer.memorizeActionPosition(exploreAction202);
		assertEquals(2, labRecruitsExplorer.getUnexploredActionPositions().size());
	}

	@Test
	@Repeat( times = 100 ) // Repeat to test we always select the near entity
	public void testNearActionEntity() {
		LabRecruitsExplorer labRecruitsExplorer = new LabRecruitsExplorer();
		system.set(IV4XRtags.iv4xrTestAgent, new LabRecruitsAgentTESTAR("agent1"));

		State state = new StdState();
		state.set(IV4XRtags.entityId, "state");

		Vec3 agentPosition = new Vec3(0, 0, 0);
		Widget agentWidget = new StdWidget();
		agentWidget.set(IV4XRtags.agentPosition, agentPosition);
		state.set(IV4XRtags.agentWidget, agentWidget);

		// Create Action for button 1, far away from the agent
		Widget widgetButton1 = new StdWidget();
		widgetButton1.set(IV4XRtags.entityId, "button1");
		widgetButton1.set(IV4XRtags.entityPosition, new Vec3(5, 0, 9));
		Action actionInteractEntityButton1 = new labActionGoalEntityInteracted(widgetButton1, system);
		labRecruitsExplorer.memorizeActionEntity(actionInteractEntityButton1);

		// Create Action for button 2, near the agent
		Widget widgetButton2 = new StdWidget();
		widgetButton2.set(IV4XRtags.entityId, "button2");
		widgetButton2.set(IV4XRtags.entityPosition, new Vec3(1, 0, 1));
		Action actionInteractEntityButton2 = new labActionGoalEntityInteracted(widgetButton2, system);
		labRecruitsExplorer.memorizeActionEntity(actionInteractEntityButton2);

		// Check nearest button2 is selected
		Set<Action> actions = new HashSet<>(Arrays.asList(actionInteractEntityButton1, actionInteractEntityButton2));
		Action prioAction = labRecruitsExplorer.prioritizeVisibleOfMemorizedAction(state, actions);
		assertEquals(prioAction, actionInteractEntityButton2);
		prioAction = labRecruitsExplorer.prioritizeMemorizedAction(state, actions);
		assertEquals(prioAction, actionInteractEntityButton2);
	}

	@Test
	@Repeat( times = 100 ) // Repeat to test we always select the far position
	public void testFarActionPosition() {
		LabRecruitsExplorer labRecruitsExplorer = new LabRecruitsExplorer();
		system.set(IV4XRtags.iv4xrTestAgent, new LabRecruitsAgentTESTAR("agent1"));

		State state = new StdState();
		state.set(IV4XRtags.entityId, "state");

		Vec3 agentPosition = new Vec3(0, 0, 0);
		Widget agentWidget = new StdWidget();
		agentWidget.set(IV4XRtags.agentPosition, agentPosition);
		state.set(IV4XRtags.agentWidget, agentWidget);

		// Create Action for position 9,0,9, far away from the agent
		Vec3 goalPosition909 = new Vec3(9.0f, 0.0f, 9.0f);
		Action exploreAction909 = new labActionGoalPositionInCloseRange(state, system, goalPosition909);
		labRecruitsExplorer.memorizeActionPosition(exploreAction909);

		// Create Action for position 1.0.1, near the agent
		Vec3 goalPosition101 = new Vec3(1.0f, 0.0f, 1.0f);
		Action exploreAction101 = new labActionGoalPositionInCloseRange(state, system, goalPosition101);
		labRecruitsExplorer.memorizeActionPosition(exploreAction101);

		// Check far position 9,0,9 is selected
		Set<Action> actions = new HashSet<>(Arrays.asList(exploreAction909, exploreAction101));
		Action prioAction = labRecruitsExplorer.prioritizeVisibleOfMemorizedAction(state, actions);
		assertEquals(prioAction, exploreAction909);
		prioAction = labRecruitsExplorer.prioritizeMemorizedAction(state, actions);
		assertEquals(prioAction, exploreAction909);
	}

}

//https://gist.github.com/fappel/8bcb2aea4b39ff9cfb6e
//JUnit 4 TestRule to run a test repeatedly for a specified amount of repetitions
//TODO: When migrate to JUnit 5 use the @RepeatedTest annotation

@Retention( java.lang.annotation.RetentionPolicy.RUNTIME )
@Target( { java.lang.annotation.ElementType.METHOD } )
@interface Repeat {
	public abstract int times();
}

class RepeatRule implements TestRule {

	private static class RepeatStatement extends Statement {

		private final int times;
		private final Statement statement;

		private RepeatStatement( int times, Statement statement ) {
			this.times = times;
			this.statement = statement;
		}

		@Override
		public void evaluate() throws Throwable {
			for( int i = 0; i < times; i++ ) {
				statement.evaluate();
			}
		}
	}

	@Override
	public Statement apply( Statement statement, Description description ) {
		Statement result = statement;
		Repeat repeat = description.getAnnotation( Repeat.class );
		if( repeat != null ) {
			int times = repeat.times();
			result = new RepeatStatement( times, statement );
		}
		return result;
	}
}
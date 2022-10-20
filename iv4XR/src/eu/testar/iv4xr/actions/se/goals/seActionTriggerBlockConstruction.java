package eu.testar.iv4xr.actions.se.goals;

import java.util.HashMap;
import java.util.Map;

import org.fruit.Util;
import org.fruit.alayer.SUT;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Verdict;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.ActionFailedException;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.iv4xrActionRoles;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import spaceEngineers.controller.SpaceEngineers;
import spaceEngineers.model.Block;
import spaceEngineers.model.DefinitionId;
import spaceEngineers.model.Observation;
import spaceEngineers.model.ToolbarConfigData;
import spaceEngineers.model.ToolbarLocation;
import spaceEngineers.model.Vec2F;
import spaceEngineers.model.Vec3F;

public class seActionTriggerBlockConstruction extends seActionGoal {
	private static final long serialVersionUID = -998088880955008863L;

	protected String widgetType;
	protected String widgetId;
	protected eu.iv4xr.framework.spatial.Vec3 widgetPosition;
	protected Vec3F targetPosition;
	protected Vec3 calculatedReachablePosition;
	protected final float DEGREES = 2416f;
	protected String blockType;

	public static Map<String, String> blockTypeDescriptionMap;
	static {
		blockTypeDescriptionMap = new HashMap<>();
		blockTypeDescriptionMap.put("LargeBlockArmorBlock", "Light Armor Block");
		blockTypeDescriptionMap.put("LargeHeavyBlockArmorBlock", "Heavy Armor Block");
	}

	public String getBlockType() {
		return blockType;
	}

	public seActionTriggerBlockConstruction(Widget w, SUT system, String agentId, String blockType){
		this.agentId = agentId;
		this.set(Tags.OriginWidget, w);
		this.widgetType = w.get(IV4XRtags.entityType);
		this.widgetId = w.get(IV4XRtags.entityId);
		this.widgetPosition = w.get(IV4XRtags.entityPosition);
		this.targetPosition = SVec3.labToSE(w.get(IV4XRtags.entityPosition));
		this.blockType = blockType;
		this.set(Tags.Role, iv4xrActionRoles.iv4xrActionCommandMove);
		this.set(Tags.Desc, toShortString());
		// TODO: Update with Goal Solving agents
		this.set(IV4XRtags.agentAction, false);
		this.set(IV4XRtags.newActionByAgent, false);

		this.testAgent = system.get(IV4XRtags.iv4xrTestAgent);
	}

	@Override
	public void run(SUT system, State state, double duration) throws ActionFailedException {
		rotateToBlockDestination(system);
		openSearchScreen(system);
		aimBelow(system);
		placeBlock(system);
		aimUp(system);
		equipGrinder(system);
		useGrinder(system);

		//Reset aim with jetpack
		resetAimWithJetpack(system);

		// After place block action equip an empty object
		spaceEngineers.controller.Items seItems = system.get(IV4XRtags.iv4xrSpaceEngItems);
		seItems.equip(ToolbarLocation.Companion.fromIndex(1, 1));
	}

	/**
	 * Rotate tick by tick until the agent aims the block destination. 
	 * Based on: https://github.com/iv4xr-project/iv4xr-se-plugin/blob/uubranch3D/JvmClient/src/jvmMain/java/uuspaceagent/UUTacticLib.java#L160
	 * 
	 * @param system
	 */
	protected void rotateToBlockDestination(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		spaceEngineers.controller.SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);
		spaceEngineers.controller.Observer seObserver = seController.getObserver();

		eu.iv4xr.framework.spatial.Vec3 agentPosition = SVec3.seToLab(seObserver.observe().getPosition());
		eu.iv4xr.framework.spatial.Vec3 entityPosition = SVec3.seToLab(targetPosition);
		eu.iv4xr.framework.spatial.Vec3 directionToGo = eu.iv4xr.framework.spatial.Vec3.sub(agentPosition, entityPosition);
		eu.iv4xr.framework.spatial.Vec3 agentOrientation = SVec3.seToLab(seObserver.observe().getOrientationForward());

		directionToGo.y = 0;
		agentOrientation.y = 0;

		directionToGo = directionToGo.normalized();
		agentOrientation = agentOrientation.normalized();

		float cos_alpha = eu.iv4xr.framework.spatial.Vec3.dot(agentOrientation, directionToGo);

		while(cos_alpha > -0.99f) {
			// rotate faster until the aiming is close
			if(cos_alpha > -0.95f) {seCharacter.moveAndRotate(new Vec3F(0,0,0),  new Vec2F(0, DEGREES*0.007f), 0f, 1);}
			else {seCharacter.moveAndRotate(new Vec3F(0,0,0), Vec2F.Companion.getROTATE_RIGHT(), 0f, 1);}

			agentPosition = SVec3.seToLab(seObserver.observe().getPosition());
			entityPosition = SVec3.seToLab(targetPosition);
			directionToGo = eu.iv4xr.framework.spatial.Vec3.sub(agentPosition, entityPosition);
			agentOrientation = SVec3.seToLab(seObserver.observe().getOrientationForward());

			directionToGo.y = 0;
			agentOrientation.y = 0;

			directionToGo = directionToGo.normalized();
			agentOrientation = agentOrientation.normalized();

			cos_alpha = eu.iv4xr.framework.spatial.Vec3.dot(agentOrientation, directionToGo);
		}
	}

	protected void openSearchScreen(SUT system) {
		SpaceEngineers controller = system.get(IV4XRtags.iv4xrSpaceEngineers);
		controller.getScreens().getGamePlay().showToolbarConfig();
		Util.pause(1);
		controller.getScreens().getToolbarConfig().search(blockTypeDescriptionMap.get(blockType));
		Util.pause(1);
		ToolbarConfigData tbcData = controller.getScreens().getToolbarConfig().data();
		// Create a Verdict inside the action to verify
		// Search works and the search function shows blocks
		if(tbcData.getGridItems().isEmpty()) {
			actionVerdict = new Verdict(Verdict.BLOCK_SEARCH_ERROR, 
					"The are not existing block with the name: " + blockTypeDescriptionMap.get(blockType));
			return;
		}
		// Verify that the search returns the desired block as the first element
		if(!tbcData.getGridItems().get(0).toString().equals("MyObjectBuilder_CubeBlock/" + blockType)) {
			actionVerdict = new Verdict(Verdict.BLOCK_SEARCH_ERROR, 
					"The block definition Id and the block name does not match: "
							+ blockTypeDescriptionMap.get(blockType)
							+ "MyObjectBuilder_CubeBlock/" + blockType);
			return;
		}
		Util.pause(1);
		controller.getScreens().getToolbarConfig().close();
	}

	protected void aimBelow(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		seCharacter.moveAndRotate(new Vec3F(0,0,0), new Vec2F(150f, 0), 0f, 1);
	}

	protected void placeBlock(SUT system) {
		spaceEngineers.controller.Items seItems = system.get(IV4XRtags.iv4xrSpaceEngItems);
		spaceEngineers.controller.SpaceEngineers seController = system.get(IV4XRtags.iv4xrSpaceEngineers);

		seItems.setToolbarItem(DefinitionId.Companion.cubeBlock(blockType), ToolbarLocation.Companion.fromIndex(1, 2));

		Util.pause(1);

		seItems.equip(ToolbarLocation.Companion.fromIndex(1, 2));

		Util.pause(1);

		// This is to empty the "buffer" of new block before placing the desired block
		seController.getObserver().observeBlocks();
		seController.getObserver().observeNewBlocks();

		Util.pause(1);

		seController.getBlocks().place();

		Util.pause(1);

		Observation newObsBlocks = seController.getObserver().observeNewBlocks();
		// Check that one new block exists and that is the placed one
		if(newObsBlocks.getGrids().isEmpty() || newObsBlocks.getGrids().get(0).getBlocks().isEmpty()) {
			actionVerdict = new Verdict(Verdict.BLOCK_CONSTRUCTION_ERROR, 
					"No block was placed trying to add to the level the block: " + blockType);
			return;
		}
		Block newBlock = newObsBlocks.getGrids().get(0).getBlocks().get(0);
		if(!newBlock.getDefinitionId().toString().equals("MyObjectBuilder_CubeBlock/" + blockType)) {
			actionVerdict = new Verdict(Verdict.BLOCK_CONSTRUCTION_ERROR, 
					"The desired block was not correctly placed to the level." 
							+ " Expected: " + blockType
							+ " Observed: " + newBlock.getDefinitionId().toString());
			return;
		}
	}

	protected void aimUp(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		seCharacter.moveAndRotate(new Vec3F(0,0,0), new Vec2F(-150f, 0), 0f, 1);
	}

	/**
	 * Prepare the Grinder tool in the SE tool bar. 
	 * 
	 * @param seItems
	 */
	private void equipGrinder(SUT system) {
		spaceEngineers.controller.Items seItems = system.get(IV4XRtags.iv4xrSpaceEngItems);

		seItems.setToolbarItem(DefinitionId.Companion.physicalGun("AngleGrinderItem"), ToolbarLocation.Companion.fromIndex(5, 6));
		Util.pause(0.5);
		seItems.equip(ToolbarLocation.Companion.fromIndex(5, 6));
		Util.pause(0.5);
	}

	/**
	 * Use the Grinder tool the desired amount of time. 
	 * 
	 * @param seItems
	 */
	private void useGrinder(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);

		seCharacter.beginUsingTool();
		Util.pause(2);
		seCharacter.endUsingTool();
	}

	private void resetAimWithJetpack(SUT system) {
		spaceEngineers.controller.Character seCharacter = system.get(IV4XRtags.iv4xrSpaceEngCharacter);
		seCharacter.turnOnJetpack();
		Util.pause(0.5);
		seCharacter.turnOffJetpack();
	}

	@Override
	public String toShortString() {
		return String.format("Agent %s triggering construction of block %s", agentId, blockType);
	}

}

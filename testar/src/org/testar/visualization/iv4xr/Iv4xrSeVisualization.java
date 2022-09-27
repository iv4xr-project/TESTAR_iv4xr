/***************************************************************************************************
 *
 * Copyright (c) 2021 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2021 Open Universiteit - www.ou.nl
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

package org.testar.visualization.iv4xr;

import java.awt.MouseInfo;
import java.util.HashMap;
import java.util.Map;

import org.fruit.alayer.AbsolutePosition;
import org.fruit.alayer.Canvas;
import org.fruit.alayer.Color;
import org.fruit.alayer.FillPattern;
import org.fruit.alayer.Pen;
import org.fruit.alayer.Position;
import org.fruit.alayer.Shape;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.visualizers.EllipseVisualizer;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;
import spaceEngineers.model.DoorBase;
import spaceEngineers.model.FueledPowerProducer;
import spaceEngineers.model.FunctionalBlock;
import spaceEngineers.model.TerminalBlock;

public class Iv4xrSeVisualization {

	private static Pen GreenPen = Pen.newPen().setColor(Color.Green).setFillPattern(FillPattern.Solid).setStrokeWidth(3).build();
	private static Pen YellowPen = Pen.newPen().setColor(Color.Yellow).setFillPattern(FillPattern.Solid).setStrokeWidth(3).build();
	private static Pen BlackPen = Pen.newPen().setColor(Color.Black).setFillPattern(FillPattern.Solid).setStrokeWidth(3).build();
	private static Pen BluePen = Pen.newPen().setColor(Color.Blue).setFillPattern(FillPattern.Solid).setStrokeWidth(3).build();
	private static Pen WhitePen = Pen.newPen().setColor(Color.White).setFillPattern(FillPattern.Solid).setStrokeWidth(3).build();
	private static Pen RedPen = Pen.newPen().setColor(Color.Red).setFillPattern(FillPattern.Solid).setStrokeWidth(3).build();
	private static Pen vp = Pen.PEN_IGNORE;

	private static Map<String, Pen> entitiesColors;
	static {
		entitiesColors = new HashMap<String, Pen>();
		entitiesColors.put("largeblockarmorblock", GreenPen);
		entitiesColors.put("largeheavyblockarmorblock", BlackPen);
		entitiesColors.put("largegrid", YellowPen);
		entitiesColors.put("staticgrid", YellowPen);
	}

	public static Pen getEntityColor(String entityType) {
		return entitiesColors.getOrDefault(entityType.toLowerCase().trim(), BluePen);
	}

	/**
	 * Create a panel in the top left corner of the windows screen, 
	 * to show the information of the observed/existing entities/widgets 
	 * in the current state. 
	 * 
	 * @param canvas
	 * @param state
	 */
	public static synchronized void showStateObservation(Canvas canvas, State state){
		// Prepare the visual rectangle in the left side of the screen
		Shape visualShape = org.fruit.alayer.Rect.from(0, 200, 600, 600);
		visualShape.paint(canvas, Pen.PEN_MARK_ALPHA);
		visualShape.paint(canvas, Pen.PEN_MARK_BORDER);

		canvas.text(Pen.PEN_RED, 10, 210, 0, "State Abstract Custom Identifier");
		canvas.text(Pen.PEN_BLUE, 10, 230, 0, state.get(Tags.AbstractIDCustom, "NoAbstractIDCustom"));

		// Iterate over all the observed widgets/entities to draw the Identifier in the left panel
		canvas.text(Pen.PEN_RED, 10, 250, 0, "OBSERVED widgets/blocks");

		int countGrids = 0;
		int countBlock = 0;
		for(Widget w : state) {
			if(w.get(IV4XRtags.entityId, "").contains("Grid")) {countGrids ++;}
			if(w.get(IV4XRtags.entityType, "").contains("Block")) {countBlock ++;}
		}

		canvas.text(YellowPen, 10, 270, 0, "Number of Grids: " + countGrids);
		canvas.text(GreenPen, 10, 290, 0, "Number of Blocks: " + countBlock);

		int y = 310;
		for(Widget w : state) {
			// Ignore the Agent and State
			if(w.equals(state.get(IV4XRtags.agentWidget, null)) || w.equals(state)) continue;
			// Only close position entities
			spaceEngineers.model.Vec3F widgetPosition = SVec3.labToSE(w.get(IV4XRtags.entityPosition));
			spaceEngineers.model.Vec3F agentPosition = SVec3.labToSE(state.get(IV4XRtags.agentWidget, null).get(IV4XRtags.agentPosition));
			if(widgetPosition.similar(agentPosition, 20f)) {
				// Draw a text that contains the entity identifier using the color defined on the entitiesColors map
				float distance = seDistance(widgetPosition, agentPosition);
				String info = "Type: " + w.get(IV4XRtags.entityType, "") + ", Integrity: " + w.get(IV4XRtags.seIntegrity, -1f) + ", Distance: " + distance;
				canvas.text(getEntityColor(w.get(IV4XRtags.entityType, "")), 10, y, 0, info);
				y += 20;
			}
		}
	}

	/**
	 * Create a panel in the top left corner of the windows screen, 
	 * to show the information of the observed/existing entities/widgets 
	 * in the current state. 
	 * 
	 * @param canvas
	 * @param state
	 */
	public static synchronized void showSpaceEngineersAimingElement(Canvas canvas, spaceEngineers.model.Block block){
		// Prepare the visual rectangle in the left side of the screen
		Shape visualShape = org.fruit.alayer.Rect.from(0, 200, 600, 600);
		visualShape.paint(canvas, Pen.PEN_MARK_ALPHA);
		visualShape.paint(canvas, Pen.PEN_MARK_BORDER);

		canvas.text(YellowPen, 10, 210, 0, "Block Type: " + block.getDefinitionId().toString());
		canvas.text(YellowPen, 10, 230, 0, "Block Id: " + block.getId());
		if(block instanceof TerminalBlock) {
			canvas.text(YellowPen, 10, 250, 0, "Block CustomName: " + ((TerminalBlock)block).getCustomName());
		}
		else if(block instanceof FunctionalBlock) {
			canvas.text(YellowPen, 10, 250, 0, "Block CustomName: " + ((FunctionalBlock)block).getCustomName());
		}
		else if(block instanceof DoorBase) {
			canvas.text(YellowPen, 10, 250, 0, "Block CustomName: " + ((DoorBase)block).getCustomName());
		}
		else if(block instanceof FueledPowerProducer) {
			canvas.text(YellowPen, 10, 250, 0, "Block CustomName: " + ((FueledPowerProducer)block).getCustomName());
		}

		canvas.text(BluePen, 10, 270, 0, "Block Integrity: " + String.valueOf(block.getIntegrity()));
		canvas.text(BluePen, 10, 290, 0, "Block Max Integrity: " + String.valueOf(block.getMaxIntegrity()));

		canvas.text(BlackPen, 10, 330, 0, "Block Position: " + block.getPosition());
		canvas.text(BlackPen, 10, 350, 0, "Block Orientation Forward: " + block.getOrientationForward());
		canvas.text(BlackPen, 10, 370, 0, "Block Size: " + block.getSize());
	}

	/**
	 * Numeric distance between the widget block and the agent. 
	 * 
	 * @param targetPosition
	 * @param currentPosition
	 * @return
	 */
	private static float seDistance(spaceEngineers.model.Vec3F targetPosition, spaceEngineers.model.Vec3F currentPosition) {
		return 	(Math.abs(targetPosition.getX() - currentPosition.getX())/2) + (Math.abs(targetPosition.getZ() - currentPosition.getZ())/2);
	}

	/**
	 * Create a panel on the SUT GUI, to draw multiple dots that represents 
	 * observed entities/widgets and NavMesh nodes of the current state. 
	 * 
	 * @param canvas
	 * @param state
	 * @param agentWidget
	 * @param spyIncrement
	 */
	public static synchronized void showStateElements(Canvas canvas, State state, Widget agentWidget, int spyIncrement){
		Shape stateShape = state.get(Tags.Shape);
		stateShape.paint(canvas, Pen.PEN_MARK_ALPHA);
		stateShape.paint(canvas, Pen.PEN_MARK_BORDER);

		if(agentWidget == null) {
			System.out.println("WARNING: Spy mode does not detect the Agent view");
		}

		VirtualEntitesPosition virtualEntitesPos = new VirtualEntitesPosition();

		// The center coordinate of the SUT GUI
		double centerX = state.get(Tags.Shape).x() + (state.get(Tags.Shape).width() / 2);
		double centerY = state.get(Tags.Shape).y() + (state.get(Tags.Shape).height() / 2);

		// Iterate over all the observed widgets/entities to draw the dot visualization in the GUI coordinates
		for(Widget w : state) {
			// Ignore the Agent and State dot representation
			if(w.equals(agentWidget) || w.equals(state)) continue;

			double distanceX = w.get(IV4XRtags.entityPosition).x - agentWidget.get(IV4XRtags.agentPosition).x;
			double distanceZ = w.get(IV4XRtags.entityPosition).z - agentWidget.get(IV4XRtags.agentPosition).z;

			// Draw a colored dot that represents the position observed by the agent
			Position entityPosition = new AbsolutePosition(centerX + distanceX * spyIncrement, centerY - distanceZ * spyIncrement);
			Pen entityColor = getEntityColor(w.get(IV4XRtags.entityType, ""));
			new EllipseVisualizer(entityPosition, entityColor, 10, 10).run(state, canvas, vp);
		}

		showElementFromMouse(canvas, virtualEntitesPos);
	}

	/**
	 * If the system mouse is over an specific widget, draw specific properties information. 
	 * 
	 * @param canvas
	 * @param virtualEntitesPos
	 */
	private static synchronized void showElementFromMouse(Canvas canvas, VirtualEntitesPosition virtualEntitesPos) {
		java.awt.Point mousePoint = MouseInfo.getPointerInfo().getLocation();
		Widget virtualEntity;
		if((virtualEntity = virtualEntitesPos.getEntityFromPosition(mousePoint.getX(), mousePoint.getY())) != null) {
			// Prepare the visual rectangle in the right side of the mouse/widget
			double widgetX = mousePoint.getX() + 20;
			double widgetY = mousePoint.getY();
			Shape mouseShape = org.fruit.alayer.Rect.from(widgetX, widgetY, 300, 100);
			mouseShape.paint(canvas, Pen.PEN_MARK_BORDER);

			Pen entityColor = getEntityColor(virtualEntity.get(IV4XRtags.entityType, ""));

			canvas.text(entityColor, widgetX, widgetY + 10, 0, "EntityType : " + virtualEntity.get(IV4XRtags.entityType, ""));
			canvas.text(entityColor, widgetX, widgetY + 30, 0, "EntityId : " + virtualEntity.get(IV4XRtags.entityId, ""));
			canvas.text(entityColor, widgetX, widgetY + 70, 0, "EntityPosition : " + virtualEntity.get(IV4XRtags.entityPosition, new Vec3(0, 0, 0)));
		}
	}
}

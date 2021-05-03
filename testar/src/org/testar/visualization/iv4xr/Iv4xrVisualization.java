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
import org.fruit.Util;
import org.fruit.alayer.AbsolutePosition;
import org.fruit.alayer.Action;
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
import eu.testar.iv4xr.actions.commands.labActionExplorePosition;
import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;

public class Iv4xrVisualization {

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
		entitiesColors.put("door", YellowPen);
		entitiesColors.put("firehazard", BlackPen);
		entitiesColors.put("button", GreenPen);
		entitiesColors.put("switch", GreenPen);
	}

	public static Pen getEntityColor(String entityType) {
		return entitiesColors.getOrDefault(entityType.toLowerCase(), BluePen);
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
		Shape visualShape = org.fruit.alayer.Rect.from(0, 300, 300, 300);
		visualShape.paint(canvas, Pen.PEN_MARK_ALPHA);
		visualShape.paint(canvas, Pen.PEN_MARK_BORDER);

		canvas.text(Pen.PEN_RED, 10, 310, 0, "State Abstract Custom Identifier");
		canvas.text(Pen.PEN_BLUE, 10, 330, 0, state.get(Tags.AbstractIDCustom, "NoAbstractIDCustom"));

		// Iterate over all the observed widgets/entities to draw the Identifier in the left panel
		canvas.text(Pen.PEN_RED, 10, 350, 0, "OBSERVED widgets/entites and NavMesh");
		int y = 370;
		for(Widget w : state) {
			// Ignore the State
			if(w.equals(state)) continue;

			// Draw a text that contains the entity identifier using the color defined on the entitiesColors map
			canvas.text(getEntityColor(w.get(IV4XRtags.entityType, "")), 10, y, 0, w.get(IV4XRtags.entityId, "noEntityId"));

			y += 20;
		}

		// Draw the number of observed NavMesh nodes in the left panel
		if(state.get(IV4XRtags.labRecruitsNavMesh, null) != null) {
			canvas.text(WhitePen, 10, y, 0, "Visible NavMesh Nodes : " + state.get(IV4XRtags.labRecruitsNavMesh).size());
		}
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
			virtualEntitesPos.addEntityPosition(w, centerX + distanceX * spyIncrement, centerY - distanceZ * spyIncrement);
		}

		// Iterate over all NavMesh node coordinates to draw the dot visualization in the GUI coordinates
		if(state.get(IV4XRtags.labRecruitsNavMesh, null) != null && !state.get(IV4XRtags.labRecruitsNavMesh).isEmpty()) {
			for(SVec3 nodeNavMesh : state.get(IV4XRtags.labRecruitsNavMesh)) {
				double distanceX = nodeNavMesh.x - agentWidget.get(IV4XRtags.agentPosition).x;
				double distanceZ = nodeNavMesh.z - agentWidget.get(IV4XRtags.agentPosition).z;
				new EllipseVisualizer(new AbsolutePosition(centerX + distanceX * spyIncrement, centerY - distanceZ * spyIncrement), WhitePen, 4, 4).run(state, canvas, vp);
			}
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
			canvas.text(entityColor, widgetX, widgetY + 50, 0, "EntityIsActive ? " + virtualEntity.get(IV4XRtags.labRecruitsEntityIsActive, false));
			canvas.text(entityColor, widgetX, widgetY + 70, 0, "EntityPosition : " + virtualEntity.get(IV4XRtags.entityPosition, new Vec3(0, 0, 0)));
		}
	}

	/**
	 * Draw a blink dot on the selected entity on which TESTAR is going to execute an action. 
	 * 
	 * @param canvas
	 * @param state
	 * @param agentWidget
	 * @param spyIncrement
	 */
	public static synchronized void showSelectedElement(Canvas canvas, State state, Widget agentWidget, Action action, int spyIncrement) {
		if(agentWidget == null) {
			System.out.println("WARNING: Spy mode does not detect the Agent view");
		}

		// The center coordinate of the SUT GUI
		double centerX = state.get(Tags.Shape).x() + (state.get(Tags.Shape).width() / 2);
		double centerY = state.get(Tags.Shape).y() + (state.get(Tags.Shape).height() / 2);

		Vec3 actionPosition;
		// If the action is an exploratory one, get the position to explore
		if(action instanceof labActionExplorePosition) {
			actionPosition = ((labActionExplorePosition) action).getExplorePosition();
		}
		// If the action as an origin entity position, get the position of this entity
		else if (action.get(Tags.OriginWidget).get(IV4XRtags.entityPosition, new Vec3(0, 0, 0)) != new Vec3(0, 0, 0)) {
			actionPosition = action.get(Tags.OriginWidget).get(IV4XRtags.entityPosition);
		}
		// If nothing do not draw in the canvas
		else {
			return;
		}

		double distanceX = actionPosition.x - agentWidget.get(IV4XRtags.agentPosition).x;
		double distanceZ = actionPosition.z - agentWidget.get(IV4XRtags.agentPosition).z;

		final int BLINK_COUNT = 3;
		final double BLINK_DELAY = 0.3;
		for(int i = 0; i < BLINK_COUNT; i++){
			Util.pause(BLINK_DELAY);
			canvas.begin();
			new EllipseVisualizer(new AbsolutePosition(centerX + distanceX * spyIncrement, centerY - distanceZ * spyIncrement), RedPen, 4, 4).run(state, canvas, vp);
			canvas.end();
			Util.pause(BLINK_DELAY);
			canvas.begin();
			new EllipseVisualizer(new AbsolutePosition(centerX + distanceX * spyIncrement, centerY - distanceZ * spyIncrement), RedPen, 4, 4).run(state, canvas, vp);
			canvas.end();
		}
	}
}

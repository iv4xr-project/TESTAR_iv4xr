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

package org.testar.protocols.iv4xr;

import org.fruit.alayer.AbsolutePosition;
import org.fruit.alayer.Canvas;
import org.fruit.alayer.Color;
import org.fruit.alayer.FillPattern;
import org.fruit.alayer.Pen;
import org.fruit.alayer.Shape;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.visualizers.EllipseVisualizer;

import eu.testar.iv4xr.enums.IV4XRtags;
import eu.testar.iv4xr.enums.SVec3;

public class Iv4xrVisualization {

	private static Pen GreenPen = Pen.newPen().setColor(Color.Green).setFillPattern(FillPattern.Solid).setStrokeWidth(3).build();
	private static Pen YellowPen = Pen.newPen().setColor(Color.Yellow).setFillPattern(FillPattern.Solid).setStrokeWidth(3).build();
	private static Pen BlackPen = Pen.newPen().setColor(Color.Black).setFillPattern(FillPattern.Solid).setStrokeWidth(3).build();
	private static Pen BluePen = Pen.newPen().setColor(Color.Blue).setFillPattern(FillPattern.Solid).setStrokeWidth(3).build();
	private static Pen WhitePen = Pen.newPen().setColor(Color.White).setFillPattern(FillPattern.Solid).setStrokeWidth(3).build();
	private static Pen vp = Pen.PEN_IGNORE;

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

			if(w.get(IV4XRtags.entityType, "").toLowerCase().contains("door")) {
				canvas.text(YellowPen, 10, y, 0, w.get(IV4XRtags.entityId, "noEntityId"));
			} 
			else if(w.get(IV4XRtags.entityType, "").toLowerCase().contains("fire")) {
				canvas.text(BlackPen, 10, y, 0, w.get(IV4XRtags.entityId, "noEntityId"));
			}
			else if(w.get(IV4XRtags.entityType, "").toLowerCase().contains("button") || w.get(IV4XRtags.entityType, "").toLowerCase().contains("switch")) {
				canvas.text(GreenPen, 10, y, 0, w.get(IV4XRtags.entityId, "noEntityId"));
			}
			else {
				canvas.text(BluePen, 10, y, 0, w.get(IV4XRtags.entityId, "noEntityId"));
			}

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

		// The center coordinate of the SUT GUI
		double centerX = state.get(Tags.Shape).x() + (state.get(Tags.Shape).width() / 2);
		double centerY = state.get(Tags.Shape).y() + (state.get(Tags.Shape).height() / 2);

		// Iterate over all the observed widgets/entities to draw the dot visualization in the GUI coordinates
		for(Widget w : state) {
			// Ignore the Agent and State dot representation
			if(w.equals(agentWidget) || w.equals(state)) continue;

			double distanceX = w.get(IV4XRtags.entityPosition).x - agentWidget.get(IV4XRtags.agentPosition).x;
			double distanceZ = w.get(IV4XRtags.entityPosition).z - agentWidget.get(IV4XRtags.agentPosition).z;

			if(w.get(IV4XRtags.entityType, "").toLowerCase().contains("door")) {
				new EllipseVisualizer(new AbsolutePosition(centerX + distanceX * spyIncrement, centerY - distanceZ * spyIncrement), YellowPen, 10, 10).run(state, canvas, vp);
			} 
			else if(w.get(IV4XRtags.entityType, "").toLowerCase().contains("fire")) {
				new EllipseVisualizer(new AbsolutePosition(centerX + distanceX * spyIncrement, centerY - distanceZ * spyIncrement), BlackPen, 10, 10).run(state, canvas, vp);
			}
			else if(w.get(IV4XRtags.entityType, "").toLowerCase().contains("button") || w.get(IV4XRtags.entityType, "").toLowerCase().contains("switch")) {
				new EllipseVisualizer(new AbsolutePosition(centerX + distanceX * spyIncrement, centerY - distanceZ * spyIncrement), GreenPen, 10, 10).run(state, canvas, vp);
			}
			else {
				new EllipseVisualizer(new AbsolutePosition(centerX + distanceX * spyIncrement, centerY - distanceZ * spyIncrement), BluePen, 10, 10).run(state, canvas, vp);
			}
		}

		// Iterate over all NavMesh node coordinates to draw the dot visualization in the GUI coordinates
		if(state.get(IV4XRtags.labRecruitsNavMesh, null) != null && !state.get(IV4XRtags.labRecruitsNavMesh).isEmpty()) {
			for(SVec3 nodeNavMesh : state.get(IV4XRtags.labRecruitsNavMesh)) {
				double distanceX = nodeNavMesh.x - agentWidget.get(IV4XRtags.agentPosition).x;
				double distanceZ = nodeNavMesh.z - agentWidget.get(IV4XRtags.agentPosition).z;
				new EllipseVisualizer(new AbsolutePosition(centerX + distanceX * spyIncrement, centerY - distanceZ * spyIncrement), WhitePen, 4, 4).run(state, canvas, vp);
			}
		}

	}
}

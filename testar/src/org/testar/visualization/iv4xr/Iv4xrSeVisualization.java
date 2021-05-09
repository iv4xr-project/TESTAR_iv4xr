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

import java.util.HashMap;
import java.util.Map;
import org.fruit.alayer.Canvas;
import org.fruit.alayer.Color;
import org.fruit.alayer.FillPattern;
import org.fruit.alayer.Pen;
import org.fruit.alayer.Shape;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;

import eu.testar.iv4xr.enums.IV4XRtags;

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
		entitiesColors.put("largegrid", YellowPen);
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
		canvas.text(Pen.PEN_RED, 10, 350, 0, "OBSERVED widgets/blocks");

		int countGrids = 0;
		int countBlock = 0;
		for(Widget w : state) {
			if(w.get(IV4XRtags.entityId, "").contains("Grid")) {countGrids ++;}
			if(w.get(IV4XRtags.entityType, "").contains("Block")) {countBlock ++;}
		}

		canvas.text(YellowPen, 10, 390, 0, "Number of Grids: " + countGrids);
		canvas.text(GreenPen, 10, 370, 0, "Number of Blocks: " + countBlock);
	}
}

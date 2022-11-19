/***************************************************************************************************
 *
 * Copyright (c) 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2022 Open Universiteit - www.ou.nl
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

package org.testar.iv4xr;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.fruit.Assert;
import org.fruit.Pair;
import org.fruit.alayer.Action;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.Widget;
import org.fruit.alayer.exceptions.SystemStartException;
import org.testar.OutputStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.enums.IV4XRtags;

public class SpatialXMLmap {

	private SpatialXMLmap() {}

	private static int [][] xml_space_blocks = {{0,0,0}};

	public static int agentObservationRadius = 0; //TODO: Verify 1 to 2,5 game distance relation
	private static Pair<String, String> agentPlatformOrientation = new Pair<String, String>("", "");
	private static Vec3 initialAgentPosition = new Vec3(0, 0, 0);
	private static Vec3 initialPlatformPosition = new Vec3(0, 0, 0);
	private static int minX = 0, maxX = 0, minZ = 0, maxZ = 0;
	private static int WIDTH = 0;
	private static int HEIGHT = 0;
	private static int reSizeMap = 10; // This is to have a picture with a bigger scale

	private static float largeGameToXML = 2.5f;
	private static float smallGameToXML = 0.5f;

	public static void prepareSpatialXMLmap(String levelPath) {
		// First, clear and load the initial XML blocks information
		xml_space_blocks = new int[][]{{0,0,0}};

		try {
			obtainObservationRadius();
			// First load the space blocks to calculate the max and min axis
			loadAgentOrientation(levelPath);
			loadSpaceBlocks(levelPath);
			loadInitialInteractiveBlocks(levelPath);
			loadInitialAgentPosition(levelPath);
			loadInitialPlatformPosition(levelPath);
		} catch(Exception e) {
			e.printStackTrace();
			throw new SystemStartException("Exception reading the Space Engineers file to prepare the xml coverage");
		}
	}

	private static void obtainObservationRadius() throws IOException {
		Gson gson = new Gson();
		Reader reader = Files.newBufferedReader(Paths.get(System.getProperty("user.home") + File.separator + "AppData/Roaming/SpaceEngineers/ivxr-plugin.config"));
		Map<?, ?> map = gson.fromJson(reader, Map.class);
		agentObservationRadius = (int)Math.round((double)map.get("ObservationRadius"));
		System.out.println("ObservationRadius: " + agentObservationRadius);
		reader.close();
	}

	private static void loadAgentOrientation(String levelPath) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		// SANDBOX_0_0_0_.sbs is the SE file that contains information about the existing blocks of the level
		FileInputStream fileIS = new FileInputStream(new File(levelPath + File.separator + "SANDBOX_0_0_0_.sbs"));
		// Prepare a xPath expression to obtain the character initial orientation up
		String expression = "/MyObjectBuilder_Sector/SectorObjects/MyObjectBuilder_EntityBase[@type='MyObjectBuilder_Character']/PositionAndOrientation/Up";

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(fileIS);
		XPath xPath = XPathFactory.newInstance().newXPath();

		// It should exist only one MyObjectBuilder_Character node with this character orientation up
		NodeList characterCoordinatesList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
		Assert.isTrue(characterCoordinatesList.getLength() == 1);
		Node characterCoordinates = characterCoordinatesList.item(0);

		// Obtain the x, y, z attributes from the orientation up node
		int x = Math.abs((int) Math.round(Double.parseDouble(characterCoordinates.getAttributes().getNamedItem("x").getNodeValue())));
		int y = Math.abs((int) Math.round(Double.parseDouble(characterCoordinates.getAttributes().getNamedItem("y").getNodeValue())));
		int z = Math.abs((int) Math.round(Double.parseDouble(characterCoordinates.getAttributes().getNamedItem("z").getNodeValue())));

		List<Integer> orientationUpList = Arrays.asList(x, y, z);
		if(Collections.frequency(orientationUpList, 1) != 1) {
			throw new SystemStartException("SpatialXMLmap loadAgentOrientation: error calculating the agentPlatformOrientation up, multiple frequencies");
		}

		if(x == 1) agentPlatformOrientation = new Pair<String, String>("y", "z");
		else if(y == 1) agentPlatformOrientation = new Pair<String, String>("x", "z");
		else if(z == 1) agentPlatformOrientation = new Pair<String, String>("x", "y");
		else throw new SystemStartException("SpatialXMLmap loadAgentOrientation: error calculating the agentPlatformOrientation up, no agentPlatformOrientation Up detected");

		System.out.println("agentPlatformOrientation: " + agentPlatformOrientation);

		fileIS.close();
	}

	private static void loadSpaceBlocks(String levelPath) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		Set<Node> coordNodesSet = new HashSet<>();
		// SANDBOX_0_0_0_.sbs is the SE file that contains information about the existing blocks of the level
		FileInputStream fileIS = new FileInputStream(new File(levelPath + File.separator + "SANDBOX_0_0_0_.sbs"));
		// MyObjectBuilder_CubeBlock seems to be the XML element that represents each block
		// Prepare a xPath expression to obtain all the block coordinates
		String expression = "/MyObjectBuilder_Sector/SectorObjects/MyObjectBuilder_EntityBase/CubeBlocks/MyObjectBuilder_CubeBlock/Min";

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(fileIS);
		XPath xPath = XPathFactory.newInstance().newXPath();

		NodeList allBlocksCoordinates = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

		for (int i = 0; i < allBlocksCoordinates.getLength(); i++) {
			Node coordElement = allBlocksCoordinates.item(i);
			coordNodesSet.add(coordElement);
			// Obtain the x, y, z attributes from the coordinates node
			int x = Integer.parseInt(coordElement.getAttributes().getNamedItem(agentPlatformOrientation.left()).getNodeValue());
			int z = Integer.parseInt(coordElement.getAttributes().getNamedItem(agentPlatformOrientation.right()).getNodeValue());

			if(x < minX) minX = x;
			if(x > maxX) maxX = x;
			if(z < minZ) minZ = z;
			if(z > maxZ) maxZ = z;
		}

		WIDTH = (maxX - minX + 1);
		HEIGHT = (maxZ - minZ + 1);

		xml_space_blocks = new int[ WIDTH ][ HEIGHT ];

		// Initial always exists
		xml_space_blocks[0 - minX][0 - minZ] = 1;

		for(Node coordElement : coordNodesSet) {
			// Obtain the x, y, z attributes from the coordinates node
			int x = (int) Math.round(Double.parseDouble(coordElement.getAttributes().getNamedItem(agentPlatformOrientation.left()).getNodeValue()));
			int z = (int) Math.round(Double.parseDouble(coordElement.getAttributes().getNamedItem(agentPlatformOrientation.right()).getNodeValue()));

			xml_space_blocks[x - minX][z - minZ] = 1;
		}

		fileIS.close();
	}

	private static void loadInitialInteractiveBlocks(String levelPath) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		// SANDBOX_0_0_0_.sbs is the SE file that contains information about the existing blocks of the level
		FileInputStream fileIS = new FileInputStream(new File(levelPath + File.separator + "SANDBOX_0_0_0_.sbs"));
		// MyObjectBuilder_CubeBlock seems to be the XML element that represents each block
		// Prepare a xPath expression to obtain all the functional blocks that contains the entity id property
		String expression = "/MyObjectBuilder_Sector/SectorObjects/MyObjectBuilder_EntityBase/CubeBlocks/MyObjectBuilder_CubeBlock/EntityId";

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(fileIS);
		XPath xPath = XPathFactory.newInstance().newXPath();

		NodeList allBlocksElements = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
		for (int i = 0; i < allBlocksElements.getLength(); i++) {
			Node blockElement = allBlocksElements.item(i);
			// Now extract the position for the XML space map
			// TODO: Improve with a new xPath expression
			NodeList allBlockProperties = blockElement.getParentNode().getChildNodes();
			for (int j = 0; j < allBlockProperties.getLength(); j++) {
				// Search for the Min property that represents the block coordinates
				if(allBlockProperties.item(j).getNodeName().equals("Min")) {
					// Obtain the x, y, z attributes from the Min property
					int x = Integer.parseInt(allBlockProperties.item(j).getAttributes().getNamedItem(agentPlatformOrientation.left()).getNodeValue());
					int z = Integer.parseInt(allBlockProperties.item(j).getAttributes().getNamedItem(agentPlatformOrientation.right()).getNodeValue());

					xml_space_blocks[x - minX][z - minZ] = 3;
				}
			}
		}

		fileIS.close();
	}

	private static void loadInitialAgentPosition(String levelPath) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		// SANDBOX_0_0_0_.sbs is the SE file that contains information about the existing blocks of the level
		FileInputStream fileIS = new FileInputStream(new File(levelPath + File.separator + "SANDBOX_0_0_0_.sbs"));
		// Prepare a xPath expression to obtain the character initial position
		String expression = "/MyObjectBuilder_Sector/SectorObjects/MyObjectBuilder_EntityBase[@type='MyObjectBuilder_Character']/PositionAndOrientation/Position";

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(fileIS);
		XPath xPath = XPathFactory.newInstance().newXPath();

		// It should exist only one MyObjectBuilder_Character node with this character position
		NodeList characterCoordinatesList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
		Assert.isTrue(characterCoordinatesList.getLength() == 1);
		Node characterCoordinates = characterCoordinatesList.item(0);

		// Obtain the x, y, z attributes from the coordinates node
		int x = (int) Math.round(Double.parseDouble(characterCoordinates.getAttributes().getNamedItem("x").getNodeValue()));
		int y = (int) Math.round(Double.parseDouble(characterCoordinates.getAttributes().getNamedItem("y").getNodeValue()));
		int z = (int) Math.round(Double.parseDouble(characterCoordinates.getAttributes().getNamedItem("z").getNodeValue()));

		initialAgentPosition = new Vec3(x, y, z);
		System.out.println("initialAgentPosition: " + initialAgentPosition);

		fileIS.close();
	}

	private static void loadInitialPlatformPosition(String levelPath) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		// SANDBOX_0_0_0_.sbs is the SE file that contains information about the existing blocks of the level
		FileInputStream fileIS = new FileInputStream(new File(levelPath + File.separator + "SANDBOX_0_0_0_.sbs"));
		// Prepare a xPath expression to obtain the platform initial position
		String expression = "/MyObjectBuilder_Sector/SectorObjects/MyObjectBuilder_EntityBase[@type='MyObjectBuilder_CubeGrid']/PositionAndOrientation/Position";

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(fileIS);
		XPath xPath = XPathFactory.newInstance().newXPath();

		// For now, we need to work with a level on which the main platform should be the first entity base object
		NodeList platformCoordinatesList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
		if(platformCoordinatesList.getLength() != 1) {
			System.out.println("WARNING! More that one MyObjectBuilder_EntityBase Grid detected");
			System.out.println("WARNING! Only the first one will be used to calculate the spatial XML map");
		}
		Node platformCoordinates = platformCoordinatesList.item(0);

		// Obtain the x, y, z attributes from the coordinates node
		int x = (int) Math.round(Double.parseDouble(platformCoordinates.getAttributes().getNamedItem("x").getNodeValue()));
		int y = (int) Math.round(Double.parseDouble(platformCoordinates.getAttributes().getNamedItem("y").getNodeValue()));
		int z = (int) Math.round(Double.parseDouble(platformCoordinates.getAttributes().getNamedItem("z").getNodeValue()));

		initialPlatformPosition = new Vec3(x, y, z);
		System.out.println("initialPlatformPosition: " + initialPlatformPosition);

		fileIS.close();
	}

	public static void updateAgentObservation(State state) {
		// Prepare the relative position to match the relative XML information
		Vec3 relativePosition = Vec3.sub(initialAgentPosition, initialPlatformPosition);

		updateAgentPosition(state, relativePosition);
		updateObservedEntities(state, relativePosition);
	}

	private static float getPositionCoordinate(Vec3 position, String coordinate) {
		if(coordinate.equals("x")) {return position.x;}
		else if(coordinate.equals("y")) {return position.y;}
		else if(coordinate.equals("z")) {return position.z;}
		else throw new SystemStartException("SpatialXMLmap getAgentPositionCoordinate: error obtaining the desired position coordinate");
	}

	private static void updateAgentPosition(State state, Vec3 relativePosition) {
		Vec3 agentPosition = state.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition);
		agentPosition = Vec3.add(Vec3.sub(agentPosition, initialAgentPosition), relativePosition);

		int x = Math.round(getPositionCoordinate(agentPosition, agentPlatformOrientation.left()) / largeGameToXML);
		int z = Math.round(getPositionCoordinate(agentPosition, agentPlatformOrientation.right()) / largeGameToXML);

		// 2 represents the space explored by agent
		// Because this position is updated every state, 
		// do not overwrite the position of functional blocks (3,4,5)
		if(xml_space_blocks[x - minX][z - minZ] < 2) {
			xml_space_blocks[x - minX][z - minZ] = 2;
		}
	}

	private static void updateObservedEntities(State state, Vec3 relativePosition) {
		for(Widget w : state) {
			if(w.get(IV4XRtags.seFunctional, false)) {
				Vec3 widgetBlockPosition = w.get(IV4XRtags.entityPosition);
				widgetBlockPosition = Vec3.add(Vec3.sub(widgetBlockPosition, initialAgentPosition), relativePosition);

				int x = Math.round(getPositionCoordinate(widgetBlockPosition, agentPlatformOrientation.left()) / largeGameToXML);
				int z = Math.round(getPositionCoordinate(widgetBlockPosition, agentPlatformOrientation.right()) / largeGameToXML);

				// 4 represents an observed functional entity
				// Because this position is updated every state, 
				// do not overwrite the position of interacted functional blocks (5)
				if(xml_space_blocks[x - minX][z - minZ] < 4) {
					xml_space_blocks[x - minX][z - minZ] = 4;
				}
			}
		}
	}

	public static void updateInteractedBlock(Action action) {
		// Prepare the relative position to match the relative XML information
		Vec3 relativePosition = Vec3.sub(initialAgentPosition, initialPlatformPosition);
		// If the action contains a functional origin widget
		if(action.get(Tags.OriginWidget, null) != null && action.get(Tags.OriginWidget).get(IV4XRtags.seFunctional, false)) {
			Widget interactedWidget = action.get(Tags.OriginWidget);
			Vec3 widgetBlockPosition = interactedWidget.get(IV4XRtags.entityPosition);
			widgetBlockPosition = Vec3.add(Vec3.sub(widgetBlockPosition, initialAgentPosition), relativePosition);

			int x = Math.round(getPositionCoordinate(widgetBlockPosition, agentPlatformOrientation.left()) / largeGameToXML);
			int z = Math.round(getPositionCoordinate(widgetBlockPosition, agentPlatformOrientation.right()) / largeGameToXML);

			// 5 represents an interacted functional entity
			xml_space_blocks[x - minX][z - minZ] = 5;
		}
	}

	private static int observedBlocksPositions = 0;

	public static void createXMLspatialMap() {
		printSpaceBlocks();
		extractSummarySpatial();
	}

	private static void printSpaceBlocks() {
		try {
			System.out.println(Arrays.deepToString(xml_space_blocks));

			Area observedLevelArea = new Area();

			BufferedImage image = new BufferedImage(WIDTH * reSizeMap + 1, HEIGHT * reSizeMap + 1, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.setBackground(java.awt.Color.white);

			List<Rectangle> emptySpaceList = new ArrayList<>();
			List<Rectangle> existingBlockList = new ArrayList<>();
			List<Rectangle> exploredBlockList = new ArrayList<>();
			List<Rectangle> existingFunctional = new ArrayList<>();
			List<Rectangle> observedFunctional = new ArrayList<>();
			List<Rectangle> interactedFunctional = new ArrayList<>();

			// Prepare the list of elements to draw the map
			for(int i = 0; i < WIDTH; i++) {
				for(int j = 0; j < HEIGHT; j++) {
					// 0 represents a space square without floor blocks
					if(xml_space_blocks[i][j] == 0) {
						emptySpaceList.add(new Rectangle(i * reSizeMap, j * reSizeMap, reSizeMap, reSizeMap));
					}
					// 1 represents an existing floor block in the XML level file
					else if(xml_space_blocks[i][j] == 1) {
						existingBlockList.add(new Rectangle(i * reSizeMap, j * reSizeMap, reSizeMap, reSizeMap));
					}
					// 2 represents a point explored by the agent
					else if(xml_space_blocks[i][j] == 2) {
						exploredBlockList.add(new Rectangle(i * reSizeMap, j * reSizeMap, reSizeMap, reSizeMap));
					}
					// 3 represents an existing functional block
					else if(xml_space_blocks[i][j] == 3) {
						existingFunctional.add(new Rectangle(i * reSizeMap, j * reSizeMap, reSizeMap, reSizeMap));
					}
					// 4 represents an observed functional block
					else if(xml_space_blocks[i][j] == 4) {
						observedFunctional.add(new Rectangle(i * reSizeMap, j * reSizeMap, reSizeMap, reSizeMap));
					}
					// 5 represents an interacted functional block
					else if(xml_space_blocks[i][j] == 5) {
						interactedFunctional.add(new Rectangle(i * reSizeMap, j * reSizeMap, reSizeMap, reSizeMap));
					}
					else {
						System.err.println("ERROR trying to process and printing exploration map");
					}
				}
			}

			// Now draw the map in order
			for(Rectangle r : emptySpaceList) {g.setColor(java.awt.Color.red); g.drawRect(r.x, r.y, r.width, r.height);}
			for(Rectangle r : existingBlockList) {g.setColor(java.awt.Color.yellow); g.drawRect(r.x, r.y, r.width, r.height);}
			for(Rectangle r : exploredBlockList) {
				// draw the agent observation
				g.setColor(java.awt.Color.blue);
				Ellipse2D observationEllipse = drawCenteredCircle(g, r.x, r.y, agentObservationRadius * reSizeMap);
				// fill the agent position
				g.setColor(java.awt.Color.green); 
				g.fillOval(r.x, r.y, r.width, r.height);

				// Add the observed blocks to the observed level area
				observedLevelArea.add(new Area(observationEllipse));
			}
			for(Rectangle r : existingFunctional) {g.setColor(java.awt.Color.magenta); g.fillOval(r.x, r.y, r.width, r.height);}
			for(Rectangle r : observedFunctional) {g.setColor(java.awt.Color.pink); g.fillOval(r.x, r.y, r.width, r.height);}
			for(Rectangle r : interactedFunctional) {g.setColor(java.awt.Color.orange); g.fillOval(r.x, r.y, r.width, r.height);}

			ImageIO.write(image, "png", new File(OutputStructure.outerLoopOutputDir + File.separator + "xml_map" + OutputStructure.sequenceInnerLoopCount + ".png"));

			// Count the observed blocks
			for(Rectangle r : existingBlockList) {
				if(observedLevelArea.contains(r)) {
					observedBlocksPositions ++;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Ellipse2D drawCenteredCircle(Graphics2D g, int x, int y, int r) {
		x = x-(r/2);
		y = y-(r/2);
		g.drawOval(x,y,r,r);
		return new Ellipse2D.Double(x, y, r, r);
	}

	private static void extractSummarySpatial() {
		int interactedBlocksCount = linearSearch(xml_space_blocks, 5);
		int observedBlocksCount = linearSearch(xml_space_blocks, 4) + interactedBlocksCount;
		int existingBlocksCount = linearSearch(xml_space_blocks, 3) + observedBlocksCount;

		int walkedPositions = linearSearch(xml_space_blocks, 2);
		int existingPositions = linearSearch(xml_space_blocks, 1) + walkedPositions;

		String totalSummary = "Sequence | " + OutputStructure.sequenceInnerLoopCount +
				" | existingBlocks | " + existingBlocksCount +
				// Observed entities number and percentage
				" | observedBlocks | " + observedBlocksCount +
				" | " + String.format("%.2f", (double)observedBlocksCount * 100.0 / (double)existingBlocksCount).replace(".", ",") +
				// Interacted number and percentage
				" | interactedBlocks | " + interactedBlocksCount +
				" | " + String.format("%.2f", (double)interactedBlocksCount * 100.0 / (double)existingBlocksCount).replace(".", ",") +

				" | existingPositions | " + existingPositions +
				// Walked number and percentage
				" | walkedPositions | " + walkedPositions +
				" | " + String.format("%.2f", (double)walkedPositions * 100.0 / (double)existingPositions).replace(".", ",") +
				// Observed position number and percentage
				" | observedLevelArea | " + observedBlocksPositions +
				" | " + String.format("%.2f", (double)observedBlocksPositions * 100.0 / (double)existingPositions).replace(".", ",") +
				// Unexplored position number and percentage
				" | unexploredLevelArea | " + (existingPositions - observedBlocksPositions) +
				" | " + String.format("%.2f", (double)(existingPositions - observedBlocksPositions) * 100.0 / (double)existingPositions).replace(".", ",");

		try {
			File metricsFile = new File(OutputStructure.outerLoopOutputDir + File.separator + "summary_spatial_coverage.txt").getAbsoluteFile();
			metricsFile.createNewFile();
			FileWriter myWriter = new FileWriter(metricsFile, true);
			myWriter.write(totalSummary + "\r\n");
			myWriter.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private static int linearSearch(int[][] arr, int target) {
		int count = 0;
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				if (arr[i][j] == target) {
					count++;
				}
			}
		}
		return count;
	}
}

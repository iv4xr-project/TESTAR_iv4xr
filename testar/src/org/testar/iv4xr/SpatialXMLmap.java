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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import org.fruit.alayer.Action;
import org.fruit.alayer.State;
import org.fruit.alayer.Tags;
import org.fruit.alayer.exceptions.SystemStartException;
import org.testar.OutputStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;

import eu.iv4xr.framework.spatial.Vec3;
import eu.testar.iv4xr.actions.se.goals.seActionNavigateGrinderBlock;
import eu.testar.iv4xr.actions.se.goals.seActionNavigateInteract;
import eu.testar.iv4xr.enums.IV4XRtags;

public class SpatialXMLmap {

	private SpatialXMLmap() {}

	private static HashMap<String, String> xml_initial_interactive_blocks = new HashMap<>();
	private static Set<String> xml_interacted_blocks = new HashSet<>();

	private static int [][] xml_space_blocks = {{0,0,0}};
	private static int agentObservationRadius = 0; //TODO: Verify 1 to 2,5 game distance relation
	private static Vec3 initialAgentPosition = new Vec3(0, 0, 0);
	private static Vec3 initialPlatformPosition = new Vec3(0, 0, 0);
	private static int minX = 0, maxX = 0, minZ = 0, maxZ = 0;
	private static int WIDTH = 0;
	private static int HEIGHT = 0;
	private static int reSizeMap = 10;

	public static void prepareSpatialXMLmap(String levelPath) {
		// First, clear and load the initial XML blocks information
		xml_initial_interactive_blocks = new HashMap<>();
		xml_interacted_blocks = new HashSet<>();
		xml_space_blocks = new int[][]{{0,0,0}};

		try {
			obtainObservationRadius();
			loadInitialInteractiveBlocks(levelPath);
			loadSpaceBlocks(levelPath);
			loadInitialAgentPosition(levelPath);
			loadInitialPlatformPosition(levelPath);
		} catch(Exception e) {
			e.printStackTrace();
			throw new SystemStartException("Exception reading the Space Engineers file to prepare the xml coverage");
		}

		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("Initial interactive block detected in the XML file: " + levelPath);
		System.out.println("---------------------------------------------------------------------------------");
		xml_initial_interactive_blocks.entrySet().forEach(entry -> {
			System.out.println("BlockEntityId: " + entry.getKey() + " with Type: " + entry.getValue());
		});
		System.out.println("---------------------------------------------------------------------------------");
	}

	private static void obtainObservationRadius() throws IOException {
		Gson gson = new Gson();
		Reader reader = Files.newBufferedReader(Paths.get(System.getProperty("user.home") + File.separator + "AppData/Roaming/SpaceEngineers/ivxr-plugin.config"));
		Map<?, ?> map = gson.fromJson(reader, Map.class);
		agentObservationRadius = (int)Math.round((double)map.get("ObservationRadius"));
		System.out.println("ObservationRadius: " + agentObservationRadius);
		reader.close();
	}

	private static void loadInitialInteractiveBlocks(String levelPath) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		// SANDBOX_0_0_0_.sbs is the SE file that contains information about the existing blocks of the level
		FileInputStream fileIS = new FileInputStream(new File(levelPath + File.separator + "SANDBOX_0_0_0_.sbs"));
		// MyObjectBuilder_CubeBlock seems to be the XML element that represents each block
		// Prepare a xPath expression to obtain all the block that contains the entity id property
		String expression = "/MyObjectBuilder_Sector/SectorObjects/MyObjectBuilder_EntityBase/CubeBlocks/MyObjectBuilder_CubeBlock/EntityId";

		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(fileIS);
		XPath xPath = XPathFactory.newInstance().newXPath();

		NodeList allBlocksElements = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
		for (int i = 0; i < allBlocksElements.getLength(); i++) {
			Node blockElement = allBlocksElements.item(i);
			// Prepare the entity id for the map
			String blockEntityId = blockElement.getTextContent().trim();
			// Also obtain the type of block because is a more descriptive information
			String blockType = blockElement.getParentNode().getAttributes().getNamedItem("xsi:type").getNodeValue();
			xml_initial_interactive_blocks.put(blockEntityId, blockType);
		}

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
			int x = Integer.parseInt(coordElement.getAttributes().getNamedItem("x").getNodeValue());
			//int y = Integer.parseInt(coordElement.getAttributes().getNamedItem("y").getNodeValue());
			int z = Integer.parseInt(coordElement.getAttributes().getNamedItem("z").getNodeValue());

			if(x < minX) minX = x;
			if(x > maxX) maxX = x;
			if(z < minZ) minZ = z;
			if(z > maxZ) maxZ = z;
		}

		WIDTH = (maxX - minX + 1);
		HEIGHT = (maxZ - minZ + 1);
		reSizeMap = 10; // This is to have a picture with a bigger scale

		xml_space_blocks = new int[ WIDTH ][ HEIGHT ];

		// Initial always exists
		xml_space_blocks[0 - minX][0 - minZ] = 1;

		for(Node coordElement : coordNodesSet) {
			// Obtain the x, y, z attributes from the coordinates node
			int x = (int) Math.round(Double.parseDouble(coordElement.getAttributes().getNamedItem("x").getNodeValue()));
			int y = (int) Math.round(Double.parseDouble(coordElement.getAttributes().getNamedItem("y").getNodeValue()));
			int z = (int) Math.round(Double.parseDouble(coordElement.getAttributes().getNamedItem("z").getNodeValue()));

			xml_space_blocks[x - minX][z - minZ] = 1;
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

		// For now, we need to work with a level on which it should exist only one platform node
		NodeList platformCoordinatesList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
		Assert.isTrue(platformCoordinatesList.getLength() == 1);
		Node platformCoordinates = platformCoordinatesList.item(0);

		// Obtain the x, y, z attributes from the coordinates node
		int x = (int) Math.round(Double.parseDouble(platformCoordinates.getAttributes().getNamedItem("x").getNodeValue()));
		int y = (int) Math.round(Double.parseDouble(platformCoordinates.getAttributes().getNamedItem("y").getNodeValue()));
		int z = (int) Math.round(Double.parseDouble(platformCoordinates.getAttributes().getNamedItem("z").getNodeValue()));

		initialPlatformPosition = new Vec3(x, y, z);
		System.out.println("initialPlatformPosition: " + initialPlatformPosition);

		fileIS.close();
	}

	public static void updateAgentPosition(State state) {
		Vec3 agentPosition = state.get(IV4XRtags.agentWidget).get(IV4XRtags.agentPosition);
		System.out.println("agentPosition: " + agentPosition);

		// Prepare the relative position to match the relative XML information
		Vec3 relativePosition = Vec3.sub(initialAgentPosition, initialPlatformPosition);
		agentPosition = Vec3.add(Vec3.sub(agentPosition, initialAgentPosition), relativePosition);
		System.out.println("relativePosition: " + agentPosition);

		float gameToXML = 2.5f;
		int x = Math.round(agentPosition.x / gameToXML);
		//int y = Math.round(agentPosition.y / gameToXML);
		int z = Math.round(agentPosition.z / gameToXML);

		// 2 represents the space explored by agent
		xml_space_blocks[x - minX][z - minZ] = 2;
	}

	public static void updateInteractedBlock(Action action) {
		if(action instanceof seActionNavigateGrinderBlock || action instanceof seActionNavigateInteract) {
			String interactedBlockId = action.get(Tags.OriginWidget).get(IV4XRtags.entityId);
			System.out.println("Interacted Block Id: " + interactedBlockId);
			xml_interacted_blocks.add(interactedBlockId);
		}
	}

	public static void createXMLspatialMap() {

		printSpaceBlocks();

		System.out.println("---------------------------------------------------------------------------------");
		System.out.println("Interacted block entities by Id");
		System.out.println("---------------------------------------------------------------------------------");

		xml_interacted_blocks.forEach(System.out::println);

		System.out.println("*********************************************************************************");
		System.out.println("Initial BUT NOT interacted entities");
		System.out.println("*********************************************************************************");
		xml_initial_interactive_blocks.entrySet().forEach(entry -> {
			if(!xml_interacted_blocks.contains(entry.getKey())) {
				System.out.println("BlockEntityId: " + entry.getKey() + " with Type: " + entry.getValue());
			}
		});
	}

	private static void printSpaceBlocks() {
		try {
			System.out.println(Arrays.deepToString(xml_space_blocks));

			BufferedImage image = new BufferedImage(WIDTH * reSizeMap + 1, HEIGHT * reSizeMap + 1, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			g.setBackground(java.awt.Color.white);

			List<Rectangle> emptySpaceList = new ArrayList<>();
			List<Rectangle> existingBlockList = new ArrayList<>();
			List<Rectangle> exploredBlockList = new ArrayList<>();

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
					else {
						System.err.println("ERROR trying to process and printing exploration map");
					}
					//ImageIO.write(image, "png", new File("map" + i + j + ".png"));
				}
			}

			// Now draw the map in order
			for(Rectangle r : emptySpaceList) {g.setColor(java.awt.Color.red); g.drawRect(r.x, r.y, r.width, r.height);}
			for(Rectangle r : existingBlockList) {g.setColor(java.awt.Color.yellow); g.drawRect(r.x, r.y, r.width, r.height);}
			for(Rectangle r : exploredBlockList) {
				// draw the agent observation
				g.setColor(java.awt.Color.blue); 
				drawCenteredCircle(g, r.x, r.y, agentObservationRadius * reSizeMap);
				// fill the agent position
				g.setColor(java.awt.Color.green); 
				g.fillOval(r.x, r.y, r.width, r.height);
			}

			ImageIO.write(image, "png", new File(OutputStructure.outerLoopOutputDir + File.separator + "xml_map" + OutputStructure.sequenceInnerLoopCount + ".png"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void drawCenteredCircle(Graphics2D g, int x, int y, int r) {
		x = x-(r/2);
		y = y-(r/2);
		g.drawOval(x,y,r,r);
	}
}

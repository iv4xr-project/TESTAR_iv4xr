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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.fruit.Assert;
import org.fruit.Util;
import org.fruit.alayer.exceptions.SystemStartException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Main;
import org.fruit.monkey.Settings;
import org.testar.OutputStructure;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class OpenCoverage {

	private OpenCoverage() {}

	public static void prepareLaunchOpenCoverWithSUT(Settings settings) {

		verifySteamAppidFile(settings.get(ConfigTags.OpenCoverTarget));

		verifyPdbFiles(settings.get(ConfigTags.PdbFilesPath));

		try {
			downloadOpenCoverageTools(settings);
			// Add OpenCover and ReportGenerator folder temporally to the environment path
			//String customPathEnv = settings.get(ConfigTags.OpenCoverPath).toAbsolutePath() + ";" + settings.get(ConfigTags.ReportGeneratorPath).toAbsolutePath() + ";";
			//Util.getModifiableEnvironment().put("Path", System.getenv("Path") + customPathEnv);
			// Launch the SUT with OpenCover tool
			prepareLaunchOpenCoverSUTconnector(settings);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Verify that the target folder that contains the SpaceEngineers executable contains the steam_appid.txt file. 
	 * If this file does not exist, the SE executable invokes Steam instead of launching the game process. 
	 * This Steam invocation does not allow OpenCover to be attached to the process properly. 
	 * 
	 * @param seTargetFile
	 */
	private static void verifySteamAppidFile(Path seTargetFile) {
		try {
			File seFolder = new File(seTargetFile.toString()).getAbsoluteFile().getParentFile();
			File seSteamAppFile = new File(seFolder + File.separator + "steam_appid.txt");
			if(seSteamAppFile.exists() && Files.readString(seSteamAppFile.toPath()).equals("244850")) {
				System.out.println("SE steam_appid file exists: " + seSteamAppFile.toString());
			} else {
				System.err.println("You need to create the steam_appid.txt with 244850 string content to allow running OpenCover tool with SpaceEngineers");
				System.exit(0);
			}
		} catch (IOException ioe) {
			System.err.println("You need to create the steam_appid.txt with 244850 string content to allow running OpenCover tool with SpaceEngineers");
			ioe.printStackTrace();
			System.exit(0);
		}
	}

	/**
	 * Verify that the PDB files of the .NET SUT exist and are correctly indicated in the PdbFilesPath setting. 
	 * 
	 * @param pdbFilesPath
	 */
	private static void verifyPdbFiles(Path pdbFilesPath) {
		String[] pdbFiles = getPdbFiles(pdbFilesPath);
		System.out.println("PDB files: " + Arrays.toString(pdbFiles));
		if(pdbFiles.length == 0) {
			System.err.println("In order to obtain OpenCover coverage the tool needs access to the PDB files");
			System.err.println("To do that the user needs to customize in TESTAR the PdbFilesPath setting");
			System.exit(0);
		}
	}

	private static String[] getPdbFiles(Path pdbPath) {
		return new File(pdbPath.toAbsolutePath().toString()).list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".pdb");
			}
		});
	}

	/**
	 * Check and download OpenCover and ReportGenerator tools. 
	 * @throws IOException 
	 * @throws MalformedURLException 
	 */
	private static void downloadOpenCoverageTools(Settings settings) throws MalformedURLException, IOException {
		// If the folder of the OpenCover and ReportGenerator tools does not exist, 
		// or if these are empty. 
		// Download and prepare the tools. 
		if(!Files.exists(settings.get(ConfigTags.OpenCoverPath)) || Util.folderIsEmpty(settings.get(ConfigTags.OpenCoverPath), true)) {
			System.out.println("Downloading OpenCover tool...");
			String openCoverURL = "https://github.com/OpenCover/opencover/releases/download/4.7.1221/opencover.4.7.1221.zip";
			String openCoverTempZip = Main.tempDir + "OpenCoverTool.zip";
			// Download the OpenCover tool
			FileUtils.copyURLToFile(new URL(openCoverURL), new File(openCoverTempZip), 2000, 2000);
			// Verify MD5 checksum
			String openCoverMD5 = Util.fileMD5(openCoverTempZip);
			String openCoverExpected = "07726b884edc145cd46debeaca67b498";
			// If MD5 is correct, extract open cover tool zip content in the expected folder
			if(openCoverMD5.equalsIgnoreCase(openCoverExpected)) {
				Util.unzipFile(openCoverTempZip, settings.get(ConfigTags.OpenCoverPath).toString());
				Files.delete(Paths.get(openCoverTempZip));
				System.out.println("Download completed! " + settings.get(ConfigTags.OpenCoverPath).toString());
			} 
			// If MD5 is not the correct one, delete zip file and throw an error
			else {
				Files.delete(Paths.get(openCoverTempZip));
				throw new SystemStartException(String.format("MD5 value of the OpenCover tool is not correct. URL %s , MD5 %s , expected %s", openCoverURL, openCoverMD5, openCoverExpected));
			}
		}
		if(!Files.exists(settings.get(ConfigTags.ReportGeneratorPath)) || Util.folderIsEmpty(settings.get(ConfigTags.ReportGeneratorPath), true)) {
			System.out.println("Downloading ReportGenerator tool...");
			String reportGeneratorURL = "https://github.com/danielpalme/ReportGenerator/releases/download/v5.1.9/ReportGenerator_5.1.9.zip";
			String reportGeneratorTempZip = Main.tempDir + "ReportGeneratorTool.zip";
			// Download the ReportGenerator tool
			FileUtils.copyURLToFile(new URL(reportGeneratorURL), new File(reportGeneratorTempZip), 2000, 2000);
			// Verify MD5 checksum
			String reportGeneratorMD5 = Util.fileMD5(reportGeneratorTempZip);
			String reportGeneratorExpected = "37e60f620045eaf38ecf1e7b4b7b9653";
			// If MD5 is correct, extract report generator tool zip content in the expected folder
			if(reportGeneratorMD5.equalsIgnoreCase(reportGeneratorExpected)) {
				Util.unzipFile(reportGeneratorTempZip, settings.get(ConfigTags.ReportGeneratorPath).toString());
				Files.delete(Paths.get(reportGeneratorTempZip));
				System.out.println("Download completed! " + settings.get(ConfigTags.ReportGeneratorPath).toString());
			} 
			// If MD5 is not the correct one, delete zip file and throw an error
			else {
				Files.delete(Paths.get(reportGeneratorTempZip));
				throw new SystemStartException(String.format("MD5 value of the ReportGenerator tool is not correct. URL %s , MD5 %s , expected %s", reportGeneratorURL, reportGeneratorMD5, reportGeneratorExpected));
			}
		}
	}

	private static void prepareLaunchOpenCoverSUTconnector(Settings settings) throws IOException {
		String openCoverTool = settings.get(ConfigTags.OpenCoverPath).toAbsolutePath().toString() + File.separator + "OpenCover.Console.exe";
		//TODO: The usage of PROGRA~2 works for the targetargs plugin but not for the SpaceEngineers.exe target
		String target = " -target:\"" + settings.get(ConfigTags.OpenCoverTarget).toString() + "\"";
		String targetargs = " -targetargs:\"" + settings.get(ConfigTags.OpenCoverTargetArgs) + "\"";
		String output = " -output:\"" + OutputStructure.outerLoopOutputDir + File.separator + "se_coverage.xml\"";
		String pdbfiles = " -searchdirs:\"" + settings.get(ConfigTags.PdbFilesPath).toString() + "\"";

		// Execute OpenCover with SpaceEngineers in a new process
		String command = openCoverTool + target + targetargs + output + pdbfiles + " -register:user";
		System.out.println("Running SE OpenCover command: " + command);
		Runtime.getRuntime().exec(command);

		// Wait to launch the SE game
		// TODO: Improve to wait until the process is ready
		Util.pause(60);
	}

	/**
	 * Use taskkill windows method to stop the desired process. 
	 * And wait some seconds because OpenCover needs to extract the data. 
	 * 
	 * @param process
	 */
	public static void finishOpenCoverSUTandWait(String process, int seconds) {
		try {
			Runtime.getRuntime().exec("taskkill /F /IM " + process);
		} catch (IOException ioe) {
			System.err.println("Error finishing: " + process);
			ioe.printStackTrace();
		}
		// We need to pause to give time to OpenCover to extract the coverage results
		Util.pause(seconds);
	}

	public static void extractSummaryCoverage() {
		try {
			FileInputStream fileIS = new FileInputStream(new File(OutputStructure.outerLoopOutputDir + File.separator + "se_coverage.xml").getAbsolutePath());
			// Prepare a xPath expression to obtain the Summary information
			String expression = "/CoverageSession/Summary";

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document xmlDocument = builder.parse(fileIS);
			XPath xPath = XPathFactory.newInstance().newXPath();

			// It should exist only one Summary node
			NodeList summaryList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			Assert.isTrue(summaryList.getLength() == 1);
			Node summaryNode = summaryList.item(0);

			int totalLines = Integer.parseInt(summaryNode.getAttributes().getNamedItem("numSequencePoints").getNodeValue());
			int visitedLines = Integer.parseInt(summaryNode.getAttributes().getNamedItem("visitedSequencePoints").getNodeValue());
			double coverageLines = Double.parseDouble(summaryNode.getAttributes().getNamedItem("sequenceCoverage").getNodeValue());

			int totalBranches = Integer.parseInt(summaryNode.getAttributes().getNamedItem("numBranchPoints").getNodeValue());
			int visitedBranches = Integer.parseInt(summaryNode.getAttributes().getNamedItem("visitedBranchPoints").getNodeValue());
			double coverageBranches = Double.parseDouble(summaryNode.getAttributes().getNamedItem("branchCoverage").getNodeValue());

			int totalClasses = Integer.parseInt(summaryNode.getAttributes().getNamedItem("numClasses").getNodeValue());
			int visitedClasses = Integer.parseInt(summaryNode.getAttributes().getNamedItem("visitedClasses").getNodeValue());

			int totalMethods = Integer.parseInt(summaryNode.getAttributes().getNamedItem("numMethods").getNodeValue());
			int visitedMethods = Integer.parseInt(summaryNode.getAttributes().getNamedItem("visitedMethods").getNodeValue());

			String totalSummary = "Sequence | " + OutputStructure.sequenceInnerLoopCount +
					" | totalLines | " + totalLines +
					" | visitedLines | " + visitedLines +
					" | coverageLines | " + coverageLines +
					" | totalBranches | " + totalBranches +
					" | visitedBranches | " + visitedBranches +
					" | coverageBranches | " + coverageBranches +
					" | totalClasses | " + totalClasses +
					" | visitedClasses | " + visitedClasses +
					" | totalMethods | " + totalMethods +
					" | visitedMethods | " + visitedMethods;

			fileIS.close();

			File metricsFile = new File(OutputStructure.outerLoopOutputDir + File.separator + "summary_code_coverage.txt").getAbsoluteFile();
			metricsFile.createNewFile();
			FileWriter myWriter = new FileWriter(metricsFile, true);
			myWriter.write(totalSummary + "\r\n");
			myWriter.close();
		} catch (IOException | XPathExpressionException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
	}

	public static void createHTMLCoverageReport(Settings settings) {
		// Create a file for the HTML coverage report
		File htmlCoverageReportFile = new File(OutputStructure.outerLoopOutputDir + File.separator + "HTMLcoverage" + File.separator).getAbsoluteFile();
		if (!htmlCoverageReportFile.exists()) htmlCoverageReportFile.mkdirs();

		//FIXME: The usage of the ReportGenerate net version varies depending on the dotnet version of the host system
		String reportGeneratorTool = settings.get(ConfigTags.ReportGeneratorPath).toAbsolutePath().toString() + File.separator + "net47" + File.separator + "ReportGenerator.exe";
		String reports = " -reports:\"" + new File(OutputStructure.outerLoopOutputDir + File.separator + "se_coverage.xml").getAbsolutePath() + "\"";
		String targetdir = " -targetdir:\"" + htmlCoverageReportFile + "\"";
		String sourcedirs = " -sourcedirs:\"" + settings.get(ConfigTags.PdbFilesPath).toString() + "\"";

		// Execute ReportGenerator with SpaceEngineers in a new process
		String command = reportGeneratorTool + reports + targetdir + sourcedirs;
		System.out.println("Running ReportGenerator command: " + command);
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException ioe) {
			System.err.println("Error creating the HTML report using ReportGenerator : " + reports);
			ioe.printStackTrace();
		}
	}
}

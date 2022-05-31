/***************************************************************************************************
 *
 * Copyright (c) 2021 - 2022 Universitat Politecnica de Valencia - www.upv.es
 * Copyright (c) 2021 - 2022 Open Universiteit - www.ou.nl
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

import static org.fruit.Util.compileProtocol;
import static org.fruit.monkey.ConfigTags.MyClassPath;
import static org.fruit.monkey.ConfigTags.ProtocolClass;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.fruit.Environment;
import org.fruit.UnProc;
import org.fruit.UnknownEnvironment;
import org.fruit.Util;
import org.fruit.alayer.Tag;
import org.fruit.alayer.windows.Windows10;
import org.fruit.monkey.ConfigException;
import org.fruit.monkey.ConfigTags;
import org.fruit.monkey.Settings;

import es.upv.staq.testar.CodingManager;
import es.upv.staq.testar.NativeLinker;
import es.upv.staq.testar.OperatingSystems;
import es.upv.staq.testar.StateManagementTags;
import es.upv.staq.testar.serialisation.LogSerialiser;

public class TestarAgentLoader {

	/**
	 * Load external settings for all the configurable settings and add/overwrite with those from the file. 
	 * Also initialized the coding manager tags to create the State Model 
	 * and OS environment values to obtain correct screenshots. 
	 * 
	 * @param args
	 * @param testSettingsFile
	 * @return settings
	 * @throws ConfigException
	 */
	public static Settings loadSettings(String[] args, String testSettingsFile) throws ConfigException {
		Settings settings = org.fruit.monkey.Main.loadSettings(args, testSettingsFile);

		initCodingManager(settings);

		initOperatingSystem();

		return settings;
	}

	/**
	 * This method initializes the coding manager with custom tags to use for constructing
	 * concrete and abstract state ids, if provided of course.
	 * @param settings
	 */
	private static void initCodingManager(Settings settings) {
		// we look if there are user-provided custom state tags in the settings
		// if so, we provide these to the coding manager

		Set<Tag<?>> stateManagementTags = StateManagementTags.getAllTags();
		// for the concrete state tags we use all the state management tags that are available
		if (!stateManagementTags.isEmpty()) {
			CodingManager.setCustomTagsForConcreteId(stateManagementTags.toArray(new Tag<?>[0]));
		}

		// then the attributes for the abstract state id
		if (!settings.get(ConfigTags.AbstractStateAttributes).isEmpty()) {
			Tag<?>[] abstractTags = settings.get(ConfigTags.AbstractStateAttributes).stream().map(StateManagementTags::getTagFromSettingsString).filter(Objects::nonNull).toArray(Tag<?>[]::new);
			CodingManager.setCustomTagsForAbstractId(abstractTags);
		}
	}

	/**
	 * Set the concrete implementation of IEnvironment based on the Operating system on which the application is running.
	 */
	private static void initOperatingSystem() {
		if (NativeLinker.getPLATFORM_OS().contains(OperatingSystems.WINDOWS_10)) {
			Environment.setInstance(new Windows10());
		} else {
			System.out.printf("WARNING: Current OS %s has no concrete environment implementation, using default environment\n", NativeLinker.getPLATFORM_OS());
			Environment.setInstance(new UnknownEnvironment());
		}
	}

	/**
	 * Start TESTAR protocol with the selected settings. 
	 * 
	 * This method get the specific protocol class of the selected settings to run TESTAR. 
	 * 
	 * @param settings
	 */
	@SuppressWarnings({ "unchecked", "resource" })
	public UnProc<Settings> prepareProtocol(Settings settings, String settingsDir) {

		compileProtocol(settingsDir, settings.get(ConfigTags.ProtocolClass), settings.get(ConfigTags.ProtocolCompileDirectory));			

		URLClassLoader loader = null;

		try {
			List<String> cp = new ArrayList<>(settings.get(MyClassPath));
			cp.add(settings.get(ConfigTags.ProtocolCompileDirectory));
			URL[] classPath = new URL[cp.size()];
			for (int i = 0; i < cp.size(); i++) {

				classPath[i] = new File(cp.get(i)).toURI().toURL();
			}

			loader = new URLClassLoader(classPath);

			String pc = settings.get(ProtocolClass);
			String protocolClass = pc.substring(pc.lastIndexOf('/')+1, pc.length());

			LogSerialiser.log("Trying to load TESTAR protocol in class '" + protocolClass + "' with class path '" + Util.toString(cp) + "'\n", LogSerialiser.LogLevel.Debug);

			LogSerialiser.log("TESTAR protocol loaded!\n", LogSerialiser.LogLevel.Debug);
			LogSerialiser.log("Starting TESTAR protocol ...\n", LogSerialiser.LogLevel.Debug);

			return (UnProc<Settings>) loader.loadClass(protocolClass).getConstructor().newInstance();
		} catch (Throwable t) {
			LogSerialiser.log("An unexpected error occurred: " + t + "\n", LogSerialiser.LogLevel.Critical);
			System.out.println("Main: Exception caught");
			t.printStackTrace();
			t.printStackTrace(LogSerialiser.getLogStream());
		}
		return null;
	}
}

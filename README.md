## TESTAR-iv4xr project

This project contains the first version of the integration of the TESTAR tool within the iv4xr framework.

**Detailed and updated information can be found in the wiki section:**  
https://github.com/iv4xr-project/TESTAR_iv4xr/wiki

#### Recommended Environment

- Windows 10, 11, Server 2016, Server 2019 OS
- Java 11 to Java 14 (With Java > 15 it is not possible to edit and modify the protocol from the GUI)
- OrientDB 3.0.XX to infer the State Model (more details in the wiki section)

#### Distributed binaries

Follow the latest distribution version of TESTAR here:  
https://github.com/iv4xr-project/TESTAR_iv4xr/releases

### Supported iv4XR applications

#### LabRecruits

[LabRecruits](https://github.com/iv4xr-project/labrecruits) is a 3D demo game created to test the integration of intelligent agents by using the [iv4XRDemo framework](https://github.com/iv4xr-project/iv4xrDemo).  

LabRecruits files:  
- ``testar\bin\suts\gym\Windows\bin`` LabRecruits demo game is downloaded automatically when TESTAR compiles.  
- ``testar\bin\suts\levels`` contains LabRecruits demo levels.  

TESTAR protocols allow users to define how the tool connects and interacts with LabRecruits. 
These protocols are a set of directories inside ``testar\bin\settings`` that contain a java protocol and test.setting file, 
on which it is possible to add new directories (with java + test.setting) to create additional protocols.

Additional information regarding the configuration of these protocols can be found in the 
[TESTAR LabRecruits instructions](https://github.com/iv4xr-project/TESTAR_iv4xr/wiki/LabRecruits-instructions) wiki section.

#### SpaceEngineers

`INFO:` [SE plugin main branch](https://github.com/iv4xr-project/iv4xr-se-plugin) contains at least two known bugs that affect the TESTAR exploration (related to the observed SE entities).  
`INFO:` We recommend using the [SE plugin navgraph_entity branch](https://github.com/iv4xr-project/iv4xr-se-plugin/tree/navgraph_entity), which reverts these bugs.  
`INFO:` Space Engineers DLLs related to navgraph_entity branch can be found here [TESTAR_iv4xr release v3.6](https://github.com/iv4xr-project/TESTAR_iv4xr/releases/tag/v3.6)  

[Space Engineers](https://www.spaceengineersgame.com/) is an industrial 3D game developed by Keen Software and GoodAI. The iv4xr framework contains a [SE plugin](https://github.com/iv4xr-project/iv4xr-se-plugin) that allows the observation and execution of actions in the virtual environment.  

Before executing TESTAR the user needs to:
- Install SpaceEngineers game from Steam.
- Follow the [SE plugin](https://github.com/iv4xr-project/iv4xr-se-plugin) instructions to enable the iv4xr game-framework communication.
- Prepare the SE level we want to test with TESTAR in the ``testar\bin\suts\se_levels`` directory. 

Additional information regarding the configuration of these protocols can be found in the 
[TESTAR Space Engineers instructions](https://github.com/iv4xr-project/TESTAR_iv4xr/wiki/Space-Engineers-instructions) wiki section.

### Known Issues

https://github.com/iv4xr-project/TESTAR_iv4xr/issues

## Development

TESTAR_iv4XR software can be built with Gradle.  
It is possible to use Gradle tasks outside an IDE to compile and launch TESTAR or to use an IDE such as Eclipse or IntelliJ to import, develop, compile and execute TESTAR.

[TESTAR_iv4xr project installation video](https://www.youtube.com/watch?v=WxMFVnh5Uso)

### Gradle tasks

`gradlew` is the instruction to use the Gradle wrapper. 

TESTAR downloads Gradle wrapper dependencies into the system and uses it to compile the project. The Gradle version is indicated inside the file `TESTAR_dev\gradle\wrapper\gradle-wrapper.properties`

#### Gradle build
`gradlew build` task: is configured to compile TESTAR project at Java level for error and warning checking.

#### Gradle distZip
`gradlew distZip` task: prepares a distributed zip version of TESTAR inside ``testar\target\distributions\testar.zip``

#### Gradle installDist
`gradlew installDist` task: prepares a compiled version of TESTAR ready for launching inside ``testar\target\install\testar\bin``

These tasks will also execute `downloadAndUnzipLabRecruits` task: to automatically download the LabRecruits game.

### Import Gradle project into Eclipse (similar to other IDEs with Gradle)

1. Create a new empty workspace for Eclipse in a folder that is not the folder that contains the source
code.  
2. Select File -> Import to open the import dialog
3. Select Gradle -> Existing Gradle project to open the import dialog 
4. Select the folder that contains the root of the source code and start the import

#### Running TESTAR within IDE

After importing the project into the desired IDE, it is possible to execute TESTAR from the IDE.
1. Execute the `debuggingDistribution` task from the group **distribution_iv4XR**  
2a. Launch the `testar\org\fruit\monkey\Main.java` class as a Java application  
2b. For IntelliJ (not Eclipse), the user needs to change the launching directory to `TESTAR_iv4xr\testar`  

## Contact

For any suggestions or errors found, please open an issue or send an email to _tanja@testar.org_ and _fernando@testar.org_ .

### TESTAR for desktop, web and mobile applications

TESTARtool development repository: https://github.com/TESTARtool/TESTAR_dev

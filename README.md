## README

This is the github development root folder for TESTAR development. 
The software can be build with both ant and gradle.

### Required tools to build the software

In order to build the windows native code, Nmake and the compile for Microsoft visual studio are required.
These tools can be downloaded using the following [link](https://www.visualstudio.com/thank-you-downloading-visual-studio/?sku=BuildTools&rel=15#).

After installation of these tools  the file compile_w10.bat and clean_w10.bat need to be adapted.
CALL "C:<*PATH*>\2017\BuildTools\Common7\Tools\VsDevCmd.bat" -arch=x64

Path needs to be set to the folder where the tools are installed.

### Gradle build

To build the Gradle project and run it execute the following procedure;

1. Run `gradlew installDist` in the root of the project
2. Change directory to testar/target/install/bin
3. Run testar.bat

It is also possible to generate a zip file containing TESTAR. This zip can be extracted on any other machine
that has a 64-bit Windows operating system and Visual Studio redistributable installed. A proper way of using
TESTAR is to run the tool in a virtual-machine.
To build the zip execute the following command.

1. Run `gradlew distZip` in the root of the project. 
2. Extract the zip on the machine where TESTAR is used.

NOTE: TESTAR requires Visual Redistributable which can be downloaded from the following
 [link](https://go.microsoft.com/fwlink/?LinkId=746572) .Also a JAVA 1.8 JDK is required.

#### Running Gradle in Eclipse
To use Gradle within Eclipse the
[eclipse plugin](https://docs.gradle.org/3.3/userguide/eclipse_plugin.html) is added to the root project. The following
steps need to be performed to start development in Eclipse;
1. Open a command prompt in the root of the project
2. Execute `gradlew eclipse` (this will generate the required Eclipse information)
3. Open Eclipse (make sure the workspace folder is outside the project folder) and choose File -&gt; Import existing project into workspace
4. Browse to the directory where the Testar souces are located and import the project
5. Right-Click on the project -&gt; Select Configure -&gt; Add gradle nature.

After these steps it is possible to execute gradle tasks. A separate view for Gradle is available.

The procedure presented in this chapter requires the installation of the [buildship](https://projects.eclipse.org/projects/tools.buildship) plugin. This plugin comes pre-installed with Eclipse Neon.

## Known issue
- Testar hangs at the end of the execution.

## GraphDb support
This version of Testar supports the output of data to an http://orientdb.com database.
Currently Widget's, Actions, States and there relation are recorded to the graph database. 

### using graphdb on the local filesystem
The easiest way to use the graph database support is to write the database to the local file system
After starting Testar, it's required to configure the database settings in the tab "GraphDB".
- url : plocal:&lt;path&gt; (for instance plocal:output/testdb) 
- user: admin
- password: admin
- Check the checkbox.

### using graphdb with orientdb server
In order to use the graphdb feature it's advised to install a graph database on your machine  The current implementation 
of Testar has a backend for Orientdb. You can download the
community edition from [orientdb](orientdb.com). Follow the installation instructions to install
the database on your machine. 

When orientdb is started the first time. The root password needs to be configured. Make sure you remember thes password.

In order to use the graphdb feature. A database must be created in Orientdb. To do this follow the following procedure;
- Start the database server (ORIENTDB_HOME/bin/server.bat)
- Start orientdb studio in a webbrowser [http://localhost:2480](http://localhost:2480)
- Choose "New DB" and provide the name, root user and password. (The database will also get a default admin/adimin 
user/password).

After starting Testar, it's required to configure the database settings in the tab "GraphDB".
- url : remote:<hostname>/&lt;database name> (for instance remote:/localhost/demo)
- user: admin
- password: admin
- Check the checkbox.

When Testar finishes, the data be inspected in the database. The easiest way to see the complete
graph is to type the following query in the graph tab "Select * From E". This will display the complete
graph.

### Exploring the graph database with Gremlin.

[Gremlin](http://tinkerpop.apache.org/docs/current/reference/#_tinkerpop3) is a graph traversel engine which can be used 
to query a graph database. OrientDB supports Gremlin and provides an implementation of the traversal engine in it's 
community release.

To start Gremlin run the following command; orientdb-gremlin. This command is delivered with the community 
edition of Orientdb.

See the manuals of orientdb to learn about the possibilities to query the model.

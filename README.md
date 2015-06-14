StrongestPath is a Cytoscape plugin that allows users to find the most confident paths in protein-protein interaction (PPI) networks. While all interaction links of a PPI network are not of the same confidence level, it is important to find out from all possible paths, the ones with the highest confidence that connect a pair of proteins. StrongestPath plugin can be used while we have two groups of proteins and are looking for the highest confidence path connecting some proteins of the first group to some proteins of the second group.

## Requirements

#### Running
* Cytoscape version 3.x.

#### Compiling Java code 
* [Balloon tooltip java library](https://balloontip.java.net)
* Cytoscape 3.x jar file.

Both are included in the project files under `lib` folder.


## How to compile

Please run the following commands in the same directory as the `.java` files are located.
```
javac -source 1.6 -cp ".:./lib/*" *.java
jar cfm StrongestPath.jar META-INF/MANIFEST.MF *.class
```



## Quick Start - Demo

All you need to get started is Cytoscape 3.x and the StrongestPath app!

1- Download and install [Cytoscape](http://www.cytoscape.org).

2- Download the app from here: [Strongest Path](http://apps.cytoscape.org/apps/strongestpath)

<!-- 3- More detailed information can be found in the Installation Section. -->

3- Open Cytoscape and install the StrongestPath app by:
Apps -> App Manager -> Install from file
Then browse to the location you downloaded the StrongestPath app.

4- Run the plugin, which is under Apps menu.

* You can find the instruction on how to run the app [here](https://github.com/strpaths/release/wiki/How-to-run-the-app).

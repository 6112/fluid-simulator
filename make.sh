#!/usr/bin/sh

# Recompile the project, and remake the .jar file needed for the Java Applet.

# clear the bin/ directory
rm -r bin/*

# recompile
cd src/
javac *.java -d ../bin/
# javac **/*.java -d ../bin/
cd ../

# add the resources (mostly images) to the bin/ directory
cp -Rv resources/* bin/

# remove the old jar file
rm -v FluidSimulator.jar

# make a jar file
cd bin/
jar cfm ../FluidSimulator.jar ../manifest.txt ./
cd ../

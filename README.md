# Fluid Simulator

A real-time, 2D fluid simulator written in Java. It allows basic user
interaction with the mouse by clicking and dragging to "pick up" some of the
fluid. Currently, this application can be used as a Java Applet.

It was made with Alexandre D'Amboise for my final project in CEGEP. This is why
comments in the code are in French.

## Running

There are two ways to run this project the simplest way is to open the file
`simulator.html` in a web browser, assuming you have the necessary Java plugins
for that browser.

You can also use the applet viewer to try out the applet in a separate window.

```bash
appletviewer FluidSimulator.jar
```

## Building

Assuming you have Apache Ant installed, you can build simply by using the `ant`
command.

```bash
ant clean
ant
```

# Soot Tutorial
[![Build Status](https://travis-ci.com/noidsirius/SootTutorial.svg?branch=master)](https://travis-ci.com/noidsirius/SootTutorial)

This repository contains (will contain) several simple examples of static program analysis in Java using [Soot](https://github.com/Sable/soot).

## Who this tutorial is for?
Anybody who knows Java programming and wants to do some static analysis in practice but does not know anything about Soot and static analysis in theory.

If you have some knowledge about static program analysis I suggest you learn Soot from [here](https://github.com/Sable/soot/wiki/Tutorials).

### [Why another tutorial for Soot?](https://github.com/noidsirius/SootTutorial/blob/master/docs/Other/Motivation.md)

## Setup
[Setup](https://github.com/noidsirius/SootTutorial/blob/master/docs/Setup/)
  
## Chapters
### 1: Get your hands dirty

In this chapter, you will visit a very simple code example to be familiar with Soot essential data structures and **Jimple**, Soot's principle intermediate representation.

* `./gradlew run --args="HelloSoot"`: The Jimple representation of the [printFizzBuzz](https://github.com/noidsirius/SootTutorial/tree/master/demo/HelloSoot/FizzBuzz.java) method alongside the branch statement.
* `./gradlew run --args="HelloSoot draw"`: The visualization of the [printFizzBuzz](https://github.com/noidsirius/SootTutorial/tree/master/demo/HelloSoot/FizzBuzz.java) control-flow graph.



|Title |Tutorial | Soot Code        | Example Input  |
| :---: |:-------------: |:-------------:| :-----:|
|Hello Soot |[Doc](https://github.com/noidsirius/SootTutorial/blob/master/docs/1/)      | [HelloSoot.java](https://github.com/noidsirius/SootTutorial/tree/master/src/main/java/dev/navids/soottutorial/hellosoot/HelloSoot.java) | [FizzBuzz.java](https://github.com/noidsirius/SootTutorial/tree/master/demo/HelloSoot/FizzBuzz.java) |

<img src="https://github.com/noidsirius/SootTutorial/blob/master/docs/1/images/cfg.png" alt="Control Flow Graph" width="400"/>

### 2: Know the basic APIs

In this chapter, you will be familiar with some basic but useful methods in Soot to help read, analyze, and even update java code.

* `./gradlew run --args="BasicAPI"`: Analyze the class [Circle](https://github.com/noidsirius/SootTutorial/tree/master/demo/BasicAPI/Circle.java).
* `./gradlew run --args="BasicAPI draw"`: Analyze the class [Circle](https://github.com/noidsirius/SootTutorial/tree/master/demo/BasicAPI/Circle.java) and draws the call graph.

|Title |Tutorial | Soot Code        | Example Input  |
| :---: |:-------------: |:-------------:| :-----:|
|Basic API |[Doc](https://medium.com/@noidsirius/know-the-basic-tools-in-soot-18f394318a9c)| [BasicAPI.java](https://github.com/noidsirius/SootTutorial/tree/master/src/main/java/dev/navids/soottutorial/basicapi/BasicAPI.java) | [Circle](https://github.com/noidsirius/SootTutorial/tree/master/demo/BasicAPI/Circle.java) |


<img src="https://github.com/noidsirius/SootTutorial/blob/master/docs/2/images/callgraph.png" alt="Call Graph" width="400"/>

### 3: Some *Real* Static Analysis (:construction: WIP)

* `./gradlew run --args="UsageFinder 'void println(java.lang.String)' 'java.io.PrintStream"`: Find usages of the method with the given subsignature in all methods of [UsageExample.java](https://github.com/noidsirius/SootTutorial/tree/master/demo/IntraAnalysis/UsageExample.java).
* `./gradlew run --args="UsageFinder 'void println(java.lang.String)' 'java.io.PrintStream"`: Find usages of the method with the given subsignature of the given class signature in all methods of [UsageExample.java](https://github.com/noidsirius/SootTutorial/tree/master/demo/IntraAnalysis/UsageExample.java).


|Title |Tutorial | Soot Code        | Example Input  |
| :---: |:-------------: |:-------------:| :-----:|
|Find usages of a method| | [UsageFinder.java](https://github.com/noidsirius/SootTutorial/tree/master/src/main/java/dev/navids/soottutorial/intraanalysis/usagefinder/UsageFinder.java) | [UsageExample.java](https://github.com/noidsirius/SootTutorial/tree/master/demo/IntraAnalysis/usagefinder/UsageExample.java) |
|Null Pointer Analysis ||[NullPointerAnalysis](https://github.com/noidsirius/SootTutorial/tree/master/src/main/java/dev/navids/soottutorial/intraanalysis/npanalysis/) | [NullPointerExample.java](https://github.com/noidsirius/SootTutorial/tree/master/demo/IntraAnalysis/NullPointerExample.java) |


### 4: Manipulate the code (:construction: WIP)
### 5: Call Graphs (:construction: WIP)
### 6: Interprocedural analysis (:construction: WIP)
|Title |Tutorial | Soot Code        | Example Input  |
| :---: |:-------------: |:-------------:| :-----:|
| | | | |

### 7: Android (:construction: WIP)

|Title |Tutorial | Soot Code        | Example Input  |
| :---: |:-------------: |:-------------:| :-----:|
|Trace an APK| | [AndroidTracer.java](https://github.com/noidsirius/SootTutorial/tree/master/src/main/java/dev/navids/soottutorial/android/AndroidTracer.java) | |

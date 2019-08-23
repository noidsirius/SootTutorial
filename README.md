# Soot Tutorial
[![Build Status](https://travis-ci.com/noidsirius/SootTutorial.svg?branch=master)](https://travis-ci.com/noidsirius/SootTutorial)

This repository contains (will contain) several simple examples of static program analysis in Java using [Soot](https://github.com/Sable/soot).

## Setup
Since the stable version of Soot does not support Java 9+ (with modules) yet, make sure that the version of your Java is not higher than 8. You can use [jEnv](https://www.jenv.be) to manage different versions of Java in your machine. If you do not have Java 8, install openjdk8 first (for [Linux](https://openjdk.java.net/install/), and for [Mac](https://apple.stackexchange.com/a/334385)).

* `git clone git@github.com:noidsirius/SootTutorial.git`
* `cd SootTutorial`
* (Optional) if you want to use `jEnv` to manage Java versions (e.g. the installed Java version is `1.8.0_202`)
  * `jenv local 1.8.0_202`
* (Optional) to verify everything is set up correctly
  * `./gradlew check`
  
## Run the analyzer
### HelloSoot

* `./gradlew run` will analyze `printFizzBuzz` method of `FizzBuzz` class located in `demo/HelloSoot/FizzBuzz.class`.
  * In order to see a visualization of `printFizzBuzz` control-flog graph execute `./gradlew run --args='draw'`

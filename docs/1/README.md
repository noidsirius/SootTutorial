# Chapter 1
## Introduction

Static program analysis, in its simplest form, is a black box that inputs a program (code) and outputs some of the properties of the program. For example, let's say we are interested in finding all the branch statements in a method and call this analysis *BranchDetectorAnalysis*. To illustrate this example, I am going to use a trivial program: **FizzBuzz**. FizzBuzz prints each number from 1 to 100 , but if the number is divisible to 3/ 5/ 15 it should print `Fizz` / `Buzz` / `FizzBuzz` instead of the number. Here is a Java class that implements FizzBuzz.


```java
 1) public class FizzBuzz {
 2) 
 3)     public void printFizzBuzz(int k){
 4)        if (k%15==0)
 5)            System.out.println("FizzBuzz");
 6)        else if (k%5==0)
 7)            System.out.println("Buzz");
 8)        else if (k%3==0)
 9)            System.out.println("Fizz");
10)        else
11)            System.out.println(k);
12)    }
13)
14)    public void fizzBuzz(int n){
15)        for (int i=1; i<=n; i++)
16)            printFizzBuzz(i);
17)    }
18) }
```

As a result, the input of *BranchDetectorAnalysis* is a Java method (`printFizzBuzz`) and the output will be the statements that branch the execution of the code ( lines 4, 6, and 8). Note that line 10 is not considered a branch statement since its condition is implicitly determined in line 8.

## Setup Soot
As I mentioned earlier, Soot is a complex software that has lots of configurable settings. As a result, I don't go through the details of the setup except for the most important part which is setting Soot classpath. Soot considers all Java classes in this classpath as its input. In this example, the classpath is `demo/HelloSoot` which contains FizzBuzz.class. For more information regarding this part, you can check [this link out](https://github.com/Sable/soot/wiki/Introduction:-Soot-as-a-command-line-tool).

## Method body retrieval
In order to do *BranchDetectorAnalysis* on `printFizzBuzz`, we have to retrieve its body. But we should locate the method first. Soot has some data structures to represent classes, methods, and statements of the input program.

![Control Flow Graph](https://github.com/noidsirius/SootTutorial/blob/master/docs/1/images/sootarch.png)

`Scene` is a singleton class that keeps all classes which are represented by `SootClass`. Each `SootClass` may contain several methods (`SootMethod`) and each method may have a `Body` object that keeps the statements (`Unit`s). So, after setting up the Soot, we can access these objects via Soot API. The code snippet below, get the FizzBuzz's `SootClass`, find `printFizzBuzz` method, and finally retrieve its `JimpleBody` that contains the statements of the method.
```java
SootClass mainClass = Scene.v().getSootClass("FizzBuzz");
SootMethod sm = mainClass.getMethodByName("printFizzBuzz");
JimpleBody body = (JimpleBody) sm.retrieveActiveBody();
```
### But what is Jimple?
Soot provided several Intermediate Representation (IR) of Java programs in order to make the static analysis more convenient. The default IR in Soot is **Jimple** (Java Simple) which is something between Java and Java byte codes. Java language is preferable for humans since they can read it easily and Java byte code is suitable for machines. Jimple is a statement based, typed (every variable has a type) and 3-addressed (every statement has at most 3 variables) intermediate representation. The code below is the representation of the `printFizzBuzz` method in Jimple.

```
(1) r0 := @this: FizzBuzz
(2) i0 := @parameter0: int
(3) $i1 = i0 % 15
(4) if $i1 != 0 goto $i2 = i0 % 5
(5) $r4 = <java.lang.System: java.io.PrintStream out>
(6) virtualinvoke $r4.<java.io.PrintStream: void println(java.lang.String)>("FizzBuzz")
(7) goto [?= return]
(8) $i2 = i0 % 5
(9) if $i2 != 0 goto $i3 = i0 % 3
(10) $r3 = <java.lang.System: java.io.PrintStream out>
(11) virtualinvoke $r3.<java.io.PrintStream: void println(java.lang.String)>("Buzz")
(12) goto [?= return]
(13) $i3 = i0 % 3
(14) if $i3 != 0 goto $r1 = <java.lang.System: java.io.PrintStream out>
(15) $r2 = <java.lang.System: java.io.PrintStream out>
(16) virtualinvoke $r2.<java.io.PrintStream: void println(java.lang.String)>("Fizz")
(17) goto [?= return]
(18) $r1 = <java.lang.System: java.io.PrintStream out>
(19) virtualinvoke $r1.<java.io.PrintStream: void println(int)>(i0)
(20) return
```


There is nothing implicit in Jimple. For example, `this` is represented as `r0` which is a `Local` object (the data structure of variables in Soot). Or the argument of the function is explicitly defined in `i0` and its type is `int`. Each line represents a `Unit` (or `Stmt` since the default IR is Jimple). There are 15 different types of Stmts in Jimple, but in *BranchDetectorAnalysis*, we are interested only in one of them; `JIfStmt`. Here is the code that prints branch statements:

```
for(Unit u : body.getUnits()){
    if (u instanceof JIfStmt)
        System.out.println(u.toString());
}
```

`body.getUnits()` returns the list (or more precisely `Chain`) of units in `printFizzBuzz` body. We simply iterate over these units and print any of them that are subclasses of `JIfStmt` which are lines 4, 9, and 14. 

## Control-Flow Graph
The branch statements control the flow of the execution of statements. All possible paths that may be executed in a method are represented as Control-Flow Graph (CFG). Soot is capable of creating the CFG of methods through an interface called `UnitGraph` . The image below visualizes the CFG of the printFizzBuzz method. 
You can draw this image by running

```
./gradlew run --args='draw'
```

![Control Flow Graph](https://github.com/noidsirius/SootTutorial/blob/master/docs/1/images/cfg-number.png)

Here you can see there are four possible paths from the start of the method to its end and three branch statements are colored in blue. These paths are representing the numbers divisible to 3, 5, 15, or none of them.
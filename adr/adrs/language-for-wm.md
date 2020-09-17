# Language for window manager

* Status: accepted <!-- optional -->
* Deciders: Andreas Tennert <!-- optional -->
* Date: 2019-07-10 <!-- optional -->

## Context and Problem Statement

What language shall be used for the development of the window manager?

## Decision Drivers <!-- optional -->

* I want to learn Kotlin
* I want to learn Python
* I don't want to take care of the need to update an API layer between the window manager code and the 
* Libraries for display management and tooling in that area is usually written in C

## Considered Options

* Kotlin/JVM
* Kotlin/Native
* Python

## Decision Outcome

Chosen option: "Kotlin/Native", because it compiles to fast native code and C-APIs can be used directly.

### Positive Consequences <!-- optional -->

* Can use C-APIs directly (converted automatically by tooling)
* Compiles to fast native application
* No extra API layer

### Negative Consequences <!-- optional -->

* Needs separate compiling on every machine type

## Pros and Cons of the Options <!-- optional -->

### Kotlin/JVM

* Good, because I have good knowledge on Java and some Java libraries
* Good, because same byte code working on every machine with JVM
* Good, because automatic garbage handling
* Good, because it meets Kotlin learning goal
* Bad, because extra API lib (potentially loosing support or updates)
* Bad, because it needs JVM

### Kotlin/Native

* Good, because no framework needed to run (interpreter / virtual machine) -> fast
* Good, because direct use of C-APIs
* Good, because it meets Kotlin learning goal
* Bad, because resources aqcuired via C libraries need to be cleaned up manually
* Bad, because need to be compiled on each machine type separately

### Python

* Good, because runs on any machine with python interpreter
* Good, because automatic garbage handling
* Good, because it meets Python learning goal
* Bad, because might be a little slow because interpreted language
* Bad, because extra API lib (potentially loosing support or updates)

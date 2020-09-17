# Python for tooling around the window manager

* Status: accepted <!-- optional -->
* Deciders: Andreas Tennert <!-- optional -->
* Date: 2020-04-30 <!-- optional -->

## Context and Problem Statement

The window manager needs some additional tool applications. They are separate and can therefor use a different programming language.

## Decision Drivers <!-- optional -->

* C-APIs and garbage handling
* Well known (easy for others to extend)
* I want wo learn Python

## Considered Options

* Python
* Kotlin/Native

## Decision Outcome

Chosen option: "Python", because there are more developers and I want to learn Python as well as Kotlin.

### Positive Consequences <!-- optional -->

* easier understandable to more people, because Python is more commonly used Kotlin (at this moment)

### Negative Consequences <!-- optional -->

* another language with additional tooling

## Pros and Cons of the Options <!-- optional -->

### Python

* Good, because known by many people
* Good, because good library support for data evaluation and especially the status bar is for showing data
* Bad, because another language with additional tooling

### [option 2]

* Good, because same language as already in use
* Bad, because not as well known to many people

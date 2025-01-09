# Project merge to reduce management efforts

* Status: accepted <!-- optional -->
* Deciders: Andreas Tennert <!-- optional -->
* Date: 2024-10-06 <!-- optional -->

## Context and Problem Statement

While the separate subprojects allowed for easier interchangeability, this feature was never used. Instead, it resulted additional efforts in managing dependencies and CI/CD pipelines. Additionally, Python modules were removed from APT in Ubuntu in version 2024.

## Decision Drivers <!-- optional -->

* Python modules are not part of APT anymore
* Management workload for separate packages
    * CI/CD pipelines
    * Dependencies
    * Build Scripts
    * Issue trackers

## Considered Options

1. Leave separate packages but rewrite the Python applications in different language
2. Leave separate packages and adjust build scripts to get Python modules from somewhere else
3. Combine repositories, packages, build scripts and dependencies and CI/CD pipelines, and rewrite Python apps in Kotlin

## Decision Outcome

Chosen option: "Option 3", because it reduces the future management workload, solves the Python issue and allows for shared code between all projects.

### Positive Consequences <!-- optional -->

* Management workload is reduced
* Issues are easier to track
* As a productively used project, it’s better to focus on one language and maintainability
* Project will still be well-structured by profiting from Gradle project with subprojects (mono-repo) and shared libraries
* Kotlin Multiplatform allows for creating Kotlin/Native as well as Kotlin/JVM applications with shared libraries for both targets

### Negative Consequences <!-- optional -->

* Loss of easy replacement of tool applications
* Loss of separate updates for subprojects

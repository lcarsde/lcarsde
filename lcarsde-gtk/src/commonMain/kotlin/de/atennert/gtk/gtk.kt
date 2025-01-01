package de.atennert.gtk

expect fun gtkInit()

expect fun gtkMainIteration(): Int

expect fun gtkEventsPending(): Int

package de.atennert.rx.operators

import de.atennert.rx.Operator
import de.atennert.rx.ValueOperator

fun <X,Y> map(f: (X) -> Y): Operator<X, Y> = ValueOperator { value, next ->
    next(f(value))
}

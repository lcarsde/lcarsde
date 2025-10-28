package de.atennert.rx.operators

import de.atennert.rx.Operator
import de.atennert.rx.ValueOperator

fun <X> tap(f: (X) -> Unit): Operator<X, X> = ValueOperator { value, next ->
    f(value)
    next(value)
}
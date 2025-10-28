package de.atennert.rx.operators

import de.atennert.rx.Operator
import de.atennert.rx.ValueOperator

fun <X> filter(f: (X) -> Boolean): Operator<X, X> = ValueOperator { value, next ->
    if (f(value)) {
        next(value)
    }
}

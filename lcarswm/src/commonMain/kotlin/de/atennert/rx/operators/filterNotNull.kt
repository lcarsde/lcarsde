package de.atennert.rx.operators

import de.atennert.rx.Operator
import de.atennert.rx.ValueOperator

fun <X> filterNotNull(): Operator<X?, X> = ValueOperator { value, next ->
    if (value != null) {
        next(value)
    }
}

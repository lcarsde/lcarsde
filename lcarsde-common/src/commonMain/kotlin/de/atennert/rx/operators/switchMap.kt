package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Operator

fun <X, Y> switchMap(f: (X) -> Observable<Y>): Operator<X, Y> {
    return Operator { source ->
        source
            .apply(map { f(it) })
            .apply(switchAll())
    }
}
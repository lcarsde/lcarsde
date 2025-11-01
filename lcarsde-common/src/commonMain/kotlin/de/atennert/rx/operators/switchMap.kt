package de.atennert.rx.operators

import de.atennert.rx.Observable
import de.atennert.rx.Subscribable

fun <T, U> Subscribable<T>.switchMap(f: (T) -> Observable<U>) = this
    .map { f(it) }
    .switchAll()

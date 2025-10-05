package de.atennert.rx

fun interface Operator<X, Y> {
    fun call(source: Observable<X>): Observable<Y>
}
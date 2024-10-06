package de.atennert.rx.util

/*
 * See https://dzone.com/articles/kotlin-the-tuple-type
 */

data class Tuple1<out A>(val v1: A)
data class Tuple2<out A, out B>(val v1: A, val v2: B)
data class Tuple3<out A, out B, out C>(val v1: A, val v2: B, val v3: C)
data class Tuple4<out A, out B, out C, out D>(val v1: A, val v2: B, val v3: C, val v4: D)

object Tuple {
    operator fun <A> invoke(v1: A) = Tuple1(v1)
    operator fun <A, B> invoke(v1: A, v2: B) = Tuple2(v1, v2)
    operator fun <A, B, C> invoke(v1: A, v2: B, v3: C) = Tuple3(v1, v2, v3)
    operator fun <A, B, C, D> invoke(v1: A, v2: B, v3: C, v4: D) = Tuple4(v1, v2, v3, v4)
}

fun <A> Tuple1<A>.toList() = listOf(v1)
fun <A, B> Tuple2<A, B>.toList() = listOf(v1, v2)
fun <A, B, C> Tuple3<A, B, C>.toList() = listOf(v1, v2, v3)
fun <A, B, C, D> Tuple4<A, B, C, D>.toList() = listOf(v1, v2, v3, v4)
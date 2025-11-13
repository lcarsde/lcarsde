package de.atennert.lcarsde.lifecycle

import kotlin.reflect.KClass

object ServiceLocator {
    private val mappers = mutableMapOf<KClass<*>, Lazy<Any>>()

    fun getOrPut(clazz: KClass<*>, init: () -> Any): Lazy<Any> {
        return mappers.getOrPut(clazz) { lazy(init) }
    }

    operator fun get(clazz: KClass<*>): Lazy<Any> {
        return mappers[clazz] ?: throw NoSuchElementException("No initializer present for $clazz")
    }

    fun <T : Any> provide(clazz: KClass<T>, init: () -> T) {
        mappers[clazz] = lazy(init)
    }

    inline fun <reified T : Any> provide(noinline init: () -> T) {
        provide(T::class, init)
    }
}

inline fun <reified T : Any> inject(noinline init: () -> T): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return ServiceLocator.getOrPut(T::class, init) as Lazy<T>
}

inline fun <reified T : Any> inject(): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return ServiceLocator[T::class] as Lazy<T>
}
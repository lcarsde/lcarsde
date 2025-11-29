package de.atennert.lcarsde.lifecycle

import kotlin.reflect.KClass

object ServiceLocator {
    private val mappers = mutableMapOf<KClass<*>, MutableSet<Lazy<Any>>>()

    operator fun get(clazz: KClass<*>): Lazy<Any> {
        val result = mappers[clazz] ?: throw NoSuchElementException("No initializer present for $clazz")
        if (result.size > 1) throw IllegalStateException("Multiple initializers present for $clazz")
        return result.first()
    }

    fun getAll(clazz: KClass<*>): Set<Lazy<Any>> {
        return mappers[clazz] ?: throw NoSuchElementException("No initializer present for $clazz")
    }

    fun <T : Any> provide(clazz: KClass<T>, init: () -> T) {
        mappers.getOrPut(clazz) { mutableSetOf() }
            .add(lazy(init))
    }

    inline fun <reified T : Any> provide(noinline init: () -> T) {
        provide(T::class, init)
    }

    fun clear() {
        mappers.clear()
    }
}

inline fun <reified T : Any> inject(): Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return ServiceLocator[T::class] as Lazy<T>
}

inline fun <reified T : Any> injectAll(): Lazy<List<T>> = lazy {
    @Suppress("UNCHECKED_CAST")
    ServiceLocator.getAll(T::class).map { it.value } as List<T>
}

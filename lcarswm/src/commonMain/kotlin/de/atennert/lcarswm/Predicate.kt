package de.atennert.lcarswm

/**
 * Definition of predicates for checking elements.
 *
 * @param T type of elements that are checked with the predicate
 * @return true if the element matches the requirements, false otherwise
 */
typealias Predicate<T> = (x: T) -> Boolean

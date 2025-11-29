package de.atennert.lcarsde.lifecycle

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class DITest : DescribeSpec({
    afterEach {
        ServiceLocator.clear()
    }

    describe("Dependency Injection") {
        it("should throw a NoSuchElementException when there is no provided class") {
            val exception = shouldThrow<NoSuchElementException> {
                ServiceLocator[Int::class]
            }
            println(exception.message)
            exception.message shouldBe "No initializer present for class kotlin.Int"
        }

        it("should provide a provided class instance") {
            class Test

            val instance = Test()
            ServiceLocator.provide<Test> { instance }

            val injected by inject<Test>()
            injected.shouldNotBeNull()
            injected shouldBe instance
        }

        it("should throw an Exception when there are many results but one is requested") {
            val exception = shouldThrow<IllegalStateException> {
                ServiceLocator.provide<Int> { 1 }
                ServiceLocator.provide<Int> { 2 }

                ServiceLocator[Int::class]
            }
            println(exception.message)
            exception.message shouldBe "Multiple initializers present for class kotlin.Int"
        }

        it("should provide all provided elements of a type") {
            ServiceLocator.provide<Int> { 1 }
            ServiceLocator.provide<Int> { 2 }

            val result by injectAll<Int>()
            result shouldBe listOf(1, 2)
        }
    }
})
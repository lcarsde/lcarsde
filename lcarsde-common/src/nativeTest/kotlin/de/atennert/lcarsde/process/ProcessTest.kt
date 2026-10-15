package de.atennert.lcarsde.process

import de.atennert.lcarsde.file.read
import de.atennert.lcarsde.lifecycle.ServiceLocator
import de.atennert.lcarsde.log.Logger
import de.atennert.lcarsde.log.PrintLogger
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.getpid
import platform.posix.remove
import platform.posix.usleep

@OptIn(ExperimentalForeignApi::class)
class ProcessTest : DescribeSpec({
    val outputPath = "/tmp/lcarsde-process-test-${getpid()}"

    beforeEach {
        ServiceLocator.provide<Logger> { PrintLogger() }
    }

    afterEach {
        ServiceLocator.clear()
        remove(outputPath)
    }

    describe("Process") {
        it("should run the command with all its arguments") {
            ProcessBuilder()
                .command("sh", "-c", "printf '%s' \"\$1\" > \"\$2\"", "sh", "hello world", outputPath)
                .start()

            var output = read(outputPath)
            var attempts = 0
            while (output.isNullOrEmpty() && attempts++ < 50) {
                usleep(20_000u)
                output = read(outputPath)
            }

            output shouldBe "hello world"
        }
    }
})

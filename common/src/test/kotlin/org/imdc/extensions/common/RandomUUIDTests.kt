package org.imdc.extensions.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.imdc.extensions.common.ExpressionTestHarness.Companion.withFunction
import org.imdc.extensions.common.expressions.RandomUUIDFunction

class RandomUUIDTests : FunSpec() {
    init {
        context("RandomUUID") {
            withFunction("randomUUID", RandomUUIDFunction()) {
                test("Unique results") {
                    evaluate("randomUUID() = randomUUID()") shouldBe false
                }
            }
        }
    }
}

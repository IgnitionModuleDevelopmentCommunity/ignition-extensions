package org.imdc.extensions.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import org.imdc.extensions.common.ExpressionTestHarness.Companion.withFunction
import org.imdc.extensions.common.expressions.UUID4Function
import java.util.*

class UUID4Tests : FunSpec() {
    init {
        context("RandomUUID") {
            withFunction("uuid4", UUID4Function()) {
                test("Instance of UUID") {
                    evaluate("uuid4()") should beInstanceOf<UUID>()
                }
                test("Unique results") {
                    evaluate("uuid4() = uuid4()") shouldBe false
                }
            }
        }
    }
}

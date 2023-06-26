package org.imdc.extensions.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.imdc.extensions.common.ExpressionTestHarness.Companion.withFunction
import org.imdc.extensions.common.expressions.LogicalPredicate.AllOfFunction
import org.imdc.extensions.common.expressions.LogicalPredicate.AnyOfFunction
import org.imdc.extensions.common.expressions.LogicalPredicate.NoneOfFunction

class LogicalFunctionsTests : FunSpec() {
    init {
        context("AllOf") {
            withFunction("allOf", AllOfFunction) {
                test("No arguments") {
                    evaluate("allOf()") shouldBe true
                }
                test("Simple booleans") {
                    evaluate("allOf(true, true)") shouldBe true
                    evaluate("allOf(true, false)") shouldBe false
                    evaluate("allOf(false, false)") shouldBe false
                }
            }
        }
        context("AnyOf") {
            withFunction("anyOf", AnyOfFunction) {
                test("No arguments") {
                    evaluate("anyOf()") shouldBe false
                }
                test("Simple booleans") {
                    evaluate("anyOf(true, true)") shouldBe true
                    evaluate("anyOf(true, false)") shouldBe true
                    evaluate("anyOf(false, false)") shouldBe false
                }
            }
        }
        context("NoneOf") {
            withFunction("noneOf", NoneOfFunction) {
                test("No arguments") {
                    evaluate("noneOf()") shouldBe true
                }
                test("Simple booleans") {
                    evaluate("noneOf(true, true)") shouldBe false
                    evaluate("noneOf(true, false)") shouldBe false
                    evaluate("noneOf(false, false)") shouldBe true
                }
            }
        }
    }
}

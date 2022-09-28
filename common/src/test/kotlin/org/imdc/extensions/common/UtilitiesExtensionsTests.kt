package org.imdc.extensions.common

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.python.core.Py
import org.python.core.PyDictionary
import org.python.core.PyList

@Suppress("PyUnresolvedReferences", "PyInterpreter")
class UtilitiesExtensionsTests : JythonTest(
    { globals ->
        globals["utils"] = UtilitiesExtensions(mockk())
    },
) {
    init {
        context("Deep copy tests") {
            test("No input") {
                shouldThrowPyException(Py.TypeError) {
                    eval<Any?>("utils.deepCopy()")
                }
            }
            test("Null input") {
                eval<Any?>("utils.deepCopy(None)").shouldBeNull()
            }
            context("Simple conversions") {
                test("List") {
                    eval<PyList>("utils.deepCopy([1, 2, 3])") shouldBe listOf(1, 2, 3)
                }
                test("Tuple") {
                    eval<PyList>("utils.deepCopy((1, 2, 3))") shouldBe listOf(1, 2, 3)
                }
                test("Set") {
                    eval<PyList>("utils.deepCopy({1, 2, 3})") shouldBe listOf(1, 2, 3)
                }
                test("Dict") {
                    eval<PyDictionary>("utils.deepCopy({'a': 1, 'b': 2, 'c': 3})\n") shouldBe mapOf(
                        "a" to 1,
                        "b" to 2,
                        "c" to 3,
                    )
                }
            }
        }
    }
}

package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.util.DatasetBuilder
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.python.core.Py

@Suppress("PyUnresolvedReferences", "PyInterpreter")
class DatasetExtensionsTests : JythonTest(
    { globals ->
        globals["utils"] = DatasetExtensions
        globals["dataset"] = DatasetBuilder.newBuilder()
            .colNames("a", "b", "c")
            .colTypes(Int::class.javaObjectType, Double::class.javaObjectType, String::class.java)
            .addRow(1, 3.14, "pi")
            .addRow(2, 6.28, "tau")
            .build()
    },
) {
    init {
        context("Map tests") {
            test("Null dataset") {
                shouldThrowPyException(Py.TypeError) {
                    eval<Dataset>("utils.map(None, None)")
                }
            }

            test("Simple mapper, preserving types") {
                eval<Dataset>("utils.map(dataset, lambda a, b, c: (a * 2, b * 2, c * 2), True)").asClue {
                    it.columnNames shouldBe listOf("a", "b", "c")
                    it.columnTypes shouldBe listOf(
                        Int::class.javaObjectType,
                        Double::class.javaObjectType,
                        String::class.java,
                    )
                    it.rowCount shouldBe 2
                    it.getColumnAsList(0) shouldBe listOf(2, 4)
                    it.getColumnAsList(1) shouldBe listOf(6.28, 12.56)
                    it.getColumnAsList(2) shouldBe listOf("pipi", "tautau")
                }
            }

            test("Simple mapper, discarding types") {
                eval<Dataset>("utils.map(dataset, lambda a, b, c: (a * 2, b * 2, c * 2), False)").asClue {
                    it.columnNames shouldBe listOf("a", "b", "c")
                    it.columnTypes shouldBe listOf(Any::class.java, Any::class.java, Any::class.java)
                    it.rowCount shouldBe 2
                    it.getColumnAsList(0) shouldBe listOf(2, 4)
                    it.getColumnAsList(1) shouldBe listOf(6.28, 12.56)
                    it.getColumnAsList(2) shouldBe listOf("pipi", "tautau")
                }
            }
        }

        context("Filter tests") {
            test("Constant filter") {
                eval<Dataset>("utils.filter(dataset, lambda **kwargs: False)").asClue {
                    it.columnNames shouldBe listOf("a", "b", "c")
                    it.columnTypes shouldBe listOf(
                        Int::class.javaObjectType,
                        Double::class.javaObjectType,
                        String::class.java,
                    )
                    it.rowCount shouldBe 0
                }
            }

            test("Conditional filter all kwargs") {
                eval<Dataset>("utils.filter(dataset, lambda **kwargs: kwargs['row'] >= 1)").asClue {
                    it.columnNames shouldBe listOf("a", "b", "c")
                    it.columnTypes shouldBe listOf(
                        Int::class.javaObjectType,
                        Double::class.javaObjectType,
                        String::class.java,
                    )
                    it.rowCount shouldBe 1
                }
            }

            test("Conditional filter unpacking row") {
                eval<Dataset>("utils.filter(dataset, lambda row, **kwargs: row >= 1)").asClue {
                    it.columnNames shouldBe listOf("a", "b", "c")
                    it.columnTypes shouldBe listOf(
                        Int::class.javaObjectType,
                        Double::class.javaObjectType,
                        String::class.java,
                    )
                    it.rowCount shouldBe 1
                }
            }
        }

        context("Print tests") {
            test("Basic test") {
                with(DatasetExtensions) {
                    buildString {
                        printDataset(globals["dataset"])
                    } shouldBe """
                        | Row |   a |    b |   c |
                        | --- | --- | ---- | --- |
                        |   0 |   1 | 3.14 |  pi |
                        |   1 |   2 | 6.28 | tau |

                    """.trimIndent()
                }
            }
        }
    }
}

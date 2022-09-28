package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.util.DatasetBuilder
import io.kotest.matchers.shouldBe
import org.python.core.Py

class DatasetUtilsTests : JythonTest(
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
                assertThrowsPyException(Py.TypeError) {
                    eval("utils.map(None, None)")
                }
            }

            test("Simple mapper, preserving types") {
                val result: Dataset = eval("utils.map(dataset, lambda a,b,c: (a*2, b*2, c*2), True)")
                result.columnNames shouldBe listOf("a", "b", "c")
                result.columnTypes shouldBe listOf(
                    Int::class.javaObjectType,
                    Double::class.javaObjectType,
                    String::class.java,
                )
                result.rowCount shouldBe 2
                result.getColumnAsList(0) shouldBe listOf(2, 4)
                result.getColumnAsList(1) shouldBe listOf(6.28, 12.56)
                result.getColumnAsList(2) shouldBe listOf("pipi", "tautau")
            }

            test("Simple mapper, discarding types") {
                val result: Dataset = eval("utils.map(dataset, lambda a,b,c: (a*2, b*2, c*2), False)")
                result.columnNames shouldBe listOf("a", "b", "c")
                result.columnTypes shouldBe listOf(Any::class.java, Any::class.java, Any::class.java)
                result.rowCount shouldBe 2
                result.getColumnAsList(0) shouldBe listOf(2, 4)
                result.getColumnAsList(1) shouldBe listOf(6.28, 12.56)
                result.getColumnAsList(2) shouldBe listOf("pipi", "tautau")
            }
        }

        context("Filter tests") {
            test("Constant filter") {
                val result: Dataset = eval("utils.filter(dataset, lambda row, values: False)")
                result.columnNames shouldBe listOf("a", "b", "c")
                result.columnTypes shouldBe listOf(
                    Int::class.javaObjectType,
                    Double::class.javaObjectType,
                    String::class.java,
                )
                result.rowCount shouldBe 0
            }

            test("Conditional filter") {
                val result: Dataset = eval("utils.filter(dataset, lambda row, values: row >= 1)")
                result.columnNames shouldBe listOf("a", "b", "c")
                result.columnTypes shouldBe listOf(
                    Int::class.javaObjectType,
                    Double::class.javaObjectType,
                    String::class.java,
                )
                result.rowCount shouldBe 1
            }
        }

        context("Print tests") {
            test("Basic test") {
                with(DatasetExtensions) {
                    StringBuilder().apply {
                        printDataset(globals["dataset"]!!)
                    }.toString() shouldBe """
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

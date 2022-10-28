package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.BasicDataset
import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.util.DatasetBuilder
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import org.imdc.extensions.common.DatasetExtensions.printDataset
import org.python.core.Py
import kotlin.io.path.createTempFile
import kotlin.io.path.writeBytes

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

        val excelSample =
            DatasetExtensionsTests::class.java.getResourceAsStream("sample.xlsx")!!.readAllBytes()
        val tempXlsx = createTempFile(suffix = "xlsx").also {
            it.writeBytes(excelSample)
            it.toFile().deleteOnExit()
        }
        globals["xlsxBytes"] = excelSample
        globals["xlsxFile"] = tempXlsx.toString()

        val xlsSample =
            DatasetExtensionsTests::class.java.getResourceAsStream("sample.xls")!!.readAllBytes()
        val tempXls = createTempFile(suffix = "xls").also {
            it.writeBytes(xlsSample)
            it.toFile().deleteOnExit()
        }
        globals["xlsBytes"] = xlsSample
        globals["xlsFile"] = tempXls.toString()
    },
) {
    private fun Dataset.asClue(assertions: (Dataset) -> Unit) {
        withClue(
            lazy {
                buildString {
                    printDataset(this, this@asClue, true)
                }
            },
        ) {
            assertions(this)
        }
    }

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
            context("Basic dataset") {
                test("Without types") {
                    buildString {
                        printDataset(this, globals["dataset"])
                    } shouldBe """
                        | Row |   a |    b |   c |
                        | --- | --- | ---- | --- |
                        |   0 |   1 | 3.14 |  pi |
                        |   1 |   2 | 6.28 | tau |

                    """.trimIndent()
                }

                test("With types") {
                    buildString {
                        printDataset(this, globals["dataset"], includeTypes = true)
                    } shouldBe """
                        | Row | a (Integer) | b (Double) | c (String) |
                        | --- | ----------- | ---------- | ---------- |
                        |   0 |           1 |       3.14 |         pi |
                        |   1 |           2 |       6.28 |        tau |

                    """.trimIndent()
                }
            }

            val emptyDataset = BasicDataset(
                listOf("a", "b", "c"),
                listOf(String::class.java, Int::class.java, Boolean::class.java),
            )
            context("Empty dataset") {
                test("Without types") {
                    buildString {
                        printDataset(this, emptyDataset)
                    } shouldBe """
                        | Row |   a |   b |   c |
                        | --- | --- | --- | --- |

                    """.trimIndent()
                }

                test("With types") {
                    buildString {
                        printDataset(this, emptyDataset, includeTypes = true)
                    } shouldBe """
                        | Row | a (String) | b (int) | c (boolean) |
                        | --- | ---------- | ------- | ----------- |

                    """.trimIndent()
                }
            }
        }

        context("fromExcel") {
            test("XLSX file") {
                eval<Dataset>("utils.fromExcel(xlsxFile)").asClue {
                    it.rowCount shouldBe 100
                    it.columnCount shouldBe 16
                }
            }
            test("XLS file") {
                eval<Dataset>("utils.fromExcel(xlsFile)").asClue {
                    it.rowCount shouldBe 100
                    it.columnCount shouldBe 16
                }
            }
            test("XLSX bytes") {
                eval<Dataset>("utils.fromExcel(xlsxBytes)").asClue {
                    it.rowCount shouldBe 100
                    it.columnCount shouldBe 16
                }
            }
            test("XLS bytes") {
                eval<Dataset>("utils.fromExcel(xlsBytes)").asClue {
                    it.rowCount shouldBe 100
                    it.columnCount shouldBe 16
                }
            }
            test("With headers") {
                eval<Dataset>("utils.fromExcel(xlsxBytes, headerRow=0)").asClue {
                    it.rowCount shouldBe 99
                    it.columnCount shouldBe 16
                    it.columnNames shouldBe listOf(
                        "Segment",
                        "Country",
                        "Product",
                        "Discount Band",
                        "Units Sold",
                        "Manufacturing Price",
                        "Sale Price",
                        "Gross Sales",
                        "Discounts",
                        "Sales",
                        "COGS",
                        "Profit",
                        "Date",
                        "Month Number",
                        "Month Name",
                        "Year",
                    )
                }
            }
            test("First row") {
                eval<Dataset>("utils.fromExcel(xlsxBytes, headerRow=0, firstRow=50)").asClue {
                    it.rowCount shouldBe 50
                    it.columnCount shouldBe 16
                }
            }
            test("Last row") {
                eval<Dataset>("utils.fromExcel(xlsxBytes, headerRow=0, lastRow=50)").asClue {
                    it.rowCount shouldBe 50
                    it.columnCount shouldBe 16
                }
            }
            test("First & last row") {
                eval<Dataset>("utils.fromExcel(xlsxBytes, headerRow=0, firstRow=5, lastRow=10)").asClue {
                    it.rowCount shouldBe 6
                    it.columnCount shouldBe 16
                }
            }
            test("First column") {
                eval<Dataset>("utils.fromExcel(xlsxBytes, headerRow=0, firstColumn=10)").asClue {
                    it.rowCount shouldBe 99
                    it.columnCount shouldBe 6
                    it.columnNames shouldBe listOf(
                        "COGS",
                        "Profit",
                        "Date",
                        "Month Number",
                        "Month Name",
                        "Year",
                    )
                }
            }
            test("Last column") {
                eval<Dataset>("utils.fromExcel(xlsxBytes, headerRow=0, lastColumn=5)").asClue {
                    it.rowCount shouldBe 99
                    it.columnCount shouldBe 6
                    it.columnNames shouldBe listOf(
                        "Segment",
                        "Country",
                        "Product",
                        "Discount Band",
                        "Units Sold",
                        "Manufacturing Price",
                    )
                }
            }
            test("First & last column") {
                eval<Dataset>("utils.fromExcel(xlsxBytes, headerRow=0, firstColumn=5, lastColumn=10)").asClue {
                    it.rowCount shouldBe 99
                    it.columnCount shouldBe 6
                    it.columnNames shouldBe listOf(
                        "Manufacturing Price",
                        "Sale Price",
                        "Gross Sales",
                        "Discounts",
                        "Sales",
                        "COGS",
                    )
                }
            }
        }
    }
}

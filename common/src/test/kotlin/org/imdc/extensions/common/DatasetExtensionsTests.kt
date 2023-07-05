package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.BasicDataset
import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.util.DatasetBuilder
import io.kotest.assertions.asClue
import io.kotest.assertions.withClue
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import org.imdc.extensions.common.DatasetExtensions.printDataset
import org.python.core.Py
import java.awt.Color
import java.util.Date

@Suppress("PyUnresolvedReferences", "PyInterpreter")
class DatasetExtensionsTests : JythonTest(
    { globals ->
        globals["utils"] = DatasetExtensions
        globals["builder"] = DatasetBuilder.newBuilder()
        globals["outerJoin1"] = DatasetBuilder.newBuilder()
            .colNames("EmpID", "EmpName", "City", "Designation")
            .colTypes(Int::class.javaObjectType, String::class.java, String::class.java, String::class.java)
            .addRow(1, "Charlotte Robinson", "Chicago", "Consultant")
            .addRow(2, "Madison Phillips", "Dallas", "Senior Analyst")
            .addRow(3, "Emma Hernandez", "Phoenix", "Senior Analyst")
            .addRow(4, "Samantha Sanchez", "San Diego", "Principal Conultant")
            .addRow(5, "Sadie Ward", "San Antonio", "Consultant")
            .addRow(6, "Savannah Perez", "New York", "Principal Conultant")
            .addRow(7, "Victoria Gray", "Los Angeles", "Assistant")
            .addRow(8, "Alyssa Lewis", "Houston", "Consultant")
            .addRow(9, "Anna Lee", "San Jose", "Principal Conultant")
            .addRow(10, "Riley Hall", "Philadelphia", "Senior Analyst")
            .build()
        globals["outerJoin2"] = DatasetBuilder.newBuilder()
            .colNames("EmpID", "Department_ID", "DepartmentName")
            .colTypes(Int::class.javaObjectType, Int::class.javaObjectType, String::class.java)
            .addRow(1, 0, "Executive")
            .addRow(2, 1, "Document Control")
            .addRow(3, 2, "Finance")
            .addRow(4, 3, "Engineering")
            .addRow(5, 4, "Facilities and Maintenance")
            .addRow(6, 2, "Finance")
            .addRow(10, 4, "Facilities and Maintenance")
            .build()

        globals["dataset"] = DatasetBuilder.newBuilder()
            .colNames("a", "b", "c")
            .colTypes(Int::class.javaObjectType, Double::class.javaObjectType, String::class.java)
            .addRow(1, 3.14, "pi")
            .addRow(2, 6.28, "tau")
            .build()
        globals["dataset2"] = DatasetBuilder.newBuilder()
            .colNames("a", "b2", "c2")
            .colTypes(Int::class.javaObjectType, Double::class.javaObjectType, String::class.java)
            .addRow(1, 3.1415, "pi2")
            .addRow(2, 56, "tau2")
            .build()

        val tempArray: Array<Array<Int>> = arrayOf(
            arrayOf(0, 1, 2),
            arrayOf(3),
        )
        globals["splitAt"] = tempArray

        val excelSample =
            DatasetExtensionsTests::class.java.getResourceAsStream("sample.xlsx")!!.readAllBytes()
        val tempXlsx = tempfile(suffix = "xlsx").also {
            it.writeBytes(excelSample)
        }
        globals["xlsxBytes"] = excelSample
        globals["xlsxFile"] = tempXlsx.toString()

        val excelSample2 =
            DatasetExtensionsTests::class.java.getResourceAsStream("sample2.xlsx")!!.readAllBytes()
        val tempXlsx2 = tempfile(suffix = "xlsx").also {
            it.writeBytes(excelSample2)
        }
        globals["xlsxFile2"] = tempXlsx2.toString()
        globals["xlsxBytes2"] = excelSample2

        val xlsSample =
            DatasetExtensionsTests::class.java.getResourceAsStream("sample.xls")!!.readAllBytes()
        val tempXls = tempfile(suffix = "xls").also {
            it.writeBytes(xlsSample)
        }
        globals["xlsBytes"] = xlsSample
        globals["xlsFile"] = tempXls.toString()

        globals["date"] = Date::class.java
        globals["color"] = Color::class.java
        globals["javaInt"] = Int::class.java
        globals["javaString"] = String::class.java
        globals["javaBool"] = Boolean::class.java
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
        PyDatasetBuilder.register()

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

        context("Left Join test") {
            test("Left Join") {
                eval<Dataset>("utils.leftJoin(dataset, dataset2, 0, 0)").asClue {
                    it.columnNames shouldBe listOf("a", "b", "c", "a", "b2", "c2")
                    it.columnTypes shouldBe listOf(
                        Int::class.javaObjectType,
                        Double::class.javaObjectType,
                        String::class.java,
                        Int::class.javaObjectType,
                        Double::class.javaObjectType,
                        String::class.java,
                    )
                    it.rowCount shouldBe 2
                    it.getColumnAsList(0) shouldBe listOf(1, 2)
                    it.getColumnAsList(1) shouldBe listOf(3.14, 6.28)
                    it.getColumnAsList(2) shouldBe listOf("pi", "tau")
                    it.getColumnAsList(3) shouldBe listOf(1, 2)
                    it.getColumnAsList(4) shouldBe listOf(3.1415, 56.0)
                    it.getColumnAsList(5) shouldBe listOf("pi2", "tau2")
                }
            }
        }

        // https://www.sqlshack.com/sql-outer-join-overview-and-examples/
        context("Outer Join test") {
            test("Outer Join") {
                eval<Dataset>("utils.outerJoin(outerJoin1, outerJoin2, 0, 0)").asClue {
                    it.columnNames shouldBe listOf("EmpID", "EmpName", "City", "Designation", "EmpID", "Department_ID", "DepartmentName")
                    it.columnTypes shouldBe listOf(
                        Int::class.javaObjectType,
                        String::class.java,
                        String::class.java,
                        String::class.java,
                        Int::class.javaObjectType,
                        Int::class.javaObjectType,
                        String::class.java,
                    )
                    it.getColumnAsList(6) shouldBe listOf("Executive", "Document Control", "Finance", "Engineering", "Facilities and Maintenance", "Finance", null, null, null, "Facilities and Maintenance")
                    it.rowCount shouldBe 10
                }
            }
        }

        context("Right Join test") {
            test("Right Join") {
                eval<Dataset>("utils.rightJoin(outerJoin1, outerJoin2, 0, 0)").asClue {
                    it.columnNames shouldBe listOf("EmpID", "EmpName", "City", "Designation", "EmpID", "Department_ID", "DepartmentName")
                    it.columnTypes shouldBe listOf(
                        Int::class.javaObjectType,
                        String::class.java,
                        String::class.java,
                        String::class.java,
                        Int::class.javaObjectType,
                        Int::class.javaObjectType,
                        String::class.java,
                    )
                    it.getColumnAsList(5) shouldBe listOf(0, 1, 2, 3, 4, 2, 4)
                    it.rowCount shouldBe 7
                }
            }
        }

        context("Inner Join test") {
            test("Inner Join") {
                eval<Dataset>("utils.innerJoin(outerJoin1, outerJoin2, 0, 0)").asClue {
                    it.columnNames shouldBe listOf("EmpID", "EmpName", "City", "Designation", "EmpID", "Department_ID", "DepartmentName")
                    it.columnTypes shouldBe listOf(
                        Int::class.javaObjectType,
                        String::class.java,
                        String::class.java,
                        String::class.java,
                        Int::class.javaObjectType,
                        Int::class.javaObjectType,
                        String::class.java,
                    )
                    it.getColumnAsList(5) shouldBe listOf(0, 1, 2, 3, 4, 2, 4)
                    it.rowCount shouldBe 7
                }
            }
        }

        context("Dataset Splitter") {
            test("Split Dataset") {
                eval<Array<Dataset>>("utils.splitter(outerJoin1, splitAt)").asClue {
                    it.get(0).columnNames shouldBe listOf("EmpID", "EmpName", "City")
                    it.get(0).columnTypes shouldBe listOf(
                        Int::class.javaObjectType,
                        String::class.java,
                        String::class.java,
                    )
                    it.get(0).rowCount shouldBe 10
                    it.get(1).columnNames shouldBe listOf("Designation")
                    it.get(1).columnTypes shouldBe listOf(
                        String::class.java,
                    )
                    it.get(1).rowCount shouldBe 10
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
            test("With String Override") {
                eval<Dataset>("utils.fromExcel(xlsxBytes2, headerRow=0, typeOverrides={2: 'str', 4: 'str'})").asClue {
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
                    it.columnTypes shouldBe listOf(
                        String::class.java,
                        Any::class.java,
                        String::class.java,
                        String::class.java,
                        String::class.java,
                        Int::class.javaObjectType,
                        Int::class.javaObjectType,
                        Int::class.javaObjectType,
                        Int::class.javaObjectType,
                        Int::class.javaObjectType,
                        Int::class.javaObjectType,
                        Int::class.javaObjectType,
                        Date::class.java,
                        Int::class.javaObjectType,
                        String::class.java,
                        String::class.java,
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

        context("Builder") {
            test("Basic usage") {
                eval<Dataset>("utils.builder(a=int, b=str, c=bool).addRow(1, '2', False).build()").asClue {
                    it.rowCount shouldBe 1
                    it.columnCount shouldBe 3
                    it.columnNames shouldBe listOf(
                        "a",
                        "b",
                        "c",
                    )
                    it.columnTypes shouldBe listOf(
                        Int::class.java,
                        String::class.java,
                        Boolean::class.java,
                    )
                }
            }

            test("String type codes in builder call") {
                eval<Dataset>("utils.builder(a='i', b='str', c='b').addRow(1, '2', False).build()").asClue {
                    it.rowCount shouldBe 1
                    it.columnCount shouldBe 3
                    it.columnNames shouldBe listOf(
                        "a",
                        "b",
                        "c",
                    )
                    it.columnTypes shouldBe listOf(
                        Int::class.java,
                        String::class.java,
                        Boolean::class.java,
                    )
                }
            }

            test("Separate colTypes as java types") {
                eval<Dataset>(
                    """
                    utils.builder() \
                        .colNames('a', 'b', 'c') \
                        .colTypes(javaInt, javaString, javaBool) \
                        .addRow(1, '2', False) \
                        .build()
                    """.trimIndent(),
                ).asClue {
                    it.rowCount shouldBe 1
                    it.columnCount shouldBe 3
                    it.columnNames shouldBe listOf(
                        "a",
                        "b",
                        "c",
                    )
                    it.columnTypes shouldBe listOf(
                        Int::class.java,
                        String::class.java,
                        Boolean::class.java,
                    )
                }
            }

            test("Separate colTypes as Python types") {
                eval<Dataset>(
                    """
                    utils.builder() \
                        .colNames('a', 'b', 'c') \
                        .colTypes(int, str, bool) \
                        .addRow(1, '2', False) \
                        .build()
                    """.trimIndent(),
                ).asClue {
                    it.rowCount shouldBe 1
                    it.columnCount shouldBe 3
                    it.columnNames shouldBe listOf(
                        "a",
                        "b",
                        "c",
                    )
                    it.columnTypes shouldBe listOf(
                        Int::class.java,
                        String::class.java,
                        Boolean::class.java,
                    )
                }
            }

            test("Separate colTypes as string shortcodes") {
                eval<Dataset>(
                    """
                    utils.builder() \
                        .colNames('a', 'b', 'c') \
                        .colTypes('i', 'str', 'b') \
                        .addRow(1, '2', False) \
                        .build()
                    """.trimIndent(),
                ).asClue {
                    it.rowCount shouldBe 1
                    it.columnCount shouldBe 3
                    it.columnNames shouldBe listOf(
                        "a",
                        "b",
                        "c",
                    )
                    it.columnTypes shouldBe listOf(
                        Int::class.java,
                        String::class.java,
                        Boolean::class.java,
                    )
                }
            }

            test("Complex types") {
                eval<Dataset>(
                    """
                    utils.builder(date=date, color=color, unicode=unicode) \
                        .addRow(date(), color.RED, u'test') \
                        .build()
                    """.trimIndent(),
                ).asClue {
                    it.rowCount shouldBe 1
                    it.columnCount shouldBe 3
                    it.columnNames shouldBe listOf(
                        "date",
                        "color",
                        "unicode",
                    )
                    it.columnTypes shouldBe listOf(
                        Date::class.java,
                        Color::class.java,
                        String::class.java,
                    )
                }
            }

            test("Nulls in rows") {
                eval<Dataset>(
                    """
                    utils.builder(a=int, b=str, c=bool) \
                        .addRow(1, '2', False) \
                        .addRow(None, None, None) \
                        .build()
                    """.trimIndent(),
                ).asClue {
                    it.rowCount shouldBe 2
                    it.columnCount shouldBe 3
                    it.columnNames shouldBe listOf(
                        "a",
                        "b",
                        "c",
                    )
                    it.columnTypes shouldBe listOf(
                        Int::class.java,
                        String::class.java,
                        Boolean::class.java,
                    )
                }
            }

            test("Empty dataset") {
                eval<Dataset>("utils.builder().build()").asClue {
                    it.rowCount shouldBe 0
                    it.columnCount shouldBe 0
                }
            }

            test("Add a row as a list") {
                eval<Dataset>(
                    """
                    utils.builder(a=int, b=str, c=bool) \
                        .addRow([1, '2', False]) \
                        .build()
                    """.trimIndent(),
                ).asClue {
                    it.rowCount shouldBe 1
                    it.columnCount shouldBe 3
                }
            }

            test("Add a row as a tuple") {
                eval<Dataset>(
                    """
                    utils.builder(a=int, b=str, c=bool) \
                        .addRow((1, '2', False)) \
                        .build()
                    """.trimIndent(),
                ).asClue {
                    it.rowCount shouldBe 1
                    it.columnCount shouldBe 3
                }
            }

            test("Columns with complex names") {
                eval<Dataset>(
                    """
                    utils.builder(**{'a space': int, u'😍': bool, r',./;\'[]-=<>?:"{}|_+!@#%^&*()`~': str}) \
                        .addRow((1, '2', False)) \
                        .build()
                    """.trimIndent(),
                ).asClue {
                    it.rowCount shouldBe 1
                    it.columnCount shouldBe 3
                    it.columnNames shouldBe listOf(
                        "a space",
                        "\uD83D\uDE0D",
                        """,./;\'[]-=<>?:"{}|_+!@#%^&*()`~""",
                    )
                }
            }

            test("Invalid types") {
                shouldThrowPyException(Py.TypeError) {
                    eval<Dataset>("utils.builder(a=1).build()")
                }
                shouldThrowPyException(Py.TypeError) {
                    eval<Dataset>("utils.builder(a=None).build()")
                }
            }

            test("Row without enough vararg values") {
                shouldThrowPyException(Py.TypeError) {
                    eval<Dataset>(
                        """
                        utils.builder(a=int, b=str, c=bool) \
                            .addRow(1, '2') \
                            .build()
                        """.trimIndent(),
                    )
                }
            }

            test("Row without enough list values") {
                shouldThrowPyException(Py.TypeError) {
                    eval<Dataset>(
                        """
                        utils.builder(a=int, b=str, c=bool) \
                            .addRow([1, '2']) \
                            .build()
                        """.trimIndent(),
                    )
                }
            }

            test("Column types regression (issue #24)") {
                eval<Dataset>(
                    """
                        builder.colNames(dataset.columnNames) \
                            .colTypes(dataset.columnTypes) \
                            .build()
                    """.trimIndent(),
                )
            }
        }
    }
}

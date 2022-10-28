package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.script.PyArgParser
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.inductiveautomation.ignition.common.util.DatasetBuilder
import org.apache.poi.ss.usermodel.CellType.BOOLEAN
import org.apache.poi.ss.usermodel.CellType.FORMULA
import org.apache.poi.ss.usermodel.CellType.NUMERIC
import org.apache.poi.ss.usermodel.CellType.STRING
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.python.core.Py
import org.python.core.PyFunction
import org.python.core.PyObject
import java.io.File
import java.math.BigDecimal
import java.util.Date
import kotlin.math.max

object DatasetExtensions {
    @Suppress("unused")
    @ScriptFunction(docBundlePrefix = "DatasetExtensions")
    @KeywordArgs(
        names = ["dataset", "mapper", "preserveColumnTypes"],
        types = [Dataset::class, PyFunction::class, Boolean::class],
    )
    fun map(args: Array<PyObject>, keywords: Array<String>): Dataset? {
        val parsedArgs = PyArgParser.parseArgs(
            args,
            keywords,
            arrayOf("dataset", "mapper", "preserveColumnTypes"),
            arrayOf(Dataset::class.java, PyObject::class.java, Boolean::class.javaObjectType),
            "map",
        )
        val dataset = parsedArgs.requirePyObject("dataset").toJava<Dataset>()
        val mapper = parsedArgs.requirePyObject("mapper")
        val preserveColumnTypes = parsedArgs.getBoolean("preserveColumnTypes").filter { it }.isPresent

        val columnTypes = if (preserveColumnTypes) {
            dataset.columnTypes
        } else {
            List(dataset.columnCount) { Any::class.java }
        }

        val builder = DatasetBuilder.newBuilder().colNames(dataset.columnNames).colTypes(columnTypes)

        for (row in dataset.rowIndices) {
            val columnValues = Array<PyObject>(dataset.columnCount) { col ->
                Py.java2py(dataset[row, col])
            }
            val returnValue = mapper.__call__(columnValues, dataset.columnNames.toTypedArray())

            val newValues = returnValue.asIterable().map(TypeUtilities::pyToJava).toTypedArray()
            builder.addRow(*newValues)
        }

        return builder.build()
    }

    @Suppress("unused")
    @ScriptFunction(docBundlePrefix = "DatasetExtensions")
    @KeywordArgs(
        names = ["dataset", "filter"],
        types = [Dataset::class, PyFunction::class],
    )
    fun filter(args: Array<PyObject>, keywords: Array<String>): Dataset? {
        val parsedArgs = PyArgParser.parseArgs(
            args,
            keywords,
            arrayOf("dataset", "filter"),
            arrayOf(Dataset::class.java, PyObject::class.java),
            "filter",
        )
        val dataset = parsedArgs.requirePyObject("dataset").toJava<Dataset>()
        val filter = parsedArgs.requirePyObject("filter")

        val builder = DatasetBuilder.newBuilder()
            .colNames(dataset.columnNames)
            .colTypes(dataset.columnTypes)

        for (row in dataset.rowIndices) {
            val filterArgs = Array(dataset.columnCount + 1) { col ->
                if (col == 0) {
                    Py.newInteger(row)
                } else {
                    Py.java2py(dataset[row, col - 1])
                }
            }
            val filterKeywords = Array(dataset.columnCount + 1) { col ->
                if (col == 0) {
                    "row"
                } else {
                    dataset.getColumnName(col - 1)
                }
            }
            val returnValue = filter.__call__(filterArgs, filterKeywords).__nonzero__()
            if (returnValue) {
                val columnValues = Array(dataset.columnCount) { col ->
                    dataset[row, col]
                }
                builder.addRow(*columnValues)
            }
        }

        return builder.build()
    }

    @Suppress("unused")
    @ScriptFunction(docBundlePrefix = "DatasetExtensions")
    @KeywordArgs(
        names = ["dataset", "output", "includeTypes"],
        types = [Dataset::class, Appendable::class, Boolean::class],
    )
    fun print(args: Array<PyObject>, keywords: Array<String>) {
        val parsedArgs = PyArgParser.parseArgs(
            args,
            keywords,
            arrayOf("dataset", "output", "includeTypes"),
            Array(3) { PyObject::class.java },
            "print",
        )
        val dataset = parsedArgs.requirePyObject("dataset").toJava<Dataset>()
        val appendable = parsedArgs.getPyObject("output")
            .orElse(Py.getSystemState().stdout)
            .let(::PyObjectAppendable)
        val includeTypes = parsedArgs.getBoolean("includeTypes").orElse(false)

        return printDataset(appendable, dataset, includeTypes)
    }

    internal fun printDataset(appendable: Appendable, dataset: Dataset, includeTypes: Boolean = false) {
        val typeNames = List<String>(dataset.columnCount) { column ->
            if (includeTypes) {
                dataset.getColumnType(column).simpleName
            } else {
                ""
            }
        }

        val columnWidths = IntArray(dataset.columnCount) { column ->
            maxOf(
                // longest value in a row
                if (dataset.rowCount > 0) {
                    dataset.rowIndices.maxOf { row -> dataset[row, column].toString().length }
                } else {
                    0
                },
                // longest value in a header
                if (includeTypes) {
                    dataset.getColumnName(column).length + typeNames[column].length + 3 // 3 = two parens and a space
                } else {
                    dataset.getColumnName(column).length
                },
                // absolute minimum width for markdown table (and human eyeballs)
                3,
            )
        }

        val separator = "|"

        fun Sequence<String>.joinToBuffer() {
            joinTo(
                buffer = appendable,
                separator = " $separator ",
                prefix = "$separator ",
                postfix = " $separator\n",
            )
        }

        // headers
        sequence {
            yield("Row")
            for (column in dataset.columnIndices) {
                val headerValue = buildString {
                    append(dataset.getColumnName(column))
                    if (includeTypes) {
                        append(" (").append(typeNames[column]).append(")")
                    }
                    while (length < columnWidths[column]) {
                        insert(0, ' ')
                    }
                }
                yield(headerValue)
            }
        }.joinToBuffer()

        // separator
        sequence {
            yield("---")
            for (column in dataset.columnIndices) {
                yield("-".repeat(columnWidths[column]))
            }
        }.joinToBuffer()

        // data
        val maxRowLength = dataset.rowCount.toString().length.coerceAtLeast(3)
        for (row in dataset.rowIndices) {
            sequence {
                yield(row.toString().padStart(maxRowLength))
                for (column in dataset.columnIndices) {
                    yield(dataset[row, column].toString().padStart(columnWidths[column]))
                }
            }.joinToBuffer()
        }
    }

    @Suppress("unused")
    @ScriptFunction(docBundlePrefix = "DatasetExtensions")
    @KeywordArgs(
        names = ["input", "headerRow", "sheetNumber", "firstRow", "lastRow", "firstColumn", "lastColumn"],
        types = [ByteArray::class, Integer::class, Integer::class, Integer::class, Integer::class, Integer::class, Integer::class],
    )
    fun fromExcel(args: Array<PyObject>, keywords: Array<String>): Dataset {
        val parsedArgs = PyArgParser.parseArgs(
            args,
            keywords,
            arrayOf(
                "input",
                "headerRow",
                "sheetNumber",
                "firstRow",
                "lastRow",
                "firstColumn",
                "lastColumn",
            ),
            Array(7) { Any::class.java },
            "fromExcel",
        )

        when (val input = parsedArgs.requirePyObject("input").toJava<Any>()) {
            is String -> WorkbookFactory.create(File(input))
            is ByteArray -> WorkbookFactory.create(input.inputStream().buffered())
            else -> throw Py.TypeError("Unable to create Workbook from input; should be string or binary data. Got ${input::class.simpleName} instead.")
        }.use { workbook ->
            val sheetNumber = parsedArgs.getInteger("sheetNumber").orElse(0)
            val sheet = workbook.getSheetAt(sheetNumber)

            val headerRow = parsedArgs.getInteger("headerRow").orElse(-1)
            val firstRow = parsedArgs.getInteger("firstRow").orElseGet { max(sheet.firstRowNum, headerRow + 1) }
            val lastRow = parsedArgs.getInteger("lastRow").orElseGet { sheet.lastRowNum }

            val dataRange = firstRow..lastRow

            if (firstRow >= lastRow) {
                throw Py.ValueError("firstRow ($firstRow) must be less than lastRow ($lastRow)")
            }
            if (headerRow >= 0 && headerRow in dataRange) {
                throw Py.ValueError("headerRow must not be in firstRow..lastRow ($dataRange)")
            }

            val columnRow = sheet.getRow(if (headerRow >= 0) headerRow else firstRow)
            val firstColumn = parsedArgs.getInteger("firstColumn").orElseGet { columnRow.firstCellNum.toInt() }
            val lastColumn =
                parsedArgs.getInteger("lastColumn").map { it + 1 }.orElseGet { columnRow.lastCellNum.toInt() }
            if (firstColumn >= lastColumn) {
                throw Py.ValueError("firstColumn ($firstColumn) must be less than lastColumn ($lastColumn)")
            }

            val columnCount = lastColumn - firstColumn

            val dataset = DatasetBuilder()
            dataset.colNames(
                List(columnCount) {
                    if (headerRow >= 0) {
                        columnRow.getCell(it + firstColumn).toString()
                    } else {
                        "Col $it"
                    }
                },
            )

            var typesSet = false
            val columnTypes = mutableListOf<Class<*>>()

            for (i in dataRange) {
                if (i == headerRow) {
                    continue
                }

                val row = sheet.getRow(i)

                val rowValues = Array<Any?>(columnCount) { j ->
                    val cell = row.getCell(j + firstColumn)

                    when (cell?.cellType.takeUnless { it == FORMULA } ?: cell.cachedFormulaResultType) {
                        NUMERIC -> {
                            if (DateUtil.isCellDateFormatted(cell)) {
                                if (!typesSet) {
                                    columnTypes.add(Date::class.java)
                                }
                                cell.dateCellValue
                            } else {
                                val numericCellValue = cell.numericCellValue
                                if (BigDecimal(numericCellValue).scale() == 0) {
                                    if (!typesSet) {
                                        columnTypes.add(Int::class.javaObjectType)
                                    }
                                    numericCellValue.toInt()
                                } else {
                                    if (!typesSet) {
                                        columnTypes.add(Double::class.javaObjectType)
                                    }
                                    numericCellValue
                                }
                            }
                        }

                        STRING -> {
                            if (!typesSet) {
                                columnTypes.add(String::class.java)
                            }
                            cell.stringCellValue
                        }

                        BOOLEAN -> {
                            if (!typesSet) {
                                columnTypes.add(Boolean::class.javaObjectType)
                            }
                            cell.booleanCellValue
                        }

                        else -> {
                            if (!typesSet) {
                                columnTypes.add(Any::class.java)
                            }
                            null
                        }
                    }
                }

                if (!typesSet) {
                    typesSet = true
                    dataset.colTypes(columnTypes)
                }

                dataset.addRow(*rowValues)
            }

            return dataset.build()
        }
    }
}

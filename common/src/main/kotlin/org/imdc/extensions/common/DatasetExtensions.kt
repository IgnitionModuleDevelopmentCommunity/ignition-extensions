package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.script.PyArgParser
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.inductiveautomation.ignition.common.util.DatasetBuilder
import org.python.core.Py
import org.python.core.PyFunction
import org.python.core.PyList
import org.python.core.PyObject
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

        val builder = DatasetBuilder.newBuilder()
            .colNames(dataset.columnNames)
            .colTypes(columnTypes)

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
            val columnValues = Array(dataset.columnCount) { col ->
                dataset[row, col]
            }
            val filterArgs = arrayOf(Py.newInteger(row), PyList(columnValues.toList()))
            val returnValue = filter.__call__(filterArgs).__nonzero__()
            if (returnValue) {
                builder.addRow(*columnValues)
            }
        }

        return builder.build()
    }

    @Suppress("unused")
    @ScriptFunction(docBundlePrefix = "DatasetExtensions")
    @KeywordArgs(
        names = ["dataset", "output"],
        types = [Dataset::class, Appendable::class],
    )
    fun print(args: Array<PyObject>, keywords: Array<String>) {
        val parsedArgs = PyArgParser.parseArgs(
            args,
            keywords,
            arrayOf("dataset", "output"),
            arrayOf(Dataset::class.java, PyObject::class.java),
            "print",
        )
        val dataset = parsedArgs.requirePyObject("dataset").toJava<Dataset>()
        val appendable = parsedArgs.getPyObject("output")
            .orElse(Py.getSystemState().stdout)
            .let(::PyObjectAppendable)

        return appendable.printDataset(dataset)
    }

    internal fun Appendable.printDataset(dataset: Dataset, separator: String = "|") {
        val columnWidths = dataset.columnIndices.associateWith { column ->
            max(
                dataset.rowIndices.maxOf { row ->
                    dataset[row, column].toString().length
                },
                dataset.getColumnName(column).length,
            ).coerceAtLeast(3)
        }

        fun Sequence<String>.joinToBuffer() {
            joinTo(
                buffer = this@printDataset,
                separator = " $separator ",
                prefix = "$separator ",
                postfix = " $separator\n",
            )
        }

        // headers
        sequence {
            yield("Row")
            for (column in dataset.columnIndices) {
                yield(dataset.getColumnName(column).padStart(columnWidths.getValue(column)))
            }
        }.joinToBuffer()

        // separator
        sequence {
            yield("---")
            for (column in dataset.columnIndices) {
                yield("-".repeat(columnWidths.getValue(column)))
            }
        }.joinToBuffer()

        // data
        val maxRowLength = dataset.rowCount.toString().length.coerceAtLeast(3)
        for (row in dataset.rowIndices) {
            sequence {
                yield(row.toString().padStart(maxRowLength))
                for (column in dataset.columnIndices) {
                    yield(dataset[row, column].toString().padStart(columnWidths.getValue(column)))
                }
            }.joinToBuffer()
        }
    }
}

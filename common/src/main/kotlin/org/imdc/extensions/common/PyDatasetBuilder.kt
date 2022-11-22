package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.script.DisposablePyObjectAdapter
import com.inductiveautomation.ignition.common.util.DatasetBuilder
import com.inductiveautomation.ignition.common.xmlserialization.ClassNameResolver
import org.imdc.extensions.common.DatasetExtensions.asJavaClass
import org.python.core.Py
import org.python.core.PyObject
import org.python.core.adapter.PyObjectAdapter

@Suppress("unused")
class PyDatasetBuilder(private val builder: DatasetBuilder) : PyObject() {
    private val resolver = ClassNameResolver.createBasic()

    fun colNames(vararg names: String) = apply {
        builder.colNames(names.toList())
    }

    fun colNames(names: List<String>) = apply {
        builder.colNames(names)
    }

    fun colTypes(vararg types: PyObject) = apply {
        builder.colTypes(types.map { it.asJavaClass() })
    }

    fun colTypes(types: List<Class<*>>) = apply {
        builder.colTypes(types)
    }

    fun addRow(vararg values: Any?) = apply {
        builder.addRow(*values)
    }

    fun build(): Dataset = builder.build()

    companion object {
        private val adapter = DisposablePyObjectAdapter(DatasetBuilderAdapter())

        fun register() {
            Py.getAdapter().addPostClass(adapter)
        }

        fun unregister() {
            adapter.dispose()
        }
    }
}

class DatasetBuilderAdapter : PyObjectAdapter {
    override fun adapt(o: Any?): PyObject = PyDatasetBuilder(o as DatasetBuilder)
    override fun canAdapt(o: Any?): Boolean = o is DatasetBuilder
}

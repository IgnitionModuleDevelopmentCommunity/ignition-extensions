package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.BasicDataset
import com.inductiveautomation.ignition.common.Dataset

class DSBuilder {
    data class Column(val name: String, val rows: List<*>, val type: Class<*>)

    val columns = mutableListOf<Column>()

    inline fun <reified T> column(name: String, data: List<T>) {
        columns.add(Column(name, data, T::class.java))
    }

    inline fun <reified T> column(name: String, builder: MutableList<T>.() -> Unit) {
        column(name, buildList(builder))
    }

    fun build(): Dataset {
        val colCount = columns.size
        val rowCount = columns.maxOf { it.rows.size }
        val data = Array(colCount) { arrayOfNulls<Any>(rowCount) }

        for (c in 0 until colCount) {
            for (r in 0 until rowCount) {
                data[c][r] = columns.getOrNull(c)?.rows?.getOrNull(r)
            }
        }

        return BasicDataset(columns.map { it.name }, columns.map { it.type }, data)
    }

    companion object {
        fun dataset(block: DSBuilder.() -> Unit): Dataset {
            return DSBuilder().apply(block).build()
        }
    }
}

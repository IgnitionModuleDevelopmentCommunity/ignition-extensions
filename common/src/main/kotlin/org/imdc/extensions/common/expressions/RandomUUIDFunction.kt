package org.imdc.extensions.common.expressions

import com.inductiveautomation.ignition.common.expressions.Expression
import com.inductiveautomation.ignition.common.expressions.functions.AbstractFunction
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import java.util.UUID

class RandomUUIDFunction : AbstractFunction() {
    override fun validateNumArgs(num: Int): Boolean = num == 0
    override fun execute(expressions: Array<out Expression>): QualifiedValue {
        return BasicQualifiedValue(UUID.randomUUID())
    }

    override fun getArgDocString(): String = ""
    override fun getFunctionDisplayName(): String = NAME
    override fun getType(): Class<*> = UUID::class.java

    companion object {
        const val NAME = "randomUUID"
        const val CATEGORY = "Advanced"
    }
}

package org.imdc.extensions.common.expressions

import com.inductiveautomation.ignition.common.expressions.Expression
import com.inductiveautomation.ignition.common.expressions.functions.AbstractFunction
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualityCode

class IsAvailableFunction : AbstractFunction() {
    override fun validateNumArgs(num: Int): Boolean = num == 1
    override fun execute(expressions: Array<out Expression>): QualifiedValue {
        val qualifiedValue = expressions[0].execute()
        val value =
            qualifiedValue.quality.isNot(QualityCode.Bad_NotFound) && qualifiedValue.quality.isNot(QualityCode.Bad_Disabled)
        return BasicQualifiedValue(value)
    }

    override fun getArgDocString(): String = "value"
    override fun getFunctionDisplayName(): String = NAME
    override fun getType(): Class<*> = Boolean::class.java

    companion object {
        const val NAME = "isAvailable"
        const val CATEGORY = "Logic"
    }
}

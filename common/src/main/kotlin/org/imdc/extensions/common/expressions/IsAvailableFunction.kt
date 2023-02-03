package org.imdc.extensions.common.expressions

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.expressions.Expression
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager
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

sealed class LogicalFunction(val name: String) : AbstractFunction() {
    override fun execute(expressions: Array<out Expression>): QualifiedValue {
        return BasicQualifiedValue(expressions.apply())
    }

    abstract fun Array<out Expression>.apply(): Boolean

    override fun getArgDocString(): String = "values..."
    override fun getFunctionDisplayName(): String = name
    override fun getType(): Class<*> = Boolean::class.java

    object AllOfFunction : LogicalFunction("allOf") {
        override fun Array<out Expression>.apply(): Boolean = all { it.toBoolean() }
    }

    object AnyOfFunction : LogicalFunction("anyOf") {
        override fun Array<out Expression>.apply(): Boolean = any { it.toBoolean() }
    }

    object NoneOfFunction : LogicalFunction("noneOf") {
        override fun Array<out Expression>.apply(): Boolean = none { it.toBoolean() }
    }

    companion object {
        private const val CATEGORY = "Logic"

        private fun Expression.toBoolean(): Boolean = TypeUtilities.toBool(execute().value)

        fun ExpressionFunctionManager.registerLogicFunctions() {
            addFunction(AllOfFunction.name, CATEGORY, AllOfFunction)
            addFunction(AnyOfFunction.name, CATEGORY, AnyOfFunction)
            addFunction(NoneOfFunction.name, CATEGORY, NoneOfFunction)
        }
    }
}

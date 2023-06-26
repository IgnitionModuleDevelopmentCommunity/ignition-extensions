package org.imdc.extensions.common.expressions

import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.expressions.Expression
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager
import com.inductiveautomation.ignition.common.expressions.functions.AbstractFunction
import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue
import com.inductiveautomation.ignition.common.model.values.QualifiedValue

sealed class LogicalPredicate(val name: String) : AbstractFunction() {
    override fun execute(expressions: Array<out Expression>): QualifiedValue {
        return BasicQualifiedValue(expressions.apply())
    }

    abstract fun Array<out Expression>.apply(): Boolean

    override fun getArgDocString(): String = "values..."
    override fun getFunctionDisplayName(): String = name
    override fun getType(): Class<*> = Boolean::class.java

    object AllOfFunction : LogicalPredicate("allOf") {
        override fun Array<out Expression>.apply() = all { it.toBoolean() }
    }

    object AnyOfFunction : LogicalPredicate("anyOf") {
        override fun Array<out Expression>.apply() = any { it.toBoolean() }
    }

    object NoneOfFunction : LogicalPredicate("noneOf") {
        override fun Array<out Expression>.apply() = none { it.toBoolean() }
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

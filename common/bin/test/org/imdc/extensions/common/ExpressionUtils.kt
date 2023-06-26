package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.expressions.AbstractFunctionFactory
import com.inductiveautomation.ignition.common.expressions.ConstantExpression
import com.inductiveautomation.ignition.common.expressions.Expression
import com.inductiveautomation.ignition.common.expressions.ExpressionParseContext
import com.inductiveautomation.ignition.common.expressions.FunctionFactory
import com.inductiveautomation.ignition.common.expressions.functions.Function
import com.inductiveautomation.ignition.common.expressions.parsing.ELParserHarness

class ExpressionTestHarness : AbstractFunctionFactory(null) {
    private val parser = ELParserHarness()

    fun evaluate(expression: String, constants: Map<String, Any?> = emptyMap()): Any? {
        val parseContext = StaticParseContext(constants, this)
        return parser.parse(expression, parseContext).execute().value
    }

    companion object {
        suspend fun withFunction(name: String, function: Function, test: suspend ExpressionTestHarness.() -> Unit) {
            ExpressionTestHarness().apply {
                addFunction(name, "", function)
                test()
            }
        }
    }
}

class StaticParseContext(
    private val keywords: Map<String, Any?>,
    private val functionFactory: FunctionFactory,
) : ExpressionParseContext {
    override fun createBoundExpression(reference: String): Expression {
        return ConstantExpression(keywords.getValue(reference))
    }

    override fun getFunctionFactory(): FunctionFactory {
        return functionFactory
    }
}

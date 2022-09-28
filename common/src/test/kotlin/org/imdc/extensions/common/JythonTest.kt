package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.TypeUtilities
import io.kotest.core.listeners.BeforeEachListener
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.booleans.shouldBeTrue
import org.intellij.lang.annotations.Language
import org.python.core.CompileMode
import org.python.core.CompilerFlags
import org.python.core.Py
import org.python.core.PyBaseException
import org.python.core.PyCode
import org.python.core.PyException
import org.python.core.PyObject
import org.python.core.PyStringMap
import org.python.core.PyType

@Suppress("PyInterpreter")
abstract class JythonTest(init: (globals: PyStringMap) -> Unit) : FunSpec() {
    protected var globals: PyStringMap = PyStringMap()

    init {
        extension(
            object : BeforeEachListener {
                override suspend fun beforeEach(testCase: TestCase) {
                    globals.clear()
                    init(globals)
                }
            },
        )
    }

    protected inline fun <reified T> eval(@Language("python") script: String): T {
        exec("$RESULT = $script")
        return globals[RESULT]
    }

    protected fun exec(@Language("python") script: String?) {
        val compiledCall = compile(script)
        Py.runCode(compiledCall, null, globals)
    }

    private fun compile(@Language("python") script: String?): PyCode {
        return Py.compile_flags(
            script,
            "<test>",
            CompileMode.exec,
            CompilerFlags(CompilerFlags.PyCF_SOURCE_IS_UTF8),
        )
    }

    fun shouldThrowPyException(type: PyObject, case: () -> Unit) {
        require(type is PyType)
        require(type.isSubType(PyBaseException.TYPE))
        try {
            case()
        } catch (exception: PyException) {
            exception.match(type).shouldBeTrue()
        }
    }

    companion object {
        const val RESULT = "__RESULT"

        init {
            Py.setSystemState(Py.defaultSystemState)
        }

        inline operator fun <reified T> PyStringMap.get(variable: String): T {
            val value = this[Py.newStringOrUnicode(variable)]
            return TypeUtilities.pyToJava(value) as T
        }

        operator fun PyStringMap.set(key: String, value: Any?) {
            return __setitem__(key, Py.java2py(value))
        }
    }
}

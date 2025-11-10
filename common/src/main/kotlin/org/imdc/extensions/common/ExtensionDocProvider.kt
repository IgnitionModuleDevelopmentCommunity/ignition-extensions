package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.ignition.common.script.hints.ScriptFunctionDocProvider
import com.inductiveautomation.ignition.common.script.typing.CompletionDescriptor
import java.lang.reflect.Method

private val propertiesFileDocProvider = PropertiesFileDocProvider()

private val WARNING = """
    THIS IS AN UNOFFICIAL IGNITION EXTENSION. 
    IT MAY RELY ON OR EXPOSE UNDOCUMENTED OR DANGEROUS FUNCTIONALITY. 
    USE AT YOUR OWN RISK.
""".trimIndent()

object ExtensionDocProvider : ScriptFunctionDocProvider by propertiesFileDocProvider {
    override fun getMethodDescriptor(path: String, method: Method): CompletionDescriptor.Method? {
        val base = propertiesFileDocProvider.getMethodDescriptor(path, method)

        val unsafeAnnotation = method.getAnnotation<UnsafeExtension>()

        return if (unsafeAnnotation != null) {
            base?.copy(description = buildString {
                append("<html><b>")
                append(WARNING)
                if (unsafeAnnotation.note.isNotEmpty()) {
                    append("<br>").append(unsafeAnnotation.note)
                }
                append("</b><br><br>")
                append(base.description.orEmpty())
            })
        } else {
            base
        }
    }
}

annotation class UnsafeExtension(val note: String = "")

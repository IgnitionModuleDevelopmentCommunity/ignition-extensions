package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.ignition.common.script.hints.ScriptFunctionDocProvider
import java.lang.reflect.Method

private val propertiesFileDocProvider = PropertiesFileDocProvider()

private val WARNING = """
    THIS IS AN UNOFFICIAL IGNITION EXTENSION. 
    IT MAY RELY ON OR EXPOSE UNDOCUMENTED OR DANGEROUS FUNCTIONALITY. 
    USE AT YOUR OWN RISK.
""".trimIndent()

object ExtensionDocProvider : ScriptFunctionDocProvider by propertiesFileDocProvider {
    override fun getMethodDescription(path: String, method: Method): String {
        val methodDescription: String? = propertiesFileDocProvider.getMethodDescription(path, method)
        val unsafeAnnotation = method.getAnnotation<UnsafeExtension>()

        return buildString {
            if (unsafeAnnotation != null) {
                append("<html><b>")
                append(WARNING)
                if (unsafeAnnotation.note.isNotEmpty()) {
                    append("<br>").append(unsafeAnnotation.note)
                }
                append("</b><br><br>")
            }
            append(methodDescription.orEmpty())
        }
    }
}

annotation class UnsafeExtension(val note: String = "")

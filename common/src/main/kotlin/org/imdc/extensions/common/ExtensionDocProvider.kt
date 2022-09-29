package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.ignition.common.script.hints.ScriptFunctionDocProvider
import java.lang.reflect.Method

private val propertiesFileDocProvider = PropertiesFileDocProvider()

object ExtensionDocProvider : ScriptFunctionDocProvider by propertiesFileDocProvider {
    override fun getMethodDescription(path: String, method: Method): String? {
        val methodDescription: String? = propertiesFileDocProvider.getMethodDescription(path, method)
        return if (method.isAnnotationPresent(UnsafeExtension::class.java)) {
            """
            <html><b>
            THIS IS AN UNOFFICIAL IGNITION EXTENSION. IT MAY RELY ON OR EXPOSE UNDOCUMENTED OR DANGEROUS FUNCTIONALITY. USE AT YOUR OWN RISK.
            </b><br>
            ${methodDescription.orEmpty()}
            """.trimIndent()
        } else {
            methodDescription
        }
    }
}

annotation class UnsafeExtension

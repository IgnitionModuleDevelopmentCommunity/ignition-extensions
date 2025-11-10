package org.imdc.extensions.gateway

import com.inductiveautomation.ignition.common.resourcecollection.RuntimeResourceCollection
import com.inductiveautomation.ignition.common.script.ScriptContext
import com.inductiveautomation.ignition.common.script.hints.JythonElement
import com.inductiveautomation.ignition.common.script.hints.ScriptArg
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import org.imdc.extensions.common.ProjectExtensions
import org.python.core.Py

class GatewayProjectExtensions(private val context: GatewayContext) : ProjectExtensions {
    @JythonElement(docBundlePrefix = "GatewayProjectExtensions")
    override fun getProject(): RuntimeResourceCollection {
        val defaultProject = ScriptContext.getDefaultProject()
            .orElseThrow { Py.EnvironmentError("No context project populated") }

        return requireNotNull(getProject(defaultProject)) {
            "No such project $defaultProject"
        }
    }

    @JythonElement(docBundlePrefix = "GatewayProjectExtensions")
    fun getProject(
        @ScriptArg("project", optional = true) project: String,
    ): RuntimeResourceCollection? {
        return context.projectManager.find(project).orElse(null)
    }
}

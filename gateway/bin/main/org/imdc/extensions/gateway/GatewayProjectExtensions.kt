package org.imdc.extensions.gateway

import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.common.script.ScriptContext
import com.inductiveautomation.ignition.common.script.hints.ScriptArg
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import org.imdc.extensions.common.ProjectExtensions
import org.python.core.Py

class GatewayProjectExtensions(private val context: GatewayContext) : ProjectExtensions {
    @ScriptFunction(docBundlePrefix = "GatewayProjectExtensions")
    override fun getProject(): RuntimeProject {
        val defaultProject = ScriptContext.defaultProject() ?: throw Py.EnvironmentError("No context project populated")
        return requireNotNull(getProject(defaultProject)) { "No such project $defaultProject" }
    }

    @ScriptFunction(docBundlePrefix = "GatewayProjectExtensions")
    fun getProject(
        @ScriptArg("project", optional = true) project: String,
    ): RuntimeProject? {
        return context.projectManager.getProject(project).orElse(null)
    }
}

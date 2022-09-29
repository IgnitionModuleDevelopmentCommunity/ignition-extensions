package org.imdc.extensions.client

import com.inductiveautomation.ignition.client.model.ClientContext
import com.inductiveautomation.ignition.common.project.Project
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import org.imdc.extensions.common.ProjectExtensions

class ClientProjectExtensions(private val context: ClientContext) : ProjectExtensions {
    @ScriptFunction(docBundlePrefix = "ClientProjectExtensions")
    override fun getProject(): Project {
        return requireNotNull(context.project)
    }
}

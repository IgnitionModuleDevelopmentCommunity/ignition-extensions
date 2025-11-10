package org.imdc.extensions.client

import com.inductiveautomation.ignition.client.model.ClientContext
import com.inductiveautomation.ignition.common.resourcecollection.ResourceCollection
import com.inductiveautomation.ignition.common.script.hints.JythonElement
import org.imdc.extensions.common.ProjectExtensions

class ClientProjectExtensions(private val context: ClientContext) : ProjectExtensions {
    @JythonElement(docBundlePrefix = "ClientProjectExtensions")
    override fun getProject(): ResourceCollection {
        return requireNotNull(context.project)
    }
}

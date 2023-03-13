package org.imdc.extensions.gateway

import com.inductiveautomation.ignition.common.tags.config.TagConfigurationModel
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import org.imdc.extensions.common.TagExtensions
import org.python.core.Py

class GatewayTagExtensions(private val context: GatewayContext) : TagExtensions() {
    override fun getConfigurationImpl(basePath: TagPath, recursive: Boolean): List<TagConfigurationModel> {
        val provider = (
            context.tagManager.getTagProvider(basePath.source)
                ?: throw Py.ValueError("No such tag provider ${basePath.source}")
            )

        return provider.getTagConfigsAsync(listOf(basePath), recursive, true).get()
    }
}

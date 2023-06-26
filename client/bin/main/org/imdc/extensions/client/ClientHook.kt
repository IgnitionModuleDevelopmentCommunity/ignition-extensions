package org.imdc.extensions.client

import com.inductiveautomation.ignition.client.model.ClientContext
import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.vision.api.client.AbstractClientModuleHook
import org.imdc.extensions.common.DatasetExtensions
import org.imdc.extensions.common.ExtensionDocProvider
import org.imdc.extensions.common.PyDatasetBuilder
import org.imdc.extensions.common.UtilitiesExtensions
import org.imdc.extensions.common.addPropertyBundle
import org.imdc.extensions.common.expressions.IsAvailableFunction
import org.imdc.extensions.common.expressions.LogicalPredicate.Companion.registerLogicFunctions

@Suppress("unused")
class ClientHook : AbstractClientModuleHook() {
    private lateinit var context: ClientContext

    override fun startup(context: ClientContext, activationState: LicenseState) {
        this.context = context

        BundleUtil.get().apply {
            addPropertyBundle<DatasetExtensions>()
            addPropertyBundle<UtilitiesExtensions>()
            addPropertyBundle<ClientProjectExtensions>()
        }

        PyDatasetBuilder.register()
    }

    override fun shutdown() {
        PyDatasetBuilder.unregister()
    }

    override fun initializeScriptManager(manager: ScriptManager) {
        manager.apply {
            addScriptModule("system.dataset", DatasetExtensions, ExtensionDocProvider)
            addScriptModule("system.util", UtilitiesExtensions(context), ExtensionDocProvider)
            addScriptModule("system.project", ClientProjectExtensions(context), ExtensionDocProvider)
        }
    }

    override fun configureFunctionFactory(factory: ExpressionFunctionManager) {
        factory.apply {
            addFunction(
                IsAvailableFunction.NAME,
                IsAvailableFunction.CATEGORY,
                IsAvailableFunction(),
            )
            registerLogicFunctions()
        }
    }
}

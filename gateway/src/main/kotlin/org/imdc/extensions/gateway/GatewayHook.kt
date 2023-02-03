package org.imdc.extensions.gateway

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import org.imdc.extensions.common.DatasetExtensions
import org.imdc.extensions.common.ExtensionDocProvider
import org.imdc.extensions.common.PyDatasetBuilder
import org.imdc.extensions.common.UtilitiesExtensions
import org.imdc.extensions.common.addPropertyBundle
import org.imdc.extensions.common.expressions.IsAvailableFunction

@Suppress("unused")
class GatewayHook : AbstractGatewayModuleHook() {
    private lateinit var context: GatewayContext

    override fun setup(context: GatewayContext) {
        this.context = context

        BundleUtil.get().apply {
            addPropertyBundle<DatasetExtensions>()
            addPropertyBundle<UtilitiesExtensions>()
            addPropertyBundle<GatewayProjectExtensions>()
        }

        PyDatasetBuilder.register()

        context.webResourceManager.addServlet(HistoryServlet.PATH, HistoryServlet::class.java)
    }

    override fun startup(activationState: LicenseState) = Unit

    override fun shutdown() {
        PyDatasetBuilder.unregister()
        context.webResourceManager.removeServlet(HistoryServlet.PATH)
    }

    override fun initializeScriptManager(manager: ScriptManager) {
        manager.apply {
            addScriptModule("system.dataset", DatasetExtensions, ExtensionDocProvider)
            addScriptModule("system.util", UtilitiesExtensions(context), ExtensionDocProvider)
            addScriptModule("system.project", GatewayProjectExtensions(context), ExtensionDocProvider)
        }
    }

    override fun configureFunctionFactory(factory: ExpressionFunctionManager) {
        factory.apply {
            addFunction(
                IsAvailableFunction.NAME,
                IsAvailableFunction.CATEGORY,
                IsAvailableFunction(),
            )
        }
    }

    override fun isFreeModule() = true
    override fun isMakerEditionCompatible() = true
}

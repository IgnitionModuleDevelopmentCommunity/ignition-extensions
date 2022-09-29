package org.imdc.extensions.gateway

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook
import com.inductiveautomation.ignition.gateway.model.GatewayContext
import org.imdc.extensions.common.DatasetExtensions
import org.imdc.extensions.common.ExtensionDocProvider
import org.imdc.extensions.common.UtilitiesExtensions
import org.imdc.extensions.common.addPropertyBundle

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
    }

    override fun startup(activationState: LicenseState) {}
    override fun shutdown() {}

    override fun initializeScriptManager(manager: ScriptManager) {
        manager.apply {
            addScriptModule("system.dataset", DatasetExtensions, ExtensionDocProvider)
            addScriptModule("system.util", UtilitiesExtensions(context), ExtensionDocProvider)
            addScriptModule("system.project", GatewayProjectExtensions(context), ExtensionDocProvider)
        }
    }

    override fun isFreeModule(): Boolean = true
    override fun isMakerEditionCompatible(): Boolean = true
}

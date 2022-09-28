package org.imdc.extensions.client

import com.inductiveautomation.ignition.client.model.ClientContext
import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.common.script.hints.PropertiesFileDocProvider
import com.inductiveautomation.vision.api.client.AbstractClientModuleHook
import org.imdc.extensions.common.DatasetExtensions
import org.imdc.extensions.common.UtilitiesExtensions
import org.imdc.extensions.common.addPropertyBundle

@Suppress("unused")
class ClientHook : AbstractClientModuleHook() {
    private lateinit var context: ClientContext

    override fun startup(context: ClientContext, activationState: LicenseState) {
        this.context = context

        BundleUtil.get().apply {
            addPropertyBundle<DatasetExtensions>()
            addPropertyBundle<UtilitiesExtensions>()
        }
    }

    override fun initializeScriptManager(manager: ScriptManager) {
        manager.addScriptModule("system.dataset", DatasetExtensions, PropertiesFileDocProvider())
        manager.addScriptModule("system.utils", UtilitiesExtensions(context), PropertiesFileDocProvider())
    }
}

package org.imdc.extensions.designer

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager
import com.inductiveautomation.ignition.common.licensing.LicenseState
import com.inductiveautomation.ignition.common.script.ScriptManager
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook
import com.inductiveautomation.ignition.designer.model.DesignerContext
import org.imdc.extensions.common.DatasetExtensions
import org.imdc.extensions.common.ExtensionDocProvider
import org.imdc.extensions.common.PyDatasetBuilder
import org.imdc.extensions.common.UtilitiesExtensions
import org.imdc.extensions.common.addPropertyBundle
import org.imdc.extensions.common.expressions.IsAvailableFunction

@Suppress("unused")
class DesignerHook : AbstractDesignerModuleHook() {
    private lateinit var context: DesignerContext

    override fun startup(context: DesignerContext, activationState: LicenseState) {
        this.context = context

        BundleUtil.get().apply {
            addPropertyBundle<DatasetExtensions>()
            addPropertyBundle<UtilitiesExtensions>()
            addPropertyBundle<DesignerProjectExtensions>()
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
            addScriptModule("system.project", DesignerProjectExtensions(context), ExtensionDocProvider)
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
}

package org.imdc.extensions.designer

import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.inductiveautomation.ignition.designer.IgnitionDesigner
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.project.DesignableProject
import org.apache.commons.lang3.reflect.MethodUtils
import org.imdc.extensions.common.ProjectExtensions
import org.imdc.extensions.common.UnsafeExtension

class DesignerProjectExtensions(private val context: DesignerContext) : ProjectExtensions {
    @ScriptFunction(docBundlePrefix = "DesignerProjectExtensions")
    override fun getProject(): DesignableProject {
        return requireNotNull(context.project)
    }

    @ScriptFunction(docBundlePrefix = "DesignerProjectExtensions")
    @UnsafeExtension
    fun save() {
        MethodUtils.invokeMethod(
            context.frame,
            true, // forceAccess
            "handleSave",
            false, // saveAs
            null, // newName
            false, // commitOnly
            false, // skipReopen
            false, // showDialog
        )
    }

    @ScriptFunction(docBundlePrefix = "DesignerProjectExtensions")
    @UnsafeExtension
    fun update() {
        (context.frame as IgnitionDesigner).updateProject()
    }
}

package org.imdc.extensions.designer

import com.inductiveautomation.ignition.common.gui.progress.DummyTaskProgressListener
import com.inductiveautomation.ignition.common.script.hints.JythonElement
import com.inductiveautomation.ignition.designer.IgnitionDesigner
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.project.DesignableProject
import org.apache.commons.lang3.reflect.MethodUtils
import org.imdc.extensions.common.ProjectExtensions
import org.imdc.extensions.common.UnsafeExtension

class DesignerProjectExtensions(private val context: DesignerContext) : ProjectExtensions {
    @JythonElement(docBundlePrefix = "DesignerProjectExtensions")
    @UnsafeExtension
    override fun getProject(): DesignableProject {
        return requireNotNull(context.project)
    }

    @JythonElement(docBundlePrefix = "DesignerProjectExtensions")
    @UnsafeExtension
    fun save() {
        MethodUtils.invokeMethod(
            context.frame,
            true, // forceAccess
            "handleSave",
            DummyTaskProgressListener(), // pl (ProgressListener)
            false, // saveAs
            null, // newName
            false, // commitOnly
            false, // skipReopen
            false, // showDialog
        )
    }

    @JythonElement(docBundlePrefix = "DesignerProjectExtensions")
    @UnsafeExtension
    fun update() {
        (context.frame as IgnitionDesigner).updateProject()
    }
}

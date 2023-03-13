package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.config.PyTagDictionary
import com.inductiveautomation.ignition.common.config.PyTagList
import com.inductiveautomation.ignition.common.script.PyArgParser
import com.inductiveautomation.ignition.common.script.ScriptContext
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction
import com.inductiveautomation.ignition.common.tags.config.TagConfigurationModel
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser
import org.python.core.PyObject

abstract class TagExtensions {
    @UnsafeExtension
    @ScriptFunction(docBundlePrefix = "TagExtensions")
    fun getLocalConfiguration(args: Array<PyObject>, keywords: Array<String>): PyTagList {
        val parsedArgs = PyArgParser.parseArgs(
            args,
            keywords,
            arrayOf("basePath", "recursive"),
            arrayOf(String::class.java, Boolean::class.java),
            "getLocalConfiguration",
        )
        val configurationModels = getConfigurationImpl(
            parseTagPath(parsedArgs.requireString("basePath")),
            parsedArgs.getBoolean("recursive").orElse(false),
        )

        return PyTagList().apply {
            for (configurationModel in configurationModels) {
                add(
                    PyTagDictionary.Builder().apply {
                        setTagPath(configurationModel.path)
                        setTagType(configurationModel.type)
                        build(configurationModel.localConfiguration)
                    },
                )
            }
        }
    }

    protected open fun parseTagPath(path: String): TagPath {
        val parsed = TagPathParser.parse(ScriptContext.defaultTagProvider(), path)
        if (TagPathParser.isRelativePath(parsed) && ScriptContext.relativeTagPathRoot() != null) {
            return TagPathParser.derelativize(parsed, ScriptContext.relativeTagPathRoot())
        }
        return parsed
    }

    protected abstract fun getConfigurationImpl(basePath: TagPath, recursive: Boolean): List<TagConfigurationModel>
}

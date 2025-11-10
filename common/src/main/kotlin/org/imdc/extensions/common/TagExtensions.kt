package org.imdc.extensions.common

import com.inductiveautomation.ignition.common.config.PyTagDictionary
import com.inductiveautomation.ignition.common.config.PyTagList
import com.inductiveautomation.ignition.common.script.PyArgParser
import com.inductiveautomation.ignition.common.script.ScriptContext
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs
import com.inductiveautomation.ignition.common.script.hints.JythonElement
import com.inductiveautomation.ignition.common.tags.config.TagConfigurationModel
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser
import org.python.core.PyDictionary
import org.python.core.PyObject
import kotlin.jvm.optionals.getOrNull

abstract class TagExtensions {
    @UnsafeExtension
    @JythonElement(docBundlePrefix = "TagExtensions")
    @KeywordArgs(
        names = ["basePath", "recursive"],
        types = [String::class, Boolean::class],
    )
    fun getLocalConfiguration(args: Array<PyObject>, keywords: Array<String>): PyTagList {
        val parsedArgs = PyArgParser.parseArgs(
            args,
            keywords,
            arrayOf("basePath", "recursive"),
            arrayOf(Any::class.java, Any::class.java),
            "getLocalConfiguration",
        )
        val configurationModels = getConfigurationImpl(
            parseTagPath(parsedArgs.requireString("basePath")),
            parsedArgs.getBoolean("recursive").orElse(false),
        )

        return configurationModels.toPyTagList()
    }

    protected open fun parseTagPath(path: String): TagPath {
        val parsed = TagPathParser.parse(ScriptContext.getDefaultTagProvider().orElseThrow(), path)
        ScriptContext.getRelativeTagPathRoot().getOrNull()?.let { tagPathRoot ->
            if(TagPathParser.isRelativePath(parsed)) {
                return TagPathParser.derelativize(parsed, tagPathRoot)
            }
        }
        return parsed
    }

    private fun TagConfigurationModel.toPyDictionary(): PyDictionary {
        return PyTagDictionary.Builder()
            .setTagPath(path)
            .setTagType(type)
            .build(this).apply {
                if (children.isNotEmpty()) {
                    put("tags", children.toPyTagList())
                }
            }
    }

    private fun List<TagConfigurationModel>.toPyTagList() = fold(PyTagList()) { acc, childModel ->
        acc.add(childModel.toPyDictionary())
        acc
    }

    protected abstract fun getConfigurationImpl(basePath: TagPath, recursive: Boolean): List<TagConfigurationModel>
}

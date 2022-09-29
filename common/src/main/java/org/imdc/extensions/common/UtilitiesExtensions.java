package org.imdc.extensions.common;

import java.util.Optional;

import com.inductiveautomation.ignition.common.PyUtilities;
import com.inductiveautomation.ignition.common.model.CommonContext;
import com.inductiveautomation.ignition.common.script.PyArgParser;
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyObject;

public class UtilitiesExtensions {
    private final CommonContext context;

    public UtilitiesExtensions(CommonContext context) {
        this.context = context;
    }

    @ScriptFunction(docBundlePrefix = "UtilitiesExtensions")
    @UnsafeExtension
    public CommonContext getContext() {
        return context;
    }

    @ScriptFunction(docBundlePrefix = "UtilitiesExtensions")
    @KeywordArgs(names = {"object"}, types = {PyObject.class})
    public PyObject deepCopy(PyObject[] args, String[] keywords) {
        PyArgParser parsedArgs = PyArgParser.parseArgs(args, keywords, this.getClass(), "deepCopy");
        Optional<PyObject> maybeObject = parsedArgs.getPyObject("object");
        if (maybeObject.isEmpty()) {
            throw Py.TypeError("Deepcopy requires one argument, got none");
        }
        return recursiveConvert(maybeObject.get());
    }

    private static PyObject recursiveConvert(@NotNull PyObject object) {
        if (object.isMappingType()) {
            return PyUtilities.streamEntries(object)
                .collect(PyUtilities.toPyDictionary(Pair::getKey, pair -> recursiveConvert(pair.getValue())));
        } else if (PyUtilities.isSequence(object)) {
            return PyUtilities.stream(object)
                .map(UtilitiesExtensions::recursiveConvert)
                .collect(PyUtilities.toPyList());
        } else {
            try {
                Iterable<PyObject> iterable = object.asIterable();
                return new PyList(iterable.iterator());
            } catch (PyException pye) {
                return object;
            }
        }
    }
}

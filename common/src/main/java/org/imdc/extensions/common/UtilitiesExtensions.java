package org.imdc.extensions.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.inductiveautomation.ignition.common.PyUtilities;
import com.inductiveautomation.ignition.common.TypeUtilities;
import com.inductiveautomation.ignition.common.expressions.ConstantExpression;
import com.inductiveautomation.ignition.common.expressions.Expression;
import com.inductiveautomation.ignition.common.expressions.ExpressionParseContext;
import com.inductiveautomation.ignition.common.expressions.FunctionFactory;
import com.inductiveautomation.ignition.common.expressions.parsing.ELParserHarness;
import com.inductiveautomation.ignition.common.model.CommonContext;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.script.PyArgParser;
import com.inductiveautomation.ignition.common.script.ScriptContext;
import com.inductiveautomation.ignition.common.script.builtin.KeywordArgs;
import com.inductiveautomation.ignition.common.script.hints.JythonElement;
import com.inductiveautomation.ignition.common.tags.model.TagPath;
import com.inductiveautomation.ignition.common.tags.paths.parser.TagPathParser;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.python.core.Py;
import org.python.core.PyBaseString;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyObject;

public class UtilitiesExtensions {
    private final CommonContext context;

    private static final ELParserHarness EXPRESSION_PARSER = new ELParserHarness();

    public UtilitiesExtensions(CommonContext context) {
        this.context = context;
    }

    @JythonElement(docBundlePrefix = "UtilitiesExtensions")
    @UnsafeExtension
    public CommonContext getContext() {
        return context;
    }

    @JythonElement(docBundlePrefix = "UtilitiesExtensions")
    @KeywordArgs(names = {"object"}, types = {PyObject.class})
    public PyObject deepCopy(PyObject[] args, String[] keywords) {
        PyArgParser parsedArgs = PyArgParser.parseArgs(args, keywords, this.getClass(), "deepCopy");
        var toConvert = parsedArgs.getPyObject("object")
            .orElseThrow(() -> Py.TypeError("deepCopy requires one argument, got none"));
        return recursiveConvert(toConvert);
    }

    private static PyObject recursiveConvert(@NotNull PyObject object) {
        if (object.isMappingType()) {
            return PyUtilities.streamEntries(object)
                .collect(PyUtilities.toPyDictionary(Pair::getKey, pair -> recursiveConvert(pair.getValue())));
        } else if (PyUtilities.isSequence(object)) {
            return PyUtilities.stream(object)
                .map(UtilitiesExtensions::recursiveConvert)
                .collect(PyUtilities.toPyList());
        } else if (object instanceof PyBaseString) {
            return object;
        } else {
            try {
                Iterable<PyObject> iterable = object.asIterable();
                return new PyList(iterable.iterator());
            } catch (PyException pye) {
                return object;
            }
        }
    }

    @JythonElement(docBundlePrefix = "UtilitiesExtensions")
    @KeywordArgs(names = {"expression"}, types = {String.class})
    public QualifiedValue evalExpression(PyObject[] args, String[] keywords) throws Exception {
        if (args.length == 0) {
            throw Py.ValueError("Must supply at least one argument to evalExpression");
        }

        String expression = TypeUtilities.toString(TypeUtilities.pyToJava(args[0]));

        var keywordMap = new HashMap<String, Object>();
        for (int i = 0; i < keywords.length; i++) {
            keywordMap.put(keywords[i], TypeUtilities.pyToJava(args[i + 1]));
        }
        ExpressionParseContext parseContext = new KeywordParseContext(keywordMap);

        Expression actualExpression = EXPRESSION_PARSER.parse(expression, parseContext);
        try {
            actualExpression.startup();
            return actualExpression.execute();
        } finally {
            actualExpression.shutdown();
        }
    }

    private class KeywordParseContext implements ExpressionParseContext {
        private final Map<String, Object> keywords;

        private KeywordParseContext(Map<String, Object> keywords) {
            this.keywords = keywords;
        }

        @Override
        public Expression createBoundExpression(String reference) throws RuntimeException {
            if (reference == null || reference.isEmpty()) {
                throw new IllegalArgumentException("Invalid path " + reference);
            }
            if (keywords.containsKey(reference)) {
                return new ConstantExpression(keywords.get(reference));
            } else {
                try {
                    TagPath path = TagPathParser.parse(ScriptContext.getDefaultTagProvider().orElseThrow(), reference);
                    var tagValues = context.getTagManager().readAsync(List.of(path)).get(30, TimeUnit.SECONDS);
                    return new ConstantExpression(tagValues.get(0).getValue());
                } catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public FunctionFactory getFunctionFactory() {
            return context.getExpressionFunctionFactory();
        }
    }

    @JythonElement(docBundlePrefix = "UtilitiesExtensions")
    public UUID getUUID4() {
        return UUID.randomUUID();
    }
}

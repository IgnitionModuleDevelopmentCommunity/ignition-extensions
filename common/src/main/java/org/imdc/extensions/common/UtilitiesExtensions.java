package org.imdc.extensions.common;

import com.inductiveautomation.ignition.common.model.CommonContext;
import com.inductiveautomation.ignition.common.script.hints.ScriptFunction;

public class UtilitiesExtensions {
    private final CommonContext context;

    public UtilitiesExtensions(CommonContext context) {
        this.context = context;
    }

    @ScriptFunction(docBundlePrefix = "UtilitiesExtensions")
    public CommonContext getContext() {
        return context;
    }
}

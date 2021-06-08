package org.yamcs.studio.displays;

import java.util.ArrayList;
import java.util.List;

public class ParameterTable {

    private List<String> parameters;

    public ParameterTable() {
        parameters = new ArrayList<>();
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
}

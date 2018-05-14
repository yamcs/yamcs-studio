package org.yamcs.studio.alphanumeric;

import java.util.ArrayList;
import java.util.List;

public class AlphaNumericJson {

    private List<String> columns;
    private List<String> parameterList;
    
    public AlphaNumericJson() {
        parameterList = new ArrayList<>();
        columns = new ArrayList<>();
        columns.add("Parameter");
        columns.add("Eng Value");
        columns.add("Raw Value");
    }
    
    public List<String> getColumns() {
        return columns;
    }
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
    public List<String> getParameterList() {
        return parameterList;
    }
    public void setParameterList(List<String> parameterList) {
        this.parameterList = parameterList;
    }
}

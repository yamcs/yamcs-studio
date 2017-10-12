package org.yamcs.studio.ui.commanding.cmdhist;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CommandHistoryFilters {
    private List<Filter> filters = new ArrayList<Filter>();
    private String currentFilter;

    public Filter getCurrentFilter() {
        return filters.stream().filter((a) -> a.filterName.equals(currentFilter)).findFirst().get();
    }

    public Filter setCurrentFilter(String currentFilter) {
        this.currentFilter = currentFilter;
        return getCurrentFilter();
    }

    public void addFilter(Filter newFilter) {
        filters.add(newFilter);
    }

    public List<Filter> getFilters() {
        return filters;
    }

    static public class Filter {
        public String filterName;
        public List<Pattern> filterFields = new ArrayList<Pattern>();

        public Filter(String name) {
            this.filterName = name;
        }

        public boolean matchFilter(String data) {
            for (Pattern p : filterFields) {
                boolean match = p.matcher(data).find();
                if (match)
                    return true;
            }
            return false;
        }
    }

}

package org.yamcs.studio.ui.commanding.cmdhist;

import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.yamcs.studio.ui.commanding.cmdhist.CommandHistoryFilters.Filter;

public class ComboFilterContribution extends
        WorkbenchWindowControlContribution {

    private CommandHistoryFilters chfs = new CommandHistoryFilters();

    public ComboFilterContribution() {
        // TODO : Allow definition of filters in preference page
        Filter filter = new Filter("Full");
        filter.filterFields.add(Pattern.compile(".*"));
        chfs.addFilter(filter);

        filter = new Filter("Brief");

        filter.filterFields.add(Pattern.compile("^Command$"));
        filter.filterFields.add(Pattern.compile("^PTV$"));
        filter.filterFields.add(Pattern.compile("^Seq.ID$"));
        filter.filterFields.add(Pattern.compile("^FRC$"));
        filter.filterFields.add(Pattern.compile("^DASS$"));
        filter.filterFields.add(Pattern.compile("^MCS$"));
        filter.filterFields.add(Pattern.compile("^[A-Z]$"));
        filter.filterFields.add(Pattern.compile("^Comment$"));
        chfs.addFilter(filter);
    }

    @Override
    protected Control createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout glContainer = new GridLayout(2, false);
        glContainer.marginTop = -1;
        glContainer.marginHeight = 0;
        glContainer.marginWidth = 0;
        container.setLayout(glContainer);

        Label label = new Label(container, SWT.CENTER);
        label.setText("View Profile:");
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

        GridData glReader = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        // glReader.widthHint = 280;
        final Combo comboFilter = new Combo(container, SWT.BORDER | SWT.READ_ONLY
                | SWT.DROP_DOWN);
        comboFilter.setToolTipText("View Profile");
        comboFilter.setLayoutData(glReader);

        for (Filter f : chfs.getFilters()) {
            comboFilter.add(f.filterName);
        }
        comboFilter.select(0);
        comboFilter.pack();

        comboFilter.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Filter selectedFilter = chfs.setCurrentFilter(comboFilter.getText());
                CommandHistoryView.getInstance().applyFilter(selectedFilter);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });

        return container;
    }

}

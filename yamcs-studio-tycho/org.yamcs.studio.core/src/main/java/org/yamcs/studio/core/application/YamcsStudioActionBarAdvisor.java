package org.yamcs.studio.core.application;

import org.csstudio.ui.menu.app.ApplicationActionBarAdvisor;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.application.IActionBarConfigurer;

public class YamcsStudioActionBarAdvisor extends ApplicationActionBarAdvisor {

    public YamcsStudioActionBarAdvisor(IActionBarConfigurer configurer) {
        super(configurer);
    }

    @Override
    protected void fillCoolBar(ICoolBarManager coolbar) {
        super.fillCoolBar(coolbar);
        System.out.println("rrrrrrrrrr");

        coolbar.add(new ControlContribution("table") {

            @Override
            protected Control createControl(Composite parent) {
                return null;
            }

            @Override
            public void fill(CoolBar parent, int index) {
                CoolItem coolItem = new CoolItem(parent, SWT.NONE);
                Table table = new Table(parent, SWT.NONE);
                table.setHeaderVisible(true);
                TableColumn column = new TableColumn(table, SWT.LEFT);
                column.setText("Hello World");
                column.setWidth(120);
                coolItem.setControl(table);
                coolItem.setPreferredSize(table.computeSize(SWT.DEFAULT, 400));
            }
        });
    }
}

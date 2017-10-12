package org.yamcs.studio.theming;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.yamcs.studio.core.ui.utils.ColorLabelProvider;
import org.yamcs.studio.core.ui.utils.RCPUtils;

public class ThemeColorBlock extends MasterDetailsBlock {

    private StyleDefinition def;
    private Map<RGB, Color> colorMap = new HashMap<>();
    private Image upImage;
    private Image downImage;

    public ThemeColorBlock(StyleDefinition def, Device device, ResourceManager resourceManager) {
        this.def = def;
        for (ThemeColor color : def.getColors()) {
            RGB rgb = color.getRGB();
            if (!colorMap.containsKey(rgb)) {
                colorMap.put(rgb, resourceManager.createColor(rgb));
            }
        }

        upImage = resourceManager.createImage(RCPUtils.getImageDescriptor(ThemeColorBlock.class, "icons/up.gif"));
        downImage = resourceManager.createImage(RCPUtils.getImageDescriptor(ThemeColorBlock.class, "icons/down.gif"));
    }

    @Override
    protected void createMasterPart(IManagedForm managedForm, Composite parent) {
        FormToolkit tk = managedForm.getToolkit();
        Section section = tk.createSection(parent, Section.NO_TITLE);
        section.marginWidth = 10;
        section.marginHeight = 5;

        Composite client = tk.createComposite(section, SWT.WRAP);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 2;
        layout.marginHeight = 2;
        client.setLayout(layout);

        Composite tableWrapper = tk.createComposite(client, SWT.NONE);
        tableWrapper.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        TableColumnLayout tcl = new TableColumnLayout();
        tableWrapper.setLayout(tcl);

        Table t = tk.createTable(tableWrapper, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
        t.setHeaderVisible(false);
        t.setLinesVisible(false);

        tk.paintBordersFor(client);

        Composite buttonWrapper = tk.createComposite(client);
        buttonWrapper.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));

        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        buttonWrapper.setLayout(gl);

        Button b = tk.createButton(buttonWrapper, "Add", SWT.PUSH);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        b.setLayoutData(gd);

        b = tk.createButton(buttonWrapper, "Remove", SWT.PUSH);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        b.setLayoutData(gd);

        b = tk.createButton(buttonWrapper, "Up", SWT.PUSH);
        b.setImage(upImage);
        b.setToolTipText("Move Up");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        b.setLayoutData(gd);

        b = tk.createButton(buttonWrapper, "Down", SWT.PUSH);
        b.setImage(downImage);
        b.setToolTipText("Move Down");
        gd = new GridData(GridData.FILL_HORIZONTAL);
        b.setLayoutData(gd);

        section.setClient(client);

        SectionPart spart = new SectionPart(section);
        managedForm.addPart(spart);
        TableViewer viewer = new TableViewer(t);
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.addSelectionChangedListener(evt -> {
            managedForm.fireSelectionChanged(spart, evt.getSelection());
        });

        TableViewerColumn colorColumn = new TableViewerColumn(viewer, SWT.NONE);
        colorColumn.setLabelProvider(new ColorLabelProvider() {
            @Override
            public Color getColor(Object element) {
                ThemeColor color = (ThemeColor) element;
                return colorMap.get(color.getRGB());
            }

            @Override
            public Color getBorderColor(Object element) {
                return parent.getDisplay().getSystemColor(SWT.COLOR_BLACK);
            }
        });
        tcl.setColumnData(colorColumn.getColumn(), new ColumnWeightData(50, 50, false));

        TableViewerColumn textColumn = new TableViewerColumn(viewer, SWT.NONE);
        textColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                ThemeColor color = (ThemeColor) element;
                return color.getLabel();
            }
        });
        tcl.setColumnData(textColumn.getColumn(), new ColumnWeightData(200, 200, true));

        viewer.setInput(def.getColors());
    }

    @Override
    protected void createToolBarActions(IManagedForm managedForm) {
        sashForm.setWeights(new int[] { 30, 70 });
    }

    @Override
    protected void registerPages(DetailsPart detailsPart) {
        detailsPart.registerPage(ThemeColor.class, new ThemeColorDetailsPage());
    }
}

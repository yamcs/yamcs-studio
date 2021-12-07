/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.csstudio.opibuilder.visualparts;

import java.util.logging.Level;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.platform.ui.composites.resourcefilter.ResourceSelectionGroup;
import org.csstudio.utility.batik.SVGUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class represents a Dialog to choose a file (applying or not filters) in the workspace. There is an option to
 * return or not relative path.
 */
public final class FilePathDialogWithFilter extends Dialog implements Listener {

    /**
     * The default value for the file extensions.
     */
    private static final String[] IMAGE_EXTENSIONS = new String[] { "gif", "GIF", "png", "PNG", "svg", "SVG" };

    /**
     * The message to display, or <code>null</code> if none.
     */
    private String message;

    /**
     * The {@link WorkspaceTreeComposite} for this dialog.
     */
    private ResourceSelectionGroup resourceSelectionGroup;

    /**
     * The file extensions of files that will be shown for selection.
     */
    private String[] filters;

    /**
     * The path of the selected resource.
     */
    private IPath path;

    private IPath refPath;

    private Text resourcePathText;

    private Label imgOverview;

    private boolean relative;

    private boolean filtered;

    private static final int MAX_ICON_WIDTH = 100;
    private static final int MAX_ICON_HEIGHT = 100;
    private static final String PNG_EXT = "png";
    private static final String GIF_EXT = "gif";
    private static final String SVG_EXT = "svg";

    /**
     * Creates an input dialog with OK and Cancel buttons. Note that the dialog will have no visual representation (no
     * widgets) until it is told to open.
     * <p>
     * Note that the <code>open</code> method blocks for input dialogs.
     * </p>
     *
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level shell
     * @param refPath
     *            the reference path which doesn't include the file name.
     * @param dialogMessage
     *            the dialog message, or <code>null</code> if none
     * @param filters
     */
    public FilePathDialogWithFilter(Shell parentShell, IPath refPath, String dialogMessage, String[] filters) {
        super(parentShell);
        this.setShellStyle(SWT.MODELESS | SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER | SWT.RESIZE);
        this.message = dialogMessage;
        this.refPath = refPath;
        relative = true;
        filtered = true;
        this.filters = filters;
    }

    /**
     * Sets the initially selected resource. Must be called before the dialog is displayed.
     *
     * @param path
     *            the path to the initially selected resource.
     */
    public void setSelectedResource(IPath path) {
        this.path = path;
        relative = !path.isAbsolute();
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Resources");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));
        if (message != null) {
            var label = new Label(composite, SWT.WRAP);
            label.setText(message);
            var data = new GridData(
                    GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
            data.horizontalSpan = 2;
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            label.setLayoutData(data);
        }

        // The New Project and New Folder actions will be shown if there are
        // no file extensions, i.e. if the dialog is opened to select a folder.
        var showNewContainerActions = (filters == null || filters.length == 0);
        resourceSelectionGroup = new ResourceSelectionGroup(composite, this, filters, showNewContainerActions);

        var wrapper = new Group(composite, SWT.NONE);
        wrapper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        var gridLayout = new GridLayout(2, false);
        wrapper.setLayout(gridLayout);

        var text = new Label(wrapper, SWT.NONE);
        text.setText("Resource Path:");

        // Image overview
        imgOverview = new Label(wrapper, SWT.NONE);
        var gridData = new GridData();
        gridData.widthHint = MAX_ICON_WIDTH;
        gridData.verticalSpan = 4;
        gridData.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
        gridData.grabExcessVerticalSpace = false;
        gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_CENTER;
        gridData.grabExcessHorizontalSpace = false;
        imgOverview.setLayoutData(gridData);

        resourcePathText = new Text(wrapper, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        resourcePathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        if (path != null && !path.isEmpty()) {
            resourcePathText.setText(path.toString());
            if (relative) {
                resourceSelectionGroup.setSelectedResource(refPath.append(path));
            } else {
                resourceSelectionGroup.setSelectedResource(path);
            }
        }

        // the check box for relative path
        var checkBox = new Button(wrapper, SWT.CHECK);
        checkBox.setSelection(relative);
        checkBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        checkBox.setText("Return relative path");
        checkBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                relative = checkBox.getSelection();
                if (relative && path != null) {
                    resourcePathText.setText(ResourceUtil.buildRelativePath(refPath, path).toString());
                } else {
                    resourcePathText.setText(path.toString());
                }
            }
        });

        // the check box for name filter
        var filterCheckBox = new Button(wrapper, SWT.CHECK);
        filterCheckBox.setSelection(filtered);
        filterCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        filterCheckBox.setText("Filter file name with PV name");
        filterCheckBox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                filtered = filterCheckBox.getSelection();
                if (filtered) {
                    resourceSelectionGroup.refreshTreeWithFilter(filters);
                } else {
                    resourceSelectionGroup.refreshTreeWithFilter(IMAGE_EXTENSIONS);
                }
            }
        });

        return composite;
    }

    @Override
    protected void okPressed() {
        path = new Path(resourcePathText.getText());
        super.okPressed();
    }

    public IPath getSelectedResource() {
        return path;
    }

    @Override
    public void handleEvent(Event event) {
        var widget = (ResourceSelectionGroup) event.widget;
        path = widget.getFullPath();
        if (path == null) {
            return;
        }
        if (relative) {
            resourcePathText.setText(ResourceUtil.buildRelativePath(refPath, path).toString());
        } else {
            resourcePathText.setText(path.toString());
        }
        displayOverview(widget.getFullPath());
    }

    private void displayOverview(IPath imgPath) {
        if (imgPath == null || imgPath.isEmpty()) {
            return;
        }
        try {
            ImageData data = null;
            if (GIF_EXT.equalsIgnoreCase(imgPath.getFileExtension())
                    || PNG_EXT.equalsIgnoreCase(imgPath.getFileExtension())) {
                var inputStream = ResourceUtil.pathToInputStream(imgPath);
                var tmpData = new ImageData(inputStream);

                var ratio = (float) tmpData.width / tmpData.height;
                if (ratio >= 1 && tmpData.width > MAX_ICON_WIDTH) {
                    var ratio2 = (float) MAX_ICON_WIDTH / tmpData.width;
                    data = tmpData.scaledTo(MAX_ICON_WIDTH, Math.round((float) tmpData.height * ratio2));
                } else if (ratio < 1 && tmpData.height > MAX_ICON_HEIGHT) {
                    var ratio2 = (float) MAX_ICON_HEIGHT / tmpData.height;
                    data = tmpData.scaledTo(Math.round((float) tmpData.width * ratio2), MAX_ICON_HEIGHT);
                } else {
                    data = tmpData;
                }
            } else if (SVG_EXT.equalsIgnoreCase(imgPath.getFileExtension())) {
                var inputStream = ResourceUtil.pathToInputStream(imgPath);
                data = SVGUtils.loadSVG(imgPath, inputStream, MAX_ICON_WIDTH, MAX_ICON_HEIGHT);
            }
            if (data != null && data.width <= MAX_ICON_WIDTH && data.height <= MAX_ICON_HEIGHT && imgOverview != null) {
                var img = new Image(Display.getCurrent(), data);
                imgOverview.setImage(img);
            }
        } catch (Exception e) {
            OPIBuilderPlugin.getLogger().log(Level.WARNING, "Error loading file overview: " + imgPath, e);
        }
    }
}

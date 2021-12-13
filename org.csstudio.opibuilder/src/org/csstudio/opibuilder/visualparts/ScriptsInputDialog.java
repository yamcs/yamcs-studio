/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.visualparts;

import java.util.ArrayList;
import java.util.List;

import org.csstudio.opibuilder.OPIBuilderPlugin;
import org.csstudio.opibuilder.model.AbstractWidgetModel;
import org.csstudio.opibuilder.script.PVTuple;
import org.csstudio.opibuilder.script.ScriptData;
import org.csstudio.opibuilder.script.ScriptService;
import org.csstudio.opibuilder.script.ScriptService.ScriptType;
import org.csstudio.opibuilder.script.ScriptsInput;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.csstudio.ui.util.CustomMediaFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.yamcs.studio.script.FileUtil;

/**
 * The dialog for scripts input editing.
 */
public class ScriptsInputDialog extends TrayDialog {

    private Action addAction;
    private Action editAction;
    private Action removeAction;
    private Action moveUpAction;
    private Action moveDownAction;
    private Action convertToEmbedAction;

    private TableViewer scriptsViewer;
    private PVTupleTableEditor pvsEditor;
    private Button checkConnectivityButton;
    private Button stopExecuteOnErrorButton;

    private List<ScriptData> scriptDataList;
    private String title;

    private IPath startPath;

    private AbstractWidgetModel widgetModel;

    public ScriptsInputDialog(Shell parentShell, ScriptsInput scriptsInput, String dialogTitle,
            AbstractWidgetModel widgetModel) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        scriptDataList = scriptsInput.getCopy().getScriptList();
        title = dialogTitle;
        this.widgetModel = widgetModel;
        startPath = widgetModel.getRootDisplayModel().getOpiFilePath().removeLastSegments(1);
    }

    @Override
    protected void okPressed() {
        pvsEditor.forceFocus();
        for (var scriptData : scriptDataList) {
            var hasTrigger = false;
            for (var pvTuple : scriptData.getPVList()) {
                hasTrigger |= pvTuple.trigger;
            }
            if (!hasTrigger) {
                MessageDialog.openWarning(getShell(), "Warning",
                        NLS.bind("At least one trigger PV must be selected for the script:\n{0}",
                                scriptData.getPath().toString()));
                return;
            }
        }
        super.okPressed();
    }

    public final List<ScriptData> getScriptDataList() {
        return scriptDataList;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        if (title != null) {
            shell.setText(title);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        var parentComposite = (Composite) super.createDialogArea(parent);

        var topComposite = new Composite(parentComposite, SWT.NONE);
        var gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.horizontalSpacing = 0;
        topComposite.setLayout(gl);

        var gd = new GridData(GridData.FILL_BOTH);
        topComposite.setLayoutData(gd);

        var toolbarManager = new ToolBarManager(SWT.FLAT);
        var toolBar = toolbarManager.createControl(topComposite);
        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.BEGINNING;
        toolBar.setLayoutData(gd);
        createActions();
        toolbarManager.add(addAction);
        toolbarManager.add(editAction);
        toolbarManager.add(removeAction);
        toolbarManager.add(moveUpAction);
        toolbarManager.add(moveDownAction);
        toolbarManager.add(convertToEmbedAction);

        toolbarManager.update(true);

        var mainComposite = new Composite(topComposite, SWT.NONE);
        gl = new GridLayout(2, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        mainComposite.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.heightHint = 250;
        mainComposite.setLayoutData(gd);

        var leftComposite = new Composite(mainComposite, SWT.NONE);
        gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        leftComposite.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = 350;
        leftComposite.setLayoutData(gd);

        scriptsViewer = createScriptsTableViewer(leftComposite);
        scriptsViewer.setInput(scriptDataList);

        var rightComposite = new Composite(mainComposite, SWT.NONE);
        gl = new GridLayout(1, false);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        rightComposite.setLayout(gl);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.minimumWidth = 250; // Account for the StringTableEditor's minimum size
        rightComposite.setLayoutData(gd);

        var tabFolder = new TabFolder(rightComposite, SWT.NONE);
        tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        var pvTab = new TabItem(tabFolder, SWT.NONE);
        pvTab.setText("Input PVs");
        pvsEditor = new PVTupleTableEditor(tabFolder, new ArrayList<PVTuple>(), SWT.NONE);
        pvsEditor.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        pvsEditor.setEnabled(false);
        pvTab.setControl(pvsEditor);

        var optionTab = new TabItem(tabFolder, SWT.NONE);
        optionTab.setText("Options");
        var optionTabComposite = new Composite(tabFolder, SWT.None);
        optionTabComposite.setLayout(new GridLayout(1, false));

        checkConnectivityButton = new Button(optionTabComposite, SWT.CHECK);
        checkConnectivityButton.setSelection(false);
        checkConnectivityButton.setText("Run even if some PVs\nare disconnected");
        checkConnectivityButton.setToolTipText("Useful when you want to handle PV disconnection inside the script");
        checkConnectivityButton.setEnabled(false);
        checkConnectivityButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                var selection = (IStructuredSelection) scriptsViewer.getSelection();
                if (!selection.isEmpty()) {
                    ((ScriptData) selection.getFirstElement())
                            .setCheckConnectivity(!checkConnectivityButton.getSelection());
                }
                if (checkConnectivityButton.getSelection()) {
                    MessageDialog.openWarning(getShell(), "Warning",
                            "If this option is checked, "
                                    + "the script is responsible for checking PV connectivity. For example using:\n"
                                    + "pvs[#].isConnected()");
                }
            }
        });

        stopExecuteOnErrorButton = new Button(optionTabComposite, SWT.CHECK);
        stopExecuteOnErrorButton.setSelection(false);
        stopExecuteOnErrorButton.setText("Stop on error");
        stopExecuteOnErrorButton
                .setToolTipText("If enabled and an error was detected, the script will no longer trigger");
        stopExecuteOnErrorButton.setEnabled(false);
        stopExecuteOnErrorButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                var selection = (IStructuredSelection) scriptsViewer.getSelection();
                if (!selection.isEmpty()) {
                    ((ScriptData) selection.getFirstElement())
                            .setStopExecuteOnError(stopExecuteOnErrorButton.getSelection());
                }
            }
        });

        if (scriptDataList.size() > 0) {
            setScriptsViewerSelection(scriptDataList.get(0));
            checkConnectivityButton.setSelection(!scriptDataList.get(0).isCheckConnectivity());
            stopExecuteOnErrorButton.setSelection(scriptDataList.get(0).isStopExecuteOnError());
        }

        optionTab.setControl(optionTabComposite);

        return parentComposite;
    }

    /**
     * Refreshes the enabled-state of the actions.
     */
    private void refreshGUIOnSelection() {

        var selection = (IStructuredSelection) scriptsViewer.getSelection();
        if (!selection.isEmpty() && selection.getFirstElement() instanceof ScriptData) {
            removeAction.setEnabled(true);
            moveUpAction.setEnabled(true);
            moveDownAction.setEnabled(true);
            convertToEmbedAction.setEnabled(!((ScriptData) selection.getFirstElement()).isEmbedded());

            editAction.setEnabled(true);
            pvsEditor.updateInput(((ScriptData) selection.getFirstElement()).getPVList());
            pvsEditor.setEnabled(true);
            checkConnectivityButton.setSelection(!((ScriptData) selection.getFirstElement()).isCheckConnectivity());
            checkConnectivityButton.setEnabled(true);
            stopExecuteOnErrorButton.setSelection(((ScriptData) selection.getFirstElement()).isStopExecuteOnError());
            stopExecuteOnErrorButton.setEnabled(true);
        } else {
            removeAction.setEnabled(false);
            moveUpAction.setEnabled(false);
            moveDownAction.setEnabled(false);
            convertToEmbedAction.setEnabled(false);
            pvsEditor.setEnabled(false);
            editAction.setEnabled(false);
            checkConnectivityButton.setEnabled(false);
            stopExecuteOnErrorButton.setEnabled(false);
        }
    }

    private void setScriptsViewerSelection(ScriptData scriptData) {
        scriptsViewer.refresh();
        if (scriptData == null) {
            scriptsViewer.setSelection(StructuredSelection.EMPTY);
        } else {
            scriptsViewer.setSelection(new StructuredSelection(scriptData));
        }
    }

    private TableViewer createScriptsTableViewer(Composite parent) {
        var viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER | SWT.SINGLE);
        viewer.setContentProvider(new BaseWorkbenchContentProvider() {
            @SuppressWarnings("unchecked")
            @Override
            public Object[] getElements(Object element) {
                return (((List<ScriptData>) element).toArray());
            }
        });
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.addSelectionChangedListener(event -> refreshGUIOnSelection());
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return viewer;
    }

    private void createActions() {
        addAction = new Action("Add") {
            @Override
            public void run() {
                var scriptChoiceDialog = new ScriptChoiceDialog(getShell());
                if (scriptChoiceDialog.open() == Window.CANCEL) {
                    return;
                }
                ScriptData scriptData = null;
                if (scriptChoiceDialog.isEmbedded()) {
                    var scriptEditDialog = new EmbeddedScriptEditDialog(getShell(), null);
                    if (scriptEditDialog.open() == Window.OK) {
                        scriptData = scriptEditDialog.getResult();
                    }
                } else {
                    IPath path;
                    var rsd = new RelativePathSelectionDialog(Display.getCurrent().getActiveShell(), startPath,
                            "Select a script file", new String[] { ScriptService.JS, ScriptService.PY });
                    rsd.setSelectedResource("./");
                    if (rsd.open() == Window.OK) {
                        if (rsd.getSelectedResource() != null) {
                            path = Path.fromPortableString(rsd.getSelectedResource());
                            scriptData = new ScriptData(path);
                        }
                    }
                }
                if (scriptData != null) {
                    scriptDataList.add(scriptData);
                    setScriptsViewerSelection(scriptData);
                }
            }
        };
        addAction.setToolTipText("Add a script");
        addAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/add.gif"));

        editAction = new Action("Edit") {
            @Override
            public void run() {
                IPath path;
                var selection = (IStructuredSelection) scriptsViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof ScriptData) {
                    var sd = (ScriptData) selection.getFirstElement();
                    if (sd.isEmbedded()) {
                        var scriptEditDialog = new EmbeddedScriptEditDialog(getShell(), sd);
                        if (scriptEditDialog.open() == Window.OK) {
                            var newSd = scriptEditDialog.getResult();
                            sd.setScriptName(newSd.getScriptName());
                            sd.setScriptType(newSd.getScriptType());
                            sd.setScriptText(newSd.getScriptText());
                            setScriptsViewerSelection(sd);
                        }
                    } else {
                        var rsd = new RelativePathSelectionDialog(getShell(), startPath, "Select a script file",
                                new String[] { ScriptService.JS, ScriptService.PY });
                        rsd.setSelectedResource(
                                ((ScriptData) selection.getFirstElement()).getPath().toPortableString());
                        if (rsd.open() == Window.OK) {
                            if (rsd.getSelectedResource() != null) {
                                path = Path.fromPortableString(rsd.getSelectedResource());
                                sd.setPath(path);
                                setScriptsViewerSelection(sd);
                            }
                        }
                    }

                }

            }
        };
        editAction.setToolTipText("Edit/Change script path");
        editAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/edit.gif"));
        editAction.setEnabled(false);
        removeAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) scriptsViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof ScriptData) {
                    scriptDataList.remove(selection.getFirstElement());
                    setScriptsViewerSelection(null);
                    setEnabled(false);
                }
            }
        };
        removeAction.setText("Remove Script");
        removeAction.setToolTipText("Remove the selected script from the list");
        removeAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/delete.gif"));
        removeAction.setEnabled(false);

        moveUpAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) scriptsViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof ScriptData) {
                    var scriptData = (ScriptData) selection.getFirstElement();
                    var i = scriptDataList.indexOf(scriptData);
                    if (i > 0) {
                        scriptDataList.remove(scriptData);
                        scriptDataList.add(i - 1, scriptData);
                        setScriptsViewerSelection(scriptData);
                    }
                }
            }
        };
        moveUpAction.setText("Move Script Up");
        moveUpAction.setToolTipText("Move selected script up");
        moveUpAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_prev.gif"));
        moveUpAction.setEnabled(false);

        moveDownAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) scriptsViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof ScriptData) {
                    var scriptData = (ScriptData) selection.getFirstElement();
                    var i = scriptDataList.indexOf(scriptData);
                    if (i < scriptDataList.size() - 1) {
                        scriptDataList.remove(scriptData);
                        scriptDataList.add(i + 1, scriptData);
                        setScriptsViewerSelection(scriptData);
                    }
                }
            }
        };
        moveDownAction.setText("Move Script Down");
        moveDownAction.setToolTipText("Move selected script down");
        moveDownAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/search_next.gif"));
        moveDownAction.setEnabled(false);

        convertToEmbedAction = new Action() {
            @Override
            public void run() {
                var selection = (IStructuredSelection) scriptsViewer.getSelection();
                if (!selection.isEmpty() && selection.getFirstElement() instanceof ScriptData) {
                    var sd = (ScriptData) selection.getFirstElement();
                    if (!sd.isEmbedded()) {
                        var absoluteScriptPath = sd.getPath();
                        if (!absoluteScriptPath.isAbsolute()) {
                            absoluteScriptPath = ResourceUtil.buildAbsolutePath(widgetModel, absoluteScriptPath);
                        }

                        try {
                            var text = FileUtil.readTextFile(absoluteScriptPath.toString());
                            var ext = absoluteScriptPath.getFileExtension().trim().toLowerCase();
                            if (ext.equals(ScriptService.JS)) {
                                sd.setScriptType(ScriptType.JAVASCRIPT);
                            } else if (ext.equals(ScriptService.PY)) {
                                sd.setScriptType(ScriptType.PYTHON);
                            } else {
                                MessageDialog.openError(getShell(), "Failed", "The script type is not recognized.");
                                return;
                            }
                            sd.setEmbedded(true);
                            sd.setScriptText(text);
                            sd.setScriptName(absoluteScriptPath.removeFileExtension().lastSegment());
                            setScriptsViewerSelection(sd);
                        } catch (Exception e) {
                            MessageDialog.openError(getShell(), "Failed", "Failed to read script file");
                        }

                    }

                }
            }
        };
        convertToEmbedAction.setText("Convert to Embedded Script");
        convertToEmbedAction.setToolTipText("Convert to Embedded Script");
        convertToEmbedAction.setImageDescriptor(CustomMediaFactory.getInstance()
                .getImageDescriptorFromPlugin(OPIBuilderPlugin.PLUGIN_ID, "icons/convertToEmbedded.png"));
        convertToEmbedAction.setEnabled(false);
    }
}

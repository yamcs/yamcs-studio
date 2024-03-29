/********************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.ui.util.dialogs;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.yamcs.studio.core.YamcsPlugin;

/**
 * A dialog to display one or more errors to the user, as contained in an <code>IStatus</code> object. If an error
 * contains additional detailed information then a Details button is automatically supplied, which shows or hides an
 * error details viewer when pressed by the user.
 * <p>
 * Taken from http://rubenlaguna.com/wp/2007/07/25/eclipse-error-reporting-exception-stacktrace-details/ and modified.
 */
public class ExceptionDetailsErrorDialog extends IconAndMessageDialog {
    /**
     * Static to prevent opening of error dialogs for automated testing.
     */
    public static boolean AUTOMATED_MODE = false;

    /**
     * Reserve room for this many list items.
     */
    private static final int LIST_ITEM_COUNT = 7;

    /**
     * The nesting indent.
     */
    private static final String NESTING_INDENT = " ";

    /**
     * The Details button.
     */
    private Button detailsButton;

    /**
     * The title of the dialog.
     */
    private String title;

    /**
     * The SWT text control that displays the error details.
     */
    private Text text;

    /**
     * Indicates whether the error details viewer is currently created.
     */
    private boolean listCreated = false;

    /**
     * Filter mask for determining which status items to display.
     */
    private int displayMask = 0xFFFF;

    /**
     * The main status object.
     */
    private IStatus status;

    /**
     * The current clipboard. To be disposed when closing the dialog.
     */
    private Clipboard clipboard;

    private boolean shouldIncludeTopLevelErrorInDetails = false;

    /**
     * Creates an error dialog. Note that the dialog will have no visual representation (no widgets) until it is told to
     * open.
     * <p>
     * Normally one should use <code>openError</code> to create and open one of these. This constructor is useful only
     * if the error object being displayed contains child items <em>and </em> you need to specify a mask which will be
     * used to filter the displaying of these children.
     * </p>
     *
     * @param parentShell
     *            the shell under which to create this dialog
     * @param dialogTitle
     *            the title to use for this dialog, or <code>null</code> to indicate that the default title should be
     *            used
     * @param message
     *            the message to show in this dialog, or <code>null</code> to indicate that the error's message should
     *            be shown as the primary message
     * @param status
     *            the error to show to the user
     * @param displayMask
     *            the mask to use to filter the displaying of child items, as per <code>IStatus.matches</code>
     */
    public ExceptionDetailsErrorDialog(Shell parentShell, String dialogTitle, String message, IStatus status,
            int displayMask) {
        super(parentShell);
        title = dialogTitle == null ? JFaceResources.getString("Problem_Occurred") : dialogTitle;
        this.message = message == null ? status.getMessage()
                : JFaceResources.format("Reason", new Object[] { message, status.getMessage() });
        this.status = status;
        this.displayMask = displayMask;
    }

    /** Allow resize */
    @Override
    protected boolean isResizable() {
        return true;
    }

    /*
     * Handles the pressing of the Ok
     * or Details button in this dialog. If the Ok button was pressed then close
     * this dialog. If the Details button was pressed then toggle the displaying
     * of the error details area. Note that the Details button will only be
     * visible if the error being displayed specifies child details.
     */
    @Override
    protected void buttonPressed(int id) {
        if (id == IDialogConstants.DETAILS_ID) {
            // was the details button pressed?
            toggleDetailsArea();
        } else {
            super.buttonPressed(id);
        }
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // create OK and Details buttons
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createDetailsButton(parent);
    }

    /**
     * Create the details button if it should be included.
     */
    protected void createDetailsButton(Composite parent) {
        if (shouldShowDetailsButton()) {
            detailsButton = createButton(parent, IDialogConstants.DETAILS_ID, IDialogConstants.SHOW_DETAILS_LABEL,
                    false);
        }
    }

    /**
     * This implementation of the <code>Dialog</code> framework method creates and lays out a composite and calls
     * <code>createMessageArea</code> and <code>createCustomArea</code> to populate it. Subclasses should override
     * <code>createCustomArea</code> to add contents below the message.
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        createMessageArea(parent);
        // create a composite with standard margins and spacing
        var composite = new Composite(parent, SWT.NONE);
        var layout = new GridLayout();
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        layout.numColumns = 2;
        composite.setLayout(layout);
        var childData = new GridData(GridData.FILL_BOTH);
        childData.horizontalSpan = 2;
        composite.setLayoutData(childData);
        composite.setFont(parent.getFont());
        return composite;
    }

    @Override
    protected void createDialogAndButtonArea(Composite parent) {
        super.createDialogAndButtonArea(parent);
        if (dialogArea instanceof Composite) {
            // Create a label if there are no children to force a smaller layout
            var dialogComposite = (Composite) dialogArea;
            if (dialogComposite.getChildren().length == 0) {
                new Label(dialogComposite, SWT.NULL);
            }
        }
    }

    @Override
    protected Image getImage() {
        if (status != null) {
            if (status.getSeverity() == IStatus.WARNING) {
                return getWarningImage();
            }
            if (status.getSeverity() == IStatus.INFO) {
                return getInfoImage();
            }
        }
        // If it was not a warning or an error then return the error image
        return getErrorImage();
    }

    /**
     * Create this dialog's drop-down list component.
     *
     * @param parent
     *            the parent composite
     * @return the drop-down list component
     */
    protected Text createDropDownList(Composite parent) {
        // create the list
        text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        // fill the list
        populateList(text);
        var data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL
                | GridData.GRAB_VERTICAL);
        data.heightHint = text.getLineHeight() * LIST_ITEM_COUNT;
        data.horizontalSpan = 2;
        text.setEditable(false);
        text.setLayoutData(data);
        text.setFont(parent.getFont());
        var copyMenu = new Menu(text);
        var copyItem = new MenuItem(copyMenu, SWT.NONE);
        copyItem.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                copyToClipboard();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                copyToClipboard();
            }
        });
        copyItem.setText(JFaceResources.getString("copy"));
        text.setMenu(copyMenu);
        text.setSelection(0);
        listCreated = true;
        return text;
    }

    /**
     * Extends <code>Window.open()</code>. Opens an error dialog to display the error. If you specified a mask to filter
     * the displaying of these children, the error dialog will only be displayed if there is at least one child status
     * matching the mask.
     */
    @Override
    public int open() {
        if (!AUTOMATED_MODE && shouldDisplay(status, displayMask)) {
            return super.open();
        }
        setReturnCode(OK);
        return OK;
    }

    /**
     * Opens an error dialog to display the given error. Use this method if the error object being displayed does not
     * contain child items, or if you wish to display all such items without filtering.
     *
     * @param parent
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param dialogTitle
     *            the title to use for this dialog, or <code>null</code> to indicate that the default title should be
     *            used
     * @param message
     *            the message to show in this dialog, or <code>null</code> to indicate that the error's message should
     *            be shown as the primary message
     * @param status
     *            the error to show to the user
     * @return the code of the button that was pressed that resulted in this dialog closing. This will be
     *         <code>Dialog.OK</code> if the OK button was pressed, or <code>Dialog.CANCEL</code> if this dialog's close
     *         window decoration or the ESC key was used.
     */
    public static int openError(Shell parent, String dialogTitle, String message, IStatus status) {
        return openError(parent, dialogTitle, message, status,
                IStatus.OK | IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
    }

    /**
     * Opens an error dialog to display the given error. Use this method if the error object being displayed contains
     * child items <em>and</em> you wish to specify a mask which will be used to filter the displaying of these
     * children. The error dialog will only be displayed if there is at least one child status matching the mask.
     *
     * @param parentShell
     *            the parent shell of the dialog, or <code>null</code> if none
     * @param title
     *            the title to use for this dialog, or <code>null</code> to indicate that the default title should be
     *            used
     * @param message
     *            the message to show in this dialog, or <code>null</code> to indicate that the error's message should
     *            be shown as the primary message
     * @param status
     *            the error to show to the user
     * @param displayMask
     *            the mask to use to filter the displaying of child items, as per <code>IStatus.matches</code>
     * @return the code of the button that was pressed that resulted in this dialog closing. This will be
     *         <code>Dialog.OK</code> if the OK button was pressed, or <code>Dialog.CANCEL</code> if this dialog's close
     *         window decoration or the ESC key was used.
     */
    public static int openError(Shell parentShell, String title, String message, IStatus status, int displayMask) {
        var dialog = new ExceptionDetailsErrorDialog(parentShell, title, message, status, displayMask);
        return dialog.open();
    }

    /**
     * Populates the list using this error dialog's status object. This walks the child static of the status object and
     * displays them in a list. The format for each entry is status_path : status_message If the status's path was null
     * then it (and the colon) are omitted.
     *
     * @param listToPopulate
     *            The list to fill.
     */
    private void populateList(Text listToPopulate) {
        populateList(listToPopulate, status, 0, shouldIncludeTopLevelErrorInDetails);
    }

    /**
     * Populate the list with the messages from the given status. Traverse the children of the status deeply and also
     * traverse CoreExceptions that appear in the status.
     *
     * @param listToPopulate
     *            the list to populate
     * @param buildingStatus
     *            the status being displayed
     * @param nesting
     *            the nesting level (increases one level for each level of children)
     * @param includeStatus
     *            whether to include the buildingStatus in the display or just its children
     */
    private void populateList(Text listToPopulate, IStatus buildingStatus, int nesting, boolean includeStatus) {

        if (!buildingStatus.matches(displayMask)) {
            return;
        }

        var t = buildingStatus.getException();
        var isCoreException = t instanceof CoreException;
        var incrementNesting = false;

        if (includeStatus) {
            var sb = new StringBuffer();
            for (var i = 0; i < nesting; i++) {
                sb.append(NESTING_INDENT);
            }
            var message = buildingStatus.getMessage();
            sb.append(message);
            listToPopulate.append(sb.toString());
            incrementNesting = true;
        }

        if (!isCoreException && t != null) {
            // Include low-level exception message
            var sb = new StringBuffer();
            for (var i = 0; i < nesting; i++) {
                sb.append(NESTING_INDENT);
            }

            var sw = new StringWriter();
            var pw = new PrintWriter(sw);
            t.printStackTrace(pw);

            var message = sw.getBuffer().toString();
            if (message == null) {
                message = t.toString();
            }

            sb.append(message);
            listToPopulate.append(sb.toString());
            incrementNesting = true;
        }

        if (incrementNesting) {
            nesting++;
        }

        // Look for a nested core exception
        if (isCoreException) {
            var ce = (CoreException) t;
            var eStatus = ce.getStatus();
            // Only print the exception message if it is not contained in the
            // parent message
            if (message == null || message.indexOf(eStatus.getMessage()) == -1) {
                populateList(listToPopulate, eStatus, nesting, true);
            }
        }

        // Look for child status
        var children = buildingStatus.getChildren();
        for (var i = 0; i < children.length; i++) {
            populateList(listToPopulate, children[i], nesting, true);
        }
    }

    /**
     * Returns whether the given status object should be displayed.
     *
     * @param status
     *            a status object
     * @param mask
     *            a mask as per <code>IStatus.matches</code>
     * @return <code>true</code> if the given status should be displayed, and <code>false</code> otherwise
     */
    protected static boolean shouldDisplay(IStatus status, int mask) {
        var children = status.getChildren();
        if (children == null || children.length == 0) {
            return status.matches(mask);
        }
        for (var i = 0; i < children.length; i++) {
            if (children[i].matches(mask)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Toggles the unfolding of the details area. This is triggered by the user pressing the details button.
     */
    private void toggleDetailsArea() {
        var windowSize = getShell().getSize();
        var oldSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (listCreated) {
            text.dispose();
            listCreated = false;
            detailsButton.setText(IDialogConstants.SHOW_DETAILS_LABEL);
        } else {
            text = createDropDownList((Composite) getContents());
            detailsButton.setText(IDialogConstants.HIDE_DETAILS_LABEL);
        }
        var newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
        // newSize = new Point(newSize.x, Math.min(newSize.y, 500));
        getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
    }

    /**
     * Put the details of the status of the error onto the stream.
     *
     * @param buildingStatus
     * @param buffer
     * @param nesting
     */
    private void populateCopyBuffer(IStatus buildingStatus, StringBuffer buffer, int nesting) {
        if (!buildingStatus.matches(displayMask)) {
            return;
        }
        for (var i = 0; i < nesting; i++) {
            buffer.append(NESTING_INDENT);
        }
        buffer.append(buildingStatus.getMessage());
        buffer.append("\n");

        // Look for a nested core exception
        var t = buildingStatus.getException();
        if (t instanceof CoreException) {
            var ce = (CoreException) t;
            populateCopyBuffer(ce.getStatus(), buffer, nesting + 1);
        }

        var children = buildingStatus.getChildren();
        for (var i = 0; i < children.length; i++) {
            populateCopyBuffer(children[i], buffer, nesting + 1);
        }
    }

    /**
     * Copy the contents of the statuses to the clipboard.
     */
    private void copyToClipboard() {
        if (clipboard != null) {
            clipboard.dispose();
        }
        var statusBuffer = new StringBuffer();
        // populateCopyBuffer(status, statusBuffer, 0);
        statusBuffer.append(text.getText());
        clipboard = new Clipboard(text.getDisplay());
        clipboard.setContents(new Object[] { statusBuffer.toString() }, new Transfer[] { TextTransfer.getInstance() });
    }

    @Override
    public boolean close() {
        if (clipboard != null) {
            clipboard.dispose();
        }
        return super.close();
    }

    /**
     * Show the details portion of the dialog if it is not already visible. This method will only work when it is
     * invoked after the control of the dialog has been set. In other words, after the <code>createContents</code>
     * method has been invoked and has returned the control for the content area of the dialog. Invoking the method
     * before the content area has been set or after the dialog has been disposed will have no effect.
     */
    protected void showDetailsArea() {
        if (!listCreated) {
            var control = getContents();
            if (control != null && !control.isDisposed()) {
                toggleDetailsArea();
            }
        }
    }

    /**
     * Return whether the Details button should be included. This method is invoked once when the dialog is built. By
     * default, the Details button is only included if the status used when creating the dialog was a multi-status or if
     * the status contains an exception. Subclasses may override.
     */
    protected boolean shouldShowDetailsButton() {
        return status.isMultiStatus() || status.getException() != null;
    }

    /**
     * Set the status displayed by this error dialog to the given status. This only affects the status displayed by the
     * Details list. The message, image and title should be updated by the subclass, if desired.
     *
     * @param status
     *            the status to be displayed in the details list
     */
    protected void setStatus(IStatus status) {
        if (this.status != status) {
            this.status = status;
        }
        shouldIncludeTopLevelErrorInDetails = true;
        if (listCreated) {
            repopulateList();
        }
    }

    /**
     * Repopulate the supplied list widget.
     */
    private void repopulateList() {
        if (text != null && !text.isDisposed()) {
            text.setText("");
            populateList(text);
        }
    }

    public static int openError(Shell shell, String title, Exception ex) {
        IStatus status = new Status(IStatus.ERROR, YamcsPlugin.PLUGIN_ID, ex.getLocalizedMessage(), ex);
        return ExceptionDetailsErrorDialog.openError(shell, title, null, status);
    }

    public static int openError(Shell shell, String title, String message, Exception ex) {
        IStatus status = new Status(IStatus.ERROR, YamcsPlugin.PLUGIN_ID, message, ex);
        return ExceptionDetailsErrorDialog.openError(shell, title, null, status);
    }
}

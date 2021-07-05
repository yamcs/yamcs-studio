/*
 * Copyright (c) 2007 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */

package org.csstudio.ui.util.dialogs;

import org.csstudio.ui.util.composites.ResourceSelectionGroup;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * This class represents a Dialog to choose a file in the workspace.
 *
 * @author Kai Meyer, Joerg Rathlev
 */
//TODO: Copied from org.csstudio.platform.ui. Review is needed.
public final class ResourceSelectionDialog extends Dialog implements Listener {
    /**
     * The message to display, or <code>null</code> if none.
     */
    private String _message;

    /**
     * The {@link WorkspaceTreeComposite} for this dialog.
     */
    private ResourceSelectionGroup _resourceSelectionGroup;

    /**
     * The file extensions of files that will be shown for selection.
     */
    private String[] _fileExtensions;

    /**
     * The path of the selected resource.
     */
    private IPath _path;

    private Text _resourcePathText;

    /**
     * Creates an input dialog with OK and Cancel buttons. Note that the dialog
     * will have no visual representation (no widgets) until it is told to open.
     * <p>
     * Note that the <code>open</code> method blocks for input dialogs.
     * </p>
     *
     * @param parentShell
     *            the parent shell, or <code>null</code> to create a top-level
     *            shell
     * @param dialogMessage
     *            the dialog message, or <code>null</code> if none
     * @param fileExtensions
     *            the file extensions of files to show in the dialog. Use an
     *            empty array or <code>null</code> to show only containers
     *            (folders).
     */
    public ResourceSelectionDialog(final Shell parentShell,
            final String dialogMessage, final String[] fileExtensions) {
        super(parentShell);
        this.setShellStyle(SWT.MODELESS | SWT.CLOSE | SWT.MAX | SWT.TITLE
                | SWT.BORDER | SWT.RESIZE);
        _message = dialogMessage;
        _fileExtensions = fileExtensions;
    }

    /**
     * Sets the initially selected resource. Must be called before the dialog is
     * displayed.
     *
     * @param path
     *            the path to the initially selected resource.
     */
    public void setSelectedResource(final IPath path) {
        _path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureShell(final Shell shell) {
        super.configureShell(shell);
        shell.setText("Resources");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));
        if (_message != null) {
            Label label = new Label(composite, SWT.WRAP);
            label.setText(_message);
            GridData data = new GridData(GridData.GRAB_HORIZONTAL
                    | GridData.HORIZONTAL_ALIGN_FILL
                    | GridData.VERTICAL_ALIGN_CENTER);
            data.horizontalSpan = 2;
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            label.setLayoutData(data);
        }

        // The New Project and New Folder actions will be shown if there are
        // no file extensions, i.e. if the dialog is opened to select a folder.
        boolean showNewContainerActions = (_fileExtensions == null
                || _fileExtensions.length == 0);

        _resourceSelectionGroup = new ResourceSelectionGroup(composite, this,
                _fileExtensions, showNewContainerActions);
        new Label(composite, SWT.NONE).setText("Resource Path:");
        _resourcePathText = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
        _resourcePathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        if (_path != null && !_path.isEmpty()) {
            _resourcePathText.setText(_path.toString());
            _resourceSelectionGroup.setSelectedResource(_path);
        }
        return composite;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void okPressed() {
//      _path = _resourceSelectionGroup.getFullPath();
        _path = new Path(_resourcePathText.getText());

        super.okPressed();
    }

    /**
     * Returns the path to the selected resource.
     *
     * @return the path to the selected resource, or <code>null</code> if no
     *         resource was selected.
     */
    public IPath getSelectedResource() {
        return _path;
    }

    public void handleEvent(Event event) {
       if(event != null){
           ResourceSelectionGroup widget = (ResourceSelectionGroup) event.widget;
           if(widget != null &&  widget.getFullPath() != null ){
               _resourcePathText.setText(widget.getFullPath().toString());
           }
       }
       if(SWT.MouseDoubleClick == event.type ){
            okPressed();
       }
    }
}

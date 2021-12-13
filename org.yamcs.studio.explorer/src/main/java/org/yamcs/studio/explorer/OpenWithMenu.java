/*******************************************************************************
 * Copyright (c) 2021 Space Applications Services and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.yamcs.studio.explorer;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.EditorSelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.dialogs.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.part.FileEditorInput;

/**
 * A menu for opening files in the workbench.
 * <p>
 * An <code>OpenWithMenu is used to populate a menu with "Open With" actions. One action is added for each editor which
 * is applicable to the selected file. If the user selects one of these items, the corresponding editor is opened on the
 * file.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
@SuppressWarnings("restriction")
public class OpenWithMenu extends ContributionItem {
    private IWorkbenchPage page;

    private IAdaptable file;

    private IEditorRegistry registry = PlatformUI.getWorkbench().getEditorRegistry();

    private static Hashtable<ImageDescriptor, Image> imageCache = new Hashtable<>(11);

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID + ".OpenWithMenu";

    /**
     * Match both the input and id, so that different types of editor can be opened on the same input.
     */
    private static final int MATCH_BOTH = IWorkbenchPage.MATCH_INPUT | IWorkbenchPage.MATCH_ID;

    private static final Comparator<IEditorDescriptor> comparer = new Comparator<>() {
        private Collator collator = Collator.getInstance();

        @Override
        public int compare(IEditorDescriptor arg0, IEditorDescriptor arg1) {
            return collator.compare(arg0.getLabel(), arg1.getLabel());
        }
    };

    /**
     * Constructs a new instance of <code>OpenWithMenu.
     *
     * @param page
     *            the page where the editor is opened if an item within the menu is selected
     * @param file
     *            the selected file
     */
    public OpenWithMenu(IWorkbenchPage page, IAdaptable file) {
        super(ID);
        this.page = page;
        this.file = file;
    }

    /**
     * Returns an image to show for the corresponding editor descriptor.
     *
     * @param editorDesc
     *            the editor descriptor, or null for the system editor
     * @return the image or null
     */
    private Image getImage(IEditorDescriptor editorDesc) {
        var imageDesc = getImageDescriptor(editorDesc);
        if (imageDesc == null) {
            return null;
        }
        var image = imageCache.get(imageDesc);
        if (image == null) {
            image = imageDesc.createImage();
            imageCache.put(imageDesc, image);
        }
        return image;
    }

    /**
     * Returns the image descriptor for the given editor descriptor, or null if it has no image.
     */
    private ImageDescriptor getImageDescriptor(IEditorDescriptor editorDesc) {
        ImageDescriptor imageDesc = null;
        if (editorDesc == null) {
            imageDesc = registry.getImageDescriptor(getFileResource().getName());
            // TODO: is this case valid, and if so, what are the implications for content-type editor bindings?
        } else {
            imageDesc = editorDesc.getImageDescriptor();
        }
        if (imageDesc == null) {
            if (editorDesc.getId().equals(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID)) {
                imageDesc = registry.getSystemExternalEditorImageDescriptor(getFileResource().getName());
            }
        }
        return imageDesc;
    }

    /**
     * Creates the menu item for the editor descriptor.
     *
     * @param menu
     *            the menu to add the item to
     * @param descriptor
     *            the editor descriptor, or null for the system editor
     * @param preferredEditor
     *            the descriptor of the preferred editor, or <code>null
     */
    private void createMenuItem(Menu menu, IEditorDescriptor descriptor, IEditorDescriptor preferredEditor) {
        var menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText(descriptor.getLabel());
        var image = getImage(descriptor);
        if (image != null) {
            menuItem.setImage(image);
        }
        menuItem.addListener(SWT.Selection, evt -> {
            openEditor(descriptor, false);
        });
    }

    /**
     * Creates the Other... menu item
     *
     * @param menu
     *            the menu to add the item to
     */
    private void createOtherMenuItem(Menu menu) {
        var fileResource = getFileResource();
        if (fileResource == null) {
            return;
        }
        new MenuItem(menu, SWT.SEPARATOR);
        var menuItem = new MenuItem(menu, SWT.PUSH);
        menuItem.setText("Other...");
        menuItem.addListener(SWT.Selection, evt -> {
            var dialog = new EditorSelectionDialog(menu.getShell());
            dialog.setMessage("Choose the editor for opening " + fileResource.getName());
            if (dialog.open() == Window.OK) {
                var editor = dialog.getSelectedEditor();
                if (editor != null) {
                    openEditor(editor, editor.isOpenExternal());
                }
            }
        });
    }

    /*
     * Fills the menu with perspective items.
     */
    @Override
    public void fill(Menu menu, int index) {
        var file = getFileResource();
        if (file == null) {
            return;
        }

        var defaultEditor = registry.findEditor(IDEWorkbenchPlugin.DEFAULT_TEXT_EDITOR_ID); // may be null
        var preferredEditor = IDE.getDefaultEditor(file); // may be null

        var editors = registry.getEditors(file.getName(), IDE.getContentType(file));
        Collections.sort(Arrays.asList(editors), comparer);

        var defaultFound = false;

        // Check that we don't add it twice. This is possible
        // if the same editor goes to two mappings.
        var alreadyMapped = new ArrayList<IEditorDescriptor>();

        for (var i = 0; i < editors.length; i++) {
            var editor = editors[i];
            if (!alreadyMapped.contains(editor)) {
                createMenuItem(menu, editor, preferredEditor);
                if (defaultEditor != null && editor.getId().equals(defaultEditor.getId())) {
                    defaultFound = true;
                }
                alreadyMapped.add(editor);
            }
        }

        // Only add a separator if there is something to separate
        if (editors.length > 0) {
            new MenuItem(menu, SWT.SEPARATOR);
        }

        // Add default editor. Check it if it is saved as the preference.
        if (!defaultFound && defaultEditor != null) {
            createMenuItem(menu, defaultEditor, preferredEditor);
        }

        // add Other... menu item
        createOtherMenuItem(menu);
    }

    /**
     * Converts the IAdaptable file to IFile or null.
     */
    private IFile getFileResource() {
        if (file instanceof IFile) {
            return (IFile) file;
        }
        var resource = file.getAdapter(IResource.class);
        if (resource instanceof IFile) {
            return (IFile) resource;
        }

        return null;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    /**
     * Opens the given editor on the selected file.
     *
     * @param editor
     *            the editor descriptor, or null for the system editor
     * @param openUsingDescriptor
     *            use the descriptor's editor ID for opening if false (normal case), or use the descriptor itself if
     *            true (needed to fix bug 178235).
     */
    private void openEditor(IEditorDescriptor editor, boolean openUsingDescriptor) {
        var file = getFileResource();
        if (file == null) {
            return;
        }
        try {
            if (openUsingDescriptor) {
                ((WorkbenchPage) page).openEditorFromDescriptor(new FileEditorInput(file), editor, true, null);
            } else {
                var editorId = editor == null ? IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID : editor.getId();

                ((WorkbenchPage) page).openEditor(new FileEditorInput(file), editorId, true, MATCH_BOTH);
                // only remember the default editor if the open succeeds
                IDE.setDefaultEditor(file, editorId);
            }
        } catch (PartInitException e) {
            DialogUtil.openError(page.getWorkbenchWindow().getShell(), "Problems Opening Editor", e.getMessage(), e);
        }
    }
}

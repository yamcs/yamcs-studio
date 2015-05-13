package org.yamcs.studio.ui.application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * Copied from nodeclipse, but could not find a license, so assuming public domain, or EPL
 */
@SuppressWarnings({ "restriction" })
public class RelativeFileSystemStructureProvider implements IImportStructureProvider {
    private File root;

    public RelativeFileSystemStructureProvider(File root) {
        this.root = root;
    }

    public RelativeFileSystemStructureProvider(String basepath, String name) {
        this(new File(basepath, name));
    }

    public File getRoot() {
        return root;
    }

    @Override
    public List<File> getChildren(Object element) {
        File folder = (File) element;
        String[] children = folder.list();
        int childrenLength = children == null ? 0 : children.length;
        List<File> result = new ArrayList<File>(childrenLength);

        for (int i = 0; i < childrenLength; i++) {
            result.add(new File(folder, children[i]));
        }

        return result;
    }

    public List<File> collectFiles(Object element) {
        List<File> result = new ArrayList<File>();

        File root = (File) element;
        if (root.isDirectory()) {
            collectFiles(root, result);
        } else {
            result.add(root);
        }

        return result;
    }

    private void collectFiles(File parent, List<File> result) {
        File[] children = parent.listFiles();
        for (File child : children) {
            if (child.isDirectory()) {
                collectFiles(child, result);
            } else {
                result.add(child);
            }
        }
    }

    @Override
    public InputStream getContents(Object element) {
        try {
            return new FileInputStream((File) element);
        } catch (FileNotFoundException e) {
            IDEWorkbenchPlugin.log(e.getLocalizedMessage(), e);
        }
        return null;
    }

    private String stripPath(String path) {
        int index = path.indexOf(root.getName());
        path = path.substring(index + root.getName().length());
        return path;
    }

    @Override
    public String getFullPath(Object element) {
        return stripPath(((File) element).getPath());
    }

    @Override
    public String getLabel(Object element) {
        File file = (File) element;
        String name = file.getName();
        if (name.length() == 0) {
            return file.getPath();
        }
        return name;
    }

    @Override
    public boolean isFolder(Object element) {
        return ((File) element).isDirectory();
    }
}

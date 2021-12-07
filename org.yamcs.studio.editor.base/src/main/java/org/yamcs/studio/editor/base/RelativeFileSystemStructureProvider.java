package org.yamcs.studio.editor.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;

/**
 * Copied from nodeclipse, but could not find a license, so assuming public domain, or EPL
 */

public class RelativeFileSystemStructureProvider implements IImportStructureProvider {

    private static Logger log = Logger.getLogger(RelativeFileSystemStructureProvider.class.getName());

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
        var folder = (File) element;
        var children = folder.list();
        var childrenLength = children == null ? 0 : children.length;
        List<File> result = new ArrayList<>(childrenLength);

        for (var i = 0; i < childrenLength; i++) {
            result.add(new File(folder, children[i]));
        }

        return result;
    }

    public List<File> collectFiles(Object element) {
        List<File> result = new ArrayList<>();

        var root = (File) element;
        if (root.isDirectory()) {
            collectFiles(root, result);
        } else {
            result.add(root);
        }

        return result;
    }

    private void collectFiles(File parent, List<File> result) {
        var children = parent.listFiles();
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
            log.warning("Could not find " + element);
        }
        return null;
    }

    private String stripPath(String path) {
        var index = path.indexOf(root.getName());
        path = path.substring(index + root.getName().length());
        return path;
    }

    @Override
    public String getFullPath(Object element) {
        return stripPath(((File) element).getPath());
    }

    @Override
    public String getLabel(Object element) {
        var file = (File) element;
        var name = file.getName();
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

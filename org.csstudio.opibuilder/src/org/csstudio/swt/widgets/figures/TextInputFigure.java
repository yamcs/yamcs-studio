/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.swt.widgets.figures;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.csstudio.swt.widgets.datadefinition.IManualStringValueChangeListener;
import org.csstudio.swt.widgets.util.DateTimePickerDialog;
import org.csstudio.ui.util.dialogs.ResourceSelectionDialog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Button;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

public class TextInputFigure extends TextFigure {

    private static final String SEPARATOR = "|";

    public enum SelectorType {
        NONE("None", null), FILE("File", openFileImg), DATETIME("Datetime", calendarImg);

        public String description;

        public Image icon;

        SelectorType(String description, Image icon) {
            this.description = description;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return description;
        }

        public static String[] stringValues() {
            var sv = new String[values().length];
            var i = 0;
            for (var p : values()) {
                sv[i++] = p.toString();
            }
            return sv;
        }
    }

    public enum FileSource {
        WORKSPACE("Workspace"), LOCAL("Local File System");

        public String description;

        FileSource(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        public static String[] stringValues() {
            var sv = new String[values().length];
            var i = 0;
            for (var p : values()) {
                sv[i++] = p.toString();
            }
            return sv;
        }
    }

    public enum FileReturnPart {
        FULL_PATH("Full Path"), NAME_EXT("Name & Extension"), NAME_ONLY("Name Only"), DIRECTORY("Directory");

        public String description;

        FileReturnPart(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        public static String[] stringValues() {
            var sv = new String[values().length];
            var i = 0;
            for (var p : values()) {
                sv[i++] = p.toString();
            }
            return sv;
        }
    }

    private static final Image openFileImg = createImage("icons/openFile.png"),
            calendarImg = createImage("icons/calendar.gif");

    private static final int SELECTOR_WIDTH = 25;

    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    private Date dateTime;

    private Button selector;

    private SelectorType selectorType = SelectorType.NONE;
    private FileSource fileSource = FileSource.WORKSPACE;
    private FileReturnPart fileReturnPart = FileReturnPart.FULL_PATH;
    private List<IManualStringValueChangeListener> selectorListeners;

    private String startPath;

    private String currentPath;

    public TextInputFigure() {
        this(false);
    }

    public TextInputFigure(boolean runMode) {
        super(runMode);
        selectorListeners = new ArrayList<>(1);
    }

    @Override
    protected void layout() {
        super.layout();
        if (selector != null && selector.isVisible()) {
            var clientArea = getClientArea();
            selector.setBounds(new Rectangle(clientArea.x + clientArea.width - SELECTOR_WIDTH, clientArea.y,
                    SELECTOR_WIDTH, clientArea.height));
        }
    }

    public void addManualValueChangeListener(IManualStringValueChangeListener listener) {
        if (listener != null) {
            selectorListeners.add(listener);
        }
    }

    /**
     * Inform all slider listeners, that the manual value has changed.
     *
     * @param newManualValue
     *            the new manual value
     */
    public void fireManualValueChange(String newManualValue) {
        for (var l : selectorListeners) {
            l.manualValueChanged(newManualValue);
        }
    }

    /**
     * @return the dateTimeFormat
     */
    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    /**
     * @param dateTimeFormat
     *            the dateTimeFormat to set
     */
    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

    public String getStartPath() {
        return startPath;
    }

    @Override
    protected Rectangle getTextArea() {
        Rectangle textArea;
        if (selector != null && selector.isVisible()) {
            var clientArea = getClientArea();
            textArea = new Rectangle(clientArea.x, clientArea.y, clientArea.width - SELECTOR_WIDTH, clientArea.height);
        } else {
            textArea = getClientArea();
        }
        return textArea;
    }

    /**
     * @param startPath
     *            the startPath to set
     */
    public void setStartPath(String startPath) {
        this.startPath = startPath;
    }

    /**
     * @return the selectorType
     */
    public SelectorType getSelectorType() {
        return selectorType;
    }

    /**
     * @param selectorType
     *            the selectorType to set
     */
    public void setSelectorType(SelectorType selectorType) {
        this.selectorType = selectorType;
        if (selectorType != SelectorType.NONE) {
            if (selector != null) {
                remove(selector);
            }
            selector = new Button(selectorType.icon);
            selectorListeners = new ArrayList<>();
            selector.addActionListener(new SelectorListener());
            add(selector);
        } else {
            if (selector != null) {
                remove(selector);
                selector = null;
            }
        }
    }

    /**
     * @return the fileSource
     */
    public FileSource getFileSource() {
        return fileSource;
    }

    /**
     * @param fileSource
     *            the fileSource to set
     */
    public void setFileSource(FileSource fileSource) {
        this.fileSource = fileSource;
    }

    /**
     * @return the fileReturnPart
     */
    public FileReturnPart getFileReturnPart() {
        return fileReturnPart;
    }

    @Override
    public Dimension getPreferredSize(int wHint, int hHint) {
        var superSize = super.getPreferredSize(wHint, hHint);
        if (getSelectorType() != SelectorType.NONE) {
            if (superSize.height < SELECTOR_WIDTH) {
                superSize.height = SELECTOR_WIDTH;
            }
            superSize.width += SELECTOR_WIDTH + 1;
        }
        return superSize;
    }

    /**
     * @param fileReturnPart
     *            the fileReturnPart to set
     */
    public void setFileReturnPart(FileReturnPart fileReturnPart) {
        this.fileReturnPart = fileReturnPart;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    private static Image createImage(String name) {
        var stream = TextInputFigure.class.getResourceAsStream(name);
        var image = new Image(null, stream);
        try {
            stream.close();
        } catch (IOException ioe) {
        }
        return image;
    }

    private final class SelectorListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            switch (getSelectorType()) {
            case FILE:
                handleTextInputFigureFileSelector();
                break;
            case DATETIME:
                var dialog = new DateTimePickerDialog(Display.getCurrent().getActiveShell());
                if (dateTime != null) {
                    dialog.setDateTime(dateTime);
                }
                if (dialog.open() == Window.OK) {
                    dateTime = dialog.getDateTime();
                    try {
                        setText(new SimpleDateFormat(dateTimeFormat).format(dateTime));
                    } catch (Exception e) {
                        var msg = NLS.bind("Failed to return datetime. The datetime format {0} might be incorrect.\n"
                                + e.getMessage(), dateTimeFormat);
                        MessageDialog.openError(null, "Failed", msg);
                        break;
                    }
                    fireManualValueChange(getText());
                }
                break;
            case NONE:
            default:
                break;
            }
        }
    }

    private void handleTextInputFigureFileSelector() {
        var startPath = getStartPath();
        var currentPath = getCurrentPath();
        switch (getFileReturnPart()) {
        case DIRECTORY:
        case FULL_PATH:
            currentPath = getText();
            break;
        default:
            if (currentPath == null) {
                if (startPath == null) {
                    currentPath = getText();
                } else {
                    currentPath = startPath;
                }
            }
            break;
        }

        switch (getFileSource()) {
        case WORKSPACE:
            var dialog = new ResourceSelectionDialog(Display.getCurrent().getActiveShell(), "Select workspace file",
                    getFileReturnPart() == FileReturnPart.DIRECTORY ? null : new String[] { "*.*" });
            if (currentPath != null) {
                dialog.setSelectedResource(new Path(currentPath));
            }
            if (dialog.open() == Window.OK) {
                var path = dialog.getSelectedResource();
                currentPath = path.toPortableString();
                var fileString = currentPath;
                switch (getFileReturnPart()) {
                case NAME_ONLY:
                    fileString = path.removeFileExtension().lastSegment();
                    break;
                case NAME_EXT:
                    fileString = path.lastSegment();
                    break;
                case FULL_PATH:
                case DIRECTORY:
                default:
                    break;
                }
                setText(fileString);
                setCurrentPath(currentPath);
                fireManualValueChange(getText());
            }
            break;
        case LOCAL:
            IPath[] paths = null;
            if (getFileReturnPart() == FileReturnPart.DIRECTORY) {
                var directoryDialog = new DirectoryDialog(Display.getCurrent().getActiveShell());
                directoryDialog.setFilterPath(currentPath);
                var directory = directoryDialog.open();
                if (directory != null) {
                    paths = new Path[] { new Path(directory) };
                }

            } else {
                var fileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.MULTI);
                if (currentPath != null) {
                    fileDialog.setFileName(currentPath);
                }
                var firstPath = fileDialog.open();
                if (firstPath != null) {
                    paths = new Path[fileDialog.getFileNames().length];
                    paths[0] = new Path(firstPath);
                    for (var i = 1; i < paths.length; i++) {
                        paths[i] = paths[0].removeLastSegments(1).append(fileDialog.getFileNames()[i]);
                    }
                }
            }
            if (paths != null) {
                currentPath = paths[0].toOSString();
                var result = new StringBuilder();
                switch (getFileReturnPart()) {
                case NAME_ONLY:
                    for (var i = 0; i < paths.length; i++) {
                        if (i > 0) {
                            result.append(SEPARATOR);
                        }
                        result.append(paths[i].removeFileExtension().lastSegment());
                    }
                    break;
                case NAME_EXT:
                    for (var i = 0; i < paths.length; i++) {
                        if (i > 0) {
                            result.append(SEPARATOR);
                        }
                        result.append(paths[i].lastSegment());
                    }
                    break;
                case FULL_PATH:
                case DIRECTORY:
                default:
                    for (var i = 0; i < paths.length; i++) {
                        if (i > 0) {
                            result.append(SEPARATOR);
                        }
                        result.append(paths[i].toOSString());
                    }
                    break;
                }
                setText(result.toString());
                setCurrentPath(currentPath);
                fireManualValueChange(getText());
            }

            break;
        default:
            break;
        }
    }
}

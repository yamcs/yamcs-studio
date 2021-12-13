/********************************************************************************
 * Copyright (c) 2010, 2021 Oak Ridge National Laboratory and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.csstudio.opibuilder.actions;

import org.csstudio.opibuilder.util.ErrorHandlerUtil;
import org.csstudio.opibuilder.util.ResourceUtil;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;

/**
 * The action to print display.
 */
public class PrintDisplayAction extends WorkbenchPartAction {

    public static final String ID = "org.csstudio.opibuilder.actions.print";

    public PrintDisplayAction(IWorkbenchPart part) {
        super(part);
    }

    @Override
    protected boolean calculateEnabled() {
        return true;
    }

    @Override
    protected void init() {
        super.init();
        setText("Print...");
        setToolTipText("Print Display");
        setId(ActionFactory.PRINT.getId());
        setActionDefinitionId("org.eclipse.ui.file.print");
        var sharedImages = getWorkbenchPart().getSite().getWorkbenchWindow().getWorkbench().getSharedImages();
        setImageDescriptor(sharedImages.getImageDescriptor(ISharedImages.IMG_ETOOL_PRINT_EDIT));
    }

    @Override
    public void run() {
        var viewer = getWorkbenchPart().getAdapter(GraphicalViewer.class);

        viewer.getControl().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                var loader = new ImageLoader();
                ImageData[] imageData;
                try {
                    imageData = loader.load(ResourceUtil.getScreenshotFile(viewer));

                    if (imageData.length > 0) {
                        var dialog = new PrintDialog(viewer.getControl().getShell(), SWT.NULL);
                        var data = dialog.open();
                        if (data != null) {
                            var printer = new Printer(data);

                            // Calculate the scale factor between the screen resolution
                            // and printer
                            // resolution in order to correctly size the image for the
                            // printer
                            var screenDPI = viewer.getControl().getDisplay().getDPI();
                            var printerDPI = printer.getDPI();
                            var scaleFactor = printerDPI.x / screenDPI.x;

                            // Determine the bounds of the entire area of the printer
                            var trim = printer.computeTrim(0, 0, 0, 0);
                            var printerImage = new Image(printer, imageData[0]);
                            if (printer.startJob("Printing OPI")) {
                                if (printer.startPage()) {
                                    var gc = new GC(printer);
                                    var printArea = printer.getClientArea();

                                    if (imageData[0].width * scaleFactor <= printArea.width) {
                                        printArea.width = imageData[0].width * scaleFactor;
                                        printArea.height = imageData[0].height * scaleFactor;
                                    } else {
                                        printArea.height = printArea.width * imageData[0].height / imageData[0].width;
                                    }
                                    gc.drawImage(printerImage, 0, 0, imageData[0].width, imageData[0].height, -trim.x,
                                            -trim.y, printArea.width, printArea.height);
                                    gc.dispose();
                                    printer.endPage();
                                }
                            }
                            printer.endJob();
                            printer.dispose();
                            printerImage.dispose();
                        }
                    }
                } catch (Exception e) {
                    ErrorHandlerUtil.handleError("Failed to print OPI", e);
                    return;
                }
            }
        });
    }
}

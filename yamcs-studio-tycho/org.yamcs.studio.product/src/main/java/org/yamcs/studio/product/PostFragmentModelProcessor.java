package org.yamcs.studio.product;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class PostFragmentModelProcessor {

    @Inject
    private MApplication app;

    @Execute
    public void execute(EModelService modelService) {
        MTrimmedWindow mainWindow = (MTrimmedWindow) app.getChildren().get(0);

        MToolControl topControl = MMenuFactory.INSTANCE.createToolControl();

        MTrimBar topBar = modelService.getTrim(mainWindow, SideValue.TOP);
        System.out.println("find.... " + topBar);
    }
}

package org.yamcs.studio.ycl.dsl.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.resource.containers.IAllContainersState;
import org.eclipse.xtext.ui.editor.IXtextEditorCallback;
import org.eclipse.xtext.ui.editor.model.IResourceForEditorInputFactory;
import org.eclipse.xtext.ui.editor.model.ResourceForIEditorInputFactory;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.ui.resource.SimpleResourceSetProvider;
import org.eclipse.xtext.ui.shared.Access;

import com.google.inject.Provider;

/**
 * Use this class to register components to be used within the IDE.
 */
public class YCLUiModule extends org.yamcs.studio.ycl.dsl.ui.AbstractYCLUiModule {
    public YCLUiModule(AbstractUIPlugin plugin) {
        super(plugin);
    }

    @Override
    public Class<? extends IXtextEditorCallback> bindIXtextEditorCallback() {
        /*
         * This prevents an annoying pop-up from asking whether you want to add the seemingly
         * useless 'xtext nature' to the project whenever you open a *.ycl file. <p> The default
         * behaviour was to return the NatureAddingEditorCallback.
         * 
         * @author fdi
         */
        return IXtextEditorCallback.NullImpl.class;
    }

    /**
     * Prevent JDT dependencies. https://bugs.eclipse.org/bugs/show_bug.cgi?id=311167
     */
    @Override
    public Class<? extends IResourceForEditorInputFactory> bindIResourceForEditorInputFactory() {
        return ResourceForIEditorInputFactory.class;
    }

    /**
     * Prevent JDT dependencies. https://bugs.eclipse.org/bugs/show_bug.cgi?id=311167
     */
    @Override
    public Class<? extends IResourceSetProvider> bindIResourceSetProvider() {
        return SimpleResourceSetProvider.class;
    }

    /**
     * Prevent JDT dependencies. https://bugs.eclipse.org/bugs/show_bug.cgi?id=311167
     */
    @Override
    public Provider<IAllContainersState> provideIAllContainersState() {
        return Access.getWorkspaceProjectsState();
    }
}

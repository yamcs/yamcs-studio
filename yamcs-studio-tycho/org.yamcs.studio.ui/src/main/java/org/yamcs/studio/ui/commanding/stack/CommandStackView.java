package org.yamcs.studio.ui.commanding.stack;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorFactory;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditorModelAccess;
import org.eclipse.xtext.ui.editor.embedded.IEditedResourceProvider;
import org.yamcs.studio.ycl.dsl.ui.internal.YCLActivator;

import com.google.inject.Injector;

/**
 * Inspiration for embedded editors:
 * https://github.com/eclipse/xtext/tree/master/plugins/org.eclipse
 * .xtext.ui.codetemplates.ui/src/org/eclipse/xtext/ui/codetemplates/ui/preferences
 */
@SuppressWarnings("restriction")
public class CommandStackView extends ViewPart {

    private LocalResourceManager resourceManager;
    private CommandStackTableViewer tableViewer;

    @Override
    public void createPartControl(Composite parent) {
        resourceManager = new LocalResourceManager(JFaceResources.getResources(), parent);

        parent.setLayout(new FillLayout());

        SashForm topDownSplit = new SashForm(parent, SWT.VERTICAL);
        topDownSplit.setLayout(new FillLayout());

        Composite tableWrapper = new Composite(topDownSplit, SWT.NONE);
        tableViewer = new CommandStackTableViewer(tableWrapper);

        Composite entryPanel = new Composite(topDownSplit, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        entryPanel.setLayout(gl);
        installSyntaxHighlighting(entryPanel);

        topDownSplit.setWeights(new int[] { 70, 30 });
    }

    private void installSyntaxHighlighting(Composite composite) {
        YCLActivator activator = YCLActivator.getInstance();
        Injector injector = activator.getInjector(YCLActivator.ORG_YAMCS_STUDIO_YCL_DSL_YCL);

        XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
        resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE); // TODO needed?

        IEditedResourceProvider resourceProvider = new IEditedResourceProvider() {
            @Override
            public XtextResource createResource() {
                try {
                    Resource resource = resourceSet.createResource(URI.createURI("dummy:/ex.ycl"));
                    return (XtextResource) resource;
                } catch (Exception e) {
                    return null;
                }
            }
        };

        EmbeddedEditorFactory factory = injector.getInstance(EmbeddedEditorFactory.class);
        EmbeddedEditor handle = factory.newEditor(resourceProvider).withParent(composite);
        EmbeddedEditorModelAccess partialEditor = handle.createPartialEditor();
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void dispose() {
        super.dispose();
        resourceManager.dispose();
    }
}

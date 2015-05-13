package org.yamcs.studio.ui.commanding.staging;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
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
 * https://github.com/eclipse/xtext/tree/master/plugins/org.eclipse.xtext.ui.codetemplates.ui/src/
 * org/eclipse/xtext/ui/codetemplates/ui/preferences
 *
 */
@SuppressWarnings("restriction")
public class StageCommandDialog extends TitleAreaDialog {

    public StageCommandDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    public void create() {
        super.create();
        setTitle("Stage one or more commands");
        // setMessage("informative message");
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);

        Composite codeComposite = new Composite(area, SWT.NONE);
        codeComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        codeComposite.setLayout(gl);
        installSyntaxHighlighting(codeComposite);

        return area;
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
    protected Point getInitialSize() {
        return new Point(500, 375);
    }
}

package org.yamcs.studio.css.theming;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

public class StyleEditor extends EditorPart {

    public static final String ID = "org.yamcs.studio.css.theming.styleeditor";
    private static final Logger log = Logger.getLogger(StyleEditor.class.getName());

    private FileEditorInput input;

    private FormToolkit tk;
    private ScrolledForm form;
    private ResourceManager resourceManager;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        this.input = (FileEditorInput) input;
        setSite(site);
        setInput(input);
        setPartName(input.getName());
    }

    @Override
    public void createPartControl(Composite parent) {
        tk = new FormToolkit(parent.getDisplay());

        Composite formWrapper = tk.createComposite(parent);
        GridLayout gl = new GridLayout();
        formWrapper.setLayout(gl);

        form = tk.createScrolledForm(formWrapper);
        form.setText("Style Editor");
        form.setLayoutData(new GridData(GridData.FILL_BOTH));

        gl = new GridLayout();
        form.getBody().setLayout(gl);

        Section section = tk.createSection(form.getBody(), Section.TITLE_BAR);
        section.setText("Colors");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        StyleDefinition def = loadData(parent.getDisplay());

        ManagedForm managedForm = new ManagedForm(tk, form);
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
        new ThemeColorBlock(def, parent.getDisplay(), resourceManager).createContent(managedForm);

        ManagedForm fontsMForm = new ManagedForm(tk, form);
        section = tk.createSection(form.getBody(), Section.TITLE_BAR);
        section.setText("Fonts");
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        new ThemeColorBlock(def, parent.getDisplay(), resourceManager).createContent(fontsMForm);
    }

    private StyleDefinition loadData(Device device) {
        try {
            return StyleDefinition.from(input.getFile().getContents());
        } catch (IOException | CoreException e) {
            log.log(Level.SEVERE, "Could not read style definition", e);
            return null;
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public void setFocus() {
        form.setFocus();
    }

    @Override
    public void dispose() {
        tk.dispose();
        resourceManager.dispose();
        super.dispose();
    }
}

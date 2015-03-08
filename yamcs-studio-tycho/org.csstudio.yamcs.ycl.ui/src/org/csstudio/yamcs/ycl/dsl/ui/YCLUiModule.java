package org.csstudio.yamcs.ycl.dsl.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.editor.IXtextEditorCallback;

/**
 * Use this class to register components to be used within the IDE.
 */
public class YCLUiModule extends org.csstudio.yamcs.ycl.dsl.ui.AbstractYCLUiModule {
	public YCLUiModule(AbstractUIPlugin plugin) {
		super(plugin);
	}
	
	@Override
	public Class<? extends IXtextEditorCallback> bindIXtextEditorCallback() {
	    /*
	     * This prevents an annoying pop-up from asking whether you want to add
	     * the seemingly useless 'xtext nature' to the project whenever you open
	     * a *.ycl file.
	     * <p>
	     * The default behaviour was to return the NatureAddingEditorCallback. 
	     * 
	     * @author fdi
	     */
	    return IXtextEditorCallback.NullImpl.class;
	}
}

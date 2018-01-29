package org.yamcs.studio.ui.alphanum;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class ClearViewHandler extends AbstractHandler {

	private static final Logger log = Logger.getLogger(ImportAlphaNumericHandler.class.getName());

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		log.log(Level.INFO, "Clearing alpha numeric view");

		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkbenchPart part = window.getActivePage().findView(AlphaNumericView.ID);
		AlphaNumericView alphanumericView = (AlphaNumericView) part;
		alphanumericView.clear();
		return null;

	}
}

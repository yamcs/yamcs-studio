package org.yamcs.studio.css.core.prefs;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.menus.UIElement;
import org.osgi.framework.Bundle;
import org.yamcs.studio.css.core.Activator;
import org.yamcs.studio.css.core.PVManagerSubscriptionHandler;

public class SoundCommandHandler extends AbstractHandler implements IElementUpdater {

    private static ImageDescriptor image_sound_mute;
    private static ImageDescriptor image_sound_low;
    private static ImageDescriptor image_sound_on;

    public SoundCommandHandler() {
        Bundle bundle = Platform.getBundle("org.yamcs.studio.css.core");
        URL fullPathString = BundleUtility.find(bundle, "icons/sound_mute_16px.png");
        image_sound_mute = ImageDescriptor.createFromURL(fullPathString);

        fullPathString = BundleUtility.find(bundle, "icons/sound_low_16px.png");
        image_sound_low = ImageDescriptor.createFromURL(fullPathString);

        fullPathString = BundleUtility.find(bundle, "icons/sound_on_16px.png");
        image_sound_on = ImageDescriptor.createFromURL(fullPathString);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        String commandBeep = event.getParameter("org.yamcs.studio.css.core.prefs.beep");

        IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
        String previousBeep = preferenceStore.getString("triggerBeep");
        String newBeep = previousBeep;

        if (commandBeep.equals("toogle")) {
            if (previousBeep.equals("NONE")) {
                newBeep = "FIRST";
            } else if (previousBeep.equals("FIRST")) {
                newBeep = "EACH";
            } else if (previousBeep.equals("EACH")) {
                newBeep = "NONE";
            }
        } else {
            newBeep = commandBeep;
        }
        preferenceStore.setValue("triggerBeep", newBeep);
        beep = newBeep;

        PVManagerSubscriptionHandler.getInstance().getBeeper().updatePreference();

        // IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        // ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
        // if (commandService != null) {
        // commandService.refreshElements("dropdownSoundCommand", null);
        // }

        return null;
    }

    public static String beep = "";

    @Override
    public void updateElement(UIElement element, Map parameters) {

        String param = (String) parameters.get("org.yamcs.studio.css.core.prefs.beep");
        if (!param.equals("toogle")) {
            return;
        }

        if (beep == null || beep.isEmpty()) {
            IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
            beep = preferenceStore.getString("triggerBeep");
        }

        if (beep.equals("NONE")) {
            element.setIcon(image_sound_mute);
        } else if (beep.equals("FIRST")) {
            element.setIcon(image_sound_low);
        } else if (beep.equals("EACH")) {
            element.setIcon(image_sound_on);
        }
    }

}

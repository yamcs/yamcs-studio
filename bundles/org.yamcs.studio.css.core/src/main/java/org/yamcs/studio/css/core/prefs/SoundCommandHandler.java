package org.yamcs.studio.css.core.prefs;

import java.net.URL;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.util.BundleUtility;
import org.eclipse.ui.menus.UIElement;
import org.osgi.framework.Bundle;
import org.yamcs.studio.css.core.Activator;

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

        beep = event.getParameter("org.yamcs.studio.css.core.prefs.beep");

        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        ICommandService commandService = (ICommandService) window.getService(ICommandService.class);
        if (commandService != null) {
            commandService.refreshElements("dropdownSoundCommand", null);
        }

        return null;
    }

    public static String beep = "";
    static String previousBeepMode = "";

    @Override
    public void updateElement(UIElement element, Map parameters) {

        String param = (String) parameters.get("org.yamcs.studio.css.core.prefs.beep");
        
        if (beep == null || beep.isEmpty()) {
            IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
            beep = preferenceStore.getString("triggerBeep");            
        }
        if (previousBeepMode.isEmpty()) {
            IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
            previousBeepMode = preferenceStore.getString("triggerBeep");            
        }
        
        String newBeepMode = beep;
        if (beep.equals("toogle")) {
            if (previousBeepMode.equals("NONE")) {
                newBeepMode = "FIRST";
            } else if (previousBeepMode.equals("FIRST")) {
                newBeepMode = "EACH";
            } else if (previousBeepMode.equals("EACH")) {
                newBeepMode = "NONE";
            }
        }
        setBeepMode(element, newBeepMode, param.equals("toogle"));
    }

    private void setBeepMode(UIElement element, String newBeepMode, boolean updateIcon) {
        
        IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

        if (newBeepMode.equals("NONE")) {
            if (updateIcon)
                element.setIcon(image_sound_mute);
            preferenceStore.setValue("triggerBeep", "NONE");
        } else if (newBeepMode.equals("FIRST")) {
            if (updateIcon)
                element.setIcon(image_sound_low);
            preferenceStore.setValue("triggerBeep", "FIRST");
        } else if (newBeepMode.equals("EACH")) {
            if (updateIcon)
                element.setIcon(image_sound_on);
            preferenceStore.setValue("triggerBeep", "EACH");
        }
        previousBeepMode = newBeepMode;
    }

}
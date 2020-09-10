package org.csstudio.opibuilder.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.csstudio.opibuilder.preferences.PreferencesHelper;
import org.yamcs.studio.data.ExceptionHandler;
import org.yamcs.studio.data.IPV;
import org.yamcs.studio.data.PVFactory;

/**
 * The factory to create a PV for BOY. It will create either Utility PV or PVManager PV which depends on the preference
 * settings.
 */
public class BOYPVFactory {

    /**
     * The default background thread for PV change event notification.
     */
    private final static ExecutorService BOY_PV_THREAD = Executors
            .newSingleThreadExecutor();

    private final static ExceptionHandler exceptionHandler = ex -> ErrorHandlerUtil
            .handleError("Error from pv connection layer: ", ex);

    /**
     * Create a PV. If it is using PV Manager, max update rate is determined by GUI Refresh cycle.
     */
    public static IPV createPV(String name) throws Exception {
        return createPV(name, PreferencesHelper.getGUIRefreshCycle());
    }

    /**
     * Create a PV based on PV connection layer preference.
     * 
     * @param updateDuration
     *            the fastest update duration.
     */
    public static IPV createPV(String name, int updateDuration) throws Exception {
        PVFactory pvFactory = PVFactory.getInstance();
        return pvFactory.createPV(
                name, false, updateDuration, BOY_PV_THREAD, exceptionHandler);
    }
}

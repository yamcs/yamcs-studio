package org.yamcs.studio.core.pvmanager;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.yamcs.protobuf.Pvalue.AcquisitionStatus;
import org.yamcs.protobuf.Pvalue.ParameterValue;
import org.yamcs.studio.core.model.TimeCatalogue;
import org.yamcs.studio.core.model.TimeListener;
import org.yamcs.utils.TimeEncoding;

/**
 *
 * LosTracker
 *
 * Keep the last PV received and trigger an action if a PV expires.
 *
 */
public class LosTracker implements TimeListener {
    private static final Logger log = Logger.getLogger(LosTracker.class.getName());

    Calendar missionTime = null;
    Map<YamcsPVReader, PvExpiration> pvs = new HashMap<>();
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public LosTracker()
    {
        TimeCatalogue.getInstance().addTimeListener(this);
    }

    public void updatePv(YamcsPVReader pvReader, ParameterValue pval)
    {
        if (!pvs.containsKey(pvReader))
        {
            PvExpiration pvExpiration = new PvExpiration(pvReader);
            pvs.put(pvReader, pvExpiration);
        }
        PvExpiration pvExpiration = pvs.get(pvReader);
        pvExpiration.updateLosHandle(pval);
    }

    public void shutdown() {
        scheduler.shutdown();
    }

    class PvExpiration
    {
        YamcsPVReader pvReader;
        ParameterValue lastKnownValue;
        ScheduledFuture<?> losHandle;

        public PvExpiration(YamcsPVReader pvReader)
        {
            this.pvReader = pvReader;
        }

        public void updateLosHandle(ParameterValue lastKnownValue)
        {

            if (this.losHandle != null)
                this.losHandle.cancel(false);
            this.lastKnownValue = lastKnownValue;

            if (!lastKnownValue.hasExpirationTime() || missionTime == null)
                return;

            //Calendar now = Calendar.getInstance();
            Calendar expirationTime = TimeEncoding.toCalendar(lastKnownValue.getExpirationTime());

            long expirationDelayMs = expirationTime.getTimeInMillis() - missionTime.getTimeInMillis();

            losHandle = scheduler.schedule(displayLos, expirationDelayMs, TimeUnit.MILLISECONDS);
        }

        private final Runnable displayLos = new Runnable() {
            @Override
            public void run() {
                log.fine("LOS for parameter " + pvReader.getId().getName());
                pvReader.processParameterValue(ParameterValue.newBuilder(lastKnownValue).setAcquisitionStatus(AcquisitionStatus.EXPIRED).build());
            }
        };
    }

    @Override
    public void processTime(long newMissionTime) {
        if (newMissionTime == TimeEncoding.INVALID_INSTANT || newMissionTime == 0) {
            missionTime = null;
        }
        missionTime = TimeEncoding.toCalendar(newMissionTime);
    }
}

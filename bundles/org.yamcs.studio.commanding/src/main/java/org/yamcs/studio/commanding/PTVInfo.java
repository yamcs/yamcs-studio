package org.yamcs.studio.commanding;

import java.util.logging.Logger;

import org.yamcs.protobuf.Yamcs.Value;

/**
 * Server-side PTV information (captures the processing of transmission constraints). States match
 * what yamcs sends to us.
 */
public class PTVInfo implements Comparable<PTVInfo> {

    private static final Logger log = Logger.getLogger(PTVInfo.class.getName());

    public enum State {
        UNDEF(0), // The only state that comes from the client (catch-all)
        PENDING(1),
        NOK(2),
        NA(3),
        OK(4);

        private int sortOrder;

        private State(int sortOrder) {
            this.sortOrder = sortOrder;
        }

        public int getSortOrder() {
            return sortOrder;
        }

        /**
         * Converts a yamcs Value as provided by the server to a PTV state
         */
        public static State fromYamcsValue(Value value) {
            switch (value.getStringValue()) {
            case "PENDING":
                return PENDING;
            case "NOK":
                return NOK;
            case "NA":
                return NA;
            case "OK":
                return OK;
            default:
                log.warning(String.format("Converted unexpected TransmissionConstraint '%s' to state 'UNDEF'", value.getStringValue()));
                return UNDEF;
            }
        }
    }

    private State state = State.UNDEF;
    private String failureMessage;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    @Override
    public String toString() {
        if (failureMessage != null)
            return String.format("%s (%s)", state, failureMessage);
        else
            return state.toString();
    }

    @Override
    public int compareTo(PTVInfo o) {
        return Integer.compare(state.sortOrder, o.state.sortOrder);
    }
}

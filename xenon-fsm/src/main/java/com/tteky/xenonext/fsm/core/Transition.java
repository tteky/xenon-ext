package com.tteky.xenonext.fsm.core;

import com.vmware.xenon.common.Operation;

/**
 * POJO encapsulating the transition with context
 */
public class Transition {

    private final String source;
    private final String destination;
    private final String trigger;
    private Operation cause;

    /**
     * Construct a transition
     *
     * @param source      The state transitioned from
     * @param destination The state transitioned to
     * @param trigger     The trigger that caused the transition
     */
    public Transition(String source, String destination, String trigger) {
        this.source = source;
        this.destination = destination;
        this.trigger = trigger;
    }

    /**
     * The state transitioned from
     *
     * @return The state transitioned from
     */
    public String getSource() {
        return source;
    }

    /**
     * The state transitioned to
     *
     * @return The state transitioned to
     */
    public String getDestination() {
        return destination;
    }

    /**
     * The trigger that caused the transition
     *
     * @return The trigger that caused the transition
     */
    public String getTrigger() {
        return trigger;
    }

    /**
     * True if the transition is a re-entry, i.e. the identity transition
     *
     * @return True if the transition is a re-entry
     */
    public boolean isReentry() {
        return getSource().equals(getDestination());
    }

    public Operation getCause() {
        return cause;
    }

    public void setCause(Operation op) {
        this.cause = op;
    }

    @Override
    public String toString() {
        return "Transition{" +
                "source=" + source +
                ", destination=" + destination +
                ", trigger=" + trigger +
                '}';
    }
}

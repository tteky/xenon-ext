package com.tteky.xenonext.fsm.core;

import com.vmware.xenon.common.Operation;

import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Predicate;

public abstract class TriggerBehaviour {

    private final String trigger;
    private final Predicate<String> guard;

    protected TriggerBehaviour(String trigger, Predicate<String> guard) {
        this.trigger = trigger;
        this.guard = guard;
    }

    public String getTrigger() {
        return trigger;
    }

    public boolean isGuardConditionMet() {
        return guard.test(trigger);
    }

    public abstract Map.Entry<Boolean, String> resultsInTransitionFrom(String source, Operation args);


    static TriggerBehaviour newTransitionBehaviour(String trigger, String destination, Predicate<String> guard) {
        return new TransitioningTriggerBehaviour(trigger, destination, guard);
    }

    public static class TransitioningTriggerBehaviour extends TriggerBehaviour {

        private final String destination;

        private TransitioningTriggerBehaviour(String trigger, String destination, Predicate<String> guard) {
            super(trigger, guard);
            this.destination = destination;
        }

        @Override
        public Map.Entry<Boolean, String> resultsInTransitionFrom(String source, Operation args) {
            return new AbstractMap.SimpleEntry<>(true, destination);
        }
    }
}

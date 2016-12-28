package com.tteky.xenonext.fsm.core;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StateRepresentation {

    private final String state;

    private final Map<String, List<TriggerBehaviour>> triggerBehaviours = new HashMap<>();
    private final List<Consumer<Transition>> entryActions = new ArrayList<>();
    private final Map<String, Long> expiryActions = new HashMap<>();
    private final List<Consumer<Transition>> exitActions = new ArrayList<>();

    private final Logger log = LoggerFactory.getLogger(getClass());

    public StateRepresentation(String state) {
        this.state = state;
    }

    protected Map<String, List<TriggerBehaviour>> getTriggerBehaviours() {
        return triggerBehaviours;
    }

    public Boolean canHandle(String trigger) {
        return tryFindHandler(trigger) != null;
    }

    public TriggerBehaviour tryFindHandler(String trigger) {
        return tryFindLocalHandler(trigger);
    }

    TriggerBehaviour tryFindLocalHandler(String trigger/*, out TriggerBehaviour handler*/) {
        List<TriggerBehaviour> possible = triggerBehaviours.get(trigger);
        if (possible == null) {
            return null;
        }

        List<TriggerBehaviour> actual = new ArrayList<>();
        for (TriggerBehaviour triggerBehaviour : possible) {
            if (triggerBehaviour.isGuardConditionMet()) {
                actual.add(triggerBehaviour);
            }
        }

        if (actual.size() > 1) {
            throw new IllegalStateException("Multiple permitted exit transitions are configured from state '" + state + "' for trigger '" + trigger + "'. Guard clauses must be mutually exclusive.");
        }

        return actual.isEmpty() ? null : actual.get(0);
    }

    public void addEntryAction(final String trigger, final Consumer<Transition> action) {
        assert action != null : "action is null";

        entryActions.add(t -> {
            String trans_trigger = t.getTrigger();
            if (trans_trigger != null && trans_trigger.equals(trigger)) {
                action.accept(t);
            }
        });
    }

    public void addExpiryAction(String trigger, long delay) {
        assert trigger != null : "trigger is null";
        expiryActions.put(trigger, delay);
    }

    public void addEntryAction(Consumer<Transition> action) {
        assert action != null : "action is null";
        entryActions.add(action);
    }

    public void addExitAction(Consumer<Transition> action) {
        assert action != null : "action is null";
        exitActions.add(action);
    }

    public void enter(Transition transition) {
        assert transition != null : "transition is null";
        executeEntryActions(transition);
    }

    public void expiry(Transition transition, BiConsumer<Map<String, Long>, Transition> expiryScheduler) {
        assert transition != null : "transition is null";
        if (expiryActions.size() > 0) {
            if (expiryScheduler == null) {
                log.warn("Skipping expiry scheduling as no expiry scheduler is configured. Note that expiry triggers wont be fired ");
                return;
            }
            expiryScheduler.accept(expiryActions, transition);
        }
    }

    public void exit(Transition transition) {
        assert transition != null : "transition is null";
        executeExitActions(transition);
    }

    void executeEntryActions(Transition transition) {
        assert transition != null : "transition is null";
        for (Consumer<Transition> action : entryActions) {
            action.accept(transition);
        }
    }

    void executeExitActions(Transition transition) {
        assert transition != null : "transition is null";
        for (Consumer<Transition> action : exitActions) {
            action.accept(transition);
        }
    }

    public void addTriggerBehaviour(TriggerBehaviour triggerBehaviour) {
        List<TriggerBehaviour> allowed;
        if (!triggerBehaviours.containsKey(triggerBehaviour.getTrigger())) {
            allowed = new ArrayList<>();
            triggerBehaviours.put(triggerBehaviour.getTrigger(), allowed);
        }
        allowed = triggerBehaviours.get(triggerBehaviour.getTrigger());
        allowed.add(triggerBehaviour);
    }

    public String getUnderlyingState() {
        return state;
    }

    @SuppressWarnings("unchecked")
    public List<String> getPermittedTriggers() {
        Set<String> result = new HashSet<>();

        for (String t : triggerBehaviours.keySet()) {
            for (TriggerBehaviour v : triggerBehaviours.get(t)) {
                if (v.isGuardConditionMet()) {
                    result.add(t);
                    break;
                }
            }
        }
        return new ArrayList<>(result);
    }
}

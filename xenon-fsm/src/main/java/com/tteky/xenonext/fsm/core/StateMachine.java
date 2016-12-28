package com.tteky.xenonext.fsm.core;

import com.vmware.xenon.common.Operation;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static java.lang.String.format;

/**
 * Created by mageshwaranr on 11-Dec-16.
 */
public class StateMachine {

    /**
     * Static, immutable StateMachineConfig
     */
    private StateMachineConfig config;
    /**
     * Actual service instance hosting FSM
     */
    private BiConsumer<Map<String, Long>, Transition> expiryScheduler;

    public StateMachine(StateMachineConfig config) {
        this.config = config;
    }

    /**
     * Returns true if trigger is valid on current state machine
     *
     * @param state current state
     * @param op    operation which will be failed in case of validation issue
     * @return true if trigger can be fired
     */
    protected boolean validateTrigger(String state, String trigger, Operation op) {
        if (getConfig().getRepresentation(state).canHandle(trigger)) {
            return true;
        } else {
            List<String> permittedTriggers = getConfig().getRepresentation(state).getPermittedTriggers();
            String err = format("In current state %s, %s trigger is invalid. Valid triggers are %s", state, trigger, permittedTriggers);
            op.fail(new IllegalStateException(err));
            return false;
        }
    }

    /**
     * Returns the initial state
     *
     * @return
     */
    public String initState() {
        return getConfig().getInitState();
    }


    /**
     * Initiates the transition from the current state via the specified trigger.
     * The target state is determined by the configuration of the current state.
     * Handlers associated with leaving the current state and entering the new state will be invoked
     *
     * @param patch   the operation responsible for the trigger
     * @param src     the present state
     * @param trigger
     */

    public Transition fireTrigger(String src, String trigger, Operation patch, Consumer<Transition> statePersister) {
        if (validateTrigger(src, trigger, patch)) {
            Transition transition = transit(src, trigger, patch);
            assert transition != null : "transition is null";
            statePersister.accept(transition);
            fireOnExit(transition);
            scheduleExpiry(transition);
            fireOnEntry(transition);
            return transition;
        }
        return null;
    }

    /**
     * Creates a transition for the given trigger and current state.
     * <p>
     * Returns null if the trigger is not valid on given state
     */
    protected Transition transit(String src, String trigger, Operation op) {
        StateRepresentation currentRepresentation = getConfig().getRepresentation(src);
        TriggerBehaviour triggerBehaviour = currentRepresentation.tryFindHandler(trigger);
        Map.Entry<Boolean, String> canTransitVsDestState = triggerBehaviour.resultsInTransitionFrom(src, op);
        Transition transition = null;
        if (canTransitVsDestState.getKey()) {
            String destination = canTransitVsDestState.getValue();
            transition = new Transition(src, destination, trigger);
            transition.setCause(op);
        }
        return transition;
    }

    /**
     * Invokes the onExpiry handlers attached to the target state of the FSM
     */
    protected void scheduleExpiry(Transition transition) {
        getConfig().getRepresentation(transition.getDestination()).expiry(transition, expiryScheduler);
    }

    /**
     * Invokes the onExit handlers attached to the current state of the FSM
     */
    protected void fireOnExit(Transition transition) {
        getConfig().getRepresentation(transition.getSource()).exit(transition);
    }

    /**
     * Invokes the onEntry handlers attached to the target/destination state of the FSM resulted because of the trigger
     */
    protected void fireOnEntry(Transition transition) {
        getConfig().getRepresentation(transition.getDestination()).enter(transition);
    }

    public void setExpiryScheduler(BiConsumer<Map<String, Long>, Transition> expiryScheduler) {
        this.expiryScheduler = expiryScheduler;
    }

    private StateMachineConfig getConfig() {
        return config;
    }


}

package com.tteky.xenonext.fsm.core;


import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Configuration pertaining to single node/state in FSM
 */
public class StateConfiguration {

    private final Predicate<String> NO_GUARD = t -> true;

    private final StateRepresentation representation;

    public StateConfiguration(final StateRepresentation representation) {
        assert representation != null : "representation is null";
        this.representation = representation;
    }

    /**
     * Accept the specified trigger and transition to the destination state
     *
     * @param trigger          The accepted trigger
     * @param destinationState The state that the trigger will cause a transition to
     * @return The reciever
     */
    public StateConfiguration permit(String trigger, String destinationState) {
        enforceNotIdentityTransition(destinationState);
        return publicPermit(trigger, destinationState);
    }

    /**
     * Accept the specified trigger and transition to the destination state
     *
     * @param trigger          The accepted trigger
     * @param destinationState The state that the trigger will cause a transition to
     * @param guard            Function that must return true in order for the trigger to be accepted
     * @return The reciever
     */
    public StateConfiguration permitIf(String trigger, String destinationState, Predicate<String> guard) {
        enforceNotIdentityTransition(destinationState);
        return publicPermitIf(trigger, destinationState, guard);
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
     * configured state transitions to an identical sibling state
     * <p>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
     * transitioning between super- and sub-states
     *
     * @param trigger The accepted trigger
     * @return The receiver
     */
    public StateConfiguration permitReentry(String trigger) {
        return publicPermit(trigger, representation.getUnderlyingState());
    }

    /**
     * Accept the specified trigger, execute exit actions and re-execute entry actions. Reentry behaves as though the
     * configured state transitions to an identical sibling state
     * <p>
     * Applies to the current state only. Will not re-execute superstate actions, or  cause actions to execute
     * transitioning between super- and sub-states
     *
     * @param trigger The accepted trigger
     * @param guard   Function that must return true in order for the trigger to be accepted
     * @return The reciever
     */
    public StateConfiguration permitReentryIf(String trigger, Predicate<String> guard) {
        return publicPermitIf(trigger, representation.getUnderlyingState(), guard);
    }


    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param entryAction Action to execute, providing details of the transition
     * @return The receiver
     */
    public StateConfiguration onEntry(final Consumer<Transition> entryAction) {
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(entryAction);
        return this;
    }

    /**
     * Specify an action that will execute when transitioning into the configured state
     *
     * @param trigger     The trigger by which the state must be entered in order for the action to execute
     * @param entryAction Action to execute, providing details of the transition
     * @return The receiver
     */
    public StateConfiguration onEntryFrom(String trigger, final Consumer<Transition> entryAction) {
        assert entryAction != null : "entryAction is null";
        representation.addEntryAction(trigger, entryAction);
        return this;
    }

    /**
     * Specify a trigger that will initiate a transition after configured interval
     *
     * @param trigger             trigger that needs to be fired
     * @param expireAfterInMillis interval after which trigger needs to be patched
     * @return
     */
    public StateConfiguration onExpiry(String trigger, long expireAfterInMillis) {
        assert expireAfterInMillis > 0 : "expireAfter time duration is less than zero";
        representation.addExpiryAction(trigger, expireAfterInMillis);
        return this;
    }


    /**
     * Specify an action that will execute when transitioning from the configured state
     *
     * @param exitAction Action to execute
     * @return The receiver
     */
    public StateConfiguration onExit(Consumer<Transition> exitAction) {
        assert exitAction != null : "exitAction is null";
        representation.addExitAction(exitAction);
        return this;
    }

    void enforceNotIdentityTransition(String destination) {
        if (destination.equals(representation.getUnderlyingState())) {
            throw new IllegalStateException("Permit() (and PermitIf()) require that the destination state is not equal to the source state. To accept a trigger without changing state, use either Ignore() or PermitReentry().");
        }
    }

    StateConfiguration publicPermit(String trigger, String destinationState) {
        return publicPermitIf(trigger, destinationState, NO_GUARD);
    }

    StateConfiguration publicPermitIf(String trigger, String destinationState, Predicate<String> guard) {
        assert guard != null : "guard is null";
        representation.addTriggerBehaviour(TriggerBehaviour.newTransitionBehaviour(trigger, destinationState, guard));
        return this;
    }


}

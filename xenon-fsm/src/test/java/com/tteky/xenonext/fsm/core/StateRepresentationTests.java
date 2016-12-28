package com.tteky.xenonext.fsm.core;


import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.tteky.xenonext.fsm.core.TriggerBehaviour.newTransitionBehaviour;
import static org.junit.Assert.*;

public class StateRepresentationTests {

    List<Transition> transitions = new ArrayList<>();

    @Test
    public void UponEntering_EnteringActionsExecuted() {
        StateRepresentation stateRepresentation = createRepresentation(State.B);
        Transition transition = new Transition(State.A.name(), State.B.name(), Trigger.X.name());
        transitions.clear();
        stateRepresentation.addEntryAction(stateTriggerTransition -> transitions.add(stateTriggerTransition));
        stateRepresentation.enter(transition);
        assertEquals(transition, transitions.get(0));
    }

    @Test
    public void UponLeaving_EnteringActionsNotExecuted() {
        StateRepresentation stateRepresentation = createRepresentation(State.B);
        Transition transition = new Transition(State.A.name(), State.B.name(), Trigger.X.name());
        transitions.clear();
        stateRepresentation.addEntryAction(stateTriggerTransition -> transitions.add(stateTriggerTransition));
        stateRepresentation.exit(transition);
        assertTrue(transitions.isEmpty());
    }

    @Test
    public void UponLeaving_LeavingActionsExecuted() {

        StateRepresentation stateRepresentation = createRepresentation(State.A);
        Transition transition = new Transition(State.A.name(), State.B.name(), Trigger.X.name());
        transitions.clear();
        stateRepresentation.addExitAction(stateTriggerTransition -> transitions.add(stateTriggerTransition));
        stateRepresentation.exit(transition);
        assertEquals(transition, transitions.get(0));
    }

    @Test
    public void UponEntering_LeavingActionsNotExecuted() {
        StateRepresentation stateRepresentation = createRepresentation(State.A);
        Transition transition = new Transition(State.A.name(), State.B.name(), Trigger.X.name());
        transitions.clear();
        stateRepresentation.addExitAction(stateTriggerTransition -> transitions.add(stateTriggerTransition));
        stateRepresentation.enter(transition);
        assertTrue(transitions.isEmpty());
    }


    @Test
    public void EntryActionsExecuteInOrder() {
        final ArrayList<Integer> actual = new ArrayList<>();

        StateRepresentation rep = createRepresentation(State.B);
        rep.addEntryAction(stateTriggerTransition -> actual.add(0));
        rep.addEntryAction(stateTriggerTransition -> actual.add(1));
        rep.enter(new Transition(State.A.name(), State.B.name(), Trigger.X.name()));

        assertEquals(2, actual.size());
        assertEquals(0, actual.get(0).intValue());
        assertEquals(1, actual.get(1).intValue());
    }

    @Test
    public void ExitActionsExecuteInOrder() {
        final List<Integer> actual = new ArrayList<>();

        StateRepresentation rep = createRepresentation(State.B);
        rep.addExitAction(stateTriggerTransition -> actual.add(0));
        rep.addExitAction(stateTriggerTransition -> actual.add(1));
        rep.exit(new Transition(State.B.name(), State.C.name(), Trigger.X.name()));

        assertEquals(2, actual.size());
        assertEquals(0, actual.get(0).intValue());
        assertEquals(1, actual.get(1).intValue());
    }

    @Test
    public void WhenTransitionExists_TriggerCanBeFired() {
        StateRepresentation rep = createRepresentation(State.B);
        assertFalse(rep.canHandle(Trigger.X.name()));
    }

    @Test
    public void WhenTransitionExistsButGuardConditionNotMet_TriggerCanBeFired() {
        StateRepresentation rep = createRepresentation(State.B);
        rep.addTriggerBehaviour(newTransitionBehaviour(Trigger.X.name(), State.C.name(), trigger -> false));
        assertFalse(rep.canHandle(Trigger.X.name()));
    }

    @Test
    public void WhenTransitionDoesNotExist_TriggerCannotBeFired() {
        StateRepresentation rep = createRepresentation(State.B);
        rep.addTriggerBehaviour(newTransitionBehaviour(Trigger.X.name(), State.C.name(), trigger -> true));
        assertTrue(rep.canHandle(Trigger.X.name()));
    }

    StateRepresentation createRepresentation(State state) {
        return new StateRepresentation(state.name());
    }
}

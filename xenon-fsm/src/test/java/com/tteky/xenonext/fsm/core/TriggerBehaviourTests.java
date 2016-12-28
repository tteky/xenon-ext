package com.tteky.xenonext.fsm.core;

import org.junit.Test;

import java.util.Map;

import static com.tteky.xenonext.fsm.core.TriggerBehaviour.newTransitionBehaviour;
import static org.junit.Assert.*;

public class TriggerBehaviourTests {

    @Test
    public void ExposesCorrectUnderlyingTrigger() {
        TriggerBehaviour transtioning = newTransitionBehaviour(
                Trigger.X.name(), State.C.name(), trigger -> true);

        assertEquals(Trigger.X.name(), transtioning.getTrigger());
    }

    @Test
    public void WhenGuardConditionFalse_IsGuardConditionMetIsFalse() {
        TriggerBehaviour transtioning = newTransitionBehaviour(Trigger.X.name(), State.C.name(), trigger -> false);

        assertFalse(transtioning.isGuardConditionMet());
    }

    @Test
    public void WhenGuardConditionTrue_IsGuardConditionMetIsTrue() {
        TriggerBehaviour transtioning = newTransitionBehaviour(Trigger.X.name(), State.C.name(), trigger -> true);

        assertTrue(transtioning.isGuardConditionMet());
    }

    @Test
    public void TransitionsToDestinationState() {
        TriggerBehaviour transtioning = newTransitionBehaviour(Trigger.X.name(), State.C.name(), trigger -> true);

        Map.Entry<Boolean, String> canTransitVsState = transtioning.resultsInTransitionFrom(State.B.name(), null);

        assertTrue(canTransitVsState.getKey());
        assertEquals(State.C.name(), canTransitVsState.getValue());
    }
}

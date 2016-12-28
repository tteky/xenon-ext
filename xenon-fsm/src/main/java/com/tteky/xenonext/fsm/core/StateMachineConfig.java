package com.tteky.xenonext.fsm.core;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The state machine configuration
 * <p>
 * Created by mageshwaranr on 08-Dec-16.
 */
public class StateMachineConfig {

    private final Map<String, StateRepresentation> stateConfiguration = new HashMap<>();

    private final String initState;

    /**
     * Config instance holding state machine
     *
     * @param initState Initial state of the state machine
     */
    private StateMachineConfig(String initState) {
        this.initState = initState;
    }

    /**
     * Return StateRepresentation for the specified state. May return null.
     *
     * @param state The state
     * @return StateRepresentation for the specified state, or null.
     */
    public StateRepresentation getRepresentation(String state) {
        return stateConfiguration.get(state);
    }

    /**
     * Return StateRepresentation for the specified state. Creates representation if it does not exist.
     *
     * @param state The state
     * @return StateRepresentation for the specified state.
     */
    private StateRepresentation getOrCreateRepresentation(String state) {
        return stateConfiguration.computeIfAbsent(state, k -> new StateRepresentation(state));
    }


    /**
     * Begin configuration of the entry/exit actions and allowed transitions
     * when the state machine is in a particular state
     *
     * @param state The state to configure
     * @return A configuration object through which the state can be configured
     */
    private StateConfiguration configure(String state) {
        return new StateConfiguration(getOrCreateRepresentation(state));
    }

    /**
     * @return The initial state of this machine
     */
    public String getInitState() {
        return initState;
    }

    public void generateDotFileInto(final OutputStream dotFile) throws IOException {
        try (OutputStreamWriter w = new OutputStreamWriter(dotFile, "UTF-8")) {
            PrintWriter writer = new PrintWriter(w);
            writer.write("digraph G {\n");
            for (Map.Entry<String, StateRepresentation> entry : this.stateConfiguration.entrySet()) {
                Map<String, List<TriggerBehaviour>> behaviours = entry.getValue().getTriggerBehaviours();
                for (Map.Entry<String, List<TriggerBehaviour>> behaviour : behaviours.entrySet()) {
                    for (TriggerBehaviour triggerBehaviour : behaviour.getValue()) {
                        if (triggerBehaviour instanceof TriggerBehaviour.TransitioningTriggerBehaviour) {
                            Map.Entry<Boolean, String> destinationTuple = triggerBehaviour.resultsInTransitionFrom(null, null);
                            writer.write(String.format("\t%s -> %s;\n", entry.getKey(), destinationTuple.getValue()));
                        }
                    }
                }
            }
            writer.write("}");
        }
    }

    /**
     * Builder for creating a finite state machine
     *
     * @param initState the initial state of the FSM
     * @return
     */
    public static Builder newBuilder(String initState) {
        return new Builder(new StateMachineConfig(initState));
    }

    public static class Builder {


        private final StateMachineConfig config;

        private Builder(StateMachineConfig config) {
            this.config = config;
        }

        /**
         * Begin configuration of the entry/exit actions and allowed transitions
         * when the state machine is in a particular state
         *
         * @param state The state to configure
         * @return A configuration object through which the state can be configured
         */
        public StateConfiguration configure(String state) {
            return config.configure(state);
        }

        /**
         * Returns the state machine configuration
         *
         * @return
         */
        public StateMachineConfig build() {
            return config;
        }
    }
}

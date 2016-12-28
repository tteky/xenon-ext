package com.tteky.xenonext.fsm;

import com.tteky.xenonext.fsm.core.StateMachine;
import com.tteky.xenonext.fsm.core.StateMachineConfig;
import com.tteky.xenonext.fsm.core.Transition;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.StatefulService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;


/**
 * Finite state machine as a service and modeling behaviour as transitions between a finite set of states
 * <p>
 * Triggers can be issued to a document (current state of the FSM) using patch. This is a simplified
 * version of https://github.com/oxo42/stateless4j tailored to Xenon
 * <p>
 */
public abstract class FSMService<D extends FSMServiceDoc> extends StatefulService {

    private Logger log = LoggerFactory.getLogger(getClass());
    private Class<D> svcDocClass;
    private StateMachine stateMachine;

    protected FSMService(Class<D> svcDocClass) {
        super(svcDocClass);
        this.svcDocClass = svcDocClass;
        this.stateMachine = new StateMachine(stateMachineConfig());
        super.toggleOption(ServiceOption.PERSISTENCE, true);
        super.toggleOption(ServiceOption.REPLICATION, true);
        super.toggleOption(ServiceOption.OWNER_SELECTION, true);
        super.toggleOption(ServiceOption.INSTRUMENTATION, true);
        super.toggleOption(ServiceOption.PERIODIC_MAINTENANCE, true);

    }

    /**
     * This handles the initial {@code POST} that creates the FSM service. Most subclasses won't
     * need to override this method, although they have to override the {@link #stateMachineConfig()}
     * to define the state machine , {@link #validateStartPost(Operation)} to validate the body
     * and {@link #initialTrigger(Operation)} method
     */
    @Override
    public void handleStart(Operation op) {
        if (validateStartPost(op)) {
            D body = getBody(op);
            String initState = Objects.toString(stateMachine.initState(), "Start");
            if (body.state == null) { // initialize state to the initial state
                body.state = initState;
            }
            op.complete();
            if (Objects.equals(Objects.toString(body.state), initState)) {  // trigger is only for self path and do if current state is start state
                initialTrigger(op).ifPresent(t -> {
                    sendSelfPatch(body, t);
                    log.info("Sending self-patch for trigger {}", body.trigger);
                });
            }
        } else if (op.getStatusCode() == HTTP_OK) {
            op.setStatusCode(HTTP_BAD_REQUEST);
            log.debug("validation of start post failed for {}", op.getBodyRaw());
            op.fail(new IllegalArgumentException("Body not valid"));
        } else {
            log.debug("Handle start failure handled by client");
        }
    }


    /**
     * This handles the patch request of the state-machine. Transition from the current state is specified via trigger field.
     * The target state is determined by the configuration of the current state.
     * Actions associated with leaving the current state will be invoked before completing the operation
     * and entering the new state will be invoked after completing the operation
     * <p>
     * Most sub-classes wouldn't require to override this method. Can override to handle custom patch request other than FSM.
     *
     * @param patch the operation responsible for the trigger
     */
    @Override
    public void handlePatch(Operation patch) {
        D currentState = getState(patch);
        D patchBody = patch.getBody(svcDocClass);
        String src = currentState.state;
        String trigger = patchBody.trigger;
        patchBody.state = Optional.ofNullable(patchBody.state).orElse(src);
        // validate trigger
        if (trigger == null) {
            patch.setStatusCode(HTTP_BAD_REQUEST);
            patch.fail(new IllegalArgumentException(" Trigger can't be null"));
        } else if (!Objects.equals(patchBody.state, src)) {
            patch.setStatusCode(HTTP_BAD_REQUEST);
            patch.fail(new IllegalArgumentException(" Patched state " + patchBody.state + " is different from actual state " + src));
        } else {
            stateMachine.setExpiryScheduler(new ExpiryScheduler(this, patchBody.documentSelfLink, svcDocClass));
            stateMachine.fireTrigger(src, trigger, patch, this::statePopulator);
        }
    }

    /**
     * Set the FSM state as appropriate
     *
     * @param transition
     */
    protected void statePopulator(Transition transition) {
        FSMServiceDoc curState = getState(transition.getCause());
        curState.trigger = transition.getTrigger();
        curState.state = transition.getDestination();
        setState(transition.getCause(), curState);
    }


    /**
     * Completes the operation. Fall back option, shouldn't be used ideally
     *
     * @param transition
     */
    protected void completeOperation(Transition transition) {
        transition.getCause().complete();
    }


    /**
     * Send ourselves a PATCH. The caller is responsible for creating the functional PATCH body
     */
    protected void sendSelfPatch(D body, String trigger) {
        body.trigger = trigger;
        Operation patch = Operation.createPatch(getUri())
                .setBody(body);
        sendRequest(patch);
    }

    /**
     * Create a new state machine config.
     * Note that,
     * 1. Start state shouldn't have on-entry handler
     * 2. Transition can be triggered through a patch operation by providing the trigger name in body
     * 3. onExit handler of the current state will be invoked before completing the causative operation (trigger) by default
     * 4. onEntry handler of the destination state will be invoked after completing the causative operation (trigger) by default
     * 5. Operation completion needs to be taken care in one of the handler
     *
     * @return fully configured (non-mutable) StateMachineConfig
     */
    protected abstract StateMachineConfig stateMachineConfig();


    /**
     * Should validate the input and return true or false.
     * State machine trigger validation need not be handled in this method
     *
     * @param postOp causative operation
     * @return result of validation. If false, operation will be failed.
     */
    protected boolean validateStartPost(Operation postOp) {
        return true;
    }

    /**
     * Initiate the progress of the state-machine by providing the initial trigger to be fired as a self patch
     *
     * @param postOp
     * @return
     */
    protected Optional<String> initialTrigger(Operation postOp) {
        return Optional.empty();
    }

}

package com.tteky.xenonext.fsm;

import com.tteky.xenonext.fsm.core.StateMachineConfig;
import com.tteky.xenonext.fsm.core.Transition;
import com.vmware.xenon.common.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Created by mageshwaranr on 02-Dec-16.
 */
public class PhoneService extends FSMService<PhoneService.PhoneSvcDoc> {

    public static final String FACTORY_LINK = "/vrbc/common/fsm/examples/phone";

    private Logger log = LoggerFactory.getLogger(getClass());

    public PhoneService() {
        super(PhoneSvcDoc.class);
        super.toggleOption(ServiceOption.INSTRUMENTATION, true);
        super.toggleOption(ServiceOption.PERSISTENCE, true);
    }

    public enum PhoneState {
        Ringing, Connected, OnHold, OffHook

    }

    public enum PhoneTrigger {
        CallDialed, CallConnected, PlacedOnHold, LeftMessage, HungUp
    }

    public static class PhoneSvcDoc extends FSMServiceDoc {
        public String initMessage;

        public boolean onExitOfOffHook, onEntryOfRinging, onExitOfRinging, onEntryOfConnected,
                onExitOfConnected, onEntryOfOnHold;

        @Override
        public String toString() {
            return "PhoneSvcDoc{" +
                    "state='" + state + '\'' +
                    ", trigger='" + trigger + '\'' +
                    '}';
        }
    }


    public StateMachineConfig stateMachineConfig() {

        StateMachineConfig.Builder phoneCallConfig = StateMachineConfig.newBuilder(PhoneState.OffHook.name());

        phoneCallConfig.configure(PhoneState.OffHook.name())
                .permit(PhoneTrigger.CallDialed.name(), PhoneState.Ringing.name())
                .onExit(transition -> {
                    PhoneSvcDoc state = getState(transition.getCause());
                    state.onExitOfOffHook = true;
                    setState(transition.getCause(), state);
                    log.info("on Exit of OffHook");
                })
                .onExit(this::completeOperation);

        phoneCallConfig.configure(PhoneState.Ringing.name())
                .onEntry(transition -> {
                    PhoneSvcDoc state = getState(transition.getCause());
                    state.onEntryOfRinging = true;
                    setState(transition.getCause(), state);
                    log.info("on Entry of Ringing");
                })
                .onExit(transition -> {
                    PhoneSvcDoc state = getState(transition.getCause());
                    state.onExitOfRinging = true;
                    setState(transition.getCause(), state);
                    log.info("on Exit of Ringing");
                })
                .permit(PhoneTrigger.HungUp.name(), PhoneState.OffHook.name())
                .permit(PhoneTrigger.CallConnected.name(), PhoneState.Connected.name());

        phoneCallConfig.configure(PhoneState.Connected.name())
                .onEntry(transition -> {
                    PhoneSvcDoc state = getState(transition.getCause());
                    state.onEntryOfConnected = true;
                    setState(transition.getCause(), state);
                    log.info("on Entry of Connected");
                })
                .onEntryFrom(PhoneTrigger.CallConnected.name(), this::completeOperation)
                .onExit(transition -> {
                    PhoneSvcDoc state = getState(transition.getCause());
                    state.onExitOfConnected = true;
                    setState(transition.getCause(), state);
                    log.info("on Exit of Connected");
                })
                .onExit(this::completeOperation)
                .onExpiry(PhoneTrigger.HungUp.name(), 30000)
                .permit(PhoneTrigger.LeftMessage.name(), PhoneState.OffHook.name())
                .permit(PhoneTrigger.HungUp.name(), PhoneState.OffHook.name())
                .permit(PhoneTrigger.PlacedOnHold.name(), PhoneState.OnHold.name());

        phoneCallConfig.configure(PhoneState.OnHold.name())
                .onEntry(transition -> {
                    PhoneSvcDoc state = getState(transition.getCause());
                    state.onEntryOfOnHold = true;
                    setState(transition.getCause(), state);
                    log.info("on Entry of OnHold");
                })
                .onExpiry(PhoneTrigger.HungUp.name(), 300)
                .permit(PhoneTrigger.HungUp.name(), PhoneState.OffHook.name())
                .permit(PhoneTrigger.LeftMessage.name(), PhoneState.OffHook.name());


        return phoneCallConfig.build();
    }

    @Override
    public void handlePatch(Operation patch) {
        log.info("Patch Received for {}", patch.getBodyRaw());
        super.handlePatch(patch);
    }

    @Override
    protected void completeOperation(Transition transition) {
        super.completeOperation(transition);
        log.info("Operation completed");
    }

    @Override
    protected boolean validateStartPost(Operation postOp) {
        PhoneSvcDoc body = postOp.getBody(PhoneSvcDoc.class);
        return body.initMessage != null;
    }


    @Override
    protected Optional<String> initialTrigger(Operation postOp) {
        return Optional.of(PhoneTrigger.CallDialed.name());
    }


}

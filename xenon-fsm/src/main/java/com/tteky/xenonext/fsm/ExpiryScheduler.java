package com.tteky.xenonext.fsm;

import com.tteky.xenonext.fsm.core.Transition;
import com.vmware.xenon.common.Operation;
import com.vmware.xenon.common.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static com.vmware.xenon.common.UriUtils.extendUri;

/**
 * Schedules the expiry of an state using ServiceHost scheduler
 */
public class ExpiryScheduler implements BiConsumer<Map<String, Long>, Transition> {

    private final Service svc;
    private final String selfLink;
    private final Class<? extends FSMServiceDoc> svcDocClass;
    private Logger log = LoggerFactory.getLogger(getClass());

    public ExpiryScheduler(Service svc, String selfLink, Class<? extends FSMServiceDoc> svcDocClass) {
        this.svc = svc;
        this.selfLink = selfLink;
        this.svcDocClass = svcDocClass;
    }

    @Override
    public void accept(Map<String, Long> triggerAndDelays, Transition transition) {
        FSMServiceDoc curState = svc.getState(transition.getCause());
        curState.expireFromState = transition.getDestination();
        curState.expiryTriggerAndDelays = triggerAndDelays;
        svc.setState(transition.getCause(), curState);

        triggerAndDelays.forEach((expiryTrigger, delay) -> {
            svc.getHost().getScheduledExecutor().schedule(() -> {
                log.info("Triggering {} on {}", expiryTrigger, selfLink);
                Operation.createGet(svc.getHost(), selfLink)
                        .setReferer(transition.getCause().getReferer())
                        .setCompletion((completedOp, failure) -> {
                            FSMServiceDoc body = completedOp.getBody(svcDocClass);
                            body.trigger = expiryTrigger;
                            body.state = transition.getDestination();
                            svc.getHost().sendRequest(Operation
                                    .createPatch(extendUri(svc.getHost().getUri(), selfLink))
                                    .setBody(body)
                                    .setReferer(transition.getCause().getReferer())
                            );
                        })
                        .sendWith(svc.getHost());

            }, delay, TimeUnit.MILLISECONDS);
            log.info("Scheduled expiry handlers for trigger {} with id {}", expiryTrigger, selfLink);
        });


    }
}

## State machine for Xenon

Inspired by stateless and Xenon task service, this module provides ability to define state machine in java code and attach handlers to it.
State machine is modeled as a stateful service. On each trigger, handlers attached to the exit of the current state and entry of the next state are invoked. 
These handlers encapsulates the business logic required.

The state machine moves from one state to another when a patch request is issued to the service.  Based on the trigger 


#### Features
Provides ability to 
1. Attach on-entry & on-exit handlers on each state
2. Attach expiry based auto-transition
3. Basic UI to visualize the state machine diagram

#### Usage
Assumes basic knowledge about Xenon stateful service
1. Service should extend FSMService service
2. Can define its own service document, but should extend FSMServiceDoc
3. Implement stateMachineConfig() method to define the state machine
4. Add validation of initial payload by overriding validateStartPost method, if needed
5. Add initial auto-transition that should happen when a new document is created by overriding initialTrigger method, if requried
6. In order to use the UI, you have to start two more services FSMStatsServiceImpl and FSMUIService and register your FSM service with FSMStatsServiceImpl 

**Note**: The state machine config is not persisted. Only the current state and trigger is persisted.

Example :

```java
public class SampleService extends FSMService<ChildOfFSMServiceDoc> {

    public static final String FACTORY_LINK = "/vrbc/common/fsm/examples/phone";

    private Logger log = LoggerFactory.getLogger(getClass());

    public SampleService() {
        super(ChildOfFSMServiceDoc.class);
        super.toggleOption(ServiceOption.INSTRUMENTATION, true);
        super.toggleOption(ServiceOption.PERSISTENCE, true);
    }

    public StateMachineConfig stateMachineConfig() {

        StateMachineConfig.Builder sampleConfig = StateMachineConfig.newBuilder(PhoneState.OffHook.name());

        sampleConfig.configure("InitialState")
                .permit("Validated", "NextState")
                .permit("Trigger_ValidB", "ResultOfB")
                .onExit(transition -> {
                    // do something and then
                })
                .onExit(this::completeOperation);

        sampleConfig.configure("NextState")
                .permit("TriggerA", "ResultOfA")
                .permit("TriggerB", "ResultOfB")
                .onEntry(transition -> {
                  // do something
                })
                .onExit(transition -> {
                    // do something
                    // make sure you complete operation somewhere
                }).onExit(this::completeOperation);

        sampleConfig.configure("ResultOfB")
                .permit("onExpiry", "ExpiredState") // add other permits
                .onEntryFrom("TriggerB", transition -> {
                    // do something if the trigger is only TriggerB and not for Trigger_ValidB 
                })
                .onExit(transition -> {
                   // do something then
                })
                .onExit(this::completeOperation)
                .onExpiry("onExpiry", 30000); // on expiry, trigger "onExpiry"
        // other required configs
        return sampleConfig.build();
    }
    
    @Override
    protected boolean validateStartPost(Operation postOp) {
       // do you validation, invoked only for post
        return true;
    }
    
    @Override
    protected Optional<String> initialTrigger(Operation postOp) {
       // initial trigger if provided, after service doc creation
       // a patch will be triggered
    }
}
```


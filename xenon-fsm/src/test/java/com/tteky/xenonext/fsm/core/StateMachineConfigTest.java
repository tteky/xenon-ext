package com.tteky.xenonext.fsm.core;

import com.tteky.xenonext.fsm.FSMServiceDoc;
import com.tteky.xenonext.fsm.PhoneService;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 * Created by mages_000 on 30-Dec-16.
 */
public class StateMachineConfigTest {

    private StateMachineConfig stateMachineConfig;

    @Before
    public void init(){
        PhoneService svc = new PhoneService();
        stateMachineConfig = svc.stateMachineConfig();
    }

    @Test
    public void generateDotFileInto() throws Exception {
        String graphson = stateMachineConfig.generateDotFile();
        assertThat(graphson, CoreMatchers.containsString("OffHook -> Ringing"));
        assertThat(graphson, CoreMatchers.containsString("Ringing -> OffHook"));
        assertThat(graphson, CoreMatchers.containsString("Ringing -> Connected"));
        assertThat(graphson, CoreMatchers.containsString("Connected -> OffHook"));
        assertThat(graphson, CoreMatchers.containsString("Connected -> OnHold"));
        assertThat(graphson, CoreMatchers.containsString("OnHold -> OffHook"));
        System.out.println(graphson);
    }

    @Test
    public void generateDocFile() throws Exception {
        FSMServiceDoc v0 = newDoc("OffHook", null, 0);
        FSMServiceDoc v1 = newDoc("Ringing", "CallDialed", 1);
        FSMServiceDoc v2 = newDoc("Connected", "CallConnected", 2);
        FSMServiceDoc v3 = newDoc("OnHold", "PlacedOnHold", 3);
        String graphson = stateMachineConfig.generateDotFile(new FSMServiceDoc[]{v2,v1,v0,v3});
        assertThat(graphson, CoreMatchers.containsString("OffHook -> Ringing"));
        assertThat(graphson, CoreMatchers.containsString("Ringing -> Connected"));
        assertThat(graphson, CoreMatchers.containsString("Connected -> OnHold"));
        System.out.println(graphson);
    }

    private FSMServiceDoc newDoc(String state,String trigger, long version){
        FSMServiceDoc doc = new FSMServiceDoc();
        doc.state = state;
        doc.trigger = trigger;
        doc.documentVersion = version;
        return doc;
    }

}
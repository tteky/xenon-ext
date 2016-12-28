package com.tteky.xenonext.fsm;

import com.tteky.xenonext.client.StatefulServiceContract;
import com.vmware.xenon.common.BasicReusableHostTestCase;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.function.Predicate;

import static com.tteky.xenonext.client.ServiceClientUtil.newStatefulSvcContract;
import static com.tteky.xenonext.fsm.PhoneService.FACTORY_LINK;
import static com.tteky.xenonext.fsm.PhoneService.PhoneState.Connected;
import static com.tteky.xenonext.fsm.PhoneService.PhoneState.Ringing;
import static com.tteky.xenonext.fsm.PhoneService.PhoneSvcDoc;
import static com.tteky.xenonext.fsm.PhoneService.PhoneTrigger.*;
import static org.junit.Assert.*;

/**
 * Created by mageshwaranr on 08-Dec-16.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FSMServiceIntegrationTest extends BasicReusableHostTestCase {

    private static StatefulServiceContract<PhoneSvcDoc> testSvc;

    @Before
    public void init() throws Throwable {
        if (testSvc == null) {
            testSvc = newStatefulSvcContract(host, FACTORY_LINK, PhoneSvcDoc.class);
            host.startFactory(new PhoneService());
            host.waitForServiceAvailable();
        }
    }


    @Test(expected = Exception.class)
    public void onStart_ValidateStartPost_Invoked() throws Throwable {
        PhoneSvcDoc body = new PhoneSvcDoc();
        testSvc.post(body).get();
    }

    @Test
    public void onStart_InitialState_Validated() throws Throwable {
        PhoneSvcDoc body = new PhoneSvcDoc();
        body.initMessage = "Valid message";
        body.state = Ringing.name();
        body = testSvc.post(body).get();
        body = testSvc.getBySelfLink(body.documentSelfLink).get();
        assertNull(body.trigger);
    }

    @Test
    public void onStart_InitialTrigger_Invoked() throws Throwable {
        PhoneSvcDoc body = new PhoneSvcDoc();
        body.initMessage = "Valid message";
        PhoneSvcDoc phoneSvcDoc = testSvc.post(body).get();
        phoneSvcDoc = getDocMeetingCriteria(phoneSvcDoc, doc -> CallDialed.name().equals(doc.trigger));
        assertEquals(CallDialed.name(), phoneSvcDoc.trigger);
    }

    @Test
    public void verifyCallDialledTransition() throws Throwable {
        PhoneSvcDoc body = new PhoneSvcDoc();
        body.initMessage = "Valid message";
        PhoneSvcDoc phoneSvcDoc = testSvc.post(body).get();
        phoneSvcDoc = getDocMeetingCriteria(phoneSvcDoc, doc -> Ringing.name().equals(doc.state));
        assertEquals(CallDialed.name(), phoneSvcDoc.trigger);
        assertEquals(Ringing.name(), phoneSvcDoc.state);
        assertTrue(phoneSvcDoc.onExitOfOffHook);
        assertEquals(Ringing.name(), phoneSvcDoc.state);
    }

    @Test
    public void verifyCallConnectedTransition() throws Throwable {
        PhoneSvcDoc body = new PhoneSvcDoc();
        body.initMessage = "Valid message";
        PhoneSvcDoc phoneSvcDoc = testSvc.post(body).get();

        body = new PhoneSvcDoc();
        body.trigger = CallConnected.name();
        testSvc.patchBySelfLink(phoneSvcDoc.documentSelfLink, body).get();
        phoneSvcDoc = getDocMeetingCriteria(phoneSvcDoc, doc -> Connected.name().equals(doc.state));
        assertEquals(CallConnected.name(), phoneSvcDoc.trigger);
        assertTrue(phoneSvcDoc.onExitOfRinging);
        assertTrue(phoneSvcDoc.onEntryOfConnected);
        assertEquals(Connected.name(), phoneSvcDoc.state);
        assertEquals(Connected.name(), phoneSvcDoc.expireFromState);
        assertEquals(Long.valueOf(30000), phoneSvcDoc.expiryTriggerAndDelays.get(HungUp.name()));
    }

    private PhoneSvcDoc getDocMeetingCriteria(PhoneSvcDoc in, Predicate<PhoneSvcDoc> criteria) throws Throwable {
        PhoneSvcDoc out = null;
        // state transition happens asynchronously. So retry up to max count 10 with smaller slip
        for (int i = 0; i < 10; i++) {
            out = testSvc.getBySelfLink(in.documentSelfLink).get();
            if (criteria.test(out)) {
                break;
            }
            Thread.sleep(10);
        }
        return out;
    }

//    @Test
//    public void testValidateInitState() throws Throwable {
//        PhoneSvcDoc body = new PhoneSvcDoc();
//        PhoneSvcDoc phoneSvcDoc = testSvc.post(body).get();
//        assertEquals(OffHook.name(), phoneSvcDoc.state);
//        assertNull(phoneSvcDoc.trigger);
//        body.state = OffHook.name();
//        phoneSvcDoc = testSvc.post(body).get();
//        assertEquals(OffHook.name(), phoneSvcDoc.state);
//        assertNull(phoneSvcDoc.trigger);
//    }


//    @Test
//    public void defaultInitState_ShouldDoSelfPatch() throws Throwable {
//        PhoneSvcDoc body = new PhoneSvcDoc();
//        PhoneService.PhoneSvcDoc phoneSvcDoc = testSvc.post(body);
//
//        // state transition happens asynchronously. So retry up to max count 10
//        for (int i = 0; i < 10; i++) {
//            phoneSvcDoc = testSvc.getById(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink));
//            if (PhoneState.Connected == phoneSvcDoc.state) {
//                break;
//            }
//            Thread.sleep(10);
//        }
//        System.out.println(toJson(phoneSvcDoc));
//        assertEquals(PhoneState.Connected, phoneSvcDoc.state);
//        assertEquals(CallConnected, phoneSvcDoc.trigger);
//        assertTrue(phoneSvcDoc.onExitOfOffHook);
//        assertTrue(phoneSvcDoc.onExitOfRinging);
//        assertFalse(phoneSvcDoc.onEntryOfOffHook);
//        assertFalse(phoneSvcDoc.onEntryOfConnected);
//        assertFalse(phoneSvcDoc.onEntryOfRinging);
//    }
//
//
//    @Test
//    public void defaultInitState_ShouldDoSelfPatchOnlyIfInStartState() throws Throwable {
//        PhoneSvcDoc body = new PhoneSvcDoc();
//        body.state = Ringing;
//        PhoneService.PhoneSvcDoc phoneSvcDoc = testSvc.post(body);
//        phoneSvcDoc = testSvc.getById(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink));
//        assertEquals(body.state, phoneSvcDoc.state);
//    }
//
//    @Test
//    public void testTransition() throws Throwable {
//        PhoneSvcDoc body = new PhoneSvcDoc();
//        body.state = Ringing;
//        PhoneService.PhoneSvcDoc phoneSvcDoc = testSvc.post(body);
//
//        phoneSvcDoc = testSvc.getById(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink));
//        assertEquals(body.state, phoneSvcDoc.state);
//
//        body.trigger = CallConnected;
//        phoneSvcDoc = testSvc.patch(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink), body);
//        assertEquals(PhoneState.Connected, phoneSvcDoc.state);
//
//        phoneSvcDoc = testSvc.getById(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink));
//        assertEquals(PhoneState.Connected, phoneSvcDoc.state);
//        assertTrue(phoneSvcDoc.onExitOfRinging);
//        assertFalse(phoneSvcDoc.onEntryOfConnected);
//    }
//
//    @Test(expected = VrbcHttpError.class)
//    public void testTransition_IncorrectTrigger() throws Throwable {
//        PhoneSvcDoc body = new PhoneSvcDoc();
//        body.state = Ringing;
//        PhoneService.PhoneSvcDoc phoneSvcDoc = testSvc.post(body);
//
//        phoneSvcDoc = testSvc.getById(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink));
//        assertEquals(body.state, phoneSvcDoc.state);
//
//        body.trigger = PhoneTrigger.PlacedOnHold;
//        testSvc.patch(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink), body);
//
//    }
//
//
//    @Test(expected = VrbcHttpError.class)
//    public void testTransition_NoTrigger() throws Throwable {
//        PhoneSvcDoc body = new PhoneSvcDoc();
//        body.state = Ringing;
//        PhoneService.PhoneSvcDoc phoneSvcDoc = testSvc.post(body);
//
//        phoneSvcDoc = testSvc.getById(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink));
//        assertEquals(body.state, phoneSvcDoc.state);
//
//        body.trigger = null;
//        testSvc.patch(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink), body);
//
//    }
//
//    @Test(expected = VrbcHttpError.class)
//    public void testTransition_IncorrectState() throws Throwable {
//        PhoneSvcDoc body = new PhoneSvcDoc();
//        body.state = Ringing;
//        PhoneService.PhoneSvcDoc phoneSvcDoc = testSvc.post(body);
//
//        phoneSvcDoc = testSvc.getById(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink));
//        assertEquals(body.state, phoneSvcDoc.state);
//
//        body.state = PhoneState.Connected;
//        body.trigger = PhoneTrigger.PlacedOnHold;
//        testSvc.patch(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink), body);
//
//    }
//
//
//    // very bad practice of re-using data across junit methods. should refactor this
//    private static String selfLink;
//
//    @Test
//    public void testExpiry_StateIsUpdated() throws Throwable {
//        PhoneSvcDoc body = new PhoneSvcDoc();
//        body.state = Ringing;
//        PhoneService.PhoneSvcDoc phoneSvcDoc = testSvc.post(body);
//        assertEquals(body.state, phoneSvcDoc.state);
//        assertNull(phoneSvcDoc.expiryTrigger);
//        assertTrue(phoneSvcDoc.documentExpirationTimeMicros == 0);
//
//        body.trigger = CallConnected;
//        phoneSvcDoc = testSvc.patch(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink), body);
//        assertEquals(Connected, phoneSvcDoc.state);
//        assertNotNull(phoneSvcDoc.expiryTrigger);
//        assertEquals(HungUp, phoneSvcDoc.expiryTrigger);
//        assertEquals(300, phoneSvcDoc.expiryDuration);
//        assertEquals(Connected, phoneSvcDoc.expiryFromState);
//        assertTrue(phoneSvcDoc.documentExpirationTimeMicros == 0);
//        selfLink = phoneSvcDoc.documentSelfLink;
////    Thread.sleep(1000);
////    // expiry is an async operation. Wait for the expiry action to trigger
////    for (int i =0 ; i < 10; i++){
////      phoneSvcDoc = testSvc.getById(SVC_INFO.selfLinkToDocId(phoneSvcDoc.documentSelfLink));
////      if(phoneSvcDoc.state != Connected){
////        break;
////      }
////    }
////    System.out.println("Final state is "+toJson(phoneSvcDoc));
////    assertEquals(OffHook, phoneSvcDoc.state);
////    assertEquals(HungUp, phoneSvcDoc.trigger);
//    }
//
//    @Test
//    public void testExpiry_VerifyExecution() throws Throwable {
//        PhoneSvcDoc phoneSvcDoc = null;
//        // expiry is an async operation. Wait for the expiry action to trigger
//        for (int i = 0; i < 10; i++) {
//            phoneSvcDoc = testSvc.getById(SVC_INFO.selfLinkToDocId(selfLink));
//            if (phoneSvcDoc.state != Connected) {
//                break;
//            }
//            Thread.sleep(100);
//        }
//        System.out.println("Final state is " + toJson(phoneSvcDoc));
//        assertEquals(OffHook, phoneSvcDoc.state);
//        assertEquals(HungUp, phoneSvcDoc.trigger);
//    }
//
//
//    @AfterClass
//    public static void stopHost() {
//        host.stop();
//    }


}
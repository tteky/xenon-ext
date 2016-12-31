package com.tteky.xenonext.fsm.stats;

import com.vmware.xenon.ui.UiService;

/**
 * UI host FSM webpages
 */
public class FSMUIService extends UiService {
    public static final String SELF_LINK = "/fsm/gui";

    public FSMUIService() {
        super.toggleOption(ServiceOption.CONCURRENT_GET_HANDLING, true);
        super.toggleOption(ServiceOption.HTML_USER_INTERFACE, true);
    }


}

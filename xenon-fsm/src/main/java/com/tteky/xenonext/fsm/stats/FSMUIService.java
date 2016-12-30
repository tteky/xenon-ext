package com.tteky.xenonext.fsm.stats;

import com.vmware.xenon.ui.UiService;

/**
 * Created by mages_000 on 29-Dec-16.
 */
public class FSMUIService extends UiService {
    public static final String SELF_LINK = "/fsm/gui";

    public FSMUIService() {
        super.toggleOption(ServiceOption.CONCURRENT_GET_HANDLING, true);
        super.toggleOption(ServiceOption.HTML_USER_INTERFACE, true);
    }


}

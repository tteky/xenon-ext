package com.tteky.xenonext.fsm;

import com.tteky.xenonext.fsm.stats.FSMStatsServiceImpl;
import com.tteky.xenonext.fsm.stats.FSMUIService;
import com.vmware.xenon.common.ServiceHost;

/**
 * Utilities related to FSM service
 */
public class FSMServices {

    public static FSMStatsServiceImpl startUtilityService(ServiceHost host){
        FSMStatsServiceImpl service = new FSMStatsServiceImpl();
        host.startService(service);
        host.startService(new FSMUIService());
        return service;
    }

}

package com.tteky.xenonext.fsm;

import com.tteky.xenonext.fsm.stats.FSMStatsServiceImpl;
import com.tteky.xenonext.fsm.stats.FSMUIService;
import com.vmware.xenon.services.common.ExampleServiceHost;
import com.vmware.xenon.services.common.RootNamespaceService;
import com.vmware.xenon.swagger.SwaggerDescriptorService;
import com.vmware.xenon.ui.UiService;
import io.swagger.models.Info;

/**
 * Created by mages_000 on 29-Dec-16.
 */
public class FSMHost {

    public static void main(String[] args) throws Throwable {
        ExampleServiceHost host = new ExampleServiceHost();
        host.initialize(args);
        host.start();
        host.startService(new RootNamespaceService());
        host.startService(new UiService());
        SwaggerDescriptorService swagger = new SwaggerDescriptorService();
        Info apiInfo = new Info();
        apiInfo.setVersion("1.0.0");
        apiInfo.setTitle("Vrbc Xenon Host");
        swagger.setInfo(apiInfo);
        host.startService(swagger);
        host.startFactory(new PhoneService());
        FSMStatsServiceImpl service = new FSMStatsServiceImpl();
        service.registerSvc(PhoneService.FACTORY_LINK, PhoneService.class);
        host.startService(service);
        host.startService(new FSMUIService());
    }
}

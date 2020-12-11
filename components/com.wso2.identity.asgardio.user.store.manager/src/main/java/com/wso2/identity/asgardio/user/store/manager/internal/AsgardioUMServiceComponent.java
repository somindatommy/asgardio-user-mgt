/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.identity.asgardio.user.store.manager.internal;

import com.wso2.identity.asgardio.user.store.manager.AsgardioUserStoreManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.user.api.UserStoreManager;

/**
 * This class contains the service component of the AsgardioUserStoreManager.
 */
@Component(
        name = "com.wso2.identity.asgardio.user.store.manager..component",
        immediate = true
)
public class AsgardioUMServiceComponent {

    private static Log log = LogFactory.getLog(AsgardioUMServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctxt) {

        AsgardioUserStoreManager asgardioUserStoreManager = new AsgardioUserStoreManager();
        ctxt.getBundleContext().registerService(UserStoreManager.class.getName(), asgardioUserStoreManager, null);
        log.info("AsgardioUserStoreManager bundle activated successfully.");
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("AsgardioUserStoreManager bundle is deactivated.");
        }
    }
}

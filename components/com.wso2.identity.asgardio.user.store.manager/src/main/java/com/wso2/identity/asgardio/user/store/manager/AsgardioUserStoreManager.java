/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.identity.asgardio.user.store.manager;

import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;

import java.util.Map;
import javax.sql.DataSource;

/**
 * This class contains the implementation of the user store manager Asgardio.
 */
public class AsgardioUserStoreManager extends UniqueIDJDBCUserStoreManager {

    public AsgardioUserStoreManager() {

    }

    public AsgardioUserStoreManager(RealmConfiguration realmConfig, int tenantId) throws UserStoreException {

        super(realmConfig, tenantId);
    }

    public AsgardioUserStoreManager(DataSource ds, RealmConfiguration realmConfig, int tenantId,
                                    boolean addInitData) throws UserStoreException {

        super(ds, realmConfig, tenantId, addInitData);
    }

    public AsgardioUserStoreManager(DataSource ds, RealmConfiguration realmConfig) throws UserStoreException {

        super(ds, realmConfig);
    }

    public AsgardioUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
                                    ClaimManager claimManager, ProfileConfigurationManager profileManager,
                                    UserRealm realm, Integer tenantId)
            throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId);
    }

    public AsgardioUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
                                    ClaimManager claimManager, ProfileConfigurationManager profileManager,
                                    UserRealm realm, Integer tenantId, boolean skipInitData) throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, skipInitData);
    }
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.identity.asgardio.tenant.association.mgt.constants;

/**
 * This class contains the constants.
 */
public class Constants {

    public static final String USER_TENANT_ASSOCIATION_ERROR_PREFIX = "UTA-";

    /**
     * This class contains the sql constants.
     */
    public static class SQLConstants {

        public static final String GET_ASC_TYPE_BY_TENANT_ID_USER_ID_SQL =
                "SELECT ASG_ASSOCIATION FROM ASG_USER_TENANT_ASC WHERE ASG_USER_UUID=? AND ASG_TENANT_UUID=?";
    }
}

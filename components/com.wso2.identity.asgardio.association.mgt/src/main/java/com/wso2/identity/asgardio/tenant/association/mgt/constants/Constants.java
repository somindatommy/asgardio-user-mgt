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
        public static final String ADD_USER_TENANT_ASC_SQL =
                "INSERT INTO ASG_USER_TENANT_ASC (ASG_USER_UUID, ASG_TENANT_UUID, ASG_ASSOCIATION) VALUES (?, ?, ?)";
        public static final String UPDATE_USER_TENANT_ASC_SQL =
                "UPDATE ASG_USER_TENANT_ASC SET ASG_ASSOCIATION=? WHERE ASG_USER_UUID=? AND ASG_TENANT_UUID=?";
        public static final String DELETE_USER_TENANT_ASC_SQL =
                "DELETE FROM ASG_USER_TENANT_ASC WHERE ASG_USER_UUID=? AND ASG_TENANT_UUID=?";
        public static final String DELETE_TENANT_ASC_FOR_USER_SQL =
                "DELETE FROM ASG_USER_TENANT_ASC WHERE ASG_USER_UUID=?";
        public static final String DELETE_USER_ASC_FOR_TENANT_SQL =
                "DELETE FROM ASG_USER_TENANT_ASC WHERE ASG_TENANT_UUID=?";
        public static final String COUNT_ASC_TYPE_FOR_USER_IN_TENANT =
                "SELECT COUNT (ASG_USER_UUID) FROM ASG_USER_TENANT_ASC WHERE ASG_USER_UUID=? AND ASG_TENANT_UUID=? " +
                        "AND ASG_ASSOCIATION=?";

        // todo: we cannot do sorting by domain name.
        public static final String GET_USER_TENANT_ASC_BY_USER_ID_SQL =
                "SELECT ASG_TENANT_UUID, ASG_ASSOCIATION FROM ASG_USER_TENANT_ASC WHERE ASG_USER_UUID=? " +
                        "LIMIT ? OFFSET ?";
        public static final String GET_USER_TENANT_ASC_BY_USER_ID_AND_ASC_TYPE_SQL =
                "SELECT ASG_TENANT_UUID FROM ASG_USER_TENANT_ASC WHERE ASG_USER_UUID=? AND ASG_ASSOCIATION? " +
                        "LIMIT ? OFFSET ?";

        public static final String GET_USER_TENANT_ASC_BY_TENANT_ID_SQL =
                "SELECT P.UM_USER_NAME, AUTA.ASG_ASSOCIATION FROM UM_USER AS P " +
                        "INNER JOIN ASG_USER_TENANT_ASC AS AUTA ON P.UM_USER_ID=AUTA.ASG_USER_UUID " +
                        "WHERE AUTA.ASG_TENANT_UUID=? ORDER BY P.UM_USER_NAME ASC LIMIT ? OFFSET ?";
        public static final String GET_USER_TENANT_ASC_BY_TENANT_ID_AND_ASC_TYPE_SQL =
                "SELECT P.UM_USER_NAME FROM UM_USER AS P " +
                        "INNER JOIN ASG_USER_TENANT_ASC AS AUTA ON P.UM_USER_ID=AUTA.ASG_USER_UUID " +
                        "WHERE AUTA.ASG_TENANT_UUID=? AND AUTA.ASG_ASSOCIATION=?ORDER BY P.UM_USER_NAME ASC LIMIT " +
                        "10 OFFSET 0";
    }
}

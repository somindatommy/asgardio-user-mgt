/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.identity.asgardio.user.store.manager.constants;

/**
 * Class to define case sensitive sql constants.
 */
public class CaseSensitiveSQLConstants {

    // SQL Queries - Get Users.
    public static final String GET_IS_USER_NAME_EXISTING_CASE_SENSITIVE_SQL =
            "SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=?";
    public static final String SELECT_USER_ID_FROM_USER_NAME_CASE_SENSITIVE_SQL =
            "SELECT UM_USER_ID FROM UM_USER WHERE UM_USER_NAME=?";
    public static final String GET_USER_FILTER_WITH_UUID_CASE_SENSITIVE_SQL =
            "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM UM_USER U INNER JOIN " +
                    "(SELECT AUTA.ASG_USER_UUID FROM ASG_USER_TENANT_ASC AUTA WHERE AUTA.ASG_TENANT_UUID=?) P " +
                    "ON U.UM_USER_ID = P.ASG_USER_UUID WHERE U.UM_USER_NAME LIKE ? ORDER BY U.UM_USER_NAME";
    public static final String GET_USER_FILTER_WITH_ID_CASE_SENSITIVE_SQL =
            "SELECT UM_USER_ID, UM_USER_NAME FROM UM_USER WHERE UM_USER_NAME LIKE ? ORDER BY UM_USER_NAME";
    public static final String SELECT_USER_NAME_CASE_SENSITIVE_SQL =
            "SELECT U.UM_USER_ID, U.UM_USER_NAME, U.UM_USER_PASSWORD, U.UM_SALT_VALUE, U.UM_REQUIRE_CHANGE, " +
                    "U.UM_CHANGED_TIME FROM UM_USER U INNER JOIN ASG_USER_TENANT_ASC AUTA ON AUTA.ASG_USER_UUID=U" +
                    ".UM_USER_ID WHERE U.UM_USER_NAME=? AND AUTA.ASG_TENANT_UUID=?";
    public static final String COUNT_USERS_WITH_USERNAME_CASE_SENSITIVE_SQL =
            "SELECT COUNT(U.UM_USER_NAME) AS RESULT FROM UM_USER U INNER JOIN " +
                    "(SELECT AUTA.ASG_USER_UUID FROM ASG_USER_TENANT_ASC AUTA WHERE AUTA.ASG_TENANT_UUID=?) P " +
                    "ON P.ASG_USER_UUID=U.UM_USER_ID WHERE U.UM_USER_NAME LIKE ?";
    public static final String SELECT_USER_WITH_ID_SQL = "SELECT U.UM_USER_ID, U.UM_USER_NAME, " +
            "U.UM_USER_PASSWORD, U.UM_SALT_VALUE, U.UM_REQUIRE_CHANGE, U.UM_CHANGED_TIME " +
            "FROM UM_USER_ATTRIBUTE AS UUA, UM_USER AS U INNER JOIN ASG_USER_TENANT_ASC AUTA ON " +
            "AUTA.ASG_USER_UUID=U.UM_USER_ID WHERE UUA.UM_USER_ID = U.UM_ID AND UUA.UM_ATTR_NAME=? " +
            "AND UUA.UM_ATTR_VALUE =? AND UUA.UM_PROFILE_ID=? AND AUTA.ASG_TENANT_UUID=?";
}

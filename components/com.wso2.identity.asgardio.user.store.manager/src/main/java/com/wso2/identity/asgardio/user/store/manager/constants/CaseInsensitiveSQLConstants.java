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
 * Class to define case insensitive sql constants.
 */
public class CaseInsensitiveSQLConstants {

    // SQL Queries - Roles and Groups.
    public static final String ADD_USER_TO_ROLE_WITH_ID_SQL =
            "INSERT INTO UM_USER_ROLE (UM_USER_ID, UM_ROLE_ID, UM_TENANT_ID) VALUES " +
                    "((SELECT UM_ID FROM UM_USER WHERE UM_USER_ID=?),(SELECT UM_ID FROM UM_ROLE WHERE " +
                    "UM_ROLE_NAME=? AND UM_TENANT_ID=?), ?)";
    public static final String ADD_ROLE_SQL = "INSERT INTO UM_ROLE (UM_ROLE_NAME, UM_TENANT_ID) VALUES (?, ?)";

    /*In the tenant creation flow, the user has no associations, therefore using GET_USER_PROPS_FOR_PROFILE_WITH_ID_SQL
    is wrong.
     */
    public static final String GET_USER_PROPS_FOR_PROFILE_WITH_ID_SQL_TENANT_CREATION =
            "SELECT UM_ATTR_NAME, UM_ATTR_VALUE FROM UM_USER_ATTRIBUTE, UM_USER WHERE " +
                    "UM_USER.UM_ID = UM_USER_ATTRIBUTE.UM_USER_ID AND UM_USER.UM_USER_ID=? AND UM_PROFILE_ID=?";
    public static final String GET_USER_PROPS_FOR_PROFILE_WITH_ID_SQL =
            "SELECT UUA.UM_ATTR_NAME, UUA.UM_ATTR_VALUE FROM UM_USER_ATTRIBUTE AS UUA, UM_USER AS U INNER JOIN " +
                    "ASG_USER_TENANT_ASC AUTA ON U.UM_USER_ID = AUTA.ASG_USER_UUID WHERE U.UM_ID = UUA." +
                    "UM_USER_ID AND U.UM_USER_ID=? AND UUA.UM_PROFILE_ID=? AND AUTA.ASG_TENANT_UUID=?";
    public static final String GET_USERS_PROPS_FOR_PROFILE_WITH_ID_SQL =
            "SELECT U.UM_USER_ID, UUA.UM_ATTR_NAME, UUA.UM_ATTR_VALUE FROM UM_USER_ATTRIBUTE AS UUA, " +
                    "UM_USER AS U INNER JOIN ASG_USER_TENANT_ASC AUTA ON AUTA.ASG_USER_UUID = U.UM_USER_ID " +
                    "WHERE U.UM_ID = UUA.UM_USER_ID AND U.UM_USER_ID IN (?) AND UUA.UM_PROFILE_ID=? AND AUTA" +
                    ".ASG_TENANT_UUID=?";

    public static final String IS_USER_NAME_EXISTING_CASE_INSENSITIVE_SQL =
            "SELECT UM_ID FROM UM_USER WHERE LOWER(UM_USER_NAME)=LOWER(?)";
    public static final String IS_USER_EXISTING_BY_USER_UUID_SQL =
            "SELECT UM_ID FROM UM_USER WHERE UM_USER_ID=?";
    public static final String SELECT_USER_ID_FROM_USER_NAME_CASE_INSENSITIVE_SQL =
            "SELECT UM_USER_ID FROM UM_USER WHERE LOWER(UM_USER_NAME)=LOWER(?)";
    public static final String GET_USER_NAME_BY_USER_UUID_SQL =
            "SELECT UM_USER_NAME FROM UM_USER WHERE UM_USER_ID=?";
    public static final String GET_USER_FILTER_WITH_UUID_CASE_INSENSITIVE_SQL =
            "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM UM_USER U INNER JOIN " +
                    "(SELECT AUTA.ASG_USER_UUID FROM ASG_USER_TENANT_ASC AUTA WHERE AUTA.ASG_TENANT_UUID=?) P " +
                    "ON U.UM_USER_ID = P.ASG_USER_UUID WHERE LOWER(U.UM_USER_NAME) LIKE LOWER(?) " +
                    "ORDER BY U.UM_USER_NAME";
    public static final String GET_USER_FILTER_WITH_ID_CASE_INSENSITIVE_SQL =
            "SELECT UM_USER_ID, UM_USER_NAME FROM UM_USER WHERE LOWER(UM_USER_NAME) LIKE LOWER(?) ORDER BY " +
                    "UM_USER_NAME";
    public static final String SELECT_USER_NAME_CASE_INSENSITIVE_SQL =
            "SELECT U.UM_USER_ID, U.UM_USER_NAME, U.UM_USER_PASSWORD, U.UM_SALT_VALUE, U.UM_REQUIRE_CHANGE, " +
                    "U.UM_CHANGED_TIME FROM UM_USER U INNER JOIN ASG_USER_TENANT_ASC AUTA ON AUTA.ASG_USER_UUID=U" +
                    ".UM_USER_ID WHERE LOWER(U.UM_USER_NAME)=LOWER(?) AND AUTA.ASG_TENANT_UUID=?";
    public static final String COUNT_USERS_WITH_CLAIM_SQL = "SELECT COUNT(UA.UM_USER_ID) AS RESULT FROM " +
            "UM_USER_ATTRIBUTE UA INNER JOIN (SELECT U.UM_ID FROM UM_USER U INNER JOIN " +
            "(SELECT AUTA.ASG_USER_UUID FROM ASG_USER_TENANT_ASC AUTA WHERE AUTA.ASG_TENANT_UUID=?) P " +
            "ON P.ASG_USER_UUID=U.UM_USER_ID) Q ON Q.UM_ID=UA.UM_USER_ID WHERE UA.UM_ATTR_NAME=? " +
            "AND UA.UM_ATTR_VALUE LIKE ? AND UA.UM_PROFILE_ID=?";
    public static final String SELECT_USER_WITH_ID_SQL_CASE_INSENSITIVE_SQL = "SELECT U.UM_USER_ID, U.UM_USER_NAME, " +
            "U.UM_USER_PASSWORD, U.UM_SALT_VALUE, U.UM_REQUIRE_CHANGE, U.UM_CHANGED_TIME FROM UM_USER_ATTRIBUTE " +
            "AS UUA, UM_USER AS U INNER JOIN ASG_USER_TENANT_ASC AUTA ON AUTA.ASG_USER_UUID=U.UM_USER_ID " +
            "WHERE UUA.UM_USER_ID = U.UM_ID AND UUA.UM_ATTR_NAME=? AND LOWER(UUA.UM_ATTR_VALUE)=LOWER(?) " +
            "AND UUA.UM_PROFILE_ID=? AND AUTA.ASG_TENANT_UUID=?";
}

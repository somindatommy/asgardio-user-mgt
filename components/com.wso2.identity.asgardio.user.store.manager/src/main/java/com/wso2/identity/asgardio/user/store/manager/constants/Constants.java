/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.identity.asgardio.user.store.manager.constants;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;
import org.wso2.carbon.user.core.jdbc.caseinsensitive.JDBCCaseInsensitiveConstants;

import java.util.ArrayList;

import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.BASIC;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.CONNECTION;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.GROUP;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.USER;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataImportance.FALSE;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataImportance.TRUE;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.BOOLEAN;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.PASSWORD;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.SQL;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.STRING;

/**
 * Class with constants related to the asgardio user store manager.
 */
public class Constants {

    public static final ArrayList<Property> USER_STORE_MANAGER_MANDATORY_PROPERTIES = new ArrayList<>();
    public static final ArrayList<Property> USER_STORE_MANAGER_OPTIONAL_PROPERTIES = new ArrayList<>();
    public static final ArrayList<Property> USER_STORE_MANAGER_ADVANCED_PROPERTIES = new ArrayList<>();

    /*
    These will the user store properties that will be set in the realm configurations. This will be referred by
    the asgardio realm config builder.
    */
    public static final String[] USER_STORE_PROPERTIES_FOR_TENANTS = {
            JDBCRealmConstants.URL, JDBCRealmConstants.USER_NAME,
            UserStoreConfigConstants.connectionPassword, JDBCRealmConstants.DRIVER_NAME,
            UserStoreConfigConstants.usernameJavaRegEx, UserStoreConfigConstants.usernameJavaScriptRegEx,
            "UsernameJavaRegExViolationErrorMsg", UserCoreConstants.RealmConfig.PROPERTY_JAVA_REG_EX,
            UserStoreConfigConstants.passwordJavaScriptRegEx, "PasswordJavaRegExViolationErrorMsg",
            UserStoreConfigConstants.roleNameJavaRegEx, UserStoreConfigConstants.roleNameJavaScriptRegEx,
            UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME, UserStoreConfigConstants.maxUserNameListLength,
            UserStoreConfigConstants.maxRoleNameListLength, UserStoreConfigConstants.userRolesCacheEnabled,
            JDBCRealmConstants.JMX_ENABLED, JDBCRealmConstants.FAIR_QUEUE, JDBCRealmConstants.USE_EQUALS,
            "CountRetrieverClass", UserStoreConfigConstants.claimOperationsSupported,
            JDBCRealmConstants.DIGEST_FUNCTION, UserCoreConstants.TenantMgtConfig.LOCAL_NAME_TENANT_MANAGER,
            UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED, UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED,
            "MultiAttributeSeparator", "password", "IsBulkImportSupported", JDBCRealmConstants.STORE_SALTED_PASSWORDS
    };

    static {
        // Setting mandatory properties.
        setMandatoryProperty(JDBCRealmConstants.URL, "Connection URL",
                "URL of the user store database",
                false, new Property[]{CONNECTION.getProperty(), STRING.getProperty(), TRUE.getProperty()});
        setMandatoryProperty(JDBCRealmConstants.USER_NAME, "Connection Name",
                "Username for the database",
                false, new Property[]{CONNECTION.getProperty(), STRING.getProperty(), TRUE.getProperty()});
        setMandatoryProperty(JDBCRealmConstants.PASSWORD, "Connection Password",
                "Password for the database",
                true, new Property[]{CONNECTION.getProperty(), PASSWORD.getProperty(), TRUE.getProperty()});
        setMandatoryProperty(JDBCRealmConstants.DRIVER_NAME, "Driver Name",
                "Fully qualified driver name",
                false, new Property[]{CONNECTION.getProperty(), STRING.getProperty(), TRUE.getProperty()});

        // Set optional properties.
        setOptionalProperty(UserStoreConfigConstants.disabled, "Disabled",
                UserStoreConfigConstants.disabledDescription,
                new Property[]{BASIC.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty()});
        setOptionalProperty("ReadOnly", "Read-only",
                "Indicates whether the user store of this realm operates in the user read only mode or not",
                new Property[]{BASIC.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty()});
        setOptionalProperty(UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME, "Case Insensitive Username",
                UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME_DESCRIPTION,
                new Property[]{USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty()});

        // Set advance properties.
        setAdvancedProperty(JDBCRealmConstants.GET_IS_USER_NAME_EXISTING,
                "Is Username Existing Case sensitive SQL",
                CaseSensitiveSQLConstants.GET_IS_USER_NAME_EXISTING_CASE_SENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_IS_USER_NAME_EXISTING_CASE_INSENSITIVE,
                "Is Username Existing Case Insensitive SQL",
                CaseInsensitiveSQLConstants.IS_USER_NAME_EXISTING_CASE_INSENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.GET_IS_USER_EXISTING_WITH_ID,
                "Is Username Existing By User UUID SQL",
                CaseInsensitiveSQLConstants.IS_USER_EXISTING_BY_USER_UUID_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});

        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_NAME_FROM_USER_ID,
                "Select User ID From User UUID",
                CaseInsensitiveSQLConstants.GET_USER_NAME_BY_USER_UUID_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_ID_FROM_USER_NAME,
                "Select User ID From Case Sensitive Username",
                CaseSensitiveSQLConstants.SELECT_USER_ID_FROM_USER_NAME_CASE_SENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCCaseInsensitiveConstants.SELECT_USER_ID_FROM_USER_NAME_CASE_INSENSITIVE,
                "Select User ID From Case Insensitive Username",
                CaseInsensitiveSQLConstants.SELECT_USER_ID_FROM_USER_NAME_CASE_INSENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});

        setAdvancedProperty(JDBCRealmConstants.GET_USER_FILTER_WITH_ID,
                "User ID Filter With Case Sensitive Username SQL",
                CaseSensitiveSQLConstants.GET_USER_FILTER_WITH_UUID_CASE_SENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USER_FILTER_WITH_ID_CASE_INSENSITIVE,
                "User ID Filter With Case Insensitive Username SQL",
                CaseInsensitiveSQLConstants.GET_USER_FILTER_WITH_UUID_CASE_INSENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_NAME,
                "Select User Name From Case Sensitive Username",
                CaseSensitiveSQLConstants.SELECT_USER_NAME_CASE_SENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCCaseInsensitiveConstants.SELECT_USER_NAME_CASE_INSENSITIVE,
                "Select User Name From Case Insensitive Username",
                CaseInsensitiveSQLConstants.SELECT_USER_NAME_CASE_INSENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});

        setAdvancedProperty(JDBCRealmConstants.GET_USER_FILTER,
                "User Filter With ID From Case Sensitive Username",
                CaseSensitiveSQLConstants.GET_USER_FILTER_WITH_ID_CASE_SENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USER_FILTER_WITH_ID_CASE_INSENSITIVE,
                "User Filter With ID From Case Insensitive Username",
                CaseInsensitiveSQLConstants.GET_USER_FILTER_WITH_ID_CASE_INSENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCCaseInsensitiveConstants.SELECT_USER_WITH_ID_CASE_INSENSITIVE,
                "Select User With ID SQL With Case Insensitive Username",
                CaseInsensitiveSQLConstants.SELECT_USER_WITH_ID_SQL_CASE_INSENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_WITH_ID,
                "Select User With ID SQL With Case Sensitive Username",
                CaseSensitiveSQLConstants.SELECT_USER_WITH_ID_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.COUNT_USERS,
                "Count Users with username SQL",
                CaseSensitiveSQLConstants.COUNT_USERS_WITH_USERNAME_CASE_SENSITIVE_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.COUNT_USERS_WITH_CLAIM,
                "Count Users With Claim SQL",
                CaseInsensitiveSQLConstants.COUNT_USERS_WITH_CLAIM_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_PROPS_FOR_PROFILE_WITH_ID,
                "Get Users Properties for Profile With ID",
                CaseInsensitiveSQLConstants.GET_USERS_PROPS_FOR_PROFILE_WITH_ID_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.GET_PROPS_FOR_PROFILE_WITH_ID,
                "Get User Properties for Profile SQL With ID",
                CaseInsensitiveSQLConstants.GET_USER_PROPS_FOR_PROFILE_WITH_ID_SQL,
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});

        // Roles and Groups.
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE,
                "Add Role",
                CaseInsensitiveSQLConstants.ADD_ROLE_SQL,
                new Property[]{GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_WITH_ID,
                "Add User To User Role With ID",
                CaseInsensitiveSQLConstants.ADD_USER_TO_ROLE_WITH_ID_SQL,
                new Property[]{GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty()});
    }

    /**
     * Set mandatory properties of the user store manager.
     *
     * @param name            Name of the property.
     * @param displayName     Display name.
     * @param description     Description of the property.
     * @param encrypt         Whether to encrypt the value.
     * @param childProperties Child property.
     */
    private static void setMandatoryProperty(String name, String displayName, String description, boolean encrypt,
                                             Property[] childProperties) {

        String propertyDescription = displayName + "#" + description;
        if (encrypt) {
            propertyDescription += "#encrypt";
        }
        Property property = new Property(name, StringUtils.EMPTY, propertyDescription, childProperties);
        USER_STORE_MANAGER_MANDATORY_PROPERTIES.add(property);
    }

    /**
     * Set optional properties of the user store manager.
     *
     * @param name            Name of the property.
     * @param displayName     Display name.
     * @param description     Description of the property.
     * @param childProperties Child property.
     */
    private static void setOptionalProperty(String name, String displayName, String description,
                                            Property[] childProperties) {

        Property property = new Property(name, "false", displayName + "#" + description, childProperties);
        USER_STORE_MANAGER_OPTIONAL_PROPERTIES.add(property);
    }

    /**
     * Set advance properties of the user store manager.
     *
     * @param name            Name of the property.
     * @param displayName     Display name.
     * @param value           Value of the property.
     * @param childProperties Child property.
     */
    private static void setAdvancedProperty(String name, String displayName, String value, Property[] childProperties) {

        Property property = new Property(name, value, displayName + "#" + StringUtils.EMPTY, childProperties);
        USER_STORE_MANAGER_ADVANCED_PROPERTIES.add(property);
    }
}

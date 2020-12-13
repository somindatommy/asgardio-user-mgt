/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.identity.asgardio.tenant.association.mgt;

/**
 * This class contains the error codes for the error scenarios.
 */
public enum ErrorMessage {

    // Client errors.
    ERROR_CODE_EMPTY_USER_ID("60001", "Empty User ID",
            "User ID cannot be empty"),
    ERROR_CODE_EMPTY_TENANT_ID("60002", "Empty Tenant ID",
            "Tenant ID cannot be empty"),
    ERROR_CODE_INVALID_LIMIT_VALUE("60003", "Invalid limit value",
            "Limit need to be equal or greater than 0"),
    ERROR_CODE_INVALID_OFFSET_VALUE("60004", "Invalid offset value",
            "Offset need to be equal or greater than 0"),
    ERROR_CODE_EMPTY_ASSOCIATION_TYPE_ID("60005", "Empty Association type",
            "Association type cannot be empty"),
    ERROR_CODE_UNSUPPORTED_ASSOCIATION_TYPE_ID("UTA-60006", "Unsupported Association type",
            "Association type: %s is not supported"),
    ERROR_CODE_INVALID_TENANT_ID("60007", "Invalid tenant id",
            "Tenant: %s does not exist"),
    ERROR_CODE_INVALID_USER_ID("60008", "Invalid user id",
            "User: %s does not exist"),

    // Server Errors.
    ERROR_CODE_SORT_BY_NOT_IMPLEMENTED("65001", "Sort-By not implemented",
            "Sort by option is not implemented"),
    ERROR_CODE_SORT_ORDER_NOT_IMPLEMENTED("65002", "Sort-Order not implemented",
            "Sort order is not implemented"),
    ERROR_CODE_UPDATING_DATABASE("65003", "Error updating the database",
            "Error while updating the database"),
    ERROR_CODE_CONSTRAIN_VIOLATION("65004", "Constrain violation",
            "Constrain violation while updating the database"),
    ERROR_CODE_INVALID_DATA("65005", "Invalid data provided", "Invalid data provided"),
    ERROR_CODE_ERROR_GETTING_REALM("65006", "Error getting realm service",
            "Error getting the realm service"),
    ERROR_CODE_GETTING_ASC_TYPE_FOR_USER("65007", "Error getting association type for user",
            "Error occurred while getting association type for user: %s for tenant: %s"),
    ERROR_CODE_GETTING_DATABASE_CONNECTION("65008", "Error getting database connection",
            "Error getting database connection for tenant: %s"),
    ERROR_CODE_MULTIPLE_ASSOCIATIONS_FOR_USER("65009",
            "Multiple associations for user in the same tenant",
            "Multiple association found for the user: %s with tenant: %s"),
    ERROR_CODE_ERROR_ADDING_USER_TENANT_ASC("65010", "Error while adding user-tenant association",
            "Error occurred while adding user-tenant association: %s for user: %s with tenant UUID: %s"),
    ERROR_CODE_ERROR_VALIDATING_USER_EXISTENCE("65011", "Error while validating the user existence",
            "Error occurred while validating the existence of user: %s"),
    ERROR_CODE_ERROR_UPDATING_USER_TENANT_ASC("65012", "Error while updating user-tenant association",
            "Error occurred while updating user-tenant association: %s for user: %s with tenant: %s"),
    ERROR_CODE_ERROR_DELETING_USER_TENANT_ASC("65013", "Error while deleting user-tenant association",
            "Error occurred while deleting user-tenant association for user: %s with tenant: %s"),
    ERROR_CODE_GETTING_ASC_BY_USER_ID("65014", "Error while getting user-tenant associations by user id",
            "Error occurred while getting user-tenant associations for user: %s"),
    ERROR_CODE_GETTING_ASC_BY_USER_ID_AND_ASC("65015",
            "Error while getting user-tenant associations by user id and association type",
            "Error occurred while getting user-tenant associations for user: %s with association: %s"),
    ERROR_CODE_GETTING_ASC_BY_TENANT_ID("65016", "Error while getting user-tenant associations " +
            "by tenant id", "Error occurred while getting user-tenant associations for tenant: %s"),
    ERROR_CODE_GETTING_ASC_BY_TENANT_ID_AND_ASC("65017",
            "Error while getting user-tenant associations by tenant id and association type",
            "Error occurred while getting user-tenant associations for tenant: %s with association: %s"),
    ERROR_CODE_ERROR_DELETING_USER_ASC("65018", "Error deleting all tenant associations for user",
            "Error occurred while deleting all tenant associations for user: %s"),
    ERROR_CODE_ERROR_DELETING_TENANT_ASC("65019", "Error deleting all user associations for tenant",
            "Error occurred while deleting all user associations for tenant: %s"),
    ERROR_CODE_CHECKING_ASC_TYPE_FOR_USER_IN_TENANT("65020",
            "Error checking for association type for user in tenant",
            "Error while checking for association type: %s for user: %s in a tenant: %s");

    private final String code;
    private final String message;
    private final String description;

    ErrorMessage(String code, String message, String description) {

        this.code = code;
        this.message = message;
        this.description = description;
    }

    /**
     * Get the error code with the scenario.
     *
     * @return Error code without the scenario prefix.
     */
    public String getCode() {

        return code;
    }

    /**
     * Get error message.
     *
     * @return Error scenario message.
     */
    public String getMessage() {

        return message;
    }

    /**
     * Get error scenario message description.
     *
     * @return Error scenario description.
     */
    public String getDescription() {

        return description;
    }

    @Override
    public String toString() {

        return getCode() + " | " + getMessage() + " | " + getDescription();
    }
}

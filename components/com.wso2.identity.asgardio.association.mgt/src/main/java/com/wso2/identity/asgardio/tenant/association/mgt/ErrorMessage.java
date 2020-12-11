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
    ERROR_CODE_INVALID_TENANT_ID("60002", "Invalid tenant id",
            "Tenant: %s does not exist"),

    // Server Errors.
    ERROR_CODE_SORT_BY_NOT_IMPLEMENTED("65001", "Sort-By not implemented",
            "Sort by option is not implemented"),
    ERROR_CODE_SORT_ORDER_NOT_IMPLEMENTED("65002", "Sort-Order not implemented",
            "Sort order is not implemented"),
    ERROR_CODE_UPDATING_DATABASE("65003", "Error updating the database",
            "Error while updating the database"),
    ERROR_CODE_CONSTRAIN_VIOLATION("65004", "Constrain violation",
            "Constrain violation while updating the database"),
    ERROR_CODE_ERROR_GETTING_DB_CONNECTION("65005", "Error while getting the DB connection",
            "Error occurred while getting the database connection"),
    ERROR_CODE_INVALID_DATA("65006", "Invalid data provided", "Invalid data provided"),
    ERROR_CODE_ERROR_GETTING_REALM("65007", "Error getting realm service",
            "Error getting the realm service"),
    ERROR_CODE_GETTING_ASC_TYPE_FOR_USER("65009", "Error getting association type for user",
            "Error occurred while getting association type for user: %s for tenant: %s"),
    ERROR_CODE_GETTING_DATABASE_CONNECTION("65010", "Error getting database connection",
            "Error getting database connection for tenant: %s"),
    ERROR_CODE_MULTIPLE_ASSOCIATIONS_FOR_USER("65011", "Multiple associations for user in " +
            "the same tenant",
            "Multiple association found for the user: %s with tenant: %s"),;

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

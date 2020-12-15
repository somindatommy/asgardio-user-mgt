/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.identity.asgardio.user.store.manager;

/**
 * This class contains the error codes for the error scenarios.
 */
public enum ErrorMessage {

    // Client Errors.
    ERROR_CODE_EMPTY_CLAIM_URI("60008", "Empty Claim URI",
            "Claim URI cannot be empty"),
    ERROR_CODE_EMPTY_CLAIM_VALUE("60009", "Empty Claim Value",
            "Claim URI value cannot be empty"),

    // Server Errors.
    ERROR_CODE_ERROR_GETTING_DB_CONNECTION("65001", "Error while getting the DB connection",
            "Error occurred while getting the database connection"),
    ERROR_CODE_ADDING_ROLE_WITH_ID("65002", "Error while adding role with id",
            "Error occurred while adding the role: %s"),
    ERROR_CODE_UPDATING_DATABASE("65003", "Error updating the database",
            "Error while updating the database"),
    ERROR_CODE_CONSTRAIN_VIOLATION("65004", "Constrain violation",
            "Constrain violation while updating the database"),
    ERROR_CODE_INVALID_DATA("65005", "Invalid data provided", "Invalid data provided"),
    ERROR_CODE_ERROR_GETTING_USER_ATTRIBUTES("65006", "Error getting user attributes",
            "Error occurred while getting the attributes for the user: %s"),
    ERROR_CODE_ERROR_WHILE_FILTERING_USERS("65007", "Error while while retrieving users ",
            "Error occurred while retrieving users for filter: %s with max item limit: %s"),
    ERROR_CODE_GETTING_COUNT("65008", "Error occurred while getting the count",
            "Error occurred while retrieving getting count for filter: %s with value: %s"),
    ERROR_CODE_GETTING_USERS_WITH_MULTI_ATTR("65009", "Error occurred while doGetUserList for " +
            "multi attribute searching", "Error occurred while retrieving users list"),
    ERROR_CODE_INVALID_FILTER_CONDITION("65010", "Invalid filter condition",
            "Filter condition is not valid"),
    ERROR_CODE_ERROR_GETTING_TENANT_UUID("65011", "Error while getting the tenant UUID",
            "Error occurred while getting the tenant UUID for tenant: %s"),
    ERROR_CODE_INVALID_TENANT_ID("65012", "No tenant UUID found",
            "No tenant UUID found for tenant: %s"),
    ERROR_CODE_ERROR_AUTH_WITH_USERNAME("65013", "Error while authenticating with username",
            "Error occurred while authenticating with username"),
    ERROR_CODE_ERROR_AUTH_WITH_ID("65013", "Error while authenticating with id",
            "Error occurred while retrieving user authentication info for user: %s");

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

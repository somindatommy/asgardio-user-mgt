/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.identity.asgardio.tenant.association.mgt.internal.services;

import com.wso2.identity.asgardio.tenant.association.mgt.ErrorMessage;
import com.wso2.identity.asgardio.tenant.association.mgt.UserTenantAssociation;
import com.wso2.identity.asgardio.tenant.association.mgt.UserTenantAssociationManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * This class contains the default implementation of UserTenantAssociationManager.
 */
public class DefaultUserTenantAssociationManager implements UserTenantAssociationManager {

    private static final Log log = LogFactory.getLog(DefaultUserTenantAssociationManager.class);

    @Override
    public String getAssociationTypeForUser(String userUuid, String tenantUuid) throws UserStoreException {

        return null;
    }

    @Override
    public Map<String, String> getTenantAssociationsForUserByUserId(String userUuid, int limit, int offset,
                                                                    String sortBy, String sortOrder)
            throws UserStoreException {

        return null;
    }

    @Override
    public String[] getTenantAssociationsForUserByUserId(String userUuid, String associationType, int limit,
                                                         int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        return new String[0];
    }

    @Override
    public Map<String, String> getUserAssociationsForTenantByTenantId(String tenantUuid, int limit, int offset,
                                                                      String sortBy, String sortOrder)
            throws UserStoreException {

        return null;
    }

    @Override
    public String[] getUserAssociationsForTenantByTenantId(String tenantUuid, String associationType, int limit,
                                                           int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        return new String[0];
    }

    @Override
    public void addUserTenantAssociations(String userUuid, String tenantUuid, String associationType)
            throws UserStoreException {

    }

    @Override
    public void updateUserTenantAssociations(String userUuid, String tenantUuid, String associationType)
            throws UserStoreException {

    }

    @Override
    public void deleteUserTenantAssociations(String userUuid, String tenantUuid) throws UserStoreException {

    }

    @Override
    public void deleteAllTenantAssociationsForUser(String userUuid) throws UserStoreException {

    }

    @Override
    public void deleteAllUserAssociationsForTenant(String tenantUuid) throws UserStoreException {

    }

    @Override
    public boolean hasAssociationType(String userUuid, String tenantID, String associationType) {

        return false;
    }

    private void validateUserUuid(String userId, String errorScenario) throws UserStoreClientException {

        if (StringUtils.isBlank(userId)) {
            throw new UserStoreClientException(ErrorMessage.ERROR_CODE_EMPTY_USER_ID.getMessage(),
                    errorScenario + ErrorMessage.ERROR_CODE_EMPTY_USER_ID.getCode());
        }
    }

    private void validateTenantUuid(String tenantId, String errorScenario) throws UserStoreClientException {

        if (StringUtils.isBlank(tenantId)) {
            throw new UserStoreClientException(
                    ErrorMessage.ERROR_CODE_EMPTY_TENANT_ID.getDescription(),
                    errorScenario + ErrorMessage.ERROR_CODE_EMPTY_TENANT_ID.getCode());
        }
    }

    private void validateSorting(String sortBy, String sortOrder, String errorScenario) throws UserStoreException {

        if (StringUtils.isNotBlank(sortBy)) {
            throw new UserStoreException(
                    ErrorMessage.ERROR_CODE_SORT_BY_NOT_IMPLEMENTED.getDescription(),
                    errorScenario + ErrorMessage.ERROR_CODE_SORT_BY_NOT_IMPLEMENTED.getCode());
        }
        if (StringUtils.isNotBlank(sortOrder)) {
            throw new UserStoreException(
                    ErrorMessage.ERROR_CODE_SORT_ORDER_NOT_IMPLEMENTED.getDescription(),
                    errorScenario + ErrorMessage.ERROR_CODE_SORT_ORDER_NOT_IMPLEMENTED.getCode());
        }
    }

    private void validatePaginationInputs(int limit, int offset, String errorScenario) throws UserStoreClientException {

        if (limit < 0) {
            throw new UserStoreClientException(
                    ErrorMessage.ERROR_CODE_INVALID_LIMIT_VALUE.getDescription(),
                    errorScenario + ErrorMessage.ERROR_CODE_INVALID_LIMIT_VALUE.getCode());
        }
        if (offset < 0) {
            throw new UserStoreClientException(
                    ErrorMessage.ERROR_CODE_INVALID_OFFSET_VALUE.getDescription(),
                    errorScenario + ErrorMessage.ERROR_CODE_INVALID_OFFSET_VALUE.getCode());
        }
    }

    /**
     * Validate the association type with
     * {@link com.wso2.identity.asgardio.tenant.association.mgt.UserTenantAssociation}.
     *
     * @param associationType User Tenant association type.
     * @throws UserStoreClientException If the provided association type is either empty or invalid.
     */
    private void validateAssociationType(String associationType, String errorScenario) throws UserStoreClientException {

        if (StringUtils.isBlank(associationType)) {
            throw new UserStoreClientException(
                    ErrorMessage.ERROR_CODE_EMPTY_ASSOCIATION_TYPE_ID.getDescription(),
                    errorScenario + ErrorMessage.ERROR_CODE_EMPTY_ASSOCIATION_TYPE_ID.getCode());
        }

        boolean isSupportedAssociationType = false;
        for (UserTenantAssociation userTenantAssociation : UserTenantAssociation.values()) {
            if (associationType.equals(userTenantAssociation.toString())) {
                isSupportedAssociationType = true;
                break;
            }
        }
        if (!isSupportedAssociationType) {
            throw new UserStoreClientException(
                    String.format(ErrorMessage.ERROR_CODE_UNSUPPORTED_ASSOCIATION_TYPE_ID.getDescription(),
                            associationType),
                    errorScenario + ErrorMessage.ERROR_CODE_UNSUPPORTED_ASSOCIATION_TYPE_ID.getCode());
        }
    }

    /**
     * Update values to the data base. Can be used to INSERT, UPDATE and DELETE statements.
     *
     * @param dbConnection Connection
     * @param scenario     Error scenario to generate more meaningful errors.
     * @param sqlStmt      SQL statement.
     * @param params       Params.
     * @throws UserStoreException If an error occurred while executing the query.
     */
    private void updateStringValuesToDatabase(Connection dbConnection, String scenario, String sqlStmt,
                                              Object... params) throws UserStoreException {

        PreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            if (dbConnection == null) {
                localConnection = true;
                dbConnection = getDBConnection();
                dbConnection.setAutoCommit(false);
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException(ErrorMessage.ERROR_CODE_INVALID_DATA.getDescription(),
                                scenario + ErrorMessage.ERROR_CODE_INVALID_DATA.getCode());
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    } else if (param instanceof Date) {
                        prepStmt.setTimestamp(i + 1, new Timestamp(System.currentTimeMillis()));
                    } else if (param instanceof Boolean) {
                        prepStmt.setBoolean(i + 1, (Boolean) param);
                    }
                }
            }
            int count = prepStmt.executeUpdate();
            if (log.isDebugEnabled()) {
                if (count == 0) {
                    log.debug("No rows were updated");
                }
                log.debug(String.format("Executed query: %s and number of updated rows: %s", sqlStmt, count));
            }
            if (localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            DatabaseUtil.rollBack(dbConnection);
            String msg = ErrorMessage.ERROR_CODE_UPDATING_DATABASE.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            if (e instanceof SQLIntegrityConstraintViolationException) {
                // Error due to a duplicate entry.
                throw new UserStoreException(msg, scenario + ErrorMessage.ERROR_CODE_CONSTRAIN_VIOLATION.getCode(), e);
            } else {
                // Errors related to other SQL Exceptions.
                throw new UserStoreException(msg, scenario + ErrorMessage.ERROR_CODE_UPDATING_DATABASE.getCode(), e);
            }
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    private Connection getDataBaseConnection(String scenario) throws UserStoreException {

        try {
            return getDBConnection();
        } catch (SQLException e) {
            String errorMessage = ErrorMessage.ERROR_CODE_ERROR_GETTING_DB_CONNECTION.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, scenario +
                    ErrorMessage.ERROR_CODE_ERROR_GETTING_DB_CONNECTION.getCode(), e);
        }
    }
}

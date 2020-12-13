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
import com.wso2.identity.asgardio.tenant.association.mgt.constants.Constants;
import com.wso2.identity.asgardio.tenant.association.mgt.internal.AssociationManagerDataHolder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

/**
 * This class contains the default implementation of UserTenantAssociationManager.
 */
public class DefaultUserTenantAssociationManager implements UserTenantAssociationManager {

    private static final Log log = LogFactory.getLog(DefaultUserTenantAssociationManager.class);

    @Override
    public String getAssociationTypeForUser(String userUuid, String tenantUuid) throws UserStoreException {

        int tenantId = getTenantId(tenantUuid);
        validateUserUuid(tenantId, userUuid);
        String associationType = null;
        try (Connection dbConnection = createDatabaseConnection(tenantId);
             PreparedStatement prepStmt =
                     dbConnection.prepareStatement(Constants.SQLConstants.GET_ASC_TYPE_BY_TENANT_ID_USER_ID_SQL)) {

            prepStmt.setString(1, userUuid);
            prepStmt.setString(2, tenantUuid);
            ResultSet rs = prepStmt.executeQuery();
            int counter = 0;
            while (rs.next()) {
                associationType = rs.getString(1);
                counter++;
                if (counter > 1) {
                    throw new UserStoreException(String.format(
                            ErrorMessage.ERROR_CODE_MULTIPLE_ASSOCIATIONS_FOR_USER.getDescription(), userUuid,
                            tenantUuid), ErrorMessage.ERROR_CODE_MULTIPLE_ASSOCIATIONS_FOR_USER.getCode());
                }
            }
            rs.close();
        } catch (SQLException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_ASC_TYPE_FOR_USER, userUuid, tenantUuid);
        }
        return associationType;
    }

    @Override
    public Map<String, String> getTenantAssociationsForUserByUserId(String userUuid, int limit, int offset,
                                                                    String sortBy, String sortOrder)
            throws UserStoreException {

        validateSorting(sortBy, sortOrder);
        validatePaginationInputs(limit, offset);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        validateUserUuid(tenantId, userUuid);
        if (limit == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> tenantAssociationMap = new HashMap<>();
        try (Connection dbConnection = createDatabaseConnection(tenantId);
             PreparedStatement prepStmt =
                     dbConnection.prepareStatement(Constants.SQLConstants.GET_USER_TENANT_ASC_BY_USER_ID_SQL)) {
            prepStmt.setString(1, userUuid);
            prepStmt.setInt(2, limit);
            prepStmt.setInt(3, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String tenantDomain = rs.getString(1);
                    String associationType = rs.getString(2);
                    tenantAssociationMap.put(tenantDomain, associationType);
                }
            } catch (SQLException exception) {
                throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_ASC_BY_USER_ID, userUuid);
            }
        } catch (SQLException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_ASC_BY_USER_ID, userUuid);
        }
        return tenantAssociationMap;
    }

    @Override
    public String[] getTenantAssociationsForUserByUserId(String userUuid, String associationType, int limit,
                                                         int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        validateSorting(sortBy, sortOrder);
        validatePaginationInputs(limit, offset);
        validateAssociationType(associationType);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        validateUserUuid(tenantId, userUuid);
        if (limit == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        ArrayList<String> tenantDomains = new ArrayList<>();
        try (Connection dbConnection = createDatabaseConnection(tenantId);
             PreparedStatement prepStmt = dbConnection.prepareStatement(
                     Constants.SQLConstants.GET_USER_TENANT_ASC_BY_USER_ID_AND_ASC_TYPE_SQL)) {
            prepStmt.setString(1, userUuid);
            prepStmt.setString(2, associationType);
            prepStmt.setInt(3, limit);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String tenantDomain = rs.getString(1);
                    tenantDomains.add(tenantDomain);
                }
            } catch (SQLException exception) {
                throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_ASC_BY_USER_ID_AND_ASC, userUuid,
                        associationType);
            }
        } catch (SQLException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_ASC_BY_USER_ID_AND_ASC, userUuid,
                    associationType);
        }
        return tenantDomains.toArray(new String[0]);
    }

    @Override
    public Map<String, String> getUserAssociationsForTenantByTenantId(String tenantUuid, int limit, int offset,
                                                                      String sortBy, String sortOrder)
            throws UserStoreException {

        validateSorting(sortBy, sortOrder);
        validatePaginationInputs(limit, offset);
        int tenantId = getTenantId(tenantUuid);
        if (limit == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> userAssociationsMap = new HashMap<>();
        try (Connection dbConnection = createDatabaseConnection(tenantId);
             PreparedStatement prepStmt =
                     dbConnection.prepareStatement(Constants.SQLConstants.GET_USER_TENANT_ASC_BY_TENANT_ID_SQL)) {
            prepStmt.setString(1, tenantUuid);
            prepStmt.setInt(2, limit);
            prepStmt.setInt(3, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString(1);
                    String associationType = rs.getString(2);
                    userAssociationsMap.put(username, associationType);
                }
            } catch (SQLException exception) {
                throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_ASC_BY_TENANT_ID, tenantUuid);
            }
        } catch (SQLException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_ASC_BY_TENANT_ID, tenantUuid);
        }
        return userAssociationsMap;
    }

    @Override
    public String[] getUserAssociationsForTenantByTenantId(String tenantUuid, String associationType, int limit,
                                                           int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        validateAssociationType(associationType);
        validatePaginationInputs(limit, offset);
        int tenantId = getTenantId(tenantUuid);
        if (limit == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        ArrayList<String> usernames = new ArrayList<>();
        try (Connection dbConnection = createDatabaseConnection(tenantId);
             PreparedStatement prepStmt = dbConnection.prepareStatement(
                     Constants.SQLConstants.GET_USER_TENANT_ASC_BY_TENANT_ID_AND_ASC_TYPE_SQL)) {
            prepStmt.setString(1, tenantUuid);
            prepStmt.setString(2, associationType);
            prepStmt.setInt(3, limit);
            prepStmt.setInt(4, offset);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString(1);
                    usernames.add(username);
                }
            } catch (SQLException exception) {
                throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_ASC_BY_TENANT_ID_AND_ASC, tenantUuid,
                        associationType);
            }
        } catch (SQLException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_ASC_BY_TENANT_ID_AND_ASC, tenantUuid,
                    associationType);
        }
        return usernames.toArray(new String[0]);
    }

    @Override
    public void addUserTenantAssociations(String userUuid, String tenantUuid, String associationType)
            throws UserStoreException {

        int tenantId = getTenantId(tenantUuid);
        validateUserUuid(tenantId, userUuid);
        validateAssociationType(associationType);
        try (Connection dbConnection = createDatabaseConnection(tenantId)) {
            dbConnection.setAutoCommit(false);
            updateStringValuesToDatabase(dbConnection, tenantUuid, Constants.SQLConstants.ADD_USER_TENANT_ASC_SQL,
                    userUuid, tenantUuid, associationType);
            dbConnection.commit();
        } catch (SQLException e) {
            throw handleException(e, ErrorMessage.ERROR_CODE_ERROR_ADDING_USER_TENANT_ASC, associationType, userUuid,
                    tenantUuid);
        }
    }

    @Override
    public void updateUserTenantAssociations(String userUuid, String tenantUuid, String associationType)
            throws UserStoreException {

        int tenantId = getTenantId(tenantUuid);
        validateUserUuid(tenantId, userUuid);
        validateAssociationType(associationType);
        try (Connection dbConnection = createDatabaseConnection(tenantId)) {
            dbConnection.setAutoCommit(false);
            updateStringValuesToDatabase(dbConnection, tenantUuid, Constants.SQLConstants.UPDATE_USER_TENANT_ASC_SQL,
                    userUuid, tenantUuid, associationType);
            dbConnection.commit();
        } catch (SQLException e) {
            throw handleException(e, ErrorMessage.ERROR_CODE_ERROR_UPDATING_USER_TENANT_ASC, associationType, userUuid,
                    tenantUuid);
        }
    }

    @Override
    public void deleteUserTenantAssociations(String userUuid, String tenantUuid) throws UserStoreException {

        int tenantId = getTenantId(tenantUuid);
        validateUserUuid(tenantId, userUuid);
        try (Connection dbConnection = createDatabaseConnection(tenantId)) {
            dbConnection.setAutoCommit(false);
            updateStringValuesToDatabase(dbConnection, tenantUuid, Constants.SQLConstants.DELETE_USER_TENANT_ASC_SQL,
                    userUuid, tenantUuid);
            dbConnection.commit();
        } catch (SQLException e) {
            throw handleException(e, ErrorMessage.ERROR_CODE_ERROR_DELETING_USER_TENANT_ASC, userUuid,
                    tenantUuid);
        }
    }

    @Override
    public void deleteAllTenantAssociationsForUser(String userUuid) throws UserStoreException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        validateUserUuid(tenantId, userUuid);
        try (Connection dbConnection = createDatabaseConnection(tenantId)) {
            dbConnection.setAutoCommit(false);
            updateStringValuesToDatabase(dbConnection, StringUtils.EMPTY,
                    Constants.SQLConstants.DELETE_TENANT_ASC_FOR_USER_SQL, userUuid);
            dbConnection.commit();
        } catch (SQLException e) {
            throw handleException(e, ErrorMessage.ERROR_CODE_ERROR_DELETING_USER_ASC, userUuid);
        }
    }

    @Override
    public void deleteAllUserAssociationsForTenant(String tenantUuid) throws UserStoreException {

        int tenantId = getTenantId(tenantUuid);
        try (Connection dbConnection = createDatabaseConnection(tenantId)) {
            dbConnection.setAutoCommit(false);
            updateStringValuesToDatabase(dbConnection, StringUtils.EMPTY,
                    Constants.SQLConstants.DELETE_USER_ASC_FOR_TENANT_SQL, tenantUuid);
            dbConnection.commit();
        } catch (SQLException e) {
            throw handleException(e, ErrorMessage.ERROR_CODE_ERROR_DELETING_TENANT_ASC, tenantUuid);
        }
    }

    @Override
    public boolean hasAssociationType(String userUuid, String tenantUuid, String associationType)
            throws UserStoreException {

        validateAssociationType(associationType);
        int tenantId = getTenantId(tenantUuid);
        validateUserUuid(tenantId, userUuid);
        try (Connection dbConnection = createDatabaseConnection(tenantId);
             PreparedStatement prepStmt =
                     dbConnection.prepareStatement(Constants.SQLConstants.COUNT_ASC_TYPE_FOR_USER_IN_TENANT)) {
            prepStmt.setString(1, userUuid);
            prepStmt.setString(2, tenantUuid);
            prepStmt.setString(3, associationType);
            try (ResultSet rs = prepStmt.executeQuery()) {
                int count = 0;
                if (rs.next()) {
                    count = rs.getInt(1);
                }
                if (count > 1) {
                    throw new UserStoreException(String.format(
                            ErrorMessage.ERROR_CODE_MULTIPLE_ASSOCIATIONS_FOR_USER.getDescription(), userUuid,
                            tenantUuid), ErrorMessage.ERROR_CODE_MULTIPLE_ASSOCIATIONS_FOR_USER.getCode());
                }
                return count > 0;
            } catch (SQLException e) {
                throw handleException(e, ErrorMessage.ERROR_CODE_CHECKING_ASC_TYPE_FOR_USER_IN_TENANT, associationType,
                        userUuid, tenantUuid);
            }
        } catch (SQLException e) {
            throw handleException(e, ErrorMessage.ERROR_CODE_CHECKING_ASC_TYPE_FOR_USER_IN_TENANT, associationType,
                    userUuid, tenantUuid);
        }
    }

    private void validateUserUuid(int tenantId, String userUuid) throws UserStoreException {

        if (StringUtils.isBlank(userUuid)) {
            throw new UserStoreClientException(ErrorMessage.ERROR_CODE_EMPTY_USER_ID.getDescription(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX + ErrorMessage.ERROR_CODE_EMPTY_USER_ID.getCode());
        }
        RealmService realmService = AssociationManagerDataHolder.getRealmService();
        if (realmService == null) {
            throw new UserStoreException(ErrorMessage.ERROR_CODE_ERROR_GETTING_REALM.getMessage(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                            ErrorMessage.ERROR_CODE_ERROR_GETTING_REALM.getCode());
        }
        try {
            AbstractUserStoreManager userStoreManager =
                    (AbstractUserStoreManager) realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            if (!userStoreManager.isExistingUserWithID(userUuid)) {
                throw new UserStoreClientException(
                        String.format(ErrorMessage.ERROR_CODE_INVALID_USER_ID.getDescription(), userUuid),
                        Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                                ErrorMessage.ERROR_CODE_INVALID_USER_ID.getCode());
            }
        } catch (org.wso2.carbon.user.api.UserStoreException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_ERROR_VALIDATING_USER_EXISTENCE, tenantId);
        }
    }

    private void validateSorting(String sortBy, String sortOrder) throws UserStoreException {

        if (StringUtils.isNotBlank(sortBy)) {
            throw new UserStoreException(
                    ErrorMessage.ERROR_CODE_SORT_BY_NOT_IMPLEMENTED.getDescription(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                            ErrorMessage.ERROR_CODE_SORT_BY_NOT_IMPLEMENTED.getCode());
        }
        if (StringUtils.isNotBlank(sortOrder)) {
            throw new UserStoreException(
                    ErrorMessage.ERROR_CODE_SORT_ORDER_NOT_IMPLEMENTED.getDescription(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                            ErrorMessage.ERROR_CODE_SORT_ORDER_NOT_IMPLEMENTED.getCode());
        }
    }

    private void validatePaginationInputs(int limit, int offset) throws UserStoreClientException {

        if (limit < 0) {
            throw new UserStoreClientException(
                    ErrorMessage.ERROR_CODE_INVALID_LIMIT_VALUE.getDescription(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                            ErrorMessage.ERROR_CODE_INVALID_LIMIT_VALUE.getCode());
        }
        if (offset < 0) {
            throw new UserStoreClientException(
                    ErrorMessage.ERROR_CODE_INVALID_OFFSET_VALUE.getDescription(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                            ErrorMessage.ERROR_CODE_INVALID_OFFSET_VALUE.getCode());
        }
    }

    /**
     * Validate the association type with
     * {@link com.wso2.identity.asgardio.tenant.association.mgt.UserTenantAssociation}.
     *
     * @param associationType User Tenant association type.
     * @throws UserStoreClientException If the provided association type is either empty or invalid.
     */
    private void validateAssociationType(String associationType) throws UserStoreClientException {

        if (StringUtils.isBlank(associationType)) {
            throw new UserStoreClientException(
                    ErrorMessage.ERROR_CODE_EMPTY_ASSOCIATION_TYPE_ID.getDescription(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                            ErrorMessage.ERROR_CODE_EMPTY_ASSOCIATION_TYPE_ID.getCode());
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
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                            ErrorMessage.ERROR_CODE_UNSUPPORTED_ASSOCIATION_TYPE_ID.getCode());
        }
    }

    private UserStoreException handleException(Exception exception, ErrorMessage errorMessage, Object... params) {

        String error = String.format(errorMessage.getDescription(), params);
        if (log.isDebugEnabled()) {
            log.debug(error, exception);
        }
        return new UserStoreException(error, Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX
                + errorMessage.getCode(), exception);
    }

    private int getTenantId(String tenantUuid) throws UserStoreException {

        if (StringUtils.isBlank(tenantUuid)) {
            throw new UserStoreException(ErrorMessage.ERROR_CODE_EMPTY_TENANT_ID.getDescription(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                            ErrorMessage.ERROR_CODE_EMPTY_TENANT_ID.getCode());
        }
        RealmService realmService = AssociationManagerDataHolder.getRealmService();
        if (realmService == null) {
            throw new UserStoreException(ErrorMessage.ERROR_CODE_ERROR_GETTING_REALM.getDescription(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                            ErrorMessage.ERROR_CODE_ERROR_GETTING_REALM.getCode());
        }
        Tenant tenant = realmService.getTenantManager().getTenant(tenantUuid);
        if (tenant == null) {
            throw new UserStoreException(String.format(ErrorMessage.ERROR_CODE_INVALID_TENANT_ID.getDescription(),
                    tenantUuid), Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                    ErrorMessage.ERROR_CODE_INVALID_TENANT_ID.getCode());
        }
        return tenant.getId();
    }

    /**
     * Create database connection.
     *
     * @param tenantId Tenant id.
     * @return Connection.
     * @throws UserStoreException If an error occurred while getting the connection.
     */
    private Connection createDatabaseConnection(int tenantId)
            throws UserStoreException {

        RealmService realmService = AssociationManagerDataHolder.getRealmService();
        if (realmService == null) {
            throw new UserStoreException(ErrorMessage.ERROR_CODE_ERROR_GETTING_REALM.getDescription(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                            ErrorMessage.ERROR_CODE_ERROR_GETTING_REALM.getCode());
        }
        try {
            DataSource dataSource = DatabaseUtil.createUserStoreDataSource(
                    realmService.getTenantUserRealm(tenantId).getRealmConfiguration());
            return dataSource.getConnection();
        } catch (org.wso2.carbon.user.api.UserStoreException | SQLException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_DATABASE_CONNECTION, tenantId);
        }
    }

    /**
     * Update values to the data base. Can be used to INSERT, UPDATE and DELETE statements.
     *
     * @param dbConnection Connection.
     * @param sqlStmt      SQL statement.
     * @param params       Params.
     * @throws UserStoreException If an error occurred while executing the query.
     */
    private void updateStringValuesToDatabase(Connection dbConnection, String tenantUuid, String sqlStmt,
                                              Object... params) throws UserStoreException {

        PreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            if (dbConnection == null) {
                localConnection = true;
                dbConnection = (createDatabaseConnection(getTenantId(tenantUuid)));
                dbConnection.setAutoCommit(false);
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException(ErrorMessage.ERROR_CODE_INVALID_DATA.getDescription(),
                                Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                                        ErrorMessage.ERROR_CODE_INVALID_DATA.getCode());
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
            String msg = ErrorMessage.ERROR_CODE_UPDATING_DATABASE.getDescription();
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            if (e instanceof SQLIntegrityConstraintViolationException) {
                // Error due to a duplicate entry.
                throw new UserStoreException(msg, Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                        ErrorMessage.ERROR_CODE_CONSTRAIN_VIOLATION.getCode(), e);
            } else {
                // Errors related to other SQL Exceptions.
                throw new UserStoreException(msg, Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                        ErrorMessage.ERROR_CODE_UPDATING_DATABASE.getCode(), e);
            }
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }
}

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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import javax.sql.DataSource;

/**
 * This class contains the default implementation of UserTenantAssociationManager.
 */
public class DefaultUserTenantAssociationManager implements UserTenantAssociationManager {

    private static final Log log = LogFactory.getLog(DefaultUserTenantAssociationManager.class);

    @Override
    public String getAssociationTypeForUser(String userUuid, String tenantUuid) throws UserStoreException {

        validateUserUuid(userUuid);
        validateTenantUuid(tenantUuid);
        int tenantId = getTenantId(tenantUuid);
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
                            ErrorMessage.ERROR_CODE_MULTIPLE_ASSOCIATIONS_FOR_USER.getMessage(), userUuid, tenantUuid),
                            ErrorMessage.ERROR_CODE_MULTIPLE_ASSOCIATIONS_FOR_USER.getCode());
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

    private void validateUserUuid(String userId) throws UserStoreClientException {

        if (StringUtils.isBlank(userId)) {
            throw new UserStoreClientException(ErrorMessage.ERROR_CODE_EMPTY_USER_ID.getMessage(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX + ErrorMessage.ERROR_CODE_EMPTY_USER_ID.getCode());
        }
    }

    private void validateTenantUuid(String tenantId) throws UserStoreClientException {

        if (StringUtils.isBlank(tenantId)) {
            throw new UserStoreClientException(
                    ErrorMessage.ERROR_CODE_EMPTY_TENANT_ID.getDescription(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX + ErrorMessage.ERROR_CODE_EMPTY_TENANT_ID.getCode());
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

    private UserStoreException handleException(Exception exception, ErrorMessage errorMessage, Object... params) {

        String error = String.format(errorMessage.getMessage(), params);
        if (log.isDebugEnabled()) {
            log.debug(error, exception);
        }
        return new UserStoreException(error, Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX
                + errorMessage.getCode(), exception);
    }

    private int getTenantId(String tenantUuid) throws UserStoreException {

        RealmService realmService = AssociationManagerDataHolder.getRealmService();
        if (realmService == null) {
            throw new UserStoreException(ErrorMessage.ERROR_CODE_ERROR_GETTING_REALM.getMessage(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
                            ErrorMessage.ERROR_CODE_ERROR_GETTING_REALM.getCode());
        }
        Tenant tenant = realmService.getTenantManager().getTenant(tenantUuid);
        if (tenant == null) {
            throw new UserStoreException(ErrorMessage.ERROR_CODE_INVALID_TENANT_ID.getMessage(),
                    Constants.USER_TENANT_ASSOCIATION_ERROR_PREFIX +
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
            throw new UserStoreException(ErrorMessage.ERROR_CODE_ERROR_GETTING_REALM.getMessage(),
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
}

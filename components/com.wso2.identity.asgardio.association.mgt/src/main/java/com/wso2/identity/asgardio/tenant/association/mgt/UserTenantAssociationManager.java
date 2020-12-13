/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.identity.asgardio.tenant.association.mgt;

import org.wso2.carbon.user.core.UserStoreException;

import java.util.Map;

/**
 * This is the service interface for managing user-tenant associations.
 */
public interface UserTenantAssociationManager {

    /**
     * Get the association type for the user with the tenant.
     *
     * @param userUuid   User UUID.
     * @param tenantUuid Tenant UUID.
     * @return Association Type.
     * @throws UserStoreException If an error occurred while getting the association type.
     */
    String getAssociationTypeForUser(String userUuid, String tenantUuid) throws UserStoreException;

    /**
     * Get the user-tenant associations for the user with the given UUID.
     *
     * @param userUuid  UUID of the user.
     * @param limit     Number of results to be retrieved.
     * @param offset    Offset.
     * @param sortBy    Sort by.
     * @param sortOrder Sort order.
     * @return Map with the associated tenant UUID as the key.
     * @throws UserStoreException If an error occurred while getting the associations.
     */
    Map<String, String> getTenantAssociationsForUserByUserId(String userUuid, int limit, int offset,
                                                             String sortBy, String sortOrder)
            throws UserStoreException;

    /**
     * Get list of tenants for the user with the given UUID and given user-tenant association type.
     *
     * @param userUuid        UUID of the user.
     * @param associationType User-tenant association type {@link UserTenantAssociation}.
     * @param limit           Number of results to be retrieved.
     * @param offset          Offset.
     * @param sortBy          Sort by.
     * @param sortOrder       Sort order.
     * @return List of tenant UUIDs.
     * @throws UserStoreException If an error occurred while getting the associations.
     */
    String[] getTenantAssociationsForUserByUserId(String userUuid, String associationType, int limit, int offset,
                                                  String sortBy, String sortOrder)
            throws UserStoreException;

    /**
     * Get all the user-tenant associations for the given tenant.
     *
     * @param tenantUuid UUID of the tenant.
     * @param limit      Number of results to be retrieved.
     * @param offset     Offset.
     * @param sortBy     Sort by.
     * @param sortOrder  Sort order.
     * @return Map with user UUIDs with corresponding association type.
     * @throws UserStoreException If an error occurred while getting the associations.
     */
    Map<String, String> getUserAssociationsForTenantByTenantId(String tenantUuid, int limit, int offset,
                                                               String sortBy, String sortOrder)
            throws UserStoreException;

    /**
     * Get all the users in the given tenant domain with the given user-tenant association type.
     *
     * @param tenantUuid      UUID of the tenant.
     * @param associationType User-tenant association type {@link UserTenantAssociation}.
     * @param limit           Number of results to be retrieved.
     * @param offset          Offset.
     * @param sortBy          Sort by.
     * @param sortOrder       Sort order.
     * @return List of user UUIDs.
     * @throws UserStoreException If an error occurred while getting the associations.
     */
    String[] getUserAssociationsForTenantByTenantId(String tenantUuid, String associationType, int limit,
                                                    int offset, String sortBy, String sortOrder)
            throws UserStoreException;

    /**
     * Add user-tenant association for a user in a given tenant.
     *
     * @param userUuid        UUID of the user.
     * @param tenantUuid      UUID of the tenant.
     * @param associationType User-tenant association type {@link UserTenantAssociation}.
     * @throws UserStoreException If an error occurred while adding user-tenant association.
     */
    void addUserTenantAssociations(String userUuid, String tenantUuid, String associationType)
            throws UserStoreException;

    /**
     * Update user-tenant association for the user in a given tenant.
     *
     * @param userUuid        UUID of the user.
     * @param tenantUuid      UUID of the tenant.
     * @param associationType User-tenant association type {@link UserTenantAssociation}.
     * @throws UserStoreException If an error occurred while updating user-tenant association.
     */
    void updateUserTenantAssociations(String userUuid, String tenantUuid, String associationType)
            throws UserStoreException;

    /**
     * Delete the user-tenant association for the given user in given tenant.
     *
     * @param userUuid   UUID of the user.
     * @param tenantUuid UUID of the tenant.
     * @throws UserStoreException If an error occurred while deleting the association.
     */
    void deleteUserTenantAssociations(String userUuid, String tenantUuid) throws UserStoreException;

    /**
     * Delete all the tenant association for the given user.
     *
     * @param userUuid UUID of the user.
     * @throws UserStoreException If an error occurred while deleting tenant associations.
     */
    void deleteAllTenantAssociationsForUser(String userUuid) throws UserStoreException;

    /**
     * Delete all user association for the given tenant.
     *
     * @param tenantUuid UUID of the tenant.
     * @throws UserStoreException If an error occurred while deleting user associations.
     */
    void deleteAllUserAssociationsForTenant(String tenantUuid) throws UserStoreException;

    /**
     * Check whether the user has the given association type with the given tenant.
     *
     * @param userUuid        UUID of the user.
     * @param tenantUuid      UUID of the tenant.
     * @param associationType User-tenant association type {@link UserTenantAssociation}.
     * @return True if the user has given association type in the given tenant.
     * @throws UserStoreException If an error occurred while checking for association types.
     */
    boolean hasAssociationType(String userUuid, String tenantUuid, String associationType) throws UserStoreException;
}

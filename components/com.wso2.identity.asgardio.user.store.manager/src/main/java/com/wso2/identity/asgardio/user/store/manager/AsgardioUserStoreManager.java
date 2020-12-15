/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein in any form is strictly forbidden, unless permitted by WSO2 expressly.
 * You may not alter or remove any copyright or other notice from copies of this content.
 */

package com.wso2.identity.asgardio.user.store.manager;

import com.wso2.identity.asgardio.user.store.manager.constants.CaseInsensitiveSQLConstants;
import com.wso2.identity.asgardio.user.store.manager.constants.CaseSensitiveSQLConstants;
import com.wso2.identity.asgardio.user.store.manager.constants.Constants;
import com.wso2.identity.asgardio.user.store.manager.internal.AsgardioUserStoreDataHolder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.tenant.mgt.util.TenantMgtUtil;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.FailureReason;
import org.wso2.carbon.user.core.common.UniqueIDPaginatedSearchResult;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;
import org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.ExpressionAttribute;
import org.wso2.carbon.user.core.model.ExpressionCondition;
import org.wso2.carbon.user.core.model.ExpressionOperation;
import org.wso2.carbon.user.core.model.OperationalCondition;
import org.wso2.carbon.user.core.model.SqlBuilder;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTimeoutException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;

/**
 * This class contains the implementation of the user store manager Asgardio.
 */
public class AsgardioUserStoreManager extends UniqueIDJDBCUserStoreManager {

    private static final Log log = LogFactory.getLog(AsgardioUserStoreManager.class);
    private static final String DB2 = "db2";
    private static final String MSSQL = "mssql";
    private static final String ORACLE = "oracle";
    private static final String MYSQL = "mysql";
    private static final String MARIADB = "mariadb";

    public AsgardioUserStoreManager() {

    }

    public AsgardioUserStoreManager(RealmConfiguration realmConfig, int tenantId) throws UserStoreException {

        super(realmConfig, tenantId);
    }

    public AsgardioUserStoreManager(DataSource ds, RealmConfiguration realmConfig, int tenantId,
                                    boolean addInitData) throws UserStoreException {

        super(ds, realmConfig, tenantId, addInitData);
    }

    public AsgardioUserStoreManager(DataSource ds, RealmConfiguration realmConfig) throws UserStoreException {

        super(ds, realmConfig);
    }

    public AsgardioUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
                                    ClaimManager claimManager, ProfileConfigurationManager profileManager,
                                    UserRealm realm, Integer tenantId)
            throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId);
    }

    public AsgardioUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
                                    ClaimManager claimManager, ProfileConfigurationManager profileManager,
                                    UserRealm realm, Integer tenantId, boolean skipInitData) throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, skipInitData);
    }

    @Override
    public AuthenticationResult doAuthenticateWithID(String preferredUserNameProperty, String preferredUserNameValue,
                                                     Object credential, String profileName) throws UserStoreException {

        // If the preferred username property is username, then authenticate with username.
        if (preferredUserNameProperty.equals(getUserNameMappedAttribute())) {
            return doAuthenticateWithUserName(preferredUserNameValue, credential);
        }
        // Pre validate credential to avoid unnecessary db queries.
        if (!isValidCredentials(credential)) {
            String reason = "Password validation failed";
            if (log.isDebugEnabled()) {
                log.debug(reason);
            }
            return getAuthenticationResult(reason);
        }
        // Resolve profile information.
        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        String sqlStmt;
        if (isCaseSensitiveUsername()) {
            sqlStmt = CaseSensitiveSQLConstants.SELECT_USER_WITH_ID_SQL;
        } else {
            sqlStmt = CaseInsensitiveSQLConstants.SELECT_USER_WITH_ID_SQL_CASE_INSENSITIVE_SQL;
        }

        boolean isAuthed = false;
        AuthenticationResult authenticationResult = new AuthenticationResult(
                AuthenticationResult.AuthenticationStatus.FAIL);
        try (Connection dbConnection = getDataBaseConnection();
             PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            prepStmt.setString(1, preferredUserNameProperty);
            prepStmt.setString(2, preferredUserNameValue);
            prepStmt.setString(3, profileName);
            int count = 0;
            try (ResultSet rs = prepStmt.executeQuery()) {
                // Handle multiple matching users for given attribute.
                while (rs.next()) {
                    count++;
                    if (count > 1) {
                        String reason = String.format("Invalid scenario. Multiple users found for the given username " +
                                "property: %s and value: %s", preferredUserNameProperty, preferredUserNameValue);
                        if (log.isDebugEnabled()) {
                            log.debug(reason);
                        }
                        return getAuthenticationResult(reason);
                    }
                    String userID = rs.getString(1);
                    String userName = rs.getString(2);
                    String storedPassword = rs.getString(3);
                    String saltValue = null;
                    if ("true".equalsIgnoreCase(
                            realmConfig.getUserStoreProperty(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
                        saltValue = rs.getString(4);
                    }

                    boolean requireChange = rs.getBoolean(5);
                    Timestamp changedTime = rs.getTimestamp(6);

                    GregorianCalendar gc = new GregorianCalendar();
                    gc.add(GregorianCalendar.HOUR, -24);
                    Date date = gc.getTime();

                    if (requireChange && changedTime.before(date)) {
                        authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.FAIL);
                        authenticationResult.setFailureReason(new FailureReason("Password change required."));
                    } else {
                        String password = preparePassword(credential, saltValue);
                        if ((storedPassword != null) && (storedPassword.equals(password))) {
                            isAuthed = true;
                            User user = getUser(userID, userName);
                            user.setPreferredUsername(preferredUserNameProperty);
                            authenticationResult = new AuthenticationResult(
                                    AuthenticationResult.AuthenticationStatus.SUCCESS);
                            authenticationResult.setAuthenticatedUser(user);
                        }
                    }
                }
            } catch (SQLException exception) {
                throw handleException(exception, ErrorMessage.ERROR_CODE_ERROR_AUTH_WITH_ID, preferredUserNameValue);
            }
        } catch (SQLException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_ERROR_AUTH_WITH_ID, preferredUserNameValue);
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Login attempt by user: %s. Login success: %s", preferredUserNameValue, isAuthed));
        }
        return authenticationResult;
    }

    protected AuthenticationResult doAuthenticateWithUserName(String userName, Object credential)
            throws UserStoreException {

        // Pre validate username to avoid unnecessary db queries.
        if (!isValidUserName(userName)) {
            String reason = "Username validation failed";
            if (log.isDebugEnabled()) {
                log.debug(reason);
            }
            return getAuthenticationResult(reason);
        }
        if (UserCoreUtil.isRegistryAnnonymousUser(userName)) {
            String reason = "Anonymous user trying to login.";
            log.error(reason);
            return getAuthenticationResult(reason);
        }
        // Pre validate credential to avoid unnecessary db queries.
        if (!isValidCredentials(credential)) {
            String reason = "Password validation failed.";
            if (log.isDebugEnabled()) {
                log.debug(reason);
            }
            return getAuthenticationResult(reason);
        }
        String tenantUuid = getTenantUuidFromTenantID(tenantId);
        String sqlStmt;
        if (isCaseSensitiveUsername()) {
            sqlStmt = CaseSensitiveSQLConstants.SELECT_USER_NAME_CASE_SENSITIVE_SQL;
        } else {
            sqlStmt = CaseInsensitiveSQLConstants.SELECT_USER_NAME_CASE_INSENSITIVE_SQL;
        }

        boolean isAuthed = false;
        AuthenticationResult authenticationResult = new AuthenticationResult(
                AuthenticationResult.AuthenticationStatus.FAIL);

        try (Connection dbConnection = getDataBaseConnection();
             PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            prepStmt.setString(1, userName);
            prepStmt.setString(2, tenantUuid);
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String userID = rs.getString(1);
                    String storedPassword = rs.getString(3);
                    String saltValue = null;
                    if ("true".equalsIgnoreCase(
                            realmConfig.getUserStoreProperty(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
                        saltValue = rs.getString(4);
                    }
                    boolean requireChange = rs.getBoolean(5);
                    Timestamp changedTime = rs.getTimestamp(6);

                    GregorianCalendar gc = new GregorianCalendar();
                    gc.add(GregorianCalendar.HOUR, -24);
                    Date date = gc.getTime();

                    // Validate Password Status.
                    if (requireChange && changedTime.before(date)) {
                        isAuthed = false;
                        authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.FAIL);
                        authenticationResult.setFailureReason(new FailureReason("Password change required."));
                    } else {
                        // Validate Authentication.
                        String password = preparePassword(credential, saltValue);
                        if ((storedPassword != null) && (storedPassword.equals(password))) {
                            isAuthed = true;
                            User user = getUser(userID, userName);
                            authenticationResult = new AuthenticationResult(
                                    AuthenticationResult.AuthenticationStatus.SUCCESS);
                            authenticationResult.setAuthenticatedUser(user);
                        }
                    }
                }
            } catch (SQLException exception) {
                throw handleException(exception, ErrorMessage.ERROR_CODE_ERROR_AUTH_WITH_USERNAME);
            }
        } catch (SQLException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_ERROR_AUTH_WITH_USERNAME);
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("Login attempt by user: %s. Login success: %s", userName, isAuthed));
        }
        return authenticationResult;
    }

    @Override
    public void doAddRoleWithID(String roleName, String[] userIDList, boolean shared) throws UserStoreException {

        // TODO: what if this is not a tenant creation path
        // Todo: in this approach we are NOT checking the user existence with the tenant id. So we can add
        //  users who are not in the tenant domain. Ideally we need to check with the user tenant association
        //  table before adding the role mapping.
        if (shared && isSharedGroupEnabled()) {
            // TODO: 12/4/20 implement the following method
            //doAddSharedRoleWithID(roleName, userIDList);
        }
        try (Connection dbConnection = getDataBaseConnection()) {
            String sqlStmt1 = CaseInsensitiveSQLConstants.ADD_ROLE_SQL;
            this.updateStringValuesToDatabase(dbConnection, sqlStmt1, roleName, tenantId);

            // Add role to the users.
            if (!ArrayUtils.isEmpty(userIDList)) {
                String type = DatabaseCreator.getDatabaseType(dbConnection);
                String sqlStmt2 = CaseInsensitiveSQLConstants.ADD_USER_TO_ROLE_WITH_ID_SQL;
                if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, tenantId, userIDList,
                            tenantId, roleName, tenantId);
                } else {
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, userIDList, roleName,
                            tenantId, tenantId);
                }
            }
        } catch (Exception exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_ADDING_ROLE_WITH_ID, roleName);
        }
    }

    @Override
    protected Map<String, Map<String, String>> getUsersPropertyValuesWithID(List<String> users, String[] propertyNames,
                                                                            String profileName)
            throws UserStoreException {

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        String[] propertyNamesSorted = propertyNames.clone();
        Arrays.sort(propertyNamesSorted);
        Map<String, Map<String, String>> usersPropertyValuesMap = new HashMap<>();
        String sqlStmt =
                buildSQLStmtWithUsersList(CaseInsensitiveSQLConstants.GET_USERS_PROPS_FOR_PROFILE_WITH_ID_SQL, users);
        try (Connection dbConnection = getDataBaseConnection();
             PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            prepStmt.setString(1, profileName);
            prepStmt.setString(2, getTenantUuidFromTenantID(tenantId));
            try (ResultSet rs = prepStmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString(2);
                    if (Arrays.binarySearch(propertyNamesSorted, name) < 0) {
                        continue;
                    }
                    String userID = rs.getString(1);
                    String value = rs.getString(3);

                    if (usersPropertyValuesMap.get(userID) != null) {
                        usersPropertyValuesMap.get(userID).put(name, value);
                    } else {
                        Map<String, String> attributes = new HashMap<>();
                        attributes.put(name, value);
                        usersPropertyValuesMap.put(userID, attributes);
                    }
                }
                return usersPropertyValuesMap;
            } catch (SQLException exception) {
                throw handleException(exception, ErrorMessage.ERROR_CODE_ERROR_GETTING_USERS_ATTRIBUTES);
            }
        } catch (SQLException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_ERROR_GETTING_USERS_ATTRIBUTES);
        }
    }

    @Override
    public Map<String, String> getUserPropertyValuesWithID(String userID, String[] propertyNames, String profileName)
            throws UserStoreException {

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        String[] propertyNamesSorted = propertyNames.clone();
        Arrays.sort(propertyNamesSorted);
        Map<String, String> map = new HashMap<>();
        String tenantUuid = getTenantUuidFromTenantID(tenantId);
        String sqlStmt = CaseInsensitiveSQLConstants.GET_USER_PROPS_FOR_PROFILE_WITH_ID_SQL;
        boolean isTenantCreationOperation = TenantMgtUtil.isTenantAdminCreationOperation();
        if (isTenantCreationOperation) {
            sqlStmt = CaseInsensitiveSQLConstants.GET_USER_PROPS_FOR_PROFILE_WITH_ID_SQL_TENANT_CREATION;
        }
        try (Connection dbConnection = getDataBaseConnection();
             PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            prepStmt.setString(1, userID);
            prepStmt.setString(2, profileName);
            if (!isTenantCreationOperation) {
                prepStmt.setString(3, tenantUuid);
            }
            ResultSet rs = prepStmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString(1);
                String value = rs.getString(2);
                if (Arrays.binarySearch(propertyNamesSorted, name) < 0) {
                    continue;
                }
                map.put(name, value);
            }
            rs.close();
            return map;
        } catch (SQLException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_ERROR_GETTING_USER_ATTRIBUTES, userID);
        }
    }

    @Override
    public List<User> doListUsersWithID(String filter, int maxItemLimit) throws UserStoreException {

        if (maxItemLimit == 0) {
            return Collections.emptyList();
        }
        maxItemLimit = resolveMaxUserListCount(maxItemLimit);
        int searchTime = getMaxSearchTimeFromUserStore();
        if (StringUtils.isNotBlank(filter)) {
            filter = filter.trim();
            filter = filter.replace("*", "%");
            filter = filter.replace("?", "_");
        } else {
            filter = "%";
        }

        String sqlStmt;
        if (isCaseSensitiveUsername()) {
            sqlStmt = CaseSensitiveSQLConstants.GET_USER_FILTER_WITH_UUID_CASE_SENSITIVE_SQL;
        } else {
            sqlStmt = CaseInsensitiveSQLConstants.GET_USER_FILTER_WITH_UUID_CASE_INSENSITIVE_SQL;
        }

        List<User> users = new ArrayList<>();
        try (Connection dbConnection = getDataBaseConnection()) {
            String tenantUuid = getTenantUuidFromTenantID(tenantId);
            try (PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
                prepStmt.setString(1, tenantUuid);
                prepStmt.setString(2, filter);
                prepStmt.setMaxRows(maxItemLimit);
                try {
                    prepStmt.setQueryTimeout(searchTime);
                } catch (Exception e) {
                    // This can be ignored since timeout method is not implemented.
                    if (log.isDebugEnabled()) {
                        log.debug(e);
                    }
                }
                ResultSet rs = prepStmt.executeQuery();
                while (rs.next()) {
                    String userID = rs.getString(1);
                    String userName = rs.getString(2);
                    if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userID)) {
                        continue;
                    }
                    User user = getUser(userID, userName);
                    users.add(user);
                }
                rs.close();
            } catch (SQLException exception) {
                if (exception instanceof SQLTimeoutException) {
                    log.error(String.format("The cause might be a time out. Hence ignored for filter: %s with max " +
                            "limit: %s", filter, maxItemLimit), exception);
                    return users;
                }
                throw handleException(exception,
                        ErrorMessage.ERROR_CODE_ERROR_WHILE_FILTERING_USERS, filter, maxItemLimit);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException | SQLException exception) {
            if (exception instanceof SQLTimeoutException) {
                log.error(String.format("The cause might be a time out. Hence ignored for filter: %s with max " +
                        "limit: %s", filter, maxItemLimit), exception);
                return users;
            }
            throw handleException(exception, ErrorMessage.ERROR_CODE_ERROR_WHILE_FILTERING_USERS, filter, maxItemLimit);
        }
        return users;
    }

    @Override
    public long doCountUsersWithClaims(String claimUri, String value) throws UserStoreException {

        if (StringUtils.isBlank(claimUri)) {
            throw new IllegalArgumentException(ErrorMessage.ERROR_CODE_EMPTY_CLAIM_URI.getMessage());
        }
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException(ErrorMessage.ERROR_CODE_EMPTY_CLAIM_VALUE.getMessage());
        }
        String sqlStmt;
        if (isUserNameClaim(claimUri)) {
            sqlStmt = CaseSensitiveSQLConstants.COUNT_USERS_WITH_USERNAME_CASE_SENSITIVE_SQL;
        } else {
            sqlStmt = CaseInsensitiveSQLConstants.COUNT_USERS_WITH_CLAIM_SQL;
        }

        String valueFilter = value;
        if (valueFilter.equals("*")) {
            valueFilter = "%";
        } else {
            valueFilter = valueFilter.trim();
            valueFilter = valueFilter.replace("*", "%");
            valueFilter = valueFilter.replace("?", "_");
        }
        try (Connection dbConnection = getDataBaseConnection()) {
            String tenantUuid = getTenantUuidFromTenantID(tenantId);
            String domainName = getMyDomainName();
            if (StringUtils.isEmpty(domainName)) {
                domainName = UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
            }

            try (PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
                if (isUserNameClaim(claimUri)) {
                    prepStmt.setString(1, tenantUuid);
                    prepStmt.setString(2, valueFilter);
                } else {
                    prepStmt.setString(1, userRealm.getClaimManager()
                            .getAttributeName(domainName, claimUri));
                    prepStmt.setString(3, valueFilter);
                    prepStmt.setString(4, UserCoreConstants.DEFAULT_PROFILE);
                }
                ResultSet resultSet = prepStmt.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getLong("RESULT");
                } else {
                    log.warn("No result for the filter: " + value);
                    return 0;
                }
            } catch (SQLException exception) {
                throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_COUNT, claimUri, value);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException | SQLException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_COUNT, claimUri, value);
        }
    }

    @Override
    protected UniqueIDPaginatedSearchResult doGetUserListWithID(Condition condition, String profileName, int limit,
                                                                int offset, String sortBy, String sortOrder)
            throws UserStoreException {

        UniqueIDPaginatedSearchResult result = new UniqueIDPaginatedSearchResult();
        if (limit == 0) {
            return result;
        }

        boolean isGroupFiltering = false;
        boolean isUsernameFiltering = false;
        boolean isClaimFiltering = false;
        // To identify Mysql multi group filter and multi claim filter.
        int totalMultiGroupFilters = 0;
        int totalMultiClaimFilters = 0;

        // Since we support only AND operation get expressions as a list.
        List<ExpressionCondition> expressionConditions = new ArrayList<>();
        getExpressionConditions(condition, expressionConditions);

        for (ExpressionCondition expressionCondition : expressionConditions) {
            if (ExpressionAttribute.ROLE.toString().equals(expressionCondition.getAttributeName())) {
                isGroupFiltering = true;
                totalMultiGroupFilters++;
            } else if (ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                isUsernameFiltering = true;
            } else {
                isClaimFiltering = true;
                totalMultiClaimFilters++;
            }
        }

        List<User> users = new ArrayList<>();
        List<User> list = new ArrayList<>();
        try (Connection dbConnection = getDataBaseConnection()) {
            String type = DatabaseCreator.getDatabaseType(dbConnection);
            if (offset <= 0) {
                offset = 0;
            } else {
                offset = offset - 1;
            }

            // Todo: Implement limit and offset calculation for DB2, ORACLE, MSSQL. Refer to the impl in
            //  UniqueIDJDBCUserStoreManager.
            SqlBuilder sqlBuilder = getQueryString(isGroupFiltering, isUsernameFiltering, isClaimFiltering,
                    expressionConditions, limit, offset, sortBy, sortOrder, profileName, type, totalMultiGroupFilters,
                    totalMultiClaimFilters);
            if ((MYSQL.equals(type) || MARIADB.equals(type)) && totalMultiGroupFilters > 1 &&
                    totalMultiClaimFilters > 1) {
                String fullQuery = sqlBuilder.getQuery();
                String[] splits = fullQuery.split("INTERSECT ");
                int startIndex = 0;
                int endIndex = 0;
                for (String query : splits) {
                    List<User> tempUserList = new ArrayList<>();
                    int occurrence = StringUtils.countMatches(query, QUERY_BINDING_SYMBOL);
                    endIndex = endIndex + occurrence;
                    try (PreparedStatement prepStmt = dbConnection.prepareStatement(query)) {
                        populatePrepareStatement(sqlBuilder, prepStmt, startIndex, endIndex);
                        ResultSet rs = prepStmt.executeQuery();
                        while (rs.next()) {
                            String userID = rs.getString(1);
                            String userName = rs.getString(2);
                            User user = getUser(userID, userName);
                            tempUserList.add(user);
                        }
                        if (startIndex == 0) {
                            list = tempUserList;
                        } else {
                            list.retainAll(tempUserList);
                        }
                        startIndex += occurrence;
                    }
                }
            } else {
                try (PreparedStatement prepStmt = dbConnection.prepareStatement(sqlBuilder.getQuery())) {
                    int occurrence = StringUtils.countMatches(sqlBuilder.getQuery(), "?");
                    populatePrepareStatement(sqlBuilder, prepStmt, 0, occurrence);
                    ResultSet rs = prepStmt.executeQuery();
                    while (rs.next()) {
                        String userID = rs.getString(1);
                        String userName = rs.getString(2);
                        User user = getUser(userID, userName);
                        list.add(user);
                    }
                }
            }
            if (list.size() > 0) {
                users = list;
            }
            result.setUsers(users);
        } catch (Exception exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_GETTING_USERS_WITH_MULTI_ATTR);
        }
        return result;
    }

    protected SqlBuilder getQueryString(boolean isGroupFiltering, boolean isUsernameFiltering, boolean isClaimFiltering,
                                        List<ExpressionCondition> expressionConditions, int limit, int offset,
                                        String sortBy, String sortOrder, String profileName, String dbType,
                                        int totalMultiGroupFilters, int totalMultiClaimFilters)
            throws UserStoreException {

        StringBuilder sqlStatement;
        SqlBuilder sqlBuilder;
        boolean hitGroupFilter = false;
        boolean hitClaimFilter = false;
        int groupFilterCount = 0;
        int claimFilterCount = 0;

        String tenantUuid = getTenantUuidFromTenantID(tenantId);

        // Todo: Need to implement for DB2, ORACLE, MSSQL. Refer to the impl in UniqueIDJDBCUserStoreManager.
        if (isGroupFiltering && isUsernameFiltering && isClaimFiltering || isGroupFiltering && isClaimFiltering) {
            sqlStatement = new StringBuilder(
                    "SELECT DISTINCT U.UM_USER_ID, U.UM_USER_NAME FROM ASG_USER_TENANT_ASC AS AUTA INNER JOIN " +
                            "UM_ROLE R INNER JOIN UM_USER_ROLE UR INNER JOIN UM_USER U INNER JOIN UM_USER_ATTRIBUTE " +
                            "UA ON R.UM_ID = UR.UM_ROLE_ID AND UR.UM_USER_ID = U.UM_ID AND U.UM_ID = UA.UM_USER_ID");
            sqlBuilder = new SqlBuilder(sqlStatement).where("R.UM_TENANT_ID = ?", tenantId)
                    .where("UR.UM_TENANT_ID = ?", tenantId).where("UA.UM_PROFILE_ID = ?", profileName).
                            where("AUTA.ASG_TENANT_UUID = ?", tenantUuid);
        } else if (isGroupFiltering && isUsernameFiltering || isGroupFiltering) {
            sqlStatement = new StringBuilder(
                    "SELECT DISTINCT U.UM_USER_ID, U.UM_USER_NAME FROM ASG_USER_TENANT_ASC AS AUTA INNER JOIN " +
                            "UM_ROLE R INNER JOIN UM_USER_ROLE UR INNER JOIN UM_USER U ON R.UM_ID = UR.UM_ROLE_ID " +
                            "AND UR.UM_USER_ID=U.UM_ID");
            sqlBuilder = new SqlBuilder(sqlStatement).where("R.UM_TENANT_ID = ?", tenantId)
                    .where("UR.UM_TENANT_ID = ?", tenantId).where("AUTA.ASG_TENANT_UUID = ?", tenantUuid);
        } else if (isUsernameFiltering && isClaimFiltering || isClaimFiltering) {
            sqlStatement = new StringBuilder(
                    "SELECT DISTINCT U.UM_USER_ID, U.UM_USER_NAME FROM ASG_USER_TENANT_ASC AUTA " +
                            "INNER JOIN UM_USER U ON U.UM_USER_ID = AUTA.ASG_USER_UUID INNER JOIN "
                            + "UM_USER_ATTRIBUTE UA ON U.UM_ID = UA.UM_USER_ID");
            sqlBuilder = new SqlBuilder(sqlStatement).where("UA.UM_PROFILE_ID = ?", profileName).
                    where("AUTA.ASG_TENANT_UUID = ?", tenantUuid);
        } else if (isUsernameFiltering) {
            sqlStatement = new StringBuilder("SELECT U.UM_USER_ID, U.UM_USER_NAME FROM UM_USER U INNER JOIN " +
                    "ASG_USER_TENANT_ASC AUTA ON U.UM_USER_ID=AUTA.ASG_USER_UUID ");
            sqlBuilder = new SqlBuilder(sqlStatement).where("AUTA.ASG_TENANT_UUID=?", tenantUuid);
        } else {
            throw new UserStoreException(ErrorMessage.ERROR_CODE_INVALID_FILTER_CONDITION.getMessage(),
                    ErrorMessage.ERROR_CODE_INVALID_FILTER_CONDITION.getCode());
        }

        SqlBuilder header = new SqlBuilder(new StringBuilder(sqlBuilder.getSql()));
        addingWheres(sqlBuilder, header);

        for (ExpressionCondition expressionCondition : expressionConditions) {
            if (ExpressionAttribute.ROLE.toString().equals(expressionCondition.getAttributeName())) {
                if (!(MYSQL.equals(dbType) || MARIADB.equals(dbType)) || totalMultiGroupFilters > 1
                        && totalMultiClaimFilters > 1) {
                    multiGroupQueryBuilder(sqlBuilder, header, hitGroupFilter, expressionCondition);
                    hitGroupFilter = true;
                } else {
                    multiGroupMySqlQueryBuilder(sqlBuilder, groupFilterCount, expressionCondition);
                    groupFilterCount++;
                }
            } else if (ExpressionOperation.EQ.toString().equals(expressionCondition.getOperation())
                    && ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                if (isCaseSensitiveUsername()) {
                    sqlBuilder.where("U.UM_USER_NAME = ?", expressionCondition.getAttributeValue());
                } else {
                    sqlBuilder.where("U.UM_USER_NAME = LOWER(?)", expressionCondition.getAttributeValue());
                }
            } else if (ExpressionOperation.CO.toString().equals(expressionCondition.getOperation())
                    && ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                if (isCaseSensitiveUsername()) {
                    sqlBuilder.where("U.UM_USER_NAME LIKE ?", "%" +
                            expressionCondition.getAttributeValue() + "%");
                } else {
                    sqlBuilder
                            .where("U.UM_USER_NAME LIKE LOWER(?)", "%" +
                                    expressionCondition.getAttributeValue() + "%");
                }
            } else if (ExpressionOperation.EW.toString().equals(expressionCondition.getOperation())
                    && ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                if (isCaseSensitiveUsername()) {
                    sqlBuilder.where("U.UM_USER_NAME LIKE ?", "%" + expressionCondition.getAttributeValue());
                } else {
                    sqlBuilder.where("U.UM_USER_NAME LIKE LOWER(?)", "%" +
                            expressionCondition.getAttributeValue());
                }
            } else if (ExpressionOperation.SW.toString().equals(expressionCondition.getOperation())
                    && ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                if (isCaseSensitiveUsername()) {
                    sqlBuilder.where("U.UM_USER_NAME LIKE ?", expressionCondition.getAttributeValue() + "%");
                } else {
                    sqlBuilder.where("U.UM_USER_NAME LIKE LOWER(?)",
                            expressionCondition.getAttributeValue() + "%");
                }
            } else {
                // Claim filtering
                if (!(MYSQL.equals(dbType) || MARIADB.equals(dbType)) || totalMultiGroupFilters > 1
                        && totalMultiClaimFilters > 1) {
                    multiClaimQueryBuilder(sqlBuilder, header, hitClaimFilter, expressionCondition);
                    hitClaimFilter = true;
                } else {
                    multiClaimMySqlQueryBuilder(sqlBuilder, claimFilterCount, expressionCondition);
                    claimFilterCount++;
                }
            }
        }

        if (MYSQL.equals(dbType) || MARIADB.equals(dbType)) {
            sqlBuilder.updateSql(" GROUP BY U.UM_USER_NAME, U.UM_USER_ID ");
            if (groupFilterCount > 0 && claimFilterCount > 0) {
                sqlBuilder.updateSql(" HAVING (COUNT(DISTINCT R.UM_ROLE_NAME) = " + groupFilterCount +
                        " AND COUNT(DISTINCT UA.UM_ATTR_VALUE) = " + claimFilterCount + ")");
            } else if (groupFilterCount > 0) {
                sqlBuilder.updateSql(" HAVING COUNT(DISTINCT R.UM_ROLE_NAME) = " + groupFilterCount);
            } else if (claimFilterCount > 0) {
                sqlBuilder.updateSql(" HAVING COUNT(DISTINCT UA.UM_ATTR_VALUE) = " + claimFilterCount);
            }
        }

        if (!((MYSQL.equals(dbType) || MARIADB.equals(dbType)) && totalMultiGroupFilters > 1 &&
                totalMultiClaimFilters > 1)) {
            if (DB2.equals(dbType)) {
                sqlBuilder.setTail(") AS p) WHERE rn BETWEEN ? AND ?", limit, offset);
            } else if (MSSQL.equals(dbType)) {
                if (isClaimFiltering && !isGroupFiltering && !isUsernameFiltering) {
                    // Handle multi attribute filtering without username filtering.
                    sqlBuilder.setTail(") AS Q) AS S) AS R) AS P WHERE P.RowNum BETWEEN ? AND ?", limit, offset);
                } else {
                    sqlBuilder.setTail(") AS R) AS P WHERE P.RowNum BETWEEN ? AND ?", limit, offset);
                }
            } else if (ORACLE.equals(dbType)) {
                sqlBuilder.setTail(" ORDER BY UM_USER_NAME) where rownum <= ?) WHERE  rnum > ?", limit, offset);
            } else {
                sqlBuilder.setTail(" ORDER BY UM_USER_NAME ASC LIMIT ? OFFSET ?", limit, offset);
            }
        }
        return sqlBuilder;
    }

    private void populatePrepareStatement(SqlBuilder sqlBuilder, PreparedStatement prepStmt, int startIndex,
                                          int endIndex) throws SQLException {

        Map<Integer, Integer> integerParameters = sqlBuilder.getIntegerParameters();
        Map<Integer, String> stringParameters = sqlBuilder.getStringParameters();
        Map<Integer, Long> longParameters = sqlBuilder.getLongParameters();

        for (Map.Entry<Integer, Integer> entry : integerParameters.entrySet()) {
            if (entry.getKey() > startIndex && entry.getKey() <= endIndex) {
                prepStmt.setInt(entry.getKey() - startIndex, entry.getValue());
            }
        }

        for (Map.Entry<Integer, String> entry : stringParameters.entrySet()) {
            if (entry.getKey() > startIndex && entry.getKey() <= endIndex) {
                prepStmt.setString(entry.getKey() - startIndex, entry.getValue());
            }
        }

        for (Map.Entry<Integer, Long> entry : longParameters.entrySet()) {
            if (entry.getKey() > startIndex && entry.getKey() <= endIndex) {
                prepStmt.setLong(entry.getKey() - startIndex, entry.getValue());
            }
        }
    }

    private void multiClaimMySqlQueryBuilder(SqlBuilder sqlBuilder, int claimFilterCount,
                                             ExpressionCondition expressionCondition) {

        if (claimFilterCount == 0) {
            buildClaimWhereConditions(sqlBuilder, expressionCondition.getAttributeName(),
                    expressionCondition.getOperation(), expressionCondition.getAttributeValue());
        } else {
            buildClaimConditionWithOROperator(sqlBuilder, expressionCondition.getAttributeName(),
                    expressionCondition.getOperation(), expressionCondition.getAttributeValue());
        }
    }

    private void multiClaimQueryBuilder(SqlBuilder sqlBuilder, SqlBuilder header, boolean hitFirstRound,
                                        ExpressionCondition expressionCondition) {

        if (hitFirstRound) {
            sqlBuilder.updateSql(" INTERSECT " + header.getSql());
            addingWheres(header, sqlBuilder);
        }
        buildClaimWhereConditions(sqlBuilder, expressionCondition.getAttributeName(),
                expressionCondition.getOperation(), expressionCondition.getAttributeValue());
    }

    private void multiGroupMySqlQueryBuilder(SqlBuilder sqlBuilder, int groupFilterCount,
                                             ExpressionCondition expressionCondition) {

        if (groupFilterCount == 0) {
            buildGroupWhereConditions(sqlBuilder, expressionCondition.getOperation(),
                    expressionCondition.getAttributeValue());
        } else {
            buildGroupConditionWithOROperator(sqlBuilder, expressionCondition.getOperation(),
                    expressionCondition.getAttributeValue());
        }
    }

    private void multiGroupQueryBuilder(SqlBuilder sqlBuilder, SqlBuilder header, boolean hitFirstRound,
                                        ExpressionCondition expressionCondition) {

        if (hitFirstRound) {
            sqlBuilder.updateSql(" INTERSECT " + header.getSql());
            addingWheres(header, sqlBuilder);
        }
        buildGroupWhereConditions(sqlBuilder, expressionCondition.getOperation(),
                expressionCondition.getAttributeValue());
    }

    private void addingWheres(SqlBuilder baseSqlBuilder, SqlBuilder newSqlBuilder) {

        for (int i = 0; i < baseSqlBuilder.getWheres().size(); i++) {
            if (baseSqlBuilder.getIntegerParameters().containsKey(i + 1)) {
                newSqlBuilder
                        .where(baseSqlBuilder.getWheres().get(i), baseSqlBuilder.getIntegerParameters().get(i + 1));
            } else if (baseSqlBuilder.getStringParameters().containsKey(i + 1)) {
                newSqlBuilder.where(baseSqlBuilder.getWheres().get(i), baseSqlBuilder.getStringParameters().get(i + 1));
            } else if (baseSqlBuilder.getLongParameters().containsKey(i + 1)) {
                newSqlBuilder.where(baseSqlBuilder.getWheres().get(i), baseSqlBuilder.getLongParameters().get(i + 1));
            }
        }
    }

    private void getExpressionConditions(Condition condition, List<ExpressionCondition> expressionConditions) {

        if (condition instanceof ExpressionCondition) {
            expressionConditions.add((ExpressionCondition) condition);
        } else if (condition instanceof OperationalCondition) {
            Condition leftCondition = ((OperationalCondition) condition).getLeftCondition();
            getExpressionConditions(leftCondition, expressionConditions);
            Condition rightCondition = ((OperationalCondition) condition).getRightCondition();
            getExpressionConditions(rightCondition, expressionConditions);
        }
    }

    private void buildGroupConditionWithOROperator(SqlBuilder sqlBuilder, String operation, String value) {

        if (ExpressionOperation.EQ.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("R.UM_ROLE_NAME = ?", value);
        } else if (ExpressionOperation.EW.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("R.UM_ROLE_NAME LIKE ?", "%" + value);
        } else if (ExpressionOperation.CO.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("R.UM_ROLE_NAME LIKE ?", "%" + value + "%");
        } else if (ExpressionOperation.SW.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("R.UM_ROLE_NAME LIKE ?", value + "%");
        }
    }

    private void buildClaimWhereConditions(SqlBuilder sqlBuilder, String attributeName, String operation,
                                           String attributeValue) {

        sqlBuilder.where("UA.UM_ATTR_NAME = ?", attributeName);
        if (ExpressionOperation.EQ.toString().equals(operation)) {
            sqlBuilder.where("UA.UM_ATTR_VALUE = ?", attributeValue);
        } else if (ExpressionOperation.EW.toString().equals(operation)) {
            sqlBuilder.where("UA.UM_ATTR_VALUE LIKE ?", "%" + attributeValue);
        } else if (ExpressionOperation.CO.toString().equals(operation)) {
            sqlBuilder.where("UA.UM_ATTR_VALUE LIKE ?", "%" + attributeValue + "%");
        } else if (ExpressionOperation.SW.toString().equals(operation)) {
            sqlBuilder.where("UA.UM_ATTR_VALUE LIKE ?", attributeValue + "%");
        }
    }

    private void buildGroupWhereConditions(SqlBuilder sqlBuilder, String operation, String value) {

        if (ExpressionOperation.EQ.toString().equals(operation)) {
            sqlBuilder.where("R.UM_ROLE_NAME = ?", value);
        } else if (ExpressionOperation.EW.toString().equals(operation)) {
            sqlBuilder.where("R.UM_ROLE_NAME LIKE ?", "%" + value);
        } else if (ExpressionOperation.CO.toString().equals(operation)) {
            sqlBuilder.where("R.UM_ROLE_NAME LIKE ?", "%" + value + "%");
        } else if (ExpressionOperation.SW.toString().equals(operation)) {
            sqlBuilder.where("R.UM_ROLE_NAME LIKE ?", value + "%");
        }
    }

    private void buildClaimConditionWithOROperator(SqlBuilder sqlBuilder, String attributeName, String operation,
                                                   String attributeValue) {

        sqlBuilder.updateSqlWithOROperation("UA.UM_ATTR_NAME = ?", attributeName);
        if (ExpressionOperation.EQ.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("UA.UM_ATTR_VALUE = ?", attributeValue);
        } else if (ExpressionOperation.EW.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("UA.UM_ATTR_VALUE LIKE ?", "%" + attributeValue);
        } else if (ExpressionOperation.CO.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("UA.UM_ATTR_VALUE LIKE ?", "%" + attributeValue + "%");
        } else if (ExpressionOperation.SW.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("UA.UM_ATTR_VALUE LIKE ?", attributeValue + "%");
        }
    }

    private boolean isUserNameClaim(String claim) {

        return AbstractUserStoreManager.USERNAME_CLAIM_URI.equals(claim);
    }

    private boolean isCaseSensitiveUsername() {

        String isUsernameCaseInsensitiveString =
                realmConfig.getUserStoreProperty(UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME);
        return !Boolean.parseBoolean(isUsernameCaseInsensitiveString);
    }

    private int getMaxSearchTimeFromUserStore() {

        try {
            return Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
        } catch (NumberFormatException e) {
            return UserCoreConstants.MAX_SEARCH_TIME;
        }
    }

    private int resolveMaxUserListCount(int maxItemLimit) {

        int givenMax = getMaxUserListCount();
        if (maxItemLimit < 0 || maxItemLimit > givenMax) {
            maxItemLimit = givenMax;
        }
        return maxItemLimit;
    }

    private int getMaxUserListCount() {

        try {
            return Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
        } catch (NumberFormatException e) {
            return UserCoreConstants.MAX_USER_ROLE_LIST;
        }
    }

    private Connection getDataBaseConnection() throws UserStoreException {

        try {
            return getDBConnection();
        } catch (SQLException e) {
            String errorMessage = ErrorMessage.ERROR_CODE_ERROR_GETTING_DB_CONNECTION.getDescription();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage,
                    ErrorMessage.ERROR_CODE_ERROR_GETTING_DB_CONNECTION.getCode(), e);
        }
    }

    private UserStoreException handleException(Exception exception, ErrorMessage errorMessage, Object... params) {

        String error = String.format(errorMessage.getDescription(), params);
        if (log.isDebugEnabled()) {
            log.debug(error, exception);
        }
        return new UserStoreException(error, errorMessage.getCode(), exception);
    }

    /**
     * Update values to the data base. Can be used to INSERT, UPDATE and DELETE statements.
     *
     * @param dbConnection Connection.
     * @param sqlStmt      SQL statement.
     * @param params       Params.
     * @throws UserStoreException If an error occurred while executing the query.
     */
    private void updateStringValuesToDatabase(Connection dbConnection, String sqlStmt, Object... params)
            throws UserStoreException {

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
                throw new UserStoreException(msg, ErrorMessage.ERROR_CODE_CONSTRAIN_VIOLATION.getCode(), e);
            } else {
                // Errors related to other SQL Exceptions.
                throw new UserStoreException(msg, ErrorMessage.ERROR_CODE_UPDATING_DATABASE.getCode(), e);
            }
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    private AuthenticationResult getAuthenticationResult(String reason) {

        AuthenticationResult authenticationResult = new AuthenticationResult(
                AuthenticationResult.AuthenticationStatus.FAIL);
        authenticationResult.setFailureReason(new FailureReason(reason));
        return authenticationResult;
    }

    private String getTenantUuidFromTenantID(int tenantID) throws UserStoreException {

        String tenantUuid;
        try {
            tenantUuid = AsgardioUserStoreDataHolder.getRealmService().getTenantManager().
                    getTenant(tenantID).getTenantUniqueID();
            if (StringUtils.isBlank(tenantUuid)) {
                throw new UserStoreException(String.format(ErrorMessage.ERROR_CODE_INVALID_TENANT_ID.getDescription(),
                        tenantID), ErrorMessage.ERROR_CODE_INVALID_TENANT_ID.getCode());
            }
        } catch (org.wso2.carbon.user.api.UserStoreException exception) {
            throw handleException(exception, ErrorMessage.ERROR_CODE_ERROR_GETTING_TENANT_UUID, tenantId);
        }
        return tenantUuid;
    }

    private String buildSQLStmtWithUsersList(String sqlStmt, List<String> users) {

        StringBuilder usernameParameter = new StringBuilder();
        for (int i = 0; i < users.size(); i++) {
            usernameParameter.append("'").append(users.get(i)).append("'");
            if (i != users.size() - 1) {
                usernameParameter.append(",");
            }
        }
        return sqlStmt.replaceFirst("\\?", usernameParameter.toString());
    }

    @Override
    public org.wso2.carbon.user.api.Properties getDefaultUserStoreProperties() {

        // The properties have been set to none to disable query change and properties changes from the UI.
        Properties properties = new Properties();
        properties.setMandatoryProperties(Constants.USER_STORE_MANAGER_MANDATORY_PROPERTIES.toArray(new Property[0]));
        properties.setOptionalProperties(new Property[0]);
        properties.setAdvancedProperties(new Property[0]);
        return properties;
    }
}

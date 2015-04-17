/*
 * Copyright 2009-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.client.lib;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.client.lib.archive.ApplicationArchive;
import org.cloudfoundry.client.lib.domain.ApplicationLog;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudAdminBuildpack;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudApplication.DebugMode;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudEvent;
import org.cloudfoundry.client.lib.domain.CloudInfo;
import org.cloudfoundry.client.lib.domain.CloudOrganization;
import org.cloudfoundry.client.lib.domain.CloudQuota;
import org.cloudfoundry.client.lib.domain.CloudRoute;
import org.cloudfoundry.client.lib.domain.CloudSecurityGroup;
import org.cloudfoundry.client.lib.domain.CloudSecurityRules;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceBroker;
import org.cloudfoundry.client.lib.domain.CloudServiceInstance;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.cloudfoundry.client.lib.domain.CloudSpaceQuota;
import org.cloudfoundry.client.lib.domain.CloudStack;
import org.cloudfoundry.client.lib.domain.CloudUser;
import org.cloudfoundry.client.lib.domain.CloudUserNoUaa;
import org.cloudfoundry.client.lib.domain.CrashesInfo;
import org.cloudfoundry.client.lib.domain.InstancesInfo;
import org.cloudfoundry.client.lib.domain.Staging;
import org.cloudfoundry.client.lib.rest.CloudControllerClient;
import org.cloudfoundry.client.lib.rest.CloudControllerClientFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.Assert;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * A Java client to exercise the Cloud Foundry API.
 *
 * @author Ramnivas Laddad
 * @author A.B.Srinivasan
 * @author Jennifer Hickey
 * @author Dave Syer
 * @author Thomas Risberg
 * @author Alexander Orlov
 */
public class CloudFoundryClient implements CloudFoundryOperations {

	private CloudControllerClient cc;

	private CloudInfo info;

	/**
	 * Construct client for anonymous user. Useful only to get to the '/info' endpoint.
	 */

	public CloudFoundryClient(URL cloudControllerUrl) {
		this(null, cloudControllerUrl, null, (HttpProxyConfiguration) null, false);
	}

	public CloudFoundryClient(URL cloudControllerUrl, boolean trustSelfSignedCerts) {
		this(null, cloudControllerUrl, null, (HttpProxyConfiguration) null, trustSelfSignedCerts);
	}

	public CloudFoundryClient(URL cloudControllerUrl, HttpProxyConfiguration httpProxyConfiguration) {
		this(null, cloudControllerUrl, null, httpProxyConfiguration, false);
	}

	public CloudFoundryClient(URL cloudControllerUrl, HttpProxyConfiguration httpProxyConfiguration,
	                          boolean trustSelfSignedCerts) {
		this(null, cloudControllerUrl, null, httpProxyConfiguration, trustSelfSignedCerts);
	}

	/**
	 * Construct client without a default org and space.
	 */

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl) {
		this(credentials, cloudControllerUrl, null, (HttpProxyConfiguration) null, false);
	}

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl,
	                          boolean trustSelfSignedCerts) {
		this(credentials, cloudControllerUrl, null, (HttpProxyConfiguration) null, trustSelfSignedCerts);
	}

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl,
	                          HttpProxyConfiguration httpProxyConfiguration) {
		this(credentials, cloudControllerUrl, null, httpProxyConfiguration, false);
	}

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl,
	                          HttpProxyConfiguration httpProxyConfiguration, boolean trustSelfSignedCerts) {
		this(credentials, cloudControllerUrl, null, httpProxyConfiguration, trustSelfSignedCerts);
	}

	/**
	 * Construct a client with a default CloudSpace.
	 */

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl, CloudSpace sessionSpace) {
		this(credentials, cloudControllerUrl, sessionSpace, null, false);
    }

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl, CloudSpace sessionSpace,
	                          boolean trustSelfSignedCerts) {
		this(credentials, cloudControllerUrl, sessionSpace, null, trustSelfSignedCerts);
	}

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl, CloudSpace sessionSpace,
	                          HttpProxyConfiguration httpProxyConfiguration) {
		this(credentials, cloudControllerUrl, sessionSpace, httpProxyConfiguration, false);
	}

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl, CloudSpace sessionSpace,
	                          HttpProxyConfiguration httpProxyConfiguration, boolean trustSelfSignedCerts) {
		Assert.notNull(cloudControllerUrl, "URL for cloud controller cannot be null");
		CloudControllerClientFactory cloudControllerClientFactory =
				new CloudControllerClientFactory(httpProxyConfiguration, trustSelfSignedCerts);
		this.cc = cloudControllerClientFactory.newCloudController(cloudControllerUrl, credentials, sessionSpace);
	}

	/**
	 * Construct a client with a default space name and org name.
	 */

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl, String orgName, String spaceName) {
		this(credentials, cloudControllerUrl, orgName, spaceName, null, false);
	}

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl, String orgName, String spaceName,
	                          boolean trustSelfSignedCerts) {
		this(credentials, cloudControllerUrl, orgName, spaceName, null, trustSelfSignedCerts);
	}

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl, String orgName, String spaceName,
							  HttpProxyConfiguration httpProxyConfiguration) {
		this(credentials, cloudControllerUrl, orgName, spaceName, httpProxyConfiguration, false);
	}

	public CloudFoundryClient(CloudCredentials credentials, URL cloudControllerUrl, String orgName, String spaceName,
	                          HttpProxyConfiguration httpProxyConfiguration, boolean trustSelfSignedCerts) {
		Assert.notNull(cloudControllerUrl, "URL for cloud controller cannot be null");
		CloudControllerClientFactory cloudControllerClientFactory =
				new CloudControllerClientFactory(httpProxyConfiguration, trustSelfSignedCerts);
		this.cc = cloudControllerClientFactory.newCloudController(cloudControllerUrl, credentials, orgName, spaceName);
	}

	/**
	 * Construct a client with a pre-configured CloudControllerClient
	 */
	public CloudFoundryClient(CloudControllerClient cc) {
		this.cc = cc;
	}

	public void setResponseErrorHandler(ResponseErrorHandler errorHandler) {
		cc.setResponseErrorHandler(errorHandler);
	}

	public URL getCloudControllerUrl() {
		return cc.getCloudControllerUrl();
	}

	public CloudInfo getCloudInfo() {
		if (info == null) {
			info = cc.getInfo();
		}
		return info;
	}

	public List<CloudSpace> getSpaces() {
		return cc.getSpaces();
	}

	public List<CloudOrganization> getOrganizations() {
		return cc.getOrganizations();
	}

	public void register(String email, String password) {
		cc.register(email, password);
	}

	public void updatePassword(String newPassword) {
		cc.updatePassword(newPassword);
	}

	public void updatePassword(CloudCredentials credentials, String newPassword) {
		cc.updatePassword(credentials, newPassword);
	}

	public void unregister() {
		cc.unregister();
	}

	public OAuth2AccessToken login() {
		return cc.login();
	}

	public void logout() {
		cc.logout();
	}

	public List<CloudApplication> getApplications() {
		return cc.getApplications();
	}

	public CloudApplication getApplication(String appName) {
		return cc.getApplication(appName);
	}

	public CloudApplication getApplication(UUID appGuid) {
		return cc.getApplication(appGuid);
	}

	public ApplicationStats getApplicationStats(String appName) {
		return cc.getApplicationStats(appName);
	}

	public void createApplication(String appName, Staging staging, Integer memory, List<String> uris,
								  List<String> serviceNames) {
		cc.createApplication(appName, staging, memory, uris, serviceNames);
	}

	public void createApplication(String appName, Staging staging, Integer disk, Integer memory, List<String> uris,
								  List<String> serviceNames) {
		cc.createApplication(appName, staging, disk, memory, uris, serviceNames);
	}

	public void createService(CloudService service) {
		cc.createService(service);
	}

	public void createUserProvidedService(CloudService service, Map<String, Object> credentials) {
		cc.createUserProvidedService(service, credentials);
	}

	@Override
	public List<CloudRoute> deleteOrphanedRoutes() {
    	return cc.deleteOrphanedRoutes();
	}

	public void uploadApplication(String appName, String file) throws IOException {
		cc.uploadApplication(appName, new File(file), null);
	}

	public void uploadApplication(String appName, File file) throws IOException {
		cc.uploadApplication(appName, file, null);
	}

	public void uploadApplication(String appName, File file, UploadStatusCallback callback) throws IOException {
		cc.uploadApplication(appName, file, callback);
	}

	public void uploadApplication(String appName, ApplicationArchive archive) throws IOException {
		cc.uploadApplication(appName, archive, null);
	}

	public void uploadApplication(String appName, ApplicationArchive archive, UploadStatusCallback callback) throws IOException {
		cc.uploadApplication(appName, archive, callback);
	}

	public StartingInfo startApplication(String appName) {
		return cc.startApplication(appName);
	}

	public void debugApplication(String appName, DebugMode mode) {
		cc.debugApplication(appName, mode);
	}

	public void stopApplication(String appName) {
		cc.stopApplication(appName);
	}

	public StartingInfo restartApplication(String appName) {
		return cc.restartApplication(appName);
	}

	public void deleteApplication(String appName) {
		cc.deleteApplication(appName);
	}

	public void deleteAllApplications() {
		cc.deleteAllApplications();
	}

	public void deleteAllServices() {
		cc.deleteAllServices();
	}

	public void updateApplicationDiskQuota(String appName, int disk) {
		cc.updateApplicationDiskQuota(appName, disk);
	}

	public void updateApplicationMemory(String appName, int memory) {
		cc.updateApplicationMemory(appName, memory);
	}

	public void updateApplicationInstances(String appName, int instances) {
		cc.updateApplicationInstances(appName, instances);
	}

	public void updateApplicationServices(String appName, List<String> services) {
		cc.updateApplicationServices(appName, services);
	}

	public void updateApplicationStaging(String appName, Staging staging) {
		cc.updateApplicationStaging(appName, staging);
	}

	public void updateApplicationUris(String appName, List<String> uris) {
		cc.updateApplicationUris(appName, uris);
	}

	public void updateApplicationEnv(String appName, Map<String, String> env) {
		cc.updateApplicationEnv(appName, env);
	}

	public void updateApplicationEnv(String appName, List<String> env) {
		cc.updateApplicationEnv(appName, env);
	}

	/**
	 * @deprecated use {@link #streamLogs(String, ApplicationLogListener)} or {@link #getRecentLogs(String)}
	 */
	public Map<String, String> getLogs(String appName) {
		return cc.getLogs(appName);
	}

	public StreamingLogToken streamLogs(String appName, ApplicationLogListener listener) {
	    return cc.streamLogs(appName, listener);
	}

	public List<ApplicationLog> getRecentLogs(String appName) {
		return cc.getRecentLogs(appName);
	}

	/**
	 * @deprecated use {@link #streamLogs(String, ApplicationLogListener)} or {@link #getRecentLogs(String)}
	 */
	public Map<String, String> getCrashLogs(String appName) {
		return cc.getCrashLogs(appName);
	}

	public String getStagingLogs(StartingInfo info, int offset) {
		return cc.getStagingLogs(info, offset);
	}

	public String getFile(String appName, int instanceIndex, String filePath) {
		return cc.getFile(appName, instanceIndex, filePath, 0, -1);
	}

	public String getFile(String appName, int instanceIndex, String filePath, int startPosition) {
		Assert.isTrue(startPosition >= 0,
				startPosition + " is not a valid value for start position, it should be 0 or greater.");
		return cc.getFile(appName, instanceIndex, filePath, startPosition, -1);
	}

	public String getFile(String appName, int instanceIndex, String filePath, int startPosition, int endPosition) {
		Assert.isTrue(startPosition >= 0,
				startPosition + " is not a valid value for start position, it should be 0 or greater.");
		Assert.isTrue(endPosition > startPosition,
				endPosition + " is not a valid value for end position, it should be greater than startPosition " +
						"which is " + startPosition + ".");
		return cc.getFile(appName, instanceIndex, filePath, startPosition, endPosition - 1);
	}

	public void openFile(String appName, int instanceIndex, String filePath, ClientHttpResponseCallback callback) {
		cc.openFile(appName, instanceIndex, filePath, callback);
	}

	public String getFileTail(String appName, int instanceIndex, String filePath, int length) {
		Assert.isTrue(length > 0, length + " is not a valid value for length, it should be 1 or greater.");
		return cc.getFile(appName, instanceIndex, filePath, -1, length);
	}

	// list services, un/provision services, modify instance

	public List<CloudService> getServices() {
		return cc.getServices();
	}

	public List<CloudServiceBroker> getServiceBrokers() {
		return cc.getServiceBrokers();
	}

	public CloudServiceBroker getServiceBroker(String name) {
		return cc.getServiceBroker(name);
	}

	public void createServiceBroker(CloudServiceBroker serviceBroker) {
		cc.createServiceBroker(serviceBroker);
	}

	public void updateServiceBroker(CloudServiceBroker serviceBroker) {
		cc.updateServiceBroker(serviceBroker);
	}

	@Override
	public void deleteServiceBroker(String name) {
		cc.deleteServiceBroker(name);
	}

	@Override
	public void updateServicePlanVisibilityForBroker(String name, boolean visibility) {
		cc.updateServicePlanVisibilityForBroker(name, visibility);
	}

    public CloudService getService(String service) {
		return cc.getService(service);
	}

	public void deleteService(String service) {
		cc.deleteService(service);
	}

	public List<CloudServiceOffering> getServiceOfferings() {
		return cc.getServiceOfferings();
	}

	public void bindService(String appName, String serviceName) {
		cc.bindService(appName, serviceName);
	}

	public void unbindService(String appName, String serviceName) {
		cc.unbindService(appName, serviceName);
	}

	public InstancesInfo getApplicationInstances(String appName) {
		return cc.getApplicationInstances(appName);
	}

	public InstancesInfo getApplicationInstances(CloudApplication app) {
		return cc.getApplicationInstances(app);
	}

	public CrashesInfo getCrashes(String appName) {
		return cc.getCrashes(appName);
	}

	public List<CloudStack> getStacks() {
		return cc.getStacks();
	}

	public CloudStack getStack(String name) {
		return cc.getStack(name);
	}

	public void rename(String appName, String newName) {
		cc.rename(appName, newName);
	}

	public List<CloudDomain> getDomainsForOrg() {
		return cc.getDomainsForOrg();
	}

	public List<CloudDomain> getPrivateDomains() {
		return cc.getPrivateDomains();
	}

	public List<CloudDomain> getSharedDomains() {
		return cc.getSharedDomains();
	}

	public List<CloudDomain> getDomains() {
		return cc.getDomains();
	}

	public CloudDomain getDefaultDomain() {
		return cc.getDefaultDomain();
	}

	public void addDomain(String domainName) {
		cc.addDomain(domainName);
	}

	public void deleteDomain(String domainName) {
		cc.deleteDomain(domainName);
	}

	public void removeDomain(String domainName) {
		cc.removeDomain(domainName);
	}

	public List<CloudRoute> getRoutes(String domainName) {
		return cc.getRoutes(domainName);
	}

	public void addRoute(String host, String domainName) {
		cc.addRoute(host, domainName);
	}

	public void deleteRoute(String host, String domainName) {
		cc.deleteRoute(host, domainName);
	}

	public void registerRestLogListener(RestLogCallback callBack) {
		cc.registerRestLogListener(callBack);
	}

	public void unRegisterRestLogListener(RestLogCallback callBack) {
		cc.unRegisterRestLogListener(callBack);
	}
	
	public CloudOrganization getOrgByName(String orgName, boolean required){
    	return cc.getOrgByName(orgName, required);
    }

	public List<CloudQuota> getQuotas() {
		return cc.getQuotas();
	}

	public CloudQuota getQuotaByName(String quotaName, boolean required) {
		return cc.getQuotaByName(quotaName, required);
	}

	public void setQuotaToOrg(String orgName, String quotaName) {
		cc.setQuotaToOrg(orgName, quotaName);
	}

	public void createQuota(CloudQuota quota) {
		cc.createQuota(quota);
	}

	public void deleteQuota(String quotaName) {
		cc.deleteQuota(quotaName);
	}

	public void updateQuota(CloudQuota quota, String name) {
		cc.updateQuota(quota, name);
	}
	
	@Override
	public List<CloudSpaceQuota> getSpaceQuotas() {
		return cc.getSpaceQuotas();
	}

	@Override
	public List<CloudUser> getUsersByOrgName(String orgName) {
		return cc.getUsersByOrgName(orgName);
	}


	@Override
	public List<CloudUser> getUsersByOrgRole(String orgName, String roleName) {
		return cc.getUsersByOrgRole(orgName, roleName);
	}

	@Override
	public List<CloudUser> getUsersBySpaceRole(String spaceUUID, String roleName) {
		return cc.getUsersBySpaceRole(spaceUUID, roleName);
	}

	@Override
	public void createUser(String username, String password, String familyName, String givenName, String phoneNumber) {
		cc.createUser(username, password, familyName, givenName, phoneNumber);
	}

	@Override
	public List<CloudUser> getAllUsers() {
		return cc.getAllUsers();
	}

	@Override
	public CloudUser findUserByUsername(String username) {
		return cc.findUserByUsername(username);
	}

	@Override
	public void associateUserWithOrg(CloudOrganization organization,
			CloudUser user) {
		cc.associateUserWithOrg(organization,user);
		
	}

	@Override
	public void associateUserWithOrgRole(CloudOrganization organization,
			CloudUser user, String roleName) {
		cc.associateUserWithOrgRole(organization, user, roleName);
		
	}

	@Override
	public void associateUserWithSpaceRole(CloudSpace space, CloudUser user,
			String roleName) {
		cc.associateUserWithSpaceRole(space,user,roleName);
		
	}

	@Override
	public void associateOrgWithUser(CloudUser user,
			CloudOrganization organization) {
		cc.associateOrgWithUser(user, organization);
		
	}

	@Override
	public void associateOrgRoleWithUser(CloudUser user,
			CloudOrganization organization, String roleName) {
		cc.associateOrgRoleWithUser(user, organization, roleName);
		
	}

	@Override
	public void associateSpaceRoleWithUser(CloudUser user, CloudSpace space,
			String roleName) {
		cc.associateSpaceRoleWithUser(user, space, roleName);
		
	}

	@Override
	public void removeUserFormOrg(CloudOrganization organization, CloudUser user) {
		cc.removeUserFormOrg(organization, user);
		
	}

	@Override
	public void removeUserFromRoleOrg(CloudOrganization organization,
			CloudUser user, String roleName) {
		cc.removeUserFromRoleOrg(organization, user, roleName);
		
	}

	@Override
	public void removeUserFromRoleSpace(CloudSpace space, CloudUser user,
			String roleName) {
		cc.removeUserFromRoleSpace(space,user,roleName);
		
	}

	@Override
	public void removeOrgFromUser(CloudUser user, CloudOrganization organization) {
		cc.removeOrgFromUser(user, organization);
		
	}

	@Override
	public void removeRoleOrgFromUser(CloudUser user,
			CloudOrganization organization, String roleName) {
		cc.removeRoleOrgFromUser(user, organization, roleName);
		
	}

	@Override
	public void removeRoleSpaceFromUser(CloudUser user, CloudSpace space,
			String roleName) {
		cc.removeRoleSpaceFromUser(user, space, roleName);
		
	}

	@Override
	public void removeSpaceFromUser(CloudUser user, CloudSpace space) {
		cc.removeSpaceFromUser(user, space);
		
	}

	@Override
	public List<CloudSpace> getSpaceFromOrgName(String orgName) {
		return cc.getSpaceFromOrgName(orgName);
	}

	@Override
	public List<CloudApplication> getAppsFromSpaceName(String spaceGuid) {
		return cc.getAppsFromSpaceName(spaceGuid);
	}

	@Override
	public void approveUser(String userName, String displayName,
			String member_type, String authorities) {
		cc.approveUser(userName, displayName, member_type, authorities);
	}

	@Override
	public void updateGroupMember(String userName, String displayName,
			String member_type, Boolean isDelete) {
		cc.updateGroupMember(userName, displayName, member_type, isDelete);
		
	}

	@Override
	public List<CloudDomain> getDomainFromOrgName(String orgName) {
		return cc.getDomainFromOrgName(orgName);
	}

	@Override
	public CloudUser getUsersummaryFromUserName(String userName) {
		return cc.getUsersummaryFromUserName(userName);
	}

	@Override
	public Boolean isMemberByUserAndDisplayName(String user_id,
			String displayName) {
		return cc.isMemberByUserAndDisplayName(user_id, displayName);
	}

	@Override
	public Boolean isReaderByUserAndDisplayName(String user_id,
			String displayName) {
		return cc.isReaderByUserAndDisplayName(user_id, displayName);
	}

	@Override
	public Boolean isWriterByUserAndDisplayName(String user_id,
			String displayName) {
		return cc.isWriterByUserAndDisplayName(user_id, displayName);
	}

	@Override
	public void associataSpaceWithUser(CloudUser user, CloudSpace space) {
		cc.associataSpaceWithUser(user, space);
	}

	@Override
	public String registerUserOnly(String username, String password,
			String familyName, String givenName, String phoneNumber) {
		
		return cc.registerUserOnly(username, password, familyName, givenName, phoneNumber);
	}

	@Override
	public void updateUserWithUsername(String username,
			Map<String, Object> updateParams) {
		cc.updateUserWithUsername(username, updateParams);
		
	}

	@Override
	public List<CloudUser> getUserWithFileters(Map<String, Object> filters) {
		return cc.getUserWithFileters(filters);
	}

	@Override
	public void createSpaceQuota(CloudSpaceQuota spaceQuota) {
		cc.createSpaceQuota(spaceQuota);
		
	}

	@Override
	public void removeSpaceFromSpaceQuota(String spaceQuotaName,
			String spaceName , String orgName) {
		cc.removeSpaceFromSpaceQuota(spaceQuotaName, spaceName,orgName);
		
	}

	@Override
	public void associateSpaceWithSpaceQuota(String spaceQuotaName,
			String spaceName, String orgName) {
		cc.associateSpaceWithSpaceQuota(spaceQuotaName, spaceName, orgName);
		
	}

	@Override
	public void updateSpaceQuota(CloudSpaceQuota spaceQuota) {
		cc.updateSpaceQuota(spaceQuota);
		
	}

	@Override
	public void deleteSpaceQuota(String spaceQuotaName) {
		cc.deleteSpaceQuota(spaceQuotaName);
		
	}

	@Override
	public List<CloudSpaceQuota> getSpaceQuotaWithSpace(String spaceName,
			String orgName) {
		return cc.getSpaceQuotaWithSpace(orgName,spaceName);
	}

	@Override
	public List<CloudSpace> getSpacesWithSpaceQuota(String spaceQuotaName) {
		return cc.getSpacesWithSpaceQuota(spaceQuotaName);
	}

	@Override
	public void createOrganization(String organizationName, String orgQuotaName) {
		cc.createOrganization(organizationName, orgQuotaName);
		
	}

	@Override
	public void deleteOrganization(String organizationName) {
		cc.deleteOrganization(organizationName);
		
	}

	@Override
	public void updateOrganization(CloudOrganization cloudOrganization, String orgQuotaName) {
		cc.updateOrganization(cloudOrganization, orgQuotaName);
		
	}

	@Override
	public void createSpace(String spaceName, String organizationName) {
		cc.createSpace(spaceName, organizationName);
		
	}

	@Override
	public void deleteSpace(String spaceName, String organizationName) {
		cc.deleteSpace(spaceName, organizationName);
		
	}

	@Override
	public void updateSpace(CloudSpace cloudSpace, String organizationName) {
		cc.updateSpace(cloudSpace, organizationName);
		
	}

	@Override
	public List<CloudSecurityGroup> getSecurityGroups() {
		return cc.getSecurityGroups();
	}

	@Override
	public void createSecurityGroup(String name,
			List<CloudSecurityRules> cloudSecurityRules, String spaceName,
			String organizationName) {
		cc.createSecurityGroup(name, cloudSecurityRules, spaceName, organizationName);
		
	}

	@Override
	public void setSpaceWithSecurityGroup(String securityName,
			String spaceName, String orgName) {
		cc.setSpaceWithSecurityGroup(securityName, spaceName, orgName);
		
	}

	@Override
	public void deleteSecurityGroup(String securityName) {
		cc.deleteSecurityGroup(securityName);
		
	}

	@Override
	public void deleteSpaceFromSecurityGroup(String securityName,
			String spaceName, String orgName) {
		cc.deleteSpaceFromSecurityGroup(securityName, spaceName, orgName);
		
	}

	@Override
	public void updateSecurityGroup(CloudSecurityGroup cloudSecurityGroup) {
		cc.updateSecurityGroup(cloudSecurityGroup);
		
	}

	@Override
	public List<CloudSpace> getSpaceForSecurityGroup(String securityName) {
		return cc.getSpaceForSecurityGroup(securityName);
	}

	@Override
	public void setSecurityGroupForStaging(CloudSecurityGroup cloudSecurityGroup) {
		cc.setSecurityGroupForStaging(cloudSecurityGroup);		
	}

	@Override
	public void setSecurityGroupForRunningApps(
			CloudSecurityGroup cloudSecurityGroup) {
		cc.setSecurityGroupForRunningApps(cloudSecurityGroup);		
	}

	@Override
	public void deleteSecurityForStaging(CloudSecurityGroup cloudSecurityGroup) {
		cc.deleteSecurityForStaging(cloudSecurityGroup);		
	}

	@Override
	public void deleteSecurityGroupForRunningApps(
			CloudSecurityGroup cloudSecurityGroup) {
		cc.deleteSecurityGroupForRunningApps(cloudSecurityGroup);		
	}

	@Override
	public List<CloudSecurityGroup> getSecurityGroupsForStaging() {
		return cc.getSecurityGroupsForStaging();
	}

	@Override
	public List<CloudSecurityGroup> getSecurityGroupForRunningApps() {
		return cc.getSecurityGroupForRunningApps();
	}

	@Override
	public void deleteSpaceFromSecurityGroup(String securityName,
			String spaceGuid) {
		cc.deleteSpaceFromSecurityGroup(securityName, spaceGuid);		
	}

	@Override
	public void setSpaceWithSecurityGroup(String securityName, String spaceGuid) {
		cc.setSpaceWithSecurityGroup(securityName, spaceGuid);		
	}

	@Override
	public List<CloudEvent> getAllEvents() {		
		return cc.getAllEvents();
	}

	@Override
	public List<CloudEvent> getEventsByEventType(String eventType) {
		return cc.getEventsByEventType(eventType);
	}

	@Override
	public List<CloudEvent> getEventsByActeeAndTimestamp(String actee,
			String sign, String timestamp) {
		return cc.getEventsByActeeAndTimestamp(actee, sign, timestamp);
	}

	@Override
	public List<CloudService> getServicesFromSpace(String spaceGuid) {
		return cc.getServicesFromSpace(spaceGuid);
	}

	@Override
	public void deleteAppInstanceWithIndex(String appName, int index) {
		cc.deleteAppInstanceWithIndex(appName,index);
	}

	@Override
	public void deleteUserWithUserName(String username) {
		cc.deleteUserWithUserName(username);
	}

	@Override
	public byte[] downloadAppWithAppName(String appName) {
		return cc.downloadAppWithAppName(appName);		
	}

	@Override
	public List<CloudAdminBuildpack> getBuildpacks() {
		return cc.getBuildpacks();
	}

	@Override
	public Map<String, Object> getApplicationEnvironment(UUID appGuid) {
		return cc.getApplicationEnvironment(appGuid);
	}

	@Override
	public Map<String, Object> getApplicationEnvironment(String appName) {		
		return cc.getApplicationEnvironment(appName);
	}

	@Override
	public CloudServiceInstance getServiceInstance(String service) {
		return cc.getServiceInstance(service);
	}

	@Override
	public void associataSpaceWithUser(String userGuid, String spaceGuid) {
		cc.associataSpaceWithUser(userGuid, spaceGuid);		
	}

	@Override
	public void associateOrgWithUser(String userGuid, String orgGuid) {
		cc.associateOrgWithUser(userGuid, orgGuid);
	}

	@Override
	public void updateGroupMemberByUserGuid(String userGuid,
			String displayName, String member_type, Boolean isDelete) {
		cc.updateGroupMemberByUserGuid(userGuid, displayName, member_type, isDelete);	
	}

	@Override
	public List<CloudUserNoUaa> getOrganizationUsersByRoleNoUaa(String orgName,
			String roleName) {
		return cc.getOrganizationUsersByRoleNoUaa(orgName, roleName);
	}

	@Override
	public List<CloudUserNoUaa> getSpaceUsersByRoleNoUaa(String spaceGuid,
			String roleName) {
		return cc.getSpaceUsersByRoleNoUaa(spaceGuid, roleName);
	}

	@Override
	public List<CloudUserNoUaa> getOrganizationUsersNoUaa(String orgName) {
		return cc.getOrganizationUsersNoUaa(orgName);
	}

	@Override
	public List<CloudUserNoUaa> getUsersNoUaa() {
		return cc.getUsersNoUaa();
	}

	@Override
	public CloudUserNoUaa findUserByNameNoUaa(String username) {
		return cc.findUserByNameNoUaa(username);
	}

	@Override
	public List<CloudUser> getOrganizationManagers(String orgName) {
		return cc.getOrganizationManagers(orgName);
	}

	@Override
	public List<CloudUser> getOrgizationAuditors(String orgName) {
		return cc.getOrgizationAuditors(orgName);
	}

	@Override
	public List<CloudUser> getOrgizationBillingManagers(String orgName) {
		return cc.getOrgizationBillingManagers(orgName);
	}

	@Override
	public List<CloudUser> getSpaceManagers(String spaceGuid) {
		return cc.getSpaceManagers(spaceGuid);
	}

	@Override
	public List<CloudUser> getSpaceAuditors(String spaceGuid) {
		return cc.getSpaceAuditors(spaceGuid);
	}

	@Override
	public List<CloudUser> getSpaceDevelopers(String spaceGuid) {
		return cc.getSpaceDevelopers(spaceGuid);
	}

}

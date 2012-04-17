/******************************************************************************* 
 * Copyright (c) 2012 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package com.openshift.internal.client;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.openshift.client.IDomain;
import com.openshift.client.OpenShiftException;
import com.openshift.internal.client.response.unmarshalling.dto.DomainResourceDTO;
import com.openshift.internal.client.response.unmarshalling.dto.Link;
import com.openshift.internal.client.response.unmarshalling.dto.UserResourceDTO;
import com.openshift.internal.client.utils.IOpenShiftJsonConstants;

/**
 * @author Andre Dietisheim
 */
public class API extends AbstractOpenShiftResource {

	private List<IDomain> domains;
	private UserResource user;
	
	public API(IRestService service, Map<String, Link> links) {
		super(service, links);
	}

	public UserResource getUser() throws SocketTimeoutException, OpenShiftException {
		if (user == null) {
			this.user = new UserResource(getService(), new GetUserRequest().execute());
		}
		return this.user;
	}
	
	public List<IDomain> getDomains() throws OpenShiftException, SocketTimeoutException {
		if (this.domains == null) {
			this.domains = loadDomains();
		}
		return this.domains;
	}

	private List<IDomain> loadDomains() throws SocketTimeoutException, OpenShiftException {
		List<IDomain> domains = new ArrayList<IDomain>();
		for (DomainResourceDTO domainDTO : new ListDomainsRequest().execute()) {
			domains.add(new Domain(domainDTO, this));
		}
		return domains;
	}
	
	public IDomain getDomain(String namespace) throws OpenShiftException, SocketTimeoutException {
		for (IDomain domain : getDomains()) {
			if (domain.getId().equals(namespace)) {
				return domain;
			}
		}
		return null;
	}

	public IDomain createDomain(String name) throws OpenShiftException, SocketTimeoutException {
		if (hasDomain(name)) {
			throw new OpenShiftException("Domain {0} already exists", name);
		}

		final DomainResourceDTO domainDTO = new AddDomainRequest().execute(name);
		final IDomain domain = new Domain(domainDTO, this);
		this.domains.add(domain);
		return domain;
	}
	
	/**
	 * Called after a domain has been destroyed
	 * @param domain the domain to remove from the API's domains list.
	 */
	protected void removeDomain(final IDomain domain) {
		this.domains.remove(domain);
	}

	private boolean hasDomain(String name) throws OpenShiftException, SocketTimeoutException {
		return getDomain(name) != null;
	}

	private class AddDomainRequest extends ServiceRequest {

		public AddDomainRequest() throws SocketTimeoutException, OpenShiftException {
			super("ADD_DOMAIN");
		}

		public DomainResourceDTO execute(String namespace) throws SocketTimeoutException, OpenShiftException {
			return execute(new ServiceParameter(IOpenShiftJsonConstants.PROPERTY_ID, namespace));
		}
	}
	
	private class ListDomainsRequest extends ServiceRequest {

		public ListDomainsRequest() throws SocketTimeoutException, OpenShiftException {
			super("LIST_DOMAINS");
		}

		public List<DomainResourceDTO> execute() throws SocketTimeoutException, OpenShiftException {
			return super.execute();
		}
	}

	private class GetUserRequest extends ServiceRequest {

		public GetUserRequest() throws SocketTimeoutException, OpenShiftException {
			super("GET_USER");
		}

		public UserResourceDTO execute() throws SocketTimeoutException, OpenShiftException {
			return super.execute();
		}
	}

}

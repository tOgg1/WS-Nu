//-----------------------------------------------------------------------------
// Copyright (C) 2014 Tormod Haugland and Inge Edward Haulsaunet
//
// This file is part of WS-Nu.
//
// WS-Nu is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// WS-Nu is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with WS-Nu. If not, see <http://www.gnu.org/licenses/>.
//-----------------------------------------------------------------------------

package org.ntnunotif.wsnu.base.util;

import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;
import org.ntnunotif.wsnu.base.net.NuParseValidationEventInfo;

import javax.xml.namespace.NamespaceContext;
import java.util.List;
import java.util.Map;

/**
 * Class containing meta-information about a request. Primarily used in an InternalMessage
 *
 * @author Tormod Haugland
 *         Created by tormod on 24.03.14.
 */
public class RequestInformation {

    private NamespaceContext _namespaceContext;
    @EndpointReference
    private String _endpointReference;
    private String _requestURL;
    private Map<String, String[]> _parameters;
    private int _httpStatus;
    private NuNamespaceContextResolver _namespaceContextResolver;
    private List<NuParseValidationEventInfo> _parseValidationEventInfos;


    public RequestInformation() {
    }

    public RequestInformation(NamespaceContext _context, String _endpointReference, String _requestURL, Map<String, String[]> parameters) {
        this._namespaceContext = _context;
        this._endpointReference = _endpointReference;
        this._requestURL = _requestURL;
        this._parameters = parameters;
    }

    public Map<String, String[]> getParameters() {
        return _parameters;
    }

    public void setParameters(Map<String, String[]> _parameters) {
        this._parameters = _parameters;
    }

    /**
     * This method is deprecated, and ONLY available for backward compatibility. Please use <code>getNamespaceContext(Object)</code> instead.
     *
     * @return a non guaranteed namespace context
     * @deprecated use getNamespaceContext(Object) instead
     */
    @Deprecated
    public NamespaceContext getNamespaceContext() {
        return _namespaceContext;
    }

    /**
     * @param _context the non-guaranteed context
     * @deprecated use setNamespaceContextResolver(NuNamespaceContextResolver) instead
     */
    @Deprecated
    public void setNamespaceContext(NamespaceContext _context) {
        this._namespaceContext = _context;
    }

    /**
     * Sets the object resolving namespaces.
     *
     * @param resolver the object resolving namespaces
     */
    public void setNamespaceContextResolver(NuNamespaceContextResolver resolver) {
        _namespaceContextResolver = resolver;
    }

    /**
     * Gets the object resolving namespaces.
     *
     * @return the object resolving namespaces
     */
    public NuNamespaceContextResolver getNamespaceContextResolver() {
        return _namespaceContextResolver;
    }

    /**
     * Gets the namespace for this object. If the context resolver is set, the namespace context will be correct. No
     * guarantees are provided if it is not.
     *
     * @param object the object to resolve the namespace for
     * @return the namespace context for this object, or <code>null</code> if the object was not registered with the resolver
     */
    public NamespaceContext getNamespaceContext(Object object) {

        if (_namespaceContextResolver == null) {
            return _namespaceContext;
        }

        return _namespaceContextResolver.resolveNamespaceContext(object);
    }

    public String getEndpointReference() {
        return _endpointReference;
    }

    public void setEndpointReference(String _endpointReference) {
        this._endpointReference = _endpointReference;
    }

    public String getRequestURL() {
        return _requestURL;
    }

    public void setRequestURL(String _requestURL) {
        this._requestURL = _requestURL;
    }

    public int getHttpStatus() {
        return _httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this._httpStatus = httpStatus;
    }

    /**
     * Gets the parse {@link org.ntnunotif.wsnu.base.net.NuParseValidationEventInfo}s connected to the request. If
     * schema validation is turned on, this will return <code>null</code>.
     *
     * @return the {@link org.ntnunotif.wsnu.base.net.NuParseValidationEventInfo}s connected to this request or
     * <code>null</code> if schema validation is turned on.
     */
    public List<NuParseValidationEventInfo> getParseValidationEventInfos() {
        return _parseValidationEventInfos;
    }

    /**
     * Sets the {@link org.ntnunotif.wsnu.base.net.NuParseValidationEventInfo}s connected to the request
     *
     * @param parseValidationEventInfos the infos to set
     */
    public void setParseValidationEventInfos(List<NuParseValidationEventInfo> parseValidationEventInfos) {
        this._parseValidationEventInfos = parseValidationEventInfos;
    }
}

package org.ntnunotif.wsnu.base.util;

import javax.xml.namespace.NamespaceContext;

/**
 * Class containing meta-information about a request. Primarily used in an InternalMessage
 * @author Tormod Haugland
 * Created by tormod on 24.03.14.
 */
public class RequestInformation {

    private NamespaceContext _namespaceContext;
    @EndpointReference
    private String _endpointReference;
    private String _requestURL;

    public RequestInformation() {

    }

    public RequestInformation(NamespaceContext _context, String _endpointReference, String _requestURL) {
        this._namespaceContext = _context;
        this._endpointReference = _endpointReference;
        this._requestURL = _requestURL;
    }

    public NamespaceContext getNamespaceContext() {
        return _namespaceContext;
    }

    public void setNamespaceContext(NamespaceContext _context) {
        this._namespaceContext = _context;
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
}

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
     * @deprecated use getNamespaceContext(Object) instead
     * @return a non guaranteed namespace context
     */
    @Deprecated
    public NamespaceContext getNamespaceContext() {
        return _namespaceContext;
    }

    /**
     * @deprecated use setNamespaceContextResolver(NuNamespaceContextResolver) instead
     * @param _context the non-guaranteed context
     */
    @Deprecated
    public void setNamespaceContext(NamespaceContext _context) {
        this._namespaceContext = _context;
    }

    public void setNamespaceContextResolver(NuNamespaceContextResolver resolver) {
        // TODO
    }

    public NamespaceContext getNamespaceContext(Object object) {
        // TODO
        return _namespaceContext;
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

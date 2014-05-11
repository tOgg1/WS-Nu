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

package org.ntnunotif.wsnu.examples.base;

import org.ntnunotif.wsnu.base.net.NuNamespaceContextResolver;
import org.ntnunotif.wsnu.base.net.NuParseValidationEventInfo;
import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.InternalMessage;
import org.ntnunotif.wsnu.base.util.Log;
import org.ntnunotif.wsnu.base.util.RequestInformation;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;
import javax.xml.namespace.NamespaceContext;
import java.util.List;

/**
 * An example showing how the parser may be configured to do what is needed.
 */
public class XMLParserConfigurationAndUsage {

    public static void main(String[] args) {
        // Disable logging
        Log.setEnableDebug(false);
        Log.setEnableWarnings(false);
        Log.setEnableErrors(false);

        // --- !!! ADDING ADDITIONAL CLASSES !!! ---
        //
        // For the system to be able to parse custom JAXB elements, you first need to create the classes. These may
        // either be written by hand, or by using your favourite tool to create them from XSD files. This process is
        // not in the domain of this project, so no information is provided on how to do this here.
        //
        // After these classes are created, and you want to use this system to build a Web Service, you have to tell the
        // system where it can find these classes.
        //
        // You need to know what package they exist in, and this package has to have an ObjectFactory. To make the
        // allow for these classes to be used, run
        XMLParser.registerReturnObjectPackageWithObjectFactory("complete.package.path.to.classes");
        // with the correct and full package path to the ObjectFactory and the classes.

        // --- !!! SCHEMA VALIDATION !!! ---
        // --- The use of this is discouraged. See later in the example under usage ---
        //
        // By default, the parser does not perform strict schema validation. Instead it stores information about what
        // may be problems in a wrapper class, called NuParseValidationEventInfo. This is not validated against actual
        // schemas, but only tells if the XML that came in were consistent with the JAXB classes.
        //
        // The default schema validation is off, to turn it on will cause the schemas to be compiled and registered with
        // the parser. THIS WILL CAUSE THE PARSER TO FAIL DURING PARSING WHERE YOUR WEB SERVICE NEVER WILL HEAR ANYTHING
        // AND THEREFORE NOT BE ABLE TO RETURN AN APPROPRIATE FAULT! It will also take some time, since the schemas must
        // be compiled.
        // This example will not turn schema validation on, it is not recommended to do so, but it will state the
        // correct method to run if this is wanted. Run this method with false to start schema validation.
        XMLParser.setSkippingSchemaValidation(true);
        // To add additional schemas to compile, uncomment and run:

        //XMLParser.registerSchemaLocation("System id, see javadoc");

        // The above line is commented out, since it will trigger the parser to compile the schemas, and thus take time.

        // The getter/setter pair
        XMLParser.setStopParsingAtSeverity(ValidationEvent.WARNING);
        XMLParser.getStopParsingAtSeverity();
        // Sets the level of severity the parser should raise an exception on, when schema validation is turned on.


        // --- !!! USAGE !!! ---
        //
        // To parse a XML formatted message, you need to give the parser either an InputStream or a XMLStreamReader.
        // The stream may be packed within an InternalMessage, where namespace context and other request information
        // will be updated.
        //
        // All three parse commands will return or modify an InternalMessage. To see how it may be used:
        try {

            // parse an InputStream:
            InternalMessage message = XMLParser.parse(System.in);

            // parse an InputStream wrapped in an InternalMessage
            message = new InternalMessage(
                    InternalMessage.STATUS_HAS_MESSAGE | InternalMessage.STATUS_MESSAGE_IS_INPUTSTREAM, System.in
            );
            XMLParser.parse(message);

            // To see status og the parsed object:
            int status = message.statusCode;
            // this may be compared with different flags, eg.
            if ((status & (InternalMessage.STATUS_HAS_MESSAGE | InternalMessage.STATUS_FAULT)) != 0) {
                // we now know the message either has a message, or it is a fault message
                System.out.println("either fault or has message");
            }

            // To get the parsed object
            Object o = message.getMessage();
            // This object may be an instance of a JAXBElement, or it may be an instance of a class marked as XMLRoot

            // Additional information gathered during parsing is stored within the RequestInformation
            RequestInformation information = message.getRequestInformation();
            // This information object contains information about all namespaces for all object that is parsed.
            NuNamespaceContextResolver resolver = information.getNamespaceContextResolver();
            // or simply (for root element, same call for all objects in parsed tree, with the object you need the
            // context for used as argument)
            NamespaceContext context = information.getNamespaceContext(o);
            // If schema validation is turned off, information about events that may have compromised the parsing can be
            // found by
            List<NuParseValidationEventInfo> infoList = information.getParseValidationEventInfos();
            // To see more about the NuParseValidationEventInfo object, look it up in the JavaDoc


            // To write an object as XML, run
            XMLParser.writeObjectToStream(o, System.out);
            // This will not format the XML, only write it directly to stream. If schema validation is turned on, the
            // object that is to be written is also checked for schema compliance.
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        // It can be noted that by use of our connectors, e.g. UnpackingConnector, the request information for a parsed
        // object may be retrieved from the connector.
    }
}

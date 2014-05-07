package org.ntnunotif.wsnu.examples.base;

import org.ntnunotif.wsnu.base.net.XMLParser;
import org.ntnunotif.wsnu.base.util.Log;

import javax.xml.bind.ValidationEvent;

/**
 * An example showing how the parser may be configured to do what is needed.
 * Created by Inge on 07.05.2014.
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
        // TODO
    }
}

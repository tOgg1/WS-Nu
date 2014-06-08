package org.ntnunotif.wsnu.services.general;

import org.ntnunotif.wsnu.base.util.Log;
import org.oasis_open.docs.wsn.b_2.*;
import org.oasis_open.docs.wsn.br_2.PublisherRegistrationFailedFaultType;
import org.oasis_open.docs.wsn.br_2.ResourceNotDestroyedFaultType;
import org.oasis_open.docs.wsn.brw_2.PublisherRegistrationFailedFault;
import org.oasis_open.docs.wsn.brw_2.ResourceNotDestroyedFault;
import org.oasis_open.docs.wsn.bw_2.*;
import org.oasis_open.docs.wsrf.bf_2.BaseFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Utility class that holds helper classes to throw WS-N exceptions.
 */
public class ExceptionUtilities {

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws UnacceptableInitialTerminationTimeFault
     */
    public static void throwUnacceptableInitialTerminationTimeFault(String language, String description) throws UnacceptableInitialTerminationTimeFault{
        UnacceptableInitialTerminationTimeFaultType type = new UnacceptableInitialTerminationTimeFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setMinimumTime(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        type.getDescription().add(desc);

        throw new UnacceptableInitialTerminationTimeFault(description, type);
    }

    /**
     *
     * @param language
     * @param description
     * @throws UnacceptableTerminationTimeFault
     */
    public static void throwUnacceptableTerminationTimeFault(String language, String description) throws UnacceptableTerminationTimeFault {
        UnacceptableTerminationTimeFaultType type = new UnacceptableTerminationTimeFaultType();

        // TODO Should maxtime be set?
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setMinimumTime(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        type.getDescription().add(desc);

        throw new UnacceptableTerminationTimeFault(description, type);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws org.oasis_open.docs.wsn.brw_2.PublisherRegistrationFailedFault
     */
    public static void throwPublisherRegistrationFailedFault(String language, String description) throws PublisherRegistrationFailedFault {
        PublisherRegistrationFailedFaultType type = new PublisherRegistrationFailedFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        type.getDescription().add(desc);

        throw new PublisherRegistrationFailedFault(description, type);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws org.oasis_open.docs.wsrf.rw_2.ResourceUnknownFault
     */
    public static void throwResourceUnknownFault(String language, String description) throws ResourceUnknownFault {
        ResourceUnknownFaultType type = new ResourceUnknownFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);

        type.getDescription().add(desc);

        throw new ResourceUnknownFault(description, type);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws UnableToDestroySubscriptionFault
     */
    public static void throwUnableToDestroySubscriptionFault(String language, String description) throws UnableToDestroySubscriptionFault {
        UnableToDestroySubscriptionFaultType type = new UnableToDestroySubscriptionFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);

        type.getDescription().add(desc);

        throw new UnableToDestroySubscriptionFault(description, type);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws SubscribeCreationFailedFault
     */
    public static void throwSubscribeCreationFailedFault(String language, String description) throws SubscribeCreationFailedFault {
        SubscribeCreationFailedFaultType type = new SubscribeCreationFailedFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);

        type.getDescription().add(desc);

        throw new SubscribeCreationFailedFault(description, type);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws PublisherRegistrationFailedFault
     */
    public static void throwPublisherRegistrationFault(String language, String description) throws PublisherRegistrationFailedFault {
        PublisherRegistrationFailedFaultType type = new PublisherRegistrationFailedFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            type.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }

        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);

        type.getDescription().add(desc);

        throw new PublisherRegistrationFailedFault(description, type);
    }


    /**
     * Builds and throws an {@link org.oasis_open.docs.wsn.b_2.InvalidFilterFaultType}
     *
     * @param language the language of the description, as defined in {@link org.oasis_open.docs.wsrf.bf_2.BaseFaultType.Description}
     * @param description the description of the fault, as defined in {@link org.oasis_open.docs.wsrf.bf_2.BaseFaultType.Description}
     * @param filterName the name of the filter that was not understood
     * @throws InvalidFilterFault
     */
    public static void throwInvalidFilterFault(String language, String description, QName filterName) throws
            InvalidFilterFault {

        InvalidFilterFaultType faultType = new InvalidFilterFaultType();
        faultType.getUnknownFilter().add(filterName);
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            faultType.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);
        throw new InvalidFilterFault(description, faultType);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws InvalidMessageContentExpressionFault
     */
    public static void throwInvalidMessageContentExpressionFault(String language, String description) throws
            InvalidMessageContentExpressionFault {

        InvalidMessageContentExpressionFaultType faultType = new InvalidMessageContentExpressionFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            faultType.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);
        throw new InvalidMessageContentExpressionFault(description, faultType);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws org.oasis_open.docs.wsn.bw_2.NoCurrentMessageOnTopicFault
     */
    public static void throwNoCurrentMessageOnTopicFault(String language, String description) throws NoCurrentMessageOnTopicFault {
        NoCurrentMessageOnTopicFaultType faultType = new NoCurrentMessageOnTopicFaultType();
        try {
            GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            XMLGregorianCalendar calendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(now);
            faultType.setTimestamp(calendar);
        } catch (DatatypeConfigurationException e) {
            Log.e("ServiceUtilities", "Could not build XMLGregorianCalendar; fault created without timestamp");
            e.printStackTrace();
        }
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);
        throw new NoCurrentMessageOnTopicFault(description, faultType);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws org.oasis_open.docs.wsn.bw_2.TopicNotSupportedFault
     */
    public static void throwTopicNotSupportedFault(String language, String description) throws TopicNotSupportedFault {
        TopicNotSupportedFaultType faultType = new TopicNotSupportedFaultType();
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);

        throw new TopicNotSupportedFault(description, faultType);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws org.oasis_open.docs.wsn.brw_2.ResourceNotDestroyedFault
     */
    public static void throwResouceNotDestroyedFault(String language, String description) throws ResourceNotDestroyedFault {
        ResourceNotDestroyedFaultType faultType = new ResourceNotDestroyedFaultType();
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);

        throw new ResourceNotDestroyedFault(description, faultType);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws org.oasis_open.docs.wsn.bw_2.PauseFailedFault
     */
    public static void throwPauseFailedFault(String language, String description) throws PauseFailedFault {
        PauseFailedFaultType faultType = new PauseFailedFaultType();
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);

        throw new PauseFailedFault(description, faultType);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws org.oasis_open.docs.wsn.bw_2.ResumeFailedFault
     */
    public static void throwResumeFailedFault(String language, String description) throws ResumeFailedFault {
        ResumeFailedFaultType faultType = new ResumeFailedFaultType();
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);

        throw new ResumeFailedFault(description, faultType);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws org.oasis_open.docs.wsn.bw_2.InvalidTopicExpressionFault
     */
    public static void throwInvalidTopicExpressionFault(String language, String description) throws InvalidTopicExpressionFault {
        InvalidTopicExpressionFaultType faultType = new InvalidTopicExpressionFaultType();
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);

        throw new InvalidTopicExpressionFault(description, faultType);
    }

    //TODO
    /**
     *
     * @param language
     * @param description
     * @throws ResourceNotDestroyedFault
     */
    public static void throwResourceNotDestroyed(String language, String description) throws ResourceNotDestroyedFault {
        ResourceNotDestroyedFaultType faultType = new ResourceNotDestroyedFaultType();
        BaseFaultType.Description desc = new BaseFaultType.Description();
        desc.setLang(language);
        desc.setValue(description);
        faultType.getDescription().add(desc);

        throw new ResourceNotDestroyedFault(description, faultType);
    }
}

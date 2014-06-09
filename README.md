# WS-Nu

WS-Nu is an implementation of Oasis' WS-Notification specification, built around a rigid Web Service base.

## Installation

As any project, clone the repository with

```
git clone https://github.com/tOgg1/WS-Nu.git
```

WS-Nu uses maven, so build the source code with 

```
mvn clean install
```

## Adding as dependency

Want to add WS-Nu as a dependency to your project? The repository is hosted at [http://ws-nu.net/repo/](http://ws-nu.net/repo), and can be added in your maven project 
with the following additions to your pom-file:

```
  <repositories>
    <repository>
      <id>WS-Nu-repo</id>
      <url>http://ws-nu.net/repo/</url>
    </repository>
  </repositories>
```

```
    <dependency>
      <groupId>org.ntnunotif</groupId>
      <version>0.4-SNAPSHOT</version>
      <artifactId>wsnu-services</artifactId>
      <scope>compile</scope>
    </dependency>
```

## Documentation

Further documentation can be found at [the ws-nu homepage](http://ws-nu.net).

## Planned future releases

### 2.0

### 1.0

### 0.4

* Enhancements
  * Services renamed(proposed naming below):
    * GenericNotificationProducer -> NotificiationProducerImpl
    * GenericNotificationBroker -> NotificationBrokerImpl
    * NotificationConsumer -> NotificatonConsumerImpl
  * ServiceUtilities separated into four new classes(propsed naming below):
    * ExceptionUtilities
    * HelperClasses
    * ServiceUtilities
    * WsnUtilities
  * Full IPv6 support
  * Support for complex header generation 
  * All WS-N send-methods have been moved from the WebService class to WsnUtilities
    * These have also been altered to return relevant info and throw relevant exceptions that can be thrown at the external Web Service, in the form of JAXB-objects.
  * Methods added to SoapForwardingHub to allow removal of ServiceConnections with the Web Service objects
* Major bug fixes
  * WS-N messages should now be attached with a wsa:action-headers
  
## Release log

### 0.3 - first official release 

* Basic primary functionality added


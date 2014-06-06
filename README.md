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

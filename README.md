# Spiffe Spring Boot Tomcat example
This is an example on how to integrate the dynamic X509 certificates issued by Spiffe into a Java Web Server.

## Running this example
Required:
* Either Linux or BSD system since the communication between the app and spire requires a unix domain socket
* Gradle to build the application
* A running Spire server. [HowTo](https://github.com/spiffe/spire#installing-spire-server-and-agent)
* A Spire entry that matches this application.
```
cmd/spire-server/spire-server entry create \
-spiffeID spiffe://example.org/host/test \
-parentID spiffe://example.org/host \
-selector unix:uid:501
```

To start the Java server you can run `gradle bootRun`


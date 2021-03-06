# Spiffe/Spire Spring Boot Tomcat example
This is an example on how to integrate the dynamic X509 certificates issued by Spiffe/Spire into a Java Web Server.

When the server starts it will retrieve all issued SVIDs and X509 certificates from spire-agent. The retrieved certificates, keys and ca certificates will be stored in the Java Keystore. Afterwards the server will renew his SVIDs and certificates every 5 minutes. 

The issue in this example wasn't interacting with Spire but rather getting Tomcat and the Java KeyStore to deal with dynamic certificates that are not available during the startup but must be retrieved and configured after the server start. 
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

# License

Copyright 2018 Lukas Eichler

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

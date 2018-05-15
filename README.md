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

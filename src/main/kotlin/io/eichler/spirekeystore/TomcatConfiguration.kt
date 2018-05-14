package io.eichler.spirekeystore

import org.apache.catalina.connector.Connector
import org.apache.coyote.http11.Http11NioProtocol
import org.apache.tomcat.util.net.SSLHostConfig
import org.apache.tomcat.util.net.SSLHostConfigCertificate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.security.KeyStore


@Configuration
open class TomcatConfiguration {

    @Autowired
    var keyStore: KeyStore? = null

    @Bean
    open fun containerFactory(): TomcatServletWebServerFactory {
        return object : TomcatServletWebServerFactory() {
            override fun customizeConnector(connector: Connector) {
                super.customizeConnector(connector);
                connector.scheme = "https"
                connector.secure = true

                val sslHostConfig = SSLHostConfig()
                val cert = SSLHostConfigCertificate(sslHostConfig, SSLHostConfigCertificate.Type.RSA)
                sslHostConfig.addCertificate(cert)
                cert.certificateKeystore = keyStore
                cert.certificateKeystorePassword = ""
                cert.certificateKeyAlias = "default"

                connector.addSslHostConfig(sslHostConfig)
                val protocol = connector.protocolHandler as Http11NioProtocol
                protocol.setSSLProtocol("TLSv1,TLSv1.1,TLSv1.2")
                protocol.isSSLEnabled = true
            }
        };

    }

}



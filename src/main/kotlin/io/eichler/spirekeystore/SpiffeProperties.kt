package io.eichler.spirekeystore

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("spiffe")
class SpiffeProperties {
    var socketPath: String = "/tmp/agent.sock"
}
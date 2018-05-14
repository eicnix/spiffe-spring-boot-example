package io.eichler.spirekeystore

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HelloWorldController {

    @RequestMapping("/")
    fun hello(): String {
        return "Hello World from a Spiffe enabled Spring Boot server. "
    }
}
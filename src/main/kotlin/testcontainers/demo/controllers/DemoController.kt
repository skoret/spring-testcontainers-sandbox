package testcontainers.demo.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DemoController {

    @GetMapping
    fun getAllUsers(): List<String> {
        return listOf("user a", "user b")
    }

}

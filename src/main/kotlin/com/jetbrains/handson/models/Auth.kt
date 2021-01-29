package com.jetbrains.handson.models

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class SingInSignature(val email: String, val password: String)

@Serializable
class Token(val email: String, val deadline: Long) {
    fun encode(): String = "$email $deadline"

    fun isValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        return currentTime < deadline
    }
}

fun decodeToken(hash: String): Token {
    val input = hash.split(" ")
    return Token(input[0], input[1].toLong())
}

fun Route.singUp() {
    route("/register") {
        post {
            val customer = call.receive<Customer>()
            customerStorage.add(customer)
            call.respondText("Customer registered correctly", status = HttpStatusCode.Accepted)
        }
    }
}

fun Route.singIn() {
    route("/login") {
        post {
            val signature = call.receive<SingInSignature>()
            val customer: Customer = customerStorage.find {
                it.email == signature.email && it.password == signature.password
            } ?: return@post call.respondText(
                "User doesn't exist",
                status = HttpStatusCode.NotFound
            )
            call.respond(
                message = Token(customer.email, System.currentTimeMillis() + 3600000).encode(),
                status = HttpStatusCode.Accepted
            )
        }
    }
}

fun Application.registerAuthRoutes() {
    routing {
        singIn()
        singUp()
    }
}
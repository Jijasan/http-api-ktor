package com.jetbrains.handson.models

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.customerRouting() {
    route("/customer") {
        get("{id}/{token}") {
            val token = decodeToken(
                call.parameters["token"] ?: return@get call.respondText(
                    "Missing or malformed token",
                    status = HttpStatusCode.BadRequest
                )
            )
            if (!token.isValid())
                return@get call.respondText(
                    "Invalid token",
                    status = HttpStatusCode.BadRequest
                )
            val id = call.parameters["id"] ?: return@get call.respondText(
                "Missing or malformed id",
                status = HttpStatusCode.BadRequest
            )
            val customer =
                customerStorage.find { it.id == id } ?: return@get call.respondText(
                    "No customer with id $id",
                    status = HttpStatusCode.NotFound
                )
            if (token.email != customer.email)
                return@get call.respondText(
                    "Invalid token",
                    status = HttpStatusCode.BadRequest
                )
            call.respond(customer)
        }
        delete("{id}/{token}") {
            val token = decodeToken(
                call.parameters["token"] ?: return@delete call.respondText(
                    "Missing or malformed token",
                    status = HttpStatusCode.BadRequest
                )
            )
            if (!token.isValid())
                return@delete call.respondText(
                    "Invalid token",
                    status = HttpStatusCode.BadRequest
                )
            val id = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val customer: Customer? = customerStorage.find { it.id == id }
            if (customer != null && customer.email != token.email)
                return@delete call.respondText(
                    "Invalid token",
                    status = HttpStatusCode.BadRequest
                )
            if (customerStorage.removeIf { it.id == id }) {
                call.respondText("Customer removed correctly", status = HttpStatusCode.Accepted)
            } else {
                call.respondText("Not found", status = HttpStatusCode.NotFound)
            }
        }
        get("{id}/{token}/all") {
            val token = decodeToken(
                call.parameters["token"] ?: return@get call.respondText(
                    "Missing or malformed token",
                    status = HttpStatusCode.BadRequest
                )
            )
            if (!token.isValid())
                return@get call.respondText(
                    "Invalid token",
                    status = HttpStatusCode.BadRequest
                )
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val customer: Customer? = customerStorage.find { it.id == id }
            if (customer != null && customer.email != token.email)
                return@get call.respondText(
                    "Invalid token",
                    status = HttpStatusCode.BadRequest
                )
            if (customer == null)
                return@get call.respondText(
                    "Not found",
                    status = HttpStatusCode.NotFound
                )
            call.respond(orderStorage.filter { it.email == customer.email })
        }
        get("{id}/{token}/items") {
            val token = decodeToken(
                call.parameters["token"] ?: return@get call.respondText(
                    "Missing or malformed token",
                    status = HttpStatusCode.BadRequest
                )
            )
            if (!token.isValid())
                return@get call.respondText(
                    "Invalid token",
                    status = HttpStatusCode.BadRequest
                )
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val customer: Customer? = customerStorage.find { it.id == id }
            if (customer != null && customer.email != token.email)
                return@get call.respondText(
                    "Invalid token",
                    status = HttpStatusCode.BadRequest
                )
            if (customer == null)
                return@get call.respondText(
                    "Not found",
                    status = HttpStatusCode.NotFound
                )
            call.respond(orderStorage.filter { it.email == customer.email }.flatMap { it.contents })
        }
        get("{id}/{token}/{tag}") {
            val token = decodeToken(
                call.parameters["token"] ?: return@get call.respondText(
                    "Missing or malformed token",
                    status = HttpStatusCode.BadRequest
                )
            )
            if (!token.isValid())
                return@get call.respondText(
                    "Invalid token",
                    status = HttpStatusCode.BadRequest
                )
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest)
            val customer: Customer? = customerStorage.find { it.id == id }
            if (customer != null && customer.email != token.email)
                return@get call.respondText(
                    "Invalid token",
                    status = HttpStatusCode.BadRequest
                )
            if (customer == null)
                return@get call.respondText(
                    "Not found",
                    status = HttpStatusCode.NotFound
                )
            val tag = call.parameters["tag"] ?: return@get call.respondText(
                "Missing tag",
                status = HttpStatusCode.BadRequest
            )
            call.respond(orderStorage.filter { it.email == customer.email }.flatMap { it.contents }
                .filter { it.tags.contains(tag) })
        }
    }
}

fun Application.registerCustomerRoutes() {
    routing {
        customerRouting()
    }
}
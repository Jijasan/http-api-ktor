package com.jetbrains.handson.models

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class Order(val number: String, val contents: List<OrderItem>, val email: String)

@Serializable
data class OrderItem(val item: String, val amount: Int, val price: Double, val tags: List<String>)

val orderStorage = mutableListOf<Order>()

fun Route.getOrderRoute() {
    get("/order/{id}/{token}") {
        val token = decodeToken(call.parameters["token"] ?: return@get call.respondText(
            "Missing or malformed token",
            status = HttpStatusCode.BadRequest
        ))
        if (!token.isValid())
            return@get call.respondText(
                "Invalid token",
                status = HttpStatusCode.BadRequest
            )
        val id = call.parameters["id"] ?:
            return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
        val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
            "Not Found",
            status = HttpStatusCode.NotFound
        )
        if (order.email != token.email)
            return@get call.respondText(
                "Invalid token",
                status = HttpStatusCode.BadRequest
            )
        call.respond(order)
    }
    post("/order/{token}") {
        val token = decodeToken(call.parameters["token"] ?: return@post call.respondText(
            "Missing or malformed token",
            status = HttpStatusCode.BadRequest
        ))
        if (!token.isValid())
            return@post call.respondText(
                "Invalid token",
                status = HttpStatusCode.BadRequest
            )
        val order = call.receive<Order>()
        if (order.email != token.email)
            return@post call.respondText(
                "Invalid token",
                status = HttpStatusCode.BadRequest
            )
        orderStorage.add(order)
        call.respondText("Order added correctly", status = HttpStatusCode.Accepted)
    }
    delete("/order/{id}/{token}") {
        val token = decodeToken(call.parameters["token"] ?: return@delete call.respondText(
            "Missing or malformed token",
            status = HttpStatusCode.BadRequest
        ))
        if (!token.isValid())
            return@delete call.respondText(
                "Invalid token",
                status = HttpStatusCode.BadRequest
            )
        val id = call.parameters["id"] ?:
            return@delete call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
        val order: Order? = orderStorage.find { it.number == id }
        if (order != null && order.email != token.email)
            return@delete call.respondText(
                "Invalid token",
                status = HttpStatusCode.BadRequest
            )
        if (orderStorage.removeIf { it.number == id }) {
            call.respondText(
                "Order removed correctly",
                status = HttpStatusCode.Accepted
            )
        } else {
            call.respondText(
                "Not found",
                status = HttpStatusCode.NotFound
            )
        }
    }
}

fun Route.totalizeOrderRoute() {
    get("/order/{id}/{token}/total") {
        val token = decodeToken(call.parameters["token"] ?: return@get call.respondText(
            "Missing or malformed token",
            status = HttpStatusCode.BadRequest
        ))
        if (!token.isValid())
            return@get call.respondText(
                "Invalid token",
                status = HttpStatusCode.BadRequest
            )
        val id = call.parameters["id"] ?:
            return@get call.respondText("Bad Request", status = HttpStatusCode.BadRequest)
        val order = orderStorage.find { it.number == id } ?: return@get call.respondText(
            "Not Found",
            status = HttpStatusCode.NotFound
        )
        if (order.email != token.email)
            return@get call.respondText(
                "Invalid token",
                status = HttpStatusCode.BadRequest
            )
        val total = order.contents.map { it.price * it.amount }.sum()
        call.respond(total)
    }
}

fun Application.registerOrderRoutes() {
    routing {
        getOrderRoute()
        totalizeOrderRoute()
    }
}

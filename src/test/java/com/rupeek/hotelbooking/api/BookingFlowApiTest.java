package com.rupeek.hotelbooking.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** End-to-end through HTTP: onboard -> search -> book -> pay -> cancel. */
@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowApiTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private JsonNode call(org.springframework.test.web.servlet.RequestBuilder rb, int expectedStatus) throws Exception {
        String body = mvc.perform(rb).andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString();
        return body.isBlank() ? null : json.readTree(body);
    }

    @Test
    void fullLifecycle() throws Exception {
        String city = "FlowCity-" + System.nanoTime();
        LocalDate in = LocalDate.now().plusDays(10);
        LocalDate out = in.plusDays(2);

        String ownerBody = """
            {"name":"Chain","properties":[{"name":"Flow Hotel","type":"HOTEL","city":"%s","locality":"Centre",
              "starRating":4,"amenities":["WIFI","POOL"],
              "roomTypes":[{"name":"Deluxe","maxGuests":2,"pricePerNight":2500,"totalRooms":1}]}]}""".formatted(city);
        JsonNode owner = call(post("/api/v1/owners").contentType(MediaType.APPLICATION_JSON).content(ownerBody), 201);
        String propertyId = owner.at("/properties/0/id").asText();
        String roomTypeId = owner.at("/properties/0/roomTypes/0/id").asText();

        String searchUrl = "/api/v1/properties/search?city=%s&checkIn=%s&checkOut=%s&guests=2&minStars=4&amenities=POOL"
                .formatted(city, in, out);
        mvc.perform(get(searchUrl)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].property.name").value("Flow Hotel"))
                .andExpect(jsonPath("$[0].offers[0].totalPrice").value(5000.0));

        String bookBody = """
            {"propertyId":"%s","roomTypeId":"%s","guestName":"Asha","guestEmail":"asha@example.com",
             "checkIn":"%s","checkOut":"%s","guests":2}""".formatted(propertyId, roomTypeId, in, out);
        JsonNode booking = call(post("/api/v1/bookings").contentType(MediaType.APPLICATION_JSON).content(bookBody), 201);
        String bookingId = booking.get("id").asText();
        org.junit.jupiter.api.Assertions.assertEquals("PENDING_PAYMENT", booking.get("status").asText());

        // inventory is held: second attempt conflicts and search no longer lists the property
        call(post("/api/v1/bookings").contentType(MediaType.APPLICATION_JSON).content(bookBody), 409);
        mvc.perform(get(searchUrl)).andExpect(jsonPath("$").isEmpty());

        String payBody = "{\"method\":\"UPI\",\"details\":{\"vpa\":\"asha@upi\"}}";
        mvc.perform(post("/api/v1/bookings/" + bookingId + "/payments").header("Idempotency-Key", "k1")
                        .contentType(MediaType.APPLICATION_JSON).content(payBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.booking.status").value("CONFIRMED"));
        mvc.perform(post("/api/v1/bookings/" + bookingId + "/payments").header("Idempotency-Key", "k1")
                        .contentType(MediaType.APPLICATION_JSON).content(payBody))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SUCCESS"));

        mvc.perform(post("/api/v1/bookings/" + bookingId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.refundAmount").value(5000.0));
        mvc.perform(post("/api/v1/bookings/" + bookingId + "/cancel")).andExpect(status().isConflict());

        // inventory released: discoverable again
        mvc.perform(get(searchUrl)).andExpect(jsonPath("$[0].property.name").value("Flow Hotel"));
    }

    @Test
    void validationAndErrorMapping() throws Exception {
        mvc.perform(post("/api/v1/owners").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/bookings/does-not-exist")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/properties/search?city=X&checkIn=2030-01-05&checkOut=2030-01-01"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/properties/search?city=X&checkIn=oops&checkOut=2030-01-01"))
                .andExpect(status().isBadRequest());
    }
}

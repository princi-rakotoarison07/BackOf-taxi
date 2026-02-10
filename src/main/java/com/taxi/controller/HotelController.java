package com.taxi.controller;

import com.taxi.model.Hotel;
import com.taxi.util.DBConnection;
import framework.annotation.Controller;
import framework.annotation.GetMapping;
import framework.annotation.RestController;
import java.sql.Connection;
import java.util.List;

@Controller
@RestController
public class HotelController {

    @GetMapping("/api/hotels")
    public List<Hotel> listHotels() throws Exception {
        try (Connection conn = DBConnection.getConnection()) {
            return Hotel.getAll(Hotel.class, conn);
        }
    }
}

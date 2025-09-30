package org.ever._4ever_be_scm.mm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    @GetMapping
    public List<Product> getInventories() {
        return List.of(
                new Product(1L, "Steel Sheet", 120),
                new Product(2L, "Tempered Glass", 60),
                new Product(3L, "Sound Insulation", 85),
                new Product(4L, "Paint Black", 200),
                new Product(5L, "ABS Plastic", 150),
                new Product(6L, "Paint Silver", 180),
                new Product(7L, "Mounting Bolt Set", 500),
                new Product(8L, "Fog Lamp Housing", 40),
                new Product(9L, "Parking Sensor Housing", 35)
        );
    }

    public record Product(Long id, String name, int stock) {}
}

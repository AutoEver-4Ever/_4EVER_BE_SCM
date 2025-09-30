package org.ever._4ever_be_scm.mm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<Product> getProducts() {
        return List.of(
                new Product(1L, "Door Panel", 25),
                new Product(2L, "Front Bumper", 40),
                new Product(3L, "Rear Bumper", 10)
        );
    }

    public record Product(Long id, String name, int stock) {}
}

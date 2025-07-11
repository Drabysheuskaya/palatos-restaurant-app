package com.danven.web_library;


import com.danven.web_library.domain.feedback.Feedback;
import com.danven.web_library.domain.order.Order;
import com.danven.web_library.domain.order.OrderStatus;
import com.danven.web_library.domain.order.PaymentStatus;
import com.danven.web_library.domain.order.RegularOrderService;
import com.danven.web_library.domain.product.*;
import com.danven.web_library.domain.user.Address;
import com.danven.web_library.domain.user.Customer;
import com.danven.web_library.domain.user.Employee;
import com.danven.web_library.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Main Spring Boot application class for the PalaTOS system.
 * Initializes sample data on startup including users, categories,
 * products (with images), a sample order, and feedback.
 */
@SpringBootApplication
public class PalaTOSApplication implements CommandLineRunner {

    private final CustomerRepository customerRepo;
    private final EmployeeRepository employeeRepo;
    private final CategoryRepository categoryRepo;
    private final ProductRepository productRepo;
    private final OrderServiceRepository orderServiceRepo;
    private final OrderRepository orderRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public PalaTOSApplication(
            CustomerRepository customerRepo,
            EmployeeRepository employeeRepo,
            CategoryRepository categoryRepo,
            ProductRepository productRepo,
            OrderServiceRepository orderServiceRepo,
            OrderRepository orderRepo,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.customerRepo     = customerRepo;
        this.employeeRepo     = employeeRepo;
        this.categoryRepo     = categoryRepo;
        this.productRepo      = productRepo;
        this.orderServiceRepo = orderServiceRepo;
        this.orderRepo        = orderRepo;
        this.passwordEncoder  = passwordEncoder;
    }

    /**
     * Application entry point.
     *
     * @param args runtime arguments (unused)
     */
    public static void main(String[] args) {
        SpringApplication.run(PalaTOSApplication.class, args);
    }

    /**
     * Populates the database with initial data when the application starts.
     * This method is run within a transaction to ensure consistency.
     *
     * @param args command-line arguments (ignored)
     * @throws IOException if any of the sample images cannot be loaded
     */
    @Override
    @Transactional
    public void run(String... args) {
        System.out.println("🚀 Starting PalaTOS with updated restaurant categories...");

        // 1) Create and save categories
        var appetizerCat  = new Category("Appetizer");
        var mainCourseCat = new Category("Main Course");
        var beverageCat   = new Category("Beverage");
        categoryRepo.saveAll(List.of(appetizerCat, mainCourseCat, beverageCat));

        // 2) Create and save a customer and an employee
        var address = new Address(
                "Poland",
                Optional.of("Warsaw"),
                Optional.of("Lisa Kuli"),
                Optional.of("103"),
                "05-270"
        );
        var customer = new Customer(
                "Daryna",
                Optional.of("Drabysheuskaya"),
                "danka772@gmail.com",
                passwordEncoder.encode("1234jjj5"),
                true,
                "+48123456789",
                LocalDate.of(2003,10,10),
                address
        );
        var employee = new Employee(
                "John",
                Optional.of("Smith"),
                "employee1@example.com",
                passwordEncoder.encode("pArfh823"),
                true,
                "EMP101"
        );
        customerRepo.save(customer);
        employeeRepo.save(employee);

        // 3) Create and save the default order service
        var standard = new RegularOrderService("Standard Service");
        orderServiceRepo.save(standard);

        // 4) Create sample products with categories and save them
        var pancake = new Food(
                "Pancake Stack",
                "Fluffy pancakes with maple syrup",
                BigDecimal.valueOf(7.50),
                450,
                300,
                Set.of(appetizerCat),
                List.of("flour","eggs","milk","syrup")
        );
        var espresso = new Drink(
                "Espresso Shot",
                "Strong Italian coffee",
                BigDecimal.valueOf(3.00),
                5,
                50,
                0.0,
                false,
                Set.of(beverageCat)
        );
        var fruitSalad = new Dessert(
                "Fruit Salad",
                "Fresh seasonal fruits mix",
                BigDecimal.valueOf(6.25),
                150,
                200,
                0.1,
                Set.of(mainCourseCat)
        );
        var shake = new MilkCocktail(
                "Strawberry Shake",
                "Creamy shake with fresh strawberries",
                BigDecimal.valueOf(9.90),
                300,
                450,
                0.0,
                false,
                IceCreamType.STRAWBERRY,
                0.35,
                Set.of(beverageCat, mainCourseCat)
        );
        var smoothie = new MilkCocktail(
                "Chocolate Lava Smoothie",
                "Rich chocolate shake with lava core",
                BigDecimal.valueOf(11.50),
                400,
                480,
                0.0,
                true,
                IceCreamType.CHOCOLATE,
                0.40,
                Set.of(beverageCat, mainCourseCat)
        );

        // 4a) Load image files and attach to products
        try {
            // Pancake, Espresso, FruitSalad: one image each
            pancake.addImage(new Image(load("picture1.jpg"), ImageFormat.JPG, true, pancake));
            espresso.addImage(new Image(load("picture2.jpg"), ImageFormat.JPG, true, espresso));
            fruitSalad.addImage(new Image(load("picture3.jpg"), ImageFormat.JPG, true, fruitSalad));

            // Shake: first = preview, second = not preview
            shake.addImage(new Image(load("picture5.jpg"), ImageFormat.JPG, true, shake));  // preview
            shake.addImage(new Image(load("picture4.jpg"), ImageFormat.JPG, false, shake)); // not preview

            // Smoothie: first = preview, second = not preview
            smoothie.addImage(new Image(load("picture6.jpg"), ImageFormat.JPG, true, smoothie));     // preview
            smoothie.addImage(new Image(load("picture7.jpeg"), ImageFormat.JPEG, false, smoothie));  // not preview
        } catch (IOException e) {
            System.err.println("⚠️ Failed to load one or more images: " + e.getMessage());
        }


        productRepo.saveAll(List.of(pancake, espresso, fruitSalad, shake, smoothie));

        // 5) Create a sample order for the customer
        var order = new Order(
                5,
                LocalDateTime.now(),
                OrderStatus.NEW,
                PaymentStatus.UNPAID,
                "Please add extra syrup to pancakes.",
                customer,
                standard
        );
        order.addProductOrder(new ProductOrder(pancake,    order, 2, pancake.getPrice()));
        order.addProductOrder(new ProductOrder(espresso,   order, 1, espresso.getPrice()));
        order.addProductOrder(new ProductOrder(fruitSalad, order, 1, fruitSalad.getPrice()));
        orderRepo.save(order);

        // 6) Add feedback for that order
        var fb = new Feedback(
                "Loved the pancakes and coffee!",
                LocalDateTime.now(),
                order,
                customer
        );
        // feedback сохранится каскадом вместе с заказом

        System.out.println("Data initialization complete with updated categories.");
    }

    /**
     * Helper to load a classpath resource from static/images.
     *
     * @param filename the image filename under static/images
     * @return the file’s byte contents
     * @throws IOException if the file cannot be read
     */
    private byte[] load(String filename) throws IOException {
        return new ClassPathResource("static/images/" + filename)
                .getInputStream()
                .readAllBytes();
    }
}

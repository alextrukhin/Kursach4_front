package nau.coursework4.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static nau.coursework4.server.Sendgrid.sendEmail;

@RestController
public class CourseworkApplication {
    private final ProductsStore productsStore = ProductsStoreSingleton.getInstance();
    private final OrdersStore ordersStore = OrdersStoreSingleton.getInstance();

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping(path = "/flowers", produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<Object> getCatalog() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return new ResponseEntity<Object>(gson.toJson(productsStore.data), HttpStatus.OK);
    }

    @PostMapping(path = "/addProduct", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<Object> addProduct(@RequestBody Map<String, Object> datamap) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        ProductBuilder productBuilder = new ProductBuilder();

        int highestId = 0;
        for (Product product : productsStore.data) {
            if (product.getId() > highestId) {
                highestId = product.getId();
            }
        }
        productBuilder.setId(highestId + 1)
                .setName(datamap.get("name").toString())
                .setPrice(Double.parseDouble(datamap.get("price").toString()))
                .setColor(datamap.get("color").toString())
                .setDescription(datamap.get("description").toString())
                .setType(datamap.get("type").toString())
                .setSeasoning(datamap.get("seasoning").toString());
        if (datamap.get("image") != null) {
            productBuilder.setImage(datamap.get("image").toString());
        }
        if (datamap.get("image_single") != null) {
            productBuilder.setImageSingle(datamap.get("image_single").toString());
        }

        Product product = productBuilder.build();

        productsStore.addProduct(product);
        return new ResponseEntity<Object>(gson.toJson(product), HttpStatus.OK);
    }

    @PatchMapping(path = "/updateProduct", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<Object> updateProduct(@RequestBody Map<String, Object> datamap) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        Product product = productsStore.getProductById(Integer.parseInt(datamap.get("id").toString()));
        if (product == null) {
            return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
        }
        ProductBuilder productBuilder = new ProductBuilder(product);

        if (datamap.get("name") != null)
            productBuilder.setName(datamap.get("name").toString());
        if (datamap.get("price") != null)
            productBuilder.setPrice(Double.parseDouble(datamap.get("price").toString()));
        if (datamap.get("color") != null)
            productBuilder.setColor(datamap.get("color").toString());
        if (datamap.get("description") != null)
            productBuilder.setDescription(datamap.get("description").toString());
        if (datamap.get("type") != null)
            productBuilder.setType(datamap.get("type").toString());
        if (datamap.get("seasoning") != null)
            productBuilder.setSeasoning(datamap.get("seasoning").toString());
        if (datamap.get("image") != null)
            productBuilder.setImage(datamap.get("image").toString());
        if (datamap.get("image_single") != null)
            productBuilder.setImageSingle(datamap.get("image_single").toString());

        Product updatedUpdated = productBuilder.build();

        productsStore.updateProduct(updatedUpdated);
        return new ResponseEntity<Object>(gson.toJson(updatedUpdated), HttpStatus.OK);
    }

    @GetMapping(path = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<Object> getOrders() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return new ResponseEntity<Object>(gson.toJson(ordersStore.data), HttpStatus.OK);
    }

    @PostMapping(path = "/addOrder", consumes = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<Object> addOrder(@RequestBody Map<String, Object> datamap) {
        OrderBuilder orderBuilder = new OrderBuilder();

        int highestId = 0;
        for (Order order : ordersStore.data) {
            if (order.getId() > highestId) {
                highestId = order.getId();
            }
        }
        orderBuilder.setId(highestId + 1);
        orderBuilder.setProducts(((ArrayList<LinkedHashMap<String, ?>>) datamap.get("products")).stream().map(p -> {
            OrderProduct orderProduct = new OrderProduct();
            orderProduct.setProductId(Integer.parseInt(p.get("productId").toString()));
            orderProduct.setQuantity(Integer.parseInt(p.get("quantity").toString()));
            return orderProduct;
        }).collect(Collectors.toList()));
        orderBuilder.setBunches(((ArrayList<LinkedHashMap<String, ?>>) datamap.get("bunches")).stream().map(p -> {
            OrderBunch orderProduct = new OrderBunch();
            BunchBuilder bunchBuilder = new BunchBuilder();
            LinkedHashMap<String, ?> bunch = (LinkedHashMap<String, ?>) p.get("bunch");
            if (bunch.get("id") != null) {
                bunchBuilder.setId(Integer.parseInt(bunch.get("id").toString()));
            }
            ArrayList<LinkedHashMap<String, ?>> bunchProducts = (ArrayList<LinkedHashMap<String, ?>>) bunch.get("products");
            bunchBuilder.setProducts(bunchProducts.stream().map(bp -> {
                BunchProduct bunchProduct = new BunchProduct();
                bunchProduct.setId(Integer.parseInt(bp.get("id").toString()));
                bunchProduct.setX(Integer.parseInt(bp.get("x").toString()));
                bunchProduct.setY(Integer.parseInt(bp.get("y").toString()));
                return bunchProduct;
            }).collect(Collectors.toList()));
            orderProduct.setBunch(bunchBuilder.build());
            orderProduct.setQuantity(Integer.parseInt(p.get("quantity").toString()));
            return orderProduct;
        }).collect(Collectors.toList()));
        orderBuilder.setStatus(datamap.get("status").toString());
        orderBuilder.setCreatedAt(Long.parseLong(datamap.get("createdAt").toString()));
        orderBuilder.setLastStatusChange(Long.parseLong(datamap.get("lastStatusChange").toString()));
        orderBuilder.setClient_firstname(datamap.get("client_firstname").toString());
        orderBuilder.setClient_lastname(datamap.get("client_lastname").toString());
        orderBuilder.setClient_address(datamap.get("client_address").toString());
        orderBuilder.setClient_phone(datamap.get("client_phone").toString());
        orderBuilder.setClient_email(datamap.get("client_email").toString());
        orderBuilder.setClient_comments(datamap.get("client_comments").toString());
        orderBuilder.setPayment_type(Order.PaymentType.valueOf(datamap.get("payment_type").toString()));
        orderBuilder.setDelivery_type(Order.DeliveryType.valueOf(datamap.get("delivery_type").toString()));

        Order order = orderBuilder.build();
        ordersStore.addOrder(order);

        try {
            sendEmail(
                    order.getClient_email(),
                    "New order: #" + order.getId(),
                    "Your order content:\\n\\nProducts:\\n" + order.getProducts().stream()
                            .map(p -> {
                                Product product = productsStore.getProductById(p.getProductId());
                                return product.getName() + " " + product.getColor() + " - $" + product.getPrice() + " x " + p.getQuantity();
                            })
                            .collect(Collectors.joining("\\n")) + "\\n\\nCustom bunches:\\n" + order.getBunches().stream()
                            .map(bunch -> bunch.getQuantity() + "x:\\n" + bunch.getBunch().getProducts().stream()
                                    .map(p -> {
                                        Product product = productsStore.getProductById(p.getId());
                                        return product.getName() + " " + product.getColor() + " - $" + product.getPrice() + ",";
                                    })
                                    .collect(Collectors.joining("\\n"))
                            )
                            .collect(Collectors.joining("\\n"))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @PatchMapping(path = "/updateOrder", consumes = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<Object> updateOrder(@RequestBody Map<String, Object> datamap) {
        int id = Integer.parseInt(datamap.get("id").toString());
        Order order = ordersStore.getOrderById(id);
        if (order == null) {
            return new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
        }
        OrderBuilder orderBuilder = new OrderBuilder(order);

        if (datamap.get("products") != null)
            orderBuilder.setProducts(((ArrayList<LinkedHashMap<String, ?>>) datamap.get("products")).stream().map(p -> {
                OrderProduct orderProduct = new OrderProduct();
                orderProduct.setProductId(Integer.parseInt(p.get("productId").toString()));
                orderProduct.setQuantity(Integer.parseInt(p.get("quantity").toString()));
                return orderProduct;
            }).collect(Collectors.toList()));
        if (datamap.get("bunches") != null)
            orderBuilder.setBunches(((ArrayList<LinkedHashMap<String, ?>>) datamap.get("bunches")).stream().map(p -> {
                OrderBunch orderProduct = new OrderBunch();
                BunchBuilder bunchBuilder = new BunchBuilder();
                bunchBuilder.setId(Integer.parseInt(((LinkedHashMap<String, ?>) p.get("bunch")).get("id").toString()));
                bunchBuilder.setProducts(((ArrayList<LinkedHashMap<String, ?>>) ((LinkedHashMap<String, ?>) p.get("bunch")).get("products")).stream().map(bp -> {
                    BunchProduct bunchProduct = new BunchProduct();
                    bunchProduct.setId(Integer.parseInt(bp.get("id").toString()));
                    bunchProduct.setX(Integer.parseInt(bp.get("x").toString()));
                    bunchProduct.setY(Integer.parseInt(bp.get("y").toString()));
                    return bunchProduct;
                }).collect(Collectors.toList()));
                orderProduct.setBunch(bunchBuilder.build());
                orderProduct.setQuantity(Integer.parseInt(p.get("quantity").toString()));
                return orderProduct;
            }).collect(Collectors.toList()));
        if (datamap.get("status") != null)
            orderBuilder.setStatus(datamap.get("status").toString());
        if (datamap.get("createdAt") != null)
            orderBuilder.setCreatedAt(Long.parseLong(datamap.get("createdAt").toString()));
        if (datamap.get("lastStatusChange") != null)
            orderBuilder.setLastStatusChange(Long.parseLong(datamap.get("lastStatusChange").toString()));
        if (datamap.get("client_firstname") != null)
            orderBuilder.setClient_firstname(datamap.get("client_firstname").toString());
        if (datamap.get("client_lastname") != null)
            orderBuilder.setClient_lastname(datamap.get("client_lastname").toString());
        if (datamap.get("client_address") != null)
            orderBuilder.setClient_address(datamap.get("client_address").toString());
        if (datamap.get("client_phone") != null)
            orderBuilder.setClient_phone(datamap.get("client_phone").toString());
        if (datamap.get("client_email") != null)
            orderBuilder.setClient_email(datamap.get("client_email").toString());
        if (datamap.get("client_comments") != null)
            orderBuilder.setClient_comments(datamap.get("client_comments").toString());
        if (datamap.get("payment_type") != null)
            orderBuilder.setPayment_type(Order.PaymentType.valueOf(datamap.get("payment_type").toString()));
        if (datamap.get("delivery_type") != null)
            orderBuilder.setDelivery_type(Order.DeliveryType.valueOf(datamap.get("delivery_type").toString()));

        ordersStore.updateOrder(orderBuilder.build());

        return new ResponseEntity<Object>(HttpStatus.OK);
    }

    @PostMapping(path = "/updateOrderStatus", consumes = MediaType.APPLICATION_JSON_VALUE)
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<Object> updateOrderStatus(@RequestBody Map<String, Object> datamap) {
        int id = Integer.parseInt(datamap.get("id").toString());
        Order order = ordersStore.getOrderById(id);
        OrderBuilder orderBuilder = new OrderBuilder(order);

        orderBuilder.setStatus(datamap.get("status").toString());
        orderBuilder.setLastStatusChange(Long.parseLong(datamap.get("lastStatusChange").toString()));

        ordersStore.updateOrder(orderBuilder.build());

        try {
            sendEmail(order.getClient_email(), "Order #" + order.getId() + " status changed", "Order status changed to " + datamap.get("status").toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ResponseEntity<Object>(HttpStatus.OK);
    }
}

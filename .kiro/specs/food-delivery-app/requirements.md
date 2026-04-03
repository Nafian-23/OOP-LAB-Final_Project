# Requirements Document

## Introduction

A Java-based Food Delivery Application providing two console-based user interfaces (Customer and Restaurant) and a lightweight HTTP API that simulates a WSDL endpoint. The system uses file-based storage (no database) and follows OOP principles with single-responsibility classes. Customers can browse restaurants, place orders, track delivery status, and apply discount coupons. Restaurants can manage menus, accept orders, and update order status. A built-in HTTP server exposes a JSON API on port 8000.

## Glossary

- **System**: The Food Delivery Application as a whole
- **Customer**: A registered user who browses restaurants and places orders
- **Restaurant**: A registered food vendor that manages menus and fulfills orders
- **Menu**: The collection of MenuItem objects belonging to a Restaurant
- **MenuItem**: A single food item with name, price, description, availability, and stock quantity
- **Order**: A confirmed request from a Customer containing one or more OrderItems, associated with a Restaurant
- **OrderItem**: A MenuItem with a specified quantity within an Order
- **Cart**: A transient, in-memory collection of OrderItems assembled by a Customer before placing an Order
- **OrderStatus**: An enumerated state of an Order: PLACED, PREPARING, READY, DELIVERED
- **Coupon**: A discount code that applies a fixed percentage reduction to an Order total
- **DeliveryFee**: A fixed fee added to an Order based on the Restaurant's delivery configuration
- **Rider**: A simulated delivery agent selected from a predefined list
- **DataStorage**: The file-based persistence interface used by all repositories
- **API**: The HTTP server running on port 8000 that exposes restaurant, menu, and order data as JSON
- **CustomerUI**: The console interface for Customer interactions
- **RestaurantUI**: The console interface for Restaurant interactions
- **RestaurantService**: The service layer managing restaurant registration and retrieval logic
- **OrderService**: The service layer managing order lifecycle
- **UserService**: The service layer managing customer registration and authentication
- **PaymentService**: The service layer managing simulated payment processing
- **SearchService**: The service layer managing search and filter operations
- **CouponService**: The service layer managing coupon validation and discount application
- **RestaurantRepository**: The persistence layer for Restaurant objects
- **OrderRepository**: The persistence layer for Order objects
- **UserRepository**: The persistence layer for Customer objects

---

## Requirements

### Requirement 1: Customer Registration and Authentication

**User Story:** As a customer, I want to register and log in, so that I can place orders and view my order history.

#### Acceptance Criteria

1. THE UserService SHALL register a new Customer with a unique username, password, full name, and delivery address.
2. WHEN a username that already exists is submitted for registration, THE UserService SHALL reject the registration and return a descriptive error message.
3. WHEN a Customer provides valid credentials, THE UserService SHALL authenticate the Customer and return the Customer session.
4. IF a Customer provides invalid credentials, THEN THE UserService SHALL deny access and display an error message.
5. THE UserRepository SHALL persist Customer records to `users.dat` using Java serialization.
6. WHEN the application starts, THE UserRepository SHALL load all Customer records from `users.dat` if the file exists.

---

### Requirement 2: Restaurant Registration

**User Story:** As a restaurant owner, I want to register my restaurant, so that customers can discover and order from me.

#### Acceptance Criteria

1. THE RestaurantService SHALL register a new Restaurant with a unique name, address, cuisine type, contact information, opening hours, closing hours, and delivery radius in kilometers.
2. WHEN a restaurant name that already exists is submitted, THE RestaurantService SHALL reject the registration and return a descriptive error message.
3. THE RestaurantRepository SHALL persist Restaurant records to `restaurants.dat` using Java serialization.
4. WHEN the application starts, THE RestaurantRepository SHALL load all Restaurant records from `restaurants.dat` if the file exists.

---

### Requirement 3: Nearest Restaurant Discovery

**User Story:** As a customer, I want to see restaurants near my delivery address, so that I can choose a restaurant that can deliver to me.

#### Acceptance Criteria

1. WHEN a Customer enters a delivery address, THE RestaurantService SHALL return a list of Restaurants whose delivery radius covers that address, sorted by proximity in ascending order.
2. WHEN no Restaurants serve the given delivery address, THE CustomerUI SHALL display an empty-state message indicating no restaurants are available in that area.
3. THE RestaurantService SHALL calculate proximity using the street address string matching within the delivery radius field as a simplified distance model.

---

### Requirement 4: Restaurant Browsing and Sorting

**User Story:** As a customer, I want to browse and sort restaurants, so that I can find the best option for my needs.

#### Acceptance Criteria

1. WHEN a Customer requests the restaurant list, THE CustomerUI SHALL display each Restaurant's name, cuisine type, average rating, and estimated delivery time.
2. WHEN a Customer selects a sort option of "rating", THE RestaurantService SHALL return Restaurants sorted by average rating in descending order.
3. WHEN a Customer selects a sort option of "delivery time", THE RestaurantService SHALL return Restaurants sorted by estimated delivery time in ascending order.
4. WHEN a Customer selects a sort option of "name", THE RestaurantService SHALL return Restaurants sorted alphabetically by name in ascending order.

---

### Requirement 5: Menu Viewing

**User Story:** As a customer, I want to view a restaurant's full menu, so that I can decide what to order.

#### Acceptance Criteria

1. WHEN a Customer selects a Restaurant, THE CustomerUI SHALL display all MenuItems for that Restaurant, each showing name, price, description, and availability status.
2. WHILE a MenuItem has availability set to false, THE CustomerUI SHALL display that item as unavailable and prevent it from being added to the Cart.

---

### Requirement 6: Menu Management

**User Story:** As a restaurant owner, I want to manage my menu, so that I can keep my offerings accurate and up to date.

#### Acceptance Criteria

1. THE RestaurantService SHALL add a new MenuItem to a Restaurant's Menu with a name, price, description, initial stock quantity, and availability status.
2. WHEN a restaurant owner updates a MenuItem's availability, THE RestaurantService SHALL persist the updated availability status to `menu_items.dat`.
3. WHEN a restaurant owner updates a MenuItem's stock quantity, THE RestaurantService SHALL persist the updated quantity to `menu_items.dat`.
4. THE RestaurantService SHALL support adding one or more customization options (e.g., "Extra cheese", "Spicy level") to a MenuItem.
5. THE RestaurantRepository SHALL persist MenuItem records to `menu_items.dat` using Java serialization.

---

### Requirement 7: Search Functionality

**User Story:** As a customer, I want to search for restaurants and menu items, so that I can quickly find what I want.

#### Acceptance Criteria

1. WHEN a Customer submits a search query, THE SearchService SHALL return all Restaurants whose name or cuisine type contains the query string (case-insensitive).
2. WHEN a Customer submits a search query, THE SearchService SHALL return all MenuItems across all Restaurants whose name contains the query string (case-insensitive).
3. WHEN a Customer applies an availability filter, THE SearchService SHALL return only MenuItems with availability set to true.

---

### Requirement 8: Cart and Order Placement

**User Story:** As a customer, I want to add items to a cart and place an order, so that I can receive food from a restaurant.

#### Acceptance Criteria

1. THE CustomerUI SHALL maintain an in-memory Cart for the active Customer session.
2. WHEN a Customer adds a MenuItem to the Cart, THE CustomerUI SHALL allow the Customer to specify a quantity greater than zero.
3. WHEN a Customer requests an order summary, THE OrderService SHALL calculate and display the subtotal (sum of item prices × quantities), delivery fee, and total (subtotal + delivery fee).
4. WHEN a Customer confirms an order, THE OrderService SHALL create an Order with a unique order ID, set the initial OrderStatus to PLACED, and persist the Order to `orders.dat`.
5. WHEN an Order is confirmed, THE CustomerUI SHALL display the unique order ID as a confirmation reference.
6. IF a Cart is empty when a Customer attempts to place an order, THEN THE CustomerUI SHALL display an error message and prevent order submission.

---

### Requirement 9: Coupon and Discount Application

**User Story:** As a customer, I want to apply a coupon code to my order, so that I can receive a discount.

#### Acceptance Criteria

1. WHEN a Customer enters a coupon code, THE CouponService SHALL validate the code against the list of active Coupons.
2. WHEN a valid coupon code is entered, THE CouponService SHALL apply the Coupon's fixed percentage discount to the Order subtotal and return the discounted total.
3. IF an invalid or expired coupon code is entered, THEN THE CouponService SHALL return a descriptive error message and leave the Order total unchanged.
4. THE CouponService SHALL support coupons with percentage values of 10% and 20%.

---

### Requirement 10: Order Tracking

**User Story:** As a customer, I want to track my order status, so that I know when my food will arrive.

#### Acceptance Criteria

1. WHEN a Customer requests the status of an Order by order ID, THE OrderService SHALL return the current OrderStatus.
2. THE OrderService SHALL support the following OrderStatus transitions in sequence: PLACED → PREPARING → READY → DELIVERED.
3. WHEN a Customer requests their order history, THE OrderService SHALL return all Orders associated with that Customer's ID, sorted by creation time in descending order.

---

### Requirement 11: Restaurant Order Management

**User Story:** As a restaurant owner, I want to manage incoming orders, so that I can prepare and fulfill them efficiently.

#### Acceptance Criteria

1. WHEN a restaurant owner views incoming orders, THE RestaurantUI SHALL display all Orders with OrderStatus PLACED for that Restaurant, showing order ID, Customer name, items, and total.
2. WHEN a restaurant owner accepts an order, THE OrderService SHALL transition the Order's OrderStatus from PLACED to PREPARING.
3. WHEN a restaurant owner marks an order as ready, THE OrderService SHALL transition the Order's OrderStatus from PREPARING to READY.
4. WHEN a restaurant owner assigns a Rider to an Order, THE OrderService SHALL record the selected Rider name on the Order and transition the OrderStatus from READY to DELIVERED.
5. IF a restaurant owner attempts an invalid OrderStatus transition, THEN THE OrderService SHALL reject the transition and return a descriptive error message.

---

### Requirement 12: Simulated Payment Processing

**User Story:** As a customer, I want to pay for my order, so that it can be confirmed and fulfilled.

#### Acceptance Criteria

1. WHEN a Customer selects "Cash on Delivery" as the payment method, THE PaymentService SHALL record the Payment with status PENDING and associate it with the Order.
2. WHEN a Customer selects "Card Payment" as the payment method, THE PaymentService SHALL simulate card processing and record the Payment with status COMPLETED.
3. WHEN a Payment is recorded, THE PaymentService SHALL persist the Payment status alongside the Order in `orders.dat`.
4. WHEN a Customer requests payment status for an Order, THE PaymentService SHALL return the current Payment status for that Order.

---

### Requirement 13: HTTP API Endpoint

**User Story:** As an API consumer, I want to query restaurant, menu, and order data over HTTP, so that I can integrate with the Food Delivery system programmatically.

#### Acceptance Criteria

1. THE API SHALL start an HTTP server on port 8000 using `com.sun.net.httpserver`.
2. WHEN a GET request is made to `/api/restaurants?area={area}`, THE APIHandler SHALL return a JSON array of Restaurants whose delivery radius covers the specified area, with HTTP status 200.
3. WHEN a GET request is made to `/api/restaurant/{id}/menu`, THE APIHandler SHALL return a JSON array of MenuItems for the specified Restaurant, with HTTP status 200.
4. WHEN a GET request is made to `/api/order/{id}/status`, THE APIHandler SHALL return a JSON object containing the order ID and current OrderStatus, with HTTP status 200.
5. IF a request is made to `/api/restaurant/{id}/menu` with an unknown restaurant ID, THEN THE APIHandler SHALL return HTTP status 404 with a JSON error message.
6. IF a request is made to `/api/order/{id}/status` with an unknown order ID, THEN THE APIHandler SHALL return HTTP status 404 with a JSON error message.
7. THE API SHALL return all responses with `Content-Type: application/json`.

---

### Requirement 14: File-Based Data Persistence

**User Story:** As a system operator, I want all data persisted to files, so that the application state survives restarts without a database.

#### Acceptance Criteria

1. THE DataStorage interface SHALL define a `save(Object data, String filePath)` method and a `load(String filePath)` method.
2. THE RestaurantRepository SHALL implement DataStorage and persist data to `restaurants.dat` and `menu_items.dat`.
3. THE OrderRepository SHALL implement DataStorage and persist data to `orders.dat`.
4. THE UserRepository SHALL implement DataStorage and persist data to `users.dat`.
5. WHEN a `.dat` file does not exist on load, THE DataStorage implementation SHALL return an empty collection without throwing an exception.
6. WHEN a write operation fails, THE DataStorage implementation SHALL log the error and propagate a descriptive runtime exception.

---

### Requirement 15: Console UI Navigation

**User Story:** As a user, I want a clear console menu, so that I can navigate the application without confusion.

#### Acceptance Criteria

1. THE CustomerUI SHALL present a numbered main menu with options: Browse Restaurants, Search, View Cart, View Orders, Apply Coupon, and Exit.
2. THE RestaurantUI SHALL present a numbered main menu with options: Manage Menu, View Orders, Update Order Status, and Exit.
3. WHEN an invalid menu option is entered, THE CustomerUI SHALL display an error message and re-display the menu.
4. WHEN an invalid menu option is entered, THE RestaurantUI SHALL display an error message and re-display the menu.
5. THE Main class SHALL present an initial selection menu allowing the user to launch either the CustomerUI or the RestaurantUI.

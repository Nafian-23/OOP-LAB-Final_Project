# Implementation Plan: Food Delivery Application

## Overview

Java 17 console application with embedded HTTP server, file-based persistence, two console UIs, and a JSON API. No external frameworks except jqwik for property-based testing.

## Tasks

- [x] 1. Project setup
  - Create Maven `pom.xml` with Java 17 source/target, jqwik + JUnit 5 test dependencies, and `maven-jar-plugin` configured to produce a fat JAR with `Main` as the entry point
  - Create directory structure: `src/main/java/com/fooddelivery/{model,repository,service,api,ui}/` and `src/test/java/com/fooddelivery/{unit,property}/`
  - _Requirements: 14.1_

- [ ] 2. Model classes and enums
  - [ ] 2.1 Implement enums and core model POJOs
    - Create `OrderStatus` enum: `PLACED`, `PREPARING`, `READY`, `DELIVERED`
    - Create `PaymentMethod` enum: `CASH_ON_DELIVERY`, `CARD`
    - Create `PaymentStatus` enum: `PENDING`, `COMPLETED`
    - Implement `User`, `Restaurant`, `MenuItem`, `OrderItem`, `Order`, `Payment`, `Coupon` as `Serializable` classes with all fields from the design, getters/setters, and `equals`/`hashCode` based on `id` (or `code` for Coupon)
    - _Requirements: 1.1, 2.1, 6.1, 8.3, 9.4, 12.1_

  - [ ]* 2.2 Write property test for serialization round-trip (Property 4)
    - **Property 4: Serialization round-trip**
    - **Validates: Requirements 1.5, 2.3, 6.5, 12.3, 14.2, 14.3, 14.4**

- [ ] 3. Repository layer
  - [ ] 3.1 Implement `DataStorage` interface and `FileDataStorage`
    - Define `DataStorage` interface with `save(Object data, String filePath): void` and `load(String filePath): Object`
    - Implement `FileDataStorage` using `ObjectOutputStream`/`ObjectInputStream`; return empty `ArrayList` on `FileNotFoundException`; log to stderr and throw `RuntimeException` on write failure
    - _Requirements: 14.1, 14.5, 14.6_

  - [ ]* 3.2 Write property test for missing file returns empty collection (Property 21)
    - **Property 21: Missing .dat file returns empty collection**
    - **Validates: Requirements 14.5**

  - [ ]* 3.3 Write property test for failed write propagates exception (Property 22)
    - **Property 22: Failed write propagates exception**
    - **Validates: Requirements 14.6**

  - [ ] 3.4 Implement `UserRepository`, `RestaurantRepository`, `OrderRepository`
    - Each repository holds an in-memory list loaded at construction via `FileDataStorage`
    - `RestaurantRepository` manages `restaurants.dat` and `menu_items.dat`
    - `OrderRepository` manages `orders.dat`
    - `UserRepository` manages `users.dat`
    - Each mutation flushes the full in-memory list back to disk
    - _Requirements: 1.5, 1.6, 2.3, 2.4, 6.5, 14.2, 14.3, 14.4_

  - [ ]* 3.5 Write unit tests for `FileDataStorage` absent-file behavior
    - Test `load` on a non-existent path returns empty list without exception
    - _Requirements: 14.5_

- [ ] 4. Service layer — UserService
  - [ ] 4.1 Implement `UserService`
    - `register(username, password, fullName, address)`: hash password with SHA-256, reject duplicate usernames with `IllegalArgumentException`, persist via `UserRepository`
    - `login(username, password)`: hash supplied password, compare; return `User` or throw `AuthException`
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [ ]* 4.2 Write property test for registration round-trip (Property 1)
    - **Property 1: Registration round-trip**
    - **Validates: Requirements 1.1, 1.3**

  - [ ]* 4.3 Write property test for duplicate registration rejected (Property 2)
    - **Property 2: Duplicate entity registration is rejected**
    - **Validates: Requirements 1.2, 2.2**

  - [ ]* 4.4 Write property test for invalid credentials rejected (Property 3)
    - **Property 3: Invalid credentials are rejected**
    - **Validates: Requirements 1.4**

- [ ] 5. Service layer — RestaurantService
  - [ ] 5.1 Implement `RestaurantService`
    - `register(...)`: reject duplicate names with `IllegalArgumentException`, assign UUID, persist via `RestaurantRepository`
    - `findByArea(address)`: return restaurants whose `deliveryRadiusKm` field covers the address (simplified string-match model), sorted by proximity ascending
    - `sortByRating()`, `sortByDeliveryTime()`, `sortByName()`: return sorted copies of the restaurant list
    - `addMenuItem(restaurantId, ...)`: create `MenuItem` with UUID, append to restaurant's list, persist
    - `updateMenuItemAvailability(itemId, available)` and `updateMenuItemStock(itemId, qty)`: find item, update, persist
    - _Requirements: 2.1, 2.2, 3.1, 3.2, 4.2, 4.3, 4.4, 6.1, 6.2, 6.3, 6.4_

  - [ ]* 5.2 Write property test for area filter correctness (Property 5)
    - **Property 5: Area filter correctness**
    - **Validates: Requirements 3.1**

  - [ ]* 5.3 Write property test for sort ordering invariant (Property 6)
    - **Property 6: Sort ordering invariant**
    - **Validates: Requirements 4.2, 4.3, 4.4**

- [ ] 6. Service layer — SearchService and CouponService
  - [ ] 6.1 Implement `SearchService`
    - `searchRestaurants(query)`: case-insensitive match on name or cuisineType
    - `searchMenuItems(query)`: case-insensitive match on name across all restaurants
    - `filterAvailableItems(items)`: return only items where `available = true`
    - _Requirements: 7.1, 7.2, 7.3_

  - [ ]* 6.2 Write property test for search filter correctness (Property 8)
    - **Property 8: Search filter correctness**
    - **Validates: Requirements 7.1, 7.2**

  - [ ]* 6.3 Write property test for availability filter correctness (Property 9)
    - **Property 9: Availability filter correctness**
    - **Validates: Requirements 7.3**

  - [ ] 6.4 Implement `CouponService`
    - Maintain a hardcoded list of active `Coupon` objects (10% and 20% codes)
    - `validate(code)`: return matching active coupon or throw `CouponException`
    - `apply(coupon, subtotal)`: return `subtotal * (1 - discountPercent / 100.0)`
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

  - [ ]* 6.5 Write property test for coupon discount arithmetic (Property 13)
    - **Property 13: Coupon discount arithmetic**
    - **Validates: Requirements 9.2, 9.4**

  - [ ]* 6.6 Write property test for invalid coupon rejected (Property 14)
    - **Property 14: Invalid coupon is rejected**
    - **Validates: Requirements 9.3**

- [ ] 7. Service layer — PaymentService and OrderService
  - [ ] 7.1 Implement `PaymentService`
    - `processPayment(order, method)`: create `Payment`; set status `PENDING` for `CASH_ON_DELIVERY`, `COMPLETED` for `CARD`; attach to order
    - _Requirements: 12.1, 12.2, 12.3, 12.4_

  - [ ]* 7.2 Write property test for payment method determines payment status (Property 17)
    - **Property 17: Payment method determines payment status**
    - **Validates: Requirements 12.1, 12.2**

  - [ ] 7.3 Implement `OrderService`
    - `createOrder(customerId, restaurantId, cartItems, coupon, paymentMethod)`: validate non-empty cart, compute subtotal/deliveryFee/discount/total, call `PaymentService.processPayment`, assign UUID and `createdAt`, set status `PLACED`, persist via `OrderRepository`
    - `getOrder(orderId)`: return order or throw if not found
    - `getOrdersByCustomer(customerId)`: return orders sorted by `createdAt` descending
    - `advanceStatus(orderId, restaurantId)`: enforce `PLACED→PREPARING→READY`; throw `IllegalStateException` on invalid transition
    - `assignRider(orderId, riderName)`: enforce `READY→DELIVERED`; record rider name
    - _Requirements: 8.3, 8.4, 8.5, 10.1, 10.2, 10.3, 11.2, 11.3, 11.4, 11.5_

  - [ ]* 7.4 Write property test for order total calculation invariant (Property 11)
    - **Property 11: Order total calculation invariant**
    - **Validates: Requirements 8.3**

  - [ ]* 7.5 Write property test for order creation round-trip (Property 12)
    - **Property 12: Order creation round-trip**
    - **Validates: Requirements 8.4, 10.1**

  - [ ]* 7.6 Write property test for order status machine transitions (Property 15)
    - **Property 15: Order status machine transitions**
    - **Validates: Requirements 10.2, 11.2, 11.3, 11.4**

  - [ ]* 7.7 Write property test for invalid status transition rejected (Property 16)
    - **Property 16: Invalid status transition is rejected**
    - **Validates: Requirements 11.5**

  - [ ]* 7.8 Write property test for order history sorted descending (Property 18)
    - **Property 18: Order history is sorted by creation time descending**
    - **Validates: Requirements 10.3**

- [ ] 8. Checkpoint — core services
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 9. API layer
  - [ ] 9.1 Implement `FoodAppAPI` and `APIHandler`
    - `FoodAppAPI.start(port)`: create `HttpServer` on the given port, register `/api/` context to `APIHandler`
    - `APIHandler.handle(HttpExchange)`: route on path pattern:
      - `GET /api/restaurants?area=` → `RestaurantService.findByArea`, serialize to JSON array
      - `GET /api/restaurant/{id}/menu` → look up restaurant, return `menuItems` as JSON array; 404 if unknown ID
      - `GET /api/order/{id}/status` → look up order, return `{"id":..., "status":...}`; 404 if unknown ID
    - `toJson(Object)`: manual JSON serialization (no external libs); handle `List`, `Restaurant`, `MenuItem`, `Order`
    - Return 400 for malformed query parameters; always set `Content-Type: application/json`
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7_

  - [ ]* 9.2 Write property test for API 404 on unknown resources (Property 19)
    - **Property 19: API returns 404 for unknown resources**
    - **Validates: Requirements 13.5, 13.6**

  - [ ]* 9.3 Write property test for API response format invariant (Property 20)
    - **Property 20: API response format invariant**
    - **Validates: Requirements 13.2, 13.3, 13.4, 13.7**

  - [ ]* 9.4 Write unit tests for `APIHandler` routing
    - Verify each path pattern dispatches to the correct handler
    - Verify 404 returned for unknown IDs
    - _Requirements: 13.5, 13.6_

- [ ] 10. UI layer — CustomerUI
  - [ ] 10.1 Implement `CustomerUI`
    - Numbered main menu: Browse Restaurants, Search, View Cart, View Orders, Apply Coupon, Exit
    - Browse: call `RestaurantService.findByArea`, display name/cuisine/rating/delivery time; offer sort sub-menu
    - Search: call `SearchService.searchRestaurants` and `searchMenuItems`; display results
    - View Cart: display current in-memory cart items and running total; allow item removal
    - Add to cart: validate `available = true` and quantity > 0 before adding; display error otherwise
    - Apply Coupon: call `CouponService.validate` then `apply`; display discounted total or error
    - Place Order: call `OrderService.createOrder`; display order ID on success; show error if cart empty
    - View Orders: call `OrderService.getOrdersByCustomer`; display status per order
    - Invalid menu input: display error and re-show menu
    - _Requirements: 3.2, 4.1, 5.1, 5.2, 7.1, 7.2, 7.3, 8.1, 8.2, 8.5, 8.6, 9.1, 9.3, 10.3, 15.1, 15.3_

  - [ ]* 10.2 Write property test for unavailable item cannot be added to cart (Property 7)
    - **Property 7: Unavailable item cannot be added to cart**
    - **Validates: Requirements 5.2**

  - [ ]* 10.3 Write property test for invalid cart quantity rejected (Property 10)
    - **Property 10: Invalid cart quantity is rejected**
    - **Validates: Requirements 8.2**

- [ ] 11. UI layer — RestaurantUI
  - [ ] 11.1 Implement `RestaurantUI`
    - Numbered main menu: Manage Menu, View Orders, Update Order Status, Exit
    - Manage Menu: add new `MenuItem` via `RestaurantService.addMenuItem`; update availability/stock via `updateMenuItemAvailability`/`updateMenuItemStock`
    - View Orders: display all PLACED orders for the restaurant (order ID, customer name, items, total)
    - Update Order Status: call `OrderService.advanceStatus` or `assignRider`; display new status or error
    - Invalid menu input: display error and re-show menu
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 11.1, 11.2, 11.3, 11.4, 11.5, 15.2, 15.4_

- [ ] 12. Main entry point
  - Implement `Main.java`: instantiate repositories (triggers `.dat` file loading), construct services, start `FoodAppAPI` on port 8000, present top-level menu to launch `CustomerUI` or `RestaurantUI`
  - _Requirements: 1.6, 2.4, 13.1, 15.5_

- [ ] 13. Checkpoint — full integration
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 14. Unit tests
  - [ ] 14.1 Write unit tests for `OrderService.createOrder`
    - Known cart → verify order ID non-null, status = PLACED, subtotal/total arithmetic correct
    - _Requirements: 8.3, 8.4_

  - [ ] 14.2 Write unit tests for `CouponService.apply`
    - 10% coupon on known subtotal; 20% coupon on known subtotal; verify exact values
    - _Requirements: 9.2, 9.4_

  - [ ] 14.3 Write unit tests for `RestaurantService.findByArea`
    - Known restaurant set with varying radii; assert correct subset returned
    - _Requirements: 3.1_

- [ ] 15. README
  - Write `README.md` at project root documenting: build instructions (`mvn package`), run instructions (`java -jar`), API endpoint reference (paths, query params, example responses), and data file locations
  - _Requirements: 13.1, 13.2, 13.3, 13.4_

- [ ] 16. Final checkpoint
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests use jqwik with a minimum of 100 iterations each and must include the comment tag `// Feature: food-delivery-app, Property N: <property_text>`
- Unit tests and property tests are complementary — both are required for full coverage
- All service-layer exceptions are unchecked; the UI layer is the sole catch boundary for user-facing messages

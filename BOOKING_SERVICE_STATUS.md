# Booking Service - Implementation Status

## ✅ **COMPLETED**

### 1. **Core Entities**
- ✅ `Booking` entity with `@Version` for optimistic locking
- ✅ All required fields (userId, eventId, seatId, quantity, totalPrice, etc.)
- ✅ Booking statuses: PENDING, CONFIRMED, CANCELLED, EXPIRED
- ✅ Audit fields: createdAt, updatedAt, expiresAt, cancellationDate

### 2. **DTOs**
- ✅ `BookingRequest` - Create new booking
- ✅ `BookingResponse` - Return booking details
- ✅ `ConfirmBookingRequest` - Confirm booking with payment

### 3. **Repository**
- ✅ `BookingRepository` extends JpaRepository
- ✅ Custom queries:
  - `findByUserId(Long userId)`
  - `findByEventId(Long eventId)`
  - `findByBookingStatusAndExpiresAtBefore(BookingStatus, LocalDateTime)`

### 4. **gRPC Client**
- ✅ `EventGrpcClient` with ManagedChannel
- ✅ `reserveTickets()` with retry logic and exponential backoff
- ✅ `releaseTickets()` for cancellation/expiration
- ✅ `getEventById()` for event details
- ✅ Exception mapping (gRPC → Java exceptions)

### 5. **Service Layer**
- ✅ `BookingService` with all business logic:
  - `createBooking()` - Reserve tickets via gRPC, create booking
  - `confirmBooking()` - Update status to CONFIRMED after payment
  - `cancelBooking()` - Release tickets and update status to CANCELLED
  - `getBookingById()` - Retrieve booking details
  - `getUserBookings()` - Get all bookings for a user
  - `getEventBookings()` - Get all bookings for an event
  - `releaseExpiredBookings()` - Scheduled task to expire bookings
- ✅ Retry logic for optimistic locking failures
- ✅ Transaction management with `@Transactional`

### 6. **Controller**
- ✅ `BookingController` with REST endpoints:
  - `POST /api/v1/bookings` - Create booking
  - `POST /api/v1/bookings/{id}/confirm` - Confirm booking
  - `POST /api/v1/bookings/{id}/cancel` - Cancel booking
  - `GET /api/v1/bookings/{id}` - Get booking
  - `GET /api/v1/bookings/user/{userId}` - User bookings
  - `GET /api/v1/bookings/event/{eventId}` - Event bookings

### 7. **Exception Handling**
- ✅ `BookingNotFoundException`
- ✅ `BookingExpiredException`
- ✅ `SeatNotAvailableException`
- ✅ `InvalidBookingStateException`
- ✅ `GlobalExceptionHandler` with proper HTTP status codes

### 8. **Scheduler**
- ✅ `BookingExpirationScheduler` - Runs every 60 seconds
- ✅ Automatically expires PENDING bookings past expiration time
- ✅ Releases tickets back to event inventory via gRPC

### 9. **Configuration**
- ✅ Application properties with database config
- ✅ gRPC channel configuration
- ✅ Scheduling enabled with `@EnableScheduling`
- ✅ Swagger/OpenAPI documentation

---

## 🚧 **TODO - Before Testing**

### 1. **Database Setup**
```sql
-- Create the database
CREATE DATABASE bookingdb;

-- Update connection string in application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookingdb
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 2. **Start Event Service**
```bash
cd event-service
mvn spring-boot:run
```
- Event service should run on port 8082
- gRPC server should run on port 9090

### 3. **Verify ticketing-common**
```bash
cd ticketing-common
mvn clean install
```
- Ensure gRPC stubs are generated
- EventServiceProto classes should be available

### 4. **Start Booking Service**
```bash
cd booking-service
mvn spring-boot:run
```
- Should run on port 8083
- Should connect to event-service gRPC on localhost:9090

---

## 🧪 **Testing Scenarios**

### Test 1: Create Booking
```bash
curl -X POST http://localhost:8083/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "eventId": 2,
    "seatId": 100,
    "quantity": 2
  }'
```

**Expected:**
- Status 201 Created
- Booking created with status PENDING
- Expiration time set to 10 minutes from now
- Tickets reserved in event-service

### Test 2: Confirm Booking
```bash
curl -X POST http://localhost:8083/api/v1/bookings/1/confirm \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY-12345678"
  }'
```

**Expected:**
- Status 200 OK
- Booking status changed to CONFIRMED
- Expiration time cleared

### Test 3: Cancel Booking
```bash
curl -X POST http://localhost:8083/api/v1/bookings/1/cancel \
  -H "Content-Type: application/json" \
  -d '"User requested cancellation"'
```

**Expected:**
- Status 200 OK
- Booking status changed to CANCELLED
- Tickets released back to event inventory

### Test 4: Concurrent Bookings (Optimistic Locking)
```bash
# Terminal 1
curl -X POST http://localhost:8083/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"eventId":2,"seatId":100,"quantity":1}'

# Terminal 2 (immediately)
curl -X POST http://localhost:8083/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"eventId":2,"seatId":101,"quantity":1}'
```

**Expected:**
- Both should succeed if tickets available
- If only 1 ticket left, one succeeds, one fails
- No double-booking due to optimistic locking

### Test 5: Booking Expiration
```bash
# 1. Create booking
curl -X POST http://localhost:8083/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"eventId":2,"seatId":100,"quantity":1}'

# 2. Wait 11 minutes (or manually update DB: UPDATE bookings SET expires_at = NOW() - INTERVAL '1 minute')

# 3. Scheduler should automatically expire it within 1 minute
# Check logs for: "Expired X pending bookings"

# 4. Verify booking status
curl http://localhost:8083/api/v1/bookings/1
```

**Expected:**
- Booking status changed to EXPIRED
- Tickets released back to inventory

---

## 📊 **Architecture Flow**

```
┌──────────┐      HTTP       ┌──────────────────┐      gRPC       ┌──────────────┐
│  Client  │ ────────────► │ Booking Service  │ ───────────────► │ Event Service│
│          │                │  (Port 8083)     │   Reserve/       │ (Port 9090)  │
│          │                │                  │   Release        │              │
│          │                │  - BookingCtrl   │   Tickets        │ - EventGrpc  │
│          │                │  - BookingSvc    │                  │   Service    │
│          │ ◄────────────  │  - EventGrpc     │ ◄─────────────── │              │
└──────────┘      JSON      │    Client        │     Response     └──────────────┘
                            │                  │                          │
                            │  - Scheduler     │                          │
                            │    (60s)         │                          │
                            └──────────────────┘                          │
                                    │                                     │
                                    │                                     │
                                    ▼                                     ▼
                            ┌──────────────┐                     ┌──────────────┐
                            │  bookingdb   │                     │   eventdb    │
                            │              │                     │              │
                            │  Bookings    │                     │   Events     │
                            │  @Version    │                     │   @Version   │
                            └──────────────┘                     └──────────────┘
```

---

## 🎯 **Next Steps**

1. **Set up PostgreSQL database `bookingdb`**
2. **Start event-service** (must be running for gRPC calls)
3. **Test the complete flow**:
   - Create booking → Reserve tickets
   - Confirm booking → Payment success
   - Cancel booking → Release tickets
   - Wait for expiration → Scheduler releases tickets
4. **Add integration tests**
5. **Add payment gateway integration**
6. **Deploy with Docker Compose**

---

## 📚 **Key Learnings**

### **Optimistic Locking**
- Use `@Version` to prevent concurrent updates
- Retry with exponential backoff on conflicts
- Better performance than pessimistic locks

### **gRPC Communication**
- Efficient for service-to-service calls
- Type-safe with Protocol Buffers
- Handles retries and failovers

### **Scheduled Tasks**
- `@EnableScheduling` + `@Scheduled`
- Background jobs for cleanup/maintenance
- Important for releasing expired resources

### **Transaction Management**
- Use `@Transactional` for data consistency
- Rollback on exceptions
- Proper isolation levels

---

## 🔥 **Common Issues & Solutions**

### Issue 1: "gRPC channel not available"
**Solution:** Ensure event-service is running on port 9090

### Issue 2: "Database 'bookingdb' does not exist"
**Solution:** Create the database in PostgreSQL

### Issue 3: "OptimisticLockingFailureException"
**Solution:** This is expected! The retry logic handles it automatically

### Issue 4: "Booking already expired"
**Solution:** Check if scheduler is running and expiring bookings too quickly

---

## ✅ **Ready to Test!**

All code is complete. Follow the testing scenarios above to verify:
1. ✅ Booking creation
2. ✅ Ticket reservation via gRPC
3. ✅ Booking confirmation
4. ✅ Booking cancellation
5. ✅ Automatic expiration
6. ✅ Optimistic locking preventing double-booking

**Good luck with testing!** 🚀

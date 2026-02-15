# Booking Service - Optimistic Locking Implementation

## Overview
This document explains the **optimistic locking** implementation in the booking service to handle concurrent ticket purchases.

---

## 🔑 Key Concepts

### 1. **Optimistic Locking (@Version)**
```java
@Entity
public class Booking {
    @Version
    private Long version;
}
```

**How it works:**
- When you save a booking, Hibernate checks if the `version` in DB matches what you read
- If versions don't match → someone else modified it → throws `OptimisticLockingFailureException`
- This prevents two users from booking the same ticket simultaneously

### 2. **Retry Logic with Exponential Backoff**
```java
private ReserveTicketsResponse reserveTicketsWithRetry(Long eventId, int quantity) {
    int attempts = 0;
    while (attempts < MAX_RETRY_ATTEMPTS) {
        try {
            // Try to reserve tickets
            return eventGrpcClient.reserveTickets(eventId, quantity);
        } catch (OptimisticLockException) {
            attempts++;
            sleep(RETRY_DELAY_MS * attempts); // Wait longer each time
        }
    }
}
```

**Why retry?**
- If User A and User B try to book at the same time:
  - User A succeeds
  - User B gets optimistic lock failure
  - User B retries and succeeds (if tickets still available)

---

## 📊 Complete Booking Flow

### **Step 1: Create Booking**
```
POST /api/v1/bookings
{
  "userId": 1,
  "eventId": 5,
  "quantity": 2,
  "seatId": 100
}
```

**What happens:**
1. Get event details via gRPC (to calculate price)
2. Reserve tickets in event-service via gRPC ✅ **Optimistic locking here**
3. Create booking with status `PENDING`
4. Set expiration time = now + 10 minutes
5. Return booking reference

### **Step 2: Confirm Booking (After Payment)**
```
POST /api/v1/bookings/{id}/confirm
{
  "paymentId": "PAY-123456"
}
```

**What happens:**
1. Validate booking is not expired/cancelled
2. Update status to `CONFIRMED`
3. Clear expiration time
4. Return confirmed booking

### **Step 3: Scheduler Releases Expired Bookings**
```java
@Scheduled(fixedDelay = 60000) // Every 1 minute
public void releaseExpiredBookings() {
    // Find all PENDING bookings where expiresAt < now
    // Release tickets back to event via gRPC
    // Update status to EXPIRED
}
```

---

## 🏗️ Architecture

```
┌─────────────────┐         gRPC          ┌──────────────────┐
│                 │◄─────────────────────►│                  │
│ Booking Service │   Reserve/Release     │  Event Service   │
│                 │        Tickets         │  (Read-Heavy)    │
└─────────────────┘                        └──────────────────┘
        │                                           │
        │                                           │
        ▼                                           ▼
┌──────────────┐                          ┌──────────────────┐
│  bookingdb   │                          │    eventdb       │
│              │                          │   @Version on    │
│  Booking     │                          │   Event entity   │
│  @Version    │                          │                  │
└──────────────┘                          └──────────────────┘
```

---

## 🔒 Optimistic Locking in Action

### Scenario: Two users try to book last 2 tickets simultaneously

```
Time    User A                          Event DB (v1)           User B
────────────────────────────────────────────────────────────────────────
T1      Read event (v1, 2 tickets)      v1, 2 tickets          
T2                                       v1, 2 tickets          Read event (v1, 2 tickets)
T3      Reserve 1 ticket                 v1, 2 tickets          
T4      Save: v1→v2, 1 ticket ✅         v2, 1 ticket          
T5                                       v2, 1 ticket           Reserve 1 ticket
T6                                       v2, 1 ticket           Save: v1→v2 ❌ CONFLICT!
T7                                       v2, 1 ticket           RETRY: Read (v2, 1 ticket)
T8                                       v2, 1 ticket           Save: v2→v3, 0 tickets ✅
```

**Result:**
- User A: Success ✅
- User B: Retried automatically, Success ✅
- No double-booking!

---

## 🛠️ Implementation Checklist

✅ **Event Service (gRPC Server)**
- [x] `reserveTickets()` with `@Transactional`
- [x] `releaseTickets()` for expired bookings
- [x] `@Version` on Event entity
- [x] Event status validation

✅ **Booking Service (gRPC Client)**
- [x] `EventGrpcClient` with retry logic
- [x] `createBooking()` with ticket reservation
- [x] `confirmBooking()` after payment
- [x] `cancelBooking()` with ticket release
- [x] `releaseExpiredBookings()` scheduler
- [x] `@Version` on Booking entity

✅ **Exception Handling**
- [x] `OptimisticLockingFailureException` → Retry
- [x] `SeatNotAvailableException` → Return error
- [x] `BookingExpiredException` → HTTP 410 GONE
- [x] gRPC `StatusRuntimeException` → Map to HTTP

✅ **DTO & Validation**
- [x] `BookingRequest` with validations
- [x] `ConfirmBookingRequest`
- [x] `BookingResponse`

✅ **Controller Endpoints**
- [x] `POST /bookings` - Create
- [x] `POST /bookings/{id}/confirm` - Confirm
- [x] `POST /bookings/{id}/cancel` - Cancel
- [x] `GET /bookings/{id}` - Get by ID
- [x] `GET /bookings/user/{userId}` - User bookings
- [x] `GET /bookings/event/{eventId}` - Event bookings

---

## 🚀 Testing Optimistic Locking

### **Manual Test:**
```bash
# Terminal 1: User A books 1 ticket
curl -X POST http://localhost:8083/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"eventId":5,"quantity":1,"seatId":100}'

# Terminal 2: Immediately, User B books 1 ticket
curl -X POST http://localhost:8083/api/v1/bookings \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"eventId":5,"quantity":1,"seatId":101}'
```

**Expected:**
- Both succeed if tickets available
- If only 1 ticket left, one succeeds, one fails with "Insufficient tickets"
- No double-booking

---

## 📝 What You Need to Learn Next

1. **Database Configuration**
   - Create `bookingdb` PostgreSQL database
   - Update connection string in `application.properties`

2. **gRPC Server Setup**
   - Event-service: Add `@GrpcService` (already done ✅)
   - Start event-service on port 9090

3. **Scheduling**
   - Add `@EnableScheduling` to BookingServiceApplication
   - Understand `@Scheduled` annotations

4. **Integration Testing**
   - Test concurrent bookings
   - Test expiration scheduler
   - Test gRPC communication

5. **Payment Integration** (Future)
   - Connect to payment gateway
   - Handle payment callbacks
   - Update booking status on payment success/failure

---

## 🎯 Summary

**What is Optimistic Locking?**
- Assumes conflicts are rare
- Checks version before updating
- Retries if conflict detected

**Why use it?**
- Prevents double-booking
- Better than pessimistic locks (less DB load)
- Works great for high-traffic scenarios

**How does it work here?**
1. User books tickets → gRPC call to event-service
2. Event-service checks `@Version` before decreasing available tickets
3. If version mismatch → retry in booking-service
4. Booking held for 10 minutes
5. Scheduler releases expired bookings

**Next:** Set up databases and test the full flow!


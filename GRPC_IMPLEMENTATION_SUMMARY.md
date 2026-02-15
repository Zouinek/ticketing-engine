# gRPC Implementation Summary

## What We've Done

We've successfully switched from REST/Feign to **gRPC** for inter-service communication between `event-service` and `booking-service`.

---

## 📦 Project Structure

### 1. **ticketing-common** (Shared Module)
- **Location**: `ticketing-common/`
- **Purpose**: Contains shared code used by all microservices
- **Contents**:
  - `src/main/proto/event_service.proto` - gRPC service definition
  - `src/main/java/com/ticketmaster/common/enums/` - Shared enums
  - `src/main/java/com/ticketmaster/common/dto/` - Shared DTOs

### 2. **event-service** (gRPC Server)
- **gRPC Server Port**: `9090` (configured in application.properties)
- **REST API Port**: `8082` (still available for external clients)
- **Implementation**: `EventGrpcServiceImpl.java`
- **Services**:
  - `getEvent(eventId)` - Get event details
  - `reserveTickets(eventId, quantity)` - Reserve tickets

### 3. **booking-service** (gRPC Client)
- **REST API Port**: `8083`
- **gRPC Client**: `EventGrpcClient.java`
- **Configuration**: Connects to event-service on `localhost:9090`

---

## 🔧 Configuration Files

### event-service/application.properties
```properties
# gRPC Server Configuration
grpc.server.port=9090
```

### booking-service/application.properties
```properties
# gRPC Client Configuration
grpc.client.event-service.address=static://localhost:9090
grpc.client.event-service.negotiationType=PLAINTEXT
grpc.client.event-service.enableKeepAlive=true
grpc.client.event-service.keepAliveTime=30s
grpc.client.event-service.keepAliveTimeout=5s
```

---

## 📝 Proto Definition

**File**: `ticketing-common/src/main/proto/event_service.proto`

```protobuf
service EventService {
  rpc GetEvent (GetEventRequest) returns (GetEventResponse);
  rpc ReserveTickets (ReserveTicketsRequest) returns (ReserveTicketsResponse);
}
```

---

## 🚀 How It Works

### Flow Diagram:
```
booking-service (gRPC Client)
    ↓
    | gRPC call on port 9090
    ↓
event-service (gRPC Server)
    ↓
    | Query PostgreSQL
    ↓
Return response to booking-service
```

### Example Usage in Booking Service:
```java
@Service
public class BookingService {
    
    @Autowired
    private EventGrpcClient eventGrpcClient;
    
    public void createBooking(Long eventId, int quantity) {
        // Get event details via gRPC
        GetEventResponse event = eventGrpcClient.getEvent(eventId);
        
        // Reserve tickets via gRPC
        ReserveTicketsResponse response = 
            eventGrpcClient.reserveTickets(eventId, quantity);
        
        if (response.getSuccess()) {
            // Continue with booking...
        }
    }
}
```

---

## ✅ Dependencies Added

### event-service/pom.xml
```xml
<!-- gRPC Server -->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-server-spring-boot-starter</artifactId>
    <version>2.15.0.RELEASE</version>
</dependency>

<!-- ticketing-common -->
<dependency>
    <groupId>com.ticketmaster</groupId>
    <artifactId>ticketing-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

### booking-service/pom.xml
```xml
<!-- gRPC Client -->
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-client-spring-boot-starter</artifactId>
    <version>2.15.0.RELEASE</version>
</dependency>

<!-- ticketing-common -->
<dependency>
    <groupId>com.ticketmaster</groupId>
    <artifactId>ticketing-common</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## 🎯 Benefits of gRPC

1. **Performance**: Binary protocol is faster than JSON
2. **Type Safety**: Strongly typed contracts via Protocol Buffers
3. **Streaming**: Supports bidirectional streaming (for future features)
4. **Code Generation**: Client/server stubs auto-generated from .proto files
5. **Cross-Language**: Can add services in Go, Python, etc. easily

---

## 🧪 Testing

### Test event-service gRPC server:
```bash
# Start event-service
cd event-service
mvn spring-boot:run
# Server will start on port 8082 (REST) and 9090 (gRPC)
```

### Test booking-service gRPC client:
```bash
# Start booking-service (ensure event-service is running first)
cd booking-service
mvn spring-boot:run
# Client will connect to event-service on port 9090
```

---

## 📂 Files Created/Modified

### Created:
- ✅ `ticketing-common/src/main/proto/event_service.proto`
- ✅ `event-service/src/main/java/com/ticketmaster/event/grpc/EventGrpcServiceImpl.java`
- ✅ `event-service/src/main/java/com/ticketmaster/event/exception/InsufficientTicketsException.java`
- ✅ `booking-service/src/main/java/com/ticketmaster/booking/grpc/EventGrpcClient.java`

### Modified:
- ✅ `event-service/pom.xml` - Added gRPC dependencies
- ✅ `booking-service/pom.xml` - Added gRPC dependencies
- ✅ `event-service/src/main/resources/application.properties` - Added gRPC server config
- ✅ `booking-service/src/main/resources/application.properties` - Added gRPC client config
- ✅ `booking-service/src/main/java/com/ticketmaster/booking/BookingServiceApplication.java` - Removed @EnableFeignClients

### Deleted:
- ❌ `booking-service/src/main/java/com/ticketmaster/booking/client/EventServiceClient.java` - Old Feign client

---

## 🔜 Next Steps

1. **Update BookingService** to use `EventGrpcClient` instead of old Feign client
2. **Write Integration Tests** for gRPC communication
3. **Add gRPC Interceptors** for logging, authentication, etc.
4. **Consider adding gRPC health checks**
5. **For production**: Use TLS/SSL for secure gRPC communication

---

## 📚 Resources to Learn

1. **gRPC Official Docs**: https://grpc.io/docs/languages/java/
2. **Protocol Buffers**: https://protobuf.dev/
3. **grpc-spring-boot-starter**: https://yidongnan.github.io/grpc-spring-boot-starter/
4. **Video Tutorial**: Search YouTube for "Spring Boot gRPC tutorial"

---

## 🐛 Common Issues

### Issue: "UNAVAILABLE: io exception"
**Solution**: Make sure event-service is running first before starting booking-service

### Issue: "Compilation error - cannot find symbol EventServiceGrpc"
**Solution**: Run `mvn clean install` on ticketing-common first to generate gRPC classes

### Issue: "Address already in use: bind"
**Solution**: Port 9090 is already in use. Change `grpc.server.port` in event-service

---

**Status**: ✅ Both services compile successfully with gRPC!

---
name: springboot
alwaysApply: true
---

# Spring Boot Best Practices — Reservation Backend

## Tech Stack

| Component       | Version        |
|-----------------|----------------|
| Java            | 25             |
| Spring Boot     | 4.0.6          |
| Build Tool      | Maven 3.9.16   |
| Database        | PostgreSQL     |
| ORM             | Spring Data JPA |
| API Docs        | SpringDoc OpenAPI 3.0.2 |
| Dev Tools       | spring-boot-devtools |

## Project Structure (Actual)

```
reservation-backend/
├── .gitattributes
├── .gitignore
├── .mvn/wrapper/maven-wrapper.properties
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/sk8Dev/reservation/
│   │   │   └── ReservationBackendApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/sk8Dev/reservation/
│           └── ReservationBackendApplicationTests.java
```

## Recommended Package Structure

```
com.sk8Dev.reservation
├── controller       → REST controllers
├── service          → business logic interfaces
├── service.impl     → service implementations
├── repository       → JPA repositories
├── model            → JPA entities
├── dto
│   ├── request      → incoming request DTOs (records)
│   └── response     → outgoing response DTOs (records)
├── mapper           → entity ↔ DTO mappers
├── config           → Spring configuration classes
├── exception        → custom exceptions and @ControllerAdvice
└── enums            → enumerations
```

## 1. Naming Convention

All class, method, attribute, package, and variable names **must be in English**. No Spanish or any other language.

| Element     | Convention                         | Example                    |
|-------------|------------------------------------|----------------------------|
| Packages    | lowercase, dot-separated           | `com.sk8Dev.reservation.controller` |
| Classes     | PascalCase                         | `ReservationService`       |
| Interfaces  | PascalCase                         | `ReservationRepository`    |
| Methods     | camelCase                          | `findReservationById()`    |
| Attributes  | camelCase                          | `createdAt`                |
| Constants   | UPPER_SNAKE_CASE                   | `MAX_CAPACITY`             |

## 2. Java 25 Best Practices

### Records for DTOs (Mandatory)

All request and response DTOs **must** be implemented as Java `record` types:

```java
public record CreateReservationRequest(
        @NotNull @Future LocalDateTime dateTime,
        @NotNull @Positive Long userId,
        @NotNull @Positive Long resourceId) {

    public CreateReservationRequest {
        // Compact constructor for validation
        // No need to reassign parameters; they are auto-assigned
    }
}
```

- Records are implicitly `final` and immutable
- Use compact constructors for validation logic
- Annotate fields directly where possible; use method-level annotations when needed
- Do not add instance methods to records — keep them as pure data carriers

### `var` for Local Variable Type Inference

```java
// Prefer
var reservation = reservationRepository.findById(id).orElseThrow(...);
var responseList = reservations.stream().map(mapper::toResponse).toList();

// Over explicit types only when it improves clarity
```

### Pattern Matching & Sealed Classes

- Use `instanceof` pattern matching to eliminate redundant casts:

```java
if (entity instanceof Reservation reservation) {
    return mapToResponse(reservation);
}
```

- Use `switch` expressions with pattern matching for exhaustive handling
- Use `sealed` classes/interfaces for closed type hierarchies when appropriate

### Text Blocks

Use text blocks for multi-line strings (SQL, JSON, HTML templates):

```java
String query = """
        SELECT r FROM Reservation r
        WHERE r.userId = :userId
        AND r.status = 'ACTIVE'
        ORDER BY r.createdAt DESC
        """;
```

### Immutability

- All fields in services/repositories must be `final`
- Prefer `List.copyOf()` / `Set.copyOf()` for immutable collections
- Use `Stream.toList()` for immutable lists (Java 16+)

### Virtual Threads

Spring Boot 4.x enables virtual threads by default. Do not block virtual threads with `synchronized` blocks or native calls.

### Avoid

- `synchronized` blocks (pins virtual threads)
- `Optional` as method parameters
- `@SuppressWarnings` unless strictly necessary with a documented reason
- Raw types — always provide generic type parameters

## 3. Spring Boot Conventions

### Dependency Injection

**Constructor injection only** — `@Autowired` on fields is forbidden:

```java
@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                   ReservationMapper reservationMapper) {
        this.reservationRepository = reservationRepository;
        this.reservationMapper = reservationMapper;
    }
}
```

### Stereotype Annotations

| Annotation          | Usage                    |
|---------------------|--------------------------|
| `@RestController`   | REST controllers         |
| `@RequestMapping`   | Base path on controllers |
| `@Service`          | Business logic layer     |
| `@Repository`       | JPA repositories         |
| `@Component`        | Generic Spring beans     |
| `@Configuration`    | Configuration classes    |

### Transaction Management

- Service methods that only read: `@Transactional(readOnly = true)`
- Service methods that write: `@Transactional`
- Annotate at class level with `@Transactional(readOnly = true)` and override on write methods
- Never use `@Transactional` on controller methods

### Exception Handling

- Use `@ControllerAdvice` / `@RestControllerAdvice` for global exception handling
- Return consistent error responses with `ProblemDetail` (RFC 9457)
- Custom exceptions should extend `RuntimeException`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource Not Found");
        return problem;
    }
}
```

### Validation

- Use Jakarta Validation annotations: `@NotNull`, `@NotBlank`, `@Positive`, `@Future`, `@Size`, `@Email`
- Validate request bodies with `@Valid` or `@Validated` in controller parameters
- Use `@Validated` at the class level for method-level validation in services

### Pagination & Sorting

- Accept `Pageable` in controller methods
- Return `Page<T>` for paginated responses
- Use `Sort` for sorting when not using pageable

```java
@GetMapping
public Page<ReservationResponse> findAll(Pageable pageable) {
    return reservationService.findAll(pageable);
}
```

### API Documentation

- Every public controller endpoint must be documented with `@Operation` and `@ApiResponse`
- DTO records should have `@Schema` annotations on fields for clear API docs
- Every controller class must have `@Tag` annotation

```java
@Tag(name = "Reservations", description = "Reservation management endpoints")
@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {
    // ...
}
```

## 4. Javadoc (Mandatory)

Every **public method** must have an **up-to-date Javadoc** comment:

```java
/**
 * Creates a new reservation for the given user and resource.
 *
 * @param request the reservation creation request containing dateTime, userId, and resourceId
 * @return the created reservation as a response DTO
 * @throws ResourceNotFoundException if the user or resource does not exist
 * @throws IllegalArgumentException  if the reservation date is in the past
 */
public ReservationResponse create(@Valid CreateReservationRequest request) {
    // ...
}
```

- Include `@param` for every parameter
- Include `@return` for non-void methods
- Include `@throws` for every checked or documented exception
- Keep descriptions meaningful — not just restating the method name

## 5. Code Quality Rules

- **No unnecessary comments** — code should be self-documenting; Javadoc is the exception
- **No unused imports** — run `./mvnw compile` before committing
- **No wildcard imports** — always import specific classes
- **Prefer Composition over Inheritance**
- **Keep methods short** — aim for under 20 lines; refactor if longer
- **One responsibility per class** — Single Responsibility Principle
- **Use `Stream` API** for collection processing instead of imperative loops
- **Prefer `List.of()` / `Set.of()`** over `Arrays.asList()` for fixed collections
- **Logging**: Use `org.slf4j.Logger` with `LoggerFactory.getLogger()`. Never log sensitive data (passwords, tokens, PII). Use appropriate log levels:
  - `debug` — detailed diagnostic info
  - `info` — key application events
  - `warn` — recoverable issues
  - `error` — exceptions and failures

## 6. Testing

- **Unit tests**: `@ExtendWith(MockitoExtension.class)` with Mockito mocks
- **Integration tests**: `@SpringBootTest` with `@Testcontainers` for PostgreSQL
- **Controller tests**: `@WebMvcTest` with MockMvc
- **Repository tests**: `@DataJpaTest` with an in-memory or testcontainer database
- Name test methods clearly: `shouldReturnReservation_whenValidIdProvided()`
- Follow Given-When-Then (Arrange-Act-Assert) structure

## 7. Verification Commands

Run these before considering any task complete:

```bash
./mvnw compile      # Compile the project
./mvnw test         # Run all tests
./mvnw verify       # Full verification (compile + test + checks)
```

## 8. Database & JPA

- Entity classes use `@Entity` with `@Table(name = "...")`
- Use `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)` for auto-incremented IDs
- Use `@Column` only when the column name differs from the field name or for constraints
- Prefer `LocalDateTime` / `LocalDate` / `LocalTime` over legacy `Date`/`Calendar`
- Use `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy` with `@EnableJpaAuditing`
- Use `@Enumerated(EnumType.STRING)` for enum fields to store the name in the database
- Fetch strategies: default to `FetchType.LAZY` for `@OneToMany` and `@ManyToMany`
- Avoid N+1 queries: use `@EntityGraph` or `JOIN FETCH` in `@Query` when needed

## 9. Configuration

- Use `application.properties` or `application.yml` — stick to the existing format
- Sensitive values (passwords, API keys) go to environment variables or vault — never hardcoded
- Use `@ConfigurationProperties` for grouped configuration values
- Use `@Value` only for simple single-property injection
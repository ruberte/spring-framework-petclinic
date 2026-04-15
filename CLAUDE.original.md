# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Codebase Profile

**Spring Framework Petclinic** - Sample Spring MVC web app. Demo for Spring ecosystem (JPA, Data, MVC, Validation).

- **Language**: Java 17/21
- **Build**: Maven 3.8.4+
- **Framework**: Spring Framework 7.0.6, Spring Data JPA, Spring MVC
- **ORM**: Hibernate 7.3, JPA 3.2
- **Frontend**: JSP + Bootstrap 5.3 (webjars)
- **Database**: H2 (default), MySQL, PostgreSQL (switchable via Maven profiles)
- **Testing**: JUnit 5, Mockito, AssertJ
- **CI/CD**: GitHub Actions + SonarQube

## Commands

### Build & Install
```bash
./mvnw clean install              # Full build
./mvnw -DskipTests clean install  # Skip tests
./mvnw -Pcss clean install        # Compile SCSS → CSS (Bootstrap customization)
```

### Tests
```bash
./mvnw test                       # All tests, H2 profile
./mvnw -Dtest=ClinicServiceTests test
./mvnw -PMYSQL test               # Tests with MySQL profile
./mvnw -PPostgreSQL test          # Tests with PostgreSQL
./mvnw test jacoco:report         # Coverage report (target/site/jacoco/)
```

### Run Server
```bash
./mvnw jetty:run                  # Start dev server (http://localhost:8080/petclinic)
./mvnw jetty:run -PMYSQL          # With MySQL profile
```

### Code Quality
```bash
./mvnw verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.projectKey=spring-petclinic_spring-framework-petclinic \
  -Dsonar.organization=spring-petclinic
```

## Architecture

### Layers

**Model** (`src/main/java/org/springframework/samples/petclinic/model/`)
- `BaseEntity` (abstract, id field)
- `NamedEntity` (id + name)
- JPA entities: `Owner`, `Pet`, `Vet`, `Visit`, `PetType`, `Specialty`
- Validated with Hibernate Validator (`@NotEmpty`, `@NotNull`, etc.)

**Service** (`src/main/java/org/springframework/samples/petclinic/service/`)
- `ClinicService` interface
- `ClinicServiceImpl` (transactional impl)
- Mixes Spring Data JPA + raw JDBC for flexibility

**Web** (`src/main/java/org/springframework/samples/petclinic/web/`)
- Controllers: `OwnerController`, `PetController`, `VetController`, `VisitController`
- Formatters: `PetTypeFormatter` (converts PetType ID → object in forms)
- Validators: `PetValidator`

**Views** (`src/main/webapp/WEB-INF/jsp/`)
- JSP templates, Bootstrap 5.3
- JSTL for loops/conditionals

### Data Models

**Entities**:
- `Owner` ↔ `Pet` (1:N, cascade delete)
- `Pet` ↔ `Visit` (1:N, cascade delete)
- `Vet` ↔ `Specialty` (N:M via `vet_specialties` join)

**Database**: Schema auto-created from Hibernate on startup (H2 default), or use SQL scripts in `src/main/resources/db/{profile}/`.

## Code Patterns

### Entities
```java
@Entity
@Table(name = "owners")
public class Owner extends Person {
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private Set<Pet> pets;
    
    @NotEmpty
    private String address;
}
```

### Service Methods
```java
@Service
@Transactional
public class ClinicServiceImpl implements ClinicService {
    @Transactional(readOnly = true)
    public Collection<Visit> findVisits(int petId) { /* ... */ }
    
    public void saveVisit(Visit visit) { /* writes auto-committed */ }
}
```

### Controllers (Form Binding)
```java
@Controller
@RequestMapping("/owners")
public class OwnerController {
    @PostMapping("/{ownerId}/edit")
    public String processUpdateOwnerForm(
        @Valid @ModelAttribute Owner owner,
        BindingResult result) {
        if (result.hasErrors()) return "redirect:/owners/" + owner.getId();
        this.clinicService.saveOwner(owner);
        return "redirect:/owners/" + owner.getId();
    }
}
```

### Testing
- Use `@SpringBootTest` or `@DataJpaTest` for integration tests
- H2 in-memory DB for tests (default profile, fast)
- Mockito for unit tests without DB
- Test files: `*Tests.java` (not `Test.java`)

## Database Profiles

Switch DB at build time with Maven profiles:

| Profile | JDBC URL | Use Case |
|---------|----------|----------|
| **H2** (default) | `jdbc:h2:mem:petclinic` | Local dev, fast tests |
| MySQL | `jdbc:mysql://localhost:3306/petclinic` | Production-like (start MySQL first) |
| PostgreSQL | `jdbc:postgresql://localhost:5432/petclinic` | Production-like (start PostgreSQL first) |
| HSQLDB | `jdbc:hsqldb:mem:petclinic` | Alternative in-memory |

Switch: `./mvnw -PMYSQL jetty:run` or `./mvnw -PMYSQL test`

Credentials for MySQL/PostgreSQL: See `pom.xml` `<jdbc.username>`, `<jdbc.password>`.

## Common Workflows

### Add new entity property
1. Add field to entity class (e.g., `Owner.java`)
2. Add `@Column` annotation if needed
3. Add getter/setter
4. Update JSP form (e.g., `editOwnerForm.jsp`)
5. Add validator if needed (e.g., `PetValidator`)
6. Add test in `*Tests.java`
7. Run `./mvnw test` to verify

### Add new controller endpoint
1. Add method to `*Controller.java`
2. Map with `@GetMapping` or `@PostMapping`
3. Create/update JSP view in `src/main/webapp/WEB-INF/jsp/`
4. Add integration test if form-binding involved
5. Run `./mvnw jetty:run`, test manually or via `/test-runner` skill

### Debug N+1 queries
- Use `hibernate.generate_statistics=true` in `application.properties`
- Check logs for repeat queries on same entity
- Fix: Add `fetch=FetchType.LAZY` to `@OneToMany`, or use `@Query` with `JOIN FETCH`

### Run single test
```bash
./mvnw -Dtest=ClinicServiceTests#testFindOwnerByLastName test
```

## Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Dependencies, profiles, plugins, build config |
| `src/main/java/org/springframework/samples/petclinic/` | Java code (model, service, web) |
| `src/main/resources/application.properties` | Spring config (DB, logging, JPA) |
| `src/main/resources/db/` | SQL schema scripts (profile-specific) |
| `src/main/webapp/WEB-INF/jsp/` | JSP templates |
| `src/test/java/` | Tests |
| `.github/workflows/` | CI/CD (GitHub Actions) |

## Notes for Claude

1. **Transactional semantics**: Always mark writes with `@Transactional`. Reads can be `@Transactional(readOnly=true)` for optimization.

2. **Validation**: Use Hibernate Validator (`@NotEmpty`, `@Pattern`, etc.) on entities. Frontend forms rely on `BindingResult` in controllers.

3. **Cascade rules**: `cascade = CascadeType.ALL` on `@OneToMany` means delete Owner → delete Pets. Verify intent before changing.

4. **JSP forms**: Use Spring's `<form:form>` tag and `<spring:bind>` for proper form binding & error display. Avoid raw `<form>` tags.

5. **Lazy loading pitfall**: `@OneToMany` is lazy by default. Accessing `owner.getPets()` outside transaction context throws `LazyInitializationException`. Use `JOIN FETCH` in queries or `FetchType.EAGER` if always needed.

6. **Test isolation**: Tests run against H2 in-memory. Ensure each test is independent (no shared state between `@Test` methods).

7. **SonarQube integration**: CI/CD runs SonarQube on main. Fix high-priority issues before merging.

## Skills & Agents

- **`/test-runner`**: Run Maven tests with DB profiles. E.g., `/test-runner ClinicServiceTests MySQL`
- **`spring-conventions` (Claude-only)**: Background knowledge of Spring patterns & Petclinic architecture
- **`spring-db-reviewer` (subagent)**: Triggered on model/service/test edits. Detects N+1, SQL injection, missing `@Transactional`, etc.

## Quick Lint

- No `.env` files with DB creds (use profiles)
- Never edit `pom.xml` lock files manually
- Tests should not modify production DB
- Check `POST` endpoints have CSRF protection (Spring default)

---
name: spring-conventions
description: Spring Framework + JPA + Maven patterns para Petclinic
disable-model-invocation: true
user-invocable: false
---

# Spring Petclinic Conventions

## Capas arquitectura

**Model** (`src/main/java/org/springframework/samples/petclinic/model/`)
- `BaseEntity` - id, abstracta
- `NamedEntity` - id + name
- JPA entities: `Owner`, `Pet`, `Vet`, `Visit`, `PetType`, `Specialty`
- Validadores: `@NotEmpty`, `@NotNull` (Hibernate Validator)

**Repository** (implícito Spring Data JPA)
- `OwnerRepository`, `PetRepository`, etc. extienden `CrudRepository`
- En `ClinicService` interface, `ClinicServiceImpl` impl.

**Service** (`src/main/java/org/springframework/samples/petclinic/service/`)
- `ClinicService` interface + `ClinicServiceImpl`
- Transactional methods, JPA + JDBC fallback

**Web** (`src/main/java/org/springframework/samples/petclinic/web/`)
- Controllers: `OwnerController`, `PetController`, `VetController`, `VisitController`
- Formatters: `PetTypeFormatter`
- Validators: `PetValidator`

**Views** (JSP)
- `src/main/webapp/WEB-INF/jsp/`
- Bootstrap 5.3 via webjars

## Convenciones código

### Entity patterns
```java
@Entity
@Table(name = "owners")
public class Owner extends Person {
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private Set<Pet> pets;
    // ...
}
```

### Service patterns
```java
@Service
@Transactional
public class ClinicServiceImpl implements ClinicService {
    @Transactional(readOnly = true)
    public Collection<Pet> findPetsByOwner(int ownerId) { ... }
}
```

### Controller patterns
```java
@Controller
@RequestMapping("/owners")
public class OwnerController {
    @PostMapping("/{ownerId}/edit")
    public String processUpdateOwnerForm(/* ... */) { ... }
}
```

## Testing

- Test files: `*Tests.java` (no `Test.java`)
- JUnit 5, AssertJ, Mockito
- Profile: H2 in-memory default
- Ubicación: `src/test/java/org/springframework/samples/petclinic/`

## Database

Profiles en `pom.xml`:
- **H2**: `jdbc:h2:mem:petclinic` (default)
- **MySQL**: `jdbc:mysql://localhost:3306/petclinic`
- **PostgreSQL**: `jdbc:postgresql://localhost:5432/petclinic`

SQL scripts: `src/main/resources/db/{h2,mysql,postgresql}/`

## Build

```bash
./mvnw clean install              # Full build
./mvnw -DskipTests clean install  # Skip tests
./mvnw -Pcss clean install       # Compile SCSS → CSS
./mvnw -PMYSQL test               # Tests con MySQL
```

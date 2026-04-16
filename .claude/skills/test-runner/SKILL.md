---
name: test-runner
description: Run Maven tests con profiles DB (H2/MySQL/PostgreSQL)
disable-model-invocation: false
user-invocable: true
---

# Maven Test Runner

Ejecuta tests con profiles de DB específicos.

## Invocaciones

`/test-runner` → todos tests profile H2 default
`/test-runner [TestClass]` → test específico, H2
`/test-runner [TestClass] [profile]` → test + profile
`/test-runner all [profile]` → todos tests con profile

## Ejemplos

```
/test-runner ClinicServiceTests
/test-runner ClinicServiceTests MySQL
/test-runner all PostgreSQL
```

## Profiles disponibles

- **H2** (default, in-memory) - rápido, local
- **MySQL** - requiere `jdbc:mysql://localhost:3306/petclinic`
- **PostgreSQL** - requiere `jdbc:postgresql://localhost:5432/petclinic`
- **HSQLDB** - in-memory alternativo

## Output

- Test results con JUnit 5 output
- Fallos detallados + stack trace
- Cobertura JaCoCo si aplica

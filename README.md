# Class Go Backend

Backend MVP para `Class Go`, construido con Spring Boot 3, JPA, Flyway, JWT y una arquitectura modular limpia.

## Estado actual

La base entregada incluye:

- Autenticacion local con `register`, `login`, `refresh` y `me`
- Estructura preparada para login Google
- Seguridad JWT con roles `TEACHER` y `PARENT`
- Gestion de aulas y registro de alumnos por codigo
- Vinculacion de alumnos por `student_code`
- Catalogo de contenido (`subjects`, `topics`, `questions`)
- Creacion y publicacion de challenges
- Sesiones de juego, heartbeat, respuestas y cierre de sesion
- Progreso, leaderboard y resultados semanales
- Scheduler para cierre automatico de challenges
- Upload de avatar con metadata persistida
- Migraciones Flyway y seed inicial
- Manejo global de excepciones

## Arquitectura

Paquetes principales:

- `api`: controladores y DTOs
- `application`: casos de uso y servicios
- `domain`: entidades, enums y repositorios
- `infrastructure`: seguridad, storage y email
- `shared`: errores y respuestas comunes

## Integraciones externas

Para mantener el MVP compilable sin credenciales reales:

- Email usa `LoggingEmailGateway` como stub
- Storage usa `LocalUrlStorageGateway` para simular R2 con URL publica
- Google login queda desacoplado y condicionado por `app.google.enabled`

Estas piezas se pueden reemplazar por implementaciones reales de AWS SES, Cloudflare R2 y validacion de Google ID token sin alterar los controladores ni los servicios de negocio.

## Configuracion

Archivo principal: `src/main/resources/application.yml`

Variables utiles:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `CORS_ALLOWED_ORIGINS`
- `GOOGLE_AUTH_ENABLED`
- `R2_PUBLIC_BASE_URL`
- `R2_BUCKET`
- `R2_ENDPOINT`
- `EMAIL_ENABLED`
- `AWS_REGION`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_SES_FROM_EMAIL`

## Ejecutar

```bash
mvn spring-boot:run
```

## Verificacion realizada

Se valido en este workspace con:

```bash
mvn -q -DskipTests compile
mvn test
```

## Nota sobre Java

La especificacion original pide Java 21, pero este workspace tiene Java 17 instalado. Por eso el `pom.xml` quedo en `17` para asegurar compilacion y pruebas aqui. Si tu entorno de despliegue ya usa Java 21, puedes volver a fijar `java.version=21`.

# 📘 Study Platform (`javaZadanie`)

Tento repozitár obsahuje **plnohodnotnú semestrálnu aplikáciu** zloženú z:

- 🔙 **backendu** v Spring Boot (REST API, autentifikácia cez JWT, SQLite databáza),
- 🖥 **desktop klienta** v JavaFX (login, dashboard, detail skupiny a úloh, štatistiky, logy, správa používateľov),
- 🔔 jednoduchého **WebSocket** mechanizmu na oznámenie zmien úloh.

Projekt je navrhnutý ako systém na správu **študijných skupín, úloh, odovzdaní, komentárov a zdrojov**.


---

## 🧱 Použité technológie

**Backend**

- Java **21**
- **Spring Boot 2.7.18**
  - spring-boot-starter-web
  - spring-boot-starter-data-jpa
  - spring-boot-starter-security
  - spring-boot-starter-validation
- **SQLite** (jdbc + `sqlite-dialect`)
- **JPA/Hibernate**
- **JWT** (knižnica `io.jsonwebtoken:jjwt-*`)
- **SpringDoc OpenAPI** (`springdoc-openapi-ui`) – Swagger UI

**Desktop klient**

- **JavaFX 21.0.1**
  - `javafx-controls`
  - `javafx-fxml`

Build tool: **Maven** (pozri `pom.xml`).

---

## 📂 Štruktúra projektu

Hlavný projektový adresár: `javaZadanie/`

Dôležité časti:

```text
src/
  main/
    java/
      com/markovic/javazadanie/
        JavaZadanieApplication.java   # Spring Boot backend
        config/                       # security, OpenAPI, WebSocket nastavenie
        controller/                   # REST API controllery
        dto/                          # DTO triedy pre API a štatistiky
        model/                        # JPA entity (User, StudyGroup, Task, ...)
        repository/                   # Spring Data JPA repozitáre
        security/                     # JwtAuthFilter, JwtUtil, UserDetailsService
        service/                      # biznis logika (TaskService, StudyGroupService, ...)
        websocket/                    # TaskWebSocketHandler (broadcast zmien)
        fx/                           # JavaFX klient (kontroléry + API klienti)

    resources/
      application.properties          # konfigurácia SQLite
      fxml/                           # JavaFX view-y (login, dashboard, ...)
      static/ / templates/            # nevyužité (typicky pre web UI)

pom.xml                               # Maven konfigurácia
```

---

## 🗄 Databáza

### Konfigurácia

V súbore `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:sqlite:javaZadanie.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.sqlite.hibernate.dialect.SQLiteDialect
```

Pri štarte aplikácie sa v koreňovom adresári vytvorí súbor `javaZadanie.db` so schémou podľa JPA entít.

### Hlavné entity

`src/main/java/com/markovic/javazadanie/model/`

- `User` – používateľ (meno, email, heslo, dátum vytvorenia)
- `StudyGroup` – študijná skupina (názov, popis, createdAt)
- `Membership` – členstvo používateľa v skupine + rola
- `Task` – úloha (názov, popis, stav `TaskStatus`, zadávateľ, priradený používateľ, deadline)
- `TaskStatus` – enum stavu úlohy (napr. OPEN, SUBMITTED, CLOSED)
- `TaskSubmission` – odovzdanie úlohy (autor, odkaz na riešenie, čas odovzdania)
- `Comment` – komentár k úlohe
- `Resource` – zdroj/príloha (napr. link, súbor, typ)
- `ActivityLog` – log udalostí (kto čo spravil, nad čím, kedy)
- `ActivityAction` – enum akcií (TASK_CREATED, USER_REGISTERED, SUBMISSION_CREATED, ...)

---

## 🌐 REST API – prehľad

Všetky controllery sú pod balíčkom  
`com.markovic.javazadanie.controller`.

Aplikácia používa **JWT Bearer token**.  
Neautentifikované endpointy (whitelist) sú v `SecurityConfig` – typicky:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`

Ostatné endpointy vyžadujú hlavičku:

```http
Authorization: Bearer <JWT_TOKEN>
```

### 🔐 AuthController – `/api/auth`

- `POST /api/auth/register`  
  Registrácia nového používateľa.  
  Telo: meno, email, heslo (min. 6 znakov).  
  Vracia vytvoreného používateľa + záznam v ActivityLog.

- `POST /api/auth/login`  
  Prihlásenie používateľa.  
  Telo: email + heslo.  
  Odpoveď (`LoginResponseDto`):
  - `token` – JWT token
  - informácie o používateľovi (id, meno, email)

---

### 👤 UserController – `/api/users`

- `POST /api/users`  
  Vytvorenie používateľa (administratívne).

- `GET /api/users`  
  Zoznam všetkých používateľov.

- `GET /api/users/{id}`  
  Detail konkrétneho používateľa.

- `GET /api/users/me`  
  Detail aktuálne prihláseného používateľa (odvodené z JWT).

- `PUT /api/users/{id}`  
  Úprava používateľa (meno, email, atď.).

- `PUT /api/users/me`  
  Úprava profilu prihláseného používateľa.

- `DELETE /api/users/{id}`  
  Vymazanie používateľa.

---

### 👥 StudyGroupController – `/api/groups`

- `POST /api/groups`  
  Vytvorenie novej študijnej skupiny.

- `GET /api/groups`  
  Zoznam všetkých skupín.

- `GET /api/groups/{id}`  
  Detail konkrétnej skupiny.

- `PUT /api/groups/{id}`  
  Úprava skupiny (názov, popis).

- `DELETE /api/groups/{id}`  
  Vymazanie skupiny.

- `GET /api/groups/{groupId}/members`  
  Zoznam členov skupiny (`Membership` + info o používateľovi).

- `POST /api/groups/{groupId}/members/{userId}`  
  Pridanie člena do skupiny.

- `DELETE /api/groups/{groupId}/members/{userId}`  
  Odstránenie člena zo skupiny.

- `POST /api/groups/{groupId}/join`  
  Aktuálne prihlásený používateľ sa pridá do skupiny.

- `POST /api/groups/{groupId}/leave`  
  Aktuálne prihlásený používateľ opustí skupinu.

---

### 🧩 MembershipController – `/api/memberships`

- `POST /api/memberships`  
  Pridanie člena do skupiny podľa `groupId`, `userId`, `role`.

- `GET /api/memberships`  
  Zoznam všetkých členstiev.

- `GET /api/memberships/{id}`  
  Detail konkrétneho členstva.

- `GET /api/memberships/group/{groupId}`  
  Členstvá v konkrétnej skupine.

- `GET /api/memberships/user/{userId}`  
  Skupiny, v ktorých je používateľ členom.

- `PUT /api/memberships/{id}/role`  
  Zmena roly člena v skupine.

- `DELETE /api/memberships/{id}`  
  Vymazanie členstva.

---

### ✅ TaskController – `/api/tasks`

- `POST /api/tasks`  
  Vytvorenie novej úlohy.  
  Telo (`CreateTaskRequest`): `groupId`, `title`, `description`, `status`, `deadline`.  
  Autor sa berie z JWT (aktívny používateľ).

- `GET /api/tasks`  
  Zoznam všetkých úloh.

- `GET /api/tasks/{id}`  
  Detail úlohy.

- `GET /api/tasks/group/{groupId}`  
  Zoznam úloh v danej skupine.

- `PUT /api/tasks/{taskId}`  
  Úprava úlohy (`UpdateTaskRequest`: názov, popis, stav, deadline).

- `DELETE /api/tasks/{id}`  
  Vymazanie úlohy.  
  Po vymazaní sa pošle WebSocket broadcast.

---

### 📎 TaskSubmissionController – `/api/tasks/{taskId}/submissions`

- `POST /api/tasks/{taskId}/submissions`  
  Vytvorenie odovzdania pre danú úlohu. Autor sa odvodí z JWT.

- `GET /api/tasks/{taskId}/submissions`  
  Všetky odovzdania k danej úlohe.

- `GET /api/tasks/{taskId}/submissions/my`  
  Odovzdania aktuálne prihláseného používateľa pre danú úlohu.

---

### 💬 CommentController – `/api/comments`

- `POST /api/comments`  
  Vytvorenie komentára k úlohe.

- `GET /api/comments`  
  Zoznam všetkých komentárov.

- `GET /api/comments/{id}`  
  Detail komentára.

- `GET /api/comments/task/{taskId}`  
  Komentáre k jednej úlohe.

- `DELETE /api/comments/{id}`  
  Vymazanie komentára.

---

### 📚 ResourceController – `/api/resources`

- `POST /api/resources`  
  Vytvorenie zdroja (napr. odkaz na dokument) viazaného na skupinu.

- `GET /api/resources`  
  Zoznam všetkých zdrojov.

- `GET /api/resources/{id}`  
  Detail zdroja.

- `GET /api/resources/group/{groupId}`  
  Zdroje priradené ku konkrétnej skupine.

- `PUT /api/resources/{id}`  
  Úprava zdroja.

- `DELETE /api/resources/{id}`  
  Vymazanie zdroja.

---

### 📊 StatsController – `/api/stats`

- `GET /api/stats/groups/{groupId}` → `GroupStatsDto`  

Obsahuje:

- `groupId`, `groupName`
- `totalTasks`, `openTasks`, `submittedTasks`, `closedTasks`
- `completionRate` v %
- `memberSubmissions` – mapa `email -> počet odovzdaní`
- `activityPerDay` – mapa `date (String) -> počet udalostí`

Dáta sú počítané z `TaskRepository`, `TaskSubmissionRepository` a `ActivityLogRepository`.

---

### 📜 ActivityLogController – `/api/activity-log`

- `GET /api/activity-log`  
  Zoznam všetkých logov.

- `GET /api/activity-log/user/{userId}`  
  Logy konkrétneho používateľa.

---

## 🔔 WebSocket

Backend:

- `TaskWebSocketHandler` (balíček `websocket`) registruje všetkých pripojených klientov a umožňuje **broadcast textových správ**.
- `TaskService` pri vytvorení a vymazaní úlohy volá `taskWebSocketHandler.broadcast(...)`  
  – napr. `"NEW_TASK_CREATED: <title>"`.

Konfigurácia:

- `WebSocketConfig` registruje endpoint `/ws` a povolené originy.

Frontend:

- `TaskWebSocketClient` v balíčku `fx` sa pripája na `ws://localhost:8080/ws` a pri príchode správ vie zavolať callback na obnovenie dát (napr. refresh tabuľky úloh v `GroupDetailController`).

---

## 🖥 JavaFX UI

FXML view-y v `src/main/resources/fxml/`:

- `login.fxml` – prihlasovacie okno (email + heslo)
- `dashboard.fxml` – hlavný dashboard
  - zoznam študijných skupín
  - zoznam úloh
  - akcie (vytvoriť úlohu, otvoriť detail skupiny, prejsť na logy, štatistiky, používateľov)
- `group_detail.fxml` – detail skupiny
  - informácie o skupine
  - členovia skupiny
  - úlohy v skupine
  - tlačidlá na otvorenie detailu úlohy, štatistík skupiny, atď.
- `task_detail.fxml` – detail úlohy
  - informácie o úlohe
  - komentáre
  - odovzdania (submissions)
- `stats.fxml` – štatistiky skupiny
  - JavaFX `PieChart`, `BarChart`, `LineChart` z dát `/api/stats/groups/{id}`
- `logs.fxml` – prehľad `ActivityLog`
- `users.fxml` – zoznam používateľov / správa používateľov

### Spustenie JavaFX klienta

V balíčku `fx` sú dve dôležité triedy:

- `FXLauncher` – spúšťa **Spring Boot backend** a následne **JavaFX**:
  ```java
  public class FXLauncher {
      public static void main(String[] args) {
          SpringApplication.run(JavaZadanieApplication.class, args);
          Application.launch(MainApp.class, args);
      }
  }
  ```
- `MainApp` – štartuje JavaFX a načíta `login.fxml`.

Najjednoduchší spôsob spustenia:

1. V IDE (IntelliJ IDEA) otvoriť projekt ako Maven projekt.
2. Spustiť `FXLauncher.main()`.

Alebo
použiť .bat súbor v roote projektu (vyžaduje Docker)

Backend a desktop klient sa spustia v jednom JVM procese.

---

## ▶️ Spustenie z príkazového riadku (alternatíva)

> Poznámka: kvôli JavaFX je stále pohodlnejšie spúšťať projekt z IDE.

1. Uisti sa, že máš JDK 21 a Maven.
2. V koreňovom adresári (`javaZadanie/`) spusti:
   ```bash
   mvn clean package
   ```
3. Backend:
   ```bash
   mvn spring-boot:run
   ```
4. JavaFX klient:
   - buď `mvn javafx:run` (plugin má `mainClass=com.markovic.javazadanie.fx.MainApp`)
   - alebo spusti `MainApp` / `FXLauncher` z IDE.

---

## 📑 API dokumentácia (Swagger / OpenAPI)

Vďaka `OpenApiConfig` je k dispozícii OpenAPI definícia a Swagger UI (po štarte backendu):

- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## ⚠️ Známe obmedzenia / TODO

- WebSocket integrácia je jednoduchá – odosiela len textové správy; protokol by sa dal rozšíriť (napr. JSON s typom udalosti).
- Neexistujú roly administrátor / študent na úrovni Spring Security (prístup je zatiaľ skôr „flat“).
- Niektoré validačné a chybové hlášky sú generické.
- Upload súborov pre `Resource` je zatiaľ riešený len textovým poľom `pathOrUrl` (bez reálneho multipart uploadu).

---

## 📄 Licencia

Repozitár zatiaľ neobsahuje explicitnú licenciu – projekt je predpokladaný ako **študentská semestrálna práca**.

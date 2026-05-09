# Zenith — Gaming Platform

A JavaFX desktop gaming platform with real-time chat, AI game recommendations, a chatbot powered by a local LLM, friend/DM system, leaderboard, admin panel, and payment flow.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [External Services Setup](#external-services-setup)
   - [SQL Server](#1-sql-server)
   - [Ollama (AI Chatbot)](#2-ollama-ai-chatbot)
   - [RAWG API Key](#3-rawg-api-key)
3. [Database Setup](#database-setup)
4. [Clone & Build](#clone--build)
5. [Run the Application](#run-the-application)
6. [Default Credentials](#default-credentials)
7. [Project Structure](#project-structure)
8. [Configuration Reference](#configuration-reference)
9. [Troubleshooting](#troubleshooting)
10. [Contributing](#contributing)

---

## Prerequisites

| Tool | Version | Download |
|------|---------|----------|
| JDK | 21 or higher | [Adoptium](https://adoptium.net) / [Oracle](https://www.oracle.com/java/technologies/downloads/) |
| Git | any | [git-scm.com](https://git-scm.com/) |
| SQL Server | 2019 or higher | [Microsoft](https://www.microsoft.com/en-us/sql-server/sql-server-downloads) (Developer edition is free) |
| SSMS | any | [Download SSMS](https://learn.microsoft.com/en-us/sql/ssms/download-sql-server-management-studio-ssms) |
| Ollama | latest | [ollama.com](https://ollama.com/) |

> Maven is **not** required globally — the project includes a Maven Wrapper (`mvnw` / `mvnw.cmd`).

Verify your Java installation:
```bash
java -version
javac -version
```
Both must show version 21 or higher.

---

## External Services Setup

### 1. SQL Server

The app connects to a local SQL Server instance using these hardcoded credentials:

| Setting | Value |
|---------|-------|
| Host | `localhost:1433` |
| Database | `db9` |
| Username | `sa` |
| Password | `hassan1234@` |

**Steps:**

1. Install SQL Server (Developer edition is free).
2. Open **SQL Server Configuration Manager** → enable **TCP/IP** on port `1433`.
3. Open **SSMS**, connect with the `sa` login (or any sysadmin account), and verify login is enabled:
   ```sql
   ALTER LOGIN sa ENABLE;
   ALTER LOGIN sa WITH PASSWORD = 'hassan1234@';
   ```
4. Make sure **SQL Server Authentication** is enabled (right-click server → Properties → Security → SQL Server and Windows Authentication mode).

> If you want to use different credentials, edit `sampler/src/main/java/atlantafx/sampler/zenith/database/ConnectionDB.java` before building.

---

### 2. Ollama (AI Chatbot)

The chatbot streams responses from a **local** Ollama instance — no internet required for the AI.

**Install and start Ollama:**

```bash
# 1. Download and install from https://ollama.com/

# 2. Pull the model used by the app
ollama pull llama3.2

# 3. Ollama starts automatically as a background service.
#    Verify it is running:
ollama ps
```

The app connects to `http://localhost:11434`. If Ollama is not running, the chatbot will show an error message but the rest of the app works fine.

**Speed tip — GPU vs CPU:**

| Hardware | Typical response time |
|----------|-----------------------|
| NVIDIA GPU (CUDA) | 2–5 seconds |
| Apple Silicon (Metal) | 3–8 seconds |
| CPU only | 30 seconds – 2+ minutes |

If you are on CPU only, switch to the smaller 1B model for much faster responses:

```bash
ollama pull llama3.2:1b
```

Then change line 25 in `AnthropicChatService.java`:
```java
private static final String MODEL = "llama3.2:1b";
```

---

### 3. RAWG API Key

The home page AI suggestions and game browsing pull live data from [RAWG.io](https://rawg.io/). The API key is already embedded in the source (`ZenithChatBot.java`). The free tier allows 20,000 requests/month, which is more than enough for development.

If the key ever stops working, register for a free key at [rawg.io/apidocs](https://rawg.io/apidocs) and replace the value of `RAWG_API_KEY` in `ZenithChatBot.java`.

---

## Database Setup

Run the SQL scripts **in order** in SSMS. Connect to your SQL Server instance first.

### Step 1 — Main schema (required)

Open `sampler/src/main/resources/zenith/sql/schema.sql` in SSMS and execute it.

This creates:
- `db9` database
- `Users`, `Jeux`, `UsersJeux`, `Messages`, `Avis`, `ChatHistory` tables
- Seed data: 6 games, 1 admin account, 2 player accounts

### Step 2 — Community & Friends (required)

Open `sampler/src/main/resources/zenith/sql/community_schema.sql` and execute it.

This adds:
- `CommunityMessages` table (global chat)
- `Amis` table (friend requests / accepted friendships)

### Step 3 — Admin ban feature (optional)

Open `sampler/src/main/resources/zenith/sql/admin_migration.sql` and execute it.

This adds the `BannedUsers` table. Without it, the ban button in the admin panel will show a disabled notice.

> All three scripts are idempotent — running them twice will not break anything (Step 3 uses `IF NOT EXISTS`).

---

## Clone & Build

```bash
git clone https://github.com/Hassanezz11/zenith.git
cd zenith
```

**Windows:**
```bash
mvnw.cmd clean compile -pl sampler -am
```

**macOS / Linux:**
```bash
./mvnw clean compile -pl sampler -am
```

The first build downloads all Maven dependencies (~2–3 minutes). Subsequent builds are fast.

---

## Run the Application

**Windows:**
```bash
mvnw.cmd -pl sampler javafx:run
```

**macOS / Linux:**
```bash
./mvnw -pl sampler javafx:run
```

**From IntelliJ IDEA:**
1. Open the project root (`File → Open`).
2. Set Project SDK to JDK 21+ (`File → Project Structure → Project`).
3. Navigate to `sampler/src/main/java/atlantafx/sampler/zenith/ZenithApp.java`.
4. Click the green Run arrow next to `main()`.

**From VS Code:**
1. Install the **Extension Pack for Java**.
2. Open the project folder.
3. Open `ZenithApp.java` and click **Run** above `main()`.

---

## Default Credentials

These accounts are created by `schema.sql`:

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@zenith.com` | `admin1234` |
| Player | `nova@zenith.com` | `pass1234` |
| Player | `ghost@zenith.com` | `pass1234` |

The admin account unlocks the **Admin Panel** in the sidebar (shield icon).

---

## Project Structure

```
zenith/
├── base/                         Core AtlantaFX theme library
├── decorations/                  Window decoration styles
├── styles/                       SCSS source for themes
├── sampler/                      Main Zenith application
│   ├── libs/
│   │   └── mssql-jdbc-13.4.0.jre11.jar   SQL Server JDBC driver (bundled)
│   └── src/main/
│       ├── java/atlantafx/sampler/zenith/
│       │   ├── ZenithApp.java              Entry point
│       │   ├── MainController.java         Root layout + navigation
│       │   ├── HomeController.java         Home page + AI suggestions
│       │   ├── ChatBotPopupController.java Chatbot UI (streaming)
│       │   ├── AnthropicChatService.java   Ollama HTTP client
│       │   ├── ZenithChatBot.java          Game recommendation logic
│       │   ├── ZenithStore.java            Singleton app state
│       │   ├── RawgApiService.java         RAWG.io REST client
│       │   ├── Session.java                Logged-in user session
│       │   └── database/
│       │       └── ConnectionDB.java       JDBC connection (edit credentials here)
│       └── resources/zenith/
│           ├── fxml/                       UI layouts
│           └── sql/                        Database scripts
├── mvnw / mvnw.cmd               Maven Wrapper (no Maven install needed)
└── pom.xml                       Parent POM
```

---

## Configuration Reference

All values that a new developer might need to change are in one place each:

| What | File | Field |
|------|------|-------|
| DB host / port | `database/ConnectionDB.java` | `URL` |
| DB name | `database/ConnectionDB.java` | `URL` (`databaseName=db9`) |
| DB username | `database/ConnectionDB.java` | `USER` |
| DB password | `database/ConnectionDB.java` | `PSWD` |
| Ollama URL | `AnthropicChatService.java` | `OLLAMA_URL` |
| LLM model name | `AnthropicChatService.java` | `MODEL` |
| RAWG API key | `ZenithChatBot.java` | `RAWG_API_KEY` |

---

## Troubleshooting

### App starts but shows no games
SQL Server is not reachable. Check:
1. SQL Server service is running (`services.msc` on Windows).
2. TCP/IP is enabled on port 1433 (SQL Server Configuration Manager).
3. `sa` login is enabled and the password matches `ConnectionDB.java`.
4. You ran `schema.sql` in SSMS.

### Chatbot shows "Ollama is not running"
Start Ollama and make sure the model is downloaded:
```bash
ollama pull llama3.2
ollama serve        # if not running as a background service
```

### AI suggestions on Home don't load
The RAWG.io API is unreachable. Check your internet connection. If it persists, the API key may be rate-limited — get a free key from [rawg.io/apidocs](https://rawg.io/apidocs) and update `ZenithChatBot.java`.

### Build fails — "Module Not Found" or dependency errors
```bash
# Windows
mvnw.cmd clean compile -pl sampler -am -U

# macOS/Linux
./mvnw clean compile -pl sampler -am -U
```
The `-U` flag forces Maven to re-download missing dependencies.

### Build fails — Java version error
The project requires JDK 21+. Check:
```bash
java -version
```
If it shows a lower version, set `JAVA_HOME`:

**Windows:**
```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.x.x"
```

**macOS/Linux:**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Ban button in Admin Panel is disabled
Run `admin_migration.sql` in SSMS to create the `BannedUsers` table.

### IntelliJ — red errors everywhere after import
1. Right-click `pom.xml` → **Add as Maven Project**.
2. Wait for indexing to finish.
3. `File → Project Structure → Project` → set SDK to JDK 21.

---

## Contributing

```bash
# Create a feature branch
git checkout -b feature/your-feature-name

# Make changes, then commit
git add .
git commit -m "Short description of what and why"

# Push and open a Pull Request
git push -u origin feature/your-feature-name
```

Please test that the app compiles and runs before opening a PR:
```bash
# Windows
mvnw.cmd clean compile -pl sampler -am

# macOS/Linux
./mvnw clean compile -pl sampler -am
```
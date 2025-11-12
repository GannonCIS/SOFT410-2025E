# employer-worker-registration-system
An accounting program that contains employee and employer information and records of relationships between them.

---

# v1 branch
## Screenshot
<p align="center"><strong>Login</strong></p>
<p align="center"><img src="https://user-images.githubusercontent.com/71611710/157845415-c8f293df-5e1a-4ac5-a066-1971ee3ab6ae.png"></p>

| **Homepage**            | **Employer registration**|  **Worker registration**
:------------------------:|:------------------------:|:-------------------------:
![2-home_page](https://user-images.githubusercontent.com/71611710/157845986-0b99502d-ec6a-411c-999c-d37859dcf47e.png) | ![3-new_employer](https://user-images.githubusercontent.com/71611710/157849241-2a4ea23f-f195-4152-ab57-b2da20a1ea87.png)  |  ![3-new_worker](https://user-images.githubusercontent.com/71611710/157849850-5c6cfda1-05cd-4164-8287-474496cd189e.png)

| **Search Box**  | **Registration document**
:----------------:|:-------------------------:
![5-view_worker](https://user-images.githubusercontent.com/71611710/157850829-c03944a1-bd1b-41d6-875b-61f8d8ce4d62.png) | ![7-new_record_optionpane](https://user-images.githubusercontent.com/71611710/158039292-30c103d1-bdaa-4f3f-bd36-342815fd6efd.png)

---

## Quick Start

**Fastest way to run the application:**

1. Double-click `run-app.bat` in the project folder

**Or use command line:**
```cmd
run-app.bat
```

**Or use Gradle directly:**
```powershell
.\gradle-run.bat run
```

**Or in VS Code:**
- Press `F5` or click "Run" → "Run Main (Employer-Worker System)"

---

## Requirements
Postgresql is used in this program. You can find the necessary jar file for postgresql java connection here:

> https://jdbc.postgresql.org/download.html

Or you can use a different database but for this to work, change:
```

---

## Java runtime

This project is configured to use **Java 23**.

**Set JAVA_HOME permanently (recommended):**

1. Open **Settings** → **System** → **About** → **Advanced system settings** → **Environment Variables**
2. Under **System variables**, create/edit `JAVA_HOME`:
   ```
   C:\Program Files\Java\jdk-23
   ```
3. Edit the `Path` variable and ensure it includes:
   ```
   %JAVA_HOME%\bin
   ```
4. Open a **new PowerShell** window and verify:
   ```powershell
   echo $env:JAVA_HOME
   java -version
   ```

**Or set for current session only:**
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
```

---
DriverManager.getConnection("jdbc:database://host:port/database-name", "user-name", "password");
```
for postgresql:
```
DriverManager.getConnection("jdbc:postgresql://localhost:5432/db", "postgres", "password");
```
---

**And finally, in order not to get a database error, you should add the following tables to the database:**
```
CREATE TABLE admin(id smallserial primary key not null, username varchar, password varchar);
CREATE TABLE employer(employer_id serial primary key not null, name varchar not null, surname varchar not null, business varchar, phonenumber varchar);
CREATE TABLE worker(worker_id serial primary key not null, name varchar not null, surname varchar not null, phone_number varchar);
CREATE TABLE worker_record(worker_record_id serial primary key not null, worker_id integer references worker(worker_id), employer_id integer references employer(employer_id), date varchar(10) not null, wage smallint not null);
CREATE TABLE employer_record(employer_record_id serial primary key not null, employer_id integer references employer(employer_id), date varchar(10) not null, note varchar(255), number_worker smallint not null, wage smallint not null);
CREATE TABLE worker_payment(worker_payment_id serial primary key not null, worker_id integer references worker(worker_id), employer_id integer references employer(employer_id), date varchar(10) not null, paid integer not null);
CREATE TABLE employer_payment(employer_payment_id serial primary key not null, employer_id integer references employer(employer_id), date varchar(10) not null, paid integer not null);
CREATE TABLE worker_payment(worker_payment_id serial primary key not null, worker_id integer references worker(worker_id), employer_id integer references employer(employer_id), date varchar(10), not null, paid integer not null);
```

---

## Build and run with Gradle

This project uses the **Gradle Wrapper** - no separate Gradle installation needed!

**Prerequisites:**
- JDK 23 installed
- JAVA_HOME environment variable set to `C:\Program Files\Java\jdk-23`

**Build the project:**
```powershell
.\gradlew build
```

**Run the desktop app:**
```powershell
.\gradlew run
```

**Run tests:**
```powershell
.\gradlew test
```

**Clean build artifacts:**
```powershell
.\gradlew clean
```

**Create an executable jar:**
```powershell
.\gradlew jar
java -jar build\libs\employer-worker-registration-system-1.0.0.jar
```

**Note:** The PostgreSQL JDBC dependency (v42.7.4) is now managed by Gradle. You no longer need to download or configure the driver jar manually.


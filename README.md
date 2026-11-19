# BeReal-Database-Project
# BeReal Database Project

A BeReal-style social app built with **Java Swing** and **MySQL**. Users can sign up, log in, add friends, post dual-camera BeReals, comment and react with RealMojis. Admins also get an **Advanced Queries** screen.

## Requirements
- Java 17 or newer (JDK, so you have `javac`)
- MySQL 8 or newer
- The MySQL JDBC driver is already included in `java/lib/`

## 1. Set up the database
```bash
mysql -u root -p < database/Bereal_database.sql
```
This creates the `Bereal` database with all tables and the demo data.

## 2. Run the app
**macOS / Linux**
```bash
./java/run.sh
```
**Windows**: double-click `java\run.bat`

Both scripts ask for your MySQL root password, then compile and start the app.
To skip the prompt, set it first: `export DB_PASSWORD=yourpassword` (macOS/Linux) or `set DB_PASSWORD=yourpassword` (Windows).
`DB_URL` and `DB_USER` can be set the same way if yours differ from `jdbc:mysql://127.0.0.1:3306/Bereal` and `root`.

## Demo accounts
| Username | Password | Role to select |
|---|---|---|
| testuser01 | DemoPassword123 | User |
| seconduser02 | DemoPassword456 | User |
| admin01 | AdminPass123 | Admin |

You can also create a new account from the welcome screen.

## Project structure
| Folder | Contents |
|---|---|
| `database/` | Full database dump (tables + demo data) |
| `tables/` | `CREATE TABLE` script for each table |
| `queries/` | Insert / retrieve / update / delete queries for each table, plus admin and advanced queries |
| `java/` | Java source code for the GUI and database connection |
| `java/images/` | Logo and post photos |
| `docs/`, `screenshots/` | Report and screenshots |

## Tables
`USER`, `FRIENDSHIP`, `MOMENT`, `POST`, `COMMENT`, `REALMOJIS`, `REACTS_WITH`, `TAGGED_ON`

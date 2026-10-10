# FindIT — Smart Campus Lost & Found System 🔍⚡

> A modern, full-stack campus recovery platform combining a **Java REST API backend**, **MySQL database**, and a **Vite + React frontend** with automated heuristic matching.

---

## 🌟 Overview

**FindIT** addresses a daily problem on college campuses: lost ID cards, electronics, bags, keys, and personal belongings. 

Instead of scattered WhatsApp groups or unorganized lost-and-found noticeboards, FindIT provides a centralized, authenticated platform with:
* **Interactive Feed**: Live search, category filters, and Lost/Found segregation.
* **Instant Card Preview**: Real-time visual feedback while reporting items.
* **Heuristic Match Engine**: Weighted 5-factor scoring algorithm (Category 35%, Keywords 25%, Color 15%, Location 15%, Date 10%) that automatically matches lost items against found items.
* **Ownership Verification & Claims**: Secure claim workflow requiring verification evidence to prevent unauthorized pickups.
* **Campus Admin Hub**: Real-time analytics, recovery rate tracking, and one-click claim approval/rejection.

---

## 🛠️ Architecture & Tech Stack

```
┌─────────────────────────────────┐           ┌──────────────────────────────────┐           ┌──────────────────┐
│   Frontend (React + Vite)       │   Proxy   │   Backend (Java REST API)        │   JDBC    │   Database       │
│   http://localhost:5173         │ ────────> │   http://localhost:8080          │ ────────> │   MySQL 8.0      │
│   • React 19                    │  /api/*   │   • Embedded HTTP Server         │           │   findit_db      │
│   • Vanilla CSS Design System   │           │   • BCrypt Password Hashing      │           │                  │
│   • Lucide React Icons          │           │   • Google Gson (JSON)           │           │                  │
│   • Glassmorphism Dark UI       │           │   • DAO Pattern Architecture     │           │                  │
└─────────────────────────────────┘           └──────────────────────────────────┘           └──────────────────┘
```

### Backend (Java)
* **Java 17+ / 21+**
* **JDBC (MySQL Connector/J 8.3.0)**
* **DAO Pattern**: Clean separation between database queries, business logic, and API endpoints.
* **BCrypt**: 12-round salted password hashing.
* **Embedded HTTP Server**: Built-in, high-performance REST API with CORS support.
* **Google Gson**: Fast JSON serialization for all model objects.

### Frontend (React)
* **React 19** with **Vite** build tooling
* **Modern CSS3 Design System**: Custom dark glassmorphic interface, neon gradients (`#6366F1` to `#06B6D4`), and micro-animations.
* **Lucide React**: Clean semantic icons.
* **Vite Proxy**: Seamless connection between React and Java backend without CORS friction.

### Database (MySQL)
* Normalized relational schema: `users`, `items`, `claims`, `match_suggestions`.
* Full-text indexing and foreign key constraints.

---

## 🚀 Quick Setup & Installation

### 1. Database Setup
Make sure MySQL Server is running, then execute the schema and seed scripts:
```sql
mysql -u root -p < database/schema.sql
mysql -u root -p < database/sample_data.sql
```
Create `src/main/resources/db.properties` from `db.properties.example` with your MySQL credentials:
```properties
db.url=jdbc:mysql://localhost:3306/findit_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true
db.username=root
db.password=your_mysql_password
```

### 2. Run the Java Backend
```powershell
# From project root
mvn compile package -DskipTests
java -jar target/findit-1.0.0.jar
```
*Backend runs on: `http://localhost:8080`*

### 3. Run the React Frontend
```powershell
# From project root
npm run dev
# OR from frontend folder:
# cd frontend && npm run dev
```
*Frontend runs on: `http://localhost:5173`*

---

## 🔑 Demo Accounts

| Role | Email | Password | Features |
| :--- | :--- | :--- | :--- |
| **Student** | `ritesh@college.edu` | `Student@123` | Browse Feed, Report Lost/Found, View Matches, Submit Claims |
| **Student** | `priya@college.edu` | `Student@123` | Student portal |
| **Admin** | `admin@findit.com` | `Admin@123` | Admin Hub, Live Statistics, Approve/Reject Claim Verifications |

*Tip: You can also use the **Demo Switch** dropdown in the navigation bar to switch between accounts in one click.*

---

## 🧠 Smart Matching Heuristic Algorithm

When comparing a **LOST** item against an open **FOUND** item:
1. **Category Match (35 pts)**: Exact category match.
2. **Title & Keywords (25 pts)**: Jaccard similarity between tokenized keywords.
3. **Color Correlation (15 pts)**: Primary color match.
4. **Campus Location (15 pts)**: Token overlap of location descriptions.
5. **Date Proximity (10 pts)**: Loss date vs discovery date proximity ($\le 1$ day = 10 pts, $\le 3$ days = 7 pts, etc.).

Pairs with scores $\ge 30\%$ are saved to `match_suggestions` and displayed in the **Smart Matches** tab.

---

## 📂 Project Structure

```
FindIT/
├── database/
│   ├── schema.sql                 # MySQL schema creation
│   └── sample_data.sql            # Demo student & report data
├── frontend/                      # React SPA
│   ├── src/
│   │   ├── components/            # Navbar, FeedView, ReportView, MatchesView, AdminHubView
│   │   ├── App.jsx                # Root router & state
│   │   └── App.css                # Component styling
│   └── vite.config.js             # Vite config with backend proxy
├── src/main/java/com/findit/
│   ├── api/                       # ApiServer & GsonUtils (REST API)
│   ├── config/                    # DatabaseConfig (JDBC connection pooling)
│   ├── dao/                       # UserDAO, ItemDAO, ClaimDAO, MatchDAO
│   ├── model/                     # User, Item, Claim, MatchSuggestion
│   ├── service/                   # AuthService, ItemService, ClaimService, MatchingService
│   └── Main.java                  # Java backend entry point
├── pom.xml                        # Maven configuration
└── README.md
```

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).

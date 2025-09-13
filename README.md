# Public Issue Tracker

A full-stack web application for tracking and managing public issues.  
The project is built with **Spring Boot (Java)** for the backend and **React.js** for the frontend.  
Database used: **SQL**.

---

## 🚀 Features
- User authentication & authorization (JWT-based)
- Create, update, and manage issues
- Comment system for discussions
- File attachment support
- Admin dashboard for issue moderation
- Timeline tracking of issue progress

---

## 🛠️ Tech Stack
### Backend (Spring Boot)
- Java (Spring Boot)
- Spring Security (JWT Authentication)
- RESTful APIs
- SQL Database
- Maven for dependency management
- Docker Compose support

### Frontend (React)
- React.js with Hooks
- Axios for API requests
- CSS for styling
- Responsive design

---

## 📂 Project Structure
```
public-issue-master/
│── pit-frontend/           # React frontend
│   ├── src/                # Frontend source code
│   ├── public/             # Static files
│   └── package.json        # NPM dependencies
│
│── public-issue-tracker/   # Spring Boot backend
│   ├── src/main/java/      # Backend source code
│   ├── pom.xml             # Maven configuration
│   └── docker-compose.yml  # Containerization support
│
└── README.md               # Project documentation
```

---

## ⚙️ Installation & Setup

### 1. Clone Repository
```bash
git clone https://github.com/Anish-3-12/public-issue.git
cd public-issue-tracker
```

### 2. Setup Backend
```bash
cd public-issue-tracker
./mvnw spring-boot:run
```
The backend will start at `http://localhost:8080`

### 3. Setup Frontend
```bash
cd pit-frontend
npm install
npm start
```
The frontend will start at `http://localhost:3000`

---

## 🧪 Running with Docker
```bash
cd public-issue-tracker
docker-compose up --build
```

---


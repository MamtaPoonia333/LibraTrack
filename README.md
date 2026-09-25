# 📚 Library Management System

A modern, full-stack library management application with a beautiful glassmorphism UI theme, built with Spring Boot and React.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen?style=flat&logo=springboot)
![React](https://img.shields.io/badge/React-19.2.0-blue?style=flat&logo=react)
![Tailwind CSS](https://img.shields.io/badge/Tailwind-3.4.17-38bdf8?style=flat&logo=tailwindcss)

## ✨ Features

### 📖 Book Management
- Add, view, and delete books
- Book type support for physical books and eBooks
- PDF/EPUB metadata and file handling for eBooks
- ISBN validation (10 or 13 digits)
- Advanced search by title, author, or genre
- Filter by genre and availability status
- Real-time book count and availability tracking
- Book borrowing history
- Admin/librarian-only book management

### 👥 User Management
- Register new library members
- Email validation with duplicate checking
- Phone number field (10-15 digits)
- View all registered users
- Track borrowed books per user
- User borrowing history
- JWT-based login and authentication
- Email verification using OTP (planned)
- Redis-backed OTP expiration (planned)
- Role-based access control: `ADMIN`, `LIBRARIAN`, `MEMBER`
- Secure password hashing
- Refresh-token support

### 📝 Issue/Return System
- Issue books with automatic due-date assignment
- **3-book borrowing limit** per user
- **Early return support** - users can return books anytime
- Automatic book availability updates
- Overdue tracking with visual indicators
- Automatic fine calculation based on overdue days
- Fine payment and status tracking
- Days remaining and overdue calculation
- Due-soon notifications (planned)
- Borrowing history and transaction validation
- Duplicate and invalid issue prevention

### 🔔 Notifications & Messaging
- Real-time admin-to-user messaging
- Admin announcements (planned)
- Real-time notifications (planned)
- Due-date reminders (planned)
- Overdue and fine notifications (planned)
- Read/unread message status (planned)
- Persistent message history in MySQL (planned)
- WebSocket/STOMP-based real-time communication

### 🔐 Authentication & Security
- JWT-based authentication
- Access and refresh tokens
- Spring Security role-based authorization
- Secure password hashing
- Email verification with expiring OTPs (planned)
- Redis-backed OTP expiration (planned)
- Protected REST endpoints
- Logout and token invalidation
- Input validation and centralized exception handling

### ⚡ Redis Features
- OTP storage with automatic expiration (planned)
- JWT/token blacklist for logout (in-memory; Redis planned)
- Caching frequently accessed library data (planned)
- Temporary session and verification data (planned)
- Optional notification and pub/sub support (planned)

### 💰 Fine Calculation
Fine amounts will be calculated from overdue days using a configurable daily rate:

`Fine = overdueDays × finePerDay`

### 📊 Dashboard
- Total books, available books, issued books statistics
- Total users count
- **Overdue books alerts** with highlighting
- Due soon notifications (within 3 days)
- Beautiful gradient stat cards with icons

### 🎨 UI/UX Features
- **Soft glassmorphism theme** with frosted glass effects
- **Pastel color palette** (pink, purple, blue gradients)
- **Poppins font family** for modern typography
- Cute emoji icons throughout
- Smooth animations and hover effects
- Floating bubble background animations
- Custom gradient scrollbars
- Responsive design for all screen sizes

## 🛠️ Tech Stack

### Backend
- **Java 21 LTS** - Latest long-term support version
- **Spring Boot 3.3.5** - Framework for REST API
- **Maven 3.9.12** - Build automation tool
- **MySQL** - Persistent storage for users, books, loans, borrowing history, and fines
- **Spring Security and JWT** - Authentication and authorization
- **Redis** - OTP expiration, token invalidation, caching, and pub/sub (planned)
- **MySQL** - Persistent messaging and relational data storage (planned)
- **WebSocket with STOMP** - Real-time messaging and notifications

### Frontend
- **React 19.2.0** - UI library
- **Vite 7.3.1** - Fast build tool and dev server
- **Tailwind CSS 3.4.17** - Utility-first CSS framework
- **Axios** - HTTP client for API calls
- **React Hot Toast** - Toast notifications
- **Google Fonts (Poppins)** - Modern, friendly typography

### Development Tools
- **ESLint** - Code linting
- **PostCSS** - CSS processing
- **CORS Configuration** - Cross-origin resource sharing

## 📁 Project Structure

```
LIBRARY MANAGEMENT/
├── backend/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/library/management/
│   │       │   ├── LibraryManagementApplication.java
│   │       │   ├── config/
│   │       │   │   └── CorsConfig.java
│   │       │   ├── controller/
│   │       │   │   ├── BookController.java
│   │       │   │   └── UserController.java
│   │       │   ├── exception/
│   │       │   │   ├── ApiException.java
│   │       │   │   └── GlobalExceptionHandler.java
│   │       │   ├── model/
│   │       │   │   ├── Book.java
│   │       │   │   ├── EBook.java
│   │       │   │   └── User.java
│   │       │   └── service/
│   │       │       └── LibraryService.java
│   │       └── resources/
│   │           └── application.properties
│   ├── pom.xml
│   └── target/
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   │   └── client.js
│   │   ├── components/
│   │   │   ├── Layout.jsx
│   │   │   └── Skeleton.jsx
│   │   ├── pages/
│   │   │   ├── DashboardPage.jsx
│   │   │   ├── BooksPage.jsx
│   │   │   ├── UsersPage.jsx
│   │   │   └── IssueReturnPage.jsx
│   │   ├── App.jsx
│   │   ├── main.jsx
│   │   └── index.css
│   ├── index.html
│   ├── package.json
│   ├── tailwind.config.js
│   └── vite.config.js
└── README.md
```

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.9+
- Node.js 18+ and npm

### Backend Setup

1. Navigate to the backend directory:
```bash
cd backend
```

2. Build the project:
```bash
mvn clean install -DskipTests
```

3. Run the Spring Boot application:
```bash
java -jar target/library-management-0.0.1-SNAPSHOT.jar
```

The backend server will start on **http://localhost:8080**

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The frontend will be available at **http://localhost:5173**

## 🎯 Usage

1. **Dashboard** - View library statistics and due soon alerts
2. **Books** - Manage book catalog with search and filters
3. **Users** - Register and manage library members
4. **Issue/Return** - Issue books to users and process returns

### API Endpoints

#### Books
- `GET /api/books` - Get all books
- `GET /api/books?search=&genre=&available=` - Search and filter books
- `GET /api/books/stats` - Get total, available, and issued book counts
- `POST /api/books` - Add a new book
- `POST /api/books/{id}/file` - Upload a PDF or EPUB eBook file
- `GET /api/books/{id}/file` - Download an attached eBook file
- `GET /api/books/{id}/history` - Get borrowing history for a book
- `DELETE /api/books/{id}` - Delete a book
- `GET /api/books/issued` - Get all issued books

#### Users
- `GET /api/users` - Get all users
- `GET /api/users/{userId}/borrowed-books` - Get books borrowed by a user
- `GET /api/users/{userId}/history` - Get borrowing history for a user
- `POST /api/users` - Register a new user
- `POST /api/users/{userId}/role` - Change a user role (admin only)
- `POST /api/users/{userId}/issue/{bookId}` - Issue a book to user
- `POST /api/users/{userId}/return/{bookId}` - Return a book

#### Fines
- `GET /api/fines` - Get calculated fines
- `GET /api/fines/user/{userId}` - Get fines for a user
- `POST /api/fines/{fineId}/pay` - Mark a fine as paid

#### Messaging
- WebSocket/STOMP endpoint: `/ws`
- Send messages to `/app/messages`
- Subscribe to `/topic/messages`

#### Authentication
- `POST /api/auth/register` - Register with a password
- `POST /api/auth/login` - Get access and refresh tokens
- `POST /api/auth/refresh` - Rotate a refresh token
- `POST /api/auth/logout` - Revoke access and refresh tokens
- `POST /api/auth/verify-email` - Verify an email with an expiring OTP
- `POST /api/auth/request-password-reset` - Request a password-reset OTP
- `POST /api/auth/reset-password` - Reset a password with an OTP

#### Authentication
- `POST /api/auth/register` - Register with a hashed password
- `POST /api/auth/login` - Get access and refresh tokens
- `POST /api/auth/refresh` - Exchange a refresh token for a new access token
- `POST /api/auth/logout` - Invalidate a refresh token

#### Fines and Messaging
- `GET /api/fines?userId=` - Calculate outstanding fines
- `POST /api/fines/{userId}/{bookId}/pay` - Mark a fine as paid
- `GET /ws` - Connect to the STOMP WebSocket endpoint
- `SEND /app/messages` and `SUBSCRIBE /topic/messages` - Send and receive messages

## 🎨 Design Features

### Glassmorphism Theme
- Frosted glass effects with `backdrop-blur-lg`
- Semi-transparent white backgrounds (`bg-white/60`)
- Soft shadows and gradients
- Border highlights with `border-white/50`

### Color Palette
- **Primary**: Pink to Purple gradients (`from-pink-400 to-purple-400`)
- **Success**: Green to Emerald (`from-green-300 to-emerald-300`)
- **Warning**: Amber to Yellow (`from-amber-200 to-yellow-200`)
- **Error**: Red to Pink (`from-red-300 to-pink-300`)
- **Background**: Soft pastels (`from-pink-50 via-purple-50 to-blue-50`)

### Typography
- **Font Family**: Poppins (weights 300-800)
- **Headings**: Gradient text with `bg-clip-text`
- **Body**: Clean, readable with proper spacing

## 🔒 Validation & Business Rules

### Email Validation
- Format: `example@domain.com`
- Regex pattern validation
- Duplicate email prevention (case-insensitive)

### ISBN Validation
- Accepts 10 or 13 digit ISBNs
- Strips formatting characters
- Optional field

### Phone Number Validation
- Minimum 10 digits, maximum 15 digits
- Optional field
- Supports international formats

### Book Issue Rules
- Maximum 3 books per user
- Automatic due date calculation (14 days)
- Cannot issue unavailable books
- Real-time book count display

### Return Policy
- **Early returns welcome** - no restrictions
- Can return anytime before or after due date
- Overdue tracking with visual indicators
- Automatic availability update

## 📊 Data Storage

The backend uses MySQL for persistent application data. On startup, `schema.sql` creates these tables:

- `users` - Member credentials, roles, and verification state
- `books` - Physical/eBook metadata and availability
- `loans` - Active issues and due dates
- `borrow_history` - Completed and active borrowing records
- `fines` - Overdue amounts and payment status

Create `backend/.env` from `backend/.env.example` and set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. The `.env` file is ignored by Git and must never be committed.

Uploaded eBook files and local token-revocation data remain on the filesystem under the configured data directory; business records are stored in MySQL.

## 🔮 Future Enhancements

- [ ] Database integration (PostgreSQL/MySQL)
- [ ] User authentication and authorization
- [ ] Book reservation system
- [ ] Fine calculation for overdue books
- [ ] Email notifications for due dates
- [ ] Book reviews and ratings
- [ ] Advanced analytics and reports
- [ ] Multi-language support
- [ ] Dark mode toggle
- [ ] Mobile app version

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📝 License

This project is open source and available under the MIT License.

## 👤 Author

**Mamta Poonia**
- GitHub: [@MamtaPoonia333](https://github.com/MamtaPoonia333)

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- React team for the amazing UI library
- Tailwind CSS for the utility-first CSS framework
- Google Fonts for the beautiful Poppins font

---

Made with 💝 and ☕️

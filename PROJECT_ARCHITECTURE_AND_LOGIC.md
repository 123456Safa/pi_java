# PharmaX Project: Comprehensive Architecture & Logic Documentation

## 1. Project Overview
PharmaX is a modern Desktop application built with **JavaFX** for managing users with high security and AI integration. It focuses on a premium user experience, robust authentication methods, and an intelligent assistant capable of performing administrative tasks.

### Tech Stack
*   **Frontend**: JavaFX 21 (FXML + CSS), JavaCV/OpenCV for camera integration.
*   **Backend Logic**: Java 17, Maven for dependency management.
*   **Database**: MySQL (local instance `pharmax`).
*   **AI Integration**: OpenRouter API (Nemotron 3 Nano Omni model).
*   **Security**: Google Authenticator (TOTP), Google OAuth 2.0, SHA-256 hashing.
*   **Microservices**: Python-based face recognition service (Dlib/DeepFace).

---

## 2. System Architecture
The project follows a **Layered Architecture** combining MVC (Model-View-Controller) with a dedicated Service layer and external microservices.

### Layers:
1.  **Models**: Plain Java Objects (POJOs) representing database entities (`User`, `AdminLog`, `ChatMessage`).
2.  **Views**: JavaFX-based UI components. Some use FXML, while others are built programmatically for dynamic content.
3.  **Controllers**: Bridge between Views and Services, handling UI events and data binding.
4.  **Services**: Core business logic. This layer interacts with the database (`MyDatabase`) and external APIs (`OpenRouter`, `Google OAuth`, `Face ID Microservice`).
5.  **Utils**: Utility classes for session management (`SessionManager`), database connectivity, and UI helpers.
6.  **External Microservice**: A Python script (`python_microservice_template.py`) provides specialized AI/Computer Vision capabilities for facial embedding extraction.

---

## 3. Database Schema
The primary table is `user`, designed to support multiple authentication flows and metadata tracking.

| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | INT | Primary Key, Auto-increment. |
| `email` | VARCHAR(255) | Unique identifier for login. |
| `password` | VARCHAR(255) | SHA-256 hashed password. |
| `roles` | LONGTEXT | JSON string (e.g., `["ROLE_ADMIN"]`). |
| `first_name` / `last_name` | VARCHAR(255) | User personal details. |
| `avatar` | VARCHAR(255) | Path to profile picture. |
| `status` | VARCHAR(50) | `UNBLOCKED` or `BLOCKED`. |
| `google_authenticator_secret`| VARCHAR(255) | 2FA Secret key (TOTP). |
| `face_encoding` | LONGTEXT | JSON array of 128/512 facial embedding values. |
| `face_auth_enabled` | BOOLEAN | Toggle for Face ID login. |
| `failed_attempts` | INT | Tracker for brute-force protection. |
| `lockout_time` | TIMESTAMP | Timestamp until which the account is locked. |
| `google_id` | VARCHAR(255) | Google internal ID for OAuth link. |

---

## 4. Core Features & Logic

### 4.1 Authentication & Security Flows

#### A. Multi-Factor Authentication (2FA)
*   **Logic**: Uses the Time-based One-Time Password (TOTP) algorithm.
*   **Setup**: The system generates a random secret, encodes it into a `otpauth://` URI, and displays a QR code using ZXing.
*   **Verification**: The user enters the 6-digit code from their app (Google Authenticator/Authy). `TwoFactorAuthService` validates it against the stored secret.

#### B. Google OAuth 2.0 Integration
*   **Logic**: Implements the "Authorization Code Flow" for installed applications.
*   **Process**:
    1.  Launches a local Jetty server on a random port.
    2.  Opens the system browser to Google's consent screen.
    3.  Captures the code, exchanges it for a token, and fetches the user's profile info (email, name, picture).
    4.  If the email exists, the user is logged in; otherwise, a new account is created automatically (auto-registration).

#### C. Facial Recognition (Face ID)
*   **Architecture**: Java captures video frames -> Sends Base64 image to Python Microservice -> Python returns embedding.
*   **Enrollment**: Extracts the facial vector and stores it in the `face_encoding` column.
*   **Login**: Captures a live frame, gets a new vector, and calls the `/compare-embeddings` endpoint. The microservice compares vectors using Cosine Similarity or Euclidean Distance.

#### D. Account Lockout
*   **Logic**: If a user fails to login 5 times consecutively, the `lockout_time` is set to `current_time + 15 minutes`.
*   **Enforcement**: The login service checks if the current time is before `lockout_time`. If so, access is denied even with correct credentials.

#### E. Password Recovery (Email)
*   **Logic**: Uses `EmailService` to send a 6-digit verification code via SMTP (Gmail).
*   **Flow**: User enters email -> System generates code and sends it -> User enters code -> System allows password reset.

#### F. Bot Protection (reCAPTCHA)
*   **Logic**: Integration of Google reCAPTCHA v3.
*   **Verification**: The backend (`RecaptchaService`) verifies the token against Google's API to calculate a "bot score" before allowing critical actions like Registration or Password Reset.

### 4.2 AI Chatbot Assistant (PharmaX Assistant)
The chatbot is not just a conversational tool; it is an **Autonomous Agent** integrated into the system's core.

*   **Brain**: Powered by OpenRouter using the `Nemotron 3 Nano Omni` model.
*   **Function Calling**: The AI has access to a `FunctionCallDispatcher` which allows it to:
    *   **Manage Users**: Create, Delete, Update, and Search users directly from chat commands.
    *   **Auditing**: Access admin logs to explain "Who deleted user X?" or "When was Y created?".
    *   **Memory**: Load previous chat history from the database to maintain context across sessions.
    *   **UI Control**: Trigger navigation (e.g., "Take me to my profile").
*   **Logic Loop**:
    1.  User sends message.
    2.  AI determines if a tool is needed (e.g., `search_users`).
    3.  Java executes the tool and sends the result (JSON) back to the AI.
    4.  AI summarizes the result in natural language.

### 4.3 Administrative Tools

#### A. Audit Logging
*   **Logic**: Every critical action (Update, Delete, Block) is recorded in the `admin_log` table via `AdminLogService`.
*   **Data**: Stores the Admin ID, Action type, Target User ID, and a detailed JSON blob of what changed.

#### B. Data Export
*   **Logic**: `ExportService` uses **Apache POI** (for Excel) and **iText** (for PDF).
*   **Feature**: Allows exporting the entire user database into clean, formatted documents.

---

## 5. UI Design & UX
The application uses a **Modern Dark Theme** with premium aesthetics:
*   **Glassmorphism**: Subtle transparency and blur effects on sidebars and cards.
*   **Dynamic Layouts**: Responsive sidebars and grids that adapt to screen size.
*   **Feedback System**: Animated notifications and progress indicators for long-running tasks (like AI processing or Face ID capture).

---

## 6. Error Handling & Robustness
*   **Database**: Uses a Singleton connection pool with auto-reconnect logic in `MyDatabase`.
*   **API Resilience**: The AI service includes timeouts and handles HTTP 400/500 errors gracefully.
*   **Validation**: Robust regex-based validation for emails and strong password enforcement at both the UI and Service levels.

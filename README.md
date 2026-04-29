# PharmaX - Blog & Comment Management System

A modern JavaFX desktop application for managing pharmaceutical blog articles with intelligent comment moderation and RSS-based article scraping.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [Database Schema](#database-schema)
- [Development](#development)

## 🎯 Overview

PharmaX is a comprehensive blog management and comment moderation system built with JavaFX. It provides:

- **Public Blog Interface**: Browse published articles with user comments
- **Admin Dashboard**: Create, edit, and publish articles with draft management
- **Intelligent Comment Moderation**: Two-tier moderation (blacklist + AI-powered content filtering)
- **Article Scraping**: Fetch health-related content from RSS feeds in English and French
- **Modern UI**: Responsive, animated JavaFX interface with professional design

**Target Users**:
- Pharmaceutical blog administrators who need to manage content and comments
- Content moderators who need to review and approve user submissions
- Blog editors who want to curate health-related articles from external sources

## ✨ Features

### Public Features
- 📰 **Browse Articles**: View published blog articles in a responsive card-based layout
- 💬 **Comment on Articles**: Submit comments with automatic validation and moderation
- 👍 **Like Articles**: Track favorite articles (session-based)
- 🔍 **Search Articles**: Full-text search across article titles and content
- 📱 **Responsive Design**: Animations and modern UI with hover effects

### Admin Features
- ✏️ **Article Management**: Create, edit, publish, and delete articles
- 📋 **Draft Workflow**: Save articles as drafts before publishing
- 👥 **Comment Moderation**: Review, approve, or block user comments
- 🤖 **AI Moderation**: Automatic toxic content detection using HuggingFace Toxic-BERT
- 🗂️ **Comment Archival**: Store blocked/inappropriate comments for audit purposes
- 📊 **Moderation Statistics**: Real-time overview of comment statuses

### Technical Features
- 🔗 **Graceful Degradation**: App functions without database connection
- ⚡ **Async Loading**: Non-blocking UI with background data fetching
- 🔄 **Connection Recovery**: Automatic database reconnection with retry cooldown
- 🎨 **Dark/Light Themes**: Professional CSS styling with theme support
- 📡 **RSS Scraping**: Multi-source article scraping with caching

## 🏗️ Architecture

### High-Level Structure

```
┌─────────────────────────────────────────────────────────┐
│                  UI Layer (JavaFX)                      │
│  MainApp → Controllers (Admin/Front/Scraper) → Views   │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│              Service Layer (Business Logic)             │
│  ArticleService, CommentaireService,                   │
│  CommentValidationService, CommentModerationService,   │
│  AIService, ScraperService                             │
└────────────────┬────────────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────────────┐
│         Data Layer (Database + External APIs)           │
│  DatabaseConnection → MySQL Database                   │
│  ScraperService → RSS Feeds                            │
│  CommentModerationService → HuggingFace AI API         │
└─────────────────────────────────────────────────────────┘
```

### Key Components

#### Database Layer
- **DatabaseConnection**: Thread-safe singleton for MySQL connectivity with automatic reconnection
- **Schema**: 3 main tables (article, commentaire, commentaire_archive)
- See [Database Schema](#database-schema) for details

#### Service Layer
- **ArticleService**: CRUD operations for articles, search, pagination
- **CommentaireService**: Comment management with integrated validation and moderation
- **CommentValidationService**: Input validation (length, content rules)
- **CommentModerationService**: Two-tier content filtering (blacklist + AI)
- **AIService**: LLM-based article generation via Ollama
- **ScraperService**: Multi-source RSS feed scraping with caching

#### UI Layer
- **MainApp**: Application orchestration with 3-phase startup
- **FrontBlogController**: Public article browsing with animations
- **AdminBlogController**: Admin article management interface
- **AdminCommentController**: Comment moderation dashboard
- **ScraperController**: Article scraping interface
- **SplashScreen**: Animated loading screen

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed component documentation.

## 🚀 Installation

### Prerequisites
- Java 17+ (tested with Java 21)
- MySQL 8.0+
- Maven 3.8+
- JavaFX 21 (Maven handles this)

### Optional Requirements
- **For AI article generation**: Ollama with Mistral-7B model
- **For AI comment moderation**: HuggingFace API key (graceful fallback to blacklist if unavailable)

### Setup Steps

1. **Clone the repository**
   ```bash
   cd java-blog-comment
   ```

2. **Create MySQL Database**
   ```bash
   mysql -u root -p < setup_database.sql
   ```
   This creates the `pharmax` database with all required tables and indexes.

3. **Configure Database Connection**
   Edit `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://127.0.0.1:3306/pharmax?serverTimezone=UTC&useSSL=false
   db.user=root
   db.password=
   db.driver=com.mysql.cj.jdbc.Driver
   # Optional: AI API key for comment moderation
   ai.api.key=your_huggingface_key_here
   ```

4. **Build the Project**
   ```bash
   mvn clean package
   ```

5. **Run the Application**
   ```bash
   mvn javafx:run
   ```

See [CONFIGURATION.md](CONFIGURATION.md) for detailed configuration options.

## ⚙️ Configuration

All configuration is managed through `src/main/resources/db.properties`:

### Database Configuration
```properties
db.url=jdbc:mysql://127.0.0.1:3306/pharmax?serverTimezone=UTC&useSSL=false
db.user=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver
```

### Optional AI Services
```properties
ai.api.key=your_huggingface_api_key
```

### Customizable Constants
- **Comment Validation**: Min length (2), Max length (1000) - see CommentValidationService
- **AI Threshold**: Toxic score threshold (0.4) - see CommentModerationService
- **Scraper Cache**: Never invalidated by default - clear manually via UI
- **Database Connection**: Retry cooldown (15 seconds) - see DatabaseConnection

See [CONFIGURATION.md](CONFIGURATION.md) for detailed configuration guide.

## 📊 Usage

### Public Users

1. **Browse Articles**
   - Articles displayed as flip cards with image, title, and excerpt
   - Hover for animation and interaction buttons
   - Click "View Comments" to see discussion

2. **Submit Comments**
   - Comments automatically validated for length and content
   - Two-tier moderation: blacklist + optional AI detection
   - Moderated comments require admin approval before display

3. **Like and Save Articles**
   - Use the heart icon to like articles (session-based)
   - Save articles for your session

### Administrators

1. **Manage Articles**
   - **Create**: Fill in title, content, and image URL
   - **Draft**: Save without publishing
   - **Publish**: Make article visible to public
   - **Edit**: Modify existing articles
   - **Delete**: Remove articles (cascades to comments)

2. **Moderate Comments**
   - **Active Comments Tab**: Shows pending, approved, and blocked
   - **Approve**: Allow comment to display publicly
   - **Block**: Hide comment and archive for audit
   - **Archived Tab**: View blocked comments with reasons
   - **Statistics**: Real-time counts by status

3. **Scrape Articles**
   - **Search**: Enter keyword and select language (English/French)
   - **Sources**: Guardian Health, BBC Health, Santé Magazine, Le Monde
   - **Preview**: View results before importing
   - **Cache**: Clear to force fresh fetch from feeds

### Content Moderators

#### Comment Moderation Workflow

1. **Validation Phase** (automatic)
   - Length check: 2-1000 characters
   - Status validation: valide/bloque/en_attente
   
2. **Moderation Phase** (two-tier)
   - **Tier 1 - Blacklist**: 80+ bad words in English/French (always works)
   - **Tier 2 - AI**: HuggingFace Toxic-BERT (optional, best-effort)
   
3. **Status**
   - **valide**: Approved comments visible to public
   - **en_attente**: Awaiting admin approval
   - **bloque**: Blocked comments (archived)

See [CommentModerationService](src/main/java/com/pharmax/service/CommentModerationService.java) for moderation logic.

## 📁 Database Schema

### Tables Overview

#### `article`
Stores blog articles (published and draft)

```sql
CREATE TABLE article (
  id INT PRIMARY KEY AUTO_INCREMENT,
  titre VARCHAR(255) NOT NULL,
  contenu LONGTEXT NOT NULL,
  image VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  likes INT DEFAULT 0,
  is_draft BOOLEAN DEFAULT TRUE,
  INDEX idx_draft (is_draft),
  INDEX idx_created (created_at)
)
```

**Key Fields**:
- `titre`: Article title (indexed for search)
- `contenu`: Full article content (supports long text)
- `image`: URL to article image
- `is_draft`: TRUE for draft, FALSE for published
- `likes`: Counter for user likes (session-based in app)
- `created_at`, `updated_at`: Timestamps for auditing

#### `commentaire`
Stores user comments (published and pending)

```sql
CREATE TABLE commentaire (
  id INT PRIMARY KEY AUTO_INCREMENT,
  contenu TEXT NOT NULL,
  statut ENUM('valide', 'bloque', 'en_attente') DEFAULT 'en_attente',
  article_id INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (article_id) REFERENCES article(id) ON DELETE CASCADE,
  INDEX idx_article (article_id),
  INDEX idx_statut (statut),
  INDEX idx_created (created_at)
)
```

**Key Fields**:
- `contenu`: Comment text
- `statut`: Comment approval status (3 states)
- `article_id`: Reference to parent article
- **Cascade Delete**: When article deleted, all comments deleted

#### `commentaire_archive`
Stores archived (blocked) comments for audit purposes

```sql
CREATE TABLE commentaire_archive (
  id INT PRIMARY KEY AUTO_INCREMENT,
  contenu TEXT NOT NULL,
  reason VARCHAR(255),
  archived_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  article_id INT,
  INDEX idx_article (article_id),
  INDEX idx_archived (archived_at)
)
```

**Key Fields**:
- `reason`: Why comment was blocked (e.g., "toxic_content", "spam")
- `article_id`: Reference to original article (nullable if article deleted)

### Indexes
- `article.is_draft`: Fast filtering of draft vs published
- `article.created_at`: Sorting by date
- `commentaire.article_id`: Finding comments for an article
- `commentaire.statut`: Filtering by approval status
- `commentaire.created_at`: Chronological sorting

### Data Consistency Rules
- Article can have 0 or more comments
- Deleting article cascades to comments and archives
- Comment status must be one of 3 enum values
- All timestamps are UTC

See [setup_database.sql](setup_database.sql) for full schema creation script.

## 🛠️ Development

### Project Structure
```
java-blog-comment/
├── src/main/
│   ├── java/com/pharmax/
│   │   ├── Launcher.java              # JavaFX module path workaround
│   │   ├── MainApp.java               # Application entry point
│   │   ├── controller/                # UI controllers
│   │   ├── model/                     # Data models (Article, Commentaire, etc.)
│   │   ├── service/                   # Business logic (CRUD, validation, moderation)
│   │   ├── ui/                        # Custom UI components (SplashScreen)
│   │   └── util/                      # Utilities (DatabaseConnection)
│   └── resources/
│       ├── db.properties              # Database configuration
│       ├── css/                       # JavaFX stylesheets
│       └── fxml/                      # FXML view definitions
├── pom.xml                            # Maven configuration
├── setup_database.sql                 # Database schema
└── README.md                          # This file
```

### Key Dependencies
- **JavaFX 21**: UI framework
- **MySQL Connector/J**: Database driver
- **Jsoup**: HTML/XML parsing for RSS scraping
- **OkHttp3**: HTTP client for API calls
- **slf4j**: Logging (recommended, not yet implemented)

See `pom.xml` for complete dependency list.

### Running from IDE

**IntelliJ IDEA**:
1. Set up JavaFX SDK in Project Settings
2. Run → Edit Configurations
3. Create JavaFX Application config pointing to `Launcher.java`
4. Run configuration

**Eclipse**:
1. Install JavaFX plugin
2. Right-click project → Run As → Run Configurations
3. Create JavaFX Application configuration

**VS Code**:
1. Install Extension Pack for Java
2. Run Maven goal: `mvn javafx:run`

### Building for Distribution

```bash
# Create executable JAR
mvn clean package

# Run packaged JAR
java -jar target/pharmax-app.jar
```

### Testing

Currently, there are no unit tests. To add tests:

```bash
# Add JUnit 5 and test dependencies to pom.xml
# Create src/test/java/ structure mirroring src/main/java/
# Write tests for services (ArticleService, CommentaireService, etc.)
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for more development guidelines.

## 🔒 Security Notes

⚠️ **Current Limitations**:
- No user authentication (any user can access admin functions)
- Database password in plaintext in db.properties
- No input sanitization on article content (HTML/scripts can be embedded)
- API keys stored in properties file

**Recommendations**:
1. Implement user authentication (OAuth, LDAP, or simple login)
2. Use environment variables for sensitive credentials
3. Sanitize HTML input to prevent XSS attacks
4. Add role-based access control (RBAC) for admin functions
5. Implement audit logging for all moderation actions
6. Use HTTPS for external API calls

## 📞 Support

For issues, refer to:
- [ARCHITECTURE.md](ARCHITECTURE.md) - Technical architecture
- [CONFIGURATION.md](CONFIGURATION.md) - Configuration guide
- Service class documentation in source code
- Database schema comments in [setup_database.sql](setup_database.sql)

## 📄 License

[Add your license here]

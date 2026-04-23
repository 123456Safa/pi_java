# User Module - Smooth Single Stage MVC

The app now opens with authentication before dashboard access.

- `Models`: `User`
- `Services`: JDBC CRUD + SQL search/order/statistics + account lookup in `ServiceUser`
- `Controllers`: validation, role mapping, authentication, and orchestration in `UserController`
- `UI`: one stage in `Test.Main` with separate `Login` and `Register` screens, then section switching (`Create`, `Read`, `Update`, `Delete`)

## Implemented features

- Login screen shown by default at startup
- Small link/button from login to register and back
- Register creates a new DB user and auto-logs in immediately
- Session is memory-only; closing app resets login state
- CRUD sections in one window (no pop-up windows)
- Update/Delete require selected table row (manual ID input removed)
- Search across email/first name/last name/role
- SQL order-by (`ID`, `Email`, `First Name`, `Last Name`, `Role`) with ascending/descending
- Statistics cards (`Total Users`, `Admin Users`, `Normal Users`)
- Role selection via dropdown (`Admin`, `Normal User`)

## Run

```powershell
cd C:\Users\lolaa\Downloads\pi_java-gestion_utilisateur
mvn clean compile
mvn javafx:run
```

## Notes

- Database table remains `user`.
- Passwords are stored as SHA-256 hashes for new/updated accounts.
- Existing plain passwords can still login (backward compatibility), then become hashed on update.
- `Launcher` stays valid and points to `Main`.
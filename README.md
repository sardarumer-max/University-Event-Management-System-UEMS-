# University Event Management System (UEMS) – Console Version

**Repository:** University-Event-Management-System-UEMS-  
**Course:** Software Design & Analysis (SDA) – Assignment 4  
**Name:** Umer Abdullah  
**Registration No:** 24P-0557

Java implementation of the Assignment 3 class diagram of UEMS: societies propose events, the
Student Affairs administrator approves them, and students register, pay, cancel, attend and give feedback.

## Packages and classes

| Package | Class | Type | Purpose |
|---|---|---|---|
| model | `User` | abstract class | Common account data, `login()`, `logout()`; abstract `getDashboard()`, `getRole()` |
| model | `Student` | concrete (extends User) | Searches events, submits feedback |
| model | `EventOrganizer` | concrete (extends User) | Creates proposals, marks attendance, issues certificates |
| model | `Administrator` | concrete (extends User) | Approves / rejects events, generates reports |
| model | `Notification` | abstract class | Message + status; abstract `send()` |
| model | `EmailNotification` | concrete (extends Notification) | E-mail with ticket attachment |
| model | `SMSNotification` | concrete (extends Notification) | Short text, truncated to max length |
| model | `Validatable` | interface | `validate()` – implemented by Event and Feedback |
| model | `Event` | concrete (implements Validatable) | Event data, seats, status, ratings |
| model | `Feedback` | concrete (implements Validatable) | Rating and comment |
| model | `Venue` | concrete | Slot booking, availability check |
| model | `Registration` | concrete | Student–event link; owns Ticket and Attendance (composition) |
| model | `Ticket` | concrete | QR e-ticket |
| model | `Attendance` | concrete | Attendance record, certificate |
| model | `Payment` | concrete | Fee payment, refund, receipt |
| model | `PaymentGateway` | concrete (boundary) | Simulated external gateway |
| model | `RegistrationManager` | concrete (controller) | Register, pay, waitlist, cancel rules |
| main | `Main` | concrete | Console menu, `main(String[] args)` |

## How to run

**Eclipse:** File → Import → General → Existing Projects into Workspace → select this folder,
then right-click `main/Main.java` → Run As → Java Application.

**Command line (JDK 8 or newer):**

```
javac -d bin src/model/*.java src/main/*.java
java -cp bin main.Main
```

## Sample accounts (test data)

| Role | E-mail | Password | Roll no |
|---|---|---|---|
| Student | ali@uems.edu.pk | ali123 | 24P-0101 |
| Student | sara@uems.edu.pk | sara123 | 24P-0102 |
| Student | hamza@uems.edu.pk | hamza123 | 24P-0103 |
| Organizer | ayesha@uems.edu.pk | org123 | – |
| Administrator | admin@uems.edu.pk | admin123 | – |

Payments are simulated: the program asks for a wallet balance and the gateway declines the charge if
the balance is lower than the fee.

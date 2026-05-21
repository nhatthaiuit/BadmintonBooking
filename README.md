# Badminton Booking System (Mobile Application)

## Overview
This is a commercial-grade mobile application designed for badminton court owners and businesses to digitize their booking processes. The application provides a seamless, modern, and highly intuitive user interface for end-users to view court availability in real-time, select time slots via a 2D matrix, and proceed to secure checkout.

## Architecture & Tech Stack
- **Platform:** Android (Native Java)
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)
- **Design System:** Material Design Components, Custom Rounded UI (Anti-template design)
- **Key Modules:**
  - `HomeActivity`: Implements a 2D scrollable timetable matrix mimicking professional calendar software.
  - `CheckoutActivity`: Order summarization and pre-payment confirmation.
  - `SyncService`: Background service architecture for data synchronization.
  - Push Notifications: Integrated local broadcast notifications (Android 13+ compliant).

## Project Setup for Development Team
To build and run this project:
1. Clone the repository to your local machine.
2. Open the project via **Android Studio** (Flamingo or later recommended).
3. Allow Gradle to sync the dependencies.
4. Run on a physical device or an Android Emulator (API 24+).

## Backend Integration Requirements (Next Phase)
For the incoming Backend Engineering team, the following modules require API integration to transition from the current offline client architecture to a fully connected cloud architecture:

1. **Identity & Access Management (IAM):**
   - Replace the local `SharedPreferences` session management in `MainActivity.java` and `RegisterActivity.java` with secure JWT-based REST APIs or Firebase Auth.

2. **Real-time Availability Engine:**
   - In `HomeActivity.java`, the `generateTimetableMatrix()` function currently utilizes offline mock generation. 
   - **Task:** Implement an HTTP GET request to fetch the live master schedule (Branches, Courts, and Time-slot states: `EMPTY`, `BOOKED`, `PENDING`).

3. **Payment & Checkout Gateway:**
   - In `CheckoutActivity.java`, the `btnConfirmPayment` triggers a local success state.
   - **Task:** Integrate a third-party payment SDK (e.g., VNPay, MoMo, Stripe) and send a POST request to the server to lock the booking in the database.

4. **User Dashboard (History):**
   - In `HistoryActivity.java`, map the `ListView` or `RecyclerView` adapter to a GET endpoint fetching the user's historical transaction records.

---
*Confidential and Proprietary - Ready for Commercial Deployment*

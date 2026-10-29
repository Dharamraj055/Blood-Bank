# 🩸 Blood Bank - Full Stack System (BloodRadar)

Comprehensive Blood Bank and Donation Management application with a **Native Android Client (BloodRadar)** and a **Node.js REST API Backend** connected to MongoDB.

---

## 📁 Project Structure

```
├── backend/               # Node.js + Express + MongoDB REST API
│   ├── src/
│   │   ├── config/        # Database & app configuration
│   │   ├── controllers/   # Route handlers (Auth, Donors, Requests, Donations, Admin)
│   │   ├── middleware/    # Auth and file upload middleware
│   │   ├── models/        # Mongoose data models
│   │   └── routes/        # API endpoints
│   ├── package.json
│   └── start-server.js    # Auto embedded MongoDB server runner
│
├── BloodRadar/            # Native Android Mobile App (Java / Material UI)
│   ├── app/
│   │   ├── src/main/java/ # Android activities, adapters & API client
│   │   └── src/main/res/  # UI layouts, drawables & resources
│   └── build.gradle.kts
│
└── README.md
```

---

## 🚀 Getting Started

### 1. Backend Setup & Run

Navigate to `backend` directory and install dependencies:
```bash
cd backend
npm install
npm run dev
```
The backend starts on `http://localhost:3000` with MongoDB automatically connected.

### 2. Android App (BloodRadar) Setup & Run

1. Open `BloodRadar` in **Android Studio**.
2. Make sure the API Base URL in `app/src/main/java/com/abhay/bloodradar/api/AppConfig.java` points to:
   - `http://10.0.2.2:3000/api` for Android Emulator
   - `http://localhost:3000/api` for Phone via USB (`adb reverse tcp:3000 tcp:3000`)
   - `http://<YOUR_LOCAL_IP>:3000/api` for Phone on same Wi-Fi
3. Build and run on an Android Device or Emulator.

---

## 🛠️ Features

- 👤 User Registration & JWT Authentication (with OTP support)
- 🩸 Find Blood Donors by Blood Group & Location
- 📝 Create and Manage Blood Requests with Prescription/Image Uploads
- 💬 Real-Time In-App Chat between Donors and Requesters
- 🔔 Notification Broadcast System
- 🛡️ Comprehensive Admin Dashboard & Analytics


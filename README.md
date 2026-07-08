# ESIC MedStock

ESIC MedStock is a smart pharmacy assistance Android application designed to help ESIC patients check prescribed medicine availability and pharmacy queue information.

The project aims to reduce unnecessary waiting time at hospital pharmacies, where patients may wait for a long time without knowing whether their prescribed medicines are available.

## Features

- Check prescribed medicine availability
- View current medicine stock status
- Track pharmacy queue and token information
- Support for multiple ESIC hospital branches
- Real-time data retrieval from Cloud Firestore
- Centralized prescription and inventory data

## Tech Stack

- **Frontend:** Kotlin, Jetpack Compose
- **Backend Service:** Firebase
- **Database:** Cloud Firestore
- **Integration:** Firebase Android SDK
- **Development:** Android Studio, Gradle
- **Version Control:** Git and GitHub
- **Development Approach:** AI-assisted development

## Architecture

Android App → Firebase Android SDK → Cloud Firestore

The application uses Firestore collections for inventory, prescriptions, queue information, hospital status, and notifications.

## Future Scope

The project can be extended with OTP authentication, secure backend APIs, medicine restock notifications, predictive availability, and improved QR-based prescription verification.

> This project is an educational prototype and is not an official ESIC application.

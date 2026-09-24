# Resume ATS Checker

An AI-assisted Applicant Tracking System (ATS) designed to help HR teams and recruiters upload, manage, analyze, rank, and shortlist candidate resumes against job descriptions.

The system is built as a web-based application using **Java Spring Boot**, **PostgreSQL**, **Supabase Storage**, HTML/CSS/JavaScript, and AI-powered resume processing.

---

## 🚀 Project Overview

The Resume ATS Checker simplifies the recruitment screening process by automatically analyzing candidate resumes against uploaded Job Descriptions (JDs).

Instead of manually reviewing hundreds or thousands of resumes, recruiters can upload resumes in bulk and receive ATS-based matching results, including:

* Candidate information
* Skills detected from resumes
* Skills required by the Job Description
* Matching skills
* Missing skills
* ATS score
* Candidate ranking
* Shortlisting information
* Resume access management
* Job Description management

The application also supports different user roles such as **Admin, HR, and Recruiter**, with role-based access to different features.

---

## 🎯 Objectives

The main objectives of this project are:

1. Automate the initial resume screening process.
2. Reduce the time required for manual resume evaluation.
3. Match candidate resumes against Job Descriptions.
4. Rank candidates based on their ATS score.
5. Support bulk resume uploads.
6. Prevent duplicate resumes and Job Descriptions.
7. Store parsed resume information for future matching.
8. Allow recruiters to manage multiple Job Descriptions.
9. Re-evaluate existing resumes when a new Job Description is uploaded.
10. Provide secure resume access using a credit-based wallet system.
11. Provide persistent cloud storage for uploaded resume and JD files.
12. Provide an administrative dashboard for managing users and permissions.

---

## ✨ Key Features

### 👤 User Authentication

The system supports authentication and role-based access.

Supported roles include:

* **ADMIN**
* **HR**
* **RECRUITER**

Each role receives access to features according to its permissions.

---

### 📄 Resume Upload

Users can upload individual PDF resumes through the application.

The system:

1. Receives the uploaded PDF.
2. Extracts the resume text.
3. Identifies candidate information.
4. Extracts relevant skills.
5. Stores the parsed information in PostgreSQL.
6. Stores the original PDF in cloud storage.
7. Makes the resume available for ATS matching.

---

### 📦 Bulk Resume Upload

The application supports bulk resume processing.

Recruiters/HR users can upload multiple resumes, including ZIP-based bulk uploads.

The system processes the resumes automatically and stores the parsed information.

This allows recruitment teams to process a large candidate pool without manually uploading every resume individually.

---

### 🔍 Resume Parsing

The system extracts useful information from resumes, including:

* Candidate name
* Email
* Phone number
* Skills
* Resume text
* File information

PDF text extraction is handled on the backend before ATS processing.

---

### 💼 Job Description Management

Users can upload Job Descriptions in PDF format.

The system stores information such as:

* Job title
* Required skills
* Required experience
* Job Description text
* File information
* Active/inactive status

The application also maintains a Job Description library where previously uploaded JDs can be accessed.

---

### 🔄 Dynamic Job Description Matching

One of the main features of the application is dynamic ATS matching.

When a new Job Description is uploaded:

```text
New Job Description
        ↓
Extract JD text
        ↓
Extract required skills
        ↓
Compare with stored resumes
        ↓
Calculate ATS scores
        ↓
Rank candidates
        ↓
Display results
```

Existing parsed resumes can therefore be evaluated against the latest Job Description without requiring the resumes to be uploaded again.

---

### 📊 ATS Scoring

The ATS engine compares the skills detected in candidate resumes with the skills required by the selected Job Description.

The results include:

* Matched skills
* Missing skills
* ATS score
* Candidate ranking

Example:

| Candidate   | ATS Score | Matched Skills         | Missing Skills           |
| ----------- | --------: | ---------------------- | ------------------------ |
| Candidate A |       92% | Java, Spring Boot, SQL | Docker                   |
| Candidate B |       84% | Java, SQL              | Spring Boot, Docker      |
| Candidate C |       71% | Java                   | Spring Boot, SQL, Docker |

The exact scoring logic is handled by the backend ATS service.

---

### 🏆 Candidate Ranking

Candidates are automatically ranked according to their ATS results.

This allows recruiters to quickly identify candidates whose resumes most closely match the requirements of a particular Job Description.

---

### 🧑‍💼 Admin Dashboard

Administrators can manage users and system permissions.

Admin functionality includes:

* View registered users
* View user roles
* Manage recruiter/HR permissions
* Manage Excel access
* Manage wallet credits
* Access administrative reports
* Monitor system users

---

### 💳 Credit-Based Resume Access

The application includes a wallet-based access system.

For non-admin users:

```text
First access to a resume
        ↓
50 credits deducted
        ↓
Resume unlocked
        ↓
Future access
        ↓
No additional charge
```

Once a recruiter has unlocked a particular resume, they can access it again without being charged for that same resume.

Administrators have unrestricted resume access.

---

### 📑 Excel Integration

The system supports Excel-based recruitment workflows.

Authorized users can use Excel functionality for importing or working with candidate information.

Excel-related functionality is permission-controlled and can be enabled by an administrator.

---

### 🤖 AI-Assisted Resume Features

The application also includes AI-assisted functionality for resume-related tasks.

The system can integrate AI capabilities for generating or improving resume content and supporting recruitment workflows.

AI functionality is designed to complement the ATS rather than replace the core ATS matching engine.

---

### ☁️ Cloud File Storage

Uploaded resume and Job Description PDFs are designed to be stored separately from the PostgreSQL database.

The project uses **Supabase Storage** for persistent file storage.

Supabase Storage supports standard file storage as well as S3-compatible access, allowing the backend to interact with stored files using compatible storage clients.

The architecture is:

```text
                    Resume ATS
                        │
                ┌───────┴────────┐
                │                │
                ▼                ▼
          PostgreSQL       Supabase Storage
                │                │
        Parsed resume data    PDF files
        ATS results           Resume PDFs
        Users                 JD PDFs
        Wallet data
```

Keeping the actual PDF files outside the relational database makes the application easier to scale and maintain. Supabase's documentation also recommends storing files outside the database because of their size.

---

## 🏗️ System Architecture

```text
                         ┌─────────────────────┐
                         │     Web Browser     │
                         │ HTML / CSS / JS     │
                         └──────────┬──────────┘
                                    │
                                    ▼
                         ┌─────────────────────┐
                         │    Spring Boot      │
                         │     REST API        │
                         └──────────┬──────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              │                     │                     │
              ▼                     ▼                     ▼
       ┌─────────────┐      ┌───────────────┐     ┌──────────────┐
       │ PostgreSQL  │      │ ATS Processing │     │  Supabase    │
       │  Database   │      │    Engine      │     │   Storage    │
       └─────────────┘      └───────────────┘     └──────────────┘
              │                     │                     │
              ▼                     ▼                     ▼
        Users / Resume        Skill Matching        PDF Files
        JD / ATS Results      ATS Score             Resume Files
        Wallet / Access       Ranking                JD Files
```

---

## 🛠️ Technology Stack

### Backend

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Hibernate
* Maven
* Apache PDFBox

Spring Boot supports Java 17+ and is compatible with Java 21, which is used by this project.

### Database

* PostgreSQL

### Cloud Storage

* Supabase Storage
* S3-compatible storage interface

### Frontend

* HTML5
* CSS3
* JavaScript

### AI / NLP

* Gemini API integration
* Resume text processing
* Skill extraction
* AI-assisted resume functionality

### Deployment

* Docker
* Render
* GitHub

---

## 📁 Project Structure

```text
resume-ats/
│
├── .mvn/
│   └── wrapper/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ...
│   │   │
│   │   └── resources/
│   │       ├── static/
│   │       │   ├── index.html
│   │       │   ├── dashboard.html
│   │       │   ├── css/
│   │       │   └── js/
│   │       │
│   │       └── application.properties
│   │
│   └── test/
│
├── Dockerfile
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

## 🔐 Security

The application includes several security-related mechanisms:

* Role-based access
* Authenticated resume access
* Admin-only functionality
* Credit-based resume access
* Private cloud file storage
* Environment variables for sensitive configuration
* Server-side API credentials
* Database-backed access control

### Important

Sensitive credentials should **never** be committed to GitHub.

Do not commit:

```text
.env
API keys
Database passwords
Supabase secret keys
Gemini API keys
Access tokens
Private credentials
```

Use environment variables for production deployments.

---

## ⚙️ Environment Variables

Example configuration:

```properties
DB_URL=jdbc:postgresql://<HOST>:<PORT>/<DATABASE>
DB_USERNAME=<DATABASE_USERNAME>
DB_PASSWORD=<DATABASE_PASSWORD>

GEMINI_API_KEY=<GEMINI_API_KEY>

SUPABASE_URL=<SUPABASE_PROJECT_URL>
SUPABASE_ACCESS_KEY=<SUPABASE_ACCESS_KEY>
SUPABASE_SECRET_KEY=<SUPABASE_SECRET_KEY>
SUPABASE_BUCKET=<STORAGE_BUCKET_NAME>
```

**Never replace these placeholders with real credentials in the README.**

Supabase's S3 access keys provide broad access to Storage and are intended for server-side use, so they should remain on the backend and never be exposed in frontend JavaScript.

---

## ▶️ Running the Project Locally

### 1. Clone the repository

```bash
git clone https://github.com/Jithukrishnaa/resume-ats.git
```

```bash
cd resume-ats
```

### 2. Configure PostgreSQL

Create a PostgreSQL database and configure the required environment variables.

Example:

```properties
DB_URL=jdbc:postgresql://localhost:5432/ats_system
DB_USERNAME=postgres
DB_PASSWORD=your_password
```

### 3. Configure Storage

Create a Supabase project and configure a private storage bucket for:

```text
Resumes
Job Descriptions
```

Supabase Storage buckets can be created directly from the Supabase Dashboard.

### 4. Configure AI

If AI-assisted features are enabled, configure:

```properties
GEMINI_API_KEY=your_api_key
```

### 5. Run the application

Using Maven Wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw spring-boot:run
```

The application will normally be available at:

```text
http://localhost:8080
```

---

## 🐳 Running with Docker

Build the Docker image:

```bash
docker build -t resume-ats .
```

Run the container:

```bash
docker run -p 8080:8080 resume-ats
```

Production environment variables should be provided through the deployment platform rather than hard-coded into the Docker image.

---

## 🌐 Deployment

The application is designed to be deployed using:

```text
GitHub
   ↓
Docker
   ↓
Render
   ↓
Spring Boot Application
```

Persistent data is maintained through:

```text
PostgreSQL → Application Data
Supabase Storage → PDF Files
```

This avoids depending on the temporary local filesystem of the application server for permanent uploaded documents.

---

## 🔄 Recruitment Workflow

```text
Recruiter / HR Login
        ↓
Upload Job Description
        ↓
Extract JD Information
        ↓
Upload Candidate Resumes
        ↓
Parse Resume Data
        ↓
Store Candidate Information
        ↓
Compare Resume With JD
        ↓
Calculate ATS Score
        ↓
Rank Candidates
        ↓
Shortlist Candidates
        ↓
View Candidate Resume
```

---

## 📌 Duplicate Detection

The system is designed to prevent duplicate data from unnecessarily entering the candidate repository.

Duplicate detection can be applied to:

* Candidate resumes
* Resume information
* Job Descriptions

This helps maintain a cleaner candidate database and avoids repeated processing.

---

## 📈 Scalability

The system is designed with a separation between:

```text
Application Logic
Database
File Storage
```

This makes it possible to scale the individual components independently.

For example:

```text
                    Load / Traffic
                         │
                         ▼
                  Spring Boot API
                         │
             ┌───────────┴───────────┐
             ▼                       ▼
        PostgreSQL              File Storage
                                  │
                                  ▼
                             Resume PDFs
```

For larger production deployments, additional improvements can include:

* Background resume processing
* Queue-based processing
* Batch processing
* Search indexing
* Caching
* Dedicated object storage
* Database indexing
* Horizontal application scaling

---

## 🔮 Future Improvements

Planned or possible future enhancements include:

* Advanced semantic resume matching
* Embedding-based candidate search
* RAG-based recruitment assistant
* Improved NLP skill extraction
* Experience matching
* Education matching
* Job-role recommendations
* Candidate similarity search
* Automated candidate shortlisting
* Email integration
* Recruiter analytics
* Recruitment funnel analytics
* Advanced admin reporting
* Multi-company/tenant support
* Cloudflare R2 or other production-grade object storage migration
* Background processing for large resume batches

---

## 👨‍💻 Intended Users

The system is primarily designed for:

* HR teams
* Recruiters
* Recruitment agencies
* Hiring managers
* Organizations handling large numbers of resumes

---

## 📊 Example ATS Result

```text
Job Description:
Java Developer

Candidate:
John Doe

ATS Score:
89%

Matched Skills:
✓ Java
✓ Spring Boot
✓ SQL
✓ REST API
✓ Git

Missing Skills:
✗ Docker

Ranking:
#3
```

---

## 🎓 Project Purpose

This project was developed as a practical recruitment technology solution demonstrating the use of:

* Backend development
* REST APIs
* Database management
* Resume parsing
* Document processing
* ATS algorithms
* Cloud storage
* Authentication
* Role-based authorization
* AI-assisted functionality
* Docker deployment
* Cloud deployment

---

## 📜 License

This project is intended for educational, prototype, and demonstration purposes.

Add an appropriate open-source license if this repository is intended to be distributed publicly.

---

## 👤 Author

**Jithu Krishnaa**

GitHub:
https://github.com/Jithukrishnaa

---

## ⭐ Project Status

**Status: Prototype / Active Development**

The application is currently being developed and enhanced with additional ATS, recruitment, AI, cloud storage, and administrative features.

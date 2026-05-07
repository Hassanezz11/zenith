# Zenith - Setup Instructions

Welcome to Zenith! This project is built with **JavaFX** and uses **Maven** for build management. Follow these instructions to get the application running on your computer.

---

## Prerequisites

Before cloning and setting up the project, please ensure your system has the following installed:

### 1. Java Development Kit (JDK) 21

- **Download from:**
  - Oracle: https://www.oracle.com/java/technologies/downloads/#java21
  - Adoptium: https://adoptium.net
  
- **Verify installation:**
  ```bash
  java -version
  javac -version
  ```
  Both should show version 21 or higher.

### 2. Git

- **Download from:** https://git-scm.com/
- **Verify installation:**
  ```bash
  git --version
  ```

### 3. IDE (Recommended - Choose One)

- **IntelliJ IDEA** (Community or Professional)
  - Download: https://www.jetbrains.com/idea/download/
  
- **Visual Studio Code**
  - Download: https://code.visualstudio.com/
  - Install "Extension Pack for Java" from the marketplace
  
- **Eclipse IDE**
  - Download: https://www.eclipse.org/
  - Must have Java Development Tools (JDT)

---

## Setup Steps

### Step 1: Clone the Repository

```bash
git clone [<repository-url>](https://github.com/Hassanezz11/zenith.git)
cd Zenith
```


### Step 2: Build the Project

The project uses Maven Wrapper, so you don't need to install Maven separately.

**On Windows:**
```bash
mvnw.cmd clean install
```

**On macOS/Linux:**
```bash
./mvnw clean install
```

This will:
- Download all dependencies (may take a few minutes on first build)
- Compile all modules
- Run tests
- Package the application

### Step 3: Run the Application

**Using Maven (Recommended):**

Windows:
```bash
mvnw.cmd exec:java@sampler
```

macOS/Linux:
```bash
./mvnw exec:java@sampler
```

**Using IDE:**

**IntelliJ IDEA:**
1. Open the project folder
2. Navigate to `sampler/src/main/java/atlantafx/sampler/Sampler.java`
3. Right-click and select "Run Sampler"
4. Or press `Shift + F10` (Windows) / `Ctrl + R` (macOS)

**VS Code:**
1. Open the project folder
2. Open `sampler/src/main/java/atlantafx/sampler/Sampler.java`
3. Click "Run" button or press `Ctrl + Shift + D`

---

## Project Structure

```
Zenith/
├── base/              - Core library with themes and styling
├── sampler/           - Demo application (main app)
├── decorations/       - Window decoration styles
├── styles/            - SCSS stylesheets for themes
├── docs/              - Documentation
├── pom.xml            - Parent Maven configuration
└── README.md          - This file
```

---

## Development Workflow

### Setting Up Your IDE

**IntelliJ IDEA:**
1. File → Project Structure → Project
2. Set Project SDK to JDK 21
3. Set Project Language level to 21
4. Enable "Automatically sync files" in Settings

**VS Code:**
Create or edit `.vscode/settings.json`:
```json
{
  "[java]": {
    "editor.defaultFormatter": "redhat.java",
    "editor.formatOnSave": true
  },
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-21",
      "path": "/path/to/java-21"
    }
  ]
}
```

### Making Changes

1. Open the project in your IDE (not just a folder)
2. Make your code changes
3. Build: `mvnw clean install`
4. Test your changes by running the app

---

---

## Common Issues & Troubleshooting

### Java Version Not Recognized

**Solution:**
- Ensure Java 21 is installed
- Check `java -version` returns Java 21

**Windows:**
- Add Java bin directory to your system PATH:
  1. Right-click "This PC" → Properties
  2. Click "Advanced system settings"
  3. Click "Environment Variables"
  4. Add Java 21 bin folder to PATH

- Or set JAVA_HOME:
  ```bash
  set JAVA_HOME=C:\Program Files\Java\jdk-21
  ```

**macOS/Linux:**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Build Fails with "Module Not Found"

**Solution:**
```bash
# Clear cache and rebuild
mvnw clean install -U
```

If issue persists:
1. Delete the `target/` directories
2. Delete `.m2/repository` (optional, will redownload all dependencies)
3. Run build again

### IDE Doesn't Recognize Maven Project

**IntelliJ IDEA:**
1. Right-click project root
2. Select "Configure" → "Convert to Maven Project"

**VS Code:**
1. Press `Ctrl+Shift+P` (Windows/Linux) or `Cmd+Shift+P` (macOS)
2. Type "Java: Clean Language Server Workspace"
3. Reload the window

### Application Fails to Start

**Solution:**
1. Verify Java 21 is set as your IDE's default JDK
2. Rebuild: `mvnw clean install`
3. Invalidate IDE cache and restart

---

## Pushing Your Work

When you're ready to push your changes to the team:

```bash
# Create a feature branch
git checkout -b feature/your-feature-name

# Make your changes, then stage and commit
git add .
git commit -m "Describe your changes clearly"

# Push to remote
git push -u origin feature/your-feature-name
```

Then create a Pull Request on GitHub for your team to review.

---

## Need Help?

- Review the error messages in the console
- Check that all prerequisites are installed correctly
- Make sure you're using Java 21
- Try rebuilding the project
- Ask your team lead for assistance

---

**Happy coding! 🚀**

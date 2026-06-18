# Agent Workspace Rules

## Workspace Boundary

The workspace root is the security boundary.

The agent must only access files and directories inside the current workspace.

The agent must not:

* Read files outside the workspace.
* Search outside the workspace.
* Modify files outside the workspace.
* Create files outside the workspace.
* Delete files outside the workspace.
* Move or copy files outside the workspace.

Forbidden examples:

* `../`
* `../../`
* `~/`
* `/Users`
* `/home`
* `/root`
* `/Desktop`
* `/Downloads`
* Any parent directory of the workspace.

---

## MCP Usage Rules

The agent may only use MCP servers explicitly configured for this workspace.

Allowed MCP servers:

* Workspace filesystem MCP
* Repository-scoped GitHub MCP

The agent must not use:

* Global filesystem MCP
* Browser MCP
* Shell MCP with unrestricted access
* Personal file MCP
* Any MCP that can access resources outside the workspace

unless explicitly approved by the user.

---

## Skill Priority

Before answering or generating code, the agent must follow this order:

1. Workspace skills
2. Project documentation
3. Existing source code
4. Configured MCP tools
5. General knowledge

The agent should always prefer existing project patterns over introducing new patterns.

---

## Code Generation Rules

Before creating new code:

1. Search for existing implementations.
2. Reuse existing architecture.
3. Follow project conventions.
4. Avoid duplicate abstractions.

The agent must not introduce:

* New frameworks
* New architectural patterns
* New networking libraries
* New dependency injection frameworks

without explicit approval.

---

## Security Rules

The agent must never expose or upload:

* API keys
* Access tokens
* Secrets
* Passwords
* Certificates
* Keystore files

Sensitive files include:

* `.env`
* `.env.*`
* `local.properties`
* `keystore.properties`
* `*.jks`
* `*.keystore`
* `google-services.json`

unless explicitly requested by the user.

---

## Android Project Rules

Preferred technology stack:

* Kotlin
* Jetpack Compose
* Coroutines
* Flow
* ViewModel
* MVI
* Hilt
* Retrofit
* Room
* Gradle Kotlin DSL

The agent should follow existing project architecture.

---

## Command Execution Rules

Allowed commands:

* `./gradlew build`
* `./gradlew test`
* `./gradlew lint`
* `./gradlew assembleDebug`
* `./gradlew assembleRelease`

Forbidden commands:

* `rm -rf /`
* `rm -rf ~`
* `sudo *`
* `chmod -R *`
* Any destructive command without user approval.

---

## Conflict Resolution

If a task requires access outside the workspace:

1. Stop immediately.
2. Explain why access is required.
3. Ask for explicit user approval.

Never bypass workspace boundaries.

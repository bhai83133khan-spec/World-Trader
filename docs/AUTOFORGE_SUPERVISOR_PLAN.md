# AutoForge AI Telegram Supervisor — Implementation Plan

## Goal
Make the user's Telegram bot (AutoForge AI) the single control point for monitoring and operating GitHub projects. The user should not need to open ChatGPT, Gemini, or OpenCode manually for routine tasks.

## Current verified state
- Telegram bot exists: @AutoForgeProjectBot.
- GitHub repository: bhai83133khan-spec/World-Trader.
- Repository secrets visible by name in screenshots: TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID (secret values are never read or printed).
- World-Trader APK workflow can build a debug APK successfully.
- Current World-Trader workflow builds/tests the Android app; it does not run a Telegram supervisor or autonomous coding agent.

## Target architecture
1. Telegram interface: command router and owner/chat allow-list; respond only to the configured owner chat.
2. Project monitor: check GitHub commits, open PRs/issues, workflow runs, failed checks, artifacts/releases.
3. Task queue: accept a task, record it, send acknowledgment, track state, and return a final report.
4. AI planner/reviewer adapters: optional provider adapters for OpenAI and Gemini; no provider keys are embedded in code.
5. Coding executor: an OpenCode-compatible runner in an isolated job/workspace; task execution must be branch-based, never direct-to-main.
6. CI verification: run tests/build, collect logs, and report results. Create a PR with a summary instead of merging automatically.
7. Safety: allow-listed repositories, owner-only Telegram commands, secret redaction, command rate limits, bounded job duration, no destructive actions without confirmation, and audit logs.

## Phased delivery
- Phase 0 — verified configuration and architecture (this document).
- Phase 1 — Telegram bot command handling and owner/chat authentication; /start, /help, /status, /projects, /report, /build.
- Phase 2 — GitHub monitoring and concise Telegram notifications for workflow failures/successes and releases.
- Phase 3 — task queue and automated AI planning/review (provider configuration required).
- Phase 4 — isolated OpenCode execution, tests/build, branch and pull-request creation (runner/provider credentials and permissions required).
- Phase 5 — reliability hardening, recovery, deduplication, logs, and documentation.

## Commands (target)
- /start — initialize and show available commands
- /help — help
- /status — status of the configured project(s)
- /projects — monitored repository list
- /report — latest concise progress report
- /build — latest CI/build status and artifact information
- /task <description> — queue a coding task (phases 3–4)
- /cancel <task-id> — cancel a queued/running task where supported
- /agents — report agent/provider availability (no secrets shown)

## Requirements / blockers
- Telegram bot token and owner chat ID are configured as repository secrets.
- OpenAI/Gemini API credentials or other provider authorization are required for AI planning/review; these cannot be conjured from Telegram setup.
- OpenCode execution requires a hosted/runner environment and provider authorization; a GitHub Actions workflow is not a persistent always-on server.
- The current repository is an Android game. Supervisor code should ultimately be separated into a dedicated repository/service to avoid mixing bot infrastructure with game code.
- Do not expose, echo, or commit secret values. If the Telegram token is ever exposed, revoke and regenerate it through BotFather.

## Acceptance criteria
- Telegram commands respond only to the configured owner.
- Monitoring commands return data from the GitHub API and Actions.
- Task lifecycle is visible (queued, planning, coding, tests, PR, failed/cancelled).
- Coding agent changes are isolated on a branch and submitted as a PR.
- CI results are verified before claiming completion.
- APK artifact links are reported only when they exist and are accessible.
- User receives concise Urdu/Roman Urdu status messages and does not need to manually operate provider tools for routine tasks.

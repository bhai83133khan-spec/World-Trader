# AutoForge Telegram Supervisor — Phase 1

This folder contains a starter command shell for `@AutoForgeProjectBot`.

## What is implemented
- Owner-only Telegram command authorization using `TELEGRAM_CHAT_ID`
- `/start`, `/help`, `/status`, `/projects`, `/report`, and `/build` command handlers
- Basic runtime configuration from environment variables
- Unit tests for owner authorization

## Not implemented yet
- GitHub project monitoring
- Persistent project/task registry
- Autonomous resource discovery
- AI planning/coding provider adapters
- OpenCode/Copilot/Gemini/OpenAI failover
- APK build orchestration or artifact delivery

## Run
Use Python 3.11+:
```bash
pip install -r requirements.txt
cp .env.example .env
# Set secrets in .env (never commit .env)
python autoforge_bot.py
```

## Security
The bot only accepts commands from the configured numeric chat ID. Keep token and owner ID out of source control. Rotate any token that has been exposed.

## Repo note
This bootstrap is placed in a dedicated `autoforge/` folder and branch within World-Trader for initial development only. It should later be moved to a dedicated AutoForge repository/service.

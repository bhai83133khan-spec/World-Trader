"""AutoForge Telegram supervisor bootstrap (Phase 1 command shell)."""
import os
from telegram import Update
from telegram.ext import Application, CommandHandler, ContextTypes

OWNER_CHAT_ID = os.getenv("TELEGRAM_CHAT_ID", "").strip()
TOKEN = os.getenv("TELEGRAM_BOT_TOKEN", "").strip()

COMMANDS = (
    "/start - Start AutoForge",
    "/status - Check supervisor status",
    "/projects - List monitored projects",
    "/report - Progress report",
    "/build - Latest build status",
    "/task <description> - Queue a coding task (not enabled yet)",
    "/help - Show help",
)

def is_owner(update: Update) -> bool:
    chat = update.effective_chat
    return bool(chat and OWNER_CHAT_ID and str(chat.id) == OWNER_CHAT_ID)

async def guarded_reply(update: Update, text: str) -> None:
    if not is_owner(update):
        if update.message:
            await update.message.reply_text("Access denied.")
        return
    if update.message:
        await update.message.reply_text(text)

async def start(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    await guarded_reply(update, "AutoForge Phase 1 is online. This is the command shell only; project monitoring and autonomous coding are not yet enabled.\n\n" + "\n".join(COMMANDS))

async def help_cmd(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    await guarded_reply(update, "Available commands:\n" + "\n".join(COMMANDS))

async def status(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    await guarded_reply(update, "Bot backend is running. GitHub monitoring and AI task execution are not yet connected.")

async def projects(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    await guarded_reply(update, "No project registry is configured yet. Project registration is planned for the next phase.")

async def report(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    await guarded_reply(update, "AutoForge report: Phase 1 command shell. No autonomous tasks have been executed yet.")

async def build(update: Update, context: ContextTypes.DEFAULT_TYPE) -> None:
    await guarded_reply(update, "Build tracking is not connected yet. This command will be wired to GitHub Actions in Phase 2.")

def build_application(token: str) -> Application:
    app = Application.builder().token(token).build()
    app.add_handler(CommandHandler("start", start))
    app.add_handler(CommandHandler("help", help_cmd))
    app.add_handler(CommandHandler("status", status))
    app.add_handler(CommandHandler("projects", projects))
    app.add_handler(CommandHandler("report", report))
    app.add_handler(CommandHandler("build", build))
    return app

def main() -> None:
    if not TOKEN:
        raise RuntimeError("TELEGRAM_BOT_TOKEN is missing")
    if not OWNER_CHAT_ID or not OWNER_CHAT_ID.isdigit():
        raise RuntimeError("TELEGRAM_CHAT_ID must be the numeric owner chat ID")
    build_application(TOKEN).run_polling(drop_pending_updates=True)

if __name__ == "__main__":
    main()

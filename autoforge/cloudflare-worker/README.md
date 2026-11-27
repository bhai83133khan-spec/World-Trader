# AutoForge Telegram Webhook

This is a Cloudflare Worker webhook backend. It handles Telegram updates, owner-only authorization, /start, /help, /status, /task, /research, /report, and free-text AI replies via OpenRouter.

## Secrets/variables (Cloudflare Worker Settings > Variables and Secrets)
- `TELEGRAM_BOT_TOKEN` (secret)
- `OPENROUTER_API_KEY` (secret)
- `TELEGRAM_WEBHOOK_SECRET` (secret; generate a random string and use it when registering webhook)
- `OPENROUTER_MODEL` (optional plain-text variable; defaults to openrouter/auto)

## Deploy
Install Wrangler and authenticate to Cloudflare, then:
```
npx wrangler deploy
```
Copy the deployed Worker URL, then register the Telegram webhook (substitute values):
```
curl -X POST "https://api.telegram.org/bot<TOKEN>/setWebhook" -d "url=<WORKER_URL>" -d "secret_token=<WEBHOOK_SECRET>"
```
Do not commit tokens.

## Honest capability boundary
The Worker supports AI-assisted task/research responses. It does not yet autonomously create/commit a multi-file repo, run tests/builds, or deliver APKs. Those require GitHub write access and a separate runner/workflow integration.
// AutoForge Telegram webhook worker (Cloudflare Workers)
const OWNER_CHAT_ID = "8688729069";
const SYSTEM_PROMPT = `You are AutoForge, a pragmatic project-building assistant. Given a project request, produce a concise project plan, research avenues, architecture, first implementation scaffold, and concrete next steps. Never claim to have tested, deployed, or built files unless a tool did it. Keep code output organized by filename and use free/open-source options when requested. Ask only one essential clarifying question if needed.`;

async function telegram(method, payload, env) {
  const r = await fetch(`https://api.telegram.org/bot${env.TELEGRAM_BOT_TOKEN}/${method}`, {
    method: "POST", headers: {"content-type":"application/json"}, body: JSON.stringify(payload)
  });
  return r.json();
}
async function send(chatId, text, env) {
  // Telegram messages have a 4096-character limit; chunk safely.
  for (let i=0;i<text.length;i+=3800) {
    await telegram("sendMessage", {chat_id:chatId, text:text.slice(i,i+3800)}, env);
  }
}
async function askAI(prompt, env) {
  const r=await fetch("https://openrouter.ai/api/v1/chat/completions",{
    method:"POST", headers:{
      "Authorization":`Bearer ${env.OPENROUTER_API_KEY}`,
      "Content-Type":"application/json",
      "HTTP-Referer":"https://autoforge-worker.local",
      "X-Title":"AutoForge"
    },
    body:JSON.stringify({model:env.OPENROUTER_MODEL || "openrouter/auto", messages:[
      {role:"system",content:SYSTEM_PROMPT},{role:"user",content:prompt}
    ], temperature:0.3})
  });
  if(!r.ok) throw new Error(`OpenRouter HTTP ${r.status}: ${await r.text()}`);
  const d=await r.json(); return d.choices?.[0]?.message?.content || "No AI response returned.";
}
export default {
 async fetch(request, env) {
  if(request.method==="GET") return new Response("AutoForge webhook alive. Telegram webhook endpoint is ready.", {status:200});
  if(request.method!=="POST") return new Response("Method not allowed",{status:405});
  if(env.TELEGRAM_WEBHOOK_SECRET && request.headers.get("X-Telegram-Bot-Api-Secret-Token")!==env.TELEGRAM_WEBHOOK_SECRET) return new Response("Unauthorized",{status:401});
  let update; try { update=await request.json(); } catch { return new Response("Bad JSON",{status:400});}
  const msg=update.message; if(!msg?.text) return new Response("OK");
  const chatId=String(msg.chat?.id || ""); if(chatId!==OWNER_CHAT_ID) { await send(chatId,"Access denied.",env); return new Response("OK");}
  const text=msg.text.trim(); const cmd=text.split(/\\s+/)[0].toLowerCase();
  if(cmd==="/start"||cmd==="/help"){
    await send(chatId,"AutoForge is connected to its AI webhook.\n\nCommands:\n/status – backend status\n/task <project idea> – AI project planning, research direction, architecture, starter code\n/research <topic> – research brief\n/report – capability/status report\n/pause – stop current chat response (no background jobs currently)\n\nNote: autonomous GitHub commits/build/APK delivery are not enabled until GitHub write integration and runner are configured.",env);
  } else if(cmd==="/status") {
    await send(chatId,"AutoForge webhook: online\nAI provider: OpenRouter configured in hosting secrets\nOwner authentication: enabled\nTask execution: AI responses enabled\nBackground coding/build/repository automation: not connected yet.",env);
  } else if(cmd==="/report") {
    await send(chatId,"AutoForge status report:\n✓ Telegram webhook handler\n✓ Owner-only access\n✓ OpenRouter chat integration\n✓ /task and /research handlers\n✗ Persistent task queue\n✗ Repository commit/push automation\n✗ Android build orchestration/APK delivery\n\nThese remaining features require GitHub integration and a runner.",env);
  } else if(cmd==="/task"||cmd==="/research") {
    const prompt=text.replace(/^\\/(task|research)\\s*/i,"").trim();
    if(!prompt){await send(chatId,cmd===" /task"?"Send: /task <idea>":"Send a topic after /research",env); return new Response("OK");}
    await send(chatId,"Received. Asking AI to process your request…",env);
    try { const answer=await askAI((cmd==="/research"?"Research brief for: ":"Project task: ")+prompt,env); await send(chatId,answer,env);}
    catch(e){await send(chatId,"AI request failed: "+String(e.message||e).slice(0,900),env);}
  } else {
    try { const answer=await askAI("User message: "+text,env); await send(chatId,answer,env);}
    catch(e){await send(chatId,"AI error: "+String(e.message||e).slice(0,700),env);}
  }
  return new Response("OK");
 }
};

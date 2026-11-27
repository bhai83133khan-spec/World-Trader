import { test } from "node:test"; import assert from "node:assert/strict"; import worker from "../src/index.js";
test("GET health endpoint responds", async()=>{ const res=await worker.fetch(new Request("https://test.local/"),{}); assert.equal(res.status,200); assert.match(await res.text(),/webhook alive/i); });
test("POST is accepted and malformed json rejected", async()=>{ const res=await worker.fetch(new Request("https://test.local/",{method:"POST",body:"not-json"}),{}); assert.equal(res.status,400); });
test("non-POST unsupported method", async()=>{ const res=await worker.fetch(new Request("https://test.local/",{method:"PUT"}),{}); assert.equal(res.status,405); });

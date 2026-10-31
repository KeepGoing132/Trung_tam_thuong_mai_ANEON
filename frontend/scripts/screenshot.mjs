/**
 * Chụp ảnh màn hình trang web bằng Edge (puppeteer-core) để kiểm tra
 * giao diện: desktop full-page, hero, và mobile.
 * Dùng: node scripts/screenshot.mjs [url] [outDir]
 */
import puppeteer from "puppeteer-core";
import { mkdirSync } from "node:fs";
import { join } from "node:path";

const url = process.argv[2] ?? "http://localhost:3000/";
const outDir = process.argv[3] ?? "../shots";
const edgePath =
  "C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe";

mkdirSync(outDir, { recursive: true });

const browser = await puppeteer.launch({
  executablePath: edgePath,
  headless: "new",
  args: ["--no-first-run", "--disable-gpu"],
});

async function shoot(name, viewport, { fullPage = false, settleMs = 2200 } = {}) {
  const page = await browser.newPage();
  await page.setViewport(viewport);
  await page.goto(url, { waitUntil: "networkidle2", timeout: 60000 });
  // Cuộn nhẹ qua trang để kích hoạt whileInView, rồi về đầu
  await page.evaluate(async () => {
    const step = window.innerHeight * 0.9;
    for (let y = 0; y < document.body.scrollHeight; y += step) {
      window.scrollTo(0, y);
      await new Promise((r) => setTimeout(r, 220));
    }
    window.scrollTo(0, 0);
  });
  await new Promise((r) => setTimeout(r, settleMs));
  await page.screenshot({ path: join(outDir, name), fullPage });
  console.log("saved", name);
  await page.close();
}

await shoot("desktop-full.png", { width: 1440, height: 900 }, { fullPage: true });
await shoot("hero-desktop.png", { width: 1440, height: 900 });
await shoot("mobile-full.png", { width: 390, height: 844 }, { fullPage: true });

await browser.close();
console.log("done");

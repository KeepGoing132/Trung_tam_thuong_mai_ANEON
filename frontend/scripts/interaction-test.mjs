/** Kiểm tra tương tác thư viện gian hàng: tìm kiếm + lọc danh mục + lọc tầng. */
import puppeteer from "puppeteer-core";

const edgePath = "C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe";
const browser = await puppeteer.launch({
  executablePath: edgePath,
  headless: "new",
  args: ["--no-first-run"],
});
const page = await browser.newPage();
await page.setViewport({ width: 1440, height: 900 });
await page.goto("http://localhost:3000/", { waitUntil: "networkidle2" });
// Chờ Next.js hydrate xong trước khi thao tác
await new Promise((r) => setTimeout(r, 2500));

const count = async () =>
  page.$$eval("#gian-hang ul li", (els) => els.length);

const total = await count();
console.log("Tổng gian hàng hiển thị:", total);

// 1. Tìm kiếm "cà phê" qua native setter (tiếng Việt có dấu)
await page.evaluate(() => {
  const input = document.querySelector("#store-search");
  const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value").set;
  setter.call(input, "cà phê");
  input.dispatchEvent(new Event("input", { bubbles: true }));
});
await new Promise((r) => setTimeout(r, 500));
console.log("Sau khi gõ 'cà phê':", await count());

// 2. Xóa tìm kiếm, lọc danh mục "Ẩm thực"
await page.evaluate(() => {
  const input = document.querySelector("#store-search");
  const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, "value").set;
  setter.call(input, "");
  input.dispatchEvent(new Event("input", { bubbles: true }));
});
await page.evaluate(() => {
  const btn = [...document.querySelectorAll("#gian-hang button")].find((b) => b.textContent.trim() === "Ẩm thực");
  btn.click();
});
await new Promise((r) => setTimeout(r, 500));
console.log("Lọc danh mục 'Ẩm thực':", await count());

// 3. Thêm lọc tầng B1
await page.evaluate(() => {
  const btn = [...document.querySelectorAll("#gian-hang button")].find((b) => b.textContent.trim() === "B1");
  btn.click();
});
await new Promise((r) => setTimeout(r, 500));
console.log("Thêm lọc tầng 'B1':", await count());

// 4. Tìm kiếm không có kết quả
await page.evaluate(() => {
  const btn = [...document.querySelectorAll("#gian-hang button")].find((b) => b.textContent.trim() === "Ẩm thực");
  btn.click();
});
await page.type("#store-search", "zzzz");
await new Promise((r) => setTimeout(r, 500));
const empty = await page.$eval("#gian-hang p[role=status]", (el) => el.textContent);
console.log("Kết quả 0:", empty.trim());

await browser.close();
console.log("INTERACTION TEST DONE");

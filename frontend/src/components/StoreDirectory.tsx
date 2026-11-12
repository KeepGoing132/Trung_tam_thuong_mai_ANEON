"use client";

import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import { STORE_CATEGORIES, STORE_FLOORS, STORES } from "@/lib/site";
import { Eyebrow, Reveal } from "./motion";

const EASE = [0.22, 1, 0.36, 1] as const;

/** Thư viện gian hàng: tìm kiếm + lọc danh mục + lọc tầng — tương tác thật. */
export default function StoreDirectory() {
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState<(typeof STORE_CATEGORIES)[number]>("Tất cả");
  const [floor, setFloor] = useState<(typeof STORE_FLOORS)[number]>("Tất cả");

  const results = useMemo(() => {
    const q = query.trim().toLowerCase();
    return STORES.filter((s) => {
      const matchQuery =
        q === "" || s.name.toLowerCase().includes(q) || s.note.toLowerCase().includes(q);
      const matchCategory = category === "Tất cả" || s.category === category;
      const matchFloor = floor === "Tất cả" || s.floor === floor;
      return matchQuery && matchCategory && matchFloor;
    });
  }, [query, category, floor]);

  return (
    <section id="gian-hang" className="scroll-mt-20 bg-plum py-24 text-cream md:py-36">
      <div className="mx-auto max-w-7xl px-5 md:px-10">
        <div className="flex flex-col gap-8 md:flex-row md:items-end md:justify-between">
          <div>
            <Reveal>
              <Eyebrow index="02" label="Danh sách gian hàng mẫu" tone="light" />
            </Reveal>
            <h2 className="mt-8 font-display text-4xl font-light leading-[1.08] tracking-[-0.02em] sm:text-5xl md:text-[56px]">
              <span className="block">Tìm ra cửa hàng</span>
              <span className="block">
                bạn <em className="font-display italic text-gold">đang nhớ.</em>
              </span>
            </h2>
          </div>
          <Reveal delay={0.15} className="w-full max-w-md">
            <label htmlFor="store-search" className="sr-only">
              Tìm gian hàng
            </label>
            <div className="relative">
              <input
                id="store-search"
                type="search"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                placeholder="Tìm theo tên hoặc mô tả…"
                className="h-[52px] w-full rounded-full border border-cream/20 bg-cream/5 pl-12 pr-5 text-sm text-cream placeholder:text-cream/40 outline-none transition-colors focus:border-magenta"
              />
              <svg
                aria-hidden
                viewBox="0 0 20 20"
                className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-cream/60"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.6"
              >
                <circle cx="9" cy="9" r="6" />
                <path d="m13.5 13.5 4 4" strokeLinecap="round" />
              </svg>
            </div>
          </Reveal>
        </div>

        {/* Bộ lọc */}
        <Reveal delay={0.1} className="mt-10 space-y-4">
          <div className="flex flex-wrap gap-2" role="group" aria-label="Lọc theo danh mục">
            {STORE_CATEGORIES.map((c) => (
              <button
                key={c}
                type="button"
                onClick={() => setCategory(c)}
                aria-pressed={category === c}
                className={`rounded-full border px-4 py-2 text-[12px] font-medium transition-all duration-300 ${
                  category === c
                    ? "border-magenta bg-magenta text-white"
                    : "border-cream/20 text-cream/75 hover:border-cream/60 hover:text-cream"
                }`}
              >
                {c}
              </button>
            ))}
          </div>
          <div className="flex flex-wrap items-center gap-2" role="group" aria-label="Lọc theo tầng">
            <span className="mr-1 font-mono text-[10px] uppercase tracking-[0.25em] text-cream/50">
              Tầng
            </span>
            {STORE_FLOORS.map((f) => (
              <button
                key={f}
                type="button"
                onClick={() => setFloor(f)}
                aria-pressed={floor === f}
                className={`rounded-full border px-4 py-2 font-mono text-[12px] transition-all duration-300 ${
                  floor === f
                    ? "border-gold bg-gold text-ink"
                    : "border-cream/20 text-cream/75 hover:border-cream/60 hover:text-cream"
                }`}
              >
                {f}
              </button>
            ))}
          </div>
        </Reveal>

        {/* Kết quả */}
        <div className="mt-10 border-t border-cream/10">
          <p className="py-4 font-mono text-[11px] uppercase tracking-[0.25em] text-cream/50" role="status">
            {results.length} gian hàng {query || category !== "Tất cả" || floor !== "Tất cả" ? "phù hợp" : "mẫu"}
          </p>
          <ul className="grid gap-0 md:grid-cols-2">
            {results.map((store, i) => (
              <motion.li
                key={store.id}
                initial={{ opacity: 0, y: 16 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.4, delay: Math.min(i * 0.03, 0.25), ease: EASE }}
                className="group border-b border-cream/10 md:odd:border-r"
              >
                <div
                  className="flex items-baseline gap-5 px-1 py-6 transition-colors md:px-5"
                >
                  <span className="font-mono text-[11px] text-cream/40 transition-colors group-hover:text-magenta">
                    {String(i + 1).padStart(2, "0")}
                  </span>
                  <span className="flex-1">
                    <span className="flex flex-wrap items-center gap-x-3 gap-y-1">
                      <span className="font-display text-xl text-cream transition-transform duration-300 group-hover:translate-x-1 md:text-2xl">
                        {store.name}
                      </span>
                      <span className="font-mono text-[10px] uppercase tracking-[0.2em] text-gold/90">
                        {store.floor}
                      </span>
                    </span>
                    <span className="mt-1 block text-[13px] text-cream/60">{store.note}</span>
                  </span>
                  <span className="hidden font-mono text-[10px] uppercase tracking-[0.2em] text-cream/40 md:block">
                    {store.category}
                  </span>
                </div>
              </motion.li>
            ))}
          </ul>
          {results.length === 0 && (
            <p className="py-14 text-center text-sm text-cream/60">
              Không tìm thấy gian hàng phù hợp — thử bỏ bớt bộ lọc hoặc đổi từ khóa.
            </p>
          )}
        </div>
      </div>
    </section>
  );
}

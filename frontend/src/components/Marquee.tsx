"use client";

import { MARQUEE_WORDS } from "@/lib/site";

/** Dải chữ chạy kiểu editorial — dừng ở prefers-reduced-motion (CSS). */
export default function Marquee() {
  const row = [...MARQUEE_WORDS, ...MARQUEE_WORDS];
  return (
    <div
      aria-hidden
      className="overflow-hidden border-y border-cream-line bg-cream-deep py-4"
    >
      <div className="marquee-track flex w-max animate-marquee items-center gap-10 pr-10">
        {[0, 1].map((half) => (
          <div key={half} className="flex items-center gap-10">
            {row.map((word, i) => (
              <span key={`${half}-${i}`} className="flex items-center gap-10">
                <span className="font-display text-xl italic text-ink/70 md:text-2xl">
                  {word}
                </span>
                <span className="inline-block h-1.5 w-1.5 rounded-full bg-magenta/70" />
              </span>
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}

"use client";

import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import { EVENT_TAGS, EVENTS } from "@/lib/site";
import { Eyebrow, Reveal } from "./motion";

const EASE = [0.22, 1, 0.36, 1] as const;

/** Sự kiện: lọc theo tag, thẻ ảnh hover scale + overlay. */
export default function EventsSection() {
  const [tag, setTag] = useState<(typeof EVENT_TAGS)[number]>("Tất cả");
  const events = useMemo(
    () => (tag === "Tất cả" ? EVENTS : EVENTS.filter((e) => e.tag === tag)),
    [tag],
  );

  return (
    <section id="su-kien" className="scroll-mt-20 py-24 md:py-36">
      <div className="mx-auto max-w-7xl px-5 md:px-10">
        <div className="flex flex-col gap-8 md:flex-row md:items-end md:justify-between">
          <div>
            <Reveal>
              <Eyebrow index="05" label="Sự kiện tháng này" />
            </Reveal>
            <h2 className="mt-8 font-display text-4xl font-light leading-[1.08] tracking-[-0.02em] sm:text-5xl md:text-[56px]">
              <span className="block">Thành phố sẽ</span>
              <span className="block">
                <em className="font-display italic text-magenta">sáng rục</em> hơn cuối tuần này.
              </span>
            </h2>
          </div>
          <Reveal delay={0.12} className="flex flex-wrap gap-2">
            {EVENT_TAGS.map((t) => (
              <button
                key={t}
                type="button"
                onClick={() => setTag(t)}
                aria-pressed={tag === t}
                className={`rounded-full border px-4 py-2 text-[12px] font-medium transition-all duration-300 ${
                  tag === t
                    ? "border-ink bg-ink text-cream"
                    : "border-ink/20 text-ink-soft hover:border-ink/60 hover:text-ink"
                }`}
              >
                {t}
              </button>
            ))}
          </Reveal>
        </div>

        <div className="mt-14 grid gap-5 md:grid-cols-2">
          {events.map((ev, i) => (
            <motion.article
              key={ev.id}
              initial={{ opacity: 0, y: 24 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: i * 0.06, ease: EASE }}
                className={`group overflow-hidden rounded-[24px] bg-plum text-cream ${
                  i === 0 && events.length > 2 ? "md:col-span-2" : ""
                }`}
              >
                <div className={`relative ${i === 0 && events.length > 2 ? "aspect-[16/8]" : "aspect-[16/10]"}`}>
                  <img
                    src={ev.image}
                    alt={ev.title}
                    loading="lazy"
                    className="h-full w-full object-cover transition-transform duration-700 ease-[cubic-bezier(0.22,1,0.36,1)] group-hover:scale-[1.05]"
                  />
                  <div aria-hidden className="absolute inset-0 bg-gradient-to-t from-plum/85 via-plum/20 to-transparent" />
                  <div className="absolute left-5 top-5 flex items-center gap-3">
                    <span className="rounded-full bg-cream/15 px-3 py-1.5 font-mono text-[10px] uppercase tracking-[0.2em] text-cream backdrop-blur-sm">
                      {ev.tag}
                    </span>
                  </div>
                  <div className="absolute inset-x-0 bottom-0 flex items-end justify-between gap-4 p-6">
                    <div className="flex items-center gap-5">
                      <div className="text-center">
                        <p className="font-display text-4xl leading-none text-cream md:text-5xl">{ev.day}</p>
                        <p className="mt-1 font-mono text-[10px] uppercase tracking-[0.25em] text-gold">
                          {ev.month}
                        </p>
                      </div>
                      <div>
                        <h3 className="font-display text-xl leading-snug md:text-2xl">{ev.title}</h3>
                        <p className="mt-1 font-mono text-[10px] uppercase tracking-[0.2em] text-cream/65">
                          {ev.place} — {ev.time}
                        </p>
                      </div>
                    </div>
                    <span
                      aria-hidden
                      className="hidden h-11 w-11 shrink-0 items-center justify-center rounded-full border border-cream/30 text-cream transition-all duration-300 group-hover:border-cream group-hover:bg-cream group-hover:text-ink md:inline-flex"
                    >
                      <svg viewBox="0 0 16 16" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="1.6">
                        <path d="M2 8h11M9 3.5 13.5 8 9 12.5" strokeLinecap="round" strokeLinejoin="round" />
                      </svg>
                    </span>
                  </div>
                </div>
            </motion.article>
          ))}
        </div>
        {events.length === 0 && (
          <p className="py-16 text-center text-sm text-ink-soft">
            Chưa có sự kiện thuộc mục này trong tháng — hãy quay lại sau nhé.
          </p>
        )}
      </div>
    </section>
  );
}

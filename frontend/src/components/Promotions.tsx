"use client";

import { IMAGES, PROMOS } from "@/lib/site";
import { Eyebrow, Reveal } from "./motion";

/** Ưu đãi: thẻ đầu tiên dùng khung gradient động — điểm nhấn thương hiệu. */
export default function Promotions() {
  return (
    <section id="uu-dai" className="scroll-mt-20 bg-cream-deep py-24 md:py-36">
      <div className="mx-auto max-w-7xl px-5 md:px-10">
        <div className="flex flex-col gap-6 md:flex-row md:items-end md:justify-between">
          <div>
            <Reveal>
              <Eyebrow index="04" label="Ưu đãi đang diễn ra" />
            </Reveal>
            <h2 className="mt-8 font-display text-4xl font-light leading-[1.08] tracking-[-0.02em] sm:text-5xl md:text-[56px]">
              <span className="block">Đáng giá cho</span>
              <span className="block">
                chuyến <em className="font-display italic text-magenta">đi xa nhất.</em>
              </span>
            </h2>
          </div>
          <Reveal delay={0.15}>
            <p className="max-w-sm text-[15px] leading-relaxed text-ink-soft">
              Cập nhật theo tuần tại quầy thông tin và trên app AEON MALL. Số
              lượng có hạn — chi tiết điều kiện tại từng gian hàng.
            </p>
          </Reveal>
        </div>

        <div className="mt-14 grid gap-5 md:grid-cols-2 xl:grid-cols-4">
          {/* Thẻ nổi bật với khung gradient */}
          <Reveal className="xl:row-span-2">
            <article className="aeon-gradient relative h-full rounded-[24px] p-[2px]">
              <div className="flex h-full flex-col justify-between overflow-hidden rounded-[22px] bg-plum text-cream">
                <img
                  src={IMAGES.promo}
                  alt="Hộp quà lễ hội"
                  loading="lazy"
                  className="h-56 w-full object-cover opacity-90 md:h-72"
                />
                <div className="flex flex-1 flex-col justify-between p-6">
                  <div>
                    <span className="font-mono text-[10px] uppercase tracking-[0.25em] text-gold">
                      {PROMOS[0].tag}
                    </span>
                    <h3 className="mt-3 font-display text-2xl leading-snug md:text-[26px]">
                      {PROMOS[0].title}
                    </h3>
                    <p className="mt-3 text-[13px] leading-relaxed text-cream/70">
                      {PROMOS[0].detail}
                    </p>
                  </div>
                  <p className="mt-6 font-mono text-[11px] uppercase tracking-[0.25em] text-cream/60">
                    {PROMOS[0].period}
                  </p>
                </div>
              </div>
            </article>
          </Reveal>

          {PROMOS.slice(1).map((promo, i) => (
            <Reveal key={promo.id} delay={0.08 * (i + 1)}>
              <article className="group flex h-full flex-col justify-between rounded-[24px] border border-cream-line bg-cream p-6 transition-all duration-500 hover:border-ink/30 hover:shadow-[0_18px_40px_-24px_rgba(26,20,18,0.35)]">
                <div>
                  <div className="flex items-center justify-between gap-3">
                    <span className="rounded-full bg-magenta/10 px-3 py-1 font-mono text-[10px] uppercase tracking-[0.2em] text-magenta-deep">
                      {promo.tag}
                    </span>
                    <span className="font-mono text-[10px] uppercase tracking-[0.2em] text-ink-soft/70">
                      {promo.period}
                    </span>
                  </div>
                  <h3 className="mt-5 font-display text-xl leading-snug md:text-[22px]">
                    {promo.title}
                  </h3>
                  <p className="mt-3 text-[13px] leading-relaxed text-ink-soft">{promo.detail}</p>
                </div>
                <span
                  aria-hidden
                  className="mt-6 inline-flex items-center gap-2 text-[13px] font-semibold text-ink transition-colors group-hover:text-magenta"
                >
                  Xem điều kiện
                  <svg
                    viewBox="0 0 16 16"
                    className="h-3.5 w-3.5 transition-transform duration-300 group-hover:translate-x-1"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="1.6"
                  >
                    <path d="M2 8h11M9 3.5 13.5 8 9 12.5" strokeLinecap="round" strokeLinejoin="round" />
                  </svg>
                </span>
              </article>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  );
}

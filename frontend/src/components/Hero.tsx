"use client";

import { motion, useReducedMotion } from "framer-motion";
import { HERO_STATS, IMAGES } from "@/lib/site";
import { StaggerLines } from "./motion";

const EASE = [0.22, 1, 0.36, 1] as const;

/** Hero toàn màn hình: ảnh lớn zoom chậm, gradient overlay, headline stagger. */
export default function Hero() {
  const reduce = useReducedMotion();

  return (
    <section id="top" className="relative min-h-[100svh] overflow-hidden bg-plum">
      {/* Ảnh nền: zoom rất chậm tạo chiều sâu cinematic */}
      <div className="absolute inset-0" aria-hidden>
        <img
          src={IMAGES.hero}
          alt=""
          className={`hero-zoom-img h-full w-full object-cover ${reduce ? "" : "animate-hero-zoom"}`}
          fetchPriority="high"
        />
        <div className="absolute inset-0 bg-gradient-to-b from-plum/70 via-plum/25 to-plum/85" />
        <div className="absolute inset-0 bg-gradient-to-r from-plum/55 via-transparent to-transparent" />
      </div>

      {/* Ribbon gradient động ở mép dưới hero — điểm nhấn chuyển động */}
      <div
        aria-hidden
        className="aeon-gradient absolute inset-x-0 bottom-0 h-1.5 opacity-90"
      />

      <div className="relative z-10 mx-auto flex min-h-[100svh] max-w-7xl flex-col justify-end px-5 pb-28 pt-40 md:px-10 md:pb-32">
        <motion.p
          initial={reduce ? false : { opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, delay: 0.15, ease: EASE }}
          className="mb-5 font-mono text-[11px] uppercase tracking-[0.32em] text-cream/75 md:text-xs"
        >
          Mua sắm — Ẩm thực — Giải trí — Sự kiện
        </motion.p>

        <h1 className="max-w-4xl font-display text-[13vw] font-light leading-[1.02] tracking-[-0.02em] text-cream sm:text-6xl md:text-7xl lg:text-[86px]">
          <StaggerLines
            delay={0.25}
            lines={[
              <>Điểm hẹn niềm vui</>,
              <>
                của{" "}
                <em className="font-display italic text-gold">cả thành phố.</em>
              </>,
            ]}
          />
        </h1>

        <motion.p
          initial={reduce ? false : { opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.9, delay: 0.75, ease: EASE }}
          className="mt-6 max-w-xl text-[15px] leading-relaxed text-cream/80 md:text-base"
        >
          Hơn 250 gian hàng, 60 quán ăn và cả không gian sự kiện nằm dưới một mái
          vòm ánh sáng — mở cửa mỗi ngày từ 9:00 đến 22:00.
        </motion.p>

        <motion.div
          initial={reduce ? false : { opacity: 0, y: 18 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.9, delay: 0.9, ease: EASE }}
          className="mt-9 flex flex-wrap items-center gap-4"
        >
          <a
            href="#gian-hang"
            className="btn-slide inline-flex h-[52px] items-center rounded-full bg-cream px-8 text-sm font-semibold text-ink hover:text-cream"
            style={{ ["--slide-color" as string]: "#e5007e" }}
          >
            Khám phá gian hàng
          </a>
          <a
            href="#su-kien"
            className="btn-slide inline-flex h-[52px] items-center rounded-full border border-cream/35 px-8 text-sm font-semibold text-cream hover:border-cream hover:text-ink"
            style={{ ["--slide-color" as string]: "#fbf6ed" }}
          >
            Sự kiện trong tháng
          </a>
        </motion.div>

        {/* Dải số liệu kiểu mono, xuất hiện cuối cùng */}
        <motion.dl
          initial="hidden"
          animate="show"
          variants={{ show: { transition: { staggerChildren: 0.08, delayChildren: 1.1 } } }}
          className="mt-14 grid grid-cols-2 gap-x-6 gap-y-5 border-t border-cream/15 pt-7 md:grid-cols-4"
        >
          {HERO_STATS.map((s) => (
            <motion.div
              key={s.label}
              variants={{ hidden: { opacity: 0, y: 14 }, show: { opacity: 1, y: 0 } }}
              transition={{ duration: 0.7, ease: EASE }}
            >
              <dt className="font-display text-3xl text-cream md:text-4xl">{s.value}</dt>
              <dd className="mt-1 font-mono text-[10px] uppercase tracking-[0.28em] text-cream/60">
                {s.label}
              </dd>
            </motion.div>
          ))}
        </motion.dl>
      </div>
    </section>
  );
}

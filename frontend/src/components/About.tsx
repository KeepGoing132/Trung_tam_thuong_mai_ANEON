"use client";

import { useRef } from "react";
import { motion, useReducedMotion, useScroll, useTransform } from "framer-motion";
import { IMAGES } from "@/lib/site";
import { Eyebrow, Reveal, StaggerLines } from "./motion";

/** Giới thiệu: bố cục editorial hai cột, ảnh có parallax nhẹ. */
export default function About() {
  const sectionRef = useRef<HTMLElement>(null);
  const reduce = useReducedMotion();
  const { scrollYProgress } = useScroll({
    target: sectionRef,
    offset: ["start end", "end start"],
  });
  const yTall = useTransform(scrollYProgress, [0, 1], [reduce ? 0 : -28, reduce ? 0 : 28]);
  const ySmall = useTransform(scrollYProgress, [0, 1], [reduce ? 0 : 20, reduce ? 0 : -20]);

  return (
    <section id="gioi-thieu" ref={sectionRef} className="relative overflow-hidden py-24 md:py-36">
      {/* Khối gradient động mờ phía sau — kiểu Daylight dùng gradient làm nền */}
      <div
        aria-hidden
        className="aeon-gradient pointer-events-none absolute -right-40 -top-24 h-[420px] w-[420px] rounded-full opacity-[0.16] blur-3xl"
      />

      <div className="mx-auto max-w-7xl px-5 md:px-10">
        <Reveal>
          <Eyebrow index="01" label="Giới thiệu" />
        </Reveal>

        <div className="mt-10 grid gap-14 lg:grid-cols-2 lg:gap-20">
          <div>
            <h2 className="font-display text-4xl font-light leading-[1.08] tracking-[-0.02em] sm:text-5xl md:text-[56px]">
              <StaggerLines
                lines={[
                  <>Một thành phố thu nhỏ</>,
                  <>
                    dưới <em className="font-display italic text-magenta">một mái vòm.</em>
                  </>,
                ]}
              />
            </h2>

            <Reveal delay={0.15} className="mt-8 space-y-5 text-[15px] leading-relaxed text-ink-soft md:text-base">
              <p>
                AEON MALL không chỉ là nơi mua sắm — đó là quảng trường gặp gỡ, là
                bàn ăn chung của cả khu phố, là sân khấu của những khoảnh khắc
                đáng nhớ. Mỗi tầng được tổ chức như một chương của cùng một câu
                chuyện.
              </p>
              <p>
                Từ siêu thị Nhật Bản ở tầng hầm đến vườn hoa trên tầng 2, từ
                boutique tối giản đến rạp chiếu Dolby — mọi thứ cách nhau một
                chuyến thang cuốn.
              </p>
            </Reveal>

            <Reveal delay={0.25} className="mt-10">
              <dl className="grid grid-cols-2 gap-6 border-t border-cream-line pt-8 text-sm md:grid-cols-3">
                {[
                  ["09:00 — 22:00", "Giờ mở cửa"],
                  ["Hàng ngày", "Cả tuần, kể cả lễ"],
                  ["Miễn phí", "Gửi xe 2 giờ đầu"],
                ].map(([v, l]) => (
                  <div key={l}>
                    <dt className="font-display text-xl">{v}</dt>
                    <dd className="mt-1 font-mono text-[10px] uppercase tracking-[0.25em] text-ink-soft">
                      {l}
                    </dd>
                  </div>
                ))}
              </dl>
            </Reveal>
          </div>

          <div className="relative grid grid-cols-5 grid-rows-[auto_auto] gap-4 md:gap-5">
            <motion.figure
              style={{ y: yTall }}
              className="col-span-5 overflow-hidden rounded-[20px] md:col-span-4"
            >
              <img
                src={IMAGES.aboutTall}
                alt="Atrium hiện đại của trung tâm thương mại"
                loading="lazy"
                className="aspect-[4/3] w-full object-cover transition-transform duration-700 ease-[cubic-bezier(0.22,1,0.36,1)] hover:scale-[1.04] md:aspect-[5/4]"
              />
            </motion.figure>
            <motion.figure
              style={{ y: ySmall }}
              className="col-span-3 overflow-hidden rounded-[20px] md:col-start-3 md:col-span-3"
            >
              <img
                src={IMAGES.aboutSmall}
                alt="Khách mang túi mua sắm đi trong hành lang"
                loading="lazy"
                className="aspect-[4/3] w-full object-cover transition-transform duration-700 ease-[cubic-bezier(0.22,1,0.36,1)] hover:scale-[1.04]"
              />
            </motion.figure>
            <Reveal delay={0.2} className="col-span-2 flex items-end pb-2 md:col-start-2">
              <p className="font-mono text-[10px] uppercase leading-relaxed tracking-[0.25em] text-ink-soft">
                Kể từ 2011 — một trong những điểm đến được yêu thích nhất của
                thành phố
              </p>
            </Reveal>
          </div>
        </div>
      </div>
    </section>
  );
}

"use client";

import { FACILITIES, IMAGES } from "@/lib/site";
import { Eyebrow, Reveal } from "./motion";

/** Tiện ích: ảnh siêu thị lớn + lưới tiện ích có icon line mảnh. */
export default function Facilities() {
  return (
    <section id="tien-ich" className="scroll-mt-20 bg-cream-deep py-24 md:py-36">
      <div className="mx-auto max-w-7xl px-5 md:px-10">
        <Reveal>
          <Eyebrow index="06" label="Tiện ích trung tâm" />
        </Reveal>

        <div className="mt-12 grid gap-12 lg:grid-cols-12">
          <Reveal className="lg:col-span-5">
            <div className="relative overflow-hidden rounded-[24px]">
              <img
                src={IMAGES.supermarket}
                alt="Khu siêu thị thực phẩm tươi"
                loading="lazy"
                className="aspect-[4/3] w-full object-cover transition-transform duration-700 ease-[cubic-bezier(0.22,1,0.36,1)] hover:scale-[1.04]"
              />
              <div aria-hidden className="absolute inset-0 bg-gradient-to-t from-plum/80 via-transparent to-transparent" />
              <div className="absolute inset-x-0 bottom-0 p-6">
                <p className="font-mono text-[10px] uppercase tracking-[0.25em] text-gold">
                  Tầng hầm B1
                </p>
                <p className="mt-1 font-display text-2xl text-cream">AEON Supermarket</p>
              </div>
            </div>
            <p className="mt-6 max-w-md text-[15px] leading-relaxed text-ink-soft">
              Mỗi chi tiết trong trung tâm đều được thiết kế để chuyến đi của bạn
              nhẹ nhàng — từ chỗ đỗ xe dẫn đường đến phòng chăm sóc bé trên mỗi
              tầng.
            </p>
          </Reveal>

          <ul className="grid gap-x-8 gap-y-9 sm:grid-cols-2 lg:col-span-7">
            {FACILITIES.map((f, i) => (
              <Reveal key={f.title} delay={0.06 * i}>
                <li className="group border-t border-cream-line pt-5">
                  <p className="font-mono text-[11px] text-magenta">
                    {String(i + 1).padStart(2, "0")}
                  </p>
                  <h3 className="mt-2 font-display text-xl transition-colors group-hover:text-magenta md:text-[22px]">
                    {f.title}
                  </h3>
                  <p className="mt-2 text-[13px] leading-relaxed text-ink-soft">{f.detail}</p>
                </li>
              </Reveal>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}

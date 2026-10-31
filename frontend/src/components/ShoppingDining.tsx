"use client";

import { useRef } from "react";
import { motion, useReducedMotion, useScroll, useTransform } from "framer-motion";
import { DINING_TILES, IMAGES, SHOPPING_TILES } from "@/lib/site";
import { Eyebrow, Reveal, StaggerLines } from "./motion";

type Tile = { image: string; label: string; tag: string };

/** Thẻ ảnh có hover scale + overlay reveal */
function ImageTile({ tile, aspect = "aspect-[4/5]" }: { tile: Tile; aspect?: string }) {
  return (
    <a href="#gian-hang" className="group block">
      <div className={`relative overflow-hidden rounded-[20px] ${aspect}`}>
        <img
          src={tile.image}
          alt={tile.label}
          loading="lazy"
          className="h-full w-full object-cover transition-transform duration-700 ease-[cubic-bezier(0.22,1,0.36,1)] group-hover:scale-[1.06]"
        />
        <div
          aria-hidden
          className="absolute inset-0 bg-gradient-to-t from-plum/75 via-transparent to-transparent opacity-70 transition-opacity duration-500 group-hover:opacity-95"
        />
        <div className="absolute inset-x-0 bottom-0 translate-y-2 p-5 transition-transform duration-500 ease-[cubic-bezier(0.22,1,0.36,1)] group-hover:translate-y-0">
          <p className="font-display text-lg text-cream md:text-xl">{tile.label}</p>
          <p className="mt-1 font-mono text-[10px] uppercase tracking-[0.22em] text-cream/70 opacity-0 transition-opacity duration-500 group-hover:opacity-100">
            {tile.tag}
          </p>
        </div>
      </div>
    </a>
  );
}

/** Section hai trải nghiệm lớn: mua sắm và ẩm thực — editorial, có parallax. */
export default function ShoppingDining() {
  const reduce = useReducedMotion();
  const shoppingRef = useRef<HTMLDivElement>(null);
  const { scrollYProgress: sp } = useScroll({
    target: shoppingRef,
    offset: ["start end", "end start"],
  });
  const yShop = useTransform(sp, [0, 1], [reduce ? 0 : 26, reduce ? 0 : -26]);

  return (
    <section id="mua-sam-am-thuc" className="scroll-mt-20 py-24 md:py-36">
      <div className="mx-auto max-w-7xl px-5 md:px-10">
        <Reveal>
          <Eyebrow index="03" label="Mua sắm & Ẩm thực" />
        </Reveal>

        {/* Mua sắm */}
        <div ref={shoppingRef} className="mt-12 grid gap-10 lg:grid-cols-12">
          <Reveal className="lg:col-span-5 lg:pr-6">
            <div className="lg:sticky lg:top-28">
              <h3 className="font-display text-3xl font-light leading-[1.1] tracking-[-0.02em] sm:text-4xl md:text-[44px]">
                <StaggerLines
                  lines={[
                    <>Mua sắm —</>,
                    <>
                      <em className="font-display italic text-magenta">chọn lọc</em> mỗi mùa
                    </>,
                  ]}
                />
              </h3>
              <p className="mt-6 max-w-md text-[15px] leading-relaxed text-ink-soft">
                Các thương hiệu thời trang, làm đẹp và gia dụng được chọn theo
                mùa, kèm chương trình đổi trả thân thiện và gói quà miễn phí tại
                quầy thông tin.
              </p>
              <a
                href="#gian-hang"
                className="btn-slide mt-8 inline-flex h-[52px] items-center rounded-full border border-ink/25 px-7 text-sm font-semibold text-ink hover:border-ink hover:text-cream"
                style={{ ["--slide-color" as string]: "#1a1412" }}
              >
                Xem thư viện gian hàng
              </a>
            </div>
          </Reveal>
          <div className="grid grid-cols-2 gap-4 md:gap-5 lg:col-span-7">
            <motion.div style={{ y: yShop }} className="col-span-2">
              <ImageTile tile={SHOPPING_TILES[0]} aspect="aspect-[16/9]" />
            </motion.div>
            <ImageTile tile={SHOPPING_TILES[1]} />
            <ImageTile tile={SHOPPING_TILES[2]} />
          </div>
        </div>

        {/* Ẩm thực */}
        <div className="mt-24 grid gap-10 lg:grid-cols-12 md:mt-32">
          <div className="order-2 grid grid-cols-2 gap-4 md:gap-5 lg:order-1 lg:col-span-7">
            <div className="col-span-2">
              <ImageTile tile={DINING_TILES[0]} aspect="aspect-[16/9]" />
            </div>
            <ImageTile tile={DINING_TILES[1]} />
            <div className="relative overflow-hidden rounded-[20px] bg-cream-deep">
              <img
                src={IMAGES.supermarket}
                alt="Siêu thị tươi sạch"
                loading="lazy"
                className="h-full w-full object-cover transition-transform duration-700 ease-[cubic-bezier(0.22,1,0.36,1)] hover:scale-[1.06]"
              />
              <div aria-hidden className="absolute inset-0 bg-gradient-to-t from-plum/70 to-transparent" />
              <p className="absolute inset-x-0 bottom-0 p-5 font-display text-lg text-cream md:text-xl">
                Siêu thị & thực phẩm tươi
              </p>
            </div>
          </div>
          <Reveal className="order-1 lg:order-2 lg:col-span-5 lg:pl-6">
            <div className="lg:sticky lg:top-28">
              <h3 className="font-display text-3xl font-light leading-[1.1] tracking-[-0.02em] sm:text-4xl md:text-[44px]">
                <StaggerLines
                  lines={[
                    <>Ẩm thực —</>,
                    <>
                      từ bữa nhà{" "}
                      <em className="font-display italic text-magenta">đến fine dining</em>
                    </>,
                  ]}
                />
              </h3>
              <p className="mt-6 max-w-md text-[15px] leading-relaxed text-ink-soft">
                60 quán ăn trải trên ba tầng: từ góc cà phê rang mộc, mì Nhật
                hầm xương 12 tiếng đến nhà hàng VIEW nhìn ra quảng trường ánh
                sáng.
              </p>
              <div className="mt-8 flex flex-wrap gap-2">
                {["Nhật", "Hàn", "Việt Nam", "Ý", "Cà phê", "Tráng miệng"].map((t) => (
                  <span
                    key={t}
                    className="rounded-full border border-ink/15 px-4 py-2 font-mono text-[10px] uppercase tracking-[0.2em] text-ink-soft"
                  >
                    {t}
                  </span>
                ))}
              </div>
            </div>
          </Reveal>
        </div>
      </div>
    </section>
  );
}

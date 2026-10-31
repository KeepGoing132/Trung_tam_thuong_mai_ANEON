"use client";

import { useState } from "react";
import { NAV } from "@/lib/site";
import { Reveal } from "./motion";

/** Footer: wordmark lớn, cột liên kết, đăng ký tin (có trạng thái thành công). */
export default function Footer() {
  const [email, setEmail] = useState("");
  const [done, setDone] = useState(false);

  const submit = (e: React.FormEvent) => {
    e.preventDefault();
    if (/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email.trim())) {
      setDone(true);
    }
  };

  return (
    <footer className="relative overflow-hidden bg-plum pt-20 text-cream md:pt-28">
      <div aria-hidden className="aeon-gradient absolute inset-x-0 top-0 h-1" />

      <div className="mx-auto max-w-7xl px-5 md:px-10">
        <div className="grid gap-14 lg:grid-cols-12">
          <Reveal className="lg:col-span-5">
            <p className="font-mono text-[11px] uppercase tracking-[0.3em] text-cream/60">
              Bản tin AEON MALL
            </p>
            <h2 className="mt-5 font-display text-3xl font-light leading-[1.12] sm:text-4xl">
              Ưu đãi và sự kiện,
              <br />
              gửi <em className="font-display italic text-gold">trước một tuần.</em>
            </h2>
            {done ? (
              <p className="mt-7 rounded-full border border-gold/40 px-5 py-4 text-sm text-gold" role="status">
                Đã ghi nhận — hẹn gặp bạn trong bản tin kế tiếp.
              </p>
            ) : (
              <form onSubmit={submit} className="mt-7 flex max-w-md gap-2" noValidate={false}>
                <label htmlFor="footer-email" className="sr-only">
                  Email của bạn
                </label>
                <input
                  id="footer-email"
                  type="email"
                  required
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="email@cua-ban.vn"
                  className="h-[52px] flex-1 rounded-full border border-cream/20 bg-cream/5 px-5 text-sm text-cream placeholder:text-cream/40 outline-none transition-colors focus:border-gold"
                />
                <button
                  type="submit"
                  className="btn-slide h-[52px] shrink-0 rounded-full bg-cream px-6 text-sm font-semibold text-ink hover:text-cream"
                  style={{ ["--slide-color" as string]: "#e5007e" }}
                >
                  Đăng ký
                </button>
              </form>
            )}
            <p className="mt-4 text-xs text-cream/45">
              Mỗi tuần tối đa một email. Hủy đăng ký bất cứ lúc nào.
            </p>
          </Reveal>

          <div className="grid grid-cols-2 gap-10 sm:grid-cols-3 lg:col-span-7">
            <Reveal delay={0.08}>
              <h3 className="font-mono text-[10px] uppercase tracking-[0.28em] text-cream/50">
                Khám phá
              </h3>
              <ul className="mt-5 space-y-3 text-sm">
                {NAV.map((n) => (
                  <li key={n.href}>
                    <a href={n.href} className="text-cream/80 transition-colors hover:text-gold">
                      {n.label}
                    </a>
                  </li>
                ))}
              </ul>
            </Reveal>
            <Reveal delay={0.14}>
              <h3 className="font-mono text-[10px] uppercase tracking-[0.28em] text-cream/50">
                Giờ mở cửa
              </h3>
              <ul className="mt-5 space-y-3 text-sm text-cream/80">
                <li>Thứ Hai — Chủ Nhật</li>
                <li className="font-display text-lg text-cream">09:00 — 22:00</li>
                <li>Siêu thị: 08:00 — 22:30</li>
                <li>Rạp chiếu phim: đến 24:00</li>
              </ul>
            </Reveal>
            <Reveal delay={0.2}>
              <h3 className="font-mono text-[10px] uppercase tracking-[0.28em] text-cream/50">
                Liên hệ
              </h3>
              <ul className="mt-5 space-y-3 text-sm text-cream/80">
                <li>Số 8 Đại Lộ TTTM</li>
                <li>Khu đô thị mới, TP. Hồ Chí Minh</li>
                <li>
                  <a href="tel:+842836221111" className="transition-colors hover:text-gold">
                    (028) 3622 1111
                  </a>
                </li>
                <li>
                  <a href="mailto:hello@aeonmall.vn" className="transition-colors hover:text-gold">
                    hello@aeonmall.vn
                  </a>
                </li>
              </ul>
              <div className="mt-5 flex gap-2">
                {["Facebook", "TikTok", "Instagram"].map((s) => (
                  <a
                    key={s}
                    href="#top"
                    aria-label={s}
                    className="rounded-full border border-cream/20 px-3 py-1.5 font-mono text-[10px] uppercase tracking-[0.15em] text-cream/70 transition-colors hover:border-gold hover:text-gold"
                  >
                    {s.slice(0, 2)}
                  </a>
                ))}
              </div>
            </Reveal>
          </div>
        </div>

        {/* Wordmark lớn */}
        <p
          aria-hidden
          className="mt-20 select-none text-center font-display text-[16.5vw] font-light leading-[0.85] tracking-[-0.02em] text-cream/10 md:mt-28"
        >
          AEON MALL
        </p>

        <div className="flex flex-col items-center justify-between gap-3 border-t border-cream/10 py-7 text-xs text-cream/50 md:flex-row">
          <p>© {new Date().getFullYear()} AEON MALL — Demo dựng cho mục đích học tập.</p>
          <p className="font-mono text-[10px] uppercase tracking-[0.25em]">
            Mua sắm · Ẩm thực · Giải trí
          </p>
        </div>
      </div>
    </footer>
  );
}

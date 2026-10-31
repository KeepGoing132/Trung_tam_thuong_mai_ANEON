"use client";

import { useEffect, useState } from "react";
import { AnimatePresence, motion, useReducedMotion } from "framer-motion";
import { NAV } from "@/lib/site";

/** Header sticky: trong suốt trên hero, chuyển nền kem + blur khi cuộn. */
export default function Header() {
  const [scrolled, setScrolled] = useState(false);
  const [open, setOpen] = useState(false);
  const reduce = useReducedMotion();

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 32);
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  useEffect(() => {
    document.body.style.overflow = open ? "hidden" : "";
    return () => {
      document.body.style.overflow = "";
    };
  }, [open]);

  return (
    <header
      className={`fixed inset-x-0 top-0 z-50 transition-all duration-500 ease-[cubic-bezier(0.22,1,0.36,1)] ${
        scrolled
          ? "border-b border-cream-line bg-cream/85 backdrop-blur-md"
          : "border-b border-transparent bg-transparent"
      }`}
    >
      <div
        className={`mx-auto flex max-w-7xl items-center justify-between px-5 transition-all duration-500 md:px-10 ${
          scrolled ? "h-16" : "h-20 md:h-24"
        }`}
      >
        <a href="#top" className="group flex items-baseline gap-2" aria-label="AEON MALL — về đầu trang">
          <span
            className={`font-display text-2xl leading-none tracking-tight transition-colors md:text-[26px] ${
              scrolled ? "text-ink" : "text-cream"
            }`}
          >
            AEON
          </span>
          <span
            className={`font-mono text-[10px] uppercase tracking-[0.3em] transition-colors md:text-[11px] ${
              scrolled ? "text-magenta" : "text-gold"
            }`}
          >
            Mall
          </span>
        </a>

        <nav className="hidden items-center gap-7 lg:flex" aria-label="Điều hướng chính">
          {NAV.map((item) => (
            <a
              key={item.href}
              href={item.href}
              className={`group relative text-[13px] font-medium tracking-wide transition-colors ${
                scrolled ? "text-ink-soft hover:text-ink" : "text-cream/85 hover:text-cream"
              }`}
            >
              {item.label}
              <span
                aria-hidden
                className="absolute -bottom-1 left-0 h-px w-full origin-left scale-x-0 bg-magenta transition-transform duration-300 ease-[cubic-bezier(0.22,1,0.36,1)] group-hover:scale-x-100"
              />
            </a>
          ))}
        </nav>

        <div className="flex items-center gap-3">
          <a
            href="#gian-hang"
            className={`btn-slide hidden h-11 items-center rounded-full border px-6 text-[13px] font-semibold transition-colors md:inline-flex ${
              scrolled
                ? "border-ink/20 text-ink hover:border-ink hover:text-cream"
                : "border-cream/40 text-cream hover:border-cream hover:text-ink"
            }`}
            style={{ ["--slide-color" as string]: scrolled ? "#1a1412" : "#fbf6ed" }}
          >
            Tìm gian hàng
          </a>
          <button
            type="button"
            onClick={() => setOpen((v) => !v)}
            aria-expanded={open}
            aria-label={open ? "Đóng menu" : "Mở menu"}
            className={`flex h-11 w-11 items-center justify-center rounded-full border lg:hidden ${
              scrolled ? "border-ink/20 text-ink" : "border-cream/40 text-cream"
            }`}
          >
            <span className="relative block h-3 w-5">
              <span
                className={`absolute left-0 top-0 h-px w-5 bg-current transition-transform duration-300 ${
                  open ? "translate-y-[5px] rotate-45" : ""
                }`}
              />
              <span
                className={`absolute bottom-0 left-0 h-px w-5 bg-current transition-transform duration-300 ${
                  open ? "-translate-y-[6px] -rotate-45" : ""
                }`}
              />
            </span>
          </button>
        </div>
      </div>

      <AnimatePresence>
        {open && (
          <motion.nav
            initial={reduce ? false : { opacity: 0, y: -12 }}
            animate={{ opacity: 1, y: 0 }}
            exit={reduce ? undefined : { opacity: 0, y: -12 }}
            transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
            className="fixed inset-0 top-0 z-40 flex flex-col bg-plum px-6 pb-10 pt-28 lg:hidden"
            aria-label="Menu di động"
          >
            {NAV.map((item, i) => (
              <motion.a
                key={item.href}
                href={item.href}
                onClick={() => setOpen(false)}
                initial={reduce ? false : { opacity: 0, x: -16 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ delay: 0.06 * i + 0.1, duration: 0.5, ease: [0.22, 1, 0.36, 1] }}
                className="border-b border-cream/10 py-5 font-display text-3xl text-cream"
              >
                {item.label}
              </motion.a>
            ))}
            <a
              href="#gian-hang"
              onClick={() => setOpen(false)}
              className="mt-8 inline-flex h-12 items-center justify-center rounded-full bg-magenta px-6 font-semibold text-cream"
            >
              Tìm gian hàng
            </a>
          </motion.nav>
        )}
      </AnimatePresence>
    </header>
  );
}

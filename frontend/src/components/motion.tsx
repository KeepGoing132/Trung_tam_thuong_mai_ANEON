"use client";

import { motion, useReducedMotion } from "framer-motion";
import type { ReactNode } from "react";

const EASE = [0.22, 1, 0.36, 1] as const;

/** Khối nội dung xuất hiện khi cuộn tới: fade + slide-up. */
export function Reveal({
  children,
  delay = 0,
  y = 28,
  className,
}: {
  children: ReactNode;
  delay?: number;
  y?: number;
  className?: string;
}) {
  const reduce = useReducedMotion();
  return (
    <motion.div
      className={className}
      initial={reduce ? false : { opacity: 0, y }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: "-80px" }}
      transition={{ duration: 0.9, delay, ease: EASE }}
    >
      {children}
    </motion.div>
  );
}

/**
 * Tiêu đề lớn tách dòng, mỗi dòng hiện dần theo thứ tự.
 * Observer đặt trên KHUNG ngoài (không bị transform) để IntersectionObserver
 * luôn thấy phần tử — dòng chữ bên trong mới được reveal đúng.
 */
export function StaggerLines({
  lines,
  className,
  delay = 0,
}: {
  lines: ReactNode[];
  className?: string;
  delay?: number;
}) {
  const reduce = useReducedMotion();
  return (
    <motion.span
      className={className}
      initial={reduce ? "show" : "hidden"}
      whileInView="show"
      viewport={{ once: true, margin: "-60px" }}
      transition={{ staggerChildren: 0.12, delayChildren: delay }}
    >
      {lines.map((line, i) => (
        <span key={i} className="block overflow-hidden pb-[0.08em] -mb-[0.08em]">
          <motion.span
            className="block will-change-transform"
            variants={{
              hidden: { y: "110%", opacity: 0 },
              show: { y: "0%", opacity: 1 },
            }}
            transition={{ duration: 1, ease: EASE }}
          >
            {line}
          </motion.span>
        </span>
      ))}
    </motion.span>
  );
}

/** Eyebrow kiểu editorial: số thứ tự + nhãn mono viết hoa giãn cách. */
export function Eyebrow({
  index,
  label,
  tone = "dark",
}: {
  index: string;
  label: string;
  tone?: "dark" | "light";
}) {
  return (
    <p
      className={`font-mono text-[11px] md:text-xs uppercase tracking-[0.28em] ${
        tone === "dark" ? "text-ink-soft" : "text-cream/80"
      }`}
    >
      <span className="text-magenta">{index}</span>
      <span aria-hidden className="mx-3 opacity-40">
        —
      </span>
      {label}
    </p>
  );
}

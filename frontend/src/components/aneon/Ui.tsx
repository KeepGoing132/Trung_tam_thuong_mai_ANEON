import Link from "next/link";
import type { ReactNode } from "react";

export const input = "w-full min-w-0 rounded-xl border border-cream-line bg-white px-3.5 py-2.5 text-sm text-ink focus:border-magenta-deep focus:outline-2 focus:outline-magenta-deep";
export const primary = "inline-flex min-h-11 items-center justify-center rounded-xl bg-magenta-deep px-4 py-2.5 text-sm font-semibold text-white hover:bg-plum disabled:opacity-50";
export const secondary = "inline-flex min-h-11 items-center justify-center rounded-xl border border-cream-line bg-white px-4 py-2.5 text-sm font-semibold text-ink hover:border-magenta-deep hover:text-magenta-deep disabled:opacity-50";
export const panel = "min-w-0 rounded-2xl border border-cream-line bg-white p-5 sm:p-7";

export function TopBar() {
  return (
    <header className="border-b border-cream-line bg-cream">
      <div className="mx-auto flex min-h-18 max-w-7xl flex-wrap items-center justify-between gap-3 px-5 py-3 md:px-10">
        <Link href="/" className="font-display text-2xl font-semibold text-plum" aria-label="AEON MALL — Trang chủ">
          AEON <span className="font-sans text-xs font-bold tracking-[0.19em] text-magenta-deep">MALL</span>
        </Link>
        <nav aria-label="Điều hướng" className="flex flex-wrap items-center gap-x-5 gap-y-2 text-sm font-semibold">
          <Link href="/kham-pha" className="hover:text-magenta-deep">Sự kiện & ưu đãi</Link>
          <Link href="/check-in" className="hover:text-magenta-deep">Check-in</Link>
          <Link href="/tai-khoan" className="hover:text-magenta-deep">Tài khoản</Link>
          <Link href="/quan-ly" className="hover:text-magenta-deep">Quản lý</Link>
        </nav>
      </div>
    </header>
  );
}

export function Field({ label, children }: { label: string; children: ReactNode }) {
  return <label className="grid min-w-0 gap-1.5 text-sm font-semibold">{label}{children}</label>;
}

export function ErrorMessage({ message }: { message: string }) {
  return <p role="alert" className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-900">{message}</p>;
}

export function Pagination({ page, size, total, onPage, busy }: {
  page: number; size: number; total: number; onPage: (page: number) => void; busy?: boolean;
}) {
  if (total <= size) return null;
  return (
    <nav aria-label="Phân trang" className="mt-5 flex flex-wrap items-center justify-between gap-3">
      <button className={secondary} type="button" disabled={busy || page === 0} onClick={() => onPage(page - 1)}>Trang trước</button>
      <span className="text-sm text-ink-soft">Trang {page + 1} / {Math.ceil(total / size)}</span>
      <button className={secondary} type="button" disabled={busy || (page + 1) * size >= total} onClick={() => onPage(page + 1)}>Trang sau</button>
    </nav>
  );
}

"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { formatDate, query, request, type Page } from "@/lib/aneon-api";
import { ErrorMessage, Field, input, Pagination, primary, secondary } from "./Ui";

export type Category = "EVENT" | "PROMOTION";
type PublicItem = { id: string; title: string; category: Category; type?: string; startAt?: string; endAt?: string; location?: string };
const categories = { EVENT: "Sự kiện", PROMOTION: "Ưu đãi" };
const promotionTypes: Record<string, string> = {
  DISCOUNT: "Giảm giá", VOUCHER_GIVEAWAY: "Tặng voucher", POINT_ACCUMULATION: "Tích điểm",
};

export function Discovery({ initialCategory = "", preview = false }: { initialCategory?: Category | ""; preview?: boolean }) {
  const [filters, setFilters] = useState({ q: "", category: initialCategory, page: 0 });
  const [data, setData] = useState<Page<PublicItem>>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError("");
    setData(undefined);
    request<Page<PublicItem>>(`f10/promotions-events/search?${query({ ...filters, size: preview ? 3 : 9 })}`, { signal: controller.signal })
      .then(setData)
      .catch((err: Error) => { if (!controller.signal.aborted) setError(err.message); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [filters, preview, retry]);

  return (
    <div className="grid gap-5" aria-busy={loading}>
      {!preview && (
        <form className="grid gap-3 rounded-2xl border border-cream-line bg-white p-4 sm:grid-cols-[1fr_12rem_auto] sm:items-end" onSubmit={(event) => {
          event.preventDefault();
          const values = new FormData(event.currentTarget);
          setFilters({ q: String(values.get("q")).trim(), category: values.get("category") as Category | "", page: 0 });
        }}>
          <Field label="Từ khóa"><input name="q" type="search" maxLength={200} placeholder="Tên sự kiện hoặc ưu đãi" className={input} /></Field>
          <Field label="Danh mục"><select name="category" defaultValue={initialCategory} className={input}>
            <option value="">Tất cả</option><option value="EVENT">Sự kiện</option><option value="PROMOTION">Ưu đãi</option>
          </select></Field>
          <button className={primary} type="submit">Tìm kiếm</button>
        </form>
      )}
      {loading && <p role="status" className="text-sm text-ink-soft">Đang tải nội dung…</p>}
      {error && <div className="flex flex-wrap items-center gap-3"><ErrorMessage message={error} /><button className={secondary} onClick={() => setRetry(retry + 1)}>Thử lại</button></div>}
      {data && (
        <>
          {!preview && <p role="status" className="text-sm text-ink-soft">{data.totalElements} kết quả</p>}
          {data.content.length === 0 && <p className="rounded-2xl border border-cream-line bg-white p-6 text-ink-soft">Chưa có nội dung phù hợp.</p>}
          <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {data.content.map((item) => (
              <li key={`${item.category}-${item.id}`} className="min-w-0 rounded-2xl border border-cream-line bg-white p-5 sm:p-6">
                <span className="inline-flex rounded-full bg-magenta-deep/8 px-3 py-1 text-xs font-semibold text-magenta-deep">{categories[item.category]}</span>
                <h3 className="mt-4 font-display text-2xl leading-tight text-plum wrap-break-word">{item.title}</h3>
                {item.type && <p className="mt-2 text-sm text-ink-soft">{promotionTypes[item.type] ?? item.type}</p>}
                <dl className="mt-5 grid grid-cols-[auto_1fr] gap-x-3 gap-y-2 border-t border-cream-line pt-4 text-sm">
                  <dt className="text-ink-soft">Bắt đầu</dt><dd>{formatDate(item.startAt)}</dd>
                  <dt className="text-ink-soft">Kết thúc</dt><dd>{formatDate(item.endAt)}</dd>
                  {item.location && <><dt className="text-ink-soft">Địa điểm</dt><dd className="wrap-break-word">{item.location}</dd></>}
                </dl>
              </li>
            ))}
          </ul>
          {!preview && <Pagination page={data.page} size={data.size} total={data.totalElements} onPage={(page) => setFilters({ ...filters, page })} />}
        </>
      )}
    </div>
  );
}

export function DiscoveryPreview({ category, id }: { category: Category; id: string }) {
  return (
    <section id={id} className={`scroll-mt-20 py-20 md:py-28 ${category === "PROMOTION" ? "bg-cream-deep" : ""}`}>
      <div className="mx-auto max-w-7xl px-5 md:px-10">
        <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
          <h2 className="font-display text-4xl text-plum md:text-6xl">{categories[category]}</h2>
          <Link className={secondary} href={`/kham-pha?danh-muc=${category}`}>Xem tất cả {categories[category].toLowerCase()}</Link>
        </div>
        <Discovery initialCategory={category} preview />
      </div>
    </section>
  );
}

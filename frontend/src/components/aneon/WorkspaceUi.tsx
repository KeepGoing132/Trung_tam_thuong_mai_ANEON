"use client";

import { useEffect, useState, type FormEvent, type ReactNode } from "react";
import { query, request, UUID_PATTERN, type Identity, type Page } from "@/lib/aneon-api";
import { ErrorMessage, Field, input, Pagination, panel, primary, secondary } from "./Ui";

export type Row = Record<string, unknown>;
export type Column = [name: string, label: string, format?: (value: unknown) => ReactNode];
export type FormField = {
  name: string; label: string; type?: "uuid" | "number" | "date" | "datetime-local" | "textarea" | "checkbox" | "list";
  options?: Record<string, string>; required?: boolean; min?: number; max?: number; step?: string; pattern?: string;
};

export function allowed(identity: Identity, ...roles: string[]) {
  return identity.mode === "jwt" || roles.includes(identity.role);
}

export function selectOptions(options: Record<string, string>) {
  return Object.entries(options).map(([value, label]) => <option key={value} value={value}>{label}</option>);
}

export function display(value: unknown): ReactNode {
  if (value === null || value === undefined || value === "") return "—";
  if (typeof value === "boolean") return value ? "Có" : "Không";
  if (Array.isArray(value)) return value.join(", ") || "—";
  return String(value);
}

export function Details({ data, columns }: { data: Row; columns: Column[] }) {
  return <dl className="grid gap-x-6 gap-y-4 sm:grid-cols-2">
    {columns.map(([key, label, format]) => <div key={key} className="min-w-0 border-b border-cream-line pb-2">
      <dt className="text-xs font-semibold text-ink-soft">{label}</dt>
      <dd className="mt-1 text-sm wrap-break-word">{format ? format(data[key]) : display(data[key])}</dd>
    </div>)}
  </dl>;
}

export function useResource<T>(path: string | undefined, identity: Identity, revision = 0) {
  const [result, setResult] = useState<{ path: string; revision: number; data?: T; error?: string }>();
  useEffect(() => {
    if (!path) return;
    const controller = new AbortController();
    request<T>(path, { identity, signal: controller.signal })
      .then((data) => { if (!controller.signal.aborted) setResult({ path, revision, data }); })
      .catch((err: Error) => { if (!controller.signal.aborted) setResult({ path, revision, error: err.message }); });
    return () => controller.abort();
  }, [path, identity, revision]);
  const current = result?.path === path && result?.revision === revision ? result : undefined;
  return { data: current?.data, error: current?.error, loading: Boolean(path && !current) };
}

export function Records({ path, identity, revision, columns, filters, onSelect, extra }: {
  path: string; identity: Identity; revision: number; columns: Column[]; filters: FormField[];
  onSelect?: (id: string) => void; extra?: (data: Page<Row> & Row) => ReactNode;
}) {
  const [params, setParams] = useState<Record<string, string>>({});
  const [page, setPage] = useState(0);
  const [retry, setRetry] = useState(0);
  const { data, error, loading } = useResource<Page<Row> & Row>(`${path}?${query({ ...params, page, size: 10 })}`, identity, revision + retry);
  return <div className={`${panel} grid gap-5`}>
    <form className="flex flex-wrap items-end gap-3" onSubmit={(event) => {
      event.preventDefault();
      setPage(0);
      setParams(Object.fromEntries(Array.from(new FormData(event.currentTarget), ([name, value]) => [name, String(value).trim()])));
      setRetry((value) => value + 1);
    }}>
      {filters.map((field) => <div className="min-w-40 flex-1" key={field.name}><Control field={field} /></div>)}
      <button className={primary} type="submit">Lọc kết quả</button>
    </form>
    {loading && <p role="status" className="text-sm text-ink-soft">Đang tải dữ liệu…</p>}
    {error && <div className="flex flex-wrap items-center gap-3"><ErrorMessage message={error} /><button className={secondary} onClick={() => setRetry((value) => value + 1)}>Thử lại</button></div>}
    {data && <>
      {extra?.(data)}
      <p role="status" className="text-sm text-ink-soft">{data.totalElements} kết quả</p>
      {data.content.length > 0 ? <div className="overflow-x-auto">
        <table className="w-full border-collapse text-left text-sm">
          <thead className="bg-cream text-ink-soft"><tr>{columns.map(([key, label]) => <th key={key} scope="col" className="px-3 py-3 font-semibold">{label}</th>)}{onSelect && <th scope="col" className="px-3 py-3">Thao tác</th>}</tr></thead>
          <tbody>{data.content.map((record, index) => <tr key={String(record.id ?? index)} className="border-b border-cream-line">
            {columns.map(([key, , format]) => <td key={key} className="max-w-72 px-3 py-3 wrap-break-word">{format ? format(record[key]) : display(record[key])}</td>)}
            {onSelect && <td className="px-3 py-3"><button className="whitespace-nowrap font-semibold text-magenta-deep underline-offset-4 hover:underline" onClick={() => onSelect(String(record.id))} aria-label={`Xem chi tiết ${record.name ?? record.code}`}>Chi tiết</button></td>}
          </tr>)}</tbody>
        </table>
      </div> : <p className="text-sm text-ink-soft">Không có bản ghi phù hợp.</p>}
      <Pagination page={data.page} size={data.size} total={data.totalElements} onPage={setPage} />
    </>}
  </div>;
}

export function Control({ field, value, onChange }: { field: FormField; value?: unknown; onChange?: (value: string) => void }) {
  const { name, label, type, options, required, min, max, step, pattern } = field;
  if (type === "checkbox") return <label className="flex items-center gap-2 self-center text-sm font-semibold">
    <input name={name} type="checkbox" defaultChecked={value === true} className="size-4 accent-magenta-deep" />{label}
  </label>;
  const defaults = { name, className: input, required, defaultValue: value === undefined || value === null ? "" : String(value) };
  return <Field label={label}>
    {options ? <select {...defaults} onChange={(event) => onChange?.(event.target.value)}>
      <option value="" disabled={required}>{required ? "Chọn…" : "Tất cả / Không chọn"}</option>{selectOptions(options)}
    </select> : type === "textarea" ? <textarea {...defaults} maxLength={max} rows={3} /> : <input {...defaults}
      type={type === "uuid" || type === "list" || !type ? "text" : type}
      pattern={type === "uuid" ? UUID_PATTERN : pattern} min={min} max={type === "number" ? max : undefined}
      maxLength={!type || type === "list" ? max : undefined} step={step}
      placeholder={type === "list" ? "Phân cách bằng dấu phẩy" : undefined}
      onChange={(event) => onChange?.(event.target.value)} />}
  </Field>;
}

export function MutationForm({ identity, path, method = "POST", fields, defaults, label, build, onSaved, resultView, resetAfterSave = true }: {
  identity: Identity; path: string | ((data: Row) => string); method?: string; fields: FormField[]; defaults?: Row; label: string;
  build?: (data: Row) => unknown; onSaved?: (data: Row) => void; resultView?: (data: Row) => ReactNode;
  resetAfterSave?: boolean;
}) {
  const [result, setResult] = useState<Row>();
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    const form = event.currentTarget;
    const values = new FormData(form);
    setBusy(true);
    setError("");
    setResult(undefined);
    try {
      const data: Row = {};
      for (const { name, type, required } of fields) {
        const value = String(values.get(name) ?? "").trim();
        if (required && !value && type !== "checkbox") throw new Error("Vui lòng điền đủ các trường bắt buộc.");
        if (type === "checkbox") data[name] = values.has(name);
        else if (value !== "") data[name] = type === "number" ? Number(value)
          : type === "datetime-local" ? new Date(`${value}+07:00`).toISOString()
          : type === "list" ? value.split(",").map((item) => item.trim()).filter(Boolean) : value;
      }
      const endpoint = typeof path === "function" ? path(data) : path;
      const saved = await request<Row>(endpoint, { identity, method, body: build ? build(data) : data });
      if (resetAfterSave) form.reset();
      setResult(saved);
      onSaved?.(saved);
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return <form className="mt-4 grid gap-4" onSubmit={submit} aria-busy={busy}>
    {fields.length > 0 && <fieldset disabled={busy} className="grid gap-4 sm:grid-cols-2">
      {fields.map((field) => <Control key={field.name} field={field} value={defaults?.[field.name]} />)}
    </fieldset>}
    <button type="submit" disabled={busy} className={`${primary} justify-self-start`}>{busy ? "Đang xử lý…" : label}</button>
    {error && <ErrorMessage message={error} />}
    {result && <div role="status" className="grid gap-2 rounded-xl border border-green-200 bg-green-50 p-4 text-sm text-green-950">
      <p className="font-semibold">Đã thực hiện thành công.</p>{resultView?.(result)}
    </div>}
  </form>;
}

export function Action({ title, children }: { title: string; children: ReactNode }) {
  return <details className="rounded-xl border border-cream-line p-4 open:bg-cream/40">
    <summary className="cursor-pointer text-sm font-semibold text-plum">{title}</summary>{children}
  </details>;
}

"use client";

import { useState } from "react";
import { formatDate, query, request, type Identity } from "@/lib/aneon-api";
import { ErrorMessage, panel, secondary } from "./Ui";
import { Action, allowed, Control, Details, MutationForm, Records, useResource, type Column, type FormField, type Row } from "./WorkspaceUi";

const statuses: Record<string, string> = {
  VACANT: "Còn trống", RESERVED: "Đã giữ chỗ", LEASED: "Đã cho thuê", MAINTENANCE: "Đang bảo trì",
  SUSPENDED: "Tạm ngưng", RETIRED: "Ngừng khai thác",
};
const types = { STORE: "Cửa hàng", KIOSK: "Sạp", ADVERTISING: "Quảng cáo", EVENT_AREA: "Khu sự kiện", SERVICE_AREA: "Khu dịch vụ", OTHER: "Khác" };
const statusColumn: Column = ["status", "Trạng thái", (value) => statuses[String(value)] ?? "—"];
const status: FormField = { name: "status", label: "Trạng thái", options: statuses };
const code: FormField = { name: "code", label: "Mã", required: true, max: 50, pattern: "[A-Za-z0-9._\\-]+" };
const name: FormField = { name: "name", label: "Tên", required: true, max: 200 };
const zone: FormField = { name: "zoneId", label: "UUID khu vực", type: "uuid" };
const location: FormField = { name: "locationId", label: "UUID vị trí", type: "uuid" };
const premise: FormField = { name: "premiseId", label: "UUID mặt bằng", type: "uuid" };
const reason: FormField = { name: "reason", label: "Lý do", type: "textarea", max: 500 };
const description: FormField = { name: "description", label: "Mô tả", type: "textarea", max: 1000 };
const search: FormField = { name: "q", label: "Tên hoặc mã", max: 200 };
const premiseFields: FormField[] = [name, { ...zone, required: true },
  { name: "totalArea", label: "Tổng diện tích (m²)", type: "number", min: 0.01, step: "0.01", required: true },
  { name: "type", label: "Loại mặt bằng", options: types }, description];
const storeFields: FormField[] = [name, { ...premise, required: true },
  { name: "area", label: "Diện tích (m²)", type: "number", min: 0.01, step: "0.01", required: true },
  { name: "categoryId", label: "UUID ngành hàng", type: "uuid" }, location,
  { name: "operatingHours", label: "Giờ hoạt động", max: 200 }, description,
  { name: "publicVisible", label: "Cho phép hiển thị công khai", type: "checkbox" }];
const premiseColumns: Column[] = [["code", "Mã"], ["name", "Mặt bằng"], ["zoneName", "Khu vực"], ["floorName", "Tầng"], ["totalArea", "Diện tích (m²)"], statusColumn];
const storeColumns: Column[] = [["code", "Mã"], ["name", "Cửa hàng"], ["premiseCode", "Mã mặt bằng"], ["categoryName", "Ngành hàng"], ["area", "Diện tích (m²)"], statusColumn];
const historyTargets: Record<string, string> = { MAT_BANG: "Mặt bằng", CUA_HANG: "Cửa hàng" };
const historyActions: Record<string, string> = {
  CREATE: "Tạo mới", UPDATE: "Sửa thông tin", UPDATE_LOCATION: "Đổi vị trí",
  UPDATE_STATUS: "Đổi trạng thái", SYNC_STATUS: "Đồng bộ hợp đồng",
};

function historySnapshot(value: unknown) {
  if (typeof value !== "string" || !value) return "—";
  let formatted = value;
  try { formatted = JSON.stringify(JSON.parse(value), null, 2); } catch { /* Dữ liệu lịch sử cũ. */ }
  return <details>
    <summary className="cursor-pointer whitespace-nowrap text-magenta-deep">Xem dữ liệu</summary>
    <pre className="mt-2 max-h-56 max-w-72 overflow-auto rounded bg-cream p-2 text-xs whitespace-pre-wrap wrap-break-word">{formatted}</pre>
  </details>;
}

const historyColumns: Column[] = [
  ["createdAt", "Thời điểm", (value) => formatDate(value as string)],
  ["action", "Thao tác", (value) => historyActions[String(value)] ?? String(value)],
  ["changedBy", "UUID người thao tác"], ["reason", "Lý do"],
  ["oldValueJson", "Trước thay đổi", historySnapshot], ["newValueJson", "Sau thay đổi", historySnapshot],
];

export type PremisesView = "premises" | "stores" | "utilization" | "histories";

export function PremisesWorkspace({ view, identity }: { view: PremisesView; identity: Identity }) {
  const [selected, setSelected] = useState<string>();
  const [revision, setRevision] = useState(0);
  const [notice, setNotice] = useState("");
  const path = `f01/${view}`;
  const isStore = view === "stores";
  const columns = isStore ? storeColumns : premiseColumns;
  const detailPath = selected ? `${path}/${selected}` : undefined;
  const detail = useResource<Row>(detailPath, identity, revision);
  const canWrite = allowed(identity, "MANAGEMENT");
  const canChangeStatus = isStore ? canWrite : allowed(identity, "MANAGEMENT", "OPERATIONS");

  function saved() {
    setNotice("Đã lưu thay đổi.");
    setRevision((value) => value + 1);
  }

  if (view === "utilization") return <Records identity={identity} path={path} revision={0}
    filters={[zone, { name: "floorId", label: "UUID tầng", type: "uuid" }, status]}
    columns={[...premiseColumns, ["allocatedArea", "Đã phân bổ (m²)"]]}
    extra={(page) => page.summary ? <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
      {Object.entries({ total: "Tổng mặt bằng", occupied: "Đã cho thuê", vacant: "Còn trống", maintenance: "Đang bảo trì" }).map(([key, label]) => (
        <div key={key} className="rounded-xl bg-cream px-4 py-3"><p className="text-xs font-semibold text-ink-soft">{label}</p><p className="mt-1 font-display text-3xl text-plum">{String((page.summary as Row)[key])}</p></div>
      ))}
    </div> : null} />;

  if (view === "histories") return <Records identity={identity} path={path} revision={0}
    filters={[{ name: "targetCode", label: "Mã mặt bằng / cửa hàng", max: 50 }, { name: "targetType", label: "Đối tượng", options: historyTargets },
      { name: "action", label: "Loại thay đổi", options: historyActions }, { name: "changedBy", label: "UUID người thao tác", type: "uuid" },
      { name: "targetId", label: "UUID đối tượng", type: "uuid" },
      { name: "from", label: "Từ ngày", type: "date" }, { name: "to", label: "Đến ngày", type: "date" }]}
    columns={[["targetType", "Đối tượng", (value) => historyTargets[String(value)] ?? String(value)], ["targetId", "UUID đối tượng"], ...historyColumns]} />;

  return <div className="grid gap-5">
    {notice && <p role="status" className="text-sm font-semibold text-green-800">{notice}</p>}
    {canWrite && <div className={panel}>
      <Action title={isStore ? "Thêm cửa hàng / sạp" : "Thêm mặt bằng"}>
        {!isStore && <CodeCheck identity={identity} />}
        <MutationForm path={path} identity={identity} label={isStore ? "Tạo cửa hàng" : "Tạo mặt bằng"}
          fields={[code, ...(isStore ? storeFields : [...premiseFields, location]), { ...status, required: true }]}
          defaults={{ status: "VACANT" }} onSaved={saved} />
      </Action>
    </div>}
    <Records path={path} identity={identity} revision={revision} columns={columns}
      filters={[search, isStore ? premise : zone, status]}
      onSelect={(id) => { setSelected(id); setNotice(""); }} />
    {detailPath && <section className={`${panel} grid gap-5`} aria-label="Chi tiết bản ghi">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h3 className="font-display text-3xl text-plum">{String(detail.data?.name ?? "Chi tiết")}</h3>
        <button className={secondary} onClick={() => setSelected(undefined)}>Đóng chi tiết</button>
      </div>
      {detail.loading && <p role="status">Đang tải chi tiết…</p>}
      {detail.error && <><ErrorMessage message={detail.error} /><button className={`${secondary} justify-self-start`} onClick={() => setRevision((value) => value + 1)}>Tải lại</button></>}
      {detail.data && <>
        <Details data={detail.data} columns={[["id", "UUID"], ...columns, ["locationId", "UUID vị trí"], ["description", "Mô tả"],
          ...(isStore ? [["operatingHours", "Giờ hoạt động"], ["publicVisible", "Công khai"]] as Column[] : [["allocatedArea", "Đã phân bổ (m²)"]] as Column[])]} />
        <div className="flex flex-wrap gap-3"><button className={secondary} onClick={() => setRevision((value) => value + 1)}>Tải lại chi tiết</button></div>
        {canWrite && <Action title="Chỉnh sửa thông tin">
          <MutationForm key={`${detailPath}-${revision}`} path={detailPath} method="PUT" identity={identity} label="Lưu chỉnh sửa"
            fields={isStore ? storeFields : premiseFields} defaults={detail.data} build={(body) => ({ ...body, version: detail.data?.version })} onSaved={saved} />
        </Action>}
        {canChangeStatus && detail.data.status !== "RETIRED" && <Action title="Đổi trạng thái">
          <MutationForm path={`${detailPath}/status`} method="PATCH" identity={identity} label="Cập nhật trạng thái"
            fields={[{ ...status, required: true, options: Object.fromEntries(Object.entries(statuses).filter(([key]) => key !== detail.data?.status)) }, reason]}
            build={(body) => ({ ...body, version: detail.data?.version })} onSaved={saved} />
        </Action>}
        {canWrite && !isStore && <Action title="Gắn vị trí mặt bằng">
          <MutationForm path={`${detailPath}/location`} method="PUT" identity={identity} label="Lưu vị trí"
            fields={[{ ...location, required: true }, reason]} defaults={detail.data} onSaved={saved} />
        </Action>}
        {canChangeStatus && !isStore && <Action title="Đồng bộ trạng thái theo hợp đồng">
          <MutationForm path={`${detailPath}/sync-contract-status`} identity={identity} label="Đồng bộ hợp đồng"
            fields={[{ name: "contractId", label: "UUID hợp đồng", type: "uuid", required: true }]} onSaved={saved} />
        </Action>}
      </>}
    </section>}
  </div>;
}

function CodeCheck({ identity }: { identity: Identity }) {
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  return <form className="mt-4 grid gap-2 rounded-xl bg-cream p-3" onSubmit={async (event) => {
    event.preventDefault();
    const value = String(new FormData(event.currentTarget).get("code")).trim();
    setBusy(true); setError(""); setMessage("");
    try {
      const data = await request<{ available: boolean; code: string }>(`f01/premises/code-availability?${query({ code: value })}`, { identity });
      setMessage(data.available ? `Có thể dùng mã ${data.code}.` : `Mã ${data.code} đã được sử dụng.`);
    } catch (err) {
      setError((err as Error).message);
    } finally { setBusy(false); }
  }}>
    <Control field={{ ...code, label: "Kiểm tra mã mặt bằng" }} />
    <button className={`${secondary} justify-self-start`} disabled={busy} type="submit">Kiểm tra mã</button>
    {message && <p role="status" className="text-sm">{message}</p>}{error && <ErrorMessage message={error} />}
  </form>;
}

"use client";

import { useState } from "react";
import qrcode from "qrcode-generator";
import { formatDate, type Identity } from "@/lib/aneon-api";
import { ErrorMessage, panel, secondary } from "./Ui";
import { Action, allowed, Details, MutationForm, Records, useResource, type Column, type FormField, type Row } from "./WorkspaceUi";
import { EventAttachments } from "./EventAttachments";

const statuses: Record<string, string> = {
  DRAFT: "Bản nháp", PENDING_APPROVAL: "Chờ duyệt", APPROVED: "Đã duyệt", REJECTED: "Đã từ chối",
  ONGOING: "Đang diễn ra", FINISHED: "Đã kết thúc", CANCELLED: "Đã hủy",
};
const amount = (value: unknown) => typeof value === "number" ? new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value) : "—";
const date = (value: unknown) => formatDate(value as string | undefined);
const eventPath = "f10/events";
const location: FormField = { name: "locationId", label: "UUID địa điểm", type: "uuid", required: true };
const starts: FormField = { name: "startAt", label: "Bắt đầu (giờ Việt Nam)", type: "datetime-local", required: true };
const ends: FormField = { ...starts, name: "endAt", label: "Kết thúc (giờ Việt Nam)" };
const description: FormField = { name: "description", label: "Mô tả", type: "textarea", max: 1000 };
const eventName: FormField = { name: "name", label: "Tên sự kiện", max: 200, required: true };
const activityFields: FormField[] = [{ ...eventName, label: "Tên hoạt động" }, location, starts, ends,
  { name: "personInCharge", label: "UUID người phụ trách", type: "uuid" }, description];
const proposalFields: FormField[] = [
  { name: "code", label: "Mã sự kiện", max: 50, required: true, pattern: "[A-Za-z0-9._\\-]+" }, eventName,
  { name: "type", label: "Loại sự kiện", max: 100 }, location, starts, ends,
  { name: "estimatedBudget", label: "Ngân sách dự kiến (₫)", type: "number", min: 0, required: true },
  { name: "targetAttendees", label: "Số khách mục tiêu", type: "number", min: 0, required: true },
  { name: "participatingStoreIds", label: "UUID các gian hàng tham gia", type: "list" },
  { name: "publicVisible", label: "Hiển thị công khai sau khi duyệt", type: "checkbox" },
];
const eventColumns: Column[] = [["code", "Mã"], ["name", "Sự kiện"], ["status", "Trạng thái", (value) => statuses[String(value)] ?? String(value)],
  ["estimatedBudget", "Dự toán", amount], ["createdAt", "Ngày đề xuất", date]];

function dateInput(value: unknown) {
  return typeof value === "string" ? new Date(Date.parse(value) + 7 * 60 * 60 * 1000).toISOString().slice(0, 16) : "";
}

export function EventsWorkspace({ identity }: { identity: Identity }) {
  const [selected, setSelected] = useState<string>();
  const [revision, setRevision] = useState(0);
  const [notice, setNotice] = useState("");
  const detailPath = selected ? `${eventPath}/${selected}` : undefined;
  const detail = useResource<Row>(detailPath, identity, revision);
  const canMarket = allowed(identity, "MARKETING");
  const canApprove = allowed(identity, "MANAGEMENT", "DIRECTOR");
  const canReport = allowed(identity, "MANAGEMENT", "DIRECTOR", "MARKETING");
  const ownsDraft = detail.data?.status === "DRAFT" && canMarket && (identity.mode === "jwt" || identity.actorId === detail.data.proposedBy);

  function saved() {
    setRevision((value) => value + 1);
    setNotice("Đã lưu thay đổi cho sự kiện.");
  }

  return <div className="grid gap-5">
    {notice && <p role="status" className="font-semibold text-green-800">{notice}</p>}
    {canMarket && <div className={`${panel} grid gap-3`}><Action title="Đề xuất sự kiện">
      <MutationForm path={`${eventPath}/proposals`} identity={identity} fields={proposalFields} label="Gửi đề xuất"
        onSaved={(data) => { setSelected(String(data.id)); saved(); }} />
    </Action><Action title="Tạo bản nháp">
      <MutationForm path={`${eventPath}/drafts`} identity={identity} fields={proposalFields} label="Lưu bản nháp"
        onSaved={(data) => { setSelected(String(data.id)); saved(); }} />
    </Action></div>}
    <Records path={eventPath} identity={identity} revision={revision} columns={eventColumns}
      filters={[{ name: "q", label: "Tên hoặc mã sự kiện", max: 200 }, { name: "status", label: "Trạng thái", options: statuses }]}
      onSelect={(id) => { setSelected(id); setNotice(""); }} />
    {detailPath && <section className={`${panel} grid gap-4`} aria-label="Chi tiết sự kiện">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h3 className="font-display text-3xl text-plum">{String(detail.data?.name ?? "Chi tiết sự kiện")}</h3>
        <button className={secondary} onClick={() => setSelected(undefined)}>Đóng chi tiết</button>
      </div>
      {detail.loading && <p role="status">Đang tải chi tiết…</p>}
      {detail.error && <ErrorMessage message={detail.error} />}
      {detail.data && <>
        <Details data={detail.data} columns={[...eventColumns.filter(([key]) => key !== "createdAt"),
          ["id", "UUID sự kiện"], ["locationDescription", "Địa điểm"], ["locationId", "UUID địa điểm"], ["startAt", "Bắt đầu", date], ["endAt", "Kết thúc", date],
          ["approvedBudget", "Ngân sách đã duyệt", amount], ["targetAttendees", "Số khách mục tiêu"], ["publicVisible", "Công khai"], ["rejectionReason", "Lý do từ chối"]]} />
        {ownsDraft && <>
          <Action title="Chỉnh sửa bản nháp">
            <MutationForm identity={identity} path={`${detailPath}/draft?version=${detail.data.version}`} method="PUT"
              fields={proposalFields.filter((field) => field.name !== "code")} label="Lưu chỉnh sửa"
              defaults={{ ...detail.data, startAt: dateInput(detail.data.startAt), endAt: dateInput(detail.data.endAt) }}
              build={(body) => ({ ...body, code: detail.data?.code })} onSaved={saved} />
          </Action>
          <Action title="Gửi bản nháp để duyệt">
            <MutationForm identity={identity} path={`${detailPath}/submission?version=${detail.data.version}`}
              fields={[]} label="Gửi duyệt" onSaved={saved} />
          </Action>
        </>}
        {(detail.data.status !== "DRAFT" || ownsDraft) && <EventAttachments key={detailPath} path={`${detailPath}/files`} identity={identity} canEdit={ownsDraft} />}
        {canApprove && detail.data.status === "PENDING_APPROVAL" && <>
          <Action title="Duyệt đề xuất"><MutationForm identity={identity} path={`${detailPath}/approval`} method="PUT" label="Duyệt sự kiện"
            fields={[{ name: "approvedBudget", label: "Ngân sách duyệt (₫)", type: "number", min: 1, required: true }, { name: "note", label: "Ghi chú", max: 500 }]}
            build={(body) => ({ ...body, action: "APPROVE", version: detail.data?.version })} onSaved={saved} /></Action>
          <Action title="Từ chối đề xuất"><MutationForm identity={identity} path={`${detailPath}/approval`} method="PUT" label="Từ chối sự kiện"
            fields={[{ name: "rejectionReason", label: "Lý do từ chối", type: "textarea", max: 500, required: true }]}
            build={(body) => ({ ...body, action: "REJECT", version: detail.data?.version })} onSaved={saved} /></Action>
        </>}
        <ActivitySchedule key={detailPath} identity={identity} path={`${detailPath}/activities`} locationId={detail.data.locationId}
          canEdit={canMarket && ["PENDING_APPROVAL", "APPROVED"].includes(String(detail.data.status))} />
        {canMarket && ["APPROVED", "ONGOING"].includes(String(detail.data.status)) && <Action title="Tạo mã QR check-in">
          <p className="mt-3 text-sm text-ink-soft">Khi tạo mã mới, mã check-in trước đó sẽ hết hiệu lực.</p>
          <MutationForm identity={identity} path={`${detailPath}/qr-code`} label="Tạo mã QR" fields={[]} resultView={(data) => <QrResult data={data} />} />
        </Action>}
        {canMarket && <Action title="Ghi chi phí"><MutationForm identity={identity} path={`${detailPath}/expenses`} label="Ghi chi phí" fields={[
          { name: "supplierName", label: "Nhà cung cấp", max: 200 }, { name: "invoiceNumber", label: "Số hóa đơn", max: 100 },
          { name: "amount", label: "Số tiền (₫)", type: "number", required: true, min: 0.01, step: "0.01" },
          { name: "incurredAt", label: "Thời điểm phát sinh (giờ Việt Nam)", type: "datetime-local" },
          { name: "attachmentId", label: "UUID tệp đính kèm đã có", type: "uuid" }, description,
        ]} /></Action>}
        {canReport && <Report identity={identity} path={`${detailPath}/report`} key={detailPath} />}
      </>}
    </section>}
  </div>;
}

function ActivitySchedule({ identity, path, locationId, canEdit }: {
  identity: Identity; path: string; locationId: unknown; canEdit: boolean;
}) {
  const [revision, setRevision] = useState(0);
  const list = useResource<Row[]>(path, identity, revision);
  const refresh = () => setRevision((value) => value + 1);
  return <Action title="Lịch hoạt động">
    <div className="mt-4 grid gap-3">
      {list.loading && <p role="status" className="text-sm">Đang tải lịch hoạt động…</p>}
      {list.error && <ErrorMessage message={list.error} />}
      {list.data?.length === 0 && <p className="text-sm">Chưa có hoạt động.</p>}
      {list.data?.map((item) => {
        const editPath = `${path}/${item.id}?version=${item.version}`;
        return <details key={String(item.id)} className="rounded-lg border border-cream-line p-3">
          <summary className="cursor-pointer text-sm font-semibold">{String(item.name)} · {date(item.startAt)} – {date(item.endAt)}</summary>
          <div className="mt-3 grid gap-3">
            <Details data={item} columns={[["locationId", "UUID địa điểm"], ["personInCharge", "UUID người phụ trách"], ["description", "Mô tả"]]} />
            {canEdit && <>
              <MutationForm key={editPath} identity={identity} path={editPath} method="PUT" label="Lưu chỉnh sửa"
                fields={activityFields} defaults={{ ...item, startAt: dateInput(item.startAt), endAt: dateInput(item.endAt) }} onSaved={refresh} />
              <Action title="Gỡ hoạt động khỏi lịch"><MutationForm identity={identity} path={editPath} method="DELETE"
                label="Xác nhận gỡ hoạt động" fields={[]} onSaved={refresh} /></Action>
            </>}
          </div>
        </details>;
      })}
      {canEdit && <Action title="Thêm hoạt động"><MutationForm identity={identity} path={path} label="Lưu hoạt động"
        defaults={{ locationId }} fields={activityFields} build={(body) => ({ activities: [body] })} onSaved={refresh} /></Action>}
    </div>
  </Action>;
}

function QrResult({ data }: { data: Row }) {
  const url = String(data.checkInUrl ?? "");
  let image: string | undefined;
  try {
    const qr = qrcode(0, "M");
    qr.addData(url);
    qr.make();
    image = qr.createDataURL(5, 20);
  } catch {
    // Liên kết vẫn dùng được nếu dài hơn dung lượng của mã QR.
  }
  return <>
    {image && <img src={image} alt="Mã QR để khách quét và check-in" className="h-auto max-w-full rounded border border-green-300" />}
    <p>Hết hạn {date(data.expiresAt)}</p>
    <label className="grid gap-1.5">Liên kết dành cho khách<input className="w-full rounded border border-green-300 bg-white px-2 py-1" value={url} readOnly onFocus={(event) => event.currentTarget.select()} /></label>
  </>;
}

function Report({ identity, path }: { identity: Identity; path: string }) {
  const [load, setLoad] = useState(0);
  const report = useResource<Row>(load ? path : undefined, identity, load);
  return <Action title="Báo cáo sự kiện">
    <button className={`${secondary} my-4`} disabled={report.loading} onClick={() => setLoad((value) => value + 1)}>{report.loading ? "Đang tải…" : "Tải báo cáo"}</button>
    {report.error && <ErrorMessage message={report.error} />}
    {report.data && <Details data={report.data} columns={[["approvedBudget", "Ngân sách đã duyệt", amount], ["estimatedBudget", "Ngân sách dự kiến", amount],
      ["totalActualExpense", "Tổng chi thực tế", amount], ["budgetVariance", "Chênh lệch ngân sách", amount], ["targetAttendees", "Số khách mục tiêu"],
      ["actualAttendees", "Số khách đã check-in"], ["attendanceRate", "Tỷ lệ tham gia (%)"], ["roiPercentage", "ROI (%)"], ["roiStatus", "Trạng thái ROI"]]} />}
  </Action>;
}

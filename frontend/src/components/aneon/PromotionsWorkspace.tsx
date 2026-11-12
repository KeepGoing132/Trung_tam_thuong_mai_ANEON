"use client";

import { useState } from "react";
import { formatDate, type Identity } from "@/lib/aneon-api";
import { ErrorMessage, panel, secondary } from "./Ui";
import { Action, allowed, Control, Details, MutationForm, useResource, type FormField, type Row } from "./WorkspaceUi";

const promotionTypes = { DISCOUNT: "Giảm giá", VOUCHER_GIVEAWAY: "Tặng voucher", POINT_ACCUMULATION: "Tích điểm" };
type PromotionType = keyof typeof promotionTypes;
const name: FormField = { name: "name", label: "Tên", required: true, max: 200 };
const code: FormField = { name: "code", label: "Mã", required: true, max: 50, pattern: "[A-Za-z0-9._\\-]+" };
const starts: FormField = { name: "startAt", label: "Bắt đầu (giờ Việt Nam)", type: "datetime-local", required: true };
const ends: FormField = { ...starts, name: "endAt", label: "Kết thúc (giờ Việt Nam)" };
const minimum: FormField = { name: "minimumInvoiceAmount", label: "Giá trị hóa đơn tối thiểu (₫)", type: "number", min: 0 };
const stores: FormField = { name: "participatingStoreIds", label: "UUID các gian hàng áp dụng", type: "list" };
const voucher: FormField = { name: "voucherId", label: "UUID mẫu voucher", type: "uuid", required: true };
const customer: FormField = { name: "customerId", label: "UUID khách mua sắm", type: "uuid", required: true };
const invoice: FormField = { name: "invoiceId", label: "UUID hóa đơn", type: "uuid", required: true };
const promotion: FormField = { name: "promotionId", label: "UUID chương trình", type: "uuid", required: true };
const discountLimit: FormField = { name: "maximumDiscount", label: "Mức giảm tối đa (₫)", type: "number", min: 0 };
const voucherInstance: FormField = { name: "customerVoucherId", label: "UUID voucher của khách", type: "uuid" };
const key: FormField = { name: "idempotencyKey", label: "Mã giao dịch (dùng lại khi gửi lại cùng giao dịch)", max: 120, required: true };

const typeFields: Record<PromotionType, FormField[]> = {
  DISCOUNT: [{ name: "discountRatePercent", label: "Tỷ lệ giảm (%)", type: "number", min: 0.01, max: 100, step: "0.01", required: true }, discountLimit],
  VOUCHER_GIVEAWAY: [voucher, { name: "totalToIssue", label: "Tổng số voucher phát hành", type: "number", min: 0, required: true }],
  POINT_ACCUMULATION: [
    { name: "rewardPoints", label: "Điểm thưởng cố định", type: "number", min: 0, required: true },
    { name: "conversionAmount", label: "Mức tiền quy đổi (₫)", type: "number", min: 1, required: true },
    { name: "pointsPerConversion", label: "Điểm trên mỗi mức quy đổi", type: "number", min: 1, required: true },
  ],
};

export function PromotionsWorkspace({ identity, view }: { identity: Identity; view: "promotions" | "vouchers" }) {
  const [type, setType] = useState<PromotionType>("DISCOUNT");
  const [promotionId, setPromotionId] = useState("");
  const [realtime, setRealtime] = useState({ id: "", revision: 0 });
  const realtimeData = useResource<Row>(realtime.id ? `f10/promotions/${encodeURIComponent(realtime.id)}/realtime` : undefined, identity, realtime.revision);
  const canCreatePromotion = allowed(identity, "MANAGEMENT", "MARKETING");
  const canApply = allowed(identity, "MARKETING", "OPERATIONS");

  if (view === "vouchers") return <div className={`${panel} grid gap-4`}>
    {allowed(identity, "MANAGEMENT") && <Action title="Tạo mẫu voucher">
      <MutationForm identity={identity} path="f10/vouchers" label="Tạo mẫu voucher" fields={[
        { ...code, pattern: "[A-Z0-9]+" }, name,
        { name: "discountType", label: "Loại giảm", options: { FIXED_AMOUNT: "Số tiền cố định", PERCENTAGE: "Phần trăm" }, required: true },
        { name: "value", label: "Giá trị giảm", type: "number", min: 0.01, step: "0.01", required: true }, discountLimit, minimum,
        { name: "issuedLimit", label: "Giới hạn tổng (0 = không giới hạn)", type: "number", min: 0 },
        { name: "perCustomerLimit", label: "Giới hạn mỗi khách (0 = không giới hạn)", type: "number", min: 0 },
        { ...starts, name: "validFrom" }, { ...ends, name: "expiresAt", label: "Hết hạn (giờ Việt Nam)" }, stores,
      ]} resultView={(data) => <Details data={data} columns={[["id", "UUID mẫu voucher"], ["code", "Mã voucher"], ["name", "Tên voucher"]]} />} />
    </Action>}
    {allowed(identity, "MARKETING", "OPERATIONS") && <>
      <Action title="Phát voucher cho khách"><MutationForm identity={identity}
        path={(data) => `f10/vouchers/${encodeURIComponent(String(data.voucherId))}/issues`} label="Phát voucher"
        fields={[voucher, customer, { name: "issueReference", label: "Mã tham chiếu lần phát", max: 200 }]}
        build={({ voucherId: _voucher, ...body }) => body}
        resultView={(data) => <Details data={data} columns={[["id", "UUID voucher của khách"], ["voucherCode", "Mã voucher"], ["duplicate", "Đã phát trước đó"]]} />} /></Action>
      <Action title="Sử dụng voucher"><MutationForm identity={identity}
        path={(data) => `f10/voucher-instances/${encodeURIComponent(String(data.customerVoucherId))}/redemptions`} label="Xác nhận sử dụng"
        fields={[{ ...voucherInstance, required: true }, invoice, { ...key, required: false }]}
        build={({ customerVoucherId: _voucher, ...body }) => body}
        resultView={(data) => <Details data={data} columns={[["status", "Trạng thái"], ["redeemedAt", "Thời gian sử dụng", (value) => formatDate(value as string)]]} />} /></Action>
    </>}
  </div>;

  return <div className={`${panel} grid gap-4`}>
    {canCreatePromotion && <Action title="Tạo chương trình khuyến mãi">
      <div className="mt-4 max-w-sm"><Control field={{ name: "type", label: "Loại khuyến mãi", options: promotionTypes, required: true }}
        value={type} onChange={(value) => { if (value) setType(value as PromotionType); }} /></div>
      <MutationForm identity={identity} path="f10/promotions" label="Tạo chương trình"
        fields={[code, name, starts, ends, minimum, stores, { name: "targetTiers", label: "Hạng thành viên áp dụng", type: "list" },
          { name: "terms", label: "Điều kiện áp dụng", type: "textarea", max: 4000 },
          { name: "publicVisible", label: "Hiển thị công khai", type: "checkbox" }, ...typeFields[type]]}
        build={(data) => {
          const { discountRatePercent, maximumDiscount, voucherId, totalToIssue, rewardPoints, conversionAmount, pointsPerConversion, ...body } = data;
          return {
            ...body, type,
            discount: type === "DISCOUNT" ? { discountRatePercent, maximumDiscount } : undefined,
            giveaway: type === "VOUCHER_GIVEAWAY" ? { voucherId, totalToIssue } : undefined,
            points: type === "POINT_ACCUMULATION" ? { rewardPoints, conversionAmount, pointsPerConversion } : undefined,
          };
        }} onSaved={(data) => setPromotionId(String(data.id))}
        resultView={(data) => <Details data={data} columns={[["id", "UUID chương trình"], ["name", "Tên chương trình"], ["status", "Trạng thái"]]} />}
      />
    </Action>}
    {allowed(identity, "MANAGEMENT", "DIRECTOR", "MARKETING", "SYSTEM_ADMIN") && <Action title="Theo dõi số liệu khuyến mãi">
      <form className="my-4 flex flex-wrap items-end gap-3" onSubmit={(event) => {
        event.preventDefault();
        const id = String(new FormData(event.currentTarget).get("promotionId")).trim();
        setRealtime({ id, revision: realtime.revision + 1 });
      }}>
        <div className="min-w-56 flex-1"><Control key={promotionId} field={promotion} value={promotionId} /></div>
        <button type="submit" className={secondary} disabled={realtimeData.loading}>{realtimeData.loading ? "Đang tải…" : "Tra cứu số liệu"}</button>
      </form>
      {realtimeData.error && <ErrorMessage message={realtimeData.error} />}
      {realtimeData.data && <Details data={realtimeData.data} columns={[["promotionId", "UUID chương trình"], ["totalLimit", "Hạn mức"], ["issuedCount", "Đã phát"], ["remainingStock", "Tồn kho"],
        ["usedCount", "Đã sử dụng"], ["totalDiscountGranted", "Tổng tiền giảm (₫)"], ["totalPointsGranted", "Tổng điểm đã cấp"], ["lastUpdated", "Cập nhật", (value) => formatDate(value as string)]]} />}
    </Action>}
    {canApply && <Action title="Áp dụng khuyến mãi vào hóa đơn">
      <MutationForm identity={identity} path={(data) => `f10/promotions/${encodeURIComponent(String(data.promotionId))}/applications`} label="Áp dụng khuyến mãi"
        fields={[promotion, invoice, customer, voucherInstance, key]} build={({ promotionId: _promotion, ...body }) => body}
        resultView={(data) => <Details data={data} columns={[["discountAmount", "Số tiền giảm (₫)"], ["pointsGranted", "Điểm được cấp"], ["duplicate", "Đã áp dụng trước đó"]]} />}
      />
    </Action>}
  </div>;
}

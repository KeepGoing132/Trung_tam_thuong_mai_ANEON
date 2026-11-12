"use client";

import { useState } from "react";
import { formatDate, type Identity } from "@/lib/aneon-api";
import { EventsWorkspace } from "./EventsWorkspace";
import { IdentityForm, staffRoles } from "./IdentityForm";
import { PremisesWorkspace, type PremisesView } from "./PremisesWorkspace";
import { PromotionsWorkspace } from "./PromotionsWorkspace";
import { panel, secondary } from "./Ui";
import { allowed, MutationForm } from "./WorkspaceUi";

type View = PremisesView | "events" | "promotions" | "vouchers" | "assist";
const staffReaders = ["MANAGEMENT", "OPERATIONS", "DIRECTOR", "ACCOUNTING", "SYSTEM_ADMIN"];
const views: { id: View; label: string; group: "F01" | "F10"; roles: string[] }[] = [
  { id: "premises", label: "Mặt bằng", group: "F01", roles: staffReaders },
  { id: "stores", label: "Cửa hàng / sạp", group: "F01", roles: staffReaders },
  { id: "utilization", label: "Tình hình khai thác", group: "F01", roles: ["MANAGEMENT", "OPERATIONS", "DIRECTOR"] },
  { id: "histories", label: "Lịch sử thay đổi", group: "F01", roles: ["MANAGEMENT", "OPERATIONS", "DIRECTOR", "SYSTEM_ADMIN"] },
  { id: "events", label: "Sự kiện", group: "F10", roles: ["MANAGEMENT", "DIRECTOR", "MARKETING", "SYSTEM_ADMIN"] },
  { id: "promotions", label: "Khuyến mãi", group: "F10", roles: ["MANAGEMENT", "DIRECTOR", "MARKETING", "OPERATIONS", "SYSTEM_ADMIN"] },
  { id: "vouchers", label: "Voucher", group: "F10", roles: ["MANAGEMENT", "OPERATIONS", "MARKETING"] },
  { id: "assist", label: "Hỗ trợ check-in", group: "F10", roles: ["OPERATIONS", "MARKETING"] },
];
const groups = { F01: "Quản lý mặt bằng", F10: "Sự kiện & khuyến mãi" } as const;

export function Workspace() {
  const [identity, setIdentity] = useState<Identity>();
  const [view, setView] = useState<View>();

  if (!identity) return <section className={`${panel} max-w-xl`}>
    <h2 className="mb-2 text-lg font-semibold">Kết nối tài khoản nhân viên</h2>
    <p className="mb-5 text-sm text-ink-soft">Dùng tài khoản được cấp quyền cho nghiệp vụ bạn phụ trách.</p>
    <IdentityForm onConnect={(next) => { setView(undefined); setIdentity(next); }} />
  </section>;

  const available = views.filter(({ roles }) => allowed(identity, ...roles));
  const current = available.find(({ id }) => id === view) ?? available[0];
  const role = identity.mode === "local" ? staffRoles[identity.role as keyof typeof staffRoles] : undefined;

  return <div className="grid items-start gap-5 lg:grid-cols-[15rem_minmax(0,1fr)]">
    <aside className={panel}>
      <div className="mb-5 border-b border-cream-line pb-4">
        <p className="text-sm font-semibold text-plum">{role ? `${role} · Local` : "Phiên dùng token"}</p>
        <button className="mt-2 text-sm font-semibold text-magenta-deep underline underline-offset-4" onClick={() => setIdentity(undefined)}>Kết thúc phiên</button>
      </div>
      <nav aria-label="Nghiệp vụ" className="grid gap-5 sm:grid-cols-2 lg:grid-cols-1">
        {(Object.keys(groups) as (keyof typeof groups)[]).map((group) => {
          const items = available.filter((item) => item.group === group);
          return items.length > 0 && <div key={group}>
            <p className="mb-2 text-xs font-bold text-ink-soft">{group} · {groups[group]}</p>
            <div className="grid gap-1">{items.map(({ id, label }) => (
              <button key={id} type="button" onClick={() => setView(id)} aria-current={current?.id === id ? "page" : undefined}
                className={`rounded-lg px-3 py-2.5 text-left text-sm font-semibold ${current?.id === id ? "bg-plum text-white" : "hover:bg-cream"}`}>{label}</button>
            ))}</div>
          </div>;
        })}
      </nav>
    </aside>
    <section className="grid min-w-0 gap-4" aria-labelledby="view-heading">
      <h2 id="view-heading" className="font-display text-3xl text-plum">{current?.label ?? "Nghiệp vụ"}</h2>
      {!current && <p className={panel}>Vai trò này chưa có quyền trên màn hình quản lý.</p>}
      {current?.group === "F01" && <PremisesWorkspace identity={identity} view={current.id as PremisesView} key={current.id} />}
      {current?.id === "events" && <EventsWorkspace identity={identity} />}
      {(current?.id === "promotions" || current?.id === "vouchers") && <PromotionsWorkspace identity={identity} view={current.id} key={current.id} />}
      {current?.id === "assist" && <div className={panel}>
        <MutationForm identity={identity} label="Xác nhận check-in cho khách" fields={[
          { name: "eventId", label: "UUID sự kiện", type: "uuid", required: true },
          { name: "customerId", label: "UUID khách mua sắm", type: "uuid", required: true },
          { name: "qrToken", label: "Mã từ liên kết check-in", max: 200, required: true },
        ]} path={(data) => `f10/events/${encodeURIComponent(String(data.eventId))}/check-ins`}
          build={({ eventId: _event, ...body }) => body} resultView={(data) => <p>{data.duplicate ? "Khách đã check-in lúc" : "Thời điểm check-in"} {formatDate(data.checkInAt as string)}</p>} />
      </div>}
    </section>
  </div>;
}

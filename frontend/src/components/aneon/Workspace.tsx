"use client";

import Image from "next/image";
import Link from "next/link";
import { useState } from "react";
import { formatDate } from "@/lib/aneon-api";
import { AccountSecurity } from "./AccountSecurity";
import { EventsWorkspace } from "./EventsWorkspace";
import { IdentityForm, staffRoles } from "./IdentityForm";
import { PremisesWorkspace, type PremisesView } from "./PremisesWorkspace";
import { PromotionsWorkspace } from "./PromotionsWorkspace";
import { ErrorMessage, panel, secondary } from "./Ui";
import { allowed, MutationForm } from "./WorkspaceUi";
import { StaffAccounts } from "./StaffAccounts";
import { useAneonIdentity } from "./useAneonIdentity";

type View = PremisesView | "events" | "promotions" | "vouchers" | "assist" | "staff";
const staffReaders = ["MANAGEMENT", "OPERATIONS", "DIRECTOR", "ACCOUNTING", "SYSTEM_ADMIN"];
const views: { id: View; label: string; group: "F01" | "F10" | "F16"; roles: string[] }[] = [
  { id: "staff", label: "Tài khoản nhân viên", group: "F16", roles: ["SYSTEM_ADMIN"] },
  { id: "premises", label: "Mặt bằng", group: "F01", roles: staffReaders },
  { id: "stores", label: "Cửa hàng / sạp", group: "F01", roles: staffReaders },
  { id: "utilization", label: "Tình hình khai thác", group: "F01", roles: ["MANAGEMENT", "OPERATIONS", "DIRECTOR"] },
  { id: "histories", label: "Lịch sử thay đổi", group: "F01", roles: ["MANAGEMENT", "OPERATIONS", "DIRECTOR", "SYSTEM_ADMIN"] },
  { id: "events", label: "Sự kiện", group: "F10", roles: ["MANAGEMENT", "DIRECTOR", "MARKETING", "SYSTEM_ADMIN"] },
  { id: "promotions", label: "Khuyến mãi", group: "F10", roles: ["MANAGEMENT", "DIRECTOR", "MARKETING", "OPERATIONS", "SYSTEM_ADMIN"] },
  { id: "vouchers", label: "Voucher", group: "F10", roles: ["MANAGEMENT", "OPERATIONS", "MARKETING"] },
  { id: "assist", label: "Hỗ trợ check-in", group: "F10", roles: ["OPERATIONS", "MARKETING"] },
];
const groups = { F16: "Phân quyền", F01: "Quản lý mặt bằng", F10: "Sự kiện & khuyến mãi" } as const;
const atriumPhoto = "/aneon/mall-atrium.webp";
const eventPhoto = "/aneon/mall-event.webp";

export function Workspace() {
  const { identity, setIdentity, loading, error, signingOut, signOut } = useAneonIdentity();
  const [view, setView] = useState<View>();

  if (!identity) return (
    <div className="grid overflow-hidden rounded-[1.75rem] border border-cream-line bg-white shadow-sm md:grid-cols-[minmax(0,1.35fr)_minmax(0,1fr)]">
      <div className="relative isolate flex min-h-52 items-end overflow-hidden bg-plum p-6 text-white sm:min-h-64 sm:p-8 md:min-h-[33rem] lg:p-11">
        <Image src={atriumPhoto} alt="" fill priority unoptimized sizes="(min-width: 768px) 58vw, 100vw"
          className="-z-20 object-cover object-[60%_54%]" />
        <div className="absolute inset-0 -z-10 bg-linear-to-t from-[#21111de8] via-[#35172e33] to-transparent" />
        <h1 className="font-display text-4xl leading-tight tracking-tight sm:text-5xl lg:text-6xl">Quản lý ANEON</h1>
      </div>
      <section aria-labelledby="identity-heading" className="flex items-center p-6 sm:p-8 lg:p-11">
        <div className="mx-auto w-full max-w-md">
          <h2 id="identity-heading" className="mb-2 font-display text-2xl text-plum sm:text-3xl">Kết nối tài khoản nhân viên</h2>
          <p className="mb-6 text-sm leading-relaxed text-ink-soft">Dùng tài khoản được cấp quyền cho nghiệp vụ bạn phụ trách.</p>
          {error && <div className="mb-4"><ErrorMessage message={error} /></div>}
          {loading ? <p role="status" className="text-sm">Đang kiểm tra phiên đăng nhập…</p>
            : <IdentityForm onConnect={(next) => { setView(undefined); setIdentity(next); }} />}
        </div>
      </section>
    </div>
  );

  if (identity.mode === "session" && identity.mustChangePassword) return <div className="mx-auto grid max-w-2xl gap-4">
    <h1 className="font-display text-4xl text-plum">Quản lý ANEON</h1>
    {error && <ErrorMessage message={error} />}
    <AccountSecurity identity={identity} onChanged={setIdentity} />
    <button className={`${secondary} justify-self-start`} type="button" disabled={signingOut} onClick={() => void signOut()}>Đăng xuất</button>
  </div>;

  const available = views.filter(({ id, roles }) => allowed(identity, ...roles) && (id !== "staff" || identity.mode === "session"));
  const current = available.find(({ id }) => id === view) ?? available[0];
  const role = identity.mode === "jwt" ? undefined : staffRoles[identity.role as keyof typeof staffRoles];

  return <div className="grid items-start gap-5 lg:grid-cols-[15rem_minmax(0,1fr)]">
    <aside className={panel}>
      <div className="mb-5 border-b border-cream-line pb-4">
        <p className="text-sm font-semibold text-plum">{identity.mode === "session" ? identity.fullName || identity.username : role ? `${role} · Local` : "Phiên dùng token"}</p>
        {identity.mode === "session" && <Link href="/tai-khoan" className="mt-2 block text-sm font-semibold text-magenta-deep underline underline-offset-4">Bảo mật tài khoản</Link>}
        <button type="button" disabled={signingOut} className="mt-2 text-sm font-semibold text-magenta-deep underline underline-offset-4" onClick={() => void signOut()}>Đăng xuất</button>
        {error && <div className="mt-3"><ErrorMessage message={error} /></div>}
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
      <header className="relative isolate flex min-h-32 items-center overflow-hidden rounded-2xl bg-plum px-5 py-6 text-white sm:px-8">
        <div className="absolute inset-y-0 right-0 -z-20 w-2/3">
          <Image src={current?.group === "F10" ? eventPhoto : atriumPhoto} alt="" fill unoptimized
            sizes="(min-width: 1024px) 50vw, 67vw" className="object-cover object-[70%_55%]" />
        </div>
        <div className="absolute inset-0 -z-10 bg-[linear-gradient(90deg,#3d122f_30%,#3d122fe6_49%,#3d122f29_100%)]" />
        <div>
          <p className="mb-1 text-xs font-semibold text-[#f5d9eb]">Quản lý ANEON</p>
          <h1 id="view-heading" className="font-display text-3xl leading-tight sm:text-4xl">{current?.label ?? "Nghiệp vụ"}</h1>
        </div>
      </header>
      {!current && <p className={panel}>Vai trò này chưa có quyền trên màn hình quản lý.</p>}
      {current?.id === "staff" && identity.mode === "session" && <StaffAccounts identity={identity} />}
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

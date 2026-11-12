"use client";

import Link from "next/link";
import { AccountSecurity } from "./AccountSecurity";
import { IdentityForm, staffRoles } from "./IdentityForm";
import { ErrorMessage, panel, secondary } from "./Ui";
import { useAneonIdentity } from "./useAneonIdentity";

export function AccountScreen() {
  const { identity, setIdentity, loading, error, signingOut, signOut } = useAneonIdentity();
  return <div className="grid gap-4">
    {error && <ErrorMessage message={error} />}
    {loading && <p role="status" className={panel}>Đang kiểm tra phiên đăng nhập…</p>}
    {!loading && !identity && <section className={panel}><IdentityForm customer onConnect={setIdentity} /></section>}
    {identity?.mode === "session" && <>
      <section className={`${panel} flex flex-wrap items-center justify-between gap-3`}>
        <div><h2 className="font-display text-2xl text-plum">{identity.fullName || identity.username}</h2>
          <p className="mt-1 text-sm text-ink-soft">{identity.username} · {identity.role === "CUSTOMER" ? "Khách mua sắm" : staffRoles[identity.role as keyof typeof staffRoles] ?? identity.role}</p></div>
        <div className="flex flex-wrap gap-2">
          {identity.role !== "CUSTOMER" && !identity.mustChangePassword && <Link className={secondary} href="/quan-ly">Mở trang quản lý</Link>}
          <button className={secondary} type="button" disabled={signingOut} onClick={() => void signOut()}>Đăng xuất</button>
        </div>
      </section>
      <AccountSecurity identity={identity} onChanged={setIdentity} />
    </>}
    {identity && identity.mode !== "session" && <section className={`${panel} grid gap-3`}>
      <p className="text-sm">Dùng tên đăng nhập và mật khẩu ANEON để mở phần bảo mật tài khoản.</p>
      <button type="button" className={`${secondary} justify-self-start`} onClick={() => void signOut()}>Đổi cách kết nối</button>
    </section>}
  </div>;
}

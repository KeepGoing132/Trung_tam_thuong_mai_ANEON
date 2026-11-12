"use client";

import { useState } from "react";
import { request, type AuthAccount, type Page, type SessionIdentity } from "@/lib/aneon-api";
import { staffRoles } from "./IdentityForm";
import { ErrorMessage, Field, input, panel, Pagination, secondary } from "./Ui";
import { Action, MutationForm, useResource } from "./WorkspaceUi";

type Staff = AuthAccount & { status: string };
type Credential = { account: AuthAccount; temporaryPassword: string };
const { SYSTEM_ADMIN: _admin, ...roles } = staffRoles;

export function StaffAccounts({ identity }: { identity: SessionIdentity }) {
  const [page, setPage] = useState(0);
  const [revision, setRevision] = useState(0);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [credential, setCredential] = useState<Credential>();
  const resource = useResource<Page<Staff>>(`auth/staff?page=${page}&size=10`, identity, revision);

  async function update(account: Staff, reset: boolean) {
    if (busy) return;
    setBusy(true);
    setError("");
    setCredential(undefined);
    try {
      if (reset) setCredential(await request<Credential>(`auth/staff/${account.id}/password-reset`, { method: "POST" }));
      else await request<Staff>(`auth/staff/${account.id}/status`, { method: "PUT", body: { locked: account.status === "ACTIVE" } });
      setRevision((value) => value + 1);
    } catch (reason) { setError((reason as Error).message); }
    finally { setBusy(false); }
  }

  return <div className={`${panel} grid gap-4`}>
    <Action title="Tạo tài khoản nhân viên">
      <MutationForm identity={identity} path="auth/staff" label="Tạo tài khoản" fields={[
        { name: "username", label: "Tên đăng nhập", required: true, max: 50, pattern: "[a-zA-Z0-9._\\-]{3,50}" },
        { name: "fullName", label: "Họ và tên", required: true, max: 200 },
        { name: "role", label: "Vai trò", options: roles, required: true },
      ]} onSaved={(value) => { setCredential(value as Credential); setRevision((current) => current + 1); }} />
    </Action>
    {credential && <div className="grid gap-2 rounded-xl bg-cream p-4">
      <p role="status" className="text-sm font-semibold">Gửi mật khẩu tạm cho {credential.account.username} qua kênh riêng. Nhân viên sẽ đổi mật khẩu ở lần đăng nhập đầu.</p>
      <Field label="Mật khẩu tạm"><input className={input} readOnly value={credential.temporaryPassword} onFocus={(event) => event.currentTarget.select()} /></Field>
      <button type="button" className="justify-self-start text-sm font-semibold text-magenta-deep" onClick={() => setCredential(undefined)}>Ẩn mật khẩu tạm</button>
    </div>}
    {error && <ErrorMessage message={error} />}
    {resource.error && <ErrorMessage message={resource.error} />}
    {resource.loading && <p role="status" className="text-sm">Đang tải tài khoản…</p>}
    {resource.data?.content.length === 0 && <p className="text-sm">Chưa có tài khoản nhân viên.</p>}
    {resource.data && <ul className="grid gap-3">{resource.data.content.map(account => <li key={account.id}
      className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-cream-line p-4">
      <div className="min-w-0 wrap-anywhere"><p className="font-semibold">{account.fullName || account.username}</p>
        <p className="mt-1 text-sm text-ink-soft">{account.username} · {roles[account.role as keyof typeof roles] ?? account.role}</p>
        <p className="mt-1 text-xs text-ink-soft">{account.status === "ACTIVE" ? account.mustChangePassword ? "Chờ đổi mật khẩu tạm" : "Hoạt động" : "Đang khóa"}</p></div>
      <div className="flex flex-wrap gap-2">
        <button type="button" disabled={busy} className={secondary} onClick={() => void update(account, false)}>{account.status === "ACTIVE" ? "Khóa" : "Mở khóa"}</button>
        <Action title="Đặt lại mật khẩu"><button type="button" disabled={busy} className={`${secondary} mt-3`} onClick={() => void update(account, true)}>Tạo mật khẩu tạm mới</button></Action>
      </div>
    </li>)}</ul>}
    {resource.data && <Pagination page={page} size={resource.data.size} total={resource.data.totalElements} onPage={setPage} busy={busy} />}
  </div>;
}

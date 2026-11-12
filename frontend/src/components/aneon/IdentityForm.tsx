"use client";

import { useState, type FormEvent } from "react";
import { localIdentityEnabled, request, sessionIdentity, UUID_PATTERN, type AuthAccount, type Identity } from "@/lib/aneon-api";
import { ErrorMessage, Field, input, primary } from "./Ui";

export const staffRoles = {
  MANAGEMENT: "Ban quản lý", OPERATIONS: "Vận hành", DIRECTOR: "Giám đốc",
  MARKETING: "Marketing", ACCOUNTING: "Kế toán", SYSTEM_ADMIN: "Quản trị hệ thống",
};
type Mode = "login" | "register" | "recover" | "jwt" | "local";

export function IdentityForm({ onConnect, customer = false }: { onConnect: (identity: Identity) => void; customer?: boolean }) {
  const [mode, setMode] = useState<Mode>("login");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  function open(next: Mode) { setMode(next); setError(""); setNotice(""); }

  async function connect(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (busy) return;
    const form = new FormData(event.currentTarget);
    if (mode === "local") {
      onConnect({ mode, actorId: String(form.get("actorId")).trim(), role: customer ? "CUSTOMER" : String(form.get("role")),
        zones: String(form.get("zones") ?? "").trim() });
      return;
    }
    if (mode === "jwt") {
      const token = String(form.get("token")).trim();
      if (token) onConnect({ mode, token });
      return;
    }
    setBusy(true);
    setError("");
    setNotice("");
    try {
      const body = Object.fromEntries(form);
      if (mode === "recover") {
        await request<boolean>("auth/recover", { method: "POST", body });
        setMode("login");
        setNotice("Đã đặt lại mật khẩu. Bạn có thể đăng nhập bằng mật khẩu mới.");
        return;
      }
      const account = await request<AuthAccount>(`auth/${mode}`, { method: "POST", body });
      onConnect(sessionIdentity(account));
    } catch (reason) { setError((reason as Error).message); }
    finally { setBusy(false); }
  }

  const button = { login: "Đăng nhập", register: "Đăng ký tài khoản khách", recover: "Đặt lại mật khẩu", jwt: "Tiếp tục", local: "Tiếp tục" }[mode];
  return <div className="grid gap-4">
    {notice && <p role="status" className="text-sm font-semibold text-green-800">{notice}</p>}
    {error && <ErrorMessage message={error} />}
    <form key={mode} onSubmit={connect} className="grid gap-4" aria-busy={busy}>
      <fieldset disabled={busy} className="grid gap-4">
        {(mode === "login" || mode === "register" || mode === "recover") && <>
          <Field label="Tên đăng nhập"><input name="username" className={input} autoComplete="username" required maxLength={mode === "register" ? 50 : 100}
            pattern={mode === "register" ? "[a-zA-Z0-9._\\-]{3,50}" : undefined} /></Field>
          {mode === "register" && <>
            <Field label="Họ và tên"><input name="fullName" className={input} autoComplete="name" required maxLength={200} /></Field>
            <Field label="Email (tùy chọn)"><input name="email" type="email" className={input} autoComplete="email" maxLength={200} /></Field>
          </>}
          {mode === "recover" && <Field label="Mã khôi phục đã lưu"><input name="code" type="password" className={input} required maxLength={100} autoComplete="off" /></Field>}
          <Field label={mode === "recover" ? "Mật khẩu mới" : "Mật khẩu"}><input name={mode === "recover" ? "newPassword" : "password"} className={input}
            type="password" required minLength={mode === "login" ? undefined : 12} maxLength={128}
            autoComplete={mode === "login" ? "current-password" : "new-password"} /></Field>
          {mode !== "login" && <p className="text-xs text-ink-soft">Dùng ít nhất 12 ký tự cho mật khẩu.</p>}
        </>}
        {mode === "jwt" && <Field label="Token truy cập"><input name="token" className={input} type="password" required autoComplete="off" /></Field>}
        {mode === "local" && <>
          <Field label="UUID tài khoản thử nghiệm"><input name="actorId" required pattern={UUID_PATTERN} className={input} autoComplete="off" /></Field>
          {!customer && <>
            <Field label="Vai trò thử nghiệm"><select name="role" className={input}>
              {Object.entries(staffRoles).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select></Field>
            <Field label="Phạm vi khu vực (tùy chọn)"><input name="zones" placeholder="Các UUID cách nhau bằng dấu phẩy" className={input} /></Field>
          </>}
        </>}
      </fieldset>
      <button type="submit" className={`${primary} justify-self-start`} disabled={busy}>{busy ? "Đang xử lý…" : button}</button>
    </form>
    <div className="flex flex-wrap gap-x-5 gap-y-2 text-sm font-semibold text-magenta-deep">
      {mode === "login" ? <>
        {customer && <button type="button" disabled={busy} onClick={() => open("register")}>Tạo tài khoản khách</button>}
        <button type="button" disabled={busy} onClick={() => open("recover")}>Quên mật khẩu</button>
        {localIdentityEnabled && <button type="button" disabled={busy} onClick={() => open("local")}>Tài khoản thử nghiệm</button>}
        {process.env.NEXT_PUBLIC_ANEON_EXTERNAL_JWT === "true" && <button type="button" disabled={busy} onClick={() => open("jwt")}>Dùng token truy cập</button>}
      </> : <button type="button" disabled={busy} onClick={() => open("login")}>Quay lại đăng nhập</button>}
    </div>
  </div>;
}

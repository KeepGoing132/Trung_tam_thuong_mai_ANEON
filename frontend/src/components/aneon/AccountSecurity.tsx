"use client";

import { useState, type FormEvent } from "react";
import { request, sessionIdentity, type AuthAccount, type SessionIdentity } from "@/lib/aneon-api";
import { ErrorMessage, Field, input, panel, primary } from "./Ui";

export function AccountSecurity({ identity, onChanged }: { identity: SessionIdentity; onChanged: (identity: SessionIdentity) => void }) {
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [recovery, setRecovery] = useState("");

  async function submit(event: FormEvent<HTMLFormElement>, mode: "password" | "recovery-code") {
    event.preventDefault();
    if (busy) return;
    const form = event.currentTarget;
    const values = new FormData(form);
    const password = String(values.get("password"));
    const newPassword = String(values.get("newPassword") ?? "");
    setError("");
    setMessage("");
    if (mode === "password" && newPassword !== values.get("confirm")) { setError("Hai mật khẩu mới chưa khớp."); return; }
    setBusy(true);
    try {
      if (mode === "password") {
        const account = await request<AuthAccount>("auth/password", { method: "PUT", body: { currentPassword: password, newPassword } });
        setRecovery("");
        setMessage("Đã đổi mật khẩu và đăng xuất các phiên cũ.");
        onChanged(sessionIdentity(account));
      } else {
        const { code } = await request<{ code: string }>("auth/recovery-code", { method: "POST", body: { password } });
        setRecovery(code);
      }
      form.reset();
    } catch (reason) { setError((reason as Error).message); }
    finally { setBusy(false); }
  }

  return <div className="grid gap-4">
    {error && <ErrorMessage message={error} />}
    {message && <p role="status" className="text-sm font-semibold text-green-800">{message}</p>}
    <section className={panel} aria-labelledby="password-heading">
      <h2 id="password-heading" className="font-display text-2xl text-plum">Đổi mật khẩu</h2>
      <p className="mb-4 mt-2 text-sm text-ink-soft">{identity.mustChangePassword ? "Đổi mật khẩu tạm trước khi vào các mục nghiệp vụ." : "Mật khẩu mới cần ít nhất 12 ký tự."}</p>
      <form onSubmit={(event) => void submit(event, "password")} className="grid gap-3">
        <fieldset className="grid gap-3" disabled={busy}>
          <Field label={identity.mustChangePassword ? "Mật khẩu tạm" : "Mật khẩu hiện tại"}>
            <input className={input} name="password" type="password" autoComplete="current-password" maxLength={128} required />
          </Field>
          <Field label="Mật khẩu mới"><input className={input} name="newPassword" type="password" autoComplete="new-password" minLength={12} maxLength={128} required /></Field>
          <Field label="Nhập lại mật khẩu mới"><input className={input} name="confirm" type="password" autoComplete="new-password" minLength={12} maxLength={128} required /></Field>
        </fieldset>
        <button className={`${primary} justify-self-start`} type="submit" disabled={busy}>Đổi mật khẩu</button>
      </form>
    </section>
    {!identity.mustChangePassword && <section className={panel} aria-labelledby="recovery-heading">
      <h2 id="recovery-heading" className="font-display text-2xl text-plum">Mã khôi phục</h2>
      <p className="mb-4 mt-2 text-sm text-ink-soft">Lưu mã ở nơi riêng tư để đặt lại mật khẩu khi cần. Mỗi mã dùng được một lần. Tạo mã mới hoặc đổi mật khẩu sẽ hủy mã cũ.</p>
      <form onSubmit={(event) => void submit(event, "recovery-code")} className="grid gap-3">
        <Field label="Xác nhận mật khẩu"><input className={input} name="password" type="password" autoComplete="current-password" maxLength={128} required disabled={busy} /></Field>
        <button className={`${primary} justify-self-start`} type="submit" disabled={busy}>Tạo mã khôi phục</button>
      </form>
      {recovery && <div className="mt-4 grid gap-2 rounded-xl bg-cream p-4">
        <p role="status" className="text-sm font-semibold">Mã chỉ hiển thị trong lần tạo này. Hãy lưu lại trước khi rời trang.</p>
        <Field label="Mã khôi phục"><input className={input} value={recovery} readOnly onFocus={(event) => event.currentTarget.select()} /></Field>
      </div>}
    </section>}
  </div>;
}

"use client";

import { useState, type FormEvent } from "react";
import { formatDate, request, UUID_PATTERN } from "@/lib/aneon-api";
import Link from "next/link";
import { IdentityForm } from "./IdentityForm";
import { ErrorMessage, Field, input, panel, primary, secondary } from "./Ui";
import { useAneonIdentity } from "./useAneonIdentity";

type CheckInResult = { checkInAt: string; duplicate: boolean };

export function CheckIn({ eventId, token }: { eventId: string; token: string }) {
  const { identity, setIdentity, loading, error: sessionError, signingOut, signOut } = useAneonIdentity();
  const [result, setResult] = useState<CheckInResult>();
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const sessionReady = identity?.mode !== "session" || (identity.role === "CUSTOMER" && !identity.mustChangePassword);

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!identity || !sessionReady || busy) return;
    const form = new FormData(event.currentTarget);
    setBusy(true);
    setError("");
    try {
      setResult(await request<CheckInResult>(`f10/events/${encodeURIComponent(String(form.get("eventId")).trim())}/check-ins`, {
        identity, method: "POST", body: { qrToken: String(form.get("qrToken")).trim() },
      }));
    } catch (err) {
      setError((err as Error).message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className={`${panel} grid gap-5`}>
      {sessionError && <ErrorMessage message={sessionError} />}
      {loading && <p role="status" className="text-sm">Đang kiểm tra phiên đăng nhập…</p>}
      {!loading && !identity && <IdentityForm customer onConnect={setIdentity} />}
      {identity?.mode === "session" && !sessionReady && <div className="grid gap-3">
        <p className="text-sm">{identity.mustChangePassword ? "Hãy đổi mật khẩu tạm trước khi tiếp tục." : "Dùng tài khoản khách mua sắm để tự check-in."}</p>
        {identity.mustChangePassword && <Link href="/tai-khoan" className={`${secondary} justify-self-start`}>Đổi mật khẩu</Link>}
        <button type="button" className={`${secondary} justify-self-start`} disabled={signingOut} onClick={() => void signOut()}>Đổi tài khoản</button>
      </div>}
      {identity && sessionReady && !result && <form className="grid gap-4" onSubmit={submit}>
        <Field label="UUID sự kiện"><input name="eventId" defaultValue={eventId} required pattern={UUID_PATTERN} className={input} /></Field>
        <Field label="Mã từ liên kết check-in"><input name="qrToken" type="password" defaultValue={token} required maxLength={200} autoComplete="off" className={input} /></Field>
        {error && <ErrorMessage message={error} />}
        <div className="flex flex-wrap gap-3">
          <button type="submit" className={primary} disabled={busy}>{busy ? "Đang xác nhận…" : "Xác nhận check-in"}</button>
          <button type="button" className={secondary} disabled={busy || signingOut} onClick={() => { setError(""); void signOut(); }}>Đổi tài khoản</button>
        </div>
      </form>}
      {result && <div role="status" className="rounded-xl border border-green-200 bg-green-50 p-4 text-green-950">
        <p className="font-semibold">{result.duplicate ? "Bạn đã check-in sự kiện này" : "Check-in thành công"}</p>
        <p className="mt-1 text-sm">{formatDate(result.checkInAt)}</p>
      </div>}
    </div>
  );
}

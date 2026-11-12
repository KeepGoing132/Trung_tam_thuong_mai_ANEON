"use client";

import { useState, type FormEvent } from "react";
import { localIdentityEnabled, UUID_PATTERN, type Identity } from "@/lib/aneon-api";
import { Field, input, primary } from "./Ui";

export const staffRoles = {
  MANAGEMENT: "Ban quản lý", OPERATIONS: "Vận hành", DIRECTOR: "Giám đốc",
  MARKETING: "Marketing", ACCOUNTING: "Kế toán", SYSTEM_ADMIN: "Quản trị hệ thống",
};

export function IdentityForm({ onConnect, customer = false }: { onConnect: (identity: Identity) => void; customer?: boolean }) {
  const [mode, setMode] = useState<Identity["mode"]>(localIdentityEnabled ? "local" : "jwt");

  function connect(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    if (mode === "jwt") {
      const token = String(form.get("token")).trim();
      if (token) onConnect({ mode, token });
      return;
    }
    onConnect({
      mode, actorId: String(form.get("actorId")).trim(),
      role: customer ? "CUSTOMER" : String(form.get("role")), zones: String(form.get("zones") ?? "").trim(),
    });
  }

  return (
    <form onSubmit={connect} className="grid gap-4">
      {localIdentityEnabled && <Field label="Cách kết nối"><select className={input} value={mode} onChange={(event) => setMode(event.target.value as Identity["mode"])}>
        <option value="local">Tài khoản thử nghiệm (local)</option><option value="jwt">Token truy cập</option>
      </select></Field>}
      {mode === "jwt" ? (
        <Field label="Token truy cập"><input name="token" className={input} type="password" required autoComplete="off" spellCheck={false} placeholder="Token do hệ thống định danh cấp" /></Field>
      ) : (
        <>
          <Field label="UUID tài khoản thử nghiệm"><input name="actorId" required pattern={UUID_PATTERN} className={input} autoComplete="off" /></Field>
          {!customer && <>
            <Field label="Vai trò thử nghiệm"><select name="role" className={input}>
              {Object.entries(staffRoles).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select></Field>
            <Field label="Phạm vi khu vực (tùy chọn)"><input name="zones" placeholder="Các UUID cách nhau bằng dấu phẩy" className={input} /></Field>
          </>}
        </>
      )}
      <button type="submit" className={`${primary} justify-self-start`}>Tiếp tục</button>
    </form>
  );
}

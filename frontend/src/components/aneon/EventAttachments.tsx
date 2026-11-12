"use client";

import { useState, type FormEvent } from "react";
import { downloadAttachment, request, type Identity } from "@/lib/aneon-api";
import { ErrorMessage, Field, input, primary, secondary } from "./Ui";
import { Action, useResource } from "./WorkspaceUi";

type Attachment = { id: string; name: string; sizeBytes: number };

export function EventAttachments({ path, identity, canEdit }: { path: string; identity: Identity; canEdit: boolean }) {
  const [revision, setRevision] = useState(0);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const files = useResource<Attachment[]>(path, identity, revision);

  async function run(action: () => Promise<unknown>) {
    setBusy(true);
    setError("");
    try { await action(); }
    catch (reason) { setError((reason as Error).message); }
    finally { setBusy(false); }
  }

  function upload(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const body = new FormData(form);
    const file = body.get("file");
    if (!(file instanceof File) || file.size === 0 || file.size > 25 * 1024 * 1024) {
      setError("Chọn tệp có dung lượng từ 1 byte đến 25 MB.");
      return;
    }
    void run(async () => {
      await request<Attachment>(path, { identity, method: "POST", body });
      form.reset();
      setRevision((value) => value + 1);
    });
  }

  return <Action title="Tệp đính kèm">
    <div className="mt-4 grid gap-3" aria-busy={busy || files.loading}>
      {files.error && <ErrorMessage message={files.error} />}
      {error && <ErrorMessage message={error} />}
      {files.data?.length === 0 && <p className="text-sm">Chưa có tệp đính kèm.</p>}
      {files.data && files.data.length > 0 && <ul className="grid gap-2">{files.data.map((file) => <li key={file.id}
        className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-cream-line p-3 text-sm">
        <span className="wrap-anywhere">{file.name} · {Math.ceil(file.sizeBytes / 1024)} KB</span>
        <div className="flex flex-wrap gap-2">
          <button type="button" className={secondary} disabled={busy} onClick={() => void run(() => downloadAttachment(`${path}/${file.id}`, identity, file.name))}>Tải xuống</button>
          {canEdit && <button type="button" className={secondary} disabled={busy} onClick={() => void run(async () => {
            await request<string>(`${path}/${file.id}`, { identity, method: "DELETE" });
            setRevision((value) => value + 1);
          })}>Gỡ tệp</button>}
        </div>
      </li>)}</ul>}
      {canEdit && <form className="flex flex-wrap items-end gap-3" onSubmit={upload}>
        <Field label="Chọn tệp PDF, DOCX, PNG hoặc JPEG"><input className={input} type="file" name="file" accept=".pdf,.docx,.png,.jpg,.jpeg" required /></Field>
        <button className={primary} type="submit" disabled={busy}>Tải lên</button>
      </form>}
    </div>
  </Action>;
}

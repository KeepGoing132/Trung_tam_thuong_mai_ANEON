"use client";

import { useEffect, useState } from "react";
import { ApiRequestError, request, sessionIdentity, type AuthAccount, type Identity } from "@/lib/aneon-api";

export function useAneonIdentity() {
  const [identity, setIdentity] = useState<Identity>();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [signingOut, setSigningOut] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    request<AuthAccount>("auth/me", { signal: controller.signal })
      .then(account => { if (!controller.signal.aborted) setIdentity(sessionIdentity(account)); })
      .catch(reason => {
        if (!controller.signal.aborted && !(reason instanceof ApiRequestError && reason.status === 401)) setError(reason.message);
      })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, []);

  async function signOut() {
    if (signingOut) return;
    setSigningOut(true);
    setError("");
    try {
      if (identity?.mode === "session") await request<boolean>("auth/logout", { method: "POST" });
      setIdentity(undefined);
    } catch (reason) { setError((reason as Error).message); }
    finally { setSigningOut(false); }
  }

  return { identity, setIdentity, loading, error, signingOut, signOut };
}

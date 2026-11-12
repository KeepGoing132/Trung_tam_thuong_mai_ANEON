export type Identity =
  | { mode: "jwt"; token: string }
  | { mode: "local"; actorId: string; role: string; zones: string };

export type Page<T> = { content: T[]; page: number; size: number; totalElements: number };
type Envelope<T> = {
  data?: T;
  message?: string;
  errors?: { field: string; message: string }[];
};

export const UUID_PATTERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
export const localIdentityEnabled = process.env.NEXT_PUBLIC_ANEON_LOCAL_IDENTITY === "true";

export async function request<T>(path: string, options: {
  identity?: Identity;
  method?: string;
  body?: unknown;
  signal?: AbortSignal;
} = {}): Promise<T> {
  const headers = new Headers({ Accept: "application/json" });
  const { identity, method, body, signal } = options;
  if (identity?.mode === "jwt") headers.set("Authorization", `Bearer ${identity.token}`);
  if (identity?.mode === "local") {
    headers.set("X-Actor-Id", identity.actorId);
    headers.set("X-Actor-Roles", identity.role);
    if (identity.zones) headers.set("X-Actor-Zones", identity.zones);
  }
  if (body !== undefined) headers.set("Content-Type", "application/json");
  const response = await fetch(`/aneon-api/${path}`, {
    method, headers, signal, cache: "no-store",
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const envelope: Envelope<T> | null = await response.json().catch(() => null);
  if (!response.ok || envelope?.data === undefined) {
    const fields = envelope?.errors?.map(({ field, message }) => `${field} — ${message}`).join(". ");
    const message = {
      401: "Phiên truy cập không hợp lệ hoặc đã hết hạn.",
      403: "Tài khoản không có quyền thực hiện thao tác này.",
    }[response.status];
    throw new Error(message || fields || envelope?.message || "Không thể kết nối máy chủ ANEON. Vui lòng thử lại.");
  }
  return envelope.data;
}

export function query(params: Record<string, string | number | undefined>) {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") search.set(key, String(value));
  }
  return search.toString();
}

const dateFormat = new Intl.DateTimeFormat("vi-VN", {
  dateStyle: "medium", timeStyle: "short", timeZone: "Asia/Ho_Chi_Minh",
});
export function formatDate(value?: string) {
  return value ? dateFormat.format(new Date(value)) : "—";
}

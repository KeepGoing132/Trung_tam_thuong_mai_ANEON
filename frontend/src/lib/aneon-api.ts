export type Identity =
  | { mode: "jwt"; token: string }
  | { mode: "local"; actorId: string; role: string; zones: string }
  | { mode: "session"; actorId: string; role: string; username: string; fullName?: string; mustChangePassword: boolean };

export type AuthAccount = { id: string; username: string; fullName?: string; role: string; mustChangePassword: boolean };
export type SessionIdentity = Extract<Identity, { mode: "session" }>;

export function sessionIdentity(account: AuthAccount): SessionIdentity {
  return { mode: "session", actorId: account.id, username: account.username, fullName: account.fullName,
    role: account.role, mustChangePassword: account.mustChangePassword };
}

export class ApiRequestError extends Error {
  constructor(message: string, readonly status: number) { super(message); }
}

export type Page<T> = { content: T[]; page: number; size: number; totalElements: number };
type Envelope<T> = {
  data?: T;
  message?: string;
  errors?: { field: string; message: string }[];
};

export const UUID_PATTERN = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
export const localIdentityEnabled = process.env.NEXT_PUBLIC_ANEON_LOCAL_IDENTITY === "true";

function identityHeaders(identity?: Identity) {
  const headers = new Headers();
  if (identity?.mode === "jwt") headers.set("Authorization", `Bearer ${identity.token}`);
  if (identity?.mode === "local") {
    headers.set("X-Actor-Id", identity.actorId);
    headers.set("X-Actor-Roles", identity.role);
    if (identity.zones) headers.set("X-Actor-Zones", identity.zones);
  }
  return headers;
}

export async function request<T>(path: string, options: {
  identity?: Identity;
  method?: string;
  body?: unknown;
  signal?: AbortSignal;
} = {}): Promise<T> {
  const { identity, method, body, signal } = options;
  const headers = identityHeaders(identity);
  headers.set("Accept", "application/json");
  if (method && !["GET", "HEAD", "OPTIONS"].includes(method.toUpperCase())) headers.set("X-ANEON-Request", "aneon-web");
  const multipart = body instanceof FormData;
  if (body !== undefined && !multipart) headers.set("Content-Type", "application/json");
  const response = await fetch(`/aneon-api/${path}`, {
    method, headers, signal, cache: "no-store",
    body: multipart ? body : body === undefined ? undefined : JSON.stringify(body),
  });
  const envelope: Envelope<T> | null = await response.json().catch(() => null);
  if (!response.ok || envelope?.data === undefined) {
    const fields = envelope?.errors?.map(({ field, message }) => `${field} — ${message}`).join(". ");
    const message = {
      401: path === "auth/login" ? "Tên đăng nhập hoặc mật khẩu không đúng." : "Phiên truy cập không hợp lệ hoặc đã hết hạn.",
      403: "Tài khoản không có quyền thực hiện thao tác này.",
      429: "Bạn đã thử quá nhiều lần. Vui lòng thử lại sau.",
    }[response.status];
    throw new ApiRequestError(message || fields || envelope?.message || "Không thể kết nối máy chủ ANEON. Vui lòng thử lại.", response.status);
  }
  return envelope.data;
}

export async function downloadAttachment(path: string, identity: Identity, name: string) {
  const response = await fetch(`/aneon-api/${path}`, { headers: identityHeaders(identity), cache: "no-store" });
  if (!response.ok) throw new Error("Không tải được tệp đính kèm.");
  const url = URL.createObjectURL(await response.blob());
  const link = document.createElement("a");
  link.href = url;
  link.download = name;
  link.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
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

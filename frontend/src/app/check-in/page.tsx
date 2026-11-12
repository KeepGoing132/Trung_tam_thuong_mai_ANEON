import { CheckIn } from "@/components/aneon/CheckIn";
import { TopBar } from "@/components/aneon/Ui";

export const metadata = {
  title: "Check-in sự kiện | AEON MALL", referrer: "no-referrer",
  robots: { index: false, follow: false },
};

export default async function CheckInPage({ searchParams }: { searchParams: Promise<Record<string, string | string[] | undefined>> }) {
  const { event, token } = await searchParams;
  const eventId = typeof event === "string" ? event : "";
  const qrToken = typeof token === "string" ? token : "";
  return (
    <>
      <TopBar />
      <main className="mx-auto max-w-2xl px-5 py-12 md:py-20">
        <h1 className="font-display text-5xl text-plum">Check-in sự kiện</h1>
        <p className="mb-7 mt-4 text-ink-soft">Mở liên kết từ mã QR tại sự kiện và dùng tài khoản khách hàng đã liên kết để xác nhận tham gia.</p>
        <CheckIn key={`${eventId}-${qrToken}`} eventId={eventId} token={qrToken} />
      </main>
    </>
  );
}

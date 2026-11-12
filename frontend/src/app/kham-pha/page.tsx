import Link from "next/link";
import { Discovery } from "@/components/aneon/Discovery";
import { TopBar } from "@/components/aneon/Ui";
import { IMAGES } from "@/lib/site";

export const metadata = { title: "Sự kiện & ưu đãi | AEON MALL" };

export default async function DiscoveryPage({ searchParams }: { searchParams: Promise<{ "danh-muc"?: string }> }) {
  const { "danh-muc": category } = await searchParams;
  const initialCategory = category === "EVENT" || category === "PROMOTION" ? category : "";
  return (
    <>
      <TopBar />
      <main>
        <div className="bg-plum bg-cover bg-center text-cream" style={{ backgroundImage: `linear-gradient(90deg, #3d122f 10%, #3d122feb 45%, #3d122f7a), url(${IMAGES.eventA})` }}>
          <div className="mx-auto max-w-7xl px-5 py-12 md:px-10 md:py-20">
            <h1 className="font-display text-5xl leading-tight md:text-7xl">Sự kiện & <em className="text-[#ffaad5]">ưu đãi</em></h1>
            <p className="mt-5 max-w-xl text-cream/85">Tra cứu lịch sự kiện và chương trình khuyến mãi tại AEON MALL.</p>
            <Link href="/check-in" className="mt-6 inline-block font-semibold text-[#ffaad5] underline underline-offset-4">Check-in bằng mã sự kiện</Link>
          </div>
        </div>
        <section aria-label="Tra cứu sự kiện và ưu đãi" className="mx-auto max-w-7xl px-5 py-8 md:px-10 md:py-12">
          <Discovery key={initialCategory} initialCategory={initialCategory} />
        </section>
      </main>
    </>
  );
}

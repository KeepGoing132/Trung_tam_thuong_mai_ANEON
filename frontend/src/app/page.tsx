import Header from "@/components/Header";
import Hero from "@/components/Hero";
import Marquee from "@/components/Marquee";
import About from "@/components/About";
import StoreDirectory from "@/components/StoreDirectory";
import ShoppingDining from "@/components/ShoppingDining";
import { DiscoveryPreview } from "@/components/aneon/Discovery";
import Facilities from "@/components/Facilities";
import Footer from "@/components/Footer";

export default function Home() {
  return (
    <>
      <Header />
      <main>
        <Hero />
        <Marquee />
        <About />
        <StoreDirectory />
        <ShoppingDining />
        <DiscoveryPreview category="PROMOTION" id="uu-dai" />
        <DiscoveryPreview category="EVENT" id="su-kien" />
        <Facilities />
      </main>
      <Footer />
    </>
  );
}

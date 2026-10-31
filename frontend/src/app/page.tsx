import Header from "@/components/Header";
import Hero from "@/components/Hero";
import Marquee from "@/components/Marquee";
import About from "@/components/About";
import StoreDirectory from "@/components/StoreDirectory";
import ShoppingDining from "@/components/ShoppingDining";
import Promotions from "@/components/Promotions";
import EventsSection from "@/components/EventsSection";
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
        <Promotions />
        <EventsSection />
        <Facilities />
      </main>
      <Footer />
    </>
  );
}

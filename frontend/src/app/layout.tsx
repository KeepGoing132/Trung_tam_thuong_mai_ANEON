import type { Metadata } from "next";
import { Fraunces, Inter } from "next/font/google";
import "./globals.css";

const fraunces = Fraunces({
  subsets: ["latin", "vietnamese"],
  variable: "--font-fraunces",
  axes: ["opsz"],
});

const inter = Inter({
  subsets: ["latin", "vietnamese"],
  variable: "--font-inter",
});

export const metadata: Metadata = {
  title: "AEON MALL — Điểm hẹn niềm vui của cả thành phố",
  description:
    "Trung tâm thương mại AEON MALL: mua sắm, ẩm thực, giải trí và sự kiện — trải nghiệm cao cấp, cinematic, dành cho cả gia đình.",
  openGraph: {
    title: "AEON MALL",
    description: "Mua sắm · Ẩm thực · Giải trí · Sự kiện",
    type: "website",
  },
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="vi" className={`${fraunces.variable} ${inter.variable}`}>
      <body className="bg-cream text-ink antialiased">{children}</body>
    </html>
  );
}

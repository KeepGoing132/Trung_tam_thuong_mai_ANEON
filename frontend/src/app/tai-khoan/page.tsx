import { AccountScreen } from "@/components/aneon/AccountScreen";
import { TopBar } from "@/components/aneon/Ui";

export const metadata = { title: "Tài khoản ANEON", robots: { index: false, follow: false } };

export default function AccountPage() {
  return <>
    <TopBar />
    <main className="mx-auto max-w-2xl px-4 py-8 sm:px-6 md:py-12">
      <h1 className="mb-6 font-display text-4xl text-plum">Tài khoản ANEON</h1>
      <AccountScreen />
    </main>
  </>;
}

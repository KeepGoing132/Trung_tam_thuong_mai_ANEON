import { TopBar } from "@/components/aneon/Ui";
import { Workspace } from "@/components/aneon/Workspace";

export const metadata = { title: "Quản lý ANEON | AEON MALL", robots: { index: false, follow: false } };

export default function WorkspacePage() {
  return <>
    <TopBar />
    <main className="mx-auto max-w-7xl px-5 py-9 md:px-10 md:py-12">
      <h1 className="mb-7 font-display text-4xl text-plum md:text-5xl">Quản lý ANEON</h1>
      <Workspace />
    </main>
  </>;
}

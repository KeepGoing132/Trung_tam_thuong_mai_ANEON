"use client";

import { useState } from "react";
import { type Identity } from "@/lib/aneon-api";
import { ErrorMessage } from "./Ui";
import { MutationForm, useResource } from "./WorkspaceUi";

type Point = { id: string; x: number; y: number; label?: string };
type FloorMap = { name: string; minX: number; minY: number; maxX: number; maxY: number; points: Point[] };

export function PremiseMap({ path, identity, currentId, onSaved }: {
  path: string; identity: Identity; currentId: unknown; onSaved: () => void;
}) {
  const map = useResource<FloorMap>(`${path}/map`, identity);
  const [selected, setSelected] = useState<string>();
  if (map.loading) return <p role="status" className="mt-3 text-sm">Đang tải sơ đồ…</p>;
  if (map.error) return <div className="mt-3"><ErrorMessage message={map.error} /></div>;
  if (!map.data) return null;
  const { name, minX, minY, maxX, maxY, points } = map.data;
  const selectedPoint = points.find((point) => point.id === selected);
  return <div className="mt-4 grid gap-3">
    <p className="font-semibold text-plum">{name}</p>
    {points.length === 0 ? <p className="text-sm">Chưa có vị trí trong khu vực của mặt bằng.</p> : <>
      <div className="px-5 py-5" aria-label="Sơ đồ vị trí mặt bằng">
        <div className="relative min-h-60 rounded-lg border border-cream-line bg-cream" style={{
          backgroundImage: "linear-gradient(#e7dcc9 1px, transparent 1px), linear-gradient(90deg, #e7dcc9 1px, transparent 1px)",
          backgroundSize: "24px 24px",
        }}>
          {points.map((point, index) => <button type="button" key={point.id} onClick={() => setSelected(point.id)}
            title={point.label || `Vị trí ${index + 1}`} aria-pressed={point.id === selected}
            aria-label={`${point.label || `Vị trí ${index + 1}`}${point.id === currentId ? " (hiện tại)" : ""}`}
            style={{ left: `${100 * (point.x - minX) / (maxX - minX)}%`, top: `${100 * (point.y - minY) / (maxY - minY)}%` }}
            className={`absolute grid size-9 -translate-x-1/2 -translate-y-1/2 place-items-center rounded-full border-2 text-sm font-semibold ${point.id === selected ? "z-10 border-magenta-deep bg-magenta-deep text-white" : "border-plum bg-white text-plum"}`}>
            {index + 1}
          </button>)}
        </div>
      </div>
      <p className="text-sm">{selectedPoint ? `Đã chọn ${selectedPoint.label || "vị trí trên sơ đồ"}` : "Chọn một vị trí trên sơ đồ."}</p>
      {selectedPoint && <MutationForm key={selectedPoint.id} path={`${path}/location`} identity={identity} method="PUT"
        label="Lưu vị trí" fields={[{ name: "reason", label: "Lý do", type: "textarea", max: 500 }]}
        build={(body) => ({ ...body, locationId: selectedPoint.id })} onSaved={onSaved} />}
    </>}
  </div>;
}

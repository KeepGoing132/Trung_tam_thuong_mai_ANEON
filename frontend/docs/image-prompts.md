# Image prompts — thay ảnh Unsplash bằng ảnh AI tự tạo

Bộ ảnh hiện tại dùng ảnh Unsplash (giấy phép Unsplash cho phép dùng thương mại,
không cần ghi công). Nếu muốn tạo bộ ảnh **đồng bộ 100%** (cùng ánh sáng, tông
màu, chất ảnh) bằng công cụ tạo ảnh (Midjourney / DALL·E / Flux), dùng các prompt
sau. Đặt file vào `frontend/public/img/` và đổi đường dẫn trong
`src/lib/site.ts` (hàm `u()` → chuỗi `/img/ten-file.jpg`).

> Style suffix chung (thêm vào cuối mọi prompt):
> `cinematic editorial photography, warm golden highlights, soft atmospheric
> haze, premium composition, shallow depth of field, 35mm, natural color
> grading with creamy warm tones, high-end retail magazine look --ar 16:9`

## Hero — kiến trúc TTTM hiện đại
```
Grand interior of a modern shopping mall atrium, sweeping glass arched
ceiling with sunlight streaming through, elegant boutiques along both sides,
soft motion blur of walking visitors, warm afternoon light with gentle lens
flare, atmospheric depth
```

## About — tổng thể trung tâm
```
Wide view of a luxurious multi-level shopping mall atrium at dusk, glowing
LED ceiling, polished stone floors reflecting shop lights, minimal premium
architecture, people silhouettes in gentle motion blur
```

## Shopping — thời trang
```
Elegant fashion boutique interior, neatly curated racks of neutral-toned
garments, warm spot lighting, minimal Scandinavian styling, a customer
browsing a jacket, editorial retail photography
```

## Dining — ẩm thực
```
Upscale restaurant interior at evening, dark wood tables with brass accents,
warm pendant lights, plated gourmet dish in soft focus foreground, intimate
golden ambience
```

## Entertainment — giải trí gia đình
```
Vibrant neon-lit arcade and game center inside a shopping mall, glowing
pinball and racing machines, purple and magenta light reflections, joyful
family silhouettes, cinematic night mood
```

## Events — không gian sự kiện
```
Festival celebration at an outdoor mall plaza at night, sparkling confetti
falling over a joyful crowd, string lights and stage glow, magenta and amber
color palette, energetic yet premium atmosphere
```

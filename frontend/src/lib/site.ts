/**
 * Dữ liệu nội dung của website AEON MALL.
 * Ảnh dùng từ Unsplash (giấy phép Unsplash) — curation đồng bộ phong cách
 * cinematic editorial; prompt thay thế bằng ảnh AI riêng nằm ở
 * frontend/docs/image-prompts.md.
 */
export const u = (id: string, w = 1600) =>
  `https://images.unsplash.com/photo-${id}?auto=format&fit=crop&w=${w}&q=80`;

export const IMAGES = {
  hero: u("1481437156560-3205f6a55735", 2200),
  aboutTall: u("1519567241046-7f570eee3ce6", 1400),
  aboutSmall: u("1573855619003-97b4799dcd8b", 900),
  shoppingMain: u("1441986300917-64674bd600d8", 1600),
  shoppingA: u("1555529669-e69e7aa0ba9a", 900),
  shoppingB: u("1441984904996-e0b6ba687e04", 900),
  diningMain: u("1517248135467-4c7edcad34c4", 1600),
  diningA: u("1555396273-367ea4eb4db5", 900),
  diningB: u("1414235077428-338989a2e8c0", 900),
  funA: u("1511882150382-421056c89033", 1000),
  funB: u("1513106580091-1d82408b8cd6", 1000),
  eventA: u("1533174072545-7a4b6ad7a6c3", 1400),
  eventB: u("1492684223066-81342ee5ff30", 1000),
  promo: u("1513885535751-8b9238bd345a", 1200),
  supermarket: u("1542838132-92c53300491e", 1200),
  giftToast: u("1519671482749-fd09be7ccebf", 900),
};

export const NAV = [
  { label: "Giới thiệu", href: "#gioi-thieu" },
  { label: "Gian hàng", href: "#gian-hang" },
  { label: "Mua sắm & Ẩm thực", href: "#mua-sam-am-thuc" },
  { label: "Ưu đãi", href: "#uu-dai" },
  { label: "Sự kiện", href: "#su-kien" },
  { label: "Tiện ích", href: "#tien-ich" },
];

export const HERO_STATS = [
  { value: "250+", label: "Gian hàng" },
  { value: "60+", label: "Ẩm thực" },
  { value: "12", label: "Khu giải trí" },
  { value: "2.500", label: "Chỗ đỗ xe" },
];

export const MARQUEE_WORDS = [
  "Mua sắm",
  "Ẩm thực",
  "Giải trí",
  "Sự kiện",
  "Ưа Đãi",
  "Khám phá",
];

export type Store = {
  id: string;
  name: string;
  category: string;
  floor: string;
  note: string;
};

export const STORE_CATEGORIES = [
  "Tất cả",
  "Thời trang",
  "Ẩm thực",
  "Làm đẹp",
  "Giải trí",
  "Siêu thị",
  "Công nghệ",
  "Trẻ em",
  "Thể thao",
] as const;

export const STORE_FLOORS = ["Tất cả", "B1", "T1", "T2", "T3"] as const;

export const STORES: Store[] = [
  { id: "st-01", name: "AEON Supermarket", category: "Siêu thị", floor: "B1", note: "Siêu thị nhật bản phong cách nhật bản, thực phẩm tươi mỗi ngày." },
  { id: "st-02", name: "Atelier Rivière", category: "Thời trang", floor: "T1", note: "Boutique cao cấp theo mùa, chọn lọc vải tự nhiên." },
  { id: "st-03", name: "Maison de Sable", category: "Thời trang", floor: "T1", note: "Phong cách tối giản Paris, chất liệu len và lụa." },
  { id: "st-04", name: "Kōan Beauty House", category: "Làm đẹp", floor: "T2", note: "Skincare và spa nghỉ dưỡng theo phong cách Kyoto." },
  { id: "st-05", name: "Signature Noodle Bar", category: "Ẩm thực", floor: "T2", note: "Mì ramen đậm vị, nước dùng hầm xương 12 tiếng." },
  { id: "st-06", name: "The Curiosity Lab", category: "Trẻ em", floor: "T3", note: "Vườn chơi tương tác và workshop sáng tạo cho trẻ." },
  { id: "st-07", name: "Neon Play Arena", category: "Giải trí", floor: "T3", note: "Khu trò chơi điện tử và VR cho cả gia đình." },
  { id: "st-08", name: "Sable & Stone", category: "Thời trang", floor: "T2", note: "Trang sức thủ công và đá quý tuyển chọn." },
  { id: "st-09", name: "Ciné Cité Plaza", category: "Giải trí", floor: "T3", note: "Rạp chiếu phim màn hình rộng, âm thanh Dolby." },
  { id: "st-10", name: "Orchid & Fern", category: "Làm đẹp", floor: "T1", note: "Hoa tươi, nến thơm và quà lưu niệm sống xanh." },
  { id: "st-11", name: "Pixel District", category: "Công nghệ", floor: "T2", note: "Điện tử, máy ảnh và phụ kiện chính hãng." },
  { id: "st-12", name: "Bếp Nhà Mai", category: "Ẩm thực", floor: "T2", note: "Cơm nhà Nam Bộ, nấu bằng gạo mới mỗi ngày." },
  { id: "st-13", name: "Aurora Athletica", category: "Thể thao", floor: "T1", note: "Trang phục chạy bộ và yoga, tư vấn size tại chỗ." },
  { id: "st-14", name: "Little Lanterns", category: "Trẻ em", floor: "T1", note: "Quần áo và đồ chơi gỗ an toàn cho bé 0–8 tuổi." },
  { id: "st-15", name: "Golden Crust Bakery", category: "Ẩm thực", floor: "B1", note: "Bếp bánh mì Âu, ra lò mỗi 30 phút." },
  { id: "st-16", name: "Terracotta Living", category: "Thời trang", floor: "T2", note: "Gia dụng gốm và nội thất mộc mạc Địa Trung Hải." },
  { id: "st-17", name: "Café Solstice", category: "Ẩm thực", floor: "T1", note: "Cà phê rang mộc, bản nhạc vinyl mỗi chiều." },
  { id: "st-18", name: "Harbour Books", category: "Công nghệ", floor: "T3", note: "Nhà sách và góc đọc cả gia đình nhìn ra sân vườn." },
];

export const SHOPPING_TILES = [
  { image: IMAGES.shoppingA, label: "Thời trang cao cấp", tag: "Tầng 1 — 40+ thương hiệu" },
  { image: IMAGES.shoppingB, label: "Boutique & concept store", tag: "Tầng 2 — chọn lọc mỗi mùa" },
  { image: u("1528698827591-e19ccd7bc23d", 900), label: "Quà tặng & lưu niệm", tag: "Tầng 1 — gói quà miễn phí" },
];

export const DINING_TILES = [
  { image: IMAGES.diningA, label: "Cà phê & trà chiều", tag: "Tầng 1 — 12 quán" },
  { image: IMAGES.diningB, label: "Fine dining", tag: "Tầng 2 — nhà hàng sang trọng" },
];

export const PROMOS = [
  {
    id: "pr-01",
    tag: "Ưu đãi thành viên",
    title: "Săn voucher 500.000đ mùa lễ hội",
    period: "01.11 — 24.12",
    detail: "Chi tiêu đủ 2.000.000đ nhận ngay voucher AEON Supermarket.",
  },
  {
    id: "pr-02",
    tag: "Thời trang",
    title: "Giảm đến 50% bộ sưu tập Thu–Đông",
    period: "Toàn tháng 11",
    detail: "Áp dụng cho hơn 60 gian hàng thời trang tham gia chương trình.",
  },
  {
    id: "pr-03",
    tag: "Ẩm thực",
    title: "Buffet tối thứ Tư — mua 1 tặng 1",
    period: "Mỗi thứ Tư",
    detail: "Nhà hàng Sakura Terrace và 8 quán ăn đối tác trên tầng 2.",
  },
  {
    id: "pr-04",
    tag: "Gia đình",
    title: "Tích điểm đổi vé rạp & khu chơi",
    period: "01.11 — 30.11",
    detail: "Mỗi 100.000đ hóa đơn đổi 1 điểm — 30 điểm đổi 1 vé.",
  },
];

export const EVENTS = [
  {
    id: "ev-01",
    day: "08",
    month: "TH11",
    tag: "Lễ hội",
    title: "Lễ hội Ánh Sáng Cuối Năm",
    place: "Quảng trường trung tâm",
    time: "17:00 — 22:00",
    image: IMAGES.eventA,
  },
  {
    id: "ev-02",
    day: "15",
    month: "TH11",
    tag: "Âm nhạc",
    title: "Đêm nhạc Acoustic cuối tuần",
    place: "Sảnh chính, tầng 1",
    time: "19:30 — 21:30",
    image: IMAGES.eventB,
  },
  {
    id: "ev-03",
    day: "22",
    month: "TH11",
    tag: "Giải trí",
    title: "Neon Game Weekend — Thử thách VR",
    place: "Neon Play Arena, tầng 3",
    time: "10:00 — 21:00",
    image: IMAGES.funA,
  },
  {
    id: "ev-04",
    day: "29",
    month: "TH11",
    tag: "Workshop",
    title: "Workshop Phối đồ Thu–Đông",
    place: "Boutique Atelier Rivière, tầng 1",
    time: "14:00 — 16:00",
    image: IMAGES.shoppingB,
  },
];

export const EVENT_TAGS = ["Tất cả", "Lễ hội", "Âm nhạc", "Giải trí", "Workshop"] as const;

export const FACILITIES = [
  {
    title: "Bãi xe thông minh",
    detail: "2.500 chỗ, dẫn đường tìm chỗ trống theo thời gian thực.",
  },
  {
    title: "Gửi xe điện (EV)",
    detail: "24 trạm sạc nhanh tại tầng B1 và B2.",
  },
  {
    title: "Phòng chăm sóc bé",
    detail: "Không gian yên tĩnh, tiệt trùng, có tủ sức khỏe mẹ.",
  },
  {
    title: "Wi-Fi miễn phí",
    detail: "Sóng phủ toàn trung tâm, tốc độ cao cho khách ghé thăm.",
  },
  {
    title: "Hỗ trợ khách khuyết tật",
    detail: "Lát gạch dẫn đường, thang máy ưu tiên, xe đẩy miễn phí.",
  },
  {
    title: "Y tế & an ninh 24/7",
    detail: "Phòng y tế tầng 1 và đội an ninh luôn túc trực.",
  },
];

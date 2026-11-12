package com.company.aneon.f10;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.company.aneon.AbstractMySqlIntegrationTest;
import com.company.aneon.TestSeed;
import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.constant.StoreStatus;
import com.company.aneon.f01.dto.PremiseDtos;
import com.company.aneon.f01.dto.StoreDtos;
import com.company.aneon.f01.entity.MatBang;
import com.company.aneon.f01.repository.CuaHangSapRepository;
import com.company.aneon.f01.repository.KhuVucRepository;
import com.company.aneon.f01.repository.MatBangRepository;
import com.company.aneon.f01.repository.TangRepository;
import com.company.aneon.f01.repository.ToaNhaRepository;
import com.company.aneon.f01.repository.ViTriRepository;
import com.company.aneon.f01.service.CuaHangSapService;
import com.company.aneon.f01.service.MatBangService;
import com.company.aneon.f10.constant.EventStatus;
import com.company.aneon.f10.constant.PromotionType;
import com.company.aneon.f10.dto.EventDtos;
import com.company.aneon.f10.dto.PromotionDtos;
import com.company.aneon.f10.entity.MaQrThamGia;
import com.company.aneon.f10.entity.PhieuUuDai;
import com.company.aneon.f10.entity.SuKien;
import com.company.aneon.f10.repository.LuotThamGiaSuKienRepository;
import com.company.aneon.f10.repository.MaQrThamGiaRepository;
import com.company.aneon.f10.repository.PhieuUuDaiRepository;
import com.company.aneon.f10.repository.SuKienRepository;
import com.company.aneon.f10.service.EventService;
import com.company.aneon.f10.service.EventFiles;
import com.company.aneon.f10.service.PromotionService;
import com.company.aneon.f10.service.QrCodeService;
import com.company.aneon.f10.service.VoucherService;
import com.company.aneon.shared.error.ApiException;
import com.company.aneon.shared.security.LocalIdentityFilter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Transaction/dong toi/rang buoc tren MySQL that (rule 07): voucher cuoi
 * cung, dien tich mat bang, trung code, QR active duy nhat, check-in lap,
 * trung lich sanh, idempotency ap dung khuyen mai.
 */
@AutoConfigureMockMvc
class F10ConcurrencyIT extends AbstractMySqlIntegrationTest {

    @Autowired ToaNhaRepository toaNhaRepository;
    @Autowired TangRepository tangRepository;
    @Autowired KhuVucRepository khuVucRepository;
    @Autowired ViTriRepository viTriRepository;
    @Autowired MatBangRepository matBangRepository;
    @Autowired CuaHangSapRepository cuaHangSapRepository;
    @Autowired SuKienRepository suKienRepository;
    @Autowired MaQrThamGiaRepository qrRepository;
    @Autowired LuotThamGiaSuKienRepository attendanceRepository;
    @Autowired PhieuUuDaiRepository voucherRepository;
    @Autowired VoucherService voucherService;
    @Autowired PromotionService promotionService;
    @Autowired QrCodeService qrCodeService;
    @Autowired EventService eventService;
    @Autowired EventFiles eventFiles;
    @Autowired CuaHangSapService cuaHangSapService;
    @Autowired MatBangService matBangService;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;

    private TestSeed.SpaceChain chain;
    private final UUID actorId = UUID.randomUUID();

    @BeforeEach
    void seed() {
        TestSeed.role(jdbc, "MARKETING");
        TestSeed.role(jdbc, "MANAGEMENT");
        TestSeed.role(jdbc, "OPERATIONS");
        TestSeed.account(jdbc, actorId, "MARKETING", null);
        chain = TestSeed.space(toaNhaRepository, tangRepository, khuVucRepository, viTriRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void login(UUID id, String... roles) {
        LocalIdentityFilter.LocalAuthenticationToken token = new LocalIdentityFilter.LocalAuthenticationToken(
                new com.company.aneon.shared.security.AneonPrincipal(id, "u-" + id, java.util.Set.of(roles)),
                com.company.aneon.shared.security.RolePermissions.authoritiesForRoles(java.util.Set.of(roles)));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    private UUID newCustomer() {
        UUID customerId = UUID.randomUUID();
        TestSeed.customer(jdbc, customerId, "09" + String.format("%08d",
                Math.abs(customerId.hashCode()) % 100000000));
        return customerId;
    }

    @Test
    void timDeXuatTheoTrangThaiVaPhamViTruocKhiPhanTrang() throws Exception {
        login(actorId, "MARKETING");
        var other = TestSeed.space(toaNhaRepository, tangRepository, khuVucRepository, viTriRepository);
        String prefix = "SCOPE_" + UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        for (UUID location : List.of(chain.locationId(), other.locationId())) {
            eventService.createProposal(new EventDtos.CreateEventProposalRequest(
                    prefix + location.toString().substring(0, 4), "Sự kiện " + prefix, null, location,
                    start, start.plus(1, ChronoUnit.HOURS), BigDecimal.TEN, 10, false, List.of(), List.of()));
        }
        SecurityContextHolder.clearContext();
        var search = get("/api/v1/f10/events").param("q", prefix).param("status", "PENDING_APPROVAL")
                .param("size", "1").header("X-Actor-Id", actorId).header("X-Actor-Roles", "MANAGEMENT");
        mvc.perform(search).andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(2));
        mvc.perform(search.header("X-Actor-Zones", chain.zoneId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
        mvc.perform(get("/api/v1/f10/events")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/f10/events").header("X-Actor-Id", actorId).header("X-Actor-Roles", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void chanCacThaoTacSuKienNgoaiKhuVuc() throws Exception {
        var other = TestSeed.space(toaNhaRepository, tangRepository, khuVucRepository, viTriRepository);
        SuKien event = approvedEventNow();
        String path = "/api/v1/f10/events/" + event.getId();

        mvc.perform(inZone(get(path), "MANAGEMENT", chain.zoneId())).andExpect(status().isOk());
        mvc.perform(inZone(get(path + "/report"), "MANAGEMENT", chain.zoneId())).andExpect(status().isOk());
        mvc.perform(inZone(get(path), "MANAGEMENT", other.zoneId())).andExpect(status().isForbidden());
        mvc.perform(inZone(get(path + "/report"), "MANAGEMENT", other.zoneId())).andExpect(status().isForbidden());
        mvc.perform(inZone(put(path + "/approval"), "MANAGEMENT", other.zoneId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"action\":\"APPROVE\",\"approvedBudget\":100,\"version\":0}"))
                .andExpect(status().isForbidden());
        mvc.perform(inZone(post(path + "/activities"), "MARKETING", other.zoneId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"activities\":[]}"))
                .andExpect(status().isForbidden());
        mvc.perform(inZone(post(path + "/expenses"), "MARKETING", other.zoneId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":100}"))
                .andExpect(status().isForbidden());
        mvc.perform(inZone(post(path + "/qr-code"), "MARKETING", other.zoneId())).andExpect(status().isForbidden());
        mvc.perform(inZone(post(path + "/check-ins"), "OPERATIONS", other.zoneId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"qrToken\":\"test\",\"customerId\":\"" + newCustomer() + "\"}"))
                .andExpect(status().isForbidden());
    }

    private MockHttpServletRequestBuilder inZone(MockHttpServletRequestBuilder request, String role, UUID zoneId) {
        return request.header("X-Actor-Id", actorId).header("X-Actor-Roles", role).header("X-Actor-Zones", zoneId);
    }

    record RaceResult<T>(T value, Exception error) {
        boolean ok() {
            return error == null;
        }
    }

    private static <T> Callable<RaceResult<T>> guarded(Callable<T> task, CountDownLatch go) {
        return () -> {
            go.await();
            try {
                return new RaceResult<>(task.call(), null);
            } catch (Exception ex) {
                return new RaceResult<>(null, ex);
            }
        };
    }

    /** Hai tac vu chay song song, bat dau cung luc. */
    private <T> List<RaceResult<T>> race(Callable<T> first, Callable<T> second) throws Exception {
        CountDownLatch go = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<RaceResult<T>> f1 = pool.submit(guarded(first, go));
            Future<RaceResult<T>> f2 = pool.submit(guarded(second, go));
            go.countDown();
            return List.of(f1.get(60, TimeUnit.SECONDS), f2.get(60, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void phatVoucherCuoiCungCanhTranhChiMotThangChot() throws Exception {
        // Tao kho thuoc MANAGEMENT (f10:voucher:write), phat thuoc MARKETING
        login(actorId, "MANAGEMENT");
        PromotionDtos.VoucherSummary voucher = voucherService.create(new PromotionDtos.CreateVoucherRequest(
                "VCIT" + System.nanoTime() % 100000000L, "Voucher 1 phieu",
                com.company.aneon.f10.constant.VoucherDiscountType.FIXED_AMOUNT,
                new BigDecimal("100000"), null, null, 1, 0,
                Instant.now().minus(1, ChronoUnit.HOURS), Instant.now().plus(1, ChronoUnit.DAYS), null));
        login(actorId, "MARKETING");
        UUID customerA = newCustomer();
        UUID customerB = newCustomer();

        List<RaceResult<PromotionDtos.CustomerVoucherResponse>> results = race(
                () -> {
                    login(actorId, "MARKETING");
                    return voucherService.issue(voucher.id(),
                            new PromotionDtos.IssueVoucherRequest(customerA, null));
                },
                () -> {
                    login(actorId, "MARKETING");
                    return voucherService.issue(voucher.id(),
                            new PromotionDtos.IssueVoucherRequest(customerB, null));
                });

        assertThat(results.stream().filter(RaceResult::ok).count()).isEqualTo(1);
        PhieuUuDai reload = voucherRepository.findById(voucher.id()).orElseThrow();
        assertThat(reload.getIssuedCount()).isEqualTo(1);
        assertThat(reload.getIssuedLimit()).isEqualTo(1);
    }

    @Test
    void haiCuaHangVuotDienTichKhongTheCungCommit() throws Exception {
        login(actorId, "MANAGEMENT");
        MatBang premise = TestSeed.premise(matBangRepository, khuVucRepository, chain.zoneId(),
                "MBIT" + System.nanoTime() % 100000L, new BigDecimal("100"));

        List<RaceResult<StoreDtos.StoreListItem>> results = race(
                () -> {
                    login(actorId, "MANAGEMENT");
                    return cuaHangSapService.create(storeRequest(premise.getId(), new BigDecimal("80")));
                },
                () -> {
                    login(actorId, "MANAGEMENT");
                    return cuaHangSapService.create(storeRequest(premise.getId(), new BigDecimal("80")));
                });

        assertThat(results.stream().filter(RaceResult::ok).count()).isEqualTo(1);
        BigDecimal allocated = cuaHangSapRepository.sumAllocatedArea(premise.getId());
        assertThat(allocated).isEqualByComparingTo(new BigDecimal("80"));
    }

    private StoreDtos.CreateStoreRequest storeRequest(UUID premiseId, BigDecimal area) {
        return new StoreDtos.CreateStoreRequest(
                "CHIT" + System.nanoTime() % 100000000L, "Cua hang IT", premiseId, area,
                null, null, StoreStatus.LEASED, null, null, false);
    }

    @Test
    void taoMatBangTrungCodeSongSongChiMotThanhCong() throws Exception {
        login(actorId, "MANAGEMENT");
        String code = "MBRACE" + System.nanoTime() % 100000000L;
        List<RaceResult<PremiseDtos.CreatePremiseResult>> results = race(
                () -> {
                    login(actorId, "MANAGEMENT");
                    return matBangService.create(new PremiseDtos.CreatePremiseRequest(
                            code, "MB race", chain.zoneId(), new BigDecimal("100"),
                            null, PremiseStatus.VACANT, null, null));
                },
                () -> {
                    login(actorId, "MANAGEMENT");
                    return matBangService.create(new PremiseDtos.CreatePremiseRequest(
                            code, "MB race", chain.zoneId(), new BigDecimal("100"),
                            null, PremiseStatus.VACANT, null, null));
                });
        assertThat(results.stream().filter(RaceResult::ok).count()).isEqualTo(1);
    }

    @Test
    void motSuKienChiMotQrActiveDuSac() throws Exception {
        login(actorId, "MARKETING");
        SuKien event = approvedEventNow();
        List<RaceResult<EventDtos.QrCodeDto>> results = race(
                () -> {
                    login(actorId, "MARKETING");
                    return qrCodeService.generate(event.getId());
                },
                () -> {
                    login(actorId, "MARKETING");
                    return qrCodeService.generate(event.getId());
                });
        long activeCount = qrRepository.findByEventIdOrderByCreatedAtDesc(event.getId()).stream()
                .filter(MaQrThamGia::isActive).count();
        assertThat(activeCount).isEqualTo(1);
        assertThat(results).allMatch(RaceResult::ok);
    }

    @Test
    void checkInLapKhongTangLuotLanHai() {
        login(actorId, "MARKETING");
        SuKien event = approvedEventNow();
        EventDtos.QrCodeDto oldQr = qrCodeService.generate(event.getId());
        EventDtos.QrCodeDto qr = qrCodeService.generate(event.getId());
        UUID customerId = newCustomer();
        UUID staffId = UUID.randomUUID();
        TestSeed.account(jdbc, staffId, "OPERATIONS", null);
        String token = qrToken(qr);

        login(staffId, "OPERATIONS");
        assertThatThrownBy(() -> qrCodeService.checkIn(event.getId(),
                new EventDtos.CheckInRequest(qrToken(oldQr), customerId, null)))
                .isInstanceOf(ApiException.class).hasMessageContaining("da bi thu hoi");
        EventDtos.CheckInResult first = qrCodeService.checkIn(event.getId(),
                new EventDtos.CheckInRequest(token, customerId, null));
        EventDtos.CheckInResult second = qrCodeService.checkIn(event.getId(),
                new EventDtos.CheckInRequest(token, customerId, null));
        assertThat(first.duplicate()).isFalse();
        assertThat(second.duplicate()).isTrue();
        assertThat(attendanceRepository.countByEventId(event.getId())).isEqualTo(1);
    }

    private static String qrToken(EventDtos.QrCodeDto qr) {
        return qr.checkInUrl().substring(qr.checkInUrl().indexOf("token=") + 6).split("&")[0];
    }

    @Test
    void trungLichSanhGiuaHaiSuKienVaTiepGiapHopLe() {
        login(actorId, "MARKETING");
        SuKien eventA = approvedEventNow();
        SuKien eventB = approvedEventNow();
        eventB.setCode(eventB.getCode() + "B");
        suKienRepository.save(eventB);

        eventService.addActivities(eventA.getId(), new EventDtos.CreateActivitiesRequest(List.of(
                new EventDtos.CreateActivityRequest("Mua lan A",
                        eventA.getStartAt().plusSeconds(3600), eventA.getStartAt().plusSeconds(7200),
                        chain.locationId(), actorId, null))));
        // Khung gio tiep giap (khoang nua mo [start,end)) phai duoc phep
        EventDtos.ActivityCreatedResult adjacent = eventService.addActivities(eventB.getId(),
                new EventDtos.CreateActivitiesRequest(List.of(
                        new EventDtos.CreateActivityRequest("Mua lan B",
                                eventA.getStartAt().plusSeconds(7200),
                                eventA.getStartAt().plusSeconds(10800),
                                chain.locationId(), actorId, null))));
        assertThat(adjacent.createdCount()).isEqualTo(1);

        // Trung mot phan: bi chan voi SCHEDULE_CONFLICT
        assertThatThrownBy(() -> eventService.addActivities(eventB.getId(),
                new EventDtos.CreateActivitiesRequest(List.of(
                        new EventDtos.CreateActivityRequest("Chong trung",
                                eventA.getStartAt().plusSeconds(9000),
                                eventA.getStartAt().plusSeconds(12600),
                                chain.locationId(), actorId, null)))))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).specificCode())
                .isEqualTo("SCHEDULE_CONFLICT");
    }

    @Test
    void apDungIdempotencyChotLap() {
        login(actorId, "MARKETING");
        PromotionDtos.PromotionSummary promotion = promotionService.create(
                new PromotionDtos.CreatePromotionRequest(
                        "KMIT" + System.nanoTime() % 100000000L, "KM giam gia IT",
                        PromotionType.DISCOUNT, Instant.now().minus(1, ChronoUnit.HOURS),
                        Instant.now().plus(1, ChronoUnit.DAYS), null, null, null, false, null,
                        new PromotionDtos.DiscountSpec(new BigDecimal("10"), null), null, null));

        MatBang premise = TestSeed.premise(matBangRepository, khuVucRepository, chain.zoneId(),
                "MBKM" + System.nanoTime() % 100000L, new BigDecimal("100"));
        UUID storeId = TestSeed.store(cuaHangSapRepository, premise,
                "CHKM" + System.nanoTime() % 100000L, new BigDecimal("50")).getId();
        UUID customerId = newCustomer();
        UUID invoiceId = UUID.randomUUID();
        TestSeed.invoice(jdbc, invoiceId, customerId, storeId, new BigDecimal("500000"), true,
                "HD-" + invoiceId.toString().substring(0, 8));

        String idempotencyKey = "IT-KEY-" + System.nanoTime();
        var first = promotionService.apply(promotion.id(),
                new PromotionDtos.ApplyPromotionRequest(invoiceId, customerId, null, idempotencyKey));
        var second = promotionService.apply(promotion.id(),
                new PromotionDtos.ApplyPromotionRequest(invoiceId, customerId, null, idempotencyKey));
        assertThat(first.duplicate()).isFalse();
        assertThat(second.duplicate()).isTrue();
        assertThat(second.applicationId()).isEqualTo(first.applicationId());
        var realtime = promotionService.realtime(promotion.id());
        assertThat(realtime.usedCount()).isEqualTo(1);
        assertThat(realtime.totalDiscountGranted()).isEqualByComparingTo("50000");
    }

    @Test
    void luuSuaGuiBanNhapVaCapNhatLichHoatDong() throws Exception {
        login(actorId, "MARKETING");
        String code = "DRAFT-" + UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        var draft = eventService.createDraft(new EventDtos.CreateEventProposalRequest(code, "Ban nhap", null,
                chain.locationId(), start, end, BigDecimal.TEN, 10, false, List.of(), List.of()));
        assertThat(draft.status()).isEqualTo(EventStatus.DRAFT);
        byte[] pdf = "%PDF-1.4\n% ANEON test\n%%EOF".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        var attachment = eventFiles.upload(draft.id(), new MockMultipartFile("file", "Ke hoach.pdf", "application/pdf", pdf));
        assertThat(eventFiles.list(draft.id())).containsExactly(attachment);
        assertThat(eventFiles.download(draft.id(), attachment.id()).bytes()).containsExactly(pdf);
        assertThatThrownBy(() -> eventFiles.upload(draft.id(), new MockMultipartFile("file", "gia.pdf", "application/pdf", new byte[] { 1 })))
                .isInstanceOf(ApiException.class);
        var edited = eventService.updateDraft(draft.id(), eventService.detail(draft.id()).version(),
                new EventDtos.CreateEventProposalRequest(code, "Ban nhap da sua", null,
                        chain.locationId(), start, end, BigDecimal.TEN, 10, false, List.of(), List.of()));
        assertThat(edited.name()).isEqualTo("Ban nhap da sua");
        assertThat(eventService.submitDraft(draft.id(), edited.version()).status()).isEqualTo(EventStatus.PENDING_APPROVAL);
        assertThat(eventFiles.list(draft.id())).containsExactly(attachment);

        var activity = new EventDtos.CreateActivityRequest("Tiet muc", start, start.plusSeconds(1800), chain.locationId(), null, null);
        eventService.addActivities(draft.id(), new EventDtos.CreateActivitiesRequest(List.of(activity)));
        var item = eventService.activities(draft.id()).getFirst();
        var updated = eventService.updateActivity(draft.id(), item.id(), item.version(),
                new EventDtos.CreateActivityRequest("Tiet muc da sua", start, start.plusSeconds(1800), chain.locationId(), null, null));
        assertThat(updated.name()).isEqualTo("Tiet muc da sua");
        eventService.archiveActivity(draft.id(), item.id(), updated.version());
        assertThat(eventService.activities(draft.id())).isEmpty();
    }

    private SuKien approvedEventNow() {
        SuKien event = new SuKien();
        event.setId(UUID.randomUUID());
        event.setCode("EVIT" + System.nanoTime() % 100000000L);
        event.setName("Su kien IT");
        event.setLocation(viTriRepository.findById(chain.locationId()).orElseThrow());
        event.setStatus(EventStatus.APPROVED);
        event.setStartAt(Instant.now().minus(2, ChronoUnit.HOURS));
        event.setEndAt(Instant.now().plus(2, ChronoUnit.HOURS));
        event.setEstimatedBudget(new BigDecimal("10000000"));
        event.setTargetAttendees(100);
        event.setProposedBy(actorId);
        event.setApprovedBy(actorId);
        event.setApprovedAt(Instant.now().minus(3, ChronoUnit.HOURS));
        event.setPublicVisible(true);
        suKienRepository.save(event);
        return event;
    }
}

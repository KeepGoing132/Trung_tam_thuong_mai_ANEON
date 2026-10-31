package com.company.aneon;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.company.aneon.f01.entity.CuaHangSap;
import com.company.aneon.f01.entity.KhuVuc;
import com.company.aneon.f01.entity.MatBang;
import com.company.aneon.f01.entity.Tang;
import com.company.aneon.f01.entity.ToaNha;
import com.company.aneon.f01.entity.ViTri;
import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.constant.StoreStatus;
import com.company.aneon.f01.repository.CuaHangSapRepository;
import com.company.aneon.f01.repository.KhuVucRepository;
import com.company.aneon.f01.repository.MatBangRepository;
import com.company.aneon.f01.repository.TangRepository;
import com.company.aneon.f01.repository.ToaNhaRepository;
import com.company.aneon.f01.repository.ViTriRepository;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Du lieu goc cho IT: tao chuoi toa nha -> tang -> khu vuc -> vi tri ->
 * mat bang -> cua hang, tai khoan F16 (FK changed_by/proposed_by) va khach.
 */
public final class TestSeed {

    private TestSeed() {
    }

    public record SpaceChain(UUID buildingId, UUID floorId, UUID zoneId, UUID locationId) {
    }

    public static SpaceChain space(ToaNhaRepository toaNhaRepository, TangRepository tangRepository,
                                   KhuVucRepository khuVucRepository, ViTriRepository viTriRepository) {
        UUID buildingId = UUID.randomUUID();
        ToaNha building = new ToaNha(buildingId, "TN-" + buildingId.toString().substring(0, 8),
                "Toa nha IT", null, null);
        toaNhaRepository.save(building);

        UUID floorId = UUID.randomUUID();
        Tang floor = new Tang();
        floor.setId(floorId);
        floor.setCode("T-" + floorId.toString().substring(0, 8));
        floor.setName("Tang 1 IT");
        floor.setLevel(1);
        floor.setBuilding(building);
        tangRepository.save(floor);

        UUID zoneId = UUID.randomUUID();
        KhuVuc zone = new KhuVuc();
        zone.setId(zoneId);
        zone.setCode("KV-" + zoneId.toString().substring(0, 8));
        zone.setName("Khu vuc IT");
        zone.setFloor(floor);
        khuVucRepository.save(zone);

        UUID locationId = UUID.randomUUID();
        ViTri location = new ViTri();
        location.setId(locationId);
        location.setZone(zone);
        location.setX(new BigDecimal("10.0000"));
        location.setY(new BigDecimal("20.0000"));
        location.setTextDescription("Sanh chinh IT");
        viTriRepository.save(location);
        return new SpaceChain(buildingId, floorId, zoneId, locationId);
    }

    public static MatBang premise(MatBangRepository repository, KhuVucRepository khuVucRepository,
                                  UUID zoneId, String code, BigDecimal totalArea) {
        MatBang premise = new MatBang();
        premise.setId(UUID.randomUUID());
        premise.setCode(code);
        premise.setName("Mat bang " + code);
        premise.setZone(khuVucRepository.findById(zoneId).orElseThrow());
        premise.setTotalArea(totalArea);
        premise.setStatus(PremiseStatus.VACANT);
        return repository.save(premise);
    }

    public static CuaHangSap store(CuaHangSapRepository repository, MatBang premise,
                                   String code, BigDecimal area) {
        CuaHangSap store = new CuaHangSap();
        store.setId(UUID.randomUUID());
        store.setCode(code);
        store.setName("Cua hang " + code);
        store.setPremise(premise);
        store.setArea(area);
        store.setStatus(StoreStatus.LEASED);
        store.setPublicVisible(true);
        return repository.save(store);
    }

    public static void role(JdbcTemplate jdbc, String code) {
        jdbc.update("""
                INSERT INTO vai_tro (id, code, name, version)
                VALUES (?, ?, ?, 0)
                ON DUPLICATE KEY UPDATE name = VALUES(name)
                """, UUID.randomUUID().toString(), code, "Vai tro " + code);
    }

    public static String roleId(JdbcTemplate jdbc, String code) {
        return jdbc.queryForObject("SELECT id FROM vai_tro WHERE code = ?", String.class, code);
    }

    /** Tai khoan F16 de FK changed_by/proposed_by hop le. */
    public static void account(JdbcTemplate jdbc, UUID accountId, String roleCode, UUID customerId) {
        String roleId = roleId(jdbc, roleCode);
        jdbc.update("""
                INSERT INTO tai_khoan_nguoi_dung
                  (id, username, full_name, role_id, status, auth_version, customer_id, version)
                VALUES (?, ?, ?, ?, 'ACTIVE', 0, ?, 0)
                ON DUPLICATE KEY UPDATE username = VALUES(username)
                """, accountId.toString(), "tk-" + accountId.toString().substring(0, 8),
                "Nguoi test " + roleId, roleId, customerId == null ? null : customerId.toString());
    }

    public static void customer(JdbcTemplate jdbc, UUID customerId, String phone) {
        jdbc.update("""
                INSERT INTO khach_mua_sam (id, full_name, phone, version)
                VALUES (?, ?, ?, 0)
                ON DUPLICATE KEY UPDATE full_name = VALUES(full_name)
                """, customerId.toString(), "Khach IT " + phone, phone);
    }

    public static void invoice(JdbcTemplate jdbc, UUID invoiceId, UUID customerId, UUID storeId,
                               BigDecimal total, boolean verified, String number) {
        jdbc.update("""
                INSERT INTO hoa_don_mua_hang
                  (id, number, customer_id, store_id, total_amount, verified, version)
                VALUES (?, ?, ?, ?, ?, ?, 0)
                ON DUPLICATE KEY UPDATE number = VALUES(number)
                """, invoiceId.toString(), number, customerId.toString(), storeId.toString(),
                total, verified);
    }
}

package com.company.aneon.f01;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.company.aneon.f01.constant.PremiseStatus;
import com.company.aneon.f01.constant.StatusTransition;
import com.company.aneon.shared.error.ApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Ma tran chuyen trang thai D0005: RETIRED la trang thai ket thuc;
 * khong cho chuyen tuy y.
 */
class StatusTransitionTest {

    @Test
    void retiredKhongChuyenTiep() {
        for (PremiseStatus to : PremiseStatus.values()) {
            assertThat(StatusTransition.canTransition(PremiseStatus.RETIRED, to))
                    .as("RETIRED -> " + to)
                    .isFalse();
        }
    }

    @Test
    void khongChoChuyenChinhNo() {
        assertThat(StatusTransition.canTransition(PremiseStatus.VACANT, PremiseStatus.VACANT)).isFalse();
    }

    @Test
    void vacantSangCacTrangThaiHopLe() {
        Set.of(PremiseStatus.RESERVED, PremiseStatus.LEASED, PremiseStatus.MAINTENANCE,
                PremiseStatus.SUSPENDED, PremiseStatus.RETIRED)
                .forEach(to -> assertThat(StatusTransition.canTransition(PremiseStatus.VACANT, to)).isTrue());
    }

    @Test
    void assertCanTransitionNem409() {
        assertThatThrownBy(() -> StatusTransition.assertCanTransition(
                PremiseStatus.RETIRED, PremiseStatus.VACANT, "mat bang"))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).errorCode())
                .isEqualTo(com.company.aneon.shared.error.ErrorCode.BUSINESS_CONFLICT);
    }
}

package com.company.aneon.f01.constant;

import java.util.Map;
import java.util.Set;

import com.company.aneon.shared.error.ApiException;

/**
 * Ma tran chuyen trang thai dung cho ca PremiseStatus va StoreStatus
 * (hai tap trung gia tri) - de xuat baseline trong D0005.
 * LEASED -> VACANT can kiem tra hop dong rieng o service.
 */
public final class StatusTransition {

    private static final Map<Enum<?>, Set<Enum<?>>> ALLOWED = Map.of(
            PremiseStatus.VACANT, Set.of(PremiseStatus.RESERVED, PremiseStatus.LEASED,
                    PremiseStatus.MAINTENANCE, PremiseStatus.SUSPENDED, PremiseStatus.RETIRED),
            PremiseStatus.RESERVED, Set.of(PremiseStatus.VACANT, PremiseStatus.LEASED,
                    PremiseStatus.MAINTENANCE, PremiseStatus.SUSPENDED),
            PremiseStatus.LEASED, Set.of(PremiseStatus.RESERVED, PremiseStatus.VACANT,
                    PremiseStatus.MAINTENANCE, PremiseStatus.SUSPENDED, PremiseStatus.RETIRED),
            PremiseStatus.MAINTENANCE, Set.of(PremiseStatus.VACANT, PremiseStatus.RESERVED,
                    PremiseStatus.LEASED, PremiseStatus.SUSPENDED, PremiseStatus.RETIRED),
            PremiseStatus.SUSPENDED, Set.of(PremiseStatus.VACANT, PremiseStatus.RESERVED,
                    PremiseStatus.LEASED, PremiseStatus.MAINTENANCE, PremiseStatus.RETIRED),
            PremiseStatus.RETIRED, Set.of());

    private StatusTransition() {
    }

    public static boolean canTransition(Enum<?> from, Enum<?> to) {
        if (from == to) {
            return false;
        }
        Set<Enum<?>> targets = ALLOWED.get(from);
        return targets != null && targets.contains(to);
    }

    public static void assertCanTransition(Enum<?> from, Enum<?> to, String resource) {
        if (!canTransition(from, to)) {
            throw new ApiException(com.company.aneon.shared.error.ErrorCode.BUSINESS_CONFLICT,
                    "Khong cho phep chuyen trang thai cua " + resource + " tu " + from + " sang " + to);
        }
    }
}

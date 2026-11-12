package com.company.aneon.f01.controller;

import java.util.List;
import java.util.UUID;

import com.company.aneon.f01.integration.DigitalMapPort;
import com.company.aneon.f01.service.F01ReferenceQuery;
import com.company.aneon.shared.security.RolePermissions;
import com.company.aneon.shared.web.ApiResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/f01")
public class F01ReferenceController {
    private final F01ReferenceQuery references;

    public F01ReferenceController(F01ReferenceQuery references) {
        this.references = references;
    }

    @GetMapping("/premises/{id}/map")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_PREMISE_READ + "')")
    public ApiResponse<DigitalMapPort.FloorMap> map(@PathVariable UUID id) {
        return ApiResponse.ok("Lay so do thanh cong", references.premiseMap(id));
    }

    @GetMapping("/stores/{id}/tenants")
    @PreAuthorize("hasAuthority('" + RolePermissions.F01_STORE_READ + "')")
    public ApiResponse<List<F01ReferenceQuery.Tenant>> tenants(@PathVariable UUID id) {
        return ApiResponse.ok("Tra cuu khach thue thanh cong", references.storeTenants(id));
    }
}

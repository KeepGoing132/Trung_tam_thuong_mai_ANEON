package com.company.aneon.f10.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.company.aneon.f10.constant.EventStatus;
import com.company.aneon.f01.entity.ViTri;
import com.company.aneon.shared.persistence.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Su kien (bang su_kien). Map UTC theo schema; ten truong khac moc noi
 * trong Word (maSuKien...) duoc map ro ra cot tuong ung (rule 00).
 */
@Entity
@Table(name = "su_kien")
@Getter
@Setter
@NoArgsConstructor
public class SuKien extends Auditable {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private UUID id;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Column(name = "type", length = 100)
    private String type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private ViTri location;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @Column(name = "estimated_budget", precision = 18, scale = 2)
    private BigDecimal estimatedBudget;

    @Column(name = "approved_budget", precision = 18, scale = 2)
    private BigDecimal approvedBudget;

    @Column(name = "target_attendees")
    private Integer targetAttendees;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 40)
    private EventStatus status;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "proposed_by", length = 36)
    private UUID proposedBy;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "approved_by", length = 36)
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "public_visible", nullable = false)
    private boolean publicVisible;
}

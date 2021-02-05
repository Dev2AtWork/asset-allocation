package com.asset.allocation.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FundAllocation {
    @JsonProperty
    @Builder.Default
    private BigDecimal highRisk = BigDecimal.ZERO;

    @JsonProperty
    @Builder.Default
    private BigDecimal retirement = BigDecimal.ZERO;
}

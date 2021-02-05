package com.asset.allocation.dto;

import com.asset.allocation.domain.DepositPlan;
import com.asset.allocation.domain.FundAllocation;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
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
public class TestData {
    @JsonProperty
    private DepositPlan      depositPlan;
    @JsonProperty
    private String           description;
    @JsonProperty
    private List<BigDecimal> deposits;
    @JsonProperty
    private FundAllocation   fundAllocation;
}

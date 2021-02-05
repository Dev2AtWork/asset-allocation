package com.asset.allocation.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class DepositPlan {

    @JsonProperty
    private String         uuid;
    @JsonProperty
    private FundAllocation onetime;
    @JsonProperty
    private FundAllocation monthly;
    @JsonProperty
    private double         riskAppetite;

    public FundAllocation portfolioSummary() {
        return FundAllocation
            .builder()
            .highRisk(onetime
                .getHighRisk()
                .add(monthly.getHighRisk()))
            .retirement(onetime
                .getRetirement()
                .add(monthly.getRetirement()))
            .build();
    }
}

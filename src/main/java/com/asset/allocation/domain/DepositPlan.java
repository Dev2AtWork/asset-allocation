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
    private FundAllocation onetime;
    @JsonProperty
    private FundAllocation monthly;
    @JsonProperty
    private Double         riskAppetite;

}

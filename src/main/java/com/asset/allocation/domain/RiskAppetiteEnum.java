package com.asset.allocation.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum RiskAppetiteEnum {
    DEFENSIVE(0.3),
    BALANCED(0.6),
    AGGRESSIVE(1.0);

    private Double high;

}

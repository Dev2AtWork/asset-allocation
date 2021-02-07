package com.asset.allocation.helper;

import static com.asset.allocation.helper.FundDistribution.oneTimeHighRiskAllocationCovered;
import static com.asset.allocation.helper.FundDistribution.oneTimeRetirementAllocationCovered;

import com.asset.allocation.domain.FundAllocation;
import com.asset.allocation.domain.Portfolio;
import com.asset.allocation.domain.RiskAppetiteEnum;
import com.asset.util.TriFunction;
import java.math.BigDecimal;
import java.math.RoundingMode;

public interface OneTimeFundAllocation {
    TriFunction<BigDecimal, Portfolio, RiskAppetiteEnum, FundAllocation> allocateRemainingOneTimeByRiskAppetite = (excessFund, portfolio, riskAppetite) -> {
        switch (riskAppetite) {
            case DEFENSIVE:
                if (!oneTimeRetirementAllocationCovered.apply(portfolio)) {
                    return FundAllocation
                        .builder()
                        .retirement(excessFund)
                        .build();
                }
                BigDecimal remainingHighRisk = FundDistributionHelper.remainingHighRisk.apply(portfolio);
                if (remainingHighRisk.compareTo(excessFund) >= 0) {
                    return FundAllocation
                        .builder()
                        .highRisk(excessFund)
                        .build();
                }
                return FundAllocation
                    .builder()
                    .highRisk(remainingHighRisk)
                    .retirement(excessFund.subtract(remainingHighRisk))
                    .build();

            case BALANCED:
                if (!oneTimeRetirementAllocationCovered.apply(portfolio) && !oneTimeHighRiskAllocationCovered.apply(portfolio)) {
                    BigDecimal remainingRetirement = FundDistributionHelper.remainingRetirement.apply(portfolio);
                    BigDecimal remainingHighRiskFund = FundDistributionHelper.remainingHighRisk.apply(portfolio);
                    if (excessFund.compareTo(remainingHighRiskFund
                        .add(remainingRetirement)) >= 0) {
                        BigDecimal fundToDistribute = excessFund.subtract(remainingHighRiskFund
                            .add(remainingRetirement));
                        BigDecimal divide = fundToDistribute.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                        return FundAllocation
                            .builder()
                            .highRisk(remainingHighRiskFund.add(divide))
                            .retirement(remainingRetirement.add(fundToDistribute.subtract(divide)))
                            .build();
                    }
                    final BigDecimal highRiskAllocation = portfolio
                        .getDepositPlan()
                        .getOnetime()
                        .getHighRisk()
                        .divide(portfolio
                            .getDepositPlan()
                            .getOnetime()
                            .getHighRisk()
                            .add(portfolio
                                .getDepositPlan()
                                .getOnetime()
                                .getRetirement()), 6, RoundingMode.HALF_UP)
                        .multiply(excessFund)
                        .setScale(2, RoundingMode.HALF_UP);
                    return FundAllocation
                        .builder()
                        .highRisk(highRiskAllocation)
                        .retirement(excessFund.subtract(highRiskAllocation))
                        .build();
                } else if (!oneTimeHighRiskAllocationCovered.apply(portfolio)) {
                    BigDecimal remainingHighRiskFund = FundDistributionHelper.remainingHighRisk.apply(portfolio);
                    if (remainingHighRiskFund.compareTo(excessFund) >= 0) {
                        return FundAllocation
                            .builder()
                            .highRisk(excessFund)
                            .build();
                    }
                    BigDecimal remainingFundAfterOneTimeAllocation = excessFund.subtract(remainingHighRiskFund);
                    BigDecimal distribution = remainingFundAfterOneTimeAllocation.divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP);
                    return FundAllocation
                        .builder()
                        .highRisk(remainingHighRiskFund.add(distribution))
                        .retirement(remainingFundAfterOneTimeAllocation.subtract(distribution))
                        .build();
                }
                return FundAllocation
                    .builder()
                    .build();
            case AGGRESSIVE:
                if (!oneTimeHighRiskAllocationCovered.apply(portfolio)) {
                    return FundAllocation
                        .builder()
                        .highRisk(excessFund)
                        .build();
                }
                BigDecimal remainingRetirement = FundDistributionHelper.remainingRetirement.apply(portfolio);
                if (remainingRetirement.compareTo(excessFund) >= 0) {
                    return FundAllocation
                        .builder()
                        .retirement(excessFund)
                        .build();
                }
                return FundAllocation
                    .builder()
                    .retirement(remainingRetirement)
                    .highRisk(excessFund.subtract(remainingRetirement))
                    .build();

            default:
                throw new IllegalStateException("unknown risk category");
        }
    };
}

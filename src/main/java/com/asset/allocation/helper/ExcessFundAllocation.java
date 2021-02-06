package com.asset.allocation.helper;

import static com.asset.allocation.helper.FundDistribution.allocateExcessFundByRisk;
import static com.asset.allocation.helper.FundDistribution.oneTimeAllocationCovered;
import static com.asset.allocation.helper.FundDistribution.oneTimeHighRiskAllocationCovered;
import static com.asset.allocation.helper.FundDistribution.oneTimeRetirementAllocationCovered;
import static com.asset.allocation.helper.FundDistributionUtil.monthlyAllocationHighRisk;
import static com.asset.allocation.helper.FundDistributionUtil.monthlyAllocationRetirement;

import com.asset.allocation.domain.FundAllocation;
import com.asset.allocation.domain.Portfolio;
import com.asset.allocation.domain.RiskAppetiteEnum;
import com.asset.util.TriFunction;
import java.math.BigDecimal;
import java.math.RoundingMode;

public interface ExcessFundAllocation {
    TriFunction<BigDecimal, Portfolio, RiskAppetiteEnum, FundAllocation> allocateRemainingOneTimeByRiskAppetite = (excessFund, portfolio, riskAppetite) -> {
        switch (riskAppetite) {
            case DEFENSIVE:
                if (!oneTimeRetirementAllocationCovered.apply(portfolio)) {
                    return FundAllocation
                        .builder()
                        .retirement(excessFund)
                        .build();
                }
                BigDecimal remainingHighRisk = FundDistributionUtil.remainingHighRisk.apply(portfolio);
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
                    BigDecimal remainingRetirement = FundDistributionUtil.remainingRetirement.apply(portfolio);
                    BigDecimal remainingHighRiskFund = FundDistributionUtil.remainingHighRisk.apply(portfolio);
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
                    BigDecimal remainingHighRiskFund = FundDistributionUtil.remainingHighRisk.apply(portfolio);
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
                BigDecimal remainingRetirement = FundDistributionUtil.remainingRetirement.apply(portfolio);
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

    TriFunction<BigDecimal, Portfolio, RiskAppetiteEnum, FundAllocation> allocateExtraFundByRiskAppetite = (deposit, portfolio, riskAppetite) -> {
        FundAllocation fundAllocation = FundAllocation
            .builder()
            .build();
        fundAllocation.setHighRisk(monthlyAllocationHighRisk.apply(portfolio));
        fundAllocation.setRetirement(monthlyAllocationRetirement.apply(portfolio));
        BigDecimal remainingFund = deposit.subtract(fundAllocation
            .getRetirement()
            .add(fundAllocation.getHighRisk()));
        if (oneTimeAllocationCovered.apply(portfolio)) {
            FundAllocation excessFundAllocation = allocateExcessFundByRisk.apply(remainingFund, riskAppetite);
            fundAllocation.setHighRisk(fundAllocation
                .getHighRisk()
                .add(excessFundAllocation.getHighRisk()));
            fundAllocation.setRetirement(fundAllocation
                .getRetirement()
                .add(excessFundAllocation.getRetirement()));
        } else {
            FundAllocation excessFundOneTimeAllocation = allocateRemainingOneTimeByRiskAppetite.apply(remainingFund, portfolio,
                riskAppetite);
            fundAllocation.setHighRisk(fundAllocation
                .getHighRisk()
                .add(excessFundOneTimeAllocation.getHighRisk()));
            fundAllocation.setRetirement(fundAllocation
                .getRetirement()
                .add(excessFundOneTimeAllocation.getRetirement()));
        }

        return fundAllocation;
    };
}
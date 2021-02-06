package com.asset.allocation.helper;

import static com.asset.allocation.helper.FundAllocationHelper.allocateExcessFundByRisk;
import static com.asset.allocation.helper.FundAllocationHelper.oneTimeAllocationCovered;
import static com.asset.allocation.helper.FundAllocationHelper.oneTimeHighRiskAllocationCovered;
import static com.asset.allocation.helper.FundAllocationHelper.oneTimeRetirementAllocationCovered;
import static com.asset.allocation.helper.PortfolioProcessHelper.monthlyAllocationHighRisk;
import static com.asset.allocation.helper.PortfolioProcessHelper.monthlyAllocationRetirement;

import com.asset.allocation.domain.FundAllocation;
import com.asset.allocation.domain.Portfolio;
import com.asset.allocation.domain.RiskAppetiteEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;

public interface ExcessFundAllocationHelper {
    TriFunction<BigDecimal, Portfolio, RiskAppetiteEnum, FundAllocation> allocateRemainingOneTimeByRiskAppetite = (excessFund, portfolio, riskAppetite) -> {
        switch (riskAppetite) {
            case DEFENSIVE:
                if (!oneTimeRetirementAllocationCovered.apply(portfolio)) {
                    return FundAllocation
                        .builder()
                        .retirement(excessFund)
                        .build();
                }
                BigDecimal remainingHighRisk = PortfolioProcessHelper.remainingHighRisk.apply(portfolio);
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
                    BigDecimal remainingRetirement = PortfolioProcessHelper.remainingRetirement.apply(portfolio);
                    BigDecimal remainingHighRiskFund = PortfolioProcessHelper.remainingHighRisk.apply(portfolio);
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
                    return FundAllocation
                        .builder()
                        .build();
                } else if (!oneTimeHighRiskAllocationCovered.apply(portfolio)) {
                    BigDecimal remainingHighRiskFund = PortfolioProcessHelper.remainingHighRisk.apply(portfolio);
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
                BigDecimal remainingRetirement = PortfolioProcessHelper.remainingRetirement.apply(portfolio);
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

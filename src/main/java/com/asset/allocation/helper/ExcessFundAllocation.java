package com.asset.allocation.helper;

import static com.asset.allocation.helper.FundDistribution.allocateExcessFundByRisk;
import static com.asset.allocation.helper.FundDistribution.oneTimeAllocationCovered;
import static com.asset.allocation.helper.FundDistributionHelper.monthlyAllocationHighRisk;
import static com.asset.allocation.helper.FundDistributionHelper.monthlyAllocationRetirement;
import static com.asset.allocation.helper.OneTimeFundAllocation.allocateRemainingOneTimeByRiskAppetite;

import com.asset.allocation.domain.FundAllocation;
import com.asset.allocation.domain.Portfolio;
import com.asset.allocation.domain.RiskAppetiteEnum;
import com.asset.util.TriFunction;
import java.math.BigDecimal;
import java.math.RoundingMode;

public interface ExcessFundAllocation {

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

    TriFunction<BigDecimal, Portfolio, RiskAppetiteEnum, FundAllocation> allocateMonthlyFundByRiskAppetite = (deposit, portfolio, riskAppetite) -> {
        final BigDecimal monthlyHighRiskAllocation = monthlyAllocationHighRisk.apply(portfolio);
        final BigDecimal monthlyRetirementAllocation = monthlyAllocationRetirement.apply(portfolio);
        switch (riskAppetite) {
            case DEFENSIVE:
                if (deposit.compareTo(monthlyRetirementAllocation) <= 0)
                    return FundAllocation
                        .builder()
                        .retirement(deposit)
                        .build();
                if (deposit
                    .subtract(monthlyHighRiskAllocation.add(monthlyRetirementAllocation))
                    .compareTo(BigDecimal.ZERO) > 0)
                    return FundAllocation
                        .builder()
                        .retirement(deposit.subtract(monthlyHighRiskAllocation))
                        .highRisk(monthlyHighRiskAllocation)
                        .build();
                return FundAllocation
                    .builder()
                    .retirement(monthlyRetirementAllocation)
                    .highRisk(deposit.subtract(monthlyRetirementAllocation))
                    .build();

            case AGGRESSIVE:
                if (deposit.compareTo(monthlyHighRiskAllocation) <= 0)
                    return FundAllocation
                        .builder()
                        .highRisk(deposit)
                        .build();
                if (deposit
                    .subtract(monthlyRetirementAllocation.add(monthlyHighRiskAllocation))
                    .compareTo(BigDecimal.ZERO) > 0)
                    return FundAllocation
                        .builder()
                        .retirement(monthlyRetirementAllocation)
                        .highRisk(deposit.subtract(monthlyRetirementAllocation))
                        .build();
                return FundAllocation
                    .builder()
                    .retirement(deposit.subtract(monthlyHighRiskAllocation))
                    .highRisk(monthlyHighRiskAllocation)
                    .build();

            case BALANCED:
                final BigDecimal halfAmount = deposit.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                return FundAllocation
                    .builder()
                    .highRisk(halfAmount.compareTo(monthlyHighRiskAllocation) > 0 ? monthlyHighRiskAllocation : halfAmount)
                    .retirement(
                        halfAmount.compareTo(monthlyHighRiskAllocation) > 0 ? deposit.subtract(monthlyHighRiskAllocation) : halfAmount)
                    .build();
            default:
                throw new IllegalStateException("unknown risk category");
        }

    };
}

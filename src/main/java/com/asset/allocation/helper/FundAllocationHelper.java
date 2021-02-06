package com.asset.allocation.helper;

import com.asset.allocation.domain.FundAllocation;
import com.asset.allocation.domain.Portfolio;
import com.asset.allocation.domain.RiskAppetiteEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface FundAllocationHelper {
    BiFunction<Portfolio, BigDecimal, FundAllocation> defensiveFundAllocation = (portfolio, deposit) ->
    {
        if (deposit.compareTo(portfolio
            .getDepositPlan()
            .getOnetime()
            .getRetirement()) >= 0) {
            return FundAllocation
                .builder()
                .highRisk(deposit.subtract(portfolio
                    .getDepositPlan()
                    .getOnetime()
                    .getRetirement()))
                .retirement(portfolio
                    .getDepositPlan()
                    .getOnetime()
                    .getRetirement())
                .build();
        }

        return FundAllocation
            .builder()
            .retirement(deposit)
            .build();

    };

    BiFunction<Portfolio, BigDecimal, FundAllocation> balancedFundAllocation = (portfolio, deposit) -> {
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
            .multiply(deposit)
            .setScale(2, RoundingMode.HALF_UP);
        return FundAllocation
            .builder()
            .highRisk(highRiskAllocation)
            .retirement(deposit.subtract(highRiskAllocation))
            .build();
    };

    BiFunction<Portfolio, BigDecimal, FundAllocation> aggressiveFundAllocation = (portfolio, deposit) ->
    {
        if (deposit.compareTo(portfolio
            .getDepositPlan()
            .getOnetime()
            .getHighRisk()) >= 0) {
            return FundAllocation
                .builder()
                .retirement(deposit.subtract(portfolio
                    .getDepositPlan()
                    .getOnetime()
                    .getHighRisk()))
                .highRisk(portfolio
                    .getDepositPlan()
                    .getOnetime()
                    .getHighRisk())
                .build();
        }
        return FundAllocation
            .builder()
            .highRisk(deposit)
            .build();
    };

    BiFunction<BigDecimal, RiskAppetiteEnum, FundAllocation> allocateExcessFundByRisk = (remainingFund, riskAppetite) -> {
        switch (riskAppetite) {
            case DEFENSIVE:
                return FundAllocation
                    .builder()
                    .retirement(remainingFund)
                    .build();
            case BALANCED:
                return FundAllocation
                    .builder()
                    .retirement(remainingFund.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP))
                    .highRisk(remainingFund.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP))
                    .build();
            case AGGRESSIVE:
                return FundAllocation
                    .builder()
                    .highRisk(remainingFund)
                    .build();
            default:
                throw new IllegalStateException("unknown risk category");
        }
    };

    Function<Portfolio, Boolean> oneTimeHighRiskAllocationCovered = (portfolio ->
        portfolio
            .getPortfolioSummary()
            .getHighRisk()
            .compareTo(portfolio
                .getDepositPlan()
                .getOnetime()
                .getHighRisk()) >= 0
    );

    Function<Portfolio, Boolean> oneTimeRetirementAllocationCovered = (portfolio ->
        portfolio
            .getPortfolioSummary()
            .getRetirement()
            .compareTo(portfolio
                .getDepositPlan()
                .getOnetime()
                .getRetirement()) >= 0
    );

    Function<Portfolio, Boolean> oneTimeAllocationCovered = (portfolio ->
        oneTimeHighRiskAllocationCovered.apply(portfolio) && oneTimeRetirementAllocationCovered.apply(portfolio)
    );

}

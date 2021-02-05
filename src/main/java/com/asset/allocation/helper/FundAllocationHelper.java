package com.asset.allocation.helper;

import com.asset.allocation.domain.FundAllocation;
import com.asset.allocation.domain.Portfolio;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.BiFunction;

public interface FundAllocationHelper {
    BiFunction<Portfolio, BigDecimal, FundAllocation> defensiveFundAllocation = (portfolio, deposit) ->
        FundAllocation
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

    BiFunction<Portfolio, BigDecimal, FundAllocation> balancedFundAllocation = (portfolio, deposit) ->
    {
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
        FundAllocation
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

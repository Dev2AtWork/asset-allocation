package com.asset.allocation.helper;

import static com.asset.allocation.domain.RiskAppetiteEnum.AGGRESSIVE;
import static com.asset.allocation.domain.RiskAppetiteEnum.BALANCED;
import static com.asset.allocation.domain.RiskAppetiteEnum.DEFENSIVE;

import com.asset.allocation.domain.Portfolio;
import com.asset.allocation.domain.RiskAppetiteEnum;
import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface FundDistributionHelper {

    Function<Double, RiskAppetiteEnum> findRiskAppetiteEnum = (riskAppetite ->
        riskAppetite <= DEFENSIVE.getHigh() ? DEFENSIVE :
            riskAppetite <= BALANCED.getHigh() ? BALANCED : AGGRESSIVE
    );

    Function<Portfolio, Boolean>           firstDeposit         = (portfolio ->
        portfolio
            .getPortfolioSummary()
            .getRetirement()
            .equals(BigDecimal.ZERO) && portfolio
            .getPortfolioSummary()
            .getHighRisk()
            .equals(BigDecimal.ZERO)
    );

    Function<Portfolio, Boolean> oneTimeCovered = (portfolio ->
        portfolio
            .getPortfolioSummary()
            .getHighRisk()
            .compareTo(portfolio
                .getDepositPlan()
                .getOnetime()
                .getHighRisk()) == 0 &&
            portfolio
                .getPortfolioSummary()
                .getRetirement()
                .compareTo(portfolio
                    .getDepositPlan()
                    .getOnetime()
                    .getRetirement()) == 0
    );

    Function<Portfolio, BigDecimal> remainingHighRisk = (portfolio ->
        portfolio
            .getDepositPlan()
            .getOnetime()
            .getHighRisk()
            .subtract(portfolio
                .getPortfolioSummary()
                .getHighRisk())
    );

    Function<Portfolio, BigDecimal> remainingRetirement = (portfolio -> portfolio
        .getDepositPlan()
        .getOnetime()
        .getRetirement()
        .subtract(portfolio
            .getPortfolioSummary()
            .getRetirement()));

    Function<Portfolio, BigDecimal> monthlyAllocationHighRisk = (portfolio -> portfolio
        .getDepositPlan()
        .getMonthly()
        .getHighRisk());

    Function<Portfolio, BigDecimal> monthlyAllocationRetirement = (portfolio -> portfolio
        .getDepositPlan()
        .getMonthly()
        .getRetirement());

    BiFunction<BigDecimal, Portfolio, Boolean> remainingOneTimeAndMonthlyEqualsDeposit = (deposit, portfolio) -> deposit
        .compareTo(
            remainingHighRisk
                .apply(portfolio)
                .add(remainingRetirement.apply(portfolio))
                .add(monthlyAllocationHighRisk.apply(portfolio))
                .add(monthlyAllocationRetirement.apply(portfolio))
        ) == 0;

    BiFunction<BigDecimal, Portfolio, Boolean> depositGtMonthlyAllocation = (deposit, portfolio) -> deposit
        .compareTo(
            remainingHighRisk
                .apply(portfolio)
                .add(remainingRetirement.apply(portfolio))
                .add(monthlyAllocationHighRisk.apply(portfolio))
                .add(monthlyAllocationRetirement.apply(portfolio))
        ) > 0;

    BiFunction<BigDecimal, Portfolio, Boolean> remainingOneTimeAndMonthlyLtDeposit = (deposit, portfolio) -> deposit
        .compareTo(
            remainingHighRisk
                .apply(portfolio)
                .add(remainingRetirement.apply(portfolio))
                .add(monthlyAllocationHighRisk.apply(portfolio))
                .add(monthlyAllocationRetirement.apply(portfolio))
        ) > 0;
}

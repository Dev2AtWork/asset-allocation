package com.asset.allocation.helper;

import com.asset.allocation.domain.Portfolio;
import java.math.BigDecimal;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface PortfolioProcessHelper {
    Function<Portfolio, Boolean> firstDeposit = (portfolio ->
        portfolio
            .getPortfolioSummary()
            .getRetirement()
            .equals(BigDecimal.ZERO) && portfolio
            .getPortfolioSummary()
            .getHighRisk()
            .equals(BigDecimal.ZERO)
    );

    BiFunction<BigDecimal, Portfolio, Boolean> equalsFirstDeposit = (deposit, portfolio) -> deposit.compareTo(portfolio
        .getDepositPlan()
        .getOnetime()
        .getHighRisk()
        .add(portfolio
            .getDepositPlan()
            .getOnetime()
            .getRetirement())) == 0;

    BiFunction<BigDecimal, Portfolio, Boolean> ltFirstDeposit = (deposit, portfolio) -> deposit.compareTo(portfolio
        .getDepositPlan()
        .getOnetime()
        .getHighRisk()
        .add(portfolio
            .getDepositPlan()
            .getOnetime()
            .getRetirement())) < 0;

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

    BiFunction<BigDecimal, Portfolio, Boolean> remainingOneTimeAndMonthlyLtDeposit = (deposit, portfolio) -> deposit
        .compareTo(
            remainingHighRisk
                .apply(portfolio)
                .add(remainingRetirement.apply(portfolio))
                .add(monthlyAllocationHighRisk.apply(portfolio))
                .add(monthlyAllocationRetirement.apply(portfolio))
        ) > 0;
}

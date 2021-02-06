package com.asset.allocation.processor;

import static com.asset.allocation.domain.RiskAppetiteEnum.AGGRESSIVE;
import static com.asset.allocation.domain.RiskAppetiteEnum.BALANCED;
import static com.asset.allocation.domain.RiskAppetiteEnum.DEFENSIVE;
import static com.asset.allocation.helper.ExcessFundAllocationHelper.allocateExtraFundByRiskAppetite;
import static com.asset.allocation.helper.FundAllocationHelper.aggressiveFundAllocation;
import static com.asset.allocation.helper.FundAllocationHelper.balancedFundAllocation;
import static com.asset.allocation.helper.FundAllocationHelper.defensiveFundAllocation;
import static com.asset.allocation.helper.PortfolioProcessHelper.*;

import com.asset.allocation.domain.FundAllocation;
import com.asset.allocation.domain.Portfolio;
import com.asset.allocation.domain.RiskAppetiteEnum;
import java.math.BigDecimal;
import java.util.function.Function;

public interface FundAllocationProcessor {

    Function<Double, RiskAppetiteEnum> findRiskAppetiteEnum = (riskAppetite ->
        riskAppetite <= DEFENSIVE.getHigh() ? DEFENSIVE :
            riskAppetite <= BALANCED.getHigh() ? BALANCED : AGGRESSIVE
    );

    static FundAllocation process(final Portfolio portfolio, final BigDecimal deposit) {
        final RiskAppetiteEnum riskAppetiteEnum = findRiskAppetiteEnum.apply(portfolio
            .getDepositPlan()
            .getRiskAppetite());
        if (firstDeposit.apply(portfolio)) {
            if (equalsFirstDeposit.apply(deposit, portfolio)) {
                return FundAllocation
                    .builder()
                    .highRisk(portfolio
                        .getDepositPlan()
                        .getOnetime()
                        .getHighRisk())
                    .retirement(portfolio
                        .getDepositPlan()
                        .getOnetime()
                        .getRetirement())
                    .build();
            } else if (ltFirstDeposit.apply(deposit, portfolio)) {
                switch (riskAppetiteEnum) {
                    case DEFENSIVE:
                        return defensiveFundAllocation.apply(portfolio, deposit);
                    case BALANCED:
                        return balancedFundAllocation.apply(portfolio, deposit);
                    case AGGRESSIVE:
                        return aggressiveFundAllocation.apply(portfolio, deposit);
                }

            }
        } else if (oneTimeCovered.apply(portfolio)) {
            if (deposit.compareTo(portfolio
                .getDepositPlan()
                .getMonthly()
                .getRetirement()) == 0) {
                return FundAllocation
                    .builder()
                    .highRisk(BigDecimal.ZERO)
                    .retirement(deposit)
                    .build();
            }
        } else {
            if (remainingOneTimeAndMonthlyEqualsDeposit.apply(deposit, portfolio)) {
                return FundAllocation
                    .builder()
                    .highRisk(remainingHighRisk
                        .apply(portfolio)
                        .add(monthlyAllocationHighRisk.apply(portfolio)))
                    .retirement(remainingRetirement
                        .apply(portfolio)
                        .add(monthlyAllocationRetirement.apply(portfolio)))
                    .build();
            } else if (remainingOneTimeAndMonthlyLtDeposit.apply(deposit, portfolio)) {
                return allocateExtraFundByRiskAppetite.apply(deposit, portfolio, riskAppetiteEnum);
            }
        }

        return FundAllocation
            .builder()
            .build();
    }
}

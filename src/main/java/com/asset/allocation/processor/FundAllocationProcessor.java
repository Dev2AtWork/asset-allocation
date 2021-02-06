package com.asset.allocation.processor;

import static com.asset.allocation.helper.ExcessFundAllocation.allocateExtraFundByRiskAppetite;
import static com.asset.allocation.helper.ExcessFundAllocation.allocateMonthlyFundByRiskAppetite;
import static com.asset.allocation.helper.ExcessFundAllocation.allocateRemainingOneTimeByRiskAppetite;
import static com.asset.allocation.helper.FundDistribution.aggressiveFundAllocation;
import static com.asset.allocation.helper.FundDistribution.balancedFundAllocation;
import static com.asset.allocation.helper.FundDistribution.defensiveFundAllocation;
import static com.asset.allocation.helper.FundDistributionHelper.*;

import com.asset.allocation.domain.FundAllocation;
import com.asset.allocation.domain.Portfolio;
import com.asset.allocation.domain.RiskAppetiteEnum;
import java.math.BigDecimal;

public interface FundAllocationProcessor {

    static FundAllocation process(final Portfolio portfolio, final BigDecimal deposit) {
        final RiskAppetiteEnum riskAppetiteEnum = findRiskAppetiteEnum.apply(portfolio
            .getDepositPlan()
            .getRiskAppetite());
        if (firstDeposit.apply(portfolio)) {
            switch (riskAppetiteEnum) {
                case DEFENSIVE:
                    return defensiveFundAllocation.apply(portfolio, deposit);
                case BALANCED:
                    return balancedFundAllocation.apply(portfolio, deposit);
                case AGGRESSIVE:
                    return aggressiveFundAllocation.apply(portfolio, deposit);

            }
        }
        if (oneTimeCovered.apply(portfolio)) {
            if (depositGtMonthlyAllocation.apply(deposit, portfolio)) {
                return allocateExtraFundByRiskAppetite.apply(deposit, portfolio, riskAppetiteEnum);
            }
            return allocateMonthlyFundByRiskAppetite.apply(deposit, portfolio, riskAppetiteEnum);
        }
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
        }
        if (remainingOneTimeAndMonthlyLtDeposit.apply(deposit, portfolio)) {
            return allocateExtraFundByRiskAppetite.apply(deposit, portfolio, riskAppetiteEnum);
        }
        return allocateRemainingOneTimeByRiskAppetite.apply(deposit, portfolio, riskAppetiteEnum);

    }
}

package com.asset.allocation.domain;

import static com.asset.allocation.processor.FundAllocationProcessor.process;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

    @Builder.Default
    private String         uuid = UUID
        .randomUUID()
        .toString();
    private DepositPlan    depositPlan;
    private FundAllocation portfolioSummary;

    public void deposit(final BigDecimal deposit) {
        final FundAllocation fundAllocation = process(this, deposit);
        this
            .getPortfolioSummary()
            .setHighRisk(this
                .getPortfolioSummary()
                .getHighRisk()
                .add(fundAllocation.getHighRisk()));
        this
            .getPortfolioSummary()
            .setRetirement(this
                .getPortfolioSummary()
                .getRetirement()
                .add(fundAllocation.getRetirement()));
    }
}

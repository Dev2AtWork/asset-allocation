package com.asset.allocation.test;

import static com.asset.allocation.helper.YamlUtils.getObjectListFromYml;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.asset.allocation.domain.FundAllocation;
import com.asset.allocation.domain.Portfolio;
import com.asset.allocation.dto.TestData;
import java.io.File;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FundsAllocationTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("smallerDataSetProvider")
    public void allocateFundTest(String description, TestData testData) {

        final Portfolio portfolio = Portfolio
            .builder()
            .depositPlan(testData.getDepositPlan())
            .portfolioSummary(FundAllocation
                .builder()
                .build())
            .build();

        testData
            .getDeposits()
            .forEach(portfolio::deposit);

        assertAll(
            () -> assertEquals(testData
                .getFundAllocation()
                .getHighRisk()
                .stripTrailingZeros(), portfolio
                .getPortfolioSummary()
                .getHighRisk()
                .stripTrailingZeros(), "High Risk Fund Allocation"),
            () -> assertEquals(testData
                .getFundAllocation()
                .getRetirement()
                .stripTrailingZeros(), portfolio
                .getPortfolioSummary()
                .getRetirement()
                .stripTrailingZeros(), "Retirement Fund Allocation")
        );
    }

    private static Stream<Arguments> dataProvider() {
        return getObjectListFromYml(new File(Objects
            .requireNonNull(FundsAllocationTest.class
                .getClassLoader()
                .getResource("acceptance-criteria.yml"))
            .getFile()), TestData.class)
            .stream()
            .map(testData -> Arguments.of(testData.getDescription(), testData));
    }

    private static Stream<Arguments> smallerDataSetProvider() {
        return getObjectListFromYml(new File(Objects
            .requireNonNull(FundsAllocationTest.class
                .getClassLoader()
                .getResource("test.yml"))
            .getFile()), TestData.class)
            .stream()
            .map(testData -> Arguments.of(testData.getDescription(), testData));
    }
}

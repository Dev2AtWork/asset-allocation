package com.asset.allocation.test;

import static com.asset.allocation.helper.YamlUtils.getObjectListFromYml;

import com.asset.allocation.domain.FundAllocation;
import com.asset.allocation.dto.TestData;
import com.asset.allocation.processor.FundAllocationProcessor;
import java.io.File;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class FundsAllocationTest {

    FundAllocationProcessor allocationProcessor = new FundAllocationProcessor();

    @ParameterizedTest(name = "{0}")
    @MethodSource("dataProvider")
    public void allocateFundTest(String description, TestData testData) {
        final FundAllocation fundAllocation = allocationProcessor.allocateFund(testData.getDepositPlan(), testData.getDeposits());
        Assertions.assertEquals(fundAllocation.getHighRisk(), testData
            .getFundAllocation()
            .getHighRisk());
        Assertions.assertEquals(fundAllocation.getRetirement(), testData
            .getFundAllocation()
            .getRetirement());
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
}

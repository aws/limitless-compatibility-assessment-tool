// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.assessment.testutils.TestPostgresSqlParserHelper;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GrantStmtManagerTest {
    private static ConfigLoader configLoader;

    @BeforeAll
    public static void setup() {
        configLoader = mock(ConfigLoader.class);
    }

    @Test
    public void test_grantStmt()
    {
        GrantStmtManager manager = new GrantStmtManager(configLoader);
        HashMap<String, Feature> testSqls = new HashMap<>();
        Feature featureGrantOnDomain = new Feature("grant_privilege_on_domain", false, "GRANT PRIVILEGE ON DOMAIN is not supported");
        when(configLoader.getFeatureConfig("grant_privilege_on_domain")).thenReturn(featureGrantOnDomain);
        Feature featureGrantOnLanguage = new Feature("grant_privilege_on_language", false, "GRANT PRIVILEGE ON LANGUAGE is not supported");
        when(configLoader.getFeatureConfig("grant_privilege_on_language")).thenReturn(featureGrantOnLanguage);
        Feature featureGrantOnLargeObject = new Feature("grant_privilege_on_largeobject", false, "GRANT PRIVILEGE ON LARGEOBJECT is not supported");
        when(configLoader.getFeatureConfig("grant_privilege_on_largeobject")).thenReturn(featureGrantOnLargeObject);
        Feature featureGrantOnProcedure = new Feature("grant_privilege_on_procedure", false, "GRANT PRIVILEGE ON PROCEDURE is not supported");
        when(configLoader.getFeatureConfig("grant_privilege_on_procedure")).thenReturn(featureGrantOnProcedure);
        Feature featureGrantOnRoutine = new Feature("grant_privilege_on_routine", false, "GRANT PRIVILEGE ON ROUTINE is not supported");
        when(configLoader.getFeatureConfig("grant_privilege_on_routine")).thenReturn(featureGrantOnRoutine);
        Feature featureGrantOnType = new Feature("grant_privilege_on_type", false, "GRANT PRIVILEGE ON TYPE is not supported");
        when(configLoader.getFeatureConfig("grant_privilege_on_type")).thenReturn(featureGrantOnType);
        Feature featureGrantOnForeignServer = new Feature("grant_privilege_on_foreign_server",false, "GRANT PRIVILEGE ON FOREIGN SERVER is not supported");
        when(configLoader.getFeatureConfig("grant_privilege_on_foreign_server")).thenReturn(featureGrantOnForeignServer);
        Feature featureGrantOnFdw = new Feature("grant_privilege_on_fdw", false, "GRANT PRIVILEGE ON FDW is not supported");
        when(configLoader.getFeatureConfig("grant_privilege_on_fdw")).thenReturn(featureGrantOnFdw);

        testSqls.put("GRANT ALL PRIVILEGES ON DOMAIN x TO y", featureGrantOnDomain);
        testSqls.put("GRANT ALL PRIVILEGES ON LANGUAGE x TO Y", featureGrantOnLanguage);
        testSqls.put("GRANT ALL PRIVILEGES ON LARGE OBJECT 12345 TO y", featureGrantOnLargeObject);
        testSqls.put("GRANT ALL PRIVILEGES ON PROCEDURE x TO y", featureGrantOnProcedure);
        testSqls.put("GRANT ALL PRIVILEGES ON ALL PROCEDURES TO y", featureGrantOnProcedure);
        testSqls.put("GRANT ALL PRIVILEGES ON ROUTINE x TO y", featureGrantOnRoutine);
        testSqls.put("GRANT ALL PRIVILEGES ON ALL ROUTINES TO y", featureGrantOnRoutine);
        testSqls.put("GRANT ALL PRIVILEGES ON TYPE x TO y", featureGrantOnType);
        testSqls.put("GRANT ALL PRIVILEGES ON FOREIGN SERVER x TO y", featureGrantOnForeignServer);
        testSqls.put("GRANT ALL PRIVILEGES ON FOREIGN DATA WRAPPER x TO y", featureGrantOnFdw);
        testSqls.put("GRANT ALL ON TABLE x TO y", ConfigLoader.DEFAULT_SUPPORTED_FEATURE);

        for (Map.Entry<String, Feature> entry : testSqls.entrySet())
        {
            PostgreSQLParser.RootContext context = TestPostgresSqlParserHelper.parseSQL(entry.getKey());
            PostgreSQLParser.GrantstmtContext grantstmtContext =
                context.stmtblock().stmtmulti().stmt(0).grantstmt();
            List<StatementResult> resultList = manager.analyzeStatement(grantstmtContext);
            assertEquals(1, resultList.size());
            StatementResult result = resultList.get(0);
            assertEquals(entry.getValue().isSupported(), result.getFeature().isSupported());
            if (!result.getFeature().isSupported()) {
                assertEquals(entry.getValue().getErrorMessage(), result.getFeature().getErrorMessage());
            }
        }
    }
}

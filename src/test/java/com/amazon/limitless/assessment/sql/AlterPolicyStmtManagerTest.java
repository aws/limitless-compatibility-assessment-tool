// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;
import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.assessment.sql.AlterPolicyStmtManager;
import com.amazon.limitless.assessment.testutils.TestPostgresSqlParserHelper;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import com.amazon.limitless.assessment.common.DependencyObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class AlterPolicyStmtManagerTest {

    private static ConfigLoader configLoader;

    @BeforeAll
    public static void setup() {
        configLoader = mock(ConfigLoader.class);
    }

    @Test
    public void test_alterPolicy() {
        AlterPolicyStmtManager manager = new AlterPolicyStmtManager(configLoader);
        HashMap<String, Feature> testSqls = new HashMap<>();


        testSqls.put("ALTER POLICY dep_p1 ON dep1 TO regress_rls_bob,regress_rls_carol;",
                new Feature("alter_policy", true, null));

        testSqls.put("ALTER POLICY p1 ON s1.event_trigger_test USING (TRUE) with check (true);",
                new Feature("alter_policy", false, "relation s1.event_trigger_test does not exist"));

        testSqls.put("ALTER POLICY r1 ON s2.rec1 USING (x = (SELECT a FROM rec2 WHERE b = y));",
                new Feature("alter_policy", true, null));

        testSqls.put("ALTER POLICY r1 ON rec1 USING (x = (SELECT a FROM rec2 WHERE b = y));",
                new Feature("alter_policy", false, "relation public.rec1 does not exist"));


        Feature feature = new Feature("alter_policy", true, null);
        when(configLoader.getFeatureConfig("alter_policy")).thenReturn(feature);


        try (MockedStatic<DependencyObject> mockedDependency = mockStatic(DependencyObject.class)) {
            mockedDependency.when(() -> DependencyObject.generateFullObjectName("public", "dep1")).thenReturn("public.dep1");
            mockedDependency.when(() -> DependencyObject.generateFullObjectName("s1", "event_trigger_test")).thenReturn("s1.event_trigger_test");
            mockedDependency.when(() -> DependencyObject.generateFullObjectName("s2", "rec1")).thenReturn("s2.rec1");
            mockedDependency.when(() -> DependencyObject.generateFullObjectName("public", "rec1")).thenReturn("public.rec1");
            mockedDependency.when(() -> DependencyObject.getObject("public.dep1", "table")).thenReturn(true);
            mockedDependency.when(() -> DependencyObject.getObject("s1.event_trigger_test", "table")).thenReturn(false);
            mockedDependency.when(() -> DependencyObject.getObject("s2.rec1", "table")).thenReturn(true);
            mockedDependency.when(() -> DependencyObject.getObject("public.rec1", "table")).thenReturn(false);

            for (Map.Entry<String, Feature> entry : testSqls.entrySet()) {
                PostgreSQLParser.RootContext context = TestPostgresSqlParserHelper.parseSQL(entry.getKey());
                PostgreSQLParser.AlterpolicystmtContext alterpolicystmtContext =
                    context.stmtblock().stmtmulti().stmt(0).alterpolicystmt();
                List<StatementResult> resultList = manager.analyzeStatement(alterpolicystmtContext);
                assertEquals(1, resultList.size());
                StatementResult result = resultList.get(0);
                assertEquals(entry.getValue().isSupported(), result.getFeature().isSupported());
                if (!result.getFeature().isSupported()) {
                    assertEquals(entry.getValue().getErrorMessage(), result.getFeature().getErrorMessage());
                }
            }
        }
    }
}

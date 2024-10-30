// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.assessment.sql.CreatePolicyStmtManager;
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

public class CreatePolicyStmtManagerTest {

    private static ConfigLoader configLoader;

    @BeforeAll
    public static void setup() {
        configLoader = mock(ConfigLoader.class);
    }

    @Test
    public void test_createPolicy() {
        CreatePolicyStmtManager manager = new CreatePolicyStmtManager(configLoader);
        HashMap<String, Feature> testSqls = new HashMap<>();

        testSqls.put("CREATE POLICY p1 ON sch1.table1 FOR SELECT USING (true);",
        new Feature("create_policy", true, null));

        testSqls.put("CREATE POLICY p1 ON public.table1 FOR SELECT USING (true);",
                new Feature("create_policy", false, "relation public.table1 does not exist"));

        testSqls.put("CREATE POLICY p2 ON table1 AS PERMISSIVE FOR ALL USING (true);",
                new Feature("create_policy", false, "relation public.table1 does not exist"));

        testSqls.put("CREATE POLICY p1 ON s1.test FOR SELECT USING (true);",
                new Feature("create_policy", true, null));

        testSqls.put("CREATE POLICY p1 ON measurement FOR SELECT TO PUBLIC USING (true);",
            new Feature("create_policy", false, "relation public.measurement does not exist"));

        testSqls.put("CREATE POLICY p1 ON document AS PERMISSIVE USING (dlevel <= (SELECT seclv FROM uaccount WHERE pguser = current_user));",
            new Feature("create_policy", false, "relation public.document does not exist"));

        testSqls.put("CREATE POLICY p1 ON public.document FOR SELECT USING (true);",
                new Feature("create_policy", false, "relation public.document does not exist"));

        testSqls.put("create policy p1 on ker17115 with check (id =1);",
                new Feature("create_policy", true, null));


        Feature feature = new Feature("create_policy", true, null);
        when(configLoader.getFeatureConfig("create_policy")).thenReturn(feature);


        try (MockedStatic<DependencyObject> mockedDependency = mockStatic(DependencyObject.class)) {
            mockedDependency.when(() -> DependencyObject.generateFullObjectName("sch1", "table1")).thenReturn("sch1.table1");
            mockedDependency.when(() -> DependencyObject.generateFullObjectName("public", "table1")).thenReturn("public.table1");
            mockedDependency.when(() -> DependencyObject.generateFullObjectName("s1", "test")).thenReturn("s1.test");
            mockedDependency.when(() -> DependencyObject.generateFullObjectName("public", "measurement")).thenReturn("public.measurement");
            mockedDependency.when(() -> DependencyObject.generateFullObjectName("public", "document")).thenReturn("public.document");
            mockedDependency.when(() -> DependencyObject.generateFullObjectName("public", "ker17115")).thenReturn("public.ker17115");
            mockedDependency.when(() -> DependencyObject.getObject("sch1.table1", "table")).thenReturn(true);
            mockedDependency.when(() -> DependencyObject.getObject("public.table1", "table")).thenReturn(false);
            mockedDependency.when(() -> DependencyObject.getObject("s1.test", "table")).thenReturn(true);
            mockedDependency.when(() -> DependencyObject.getObject("public.measurement", "table")).thenReturn(false);
            mockedDependency.when(() -> DependencyObject.getObject("public.document", "table")).thenReturn(false);
            mockedDependency.when(() -> DependencyObject.getObject("public.ker17115", "table")).thenReturn(true);

            for (Map.Entry<String, Feature> entry : testSqls.entrySet()) {
                PostgreSQLParser.RootContext context = TestPostgresSqlParserHelper.parseSQL(entry.getKey());
                PostgreSQLParser.CreatepolicystmtContext createpolicystmtContext =
                    context.stmtblock().stmtmulti().stmt(0).createpolicystmt();
                List<StatementResult> resultList = manager.analyzeStatement(createpolicystmtContext);
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

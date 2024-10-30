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

public class CreateFunctionStmtManagerTest {
    private static ConfigLoader configLoader;

    @BeforeAll
    public static void setup() {
        configLoader = mock(ConfigLoader.class);
    }

    @Test
    public void test_createFunction()
    {
        CreateFunctionStmtManager manager = new CreateFunctionStmtManager(configLoader);
        HashMap<String, Feature> testSqls = new HashMap<>();
        testSqls.put("CREATE FUNCTION redact.json(a json, b jsonb) RETURNS jsonb" +
                " LANGUAGE sql IMMUTABLE AS $$ select redact.jsonb(a::jsonb, b) $$;",
            new Feature("create_function", true, null));
        testSqls.put("CREATE FUNCTION public.func2(int) RETURNS int LANGUAGE C AS $$ nosuchfile $$;",
            new Feature("create_function", false, "Invalid or untrusted language"));
        testSqls.put("CREATE FUNCTION public.calculate_factorial(n INTEGER) " +
            "RETURNS BIGINT LANGUAGE plpgsql AS $$ " +
            "DECLARE\n" +
            "    result BIGINT := 1;\n" +
            "    i INTEGER;\n" +
            "BEGIN\n" +
            "    IF n < 0 THEN\n" +
            "        RAISE EXCEPTION 'Negative input not allowed';\n" +
            "    END IF;\n" +
            "    FOR i IN 1..n LOOP\n" +
            "        result := result * i;\n" +
            "    END LOOP;\n" +
            "\n" +
            "    RETURN result;\n" +
            "END;\n" +
            "$$;", new Feature("create_function", true, null));
        testSqls.put("CREATE PROCEDURE public.ptest2() LANGUAGE SQL AS $$ SELECT 5; $$;",
            new Feature("create_function", true, null));

        Feature feature = new Feature("create_function", true, null);
        when(configLoader.getFeatureConfig("create_function")).thenReturn(feature);
        for (Map.Entry<String, Feature> entry : testSqls.entrySet())
        {
            PostgreSQLParser.RootContext context = TestPostgresSqlParserHelper.parseSQL(entry.getKey());
            PostgreSQLParser.CreatefunctionstmtContext createfunctionstmtContext =
                context.stmtblock().stmtmulti().stmt(0).createfunctionstmt();
            List<StatementResult> resultList = manager.analyzeStatement(createfunctionstmtContext);
            assertEquals(1, resultList.size());
            StatementResult result = resultList.get(0);
            assertEquals(entry.getValue().isSupported(), result.getFeature().isSupported());
            if (!result.getFeature().isSupported()) {
                assertEquals(entry.getValue().getErrorMessage(), result.getFeature().getErrorMessage());
            }
        }
    }
}

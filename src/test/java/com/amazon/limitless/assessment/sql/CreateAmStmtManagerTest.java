// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateAmStmtManagerTest {
    private static ConfigLoader configLoader;


    @BeforeAll
    public static void setup() {
        configLoader = mock(ConfigLoader.class);
    }

    @Test
    public void test_createAm()
    {
        CreateAmStmtManager manager = new CreateAmStmtManager(configLoader);
        PostgreSQLParser.CreateamstmtContext ctx = mock(
            PostgreSQLParser.CreateamstmtContext.class
        );
        Feature feature = new Feature("create_access_method", false, "CREATE ACCESS METHOD is not supported");
        when(configLoader.getFeatureConfig("create_access_method")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
	    assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }
}

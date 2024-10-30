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

public class DefaultUnsupportedStmtManagerTest {
    private static ConfigLoader configLoader;


    @BeforeAll
    public static void setup() {
        configLoader = mock(ConfigLoader.class);
    }

    @Test
    public void test_defaultUnsupported()
    {
        DefaultUnsupportedStmtManager manager = new DefaultUnsupportedStmtManager(configLoader);
        Feature feature = new Feature("unsupported", false, "This statement is not supported");
        List<StatementResult> resultList = manager.analyzeStatement();
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(result.getFeature().getContext(), ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE.getContext());
        assertEquals(result.getFeature().getErrorMessage(), ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE.getErrorMessage());
    }
}

// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

public class AlterExtensionContentsStmtManagerTest {

    private static ConfigLoader configLoader;
    
    private static AlterExtensionContentsStmtManager manager;

    private static PostgreSQLParser.AlterextensioncontentsstmtContext ctx;

    @BeforeEach
    public void setup() {
        configLoader = mock(ConfigLoader.class);
        manager = new AlterExtensionContentsStmtManager(configLoader);
        ctx = mock(PostgreSQLParser.AlterextensioncontentsstmtContext.class);
    }

    private void setupMocksForAddDropContext(boolean isAdd) {
        PostgreSQLParser.Add_dropContext add_dropCtx = mock(PostgreSQLParser.Add_dropContext.class);
        if (isAdd) {
            when(add_dropCtx.ADD_P()).thenReturn(mock(TerminalNode.class));
        } else {
            when(add_dropCtx.DROP()).thenReturn(mock(TerminalNode.class));
        }
        when(ctx.add_drop()).thenReturn(add_dropCtx);
    }

    @Test
    public void test_alterExtensionContents_forAddObject() {
        Feature feature = new Feature("alter_extension_add_object", false, "ALTER EXTENSION ADD OBJECT is not supported");

        setupMocksForAddDropContext(true);

        when(configLoader.getFeatureConfig("alter_extension_add_object")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_alterExtensionContents_forDropObject() {
        Feature feature = new Feature("alter_extension_drop_object", false, "ALTER EXTENSION DROP OBJECT is not supported");

        setupMocksForAddDropContext(false);

        when(configLoader.getFeatureConfig("alter_extension_drop_object")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }
}
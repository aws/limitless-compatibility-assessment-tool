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

public class AlterExtensionStmtManagerTest {

    private static ConfigLoader configLoader;

    private static AlterExtensionStmtManager manager;

    private static PostgreSQLParser.AlterextensionstmtContext ctx;

    private static PostgreSQLParser.NameContext nameCtx;
    
    private static PostgreSQLParser.IdentifierContext identifierCtx;

    private static PostgreSQLParser.ColidContext colidCtx;

    private static TerminalNode identifierNode;
    
    private String supportedExtension = "apgdbcc";

    private String unsupportedExtension = "non_existent_extension";

    @BeforeEach
    public void setup() {
        configLoader = mock(ConfigLoader.class);
        manager = new AlterExtensionStmtManager(configLoader);
        ctx = mock(PostgreSQLParser.AlterextensionstmtContext.class);
        nameCtx = mock(PostgreSQLParser.NameContext.class);
        identifierCtx = mock(PostgreSQLParser.IdentifierContext.class);
        colidCtx = mock(PostgreSQLParser.ColidContext.class);
        identifierNode = mock(TerminalNode.class);
    }

    private void setupMocksForAlterExtensionContext(String extensionName, boolean isSupported) {
        when(identifierNode.getText()).thenReturn(extensionName);
        when(identifierCtx.Identifier()).thenReturn(identifierNode);
        when(colidCtx.identifier()).thenReturn(identifierCtx);
        when(nameCtx.colid()).thenReturn(colidCtx);
        when(ctx.name()).thenReturn(nameCtx);
        when(configLoader.isSupportedExtension(extensionName)).thenReturn(isSupported);
    }

    @Test
    public void test_alterExtension_withSupportedExtension() {
        Feature feature = new Feature("alter_extension_update_version", true, null);

        setupMocksForAlterExtensionContext(supportedExtension, true);
        when(configLoader.getFeatureConfig("alter_extension_update_version")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void test_alterExtension_withUnsupportedExtension() {
        Feature feature = new Feature("", false, String.format("Extension %s is not supported", unsupportedExtension));

        setupMocksForAlterExtensionContext(unsupportedExtension, false);
        when(configLoader.getFeatureConfig("alter_extension_update_version")).thenReturn(feature);

        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }
}
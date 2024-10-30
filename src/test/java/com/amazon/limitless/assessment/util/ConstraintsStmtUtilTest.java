// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.util;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConstraintsStmtUtilTest {

    @Mock
    private ParserRuleContext parserRuleContextMock;

    @Mock
    private PostgreSQLParser.TableconstraintContext tableConstraintContextMock;

    @Mock
    private PostgreSQLParser.ConstraintelemContext constraintElemContextMock;

    @Mock
    private PostgreSQLParser.ColconstraintContext colConstraintContextMock;

    @Mock
    private PostgreSQLParser.ColconstraintelemContext colConstraintElemContextMock;

    @Mock
    private PostgreSQLParser.ConstraintattrContext constraintAttrContextMock;

    @Mock
    private ConfigLoader configLoaderMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAnalyzeConstraintStatement_TableConstraint_CheckConstraint() {
        when(tableConstraintContextMock.constraintelem()).thenReturn(constraintElemContextMock);
        when(constraintElemContextMock.CHECK()).thenReturn(mock(TerminalNode.class));

        Feature feature = new Feature("constraint_check_standard", true, null);
        when(configLoaderMock.getFeatureConfig("constraint_check_standard")).thenReturn(feature);

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(tableConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_TableConstraint_UniqueConstraint() {
        when(tableConstraintContextMock.constraintelem()).thenReturn(constraintElemContextMock);
        when(constraintElemContextMock.UNIQUE()).thenReturn(mock(TerminalNode.class));

        Feature feature = new Feature("constraint_unique_standard", true, null);
        when(configLoaderMock.getFeatureConfig("constraint_unique_standard")).thenReturn(feature);

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(tableConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_TableConstraint_PrimaryKeyConstraint() {
        when(tableConstraintContextMock.constraintelem()).thenReturn(constraintElemContextMock);
        when(constraintElemContextMock.PRIMARY()).thenReturn(mock(TerminalNode.class));
        when(constraintElemContextMock.KEY()).thenReturn(mock(TerminalNode.class));

        Feature feature = new Feature("constraint_primary_key_standard", true, null);
        when(configLoaderMock.getFeatureConfig("constraint_primary_key_standard")).thenReturn(feature);

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(tableConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_TableConstraint_ExcludeConstraint() {
        when(tableConstraintContextMock.constraintelem()).thenReturn(constraintElemContextMock);
        when(constraintElemContextMock.EXCLUDE()).thenReturn(mock(TerminalNode.class));

        Feature feature = new Feature("constraint_exclude_standard", true, null);
        when(configLoaderMock.getFeatureConfig("constraint_exclude_standard")).thenReturn(feature);

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(tableConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_TableConstraint_ForeignKeyConstraint() {
        when(tableConstraintContextMock.constraintelem()).thenReturn(constraintElemContextMock);
        when(constraintElemContextMock.FOREIGN()).thenReturn(mock(TerminalNode.class));

        Feature feature = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(tableConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_ColumnConstraint_NotNullConstraint() {
        when(colConstraintContextMock.colconstraintelem()).thenReturn(colConstraintElemContextMock);
        when(colConstraintElemContextMock.NOT()).thenReturn(mock(TerminalNode.class));
        when(colConstraintElemContextMock.NULL_P()).thenReturn(mock(TerminalNode.class));

        Feature feature = new Feature("constraint_not_null_standard", true, null);
        when(configLoaderMock.getFeatureConfig("constraint_not_null_standard")).thenReturn(feature);

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(colConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_ColumnConstraint_NullConstraint() {
        when(colConstraintContextMock.colconstraintelem()).thenReturn(colConstraintElemContextMock);
        when(colConstraintElemContextMock.NULL_P()).thenReturn(mock(TerminalNode.class));

        Feature feature = new Feature("constraint_null_standard", true, null);
        when(configLoaderMock.getFeatureConfig("constraint_null_standard")).thenReturn(feature);

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(colConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_ColumnConstraint_UniqueConstraint() {
        when(colConstraintContextMock.colconstraintelem()).thenReturn(colConstraintElemContextMock);
        when(colConstraintElemContextMock.UNIQUE()).thenReturn(mock(TerminalNode.class));

        Feature feature = new Feature("constraint_unique_standard", true, null);
        when(configLoaderMock.getFeatureConfig("constraint_unique_standard")).thenReturn(feature);

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(colConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_ColumnConstraint_PrimaryKeyConstraint() {
        when(colConstraintContextMock.colconstraintelem()).thenReturn(colConstraintElemContextMock);
        when(colConstraintElemContextMock.PRIMARY()).thenReturn(mock(TerminalNode.class));
        when(colConstraintElemContextMock.KEY()).thenReturn(mock(TerminalNode.class));

        Feature feature = new Feature("constraint_primary_key_standard", true, null);
        when(configLoaderMock.getFeatureConfig("constraint_primary_key_standard")).thenReturn(feature);

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(colConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_ColumnConstraint_CheckConstraint() {
        when(colConstraintContextMock.colconstraintelem()).thenReturn(colConstraintElemContextMock);
        when(colConstraintElemContextMock.CHECK()).thenReturn(mock(TerminalNode.class));

        Feature feature = new Feature("constraint_check_standard", true, null);
        when(configLoaderMock.getFeatureConfig("constraint_check_standard")).thenReturn(feature);

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(colConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_ColumnConstraint_DefaultConstraint() {
        when(colConstraintContextMock.colconstraintelem()).thenReturn(colConstraintElemContextMock);
        when(colConstraintElemContextMock.DEFAULT()).thenReturn(mock(TerminalNode.class));

        Feature feature = ConfigLoader.DEFAULT_SUPPORTED_FEATURE;

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(colConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_ColumnConstraint_GeneratedAsIdentityConstraint() {
        when(colConstraintContextMock.colconstraintelem()).thenReturn(colConstraintElemContextMock);
        when(colConstraintElemContextMock.GENERATED()).thenReturn(mock(TerminalNode.class));
        when(colConstraintElemContextMock.IDENTITY_P()).thenReturn(mock(TerminalNode.class));

        Feature feature = new Feature("constraint_generated_as_identity_standard", true, null);
        when(configLoaderMock.getFeatureConfig("constraint_generated_as_identity_standard")).thenReturn(feature);

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(colConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_ColumnConstraint_GeneratedStoredConstraint() {
        when(colConstraintContextMock.colconstraintelem()).thenReturn(colConstraintElemContextMock);
        when(colConstraintElemContextMock.GENERATED()).thenReturn(mock(TerminalNode.class));

        Feature feature = new Feature("constraint_generated_stored_standard", true, null);
        when(configLoaderMock.getFeatureConfig("constraint_generated_stored_standard")).thenReturn(feature);

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(colConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_ColumnConstraint_ConstraintAttr() {
        when(colConstraintContextMock.constraintattr()).thenReturn(constraintAttrContextMock);

        Feature feature = ConfigLoader.DEFAULT_SUPPORTED_FEATURE;

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(colConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }

    @Test
    void testAnalyzeConstraintStatement_ColumnConstraint_CollateConstraint() {
        when(colConstraintContextMock.COLLATE()).thenReturn(mock(TerminalNode.class));

        Feature feature = ConfigLoader.DEFAULT_SUPPORTED_FEATURE;

        StatementResult result = ConstraintsStmtUtil.analyzeConstraintStatement(colConstraintContextMock, true, false, configLoaderMock);
        assertNotNull(result);
        assertEquals(feature, result.getFeature());
    }
}
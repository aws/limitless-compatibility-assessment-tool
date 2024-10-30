// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.util;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public class ColumnDefStmtUtilTest {

    @Mock
    private PostgreSQLParser.ColumnDefContext columnDefContextMock;

    @Mock
    private PostgreSQLParser.TypenameContext typenameContextMock;

    @Mock
    private PostgreSQLParser.SimpletypenameContext simpletypenameContextMock;

    @Mock
    private PostgreSQLParser.Qualified_nameContext qualified_nameContextMock;

    @Mock
    private PostgreSQLParser.ColquallistContext colquallistContextMock;

    @Mock
    private ConfigLoader configLoaderMock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAnalyzeColumnDefStatement_WithColConstraints() {
        PostgreSQLParser.ColconstraintContext colConstraintContextMock = mock(PostgreSQLParser.ColconstraintContext.class);
        PostgreSQLParser.ColconstraintelemContext colConstraintElemContextMock = mock(PostgreSQLParser.ColconstraintelemContext.class);
        Feature feature = new Feature("constraint_not_null_standard", true, null);
        StatementResult constraintResult = new StatementResult();
        constraintResult.setFeature(feature);

        when(columnDefContextMock.typename()).thenReturn(typenameContextMock);
        when(typenameContextMock.simpletypename()).thenReturn(simpletypenameContextMock);
        when(simpletypenameContextMock.getText()).thenReturn("integer");
        when(columnDefContextMock.colquallist()).thenReturn(colquallistContextMock);
        when(colquallistContextMock.colconstraint()).thenReturn(List.of(colConstraintContextMock));

        when(colConstraintContextMock.colconstraintelem()).thenReturn(colConstraintElemContextMock);
        when(colConstraintElemContextMock.NULL_P()).thenReturn(mock(TerminalNode.class));
        when(colConstraintElemContextMock.NOT()).thenReturn(mock(TerminalNode.class));
        when(configLoaderMock.getFeatureConfig("constraint_not_null_standard")).thenReturn(feature);

        List<StatementResult> resultList = ColumnDefStmtUtil.analyzeColumnDefStatement(columnDefContextMock, true, configLoaderMock);
        assertFalse(resultList.isEmpty());
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertTrue(result.getFeature().isSupported());
    }

    @Test
    public void testIsValidSerialType() {
        assertTrue(ColumnDefStmtUtil.isValidSerialType("smallserial"));
        assertTrue(ColumnDefStmtUtil.isValidSerialType("serial2"));
        assertTrue(ColumnDefStmtUtil.isValidSerialType("serial"));
        assertTrue(ColumnDefStmtUtil.isValidSerialType("serial4"));
        assertTrue(ColumnDefStmtUtil.isValidSerialType("bigserial"));
        assertTrue(ColumnDefStmtUtil.isValidSerialType("serial8"));
        assertFalse(ColumnDefStmtUtil.isValidSerialType("integer"));
        assertFalse(ColumnDefStmtUtil.isValidSerialType("varchar"));
    }
}
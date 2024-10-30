// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.assessment.testutils.TestPostgresSqlParserHelper;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateSeqStmtManagerTest {
    private static ConfigLoader configLoader;


    @BeforeAll
    public static void setup() {
        configLoader = mock(ConfigLoader.class);
    }

    @Test
    public void test_createTempSequence()
    {
        CreateSeqStmtManager manager = new CreateSeqStmtManager(configLoader);
        PostgreSQLParser.CreateseqstmtContext ctx = mock(
            PostgreSQLParser.CreateseqstmtContext.class
        );
        Feature feature = new Feature("create_temp_sequence", false, "CREATE TEMP SEQUENCE is not supported");
        when(ctx.opttemp()).thenReturn(mock(PostgreSQLParser.OpttempContext.class));
        when(ctx.opttemp().TEMPORARY()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("create_temp_sequence")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createSequenceCycle()
    {
        CreateSeqStmtManager manager = new CreateSeqStmtManager(configLoader);
        PostgreSQLParser.CreateseqstmtContext ctx = mock(
            PostgreSQLParser.CreateseqstmtContext.class
        );
        Feature feature = new Feature("create_sequence_cycle", false, "CREATE SEQUENCE CYCLE OR NO CYCLE is not supported");
        when(ctx.optseqoptlist()).thenReturn(mock(PostgreSQLParser.OptseqoptlistContext.class));
        when(ctx.optseqoptlist().seqoptlist()).thenReturn(mock(PostgreSQLParser.SeqoptlistContext.class));

        PostgreSQLParser.SeqoptelemContext seqOptElemCtx = mock(PostgreSQLParser.SeqoptelemContext.class);
        List<PostgreSQLParser.SeqoptelemContext> seqOptElemList = Collections.singletonList(seqOptElemCtx);
        when(ctx.optseqoptlist().seqoptlist().seqoptelem()).thenReturn(seqOptElemList);
        when(seqOptElemCtx.CYCLE()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("create_sequence_cycle")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
        assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }


    @Test
    public void test_createSequence()
    {
        CreateSeqStmtManager manager = new CreateSeqStmtManager(configLoader);
        HashMap<String, Feature> testSqls = new HashMap<>();

        testSqls.put("CREATE SEQUENCE public.abc START WITH 5 INCREMENT BY 2 MINVALUE 1 NO MAXVALUE CACHE 1;", new Feature("create_sequence",true, null));
        testSqls.put("CREATE SEQUENCE public.abcdef234 START WITH -3 INCREMENT BY 1 MINVALUE -3 MAXVALUE -1 CACHE 1;", new Feature("create_sequence", false,
            "Not enough values to create distributed sequence. Please specify sequence size to be greater than 2 * chunk_size * sequence_increment * num_routers.chunk_size=0,sequence_increment=1,num_routers=2"));
        testSqls.put("CREATE SEQUENCE public.custom_log_id_seq AS integer START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;", new Feature("create_sequence", true, null));
        testSqls.put("CREATE SEQUENCE public.decreasing_seq START WITH 100 INCREMENT BY -1 MINVALUE 1 MAXVALUE 100 CACHE 1;", new Feature("create_sequence", true, null));

        testSqls.put("CREATE SEQUENCE public.min_max START WITH -10 INCREMENT BY -1 MINVALUE -30 NO MAXVALUE CACHE 1;"
            , new Feature("create_sequence",true, null));
        testSqls.put("CREATE SEQUENCE public.min_max START WITH -10 INCREMENT BY -1 MINVALUE -30 NO MAXVALUE CACHE 2;"
            , new Feature("create_sequence", true, null));
        testSqls.put("CREATE SEQUENCE public.default_neg START WITH -10 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;"
            , new Feature("create_sequence", true, null));
        testSqls.put("CREATE SEQUENCE public.default_neg AS smallint START WITH -1 INCREMENT BY -1  NO MINVALUE NO MAXVALUE CACHE 1;"
            , new Feature("create_sequence", true, null));


        testSqls.put("CREATE SEQUENCE public.sequence_test9 AS integer START WITH -1 INCREMENT BY -100000 NO MINVALUE NO MAXVALUE CACHE 1000;"
            , new Feature("create_sequence", false, "MINVALUE (-25000000000) is out of range for sequence data type integer"));
        testSqls.put("CREATE SEQUENCE sequence_test9 AS smallint start with 1 INCREMENT BY 100000 NO MINVALUE NO MAXVALUE cache 1000;"
            , new Feature("create_sequence", false, "MAXVALUE (409500000) is out of range for sequence data type smallint"));
        testSqls.put("CREATE SEQUENCE sequence_test10 AS integer start with 1 INCREMENT BY 100000 NO MINVALUE NO MAXVALUE cache 1000;"
            , new Feature("create_sequence",false, "MAXVALUE (25000000000) is out of range for sequence data type integer"));
        testSqls.put("create sequence inc_1 start with 1 increment by 1 NO MINVALUE MAXVALUE 40 cache 50;"
            , new Feature("create_sequence",false, "CACHE parameter must be less than chunk size: 5"));
        testSqls.put("create sequence inc_1 start with 1 increment by 1 no minvalue maxvalue 3 cache 1;"
            , new Feature("create_sequence",false, "Not enough values to create distributed sequence. Please specify sequence size to be greater than 2 * chunk_size * sequence_increment * num_routers.chunk_size=0,sequence_increment=1,num_routers=2"));
        testSqls.put("create sequence inc_1 start with 1 increment by 1 no minvalue maxvalue 30 cache 4;"
            , new Feature("create_sequence",false, "CACHE parameter must be less than chunk size: 3"));
        testSqls.put("create sequence inc_1 start with 1 increment by 1 no minvalue maxvalue 30 cache 3;"
            , new Feature("create_sequence",true, null));
        testSqls.put("CREATE SEQUENCE public.abc START WITH 5 INCREMENT BY 0 MINVALUE 1 NO MAXVALUE CACHE 1;", new Feature("create_sequence",false, "INCREMENT must not be zero"));


        Feature feature = new Feature("create_sequence", true, null);
        when(configLoader.getFeatureConfig("create_sequence")).thenReturn(feature);

        for (Map.Entry<String, Feature> entry : testSqls.entrySet())
        {
            PostgreSQLParser.RootContext context = TestPostgresSqlParserHelper.parseSQL(entry.getKey());
            PostgreSQLParser.CreateseqstmtContext createseqstmtContext =
                context.stmtblock().stmtmulti().stmt(0).createseqstmt();
            List<StatementResult> resultList = manager.analyzeStatement(createseqstmtContext);
            assertEquals(1, resultList.size());
            StatementResult result = resultList.get(0);
            assertEquals(entry.getValue().isSupported(), result.getFeature().isSupported());
            if (!result.getFeature().isSupported()) {
                assertEquals(entry.getValue().getErrorMessage(), result.getFeature().getErrorMessage());
            }
        }
    }
}
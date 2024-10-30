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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefineStmtManagerTest {
    private static ConfigLoader configLoader;


    @BeforeAll
    public static void setup() {
        configLoader = mock(ConfigLoader.class);
    }

    @Test
    public void test_createAggregate()
    {
        DefineStmtManager manager = new DefineStmtManager(configLoader);
        PostgreSQLParser.DefinestmtContext ctx = mock(
            PostgreSQLParser.DefinestmtContext.class
        );
        Feature feature = new Feature("create_aggregate", false, "CREATE AGGREGATE is not supported");
        when(ctx.AGGREGATE()).thenReturn(new TerminalNodeImpl(any()));
        when(configLoader.getFeatureConfig("create_aggregate")).thenReturn(feature);
        List<StatementResult> resultList = manager.analyzeStatement(ctx);
        assertEquals(1, resultList.size());
        StatementResult result = resultList.get(0);
        assertFalse(result.getFeature().isSupported());
	    assertEquals(feature.getErrorMessage(), result.getFeature().getErrorMessage());
    }

    @Test
    public void test_createCollation()
    {
        DefineStmtManager manager = new DefineStmtManager(configLoader);
        HashMap<String, Feature> testSqls = new HashMap<>();

        // From
        testSqls.put("CREATE COLLATION ix.x FROM y.y;", new Feature("create_collation",true, null));
        testSqls.put("CREATE COLLATION public.x FROM \"default\";", new Feature("create_collation", false,
            "collation \"default\" cannot be copied"));
        testSqls.put("CREATE COLLATION public.y FROM abc;", new Feature("create_collation", true, null));
        testSqls.put("CREATE COLLATION x from y;", new Feature("create_collation", true, null));

        // Success
        testSqls.put("CREATE COLLATION public.french_non_deterministic (provider = icu, deterministic = false, locale = 'fr_FR');"
            , new Feature("create_collation",true, null));
        testSqls.put("CREATE COLLATION public.icu_spanish (provider = icu, locale = 'es-ES-u-co-trad');"
            , new Feature("create_collation", true, null));
        testSqls.put("CREATE COLLATION custom (provider = icu, locale = 'und', rules = '&V << w <<< W');"
            , new Feature("create_collation", true, null));
        testSqls.put("CREATE COLLATION public.french_non_deterministic (PROVIDER = ICU, DETERMINISTIC = false, LOCALE = 'fr_FR');"
            , new Feature("create_collation", true, null));
        testSqls.put("CREATE COLLATION mycoll2 ( LC_COLLATE = \"POSIX\", LC_CTYPE = \"POSIX\" );"
            , new Feature("create_collation", true, null));
        testSqls.put("CREATE COLLATION german (provider = libc, locale = 'de_DE');"
            , new Feature("create_collation", true, null));

        // Fail
        testSqls.put("CREATE COLLATION public.coll_dup_chk (VERSION = '1', VERSION = \"NONSENSE\", LOCALE = '');"
            , new Feature("create_collation",false, "conflicting or redundant options"));
        testSqls.put("CREATE COLLATION public.coll_dup_chk (LC_CTYPE = \"POSIX\", LC_CTYPE = \"NONSENSE\", LC_COLLATE = \"POSIX\");"
            , new Feature("create_collation",false, "conflicting or redundant options"));
        testSqls.put("CREATE COLLATION public.coll_dup_chk (LC_COLLATE = \"POSIX\", LC_COLLATE = \"NONSENSE\", LC_CTYPE = \"POSIX\");"
            , new Feature("create_collation",false, "conflicting or redundant options"));
        testSqls.put("CREATE COLLATION public.coll_dup_chk (PROVIDER = icu, PROVIDER = NONSENSE, LC_COLLATE = \"POSIX\", LC_CTYPE = \"POSIX\");"
            , new Feature("create_collation",false, "conflicting or redundant options"));
        testSqls.put("CREATE COLLATION public.case_sensitive (LOCALE = '', LOCALE = \"NONSENSE\");"
            , new Feature("create_collation",false, "conflicting or redundant options"));
        testSqls.put("CREATE COLLATION public.case_coll (\"Lc_Collate\" = \"POSIX\", \"Lc_Ctype\" = \"POSIX\");"
            , new Feature("create_collation",false, "collation attribute \"Lc_Collate\" not recognized"));
        testSqls.put("CREATE COLLATION public.coll_dup_chk (DETERMINISTIC = TRUE, DETERMINISTIC = NONSENSE, LOCALE = '');"
            , new Feature("create_collation",false, "conflicting or redundant options"));
        testSqls.put("CREATE COLLATION public.coll_dup_chk (LC_COLLATE = \"POSIX\", LC_CTYPE = \"POSIX\", LOCALE = '');"
            , new Feature("create_collation",false,
                "LOCALE cannot be specified together with LC_COLLATE or LC_CTYPE."));
        testSqls.put("CREATE COLLATION public.coll_dup_chk (LC_COLLATE = \"POSIX\", LOCALE = '');"
            , new Feature("create_collation",false, "LOCALE cannot be specified together with LC_COLLATE or LC_CTYPE."));
        testSqls.put("CREATE COLLATION public.coll_dup_chk (FROM = \"C\", VERSION = \"1\");"
            , new Feature("create_collation",false, "conflicting or redundant options"));
        testSqls.put("CREATE COLLATION x.y (PROVIDER = libc,  rules = '&V << w <<< W');"
            , new Feature("create_collation", false,
                "parameter \"lc_collate\" must be specified"));
        testSqls.put("CREATE COLLATION x.y (DETERMINISTIC=false, PROVIDER = libc);"
            , new Feature("create_collation", false,
                "parameter \"lc_collate\" must be specified"));
        testSqls.put("CREATE COLLATION x.y (DETERMINISTIC=false, PROVIDER = libc, LC_COLLATE=\"POSIX\", LC_CTYPE=\"POSIX\");"
            , new Feature("create_collation", false,
                "nondeterministic collations not supported with this provider"));

        Feature feature = new Feature("create_collation", true, null);
        when(configLoader.getFeatureConfig("create_collation")).thenReturn(feature);

        for (Map.Entry<String, Feature> entry : testSqls.entrySet())
        {
            PostgreSQLParser.RootContext context = TestPostgresSqlParserHelper.parseSQL(entry.getKey());
            PostgreSQLParser.DefinestmtContext definestmtContext =
                context.stmtblock().stmtmulti().stmt(0).definestmt();
            List<StatementResult> resultList = manager.analyzeStatement(definestmtContext);
            assertEquals(1, resultList.size());
            StatementResult result = resultList.get(0);
            assertEquals(entry.getValue().isSupported(), result.getFeature().isSupported());
            if (!result.getFeature().isSupported()) {
                assertEquals(entry.getValue().getErrorMessage(), result.getFeature().getErrorMessage());
            }
        }
    }

    @Test
    public void test_createOperator()
    {
        DefineStmtManager manager = new DefineStmtManager(configLoader);
        HashMap<String, Feature> testSqls = new HashMap<>();

        // Success
        testSqls.put("CREATE OPERATOR !! (leftarg = point, rightarg = point, procedure = point_eq, commutator = =);"
            , new Feature("create_operator",true, null));
        testSqls.put("CREATE OPERATOR !! (leftarg = point, rightarg = point, function = point_eq, commutator = =);"
            , new Feature("create_operator",true, null));
        testSqls.put("CREATE OPERATOR !! (leftarg = point, rightarg = point, procedure = point_eq, commutator = =, negator = !=, restrict = restr);"
            , new Feature("create_operator",true, null));
        testSqls.put("CREATE OPERATOR !! (leftarg = point, rightarg = point, procedure = point_eq, commutator = =, join = join_fn, hashes, merges);"
            , new Feature("create_operator",true, null));

        // Fail
        testSqls.put("CREATE OPERATOR !! (leftarg = point, rightarg = point, \"errorAttribute\" = point_eq, commutator = =);"
            , new Feature("create_operator",false, "operator attribute \"errorAttribute\" not recognized"));

        Feature feature = new Feature("create_operator", true, null);
        when(configLoader.getFeatureConfig("create_operator")).thenReturn(feature);

        for (Map.Entry<String, Feature> entry : testSqls.entrySet())
        {
            PostgreSQLParser.RootContext context = TestPostgresSqlParserHelper.parseSQL(entry.getKey());
            PostgreSQLParser.DefinestmtContext definestmtContext =
                context.stmtblock().stmtmulti().stmt(0).definestmt();
            List<StatementResult> resultList = manager.analyzeStatement(definestmtContext);
            assertEquals(1, resultList.size());
            StatementResult result = resultList.get(0);
            assertEquals(entry.getValue().isSupported(), result.getFeature().isSupported());
            if (!result.getFeature().isSupported()) {
                assertEquals(entry.getValue().getErrorMessage(), result.getFeature().getErrorMessage());
            }
        }
    }

    @Test
    public void test_createType()
    {
        DefineStmtManager manager = new DefineStmtManager(configLoader);
        HashMap<String, Feature> testSqls = new HashMap<>();
 
        Feature feature1 = new Feature("create_type", true, "");
        Feature feature2 = new Feature("create_type_enum", true, "");
        Feature feature3 = new Feature("create_type_range", false, "CREATE TYPE AS RANGE is not supported");
        Feature feature4 = new Feature("create_base_type", false, "ERROR: must be superuser to create a base type");
 
        testSqls.put("CREATE TYPE box (INTERNALLENGTH = 16, INPUT = my_box_in_function, OUTPUT = my_box_out_function);", feature4);
 
        testSqls.put("CREATE TYPE xyz;", feature4);
 
        testSqls.put("CREATE TYPE day_of_week AS ENUM ('Monday', 'Tuesday');", feature2);
 
        testSqls.put("CREATE TYPE float_range AS RANGE (SUBTYPE = FLOAT8, SUBTYPE_OPCLASS = float_ops);", feature3);
 
        testSqls.put("CREATE TYPE employee_type AS (name TEXT, age INTEGER, salary NUMERIC, department TEXT);", ConfigLoader.DEFAULT_SUPPORTED_FEATURE);
 
        when(configLoader.getFeatureConfig("create_type")).thenReturn(feature1);
        when(configLoader.getFeatureConfig("create_type_enum")).thenReturn(feature2);
        when(configLoader.getFeatureConfig("create_type_range")).thenReturn(feature3);
        when(configLoader.getFeatureConfig("create_base_type")).thenReturn(feature4);
 
        for (Map.Entry<String, Feature> entry : testSqls.entrySet())
        {
            PostgreSQLParser.RootContext context = TestPostgresSqlParserHelper.parseSQL(entry.getKey());
            PostgreSQLParser.DefinestmtContext definestmtContext =
                context.stmtblock().stmtmulti().stmt(0).definestmt();
            List<StatementResult> resultList = manager.analyzeStatement(definestmtContext);
            assertEquals(1, resultList.size());
            StatementResult result = resultList.get(0);
            assertNotNull(result.getFeature());
            assertEquals(entry.getValue().isSupported(), result.getFeature().isSupported());
            if (!result.getFeature().isSupported()) {
                assertEquals(entry.getValue().getErrorMessage(), result.getFeature().getErrorMessage());
            }
        }
    }
}

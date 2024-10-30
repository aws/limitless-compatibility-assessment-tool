// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.statementanalyzer;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.assessment.sql.AlterOwnerStmtManager;
import com.amazon.limitless.assessment.sql.CreateFunctionStmtManager;
import com.amazon.limitless.assessment.sql.CreateSchemaStmtManager;
import com.amazon.limitless.assessment.sql.CreateExtensionStmtManager;
import com.amazon.limitless.assessment.sql.AlterExtensionStmtManager;
import com.amazon.limitless.assessment.sql.AlterExtensionContentsStmtManager;
import com.amazon.limitless.assessment.sql.AlterObjectSchemaStmtManager;
import com.amazon.limitless.assessment.sql.CreateAmStmtManager;
import com.amazon.limitless.assessment.sql.DefineStmtManager;
import com.amazon.limitless.assessment.sql.CreateCastStmtManager;
import com.amazon.limitless.assessment.sql.CreateDomainStmtManager;
import com.amazon.limitless.assessment.sql.CreateForeignTableStmtManager;
import com.amazon.limitless.assessment.sql.CreateMatViewStmtManager;
import com.amazon.limitless.assessment.sql.GrantStmtManager;
import com.amazon.limitless.assessment.sql.IndexStmtManager;
import com.amazon.limitless.assessment.sql.RenameStmtManager;
import com.amazon.limitless.assessment.sql.RuleStmtManager;
import com.amazon.limitless.assessment.sql.CreateStatsStmtManager;
import com.amazon.limitless.assessment.sql.CreateSubscriptionStmtManager;
import com.amazon.limitless.assessment.sql.CreateTrigStmtManager;
import com.amazon.limitless.assessment.sql.DefaultUnsupportedStmtManager;
import com.amazon.limitless.assessment.sql.AlterObjectDependsStmtManager;
import com.amazon.limitless.assessment.sql.AlterTableStmtManager;
import com.amazon.limitless.assessment.sql.AlterSubscriptionStmtManager;
import com.amazon.limitless.assessment.sql.ViewStmtManager;
import com.amazon.limitless.assessment.sql.CreateSeqStmtManager;
import com.amazon.limitless.assessment.sql.AlterSeqStmtManager;
import com.amazon.limitless.assessment.sql.CreateOpClassStmtManager;
import com.amazon.limitless.assessment.sql.CreateOpFamilyStmtManager;
import com.amazon.limitless.assessment.sql.AlterCompositeTypeStmtManager;
import com.amazon.limitless.assessment.sql.AlterEnumStmtManager;

import com.amazon.limitless.assessment.sql.CreateTableStmtManager;
import com.amazon.limitless.assessment.sql.CreatePolicyStmtManager;
import com.amazon.limitless.assessment.sql.AlterPolicyStmtManager;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class StatementAnalyzer {
    private CreateSchemaStmtManager createSchemaStmtManager;
    private AlterOwnerStmtManager alterOwnerStmtManager;
    private CreateExtensionStmtManager createExtensionStmtManager;
    private AlterExtensionStmtManager alterExtensionStmtManager;
    private AlterExtensionContentsStmtManager alterExtensionContentsStmtManager;
    private AlterObjectSchemaStmtManager alterObjectSchemaStmtManager;
    private CreateAmStmtManager createAmStmtManager;
    private DefineStmtManager defineStmtManager;
    private CreateCastStmtManager createCastStmtManager;
    private CreateDomainStmtManager createDomainStmtManager;
    private CreateForeignTableStmtManager createForeignTableStmtManager;
    private CreateMatViewStmtManager createMatViewStmtManager;
    private RuleStmtManager ruleStmtManager;
    private CreateStatsStmtManager createStatsStmtManager;
    private CreateSubscriptionStmtManager createSubscriptionStmtManager;
    private CreateTrigStmtManager createTrigStmtManager;
    private DefaultUnsupportedStmtManager defaultUnsupportedStmtManager;
    private AlterObjectDependsStmtManager alterObjectDependsStmtManager;
    private AlterTableStmtManager alterTableStmtManager;
    private AlterSubscriptionStmtManager alterSubscriptionStmtManager;
    private IndexStmtManager indexStmtManager;
    private RenameStmtManager renameStmtManager;
    private CreateTableStmtManager createTableStmtManager;
    private ViewStmtManager viewStmtManager;
    private CreateSeqStmtManager createSeqStmtManager;
    private AlterSeqStmtManager alterSeqStmtManager;
    private CreateFunctionStmtManager createFunctionStmtManager;
    private CreatePolicyStmtManager createPolicyStmtManager;
    private AlterPolicyStmtManager alterPolicyStmtManager;
    private CreateOpClassStmtManager createOpClassStmtManager;
    private CreateOpFamilyStmtManager createOpFamilyStmtManager;
    private GrantStmtManager grantStmtManager;
    private AlterCompositeTypeStmtManager alterCompositeTypeStmtManager;
    private AlterEnumStmtManager alterEnumStmtManager;

    public StatementAnalyzer(ConfigLoader configLoader)
    {
        createSchemaStmtManager = new CreateSchemaStmtManager(configLoader);
        alterOwnerStmtManager = new AlterOwnerStmtManager(configLoader);
        createExtensionStmtManager = new CreateExtensionStmtManager(configLoader);
        alterExtensionStmtManager = new AlterExtensionStmtManager(configLoader);
        alterExtensionContentsStmtManager = new AlterExtensionContentsStmtManager(configLoader);
        alterObjectSchemaStmtManager = new AlterObjectSchemaStmtManager(configLoader);
        createAmStmtManager = new CreateAmStmtManager(configLoader);
        defineStmtManager = new DefineStmtManager(configLoader);
        createCastStmtManager = new CreateCastStmtManager(configLoader);
        createDomainStmtManager = new CreateDomainStmtManager(configLoader);
        createForeignTableStmtManager = new CreateForeignTableStmtManager(configLoader);
        createMatViewStmtManager = new CreateMatViewStmtManager(configLoader);
        ruleStmtManager = new RuleStmtManager(configLoader);
        createStatsStmtManager = new CreateStatsStmtManager(configLoader);
        createSubscriptionStmtManager = new CreateSubscriptionStmtManager(configLoader);
        createTrigStmtManager = new CreateTrigStmtManager(configLoader);
        defaultUnsupportedStmtManager = new DefaultUnsupportedStmtManager(configLoader);
        alterObjectDependsStmtManager = new AlterObjectDependsStmtManager(configLoader);
        alterTableStmtManager = new AlterTableStmtManager(configLoader);
        alterSubscriptionStmtManager = new AlterSubscriptionStmtManager(configLoader);
        indexStmtManager = new IndexStmtManager(configLoader);
        renameStmtManager = new RenameStmtManager(configLoader);
        createTableStmtManager = new CreateTableStmtManager(configLoader);
        viewStmtManager = new ViewStmtManager(configLoader);
        createSeqStmtManager = new CreateSeqStmtManager(configLoader);
        alterSeqStmtManager = new AlterSeqStmtManager(configLoader);
        createFunctionStmtManager = new CreateFunctionStmtManager(configLoader);
        createPolicyStmtManager = new CreatePolicyStmtManager(configLoader);
        alterPolicyStmtManager = new AlterPolicyStmtManager(configLoader);
        createOpClassStmtManager = new CreateOpClassStmtManager(configLoader);
        createOpFamilyStmtManager = new CreateOpFamilyStmtManager(configLoader);
        grantStmtManager = new GrantStmtManager(configLoader);
        alterCompositeTypeStmtManager = new AlterCompositeTypeStmtManager(configLoader);
        alterEnumStmtManager = new AlterEnumStmtManager(configLoader);
    }

    public List<StatementResult> parse(ParserRuleContext ctx) {
        if (ctx instanceof PostgreSQLParser.CreateschemastmtContext)
        {
            return createSchemaStmtManager.analyzeStatement((PostgreSQLParser.CreateschemastmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.AlterownerstmtContext)
        {
            return alterOwnerStmtManager.analyzeStatement((PostgreSQLParser.AlterownerstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreateextensionstmtContext)
        {
            return createExtensionStmtManager.analyzeStatement((PostgreSQLParser.CreateextensionstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.AlterextensionstmtContext)
        {
            return alterExtensionStmtManager.analyzeStatement((PostgreSQLParser.AlterextensionstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.AlterextensioncontentsstmtContext)
        {
            return alterExtensionContentsStmtManager.analyzeStatement((PostgreSQLParser.AlterextensioncontentsstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.AlterobjectschemastmtContext)
        {
            return alterObjectSchemaStmtManager.analyzeStatement((PostgreSQLParser.AlterobjectschemastmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreateamstmtContext)
        {
            return createAmStmtManager.analyzeStatement((PostgreSQLParser.CreateamstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.DefinestmtContext)
        {
            return defineStmtManager.analyzeStatement((PostgreSQLParser.DefinestmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreatecaststmtContext)
        {
            return createCastStmtManager.analyzeStatement((PostgreSQLParser.CreatecaststmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreatedomainstmtContext)
        {
            return createDomainStmtManager.analyzeStatement((PostgreSQLParser.CreatedomainstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreateforeigntablestmtContext)
        {
            return createForeignTableStmtManager.analyzeStatement((PostgreSQLParser.CreateforeigntablestmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreatematviewstmtContext)
        {
            return createMatViewStmtManager.analyzeStatement((PostgreSQLParser.CreatematviewstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.RulestmtContext)
        {
            return ruleStmtManager.analyzeStatement((PostgreSQLParser.RulestmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreatestatsstmtContext)
        {
            return createStatsStmtManager.analyzeStatement((PostgreSQLParser.CreatestatsstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreatesubscriptionstmtContext)
        {
            return createSubscriptionStmtManager.analyzeStatement((PostgreSQLParser.CreatesubscriptionstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreatetrigstmtContext)
        {
            return createTrigStmtManager.analyzeStatement((PostgreSQLParser.CreatetrigstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.AltersubscriptionstmtContext)
        {
            return alterSubscriptionStmtManager.analyzeStatement((PostgreSQLParser.AltersubscriptionstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.AlterobjectdependsstmtContext)
        {
            return alterObjectDependsStmtManager.analyzeStatement((PostgreSQLParser.AlterobjectdependsstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.AltertablestmtContext)
        {
            return alterTableStmtManager.analyzeStatement((PostgreSQLParser.AltertablestmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.RenamestmtContext)
        {
            return renameStmtManager.analyzeStatement((PostgreSQLParser.RenamestmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.IndexstmtContext)
        {
            return indexStmtManager.analyzeStatement((PostgreSQLParser.IndexstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreatestmtContext)
        {
            return createTableStmtManager.analyzeStatement((PostgreSQLParser.CreatestmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.ViewstmtContext)
        {
            return viewStmtManager.analyzeStatement((PostgreSQLParser.ViewstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreateseqstmtContext)
        {
            return createSeqStmtManager.analyzeStatement((PostgreSQLParser.CreateseqstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.AlterseqstmtContext)
        {
            return alterSeqStmtManager.analyzeStatement((PostgreSQLParser.AlterseqstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreatefunctionstmtContext)
        {
            return createFunctionStmtManager.analyzeStatement((PostgreSQLParser.CreatefunctionstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreatepolicystmtContext)
        {
            return createPolicyStmtManager.analyzeStatement((PostgreSQLParser.CreatepolicystmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.AlterpolicystmtContext)
        {
            return alterPolicyStmtManager.analyzeStatement((PostgreSQLParser.AlterpolicystmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreateopclassstmtContext)
        {
            return createOpClassStmtManager.analyzeStatement((PostgreSQLParser.CreateopclassstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.CreateopfamilystmtContext)
        {
            return createOpFamilyStmtManager.analyzeStatement((PostgreSQLParser.CreateopfamilystmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.GrantstmtContext)
        {
            return grantStmtManager.analyzeStatement((PostgreSQLParser.GrantstmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.AltercompositetypestmtContext)
        {
            return alterCompositeTypeStmtManager.analyzeStatement((PostgreSQLParser.AltercompositetypestmtContext) ctx);
        }
        else if (ctx instanceof PostgreSQLParser.AlterenumstmtContext)
        {
            return alterEnumStmtManager.analyzeStatement((PostgreSQLParser.AlterenumstmtContext) ctx);
        }
        // TODO(tanyagp@): Handle the error message differently for unexpected SQLs (DMLs, DROP DDLs, global DDLs) later.
        return defaultUnsupportedStmtManager.analyzeStatement();
    }
}
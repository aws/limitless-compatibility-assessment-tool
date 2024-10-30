// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.listener;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.reportgenerator.AssessmentSummary;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.assessment.statementanalyzer.StatementAnalyzer;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import com.amazon.limitless.parser.postgresql.PostgreSQLParserBaseListener;

import java.util.List;

public class LimitlessAssessmentToolListener extends PostgreSQLParserBaseListener {

    private static int currCount = 0;
    private static final StatementAnalyzer statementAnalyzer = new StatementAnalyzer(ConfigLoader.getInstance());
    private static final AssessmentSummary assessmentSummary = AssessmentSummary.getInstance();

    /**
     * Exit a parse tree produced by {@link PostgreSQLParser#stmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void exitStmt(PostgreSQLParser.StmtContext ctx) {
        currCount++;
    }


    @Override
    public void enterCreateschemastmt(PostgreSQLParser.CreateschemastmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#altertablestmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAltertablestmt(PostgreSQLParser.AltertablestmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createstatsstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreatestatsstmt(PostgreSQLParser.CreatestatsstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#creatematviewstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreatematviewstmt(PostgreSQLParser.CreatematviewstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createpolicystmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreatepolicystmt(PostgreSQLParser.CreatepolicystmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createpolicystmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterpolicystmt(PostgreSQLParser.AlterpolicystmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createseqstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreateseqstmt(PostgreSQLParser.CreateseqstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);

    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#alterseqstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterseqstmt(PostgreSQLParser.AlterseqstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createplangstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreateplangstmt(PostgreSQLParser.CreateplangstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createextensionstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreateextensionstmt(PostgreSQLParser.CreateextensionstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#alterextensionstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterextensionstmt(PostgreSQLParser.AlterextensionstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#alterextensioncontentsstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterextensioncontentsstmt(PostgreSQLParser.AlterextensioncontentsstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createfdwstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreatefdwstmt(PostgreSQLParser.CreatefdwstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#alterfdwstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterfdwstmt(PostgreSQLParser.AlterfdwstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createforeignserverstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreateforeignserverstmt(PostgreSQLParser.CreateforeignserverstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#alterforeignserverstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterforeignserverstmt(PostgreSQLParser.AlterforeignserverstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createforeigntablestmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreateforeigntablestmt(PostgreSQLParser.CreateforeigntablestmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createamstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreateamstmt(PostgreSQLParser.CreateamstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createtrigstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreatetrigstmt(PostgreSQLParser.CreatetrigstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createeventtrigstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreateeventtrigstmt(PostgreSQLParser.CreateeventtrigstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#altereventtrigstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAltereventtrigstmt(PostgreSQLParser.AltereventtrigstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#definestmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterDefinestmt(PostgreSQLParser.DefinestmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#commentstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCommentstmt(PostgreSQLParser.CommentstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#alterdefaultprivilegesstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterdefaultprivilegesstmt(PostgreSQLParser.AlterdefaultprivilegesstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createcaststmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreatecaststmt(PostgreSQLParser.CreatecaststmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createtransformstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreatetransformstmt(PostgreSQLParser.CreatetransformstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#alterobjectdependsstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterobjectdependsstmt(PostgreSQLParser.AlterobjectdependsstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#alterobjectschemastmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterobjectschemastmt(PostgreSQLParser.AlterobjectschemastmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#alterownerstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterownerstmt(PostgreSQLParser.AlterownerstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createpublicationstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreatepublicationstmt(PostgreSQLParser.CreatepublicationstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#alterpublicationstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterpublicationstmt(PostgreSQLParser.AlterpublicationstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createsubscriptionstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreatesubscriptionstmt(PostgreSQLParser.CreatesubscriptionstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#altersubscriptionstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAltersubscriptionstmt(PostgreSQLParser.AltersubscriptionstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#rulestmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterRulestmt(PostgreSQLParser.RulestmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createdomainstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreatedomainstmt(PostgreSQLParser.CreatedomainstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#altertsdictionarystmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAltertsdictionarystmt(PostgreSQLParser.AltertsdictionarystmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#altertsconfigurationstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAltertsconfigurationstmt(PostgreSQLParser.AltertsconfigurationstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createconversionstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreateconversionstmt(PostgreSQLParser.CreateconversionstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    @Override
    public void enterCreatefunctionstmt(PostgreSQLParser.CreatefunctionstmtContext ctx) {
        List<StatementResult> results = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(results, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createopclassstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreateopclassstmt(PostgreSQLParser.CreateopclassstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createopfamilystmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreateopfamilystmt(PostgreSQLParser.CreateopfamilystmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#grantstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterGrantstmt(PostgreSQLParser.GrantstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#createstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterCreatestmt(PostgreSQLParser.CreatestmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
	}

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#altercompositetypestmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAltercompositetypestmt(PostgreSQLParser.AltercompositetypestmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }

    /**
     * Enter a parse tree produced by {@link PostgreSQLParser#alterenumstmt}.
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterAlterenumstmt(PostgreSQLParser.AlterenumstmtContext ctx) {
        List<StatementResult> result = statementAnalyzer.parse(ctx);
        assessmentSummary.processResult(result, currCount);
    }
}

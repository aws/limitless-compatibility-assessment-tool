// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.assessment.sql.PostgreSqlStmtBaseManager;
import com.amazon.limitless.assessment.sql.SqlStatementManager;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import com.amazon.limitless.assessment.common.DependencyObject;
import com.amazon.limitless.assessment.common.ObjectName;

import java.util.ArrayList;
import java.util.List;

public class AlterPolicyStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.AlterpolicystmtContext> {

    public AlterPolicyStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.AlterpolicystmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        Boolean isTableCreated = false;
        String fullObjectName = "";

        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;

        if (ctx.POLICY() != null)
            featureConfig = configLoader.getFeatureConfig("alter_policy");

        if (ctx.qualified_name() != null)
        {
            ObjectName objectName = getObjectName(ctx);
            fullObjectName = DependencyObject.generateFullObjectName(objectName.getNamespace(), objectName.getObjectName());
            if (fullObjectName != null)
            {
                isTableCreated = DependencyObject.getObject(fullObjectName, "table");
            }
    
            if (!isTableCreated)
            {
                String errorMsg = String.format("relation %s does not exist", fullObjectName);
                featureConfig = new Feature("alter_policy", false, errorMsg);
                result.setFeature(featureConfig);
                resultList.add(result);
            }
            else 
            {
                /**
                 * TODO: Check the map of successfully created tables and their associated policies.
                 * If the policy for the given table does not exist, throw the following error:
                 * "ERROR: failed to execute remote query with message: policy "<policyName>" for table "<tableName>" does not exist"
                 */
            }
        }

        if (ctx.rowsecurityoptionalexpr().USING() != null)
        {
            // TODO: Handle using_expression in USING clause. Fail when you find a mutable function.
        }

        if (ctx.rowsecurityoptionalwithcheck().WITH() != null)
        {
            // TODO: Handle check_expression in WITH CHECK clause. Fail when you find a mutable function.
        }

        if (resultList.isEmpty())
        {
            result.setFeature(featureConfig);
            resultList.add(result);
        }
        return resultList;
    }

    private static ObjectName getObjectName(PostgreSQLParser.AlterpolicystmtContext ctx)
    {
        String namespace;
        PostgreSQLParser.ColidContext colidCtx = ctx.qualified_name().colid();
        if (colidCtx.unreserved_keyword() != null)
            namespace = colidCtx.unreserved_keyword().getText();
        else 
            namespace = colidCtx.identifier().Identifier().getText();

        String objectName;
        if (ctx.qualified_name().indirection() != null) 
        {
            PostgreSQLParser.CollabelContext collabelCtx = ctx.qualified_name().indirection().indirection_el(0).attr_name().collabel();
            if (collabelCtx.unreserved_keyword() != null)
                objectName = collabelCtx.unreserved_keyword().getText();
            else 
                objectName = collabelCtx.identifier().getText();

            return new ObjectName(namespace, objectName);
        }
        objectName = namespace;
        return new ObjectName(objectName);
    }
}
// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreateExtensionStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.CreateextensionstmtContext> {
    
    public CreateExtensionStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.CreateextensionstmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        String extensionName = Optional.ofNullable(ctx)
            .map(context -> context.name())
            .map(nameContext -> nameContext.colid())
            .map(colidContext -> colidContext.identifier())
            .map(identifierContext -> identifierContext.Identifier())
            .map(token -> token.getText())
            .map(text -> text.trim())
            .orElse("");
        Feature featureConfig = configLoader.getFeatureConfig("create_extension");

        if (extensionName.isEmpty()) {
            featureConfig = new Feature("", false, "Could not process create extension statement");
            result.setFeature(featureConfig);
            resultList.add(result);
            return resultList;
        }

        // Check if the extension is supported or not
        boolean isSupportedExtension = configLoader.isSupportedExtension(extensionName);
        if (!isSupportedExtension)
            featureConfig = new Feature("", false, String.format("Extension %s is not supported", extensionName));

        result.setFeature(featureConfig);
        resultList.add(result);
        return resultList;
    }
}

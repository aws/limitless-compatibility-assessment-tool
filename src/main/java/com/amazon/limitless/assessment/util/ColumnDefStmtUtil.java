// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.util;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ColumnDefStmtUtil {

    private static final Set<String> VALID_SERIAL_TYPES = Set.of(
        "smallserial", "serial2", "serial", "serial4", "bigserial", "serial8"
    );

    private ColumnDefStmtUtil() {}

    public static List<StatementResult> analyzeColumnDefStatement(PostgreSQLParser.ColumnDefContext ctx, boolean isCreateTable, ConfigLoader configLoader) {
        List<StatementResult> resultList = new ArrayList<>();
        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;
        StatementResult result = new StatementResult();
        boolean isSerialCol = false;

        // Get the type name from ColumnDefContext ctx
        String typeName = "";
        if (ctx.typename() != null)
        {
            if (ctx.typename().simpletypename() != null)
                typeName = ctx.typename().simpletypename().getText();
            else if (ctx.typename().qualified_name() != null)
                typeName = ctx.typename().qualified_name().getText();
        }

        if (!typeName.isEmpty())
            isSerialCol = isValidSerialType(typeName);

        if (ctx.colquallist() != null)
        {
            List<PostgreSQLParser.ColconstraintContext> colConstraintList = ctx.colquallist().colconstraint();
            if (!colConstraintList.isEmpty())
            {
                for (PostgreSQLParser.ColconstraintContext colConstraintCtx : colConstraintList)
                {
                    result = ConstraintsStmtUtil.analyzeConstraintStatement(colConstraintCtx, isCreateTable, isSerialCol, configLoader);
                    resultList.add(result);
                }
            }
        }
        // TODO: add unsupported constraints such as SERIAL and IDENTITY

        return resultList;
    }

    public static boolean isValidSerialType(String typeName) {
        return VALID_SERIAL_TYPES.contains(typeName.toLowerCase());
    }
}
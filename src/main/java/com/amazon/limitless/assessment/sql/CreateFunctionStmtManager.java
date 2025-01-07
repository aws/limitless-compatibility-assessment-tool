// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.common.ObjectName;
import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLLexer;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateFunctionStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.CreatefunctionstmtContext>{

    public CreateFunctionStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }
    /**
     * @param ctx
     * @return
     */
    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.CreatefunctionstmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        Feature featureConfig = configLoader.getFeatureConfig("create_function");;
        // Get object name
        ObjectName objectName = getObjectName(ctx);
        boolean isStable = false; // used for read only functions
        boolean isVolatile = false;
        boolean isImmutable = false;
        boolean foundVolatility = false;
        boolean isProcedure = (ctx.PROCEDURE() != null) ? true : false;
        StringBuilder functionBody = new StringBuilder();
        boolean isSqlLanguage = true;

        // Validate procedure
        if (isProcedure && ((ctx.func_return() != null) || (ctx.table_func_column_list() != null)))
        {
            featureConfig = new Feature("create_function", false,
                "Procedure return type must be void");
            result.setFeature(featureConfig);
            resultList.add(result);
            return resultList;
        }
        // Check if return is data type
        if (!isProcedure)
        {
            if (ctx.TABLE() != null)
            {
                HashMap<String, String> colToDataTypeMap = getTableColumnList(ctx);
                // TODO(chsaikia@): Validate data type
            }
            else {
                String functionReturnType = getFunctionReturnDataType(ctx);
                //TODO(chsaikia@): Validate data type
            }
        }

        List<PostgreSQLParser.Createfunc_opt_itemContext> funcOptList = ctx.createfunc_opt_list().createfunc_opt_item();
        for (PostgreSQLParser.Createfunc_opt_itemContext item: funcOptList)
        {
            String itemName = item.getText();
            if (item.LANGUAGE() != null)
            {
                String languageLowerCase = item.nonreservedword_or_sconst().getText().toLowerCase();
                if (languageLowerCase.equals("plpgsql"))
                {
                    isSqlLanguage = false;
                }
                else if (languageLowerCase.equals("sql"))
                {
                    isSqlLanguage = true;
                }
                else
                {
                    featureConfig = new Feature("create_function", false,
                        "Invalid or untrusted language");
                    result.setFeature(featureConfig);
                    resultList.add(result);
                    return resultList;
                }
            }
            else if (item.AS() != null)
            {
                functionBody.append(item.func_as().sconst(0));
            }
            else if (item.TRANSFORM() != null)
            {
               // TODO(chsaikia@): Handle "TRANSFORM"
            }
            else if (item.WINDOW() != null)
            {
                // TODO(chsaikia@): Handle "WINDOW"
            }
            else
            {
                String functionToken = item.common_func_opt_item().getText().toLowerCase();
                switch (functionToken) {
                    case "immutable":
                        isImmutable = true;
                        foundVolatility = true;
                        break;
                    case "stable":
                        isStable = true;
                        foundVolatility = true;
                        break;
                    case "volatile":
                        isVolatile = true;
                        foundVolatility = true;
                        break;
                }
            }
        }

        if (!foundVolatility)
            isVolatile = true;

        // TODO(chsaikia@): Store a map of function name to volatility
        // TODO(chsaikia@): Parse function body
        // parseFunctionBody(functionBody.toString());
        result.setFeature(featureConfig);
        resultList.add(result);
        return resultList;
    }

    private void parseFunctionBody(String functionBody)
    {
        functionBody = functionBody.replaceAll("\\$[a-zA-Z_]*\\$", "");
        PostgreSQLLexer lexer = new PostgreSQLLexer(CharStreams.fromString(functionBody));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        PostgreSQLParser parser = new PostgreSQLParser(tokens);
        ParseTree tree = parser.root();
    }

    private HashMap<String, String> getTableColumnList(PostgreSQLParser.CreatefunctionstmtContext ctx) {
        HashMap<String, String> colToDataTypeMap = new HashMap<>();
        PostgreSQLParser.Table_func_column_listContext func_column_listContext = ctx.table_func_column_list();
        for (PostgreSQLParser.Table_func_columnContext funcColumnContext : func_column_listContext.table_func_column())
        {
            String columnName = funcColumnContext.param_name().getText();
            String dataType = funcColumnContext.func_type().getText();
            colToDataTypeMap.put(columnName, dataType);
        }
        return colToDataTypeMap;
    }

    private static String getFunctionReturnDataType(PostgreSQLParser.CreatefunctionstmtContext ctx) {
        // Get the function type to later analyze if it is a valid data type
        // If a data type is not valid, it means it could have been installed by an
        // extension. Therefore, we will provide a script that can check if data type
        // is installed by some extension.

        // If return type is TABLE, it has a list of columns where we can check the data type
        String functionReturnDataType = ctx.func_return().func_type().getText();
        return functionReturnDataType;
    }

    public ObjectName getObjectName(PostgreSQLParser.CreatefunctionstmtContext ctx)
    {
        String objectName = ctx.func_name().getText();
        if (ctx.func_name().colid() != null) {
            String namespace = ctx.func_name().colid().getText();
            return new ObjectName(objectName, namespace);
        }
        return new ObjectName(objectName);
    }
}

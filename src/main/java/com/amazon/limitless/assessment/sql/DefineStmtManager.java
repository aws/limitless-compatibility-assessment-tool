// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.sql;

import com.amazon.limitless.assessment.common.ObjectName;
import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.exceptions.ConflictingOrRedundantOption;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class DefineStmtManager extends PostgreSqlStmtBaseManager implements SqlStatementManager<PostgreSQLParser.DefinestmtContext> {

    public DefineStmtManager(ConfigLoader configLoader) {
        super(configLoader);
    }

    @Override
    public List<StatementResult> analyzeStatement(PostgreSQLParser.DefinestmtContext ctx) {
        StatementResult result = new StatementResult();
        resultList = new ArrayList<>();
        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;
        if (ctx.AGGREGATE() != null)
        {
            featureConfig = configLoader.getFeatureConfig("create_aggregate");
        }
        else if (ctx.COLLATION() != null)
        {
            featureConfig = configLoader.getFeatureConfig("create_collation");
            if (featureConfig.isSupported())
            {
                featureConfig = validate_collation(ctx);
            }
        }
        else if (ctx.OPERATOR() != null)
        {
            featureConfig = configLoader.getFeatureConfig("create_operator");
            if (featureConfig.isSupported())
            {
                featureConfig = validate_operator(ctx);
            }
        }
        else if (ctx.TYPE_P() != null)
        {
            featureConfig = validate_create_type(ctx);
        }
        result.setFeature(featureConfig);
        resultList.add(result);
        return resultList;
    }

    private static ObjectName getObjectName(PostgreSQLParser.DefinestmtContext definestmtContext)
    {
        String namespace = definestmtContext.any_name(0).colid().identifier().getText();

        String objectName;
        if (definestmtContext.any_name(0).attrs() != null) {
            objectName = definestmtContext.any_name(0).attrs().attr_name(0).collabel().identifier().getText();
            return new ObjectName(namespace, objectName);
        }
        objectName = namespace;
        return new ObjectName(objectName);
    }

    private static void extractDefinition(PostgreSQLParser.DefinitionContext definitionContext,
                                   HashMap<String, String> def_elements)
        throws ConflictingOrRedundantOption {
        PostgreSQLParser.Def_listContext def_listContext = definitionContext.def_list();
        List<PostgreSQLParser.Def_elemContext> def_elemContextList = def_listContext.def_elem();
        for (PostgreSQLParser.Def_elemContext def_elemContext : def_elemContextList)
        {
            String argName = def_elemContext.collabel().getText();
            String argValue = def_elemContext.def_arg() != null ? def_elemContext.def_arg().getText() : null;
            if (def_elements.containsKey(argName))
            {
                throw new ConflictingOrRedundantOption("conflicting or redundant options");
            }
            def_elements.put(argName, argValue);
        }
    }

    private static String getIdentifier(String input)
    {
        if (input == null || input.length() < 2) {
            return input;
        }
        if (input.startsWith("\"") && input.endsWith("\"")) {

            String modifiedInput = input.substring(1, input.length() - 1);
            if (StringUtils.isAllUpperCase(modifiedInput) || StringUtils.isAllLowerCase(modifiedInput))
            {
                return modifiedInput.toLowerCase();
            }
            return modifiedInput;
        }
        return input.toLowerCase();
    }

    private Feature validate_create_type(PostgreSQLParser.DefinestmtContext ctx) {
        Feature featureConfig = configLoader.getFeatureConfig("create_type");
        if (ctx.AS() != null)
        {
            if (ctx.ENUM_P() != null)
            {
                featureConfig = configLoader.getFeatureConfig("create_type_enum");
            }
            else if (ctx.RANGE() != null)
            {
                featureConfig = configLoader.getFeatureConfig("create_type_range");
            }
            else if (ctx.opttablefuncelementlist() != null 
                     && ctx.opttablefuncelementlist().tablefuncelementlist() != null)
            {
                featureConfig = ConfigLoader.DEFAULT_SUPPORTED_FEATURE;
            }
        }
        else
        {
            featureConfig = configLoader.getFeatureConfig("create_base_type");
        }
        return featureConfig;
    }

    private Feature validate_collation(PostgreSQLParser.DefinestmtContext definestmtContext) {
        Feature featureConfig = new Feature("create_collation", true, "");
        // Unused atm but adding for future use case or debugging purpose
        ObjectName objectName = getObjectName(definestmtContext);
        PostgreSQLParser.DefinitionContext definitionContext = definestmtContext.definition();
        StringBuilder fromStringBuilder = new StringBuilder();
        String fromStr = "";
        String localeStr = "";
        String lcCollateStr = "";
        String lcCtypeStr = "";
        String providerStr = "";
        String rulesEl = "";
        String versionEl = "";
        HashMap<String, String> defElems = new HashMap<>();
        boolean otherOptionsFound = false;
        boolean deterministic = true;

        if (definitionContext == null)
        {
            // Check from logic
            if (definestmtContext.FROM() != null)
            {
                fromStringBuilder.append(definestmtContext.any_name(1).colid().getText());
                if (definestmtContext.any_name(1).attrs() != null) {
                    fromStringBuilder.append(".");
                    fromStringBuilder.append(definestmtContext.any_name(1).attrs().attr_name(0).collabel().getText());
                }
                fromStr = fromStringBuilder.toString();
                if (fromStr.equals("\"default\""))
                {
                    featureConfig.setSupported(false);
                    featureConfig.setErrorMessage("collation \"default\" cannot be copied");
                }
            }
            return featureConfig;
        }

        try {
            extractDefinition(definitionContext, defElems);
        } catch (ConflictingOrRedundantOption e) {
            featureConfig.setSupported(false);
            featureConfig.setErrorMessage(e.getMessage());
            return featureConfig;
        }

        for (Map.Entry<String, String> defElemEntry : defElems.entrySet())
        {
            boolean invalidElementFound = false;
            String argName = getIdentifier(defElemEntry.getKey());
            switch (argName)
            {
                case "provider":
                    providerStr = getIdentifier(defElemEntry.getValue());
                    otherOptionsFound = true;
                    break;
                case "locale":
                    localeStr = defElemEntry.getValue();
                    otherOptionsFound = true;
                    break;
                case "lc_collate":
                    lcCollateStr = defElemEntry.getValue();
                    otherOptionsFound = true;
                    break;
                case "lc_ctype":
                    lcCtypeStr = defElemEntry.getValue();
                    otherOptionsFound = true;
                    break;
                case "rules":
                    rulesEl = defElemEntry.getValue();
                    break;
                case "version":
                    // Not validating version value
                    versionEl = defElemEntry.getValue();
                    otherOptionsFound = true;
                    break;
                case "deterministic":
                    String value = defElemEntry.getValue();
                    if (value.isEmpty())
                    {
                        featureConfig.setSupported(false);
                        featureConfig.setErrorMessage(String.format("collation attribute \"deterministic\" has an unexpected value: %s", value));
                        return featureConfig;
                    }
                    if (value.matches("false"))
                    {
                        deterministic = false;
                    }
                    otherOptionsFound = true;
                    break;
                case "from":
                    fromStr = defElemEntry.getValue();
                    break;
                default:
                    invalidElementFound = true;
            }

            if (invalidElementFound)
            {
                featureConfig.setSupported(false);
                featureConfig.setErrorMessage(String.format("collation attribute \"%s\" not recognized", argName));
                return featureConfig;
            }
        }

        if (!fromStr.isEmpty()) {
            if (otherOptionsFound) {
                featureConfig.setSupported(false);
                featureConfig.setErrorMessage("conflicting or redundant options");
                return featureConfig;
            }
            else if (fromStr.equalsIgnoreCase("default")) {
                featureConfig.setSupported(false);
                featureConfig.setErrorMessage("collation \"default\" cannot be copied");
                return featureConfig;
            }
        }

        if (!localeStr.isEmpty())
        {
            if (!lcCollateStr.isEmpty() || !lcCtypeStr.isEmpty()) {
                featureConfig.setSupported(false);
                featureConfig.setErrorMessage("LOCALE cannot be specified together with LC_COLLATE or LC_CTYPE.");
                return featureConfig;
            }
            else
            {
                lcCollateStr = localeStr;
                lcCtypeStr = localeStr;
            }
        }

        if (!providerStr.isEmpty() && !(providerStr.equals("icu") || providerStr.equals("libc")))
        {

            featureConfig.setSupported(false);
            featureConfig.setErrorMessage(String.format("unrecognized collation provider: %s", providerStr));
            return featureConfig;
        }

        if (providerStr.equals("libc"))
        {
            if (lcCollateStr.isEmpty())
            {
                featureConfig.setSupported(false);
                featureConfig.setErrorMessage("parameter \"lc_collate\" must be specified");
                return featureConfig;
            }
            else if (lcCtypeStr.isEmpty())
            {
                featureConfig.setSupported(false);
                featureConfig.setErrorMessage("parameter \"lc_ctype\" must be specified");
                return featureConfig;
            }
        }
        else if (providerStr.equals("icu"))
        {
            if (localeStr.isEmpty())
            {
                featureConfig.setSupported(false);
                featureConfig.setErrorMessage("parameter \"locale\" must be specified");
                return featureConfig;
            }
        }

        if (!deterministic && !providerStr.equals("icu"))
        {
            featureConfig.setSupported(false);
            featureConfig.setErrorMessage("nondeterministic collations not supported with this provider");
            return featureConfig;
        }
        else if (!rulesEl.isEmpty() && !providerStr.equals("icu")) {
            featureConfig.setSupported(false);
            featureConfig.setErrorMessage("ICU rules cannot be specified unless locale provider is ICU");
            return featureConfig;
        }

        return featureConfig;
    }

    private Feature validate_operator(PostgreSQLParser.DefinestmtContext definestmtContext) {
        Feature featureConfig = new Feature("create_operator", true, null);
        // Unused atm but adding for future use case or debugging purpose
        PostgreSQLParser.DefinitionContext definitionContext = definestmtContext.definition();
        String leftArgStr = "";
        String rightArgStr = "";
        String commutatorStr = "";
        String functionStr = "";
        String negatorStr = "";
        String restrictStr = "";
        String joinStr = "";
        boolean isHash = false;
        boolean isMerge = false;
        HashMap<String, String> defElems = new HashMap<>();
        boolean otherOptionsFound = false;

        try {
            extractDefinition(definitionContext, defElems);
        } catch (ConflictingOrRedundantOption e) {
            featureConfig.setSupported(false);
            featureConfig.setErrorMessage(e.getMessage());
            return featureConfig;
        }

        for (Map.Entry<String, String> defElemEntry : defElems.entrySet()) {
            boolean invalidElementFound = false;
            String argName = getIdentifier(defElemEntry.getKey());
            switch (argName)
            {
                case "leftarg":
                    leftArgStr = defElemEntry.getValue();
                    otherOptionsFound = true;
                    break;
                case "rightarg":
                    rightArgStr = defElemEntry.getValue();
                    otherOptionsFound = true;
                    break;
                case "commutator":
                    commutatorStr = defElemEntry.getValue();
                    otherOptionsFound = true;
                    break;
                case "procedure":
                case "function":
                    functionStr = defElemEntry.getValue();
                    otherOptionsFound = true;
                    break;
                case "negator":
                    negatorStr = defElemEntry.getValue();
                    otherOptionsFound = true;
                    break;
                case "restrict":
                    restrictStr = defElemEntry.getValue();
                    otherOptionsFound = true;
                    break;
                case "join":
                    joinStr = defElemEntry.getValue();
                    otherOptionsFound = true;
                    break;
                case "hashes":
                    isHash = true;
                    otherOptionsFound = true;
                    break;
                case "merges":
                    isMerge = true;
                    otherOptionsFound = true;
                    break;
                default:
                    invalidElementFound = true;
            }

            if (invalidElementFound)
            {
                featureConfig.setSupported(false);
                featureConfig.setErrorMessage(String.format("operator attribute \"%s\" not recognized", argName));
                return featureConfig;
            }
        }

        // TODO: Add hashset for functions and determine if operator is valid based on function type (VOLATILE etc)

        return featureConfig;
    }
}

// Copyright 2024 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: LicenseRef-.amazon.com.-AmznSL-1.0
// Licensed under the Amazon Software License  https://aws.amazon.com/asl/

package com.amazon.limitless.assessment.util;

import com.amazon.limitless.assessment.configloader.ConfigLoader;
import com.amazon.limitless.assessment.configloader.Feature;
import com.amazon.limitless.assessment.reportgenerator.StatementResult;
import com.amazon.limitless.parser.postgresql.PostgreSQLParser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;

public class ConstraintsStmtUtil {

    private ConstraintsStmtUtil() {}

    public static StatementResult analyzeConstraintStatement(ParserRuleContext ctx, boolean isCreateTable, boolean isSerialCol, ConfigLoader configLoader) {
        StatementResult result = new StatementResult();
        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;

        if (ctx instanceof PostgreSQLParser.TableconstraintContext)
        {
            PostgreSQLParser.TableconstraintContext tableConstraintCtx = (PostgreSQLParser.TableconstraintContext) ctx;
            featureConfig = analyzeTableConstraint(tableConstraintCtx, isCreateTable, configLoader);
        }
        else if (ctx instanceof PostgreSQLParser.ColconstraintContext)
        {
            PostgreSQLParser.ColconstraintContext colConstraintCtx = (PostgreSQLParser.ColconstraintContext) ctx;
            featureConfig = analyzeColumnConstraint(colConstraintCtx, isCreateTable, isSerialCol, configLoader);
        }

        result.setFeature(featureConfig);
        return result;
    }

    private static Feature analyzeTableConstraint(PostgreSQLParser.TableconstraintContext tableConstraintCtx, boolean isCreateTable, ConfigLoader configLoader) {
        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;
        String featureName = "";

        if (tableConstraintCtx.constraintelem() != null)
        {
            PostgreSQLParser.ConstraintelemContext constraintElemCtx = tableConstraintCtx.constraintelem();

            if (constraintElemCtx.CHECK() != null)
            {
                // TODO: Add check for expressions inside the CHECK constraint
                featureName = "constraint_check_standard";
            }
            else if (constraintElemCtx.UNIQUE() != null)
            {
                featureName = "constraint_unique_standard";

                if (constraintElemCtx.opt_definition() != null && constraintElemCtx.opt_definition().WITH() != null)
                {
                    featureName = "storage_parameter";
                }
            }
            else if (constraintElemCtx.PRIMARY() != null && constraintElemCtx.KEY() != null)
            {
                featureName = "constraint_primary_key_standard";
                if (constraintElemCtx.opt_definition() != null && constraintElemCtx.opt_definition().WITH() != null)
                {
                    featureName = "storage_parameter";
                }
            }
            else if (constraintElemCtx.EXCLUDE() != null)
            {
                featureName = "constraint_exclude_standard";

            }
            else if (constraintElemCtx.FOREIGN() != null)
            {
                return analyzeForeignKeyConstraint(constraintElemCtx, isCreateTable, configLoader);
            }

            if (!featureName.isEmpty())
                featureConfig = configLoader.getFeatureConfig(featureName);
        }
        return featureConfig;
    }

    private static Feature analyzeForeignKeyConstraint(PostgreSQLParser.ConstraintelemContext constraintElemCtx, boolean isCreateTable, ConfigLoader configLoader) {
        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;
        
        // TODO: Need to check if we can add validation for foreign key constraints

        return featureConfig;
    }

    private static Feature analyzeColumnConstraint(PostgreSQLParser.ColconstraintContext colConstraintCtx, boolean isCreateTable, boolean isSerialCol, ConfigLoader configLoader) {
        Feature featureConfig = ConfigLoader.DEFAULT_UNSUPPORTED_FEATURE;

        if (colConstraintCtx.colconstraintelem() != null)
        {
            PostgreSQLParser.ColconstraintelemContext colConstraintElemCtx = colConstraintCtx.colconstraintelem();
            featureConfig = analyzeColumnConstraintElement(colConstraintElemCtx, isCreateTable, isSerialCol, configLoader);
        }
        else if (colConstraintCtx.constraintattr() != null || colConstraintCtx.COLLATE() != null)
        {
            featureConfig = ConfigLoader.DEFAULT_SUPPORTED_FEATURE;
        }

        return featureConfig;
    }

    private static Feature analyzeColumnConstraintElement(PostgreSQLParser.ColconstraintelemContext colConstraintElemCtx, boolean isCreateTable, boolean isSerialCol, ConfigLoader configLoader) {
        Feature featureConfig = ConfigLoader.DEFAULT_SUPPORTED_FEATURE;
        String featureName = "";
        
        if (colConstraintElemCtx.NULL_P() != null)
        {
            if (colConstraintElemCtx.NOT() != null)
            {
                featureName = "constraint_not_null_standard";
                if (!isCreateTable && isSerialCol)
                    featureName = "alter_table_add_column_serial_not_null";
            }
            else
                featureName = "constraint_null_standard";
        }
        else if (colConstraintElemCtx.UNIQUE() != null)
        {
            featureName = "constraint_unique_standard";
            if (colConstraintElemCtx.opt_definition() != null && colConstraintElemCtx.opt_definition().WITH() != null)
            {
                featureName = "storage_parameter";
            }
        }
        else if (colConstraintElemCtx.PRIMARY() != null && colConstraintElemCtx.KEY() != null)
        {
            featureName = "constraint_primary_key_standard";
            if (colConstraintElemCtx.opt_definition() != null && colConstraintElemCtx.opt_definition().WITH() != null)
            {
                featureName = "storage_parameter";
            }
        }
        else if (colConstraintElemCtx.CHECK() != null)
        {
            // TODO: Add check for expressions inside the CHECK constraint
            featureName = "constraint_check_standard";
        }
        else if (colConstraintElemCtx.DEFAULT() != null)
        {
            featureConfig = ConfigLoader.DEFAULT_SUPPORTED_FEATURE;
            return featureConfig;
        }
        else if (colConstraintElemCtx.GENERATED() != null)
        {
            if (colConstraintElemCtx.IDENTITY_P() != null)
            {
                featureName = "constraint_generated_as_identity_standard";
            }
            else
            {
                featureName = "constraint_generated_stored_standard";
            }
        }

        if (!featureName.isEmpty())
            featureConfig = configLoader.getFeatureConfig(featureName);

        return featureConfig;
    }
}

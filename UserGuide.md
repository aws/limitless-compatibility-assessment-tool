# What Is Limitless Compatibility Assessment Tool?
Limitless Compatibility Assessment Tool is a compatibility assessment tool for AWS Aurora Limitless Database, With Limitless Compatibility Assessment Tool, users can quickly analyze compatibility of PostgreSQL database dump files with Aurora Limitless Database.

# Installing Limitless Compatibility Assessment Tool

### Prerequisites

Before installing Limitless Compatibility Assessment Tool, you must install a Java Runtime Environment (JRE) version 8 or higher (64-bit version).

Limitless Compatibility Assessment Tool produces compatibility assessment reports in HTML format. To view the HTML output, we recommend using a recent release of the Google Chrome or Mozilla Firefox browser.

On Mac/Linux, you need to be able to run a bash script (e.g. with #!/bin/bash)

### Downloading Limitless Compatibility Assessment Tool

Limitless Compatibility Assessment Tool is available as an open-source project at: https://github.com/aws/limitless-compatibility-assessment-tool

A binary version can be downloaded from: https://github.com/aws/limitless-compatibility-assessment-tool/releases/latest and choose the most recent LimitlessCompatibilityAssessmentTool.zip file.
The installation instructions that follow are based on this version

### Installation

Limitless Compatibility Assessment Tool is distributed as an executable JAR file, which requires no CLASSPATH settings. The only environmental requirement is that the Java JRE is in the PATH.

#### Windows:

1. Download the LimitlessCompatibilityAssessmentTool.zip file as detailed in the previous section.
2. Unzip the file so that the contents are placed in your installation directory of choice; this document will assume the file resides in **C:\LimitlessCompatibilityAssessmentTool**.
3. Installation is complete.

#### Mac/Linux:

1. Download the LimitlessCompatibilityAssessmentTool.zip file as detailed in the previous section
2. Unzip this file so that the contents are placed in your directory of choice, for example **/home/**_username_**/LimitlessCompatibilityAssessmentTool** (Linux) or **/Users/**_username_**/LimitlessCompatibilityAssessmentTool** (Mac)
3. Verify the LimitlessCompatibilityAssessmentTool.sh shell script is executable by running ./LimitlessCompatibilityAssessmentTool.sh. If it is not executable, run the command: **chmod +x LimitlessCompatibilityAssessmentTool.sh**
4. Installation is complete

# Limitless Compatibility Assessment Tool input file

The input file is expected to be a SQL file generated using `pg_dump --schema-only` on a database against which user wishes to run assessment.

Postgres documentation on pg_dump options: https://www.postgresql.org/docs/current/app-pgdump.html

Note: If input file is unable to be parsed, please use encoding `UTF-8` and ensure input file is not a binary file but a text/sql file.

# Limitless Compatibility Assessment Tool output directory

The output directory is where the assessment report will be generated. As of current version, the output directory must not exist when generating the report as the tool will check for its existence and create it.

If it exists, remove the directory with following command or choose a different output directory

`rm -rf /workspace/assessmentreport`

# Command-line options

To display all of the command-line options, run ./LimitlessCompatibilityAssessmentTool.bat --help for windows and ./LimitlessCompatibilityAssessmentTool.bat --help for Mac/Linux.

* --help : Displays the help message with a list of all the command line options to invoke the Limitless Compatibility Assessment Tool with
* --version : Displays the version of the Limitless Compatibility Assessment Tool
* --engine-version <arg> : Specifies the engine version of Limitless Database the compatibility assessment will run against
* --input-file : Specifies the Postgres pg_dump schema only file that is assessed
* --output-directory : Specifies the output directory where the assessment report will be generated in

# Running Limitless Compatibility Assessment Tool on Windows

To run Limitless Compatibility Assessment Tool on Windows, open a cmd prompt (a "DOS box") and navigate to the Limitless Compatibility Assessment Tool installation directory.

Then, invoke LimitlessCompatibilityAssessmentTool[.bat] with your choice of command-line options.

Limitless Compatibility Assessment Tool usage typically starts by creating an assessment report file. The assessment report output file provides a detailed summary of the supported and unsupported SQL features in Limitless for the analyzed pg_dump SQL file. To analyze a pg_dump SQL file, simply specify an input file, engine version and output directory with your call to Limitless Compatibility Assessment Tool. For example:

`./LimitlessCompatibilityAssessmentTool.bat --input-file pgdump.sql --engine-version 16.4 --output-directory /workspace/assessmentreport`

There will be a generated assessment report in the specified output directory with file name of the format: Report_LimitlessCompatibilityAssessment_<DATE>_<TIME>.html

# Running Limitless Compatibility Assessment Tool (Mac/Linux)

To run Limitless Compatibility Assessment Tool on Linux, open a bash command prompt and navigate to the Limitless Compatibility Assessment Tool installation directory.

Then, invoke LimitlessCompatibilityAssessmentTool.sh with your choice of command-line options

Limitless Compatibility Assessment Tool usage typically starts by creating an assessment report file. The assessment report output file provides a detailed summary of the supported and unsupported SQL features in Limitless for the analyzed pg_dump SQL file. To analyze a pg_dump SQL file, simply specify an input file, engine version and output directory with your call to Limitless Compatibility Assessment Tool. For example:

`./LimitlessCompatibilityAssessmentTool.sh --input-file pgdump.sql --engine-version 16.4 --output-directory /workspace/assessmentreport`

There will be a generated assessment report in the specified output directory with file name of the format: Report_LimitlessCompatibilityAssessment_<DATE>_<TIME>.html

# Sample output

Sample outputs can be found here: https://github.com/aws/limitless-compatibility-assessment-tool/tree/main/sampleOutput

# Troubleshooting

If faced with error: `Invalid SQL syntax`, please double check if there are any syntax errors and the input file is generated with `pg_dump --schema-only` and that the provided input file is not in a binary version and rather a test/sql file. If problem persists, please use `UTF-8` encoding.
package com.github.igorhakk.sqlcsv.config

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import java.io.File

data class Properties(
    val dbUrl: String,
    val query: String,
    val targetCsv: File?,
    val printResult: Boolean
) {
    companion object
}

object PropertiesParser {
    fun Properties.Companion.fromArgs(args: Array<String>): Properties? {
        val options = Options()
            .addOption("my", "mysql", true, "MySQL host")
            .addOption("pg", "pgsql", true, "PostgreSQL host")
            .addOption("db", "dbname",true, "DB Name")
            .addOption("ssl", false, "SSL Mode")
            .addOption("u", "user", true, "DB User")
            .addOption("p", "pass", true, "DB Password")
            .addOption("q", "query", true, "Query to execute")
            .addOption("s", "source", true, "Source SQL file")
            .addOption("t", "target",true, "Target CSV file")
            .addOption("o", "print",false, "Print result to console")

        val parser = DefaultParser()
        val cmd = parser.parse(options, args)

        if (cmd.options.isEmpty()) {
            HelpFormatter().printHelp("sql-csv", options)
            return null
        }

        return Properties(
            dbUrl = getDbUrl(cmd),
            query = getQuery(cmd),
            targetCsv = cmd.getOptionValue("t")?.let { File(it) },
            printResult = cmd.hasOption("o") || !cmd.hasOption("t"),
        )
    }

    private fun getDbUrl(cmd: CommandLine): String {
        if (cmd.hasOption("my") && cmd.hasOption("pg")) {
            throw IllegalArgumentException("Cannot specify both mysql and pgsql")
        }
        val dbUrl = StringBuilder()
        dbUrl.append("r2dbc")
        cmd.hasOption("ssl").takeIf { it }?.also { dbUrl.append("s") }
        if (cmd.hasOption("mysql")) {
            dbUrl.append(":mysql://")
        } else if (cmd.hasOption("pgsql")) {
            dbUrl.append(":postgresql://")
        }

        dbUrl
            .append(cmd.getOptionValue("u"))
            .append(":")
            .append(cmd.getOptionValue("p"))
            .append("@")
            .append(cmd.getOptionValue("my") ?: cmd.getOptionValue("pg"))
            .append("/")
            .append(cmd.getOptionValue("db"))

        return dbUrl.toString()
    }

    private fun getQuery(cmd: CommandLine): String {
        if (cmd.hasOption("s") && cmd.hasOption("q")) {
            throw IllegalArgumentException("Cannot specify both -s and -q")
        }
        if (cmd.hasOption("s")) {
            return File(cmd.getOptionValue("-s")).readText()
        }
        if (!cmd.hasOption("q")) {
            throw IllegalArgumentException("Query is required")
        }
        return cmd.getOptionValue("q")
    }

}
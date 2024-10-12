package com.github.igorhakk.sqlcsv

import com.github.igorhakk.sqlcsv.config.Properties
import com.github.igorhakk.sqlcsv.config.PropertiesParser.fromArgs
import com.github.igorhakk.sqlcsv.utils.ConnectionExtension.executeQuery
import io.r2dbc.spi.ConnectionFactories
import sun.misc.Signal
import sun.net.dns.ResolverConfigurationImpl
import java.io.FileOutputStream
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    Signal.handle(Signal("INT")) { exitProcess(0) }
    val props = Properties.fromArgs(args) ?: return
    val conn = ConnectionFactories.get(props.dbUrl).create()
    val targetFOS = props.targetCsv?.let {
        if (it.exists())
            it.delete()
        FileOutputStream(it)
    }

    conn.let {
        it.executeQuery(props.query) { row, md ->
            md.columnMetadatas
                .joinToString(";") { c -> "\"${row.get(c.name)}\"" }
                .plus("\n")
        }
    }.let {
      it.doOnEach { r ->
          r.get()?.also { row ->
              if (props.printResult) {
                  println(row)
              }
              targetFOS?.write(row.toByteArray())
          }
      }
    }.blockLast()

    targetFOS?.close()
}
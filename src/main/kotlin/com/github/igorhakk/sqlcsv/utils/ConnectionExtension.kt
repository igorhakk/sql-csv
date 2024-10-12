package com.github.igorhakk.sqlcsv.utils

import io.r2dbc.spi.Connection
import io.r2dbc.spi.Result
import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

object ConnectionExtension {
    fun <T> Publisher<out Connection>.executeQuery(query: String, transform: (Row, RowMetadata) -> T): Flux<T> {
        return Mono.from(this)
            .flatMapMany { Mono.from(it.createStatement(query).execute()) }
            .flatMap { it.map { row, md -> transform(row, md) } }
    }
}
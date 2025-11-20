/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package ciyin.script.impl

import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.flatMapSuccess
import kotlin.script.experimental.api.makeFailureResult
import kotlin.script.experimental.api.plus
import kotlin.script.experimental.api.valueOr
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.ExternalDependenciesResolver
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.dependencies.addRepository

suspend fun resolveFromAnnotations(
    resolver: ExternalDependenciesResolver,
    annotations: Iterable<Annotation>
): ResultWithDiagnostics<List<File>> {
    val reports = mutableListOf<ScriptDiagnostic>()
    annotations.forEach { annotation ->
        when (annotation) {
            is Repository -> {
                for (coordinates in annotation.repositoriesCoordinates) {
                    val added = resolver.addRepository(coordinates)
                        .also { reports.addAll(it.reports) }
                        .valueOr { return it }

                    if (!added)
                        return reports + makeFailureResult(
                            "Unrecognized repository coordinates: $coordinates"
                        )
                }
            }

            is DependsOn -> {}
            else -> return makeFailureResult("Unknown annotation ${annotation.javaClass}")
        }
    }
    return annotations.filterIsInstance(DependsOn::class.java).flatMapSuccess { annotation ->
        annotation.artifactsCoordinates.asIterable().flatMapSuccess { artifactCoordinates ->
            resolver.resolve(artifactCoordinates)
        }
    }
}


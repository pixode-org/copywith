package org.pixode.copywith

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeName

class TypeResolver {

    data class CollectionInfo(
        val mutableClass: ClassName,
        val toMutable: String,
        val toImmutable: String,
    )

    data class ParameterInfo(
        val name: String,
        val typeName: TypeName,
        val isNullable: Boolean,
        val collectionInfo: CollectionInfo?,
        val mutableType: TypeName?,
        val nestedBuilderClass: ClassName?,
        val elementBuilders: List<ClassName?>?,
    )

    fun nestedBuilderClass(type: KSType): ClassName? {
        val declaration = type.declaration
        val isCopyWithAnnotated = declaration.annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == CopyWith::class.qualifiedName
        }
        if (!isCopyWithAnnotated) return null
        return ClassName(declaration.packageName.asString(), "${declaration.simpleName.asString()}Builder")
    }

    fun collectionInfo(type: KSType): CollectionInfo? {
        return when (type.declaration.qualifiedName?.asString()) {
            "kotlin.collections.List" -> CollectionInfo(
                ClassName("kotlin.collections", "MutableList"),
                "toMutableList",
                "toList",
            )

            "kotlin.collections.Set" -> CollectionInfo(
                ClassName("kotlin.collections", "MutableSet"),
                "toMutableSet",
                "toSet",
            )

            "kotlin.collections.Map" -> CollectionInfo(
                ClassName("kotlin.collections", "MutableMap"),
                "toMutableMap",
                "toMap",
            )

            else -> null
        }
    }

    fun resolveParameterInfo(param: KSValueParameter, typeParamResolver: TypeParameterResolver): ParameterInfo {
        val resolvedType = param.type.resolve()
        val isNullable = resolvedType.isMarkedNullable
        val typeName = param.type.toTypeName(typeParamResolver)
        val info = collectionInfo(resolvedType)
        val elementBuilders = info?.let {
            resolvedType.arguments.map { a -> a.type?.resolve()?.let { t -> nestedBuilderClass(t) } }
        }

        val mutableType = info?.let { i ->
            val args = resolvedType.arguments.mapIndexed { idx, a ->
                elementBuilders?.get(idx) ?: a.type?.toTypeName(typeParamResolver) ?: STAR
            }
            val base = i.mutableClass.parameterizedBy(args)
            if (isNullable) base.copy(nullable = true) else base
        }

        val nestedBuilder = if (info == null) {
            nestedBuilderClass(resolvedType)
        } else {
            null
        }

        return ParameterInfo(
            name = param.name!!.asString(),
            typeName = typeName,
            isNullable = isNullable,
            collectionInfo = info,
            mutableType = mutableType,
            nestedBuilderClass = nestedBuilder,
            elementBuilders = elementBuilders,
        )
    }
}

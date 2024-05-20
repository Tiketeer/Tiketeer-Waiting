package com.tiketeer.TiketeerWaiting.util

import com.fasterxml.jackson.core.type.TypeReference
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.reflect.CodeSignature
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

@Component
class AspectUtils(private val expressionParser: SpelExpressionParser) {
	fun resolveKey(joinPoint: JoinPoint, key: String, value: String): String {
		if (StringUtils.hasText(key) && StringUtils.hasText(value)) {
			if (key.contains("#") || key.contains("'")) {
				val parameterNames: Array<String> = getParamNames(joinPoint)
				val args = joinPoint.args
				val context = StandardEvaluationContext()
				for (i in parameterNames.indices) {
					context.setVariable(parameterNames[i], args[i])
				}
				val v = expressionParser.parseExpression(key).getValue(context)
				return "$value::$v"
			}
			return "$value::$key"
		}
		throw RuntimeException("RedisReactiveCache annotation missing key or missing value")
	}

	private fun getParamNames(joinPoint: JoinPoint): Array<String> {
		val codeSignature = joinPoint.signature as CodeSignature
		return codeSignature.parameterNames
	}

	fun getTypeReference(method: Method): TypeReference<Any> {
		return object : TypeReference<Any>() {
			override fun getType(): Type {
				return getMethodActualReturnType(method)
			}
		}
	}

	private fun getMethodActualReturnType(method: Method): Type {
		return (method.genericReturnType as ParameterizedType).actualTypeArguments[0]
	}
}
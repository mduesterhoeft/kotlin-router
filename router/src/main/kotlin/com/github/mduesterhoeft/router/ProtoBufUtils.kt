package com.github.mduesterhoeft.router

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.util.JsonFormat

object ProtoBufUtils {
        fun toJsonWithoutWrappers(proto: GeneratedMessageV3): String {
            val message = com.google.protobuf.util.JsonFormat.printer().omittingInsignificantWhitespace().includingDefaultValueFields().print(proto)
            return removeWrapperObjects(message)
        }

        fun removeWrapperObjects(json: String): String {
            return ProtoBufUtils.removeWrapperObjects(
                jacksonObjectMapper().readTree(
                    json
                )
            ).toString()
        }

        fun removeWrapperObjects(json: JsonNode): JsonNode {
            if (json.isArray) {
                return ProtoBufUtils.removeWrapperObjects(json as ArrayNode)
            } else if (json.isObject) {
                if (json.has("value") && json.size() == 1) {
                    return json.get("value")
                }
                return ProtoBufUtils.removeWrapperObjects(json as ObjectNode)
            }
            return json
        }

        private fun removeWrapperObjects(json: ObjectNode): ObjectNode {
            val result = jacksonObjectMapper().createObjectNode()
            for (entry in json.fields()) {
                if (entry.value.isContainerNode) {
                    if (entry.value.size() > 0) {
                        result.set(entry.key,
                            ProtoBufUtils.removeWrapperObjects(entry.value)
                        )
                    } else {
                        result.set(entry.key, jacksonObjectMapper().nodeFactory.nullNode())
                    }
                } else {
                    result.set(entry.key, entry.value)
                }
            }
            return result
        }

        private fun removeWrapperObjects(json: ArrayNode): ArrayNode {
            val result = jacksonObjectMapper().createArrayNode()
            for (entry in json) {
                result.add(ProtoBufUtils.removeWrapperObjects(entry))
            }
            return result
        }
}
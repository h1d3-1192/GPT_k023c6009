package jp.ac.neec.it.k023c6009.gpt_k023c6009

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletion(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val usage: Usage,
    val choices: List<Choice>
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

@Serializable
data class Choice(
    val message: Message,
    val finish_reason: String,
    val index: Int
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class RequestData(
    val model: String,
    val messages: List<Message>
)
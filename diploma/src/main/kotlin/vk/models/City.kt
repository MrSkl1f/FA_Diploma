package vk.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Serializable
data class City(
    @JsonProperty("id")
    val id: Int,

    @JsonProperty("title")
    val title: String
)

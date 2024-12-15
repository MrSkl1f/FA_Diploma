package vk.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Serializable
data class Career(
    @JsonProperty("group_id")
    val groupId: Int,

    @JsonProperty("company")
    val company: String?,

    @JsonProperty("city_id")
    val cityId: Int,

    @JsonProperty("city_name")
    val cityName: String?,

    @JsonProperty("from")
    val from: Int,

    @JsonProperty("until")
    val until: Int,

    @JsonProperty("position")
    val position: String?,
)
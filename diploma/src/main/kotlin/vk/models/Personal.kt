package vk.models

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@JsonIgnoreProperties(ignoreUnknown = true)
@Serializable
data class Personal(
    @JsonProperty("political")
    val political: Int,

    @JsonProperty("people_main")
    val peopleMain: Int,

    @JsonProperty("life_main")
    val lifeMain: Int,

    @JsonProperty("smoking")
    val smoking: Int,

    @JsonProperty("alcohol")
    val alcohol: Int,
)

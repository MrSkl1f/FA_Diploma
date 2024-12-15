package vk.models

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class User(
    @JsonProperty("id")
    val id: Int,

    @JsonProperty("can_access_closed")
    val canAccessClosed: Boolean,

    @JsonProperty("sex")
    val sex: Int,

    @JsonProperty("online")
    val online: Boolean,

    @JsonProperty("bdate")
    val bdate: String,

    @JsonProperty("city")
    val city: City,

    @JsonProperty("country")
    val country: Country?,

    @JsonProperty("has_photo")
    val hasPhoto: Boolean,

    @JsonProperty("has_mobile")
    val hasMobile: Boolean,

    @JsonProperty("followers_count")
    val followersCount: Int,

    @JsonProperty("career")
    val career: List<Career>?,

    @JsonProperty("university")
    val university: Int,

    @JsonProperty("faculty")
    val faculty: Int,

    @JsonProperty("graduation")
    val graduation: Int,

    @JsonProperty("relation")
    val relation: Int,

    @JsonProperty("personal")
    val personal: Personal?,

    @JsonProperty("first_name")
    val firstName: String,

    @JsonProperty("last_name")
    val lastName: String,

    @JsonProperty("is_closed")
    val isClosed: Boolean,
) {

    @JsonIgnore
    var activities: List<Group> = emptyList()
}

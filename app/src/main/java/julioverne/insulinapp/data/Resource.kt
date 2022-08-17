package julioverne.insulinapp.data

class Resource<T>(
    val status: Status,
    val data: T? = null,
    val error: String? = null
) {

    companion object {

        @JvmStatic
        fun <T> success(data: T?) = Resource(Status.SUCCESS, data)
        fun <T> loading() = Resource<T>(Status.LOADING)
        fun <T> error(error: String? = null) = Resource<T>(Status.ERROR, error = error)
    }

    fun isSuccessful() = status == Status.SUCCESS && data != null

    override fun toString(): String = "${status.name}, ${data.toString()}"
}

enum class Status {
    LOADING, SUCCESS, ERROR
}
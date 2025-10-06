package io.github.skydynamic.maidataviewer.core

class MaiVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) : Comparable<MaiVersion> {
    constructor(major: Int, minor: Int) : this(major, minor, 0)

    private val version = versionOf(major, minor, patch)

    private fun versionOf(major: Int, minor: Int, patch: Int): Int {
        require(
            major in -1..MAX_COMPONENT_VALUE
                    && minor in 0..MAX_COMPONENT_VALUE
                    && patch in 0..MAX_COMPONENT_VALUE
        ) {
            "Version components are out of range: $major.$minor.$patch"
        }
        return major.shl(16) + minor.shl(8) + patch
    }

    override fun toString(): String {
        if (major == -1) {
            return "UNKNOWN"
        }
        return "$major.$minor-${('A' + patch - 1)}"
    }

    fun toStandardString(): String {
        if (major == -1) {
            return "-1"
        }
        return "$major.$minor.${"%02d".format(patch)}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherVersion = (other as? MaiVersion) ?: return false
        return this.version == otherVersion.version
    }

    override fun hashCode(): Int = version

    override fun compareTo(other: MaiVersion): Int = version - other.version

    fun isAtLeast(major: Int, minor: Int): Boolean =
        this.major > major || (this.major == major &&
                this.minor >= minor)

    fun isAtLeast(major: Int, minor: Int, patch: Int): Boolean =
        this.major > major || (this.major == major &&
                (this.minor > minor || this.minor == minor &&
                        this.patch >= patch))

    companion object {
        const val MAX_COMPONENT_VALUE = 99

        fun tryParse(version: String): MaiVersion? {
            val regex = Regex("""^(\d+)\.(\d+)\.(\d+)$""")
            val match = regex.matchEntire(version)
            return if (match != null) {
                val (major, minor, patch) = match.destructured
                MaiVersion(major.toInt(), minor.toInt(), patch.toInt())
            } else {
                null
            }
        }
    }
}
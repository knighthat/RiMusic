package app.kreate.utils


private var flavorArch: String? = null
private var flavorEnv: String? = null

val FLAVOR_ARCH: String
    get() = flavorArch!!
val FLAVOR_ENV: String
    get() = flavorEnv!!

/**
 * This function can only be called once,
 * subsequence calls result in [IllegalStateException].
 */
fun setFlavorArch( arch: String ) {
    check( flavorArch == null )
    flavorArch = arch
}

/**
 * This function can only be called once,
 * subsequence calls result in [IllegalStateException].
 */
fun setFlavorEnv( env: String ) {
    check( flavorEnv == null )
    flavorEnv = env
}

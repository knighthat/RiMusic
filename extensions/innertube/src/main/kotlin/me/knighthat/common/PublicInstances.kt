package me.knighthat.common

abstract class PublicInstances {

    protected lateinit var instances: Array<String>
    protected lateinit var unreachableInstances: MutableList<Regex>

    val reachableInstances: Collection<String>
        get() = instances.filter {
            for ( regex in unreachableInstances )
                if ( regex.matches(it) )
                    return@filter false

            true
        }

    open suspend fun fetchInstances() {
        this.instances = arrayOf()

        if( ::unreachableInstances.isInitialized )
            this.unreachableInstances.clear()
        else
            this.unreachableInstances = mutableListOf()
    }

    internal fun getDistinctFirstGroup(
        responseText: String,
        httpRegex: Regex
    ): Array<String> =
        httpRegex.findAll(responseText)
                 .mapNotNull { it.groups[1]?.value?.trim() }    // Get value of first group, exclude null
                 .distinct()                                    // Only take unique values
                 .toList()
                 .toTypedArray()

    /**
     * Blacklists a domain name and all of its subdomains.
     *
     * @param url domain name to block
     */
    fun blacklistUrl( url: String ) {
        if( !::unreachableInstances.isInitialized )
            throw UninitializedPropertyAccessException(
                "Please make sure \"instances\" is initialized with ${this::class.java.name}#fetchInstances"
            )
        else
            unreachableInstances.add( HttpFetcher.genMatchAllTld( url ) )
    }
}
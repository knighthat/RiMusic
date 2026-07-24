package app.kreate.utils.priomap

import java.util.TreeMap


actual fun <V> createPrioritySortedMap(): MutableMap<PriorityKey, V> = TreeMap()
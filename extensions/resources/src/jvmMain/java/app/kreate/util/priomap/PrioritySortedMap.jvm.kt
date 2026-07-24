package app.kreate.util.priomap

import app.kreate.utils.priomap.PriorityKey
import java.util.TreeMap


actual fun <V> createPrioritySortedMap(): MutableMap<PriorityKey, V> = TreeMap()
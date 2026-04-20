package off.kys.sniffer.ui.utils

/**
 * Consumes any value and returns [Unit], effectively acting as a logical
 * dead-end to satisfy compiler checks for unused expressions or assignments.
 *
 * @receiver The value to be consumed. Referencing it inside the block ensures the compiler treats it as "read."
 * @return [Unit]
 */
fun Any?.discard() {
    this.toString()
}
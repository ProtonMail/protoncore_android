package me.proton.core.util.android.datetime

import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage
import java.util.Date

@ExcludeFromCoverage
public interface Clock {
    public fun currentEpochMillis(): Long
    public fun currentEpochSeconds(): Long
    public fun currentDate(): Date

    public companion object {
        public fun systemUtc(): ClockSystemUtc = ClockSystemUtc()
        public fun fixed(currentEpochMillis: Long): ClockFixed = ClockFixed(currentEpochMillis)
    }
}

@ExcludeFromCoverage
public class ClockSystemUtc : Clock {
    public override fun currentEpochMillis(): Long = System.currentTimeMillis()
    public override fun currentEpochSeconds(): Long = currentEpochMillis() / 1000
    public override fun currentDate(): Date = Date(currentEpochMillis())
}

@ExcludeFromCoverage
public data class ClockFixed(val currentEpochMillis: Long) : Clock {
    public override fun currentEpochMillis(): Long = currentEpochMillis
    public override fun currentEpochSeconds(): Long = currentEpochMillis / 1000
    public override fun currentDate(): Date = Date(currentEpochMillis())
}

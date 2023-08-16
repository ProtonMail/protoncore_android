# Useful for keeping the `SchemaId` annotation class, which is needed at runtime.
# In R8 full mode, annotations are only kept for classes which are matched by keep rules.
# https://r8.googlesource.com/r8/+/refs/heads/master/compatibility-faq.md#r8-full-mode
-keep,allowshrinking,allowoptimization,allowobfuscation,allowaccessmodification class * extends me.proton.core.observability.domain.metrics.ObservabilityData

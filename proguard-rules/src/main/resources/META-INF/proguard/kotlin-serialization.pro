# If a Companion object defines a `serializer` method, keep the Companion object, and the `serializer` method:
-keepclasseswithmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# If a Companion object has a `serializer` method, keep the Companion field on the original class.
-if class **.*$Companion {
  kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class <1>.<2> {
  <1>.<2>$Companion Companion;
}

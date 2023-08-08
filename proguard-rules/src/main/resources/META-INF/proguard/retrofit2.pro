# The following issues have been apparently fixed, but a new version
# of Retrofit wasn't released yet (v2.9.0 it the lates at the time of writing this):
# - https://github.com/square/retrofit/issues/3751
# - https://github.com/square/retrofit/issues/3880
# This file contains additional proguard rules, that were added after version 2.9.0:
# https://github.com/square/retrofit/compare/2.9.0...029cbb4#diff-50d428633d98e235831f5f9b75d7aa48897d5edc2bed30c55c9bb9ec20b36f82

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Keep inherited services.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# R8 full mode strips generic signatures from return types if not kept.
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>

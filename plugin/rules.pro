-ignorewarnings
-allowaccessmodification
-dontusemixedcaseclassnames
-dontobfuscate

# Keep kotlin metadata so that the Kotlin compiler knows about top level functions and other things
-keep class kotlin.Metadata { *; }

# Keep FunctionX because they are used in the public API of Gradle/AGP/KGP
-keep class kotlin.jvm.functions.** { *; }

# Keep Unit for kts compatibility, functions in a Gradle extension returning a relocated Unit won't work
-keep class kotlin.Unit

# We need to keep type arguments (Signature) for Gradle to be able to instantiate abstract models like `Property`
-keepattributes Signature,Exceptions,*Annotation*,InnerClasses,PermittedSubclasses,EnclosingMethod,Deprecated,SourceFile,LineNumberTable

# Keep your public API so that it's callable from scripts
-keep public class ru.pixnews.gradle.fbase.* {
    *;
}
# -keep class ru.pixnews.gradle.fbase.** { *; }

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# -repackageclasses int


# Glass Home uses no reflection. Activities, services, receivers and the custom views in layouts are kept by the
# rules the build generates from the manifest and layouts.
#
# Those generated rules follow the visibilities of the day, so a class made package-private later would be renamed
# or stripped and the crash would only show on someone's TV, where nothing runs a build. Kept by name here instead:
# these are the classes Android reaches for by string, from the manifest and from the layouts.
-keep class com.techpp.glass.HomeActivity
-keep class com.techpp.glass.SleepService
-keep class com.techpp.glass.Screensaver
-keep class com.techpp.glass.WatchService
-keep class com.techpp.glass.SelfUpdate$Receiver
-keep class com.techpp.glass.Sleep$Alarm
-keep class com.techpp.glass.ServiceRepair$Starter

# The views the layouts inflate, with the two-argument constructor Android calls.
-keepclasseswithmembers class com.techpp.glass.Backdrop {
    <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class com.techpp.glass.HighlightView {
    <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class com.techpp.glass.Meters {
    <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class com.techpp.glass.PicksRow {
    <init>(android.content.Context, android.util.AttributeSet);
}

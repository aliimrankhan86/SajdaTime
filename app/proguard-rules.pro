# adhan-java is reflection-free; default rules are sufficient.
# Keep WorkManager workers, which are instantiated by name.
-keep class com.sajdatime.app.notify.DailyRescheduleWorker { <init>(...); }

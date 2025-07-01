# ============================
# 🚀 Cấu hình ProGuard cho GraalVM JDK 22
# ============================

# 📌 Đầu vào (class đã biên dịch) và thư viện ngoài
-injars build/classes
-injars lib/  # Đảm bảo ProGuard không xóa thư viện ngoài

# 📌 Đầu ra (file JAR đã được làm rối)
-outjars dist/1.jar

# Đường dẫn JDK 22
-libraryjars "C:/Program Files/Java/jdk-22/lib/jmods/java.base.jmod"
-libraryjars "C:/Program Files/Java/jdk-22/lib/jmods/java.logging.jmod"
-libraryjars "C:/Program Files/Java/jdk-22/lib/jmods/java.xml.jmod"

# ============================
# 🚀 Giữ lại các class quan trọng
# ============================

# 🔹 Không tối ưu các class cốt lõi của Java
-keep class java.lang.Object { *; }
-keep class java.lang.Enum { *; }

# 🔹 Giữ lại tất cả class trong package `server`
-keep class server.** { *; }

# 🔹 Giữ lại các class thuộc thư viện ngoài
-keep class org.** { *; }
-keep class com.** { *; }
-keep class javax.** { *; }
-keep class sun.** { *; }

# 🔹 Giữ lại tất cả các phương thức main()
-keep public class * {
    public static void main(java.lang.String[]); 
}

# 🔹 Tránh loại bỏ annotation quan trọng
-keepattributes *Annotation*

# 🔹 Giữ lại tất cả các class có thể dùng Reflection
-keepclassmembers class * {
    @java.lang.reflect.* *;
}

# ============================
# 🚀 Debugging (Kiểm tra lỗi)
# ============================

# 🔹 Không làm nhỏ code (để kiểm tra lỗi)
-dontshrink

# 🔹 Không tối ưu (tránh mất class)
-dontoptimize

# 🔹 Không làm rối tên (chỉ dùng để debug)
-dontobfuscate

# 🔹 Xuất file debug để kiểm tra
-printseeds seeds.txt
-printusage usage.txt
-printmapping mapping.txt

# ============================
# 🚀 Bỏ qua các cảnh báo không quan trọng
# ============================

# 🔹 Bỏ qua cảnh báo từ các thư viện ngoài
-dontwarn org.apache.**
-dontwarn javax.**
-dontwarn com.**
-keep class org.apache.log4j.** { *; }
-keep class lombok.** { *; }
-keep class network.** { *; }
-dontwarn lombok.**
-dontwarn network.**
-dontwarn org.apache.commons.lang.**
-dontwarn org.apache.log4j.**
-dontwarn org.slf4j.**



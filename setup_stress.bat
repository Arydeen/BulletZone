$setupScript = @'
@echo off
setlocal enabledelayedexpansion

set "BASE_DIR=app\src\main\java\edu\unh\cs\cs619\bulletzone"

mkdir "%BASE_DIR%" 2>nul

echo Creating ServerStressTest.java...
(
echo package edu.unh.cs.cs619.bulletzone;
echo.
echo import org.springframework.web.client.RestTemplate;
echo import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
echo import java.util.ArrayList;
echo import java.util.List;
echo import java.util.concurrent.CountDownLatch;
echo import java.util.concurrent.ExecutorService;
echo import java.util.concurrent.Executors;
echo import java.util.concurrent.TimeUnit;
echo import java.util.concurrent.atomic.AtomicInteger;
echo import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
echo import edu.unh.cs.cs619.bulletzone.util.LongWrapper;
echo.
echo public class ServerStressTest {
echo     private static final String SERVER_URL = "http://10.2.1.42:8080/games";
echo     private static final int NUM_THREADS = 10;
echo     private static final int OPERATIONS_PER_THREAD = 50;
echo     private final AtomicInteger successfulOperations = new AtomicInteger^(0^);
echo     private final AtomicInteger failedOperations = new AtomicInteger^(0^);
echo     private final List^<TestUser^> createdUsers = new ArrayList^<^>^(^);
echo     private final RestTemplate restTemplate;
echo.
echo     public ServerStressTest^(^) {
echo         restTemplate = new RestTemplate^(^);
echo         restTemplate.getMessageConverters^(^).add^(new MappingJackson2HttpMessageConverter^(^)^);
echo     }
echo.
echo     // Add the complete ServerStressTest class implementation here...
echo }
) > "%BASE_DIR%\ServerStressTest.java"

echo Updating build.gradle...
echo dependencies { >> app\build.gradle
echo     implementation 'org.springframework:spring-web:5.3.23' >> app\build.gradle
echo     implementation 'com.fasterxml.jackson.core:jackson-databind:2.13.4' >> app\build.gradle
echo } >> app\build.gradle

echo Setup complete! Server stress test has been created.
echo File location: %BASE_DIR%\ServerStressTest.java
echo Don't forget to sync your project in Android Studio.
pause
'@

$setupScript | Out-File -Encoding ASCII setup_stress_test.bat
Write-Host "Setup script created. Run setup_stress_test.bat to set up the stress test."
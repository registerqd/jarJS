@echo off
rem ============================================================================
rem JARJS v0.7 Windows CMD 入口脚本
rem 用法: jarjs.bat build app.jar -o app.html [选项]
rem ============================================================================
setlocal enabledelayedexpansion

set "SCRIPT_DIR=%~dp0"
for %%I in ("%SCRIPT_DIR%..") do set "JARJS_HOME=%%~fI"

rem ---------- 1. 探测 Python ----------
set "PY_CMD="
if defined PYTHON if exist "%PYTHON%" set "PY_CMD=%PYTHON%"
if not defined PY_CMD (
    where py >nul 2>&1 && set "PY_CMD=py -3"
)
if not defined PY_CMD (
    where python >nul 2>&1 && set "PY_CMD=python"
)
if not defined PY_CMD (
    where python3 >nul 2>&1 && set "PY_CMD=python3"
)
if not defined PY_CMD (
    echo [JARJS] 错误: 找不到 Python 3，请先安装 Python 并添加到 PATH 或设置 PYTHON 环境变量 >&2
    exit /b 1
)

rem ---------- 2. 探测 JDK (需 21+) ----------
if not defined JAVA_HOME (
    where javac >nul 2>&1
    if not errorlevel 1 (
        for /f "delims=" %%I in ('where javac') do (
            set "JAVAC_PATH=%%~dpI"
            set "JAVA_HOME=!JAVAC_PATH:~0,-5!"
            goto :found_java
        )
    )
    rem 探测常见 Windows JDK 安装路径
    for %%P in (
        "C:\Program Files\Java\jdk-21*"
        "C:\Program Files\Eclipse Adoptium\jdk-21*"
        "C:\Program Files\Microsoft\jdk-21*"
        "C:\Program Files\BellSoft\LibericaJDK-21*"
        "C:\Program Files\Zulu\zulu-21*"
        "C:\Program Files\Java\jdk-25*"
    ) do (
        for /d %%D in (%%P) do (
            if exist "%%D\bin\javac.exe" (
                set "JAVA_HOME=%%D"
                goto :found_java
            )
        )
    )
)
:found_java
if not defined JAVA_HOME (
    echo [JARJS] 错误: 找不到 JDK 21+，请安装 JDK 并设置 JAVA_HOME 环境变量 >&2
    exit /b 1
)
set "PATH=%JAVA_HOME%\bin;%PATH%"

rem ---------- 3. 探测 Maven ----------
if not defined MVN (
    if defined MAVEN_HOME if exist "%MAVEN_HOME%\bin\mvn.cmd" set "MVN=%MAVEN_HOME%\bin\mvn.cmd"
    if not defined MVN if defined M2_HOME if exist "%M2_HOME%\bin\mvn.cmd" set "MVN=%M2_HOME%\bin\mvn.cmd"
    if not defined MVN (
        where mvn.cmd >nul 2>&1 && set "MVN=mvn.cmd"
    )
    if not defined MVN (
        where mvn >nul 2>&1 && set "MVN=mvn"
    )
    if not defined MVN (
        for %%P in ("C:\Program Files\apache-maven*" "C:\apache-maven*") do (
            for /d %%D in (%%P) do (
                if exist "%%D\bin\mvn.cmd" (
                    set "MVN=%%D\bin\mvn.cmd"
                    goto :found_mvn
                )
            )
        )
    )
)
:found_mvn
if not defined MVN (
    echo [JARJS] 错误: 找不到 Maven (mvn)，请安装 Maven 并设置 PATH 或 MVN 环境变量 >&2
    exit /b 1
)

rem ---------- 4. 确保 shim 兼容层已编译 ----------
set "SHIM_JAR=%JARJS_HOME%\shim\target\shim-1.0.jar"
if not exist "%SHIM_JAR%" (
    echo [JARJS] 兼容层未构建，正在编译 shim...
    pushd "%JARJS_HOME%\shim"
    call "%MVN%" -B -q clean package
    popd
)
call "%MVN%" -B -q install:install-file -Dfile="%SHIM_JAR%" -DgroupId=jarjs -DartifactId=shim -Dversion=1.0 -Dpackaging=jar >nul 2>&1

rem ---------- 5. 参数解析 ----------
set "CMD=%~1"
if not "%CMD%"=="build" (
    echo JARJS v0.7 ^(Windows^) —— Java jar 到网页的一键转译器
    echo.
    echo 用法:
    echo   jarjs.bat build app.jar -o app.html [选项]
    echo.
    echo 选项:
    echo   -o, --output ^<file^>    输出文件（.html 单文件 或 .js）
    echo   --main ^<class^>         指定入口类（默认读取 jar 的 Main-Class）
    echo   --libs ^<dir^>           外部依赖目录：目录下所有 .jar 自动注入构建
    echo   --cp ^<a.jar;b.jar^>     外部依赖列表（分号或冒号分隔）
    echo   --offline              离线模式：桩掉网络/加密链，单机程序可直接跑
    echo   --report               静态扫描缺口：一次性输出全部缺失类与符号，不跑转译
    echo   --auto                 全自动补桩：递归补依赖类，循环到通过
    echo   --lenient              宽松插桩：缺类自动补桩，不中断编译
    echo   --width ^<px^>           画布宽（默认 480）
    echo   --height ^<px^>          画布高（默认 480）
    echo   -h, --help             显示帮助
    exit /b 0
)
shift

set "INPUT="
set "OUTPUT=app.html"
set "MAIN="
set "LIBS_DIR="
set "CP_EXTRA="
set "OFFLINE=0"
set "REPORT=0"
set "AUTO=0"
set "LENIENT=0"
set "W=480"
set "H=480"

:parse_args
if "%~1"=="" goto :done_args
if "%~1"=="-o" ( set "OUTPUT=%~2" & shift & shift & goto :parse_args )
if "%~1"=="--output" ( set "OUTPUT=%~2" & shift & shift & goto :parse_args )
if "%~1"=="--main" ( set "MAIN=%~2" & shift & shift & goto :parse_args )
if "%~1"=="--libs" ( set "LIBS_DIR=%~2" & shift & shift & goto :parse_args )
if "%~1"=="--cp" ( set "CP_EXTRA=%~2" & shift & shift & goto :parse_args )
if "%~1"=="--offline" ( set "OFFLINE=1" & shift & goto :parse_args )
if "%~1"=="--report" ( set "REPORT=1" & shift & goto :parse_args )
if "%~1"=="--auto" ( set "AUTO=1" & shift & goto :parse_args )
if "%~1"=="--lenient" ( set "LENIENT=1" & shift & goto :parse_args )
if "%~1"=="--width" ( set "W=%~2" & shift & shift & goto :parse_args )
if "%~1"=="--height" ( set "H=%~2" & shift & shift & goto :parse_args )
if "%~1"=="-h" ( goto :help )
if "%~1"=="--help" ( goto :help )
if not defined INPUT ( set "INPUT=%~1" & shift & goto :parse_args )
shift
goto :parse_args

:help
exit /b 0

:done_args
if not defined INPUT (
    echo [JARJS] 错误: 请指定输入的 jar 文件 >&2
    exit /b 1
)
if not exist "%INPUT%" (
    echo [JARJS] 错误: 找不到输入 jar: %INPUT% >&2
    exit /b 1
)

for %%I in ("%INPUT%") do set "INPUT_ABS=%%~fI"
for %%I in ("%OUTPUT%") do (
    set "OUT_DIR=%%~dpI"
    set "OUTPUT_ABS=%%~fI"
)

rem ---------- 6. 外部依赖收集 ----------
set "EXTRA_DEPS="
set "EXTRA_CP="
set /a LIB_IDX=0

if defined LIBS_DIR (
    if not exist "%LIBS_DIR%" (
        echo [JARJS] 错误: --libs 目录不存在: %LIBS_DIR% >&2
        exit /b 1
    )
    for %%F in ("%LIBS_DIR%\*.jar") do (
        set /a LIB_IDX+=1
        call "%MVN%" -B -q install:install-file -Dfile="%%~fF" -DgroupId=jarjs.ext -DartifactId=lib!LIB_IDX! -Dversion=1.0 -Dpackaging=jar >nul 2>&1
        set "EXTRA_DEPS=!EXTRA_DEPS!<dependency><groupId>jarjs.ext</groupId><artifactId>lib!LIB_IDX!</artifactId><version>1.0</version></dependency>"
        if defined EXTRA_CP (
            set "EXTRA_CP=!EXTRA_CP!;%%~fF"
        ) else (
            set "EXTRA_CP=%%~fF"
        )
    )
    if !LIB_IDX! gtr 0 echo [JARJS] 注入外部依赖 !LIB_IDX! 个...
)

rem ---------- 7. 静态报告模式 ----------
if "%REPORT%"=="1" (
    echo [JARJS] 静态扫描全部符号引用（不执行转译）...
    %PY_CMD% "%JARJS_HOME%\bin\report.py" "%INPUT_ABS%" "%SHIM_JAR%" "%LIBS_DIR%"
    exit /b 0
)

rem ---------- 8. 探测入口类 ----------
if not defined MAIN (
    for /f "delims=" %%M in ('%PY_CMD% -c "import zipfile,re; z=zipfile.ZipFile(r'%INPUT_ABS%'); m=re.search(r'Main-Class:\s*([^\r\n]+)', z.read('META-INF/MANIFEST.MF').decode('utf-8','replace')) if 'META-INF/MANIFEST.MF' in z.namelist() else None; print(m.group(1).strip() if m else '')"') do (
        set "MAIN=%%M"
    )
)
if not defined MAIN (
    echo [JARJS] 无法确定入口类，请使用 --main 包名.类名 指定 >&2
    exit /b 1
)
echo [JARJS] 入口类: %MAIN%

rem ---------- 9. 临时工作目录 ----------
set "WORK=%TEMP%\jarjs_%RANDOM%_%RANDOM%"
mkdir "%WORK%" 2>nul

echo [JARJS] 安装输入 jar...
call "%MVN%" -B -q install:install-file -Dfile="%INPUT_ABS%" -DgroupId=jarjs.input -DartifactId=app -Dversion=1.0 -Dpackaging=jar >nul 2>&1

rem 写入第一阶段 Shade pom.xml
(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"^>
echo   ^<modelVersion^>4.0.0^</modelVersion^>
echo   ^<groupId^>jarjs.build^</groupId^>^<artifactId^>build^</artifactId^>^<version^>1.0^</version^>^<packaging^>jar^</packaging^>
echo   ^<properties^>
echo     ^<maven.compiler.source^>21^</maven.compiler.source^>
echo     ^<maven.compiler.target^>21^</maven.compiler.target^>
echo     ^<project.build.sourceEncoding^>UTF-8^</project.build.sourceEncoding^>
echo   ^</properties^>
echo   ^<dependencies^>
echo     ^<dependency^>^<groupId^>jarjs.input^</groupId^>^<artifactId^>app^</artifactId^>^<version^>1.0^</version^>^</dependency^>
echo     ^<dependency^>^<groupId^>jarjs^</groupId^>^<artifactId^>shim^</artifactId^>^<version^>1.0^</version^>^</dependency^>
echo     ^<dependency^>^<groupId^>org.teavm^</groupId^>^<artifactId^>teavm-classlib^</artifactId^>^<version^>0.16.0^</version^>^<scope^>provided^</scope^>^</dependency^>
echo     ^<dependency^>^<groupId^>org.teavm^</groupId^>^<artifactId^>teavm-jso-apis^</artifactId^>^<version^>0.16.0^</version^>^<scope^>provided^</scope^>^</dependency^>
echo     %EXTRA_DEPS%
echo   ^</dependencies^>
echo   ^<build^>
echo     ^<plugins^>
echo       ^<plugin^>
echo         ^<groupId^>org.apache.maven.plugins^</groupId^>^<artifactId^>maven-shade-plugin^</artifactId^>^<version^>3.5.1^</version^>
echo         ^<executions^>^<execution^>^<phase^>package^</phase^>^<goals^>^<goal^>shade^</goal^>^</goals^>
echo           ^<configuration^>
echo             ^<createDependencyReducedPom^>false^</createDependencyReducedPom^>
echo             ^<relocations^>
echo               ^<relocation^>^<pattern^>org.lwjgl^</pattern^>^<shadedPattern^>jarjs.lwjgl^</shadedPattern^>^</relocation^>
echo               ^<relocation^>^<pattern^>javax.swing^</pattern^>^<shadedPattern^>jarjs.swing^</shadedPattern^>^</relocation^>
echo               ^<relocation^>^<pattern^>javax.imageio^</pattern^>^<shadedPattern^>jarjs.imageio^</shadedPattern^>^</relocation^>
echo               ^<relocation^>^<pattern^>javax.sound^</pattern^>^<shadedPattern^>jarjs.sound^</shadedPattern^>^</relocation^>
echo               ^<relocation^>^<pattern^>sun.misc.Unsafe^</pattern^>^<shadedPattern^>jarjs.unsafe.Unsafe^</shadedPattern^>^</relocation^>
echo               ^<relocation^>^<pattern^>java.awt^</pattern^>^<shadedPattern^>jarjs.awt^</shadedPattern^>^</relocation^>
echo               ^<relocation^>^<pattern^>java.util.concurrent.locks^</pattern^>^<shadedPattern^>jarjs.juc.locks^</shadedPattern^>^</relocation^>
echo               ^<relocation^>^<pattern^>java.util.concurrent.Executors^</pattern^>^<shadedPattern^>jarjs.juc.Executors^</shadedPattern^>^</relocation^>
echo               ^<relocation^>^<pattern^>java.util.concurrent.ExecutorService^</pattern^>^<shadedPattern^>jarjs.juc.ExecutorService^</shadedPattern^>^</relocation^>
echo               ^<relocation^>^<pattern^>java.util.concurrent.Future^</pattern^>^<shadedPattern^>jarjs.juc.Future^</shadedPattern^>^</relocation^>
echo               ^<relocation^>^<pattern^>java.util.concurrent.TimeUnit^</pattern^>^<shadedPattern^>jarjs.juc.TimeUnit^</shadedPattern^>^</relocation^>
echo             ^</relocations^>
echo           ^</configuration^>
echo         ^</execution^>^</executions^>
echo       ^</plugin^>
echo     ^</plugins^>
echo   ^</build^>
echo ^</project^>
) > "%WORK%\pom.xml"

pushd "%WORK%"
echo [JARJS] 重定位 + 打包...
call "%MVN%" -B -q package >nul 2>&1
if errorlevel 1 (
    echo [JARJS] 错误: 重定位打包失败 >&2
    popd
    rmdir /s /q "%WORK%" 2>nul
    exit /b 1
)

set "SHADED=%WORK%\target\build-1.0.jar"
if not exist "%SHADED%" (
    for %%F in ("%WORK%\target\*.jar") do (
        echo "%%~nxF" | findstr /i "original" >nul || set "SHADED=%%~fF"
    )
)
echo [JARJS] 安装重定位产物...
call "%MVN%" -B -q install:install-file -Dfile="%SHADED%" -DgroupId=jarjs.relocated -DartifactId=app -Dversion=1.0 -Dpackaging=jar >nul 2>&1

echo [JARJS] TeaVM 转译...
mkdir "%WORK%\conv" 2>nul
(
echo ^<?xml version="1.0" encoding="UTF-8"?^>
echo ^<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"^>
echo   ^<modelVersion^>4.0.0^</modelVersion^>
echo   ^<groupId^>jarjs.conv^</groupId^>^<artifactId^>conv^</artifactId^>^<version^>1.0^</version^>^<packaging^>jar^</packaging^>
echo   ^<properties^>
echo     ^<maven.compiler.source^>21^</maven.compiler.source^>
echo     ^<maven.compiler.target^>21^</maven.compiler.target^>
echo   ^</properties^>
echo   ^<dependencies^>
echo     ^<dependency^>^<groupId^>jarjs.relocated^</groupId^>^<artifactId^>app^</artifactId^>^<version^>1.0^</version^>^</dependency^>
echo     ^<dependency^>^<groupId^>jarjs^</groupId^>^<artifactId^>shim^</artifactId^>^<version^>1.0^</version^>^</dependency^>
echo     ^<dependency^>^<groupId^>org.teavm^</groupId^>^<artifactId^>teavm-classlib^</artifactId^>^<version^>0.16.0^</version^>^<scope^>provided^</scope^>^</dependency^>
echo     ^<dependency^>^<groupId^>org.teavm^</groupId^>^<artifactId^>teavm-jso-apis^</artifactId^>^<version^>0.16.0^</version^>^<scope^>provided^</scope^>^</dependency^>
echo     %EXTRA_DEPS%
echo   ^</dependencies^>
echo   ^<build^>^<plugins^>^<plugin^>
echo     ^<groupId^>org.teavm^</groupId^>^<artifactId^>teavm-maven-plugin^</artifactId^>^<version^>0.16.0^</version^>
echo     ^<executions^>^<execution^>^<id^>main^</id^>^<phase^>package^</phase^>^<goals^>^<goal^>compile^</goal^>^</goals^>
echo       ^<configuration^>
echo         ^<mainClass^>%MAIN%^</mainClass^>
echo         ^<targetDirectory^>${project.build.directory}/teavm^</targetDirectory^>
echo         ^<targetType^>JAVASCRIPT^</targetType^>
echo         ^<optimizationLevel^>ADVANCED^</optimizationLevel^>
echo         ^<debugInformationGenerated^>false^</debugInformationGenerated^>
echo         ^<sourceMapsGenerated^>false^</sourceMapsGenerated^>
echo       ^</configuration^>
echo     ^</execution^>^</executions^>
echo   ^</plugin^>^</plugins^>^</build^>
echo ^</project^>
) > "%WORK%\conv\pom.xml"

mkdir "%WORK%\conv\src" 2>nul
pushd "%WORK%\conv"
call "%MVN%" -B -q package 2>&1
popd

set "JS=%WORK%\conv\target\teavm\classes.js"
if not exist "%JS%" (
    echo [JARJS] 转译未产出有效 JS（类库缺失）
    if "%AUTO%"=="1" (
        echo [JARJS] 全自动补桩：递归补依赖类，循环到通过...
        set "FULL_CP=%SHADED%"
        if defined EXTRA_CP set "FULL_CP=!FULL_CP!;%EXTRA_CP%"
        if "%OFFLINE%"=="1" (
            %PY_CMD% "%JARJS_HOME%\bin\autostub.py" "%WORK%" "%MAIN%" --offline --cp "!FULL_CP!"
        ) else (
            %PY_CMD% "%JARJS_HOME%\bin\autostub.py" "%WORK%" "%MAIN%" --cp "!FULL_CP!"
        )
    ) else if "%LENIENT%"=="1" (
        echo [JARJS] 宽松插桩模式：尝试补桩后重试...
        set "JARJS_OFFLINE=%OFFLINE%"
        %PY_CMD% "%JARJS_HOME%\bin\lenient.py" "%WORK%" "%MAIN%"
    )
)

if not exist "%JS%" (
    echo [JARJS] 失败: 产物为空 >&2
    popd
    rmdir /s /q "%WORK%" 2>nul
    exit /b 2
)

echo [JARJS] 扫描并内嵌资源（base64）...
set "ASSETS_JS=%WORK%\assets.js"
%PY_CMD% "%JARJS_HOME%\bin\pack_assets.py" "%SHADED%" "%ASSETS_JS%"

echo [JARJS] 组装单文件 HTML...
%PY_CMD% "%JARJS_HOME%\bin\assemble.py" "%JS%" "%OUTPUT_ABS%" "%W%" "%H%" "%MAIN%" "%ASSETS_JS%"

for %%F in ("%OUTPUT_ABS%") do (
    echo [JARJS] 完成: %%~fF ^(%%~zF 字节^)
)

popd
rmdir /s /q "%WORK%" 2>nul
exit /b 0

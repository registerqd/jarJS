<#
.SYNOPSIS
    JARJS v0.8 Windows PowerShell 一键转译脚本
.DESCRIPTION
    把 Java jar 一键转成浏览器可运行的单文件 HTML。
.EXAMPLE
    .\jarjs.ps1 build app.jar -o app.html --libs .\libs --auto
#>

[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [string]$Command = "build",

    [Parameter(Position = 1)]
    [string]$InputJar,

    [Alias("o")]
    [string]$Output = "app.html",

    [string]$MainClass,
    [string]$Libs,
    [string]$Cp,
    [switch]$Offline,
    [switch]$Report,
    [switch]$Auto,
    [switch]$Lenient,
    [int]$Width = 480,
    [int]$Height = 480
)

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$JarjsHome = (Resolve-Path (Join-Path $ScriptDir "..")).Path

# ---------- 帮助信息 ----------
if ($Command -ne "build" -or -not $InputJar) {
    Write-Host @"
JARJS v0.8 (PowerShell) —— Java jar 到网页的一键转译器

用法:
  .\bin\jarjs.ps1 build app.jar [-o app.html] [选项]
  或者直接在根目录:
  .\jarjs.ps1 build app.jar [-o app.html] [选项]

选项:
  -o, -Output <file>      输出文件（.html 单文件 或 .js）
  -MainClass <class>      指定入口类（默认读取 jar 的 Main-Class）
  -Libs <dir>             外部依赖目录：目录下所有 .jar 自动注入构建
  -Cp <a.jar;b.jar>       外部依赖列表（分号分隔）
  -Offline                离线模式：桩掉网络/加密链，单机程序可直接跑
  -Report                 静态扫描缺口：一次性输出全部缺失类与符号，不跑转译
  -Auto                   全自动补桩：递归补依赖类，循环到通过（推荐给 AI 直接调用）
  -Lenient                宽松插桩：缺类自动补桩，不中断编译
  -Width <px>             画布宽（默认 480）
  -Height <px>            画布高（默认 480）
"@
    exit 0
}

# ---------- 1. 探测 Python ----------
$PyCmd = $null
if ($env:PYTHON -and (Test-Path $env:PYTHON)) {
    $PyCmd = $env:PYTHON
} else {
    foreach ($cand in @("python", "py", "python3")) {
        $cmdObj = Get-Command $cand -ErrorAction SilentlyContinue
        if ($cmdObj) { $PyCmd = $cand; break }
    }
}
if (-not $PyCmd) {
    Write-Error "[JARJS] 找不到 Python 3，请先安装 Python 并添加到 PATH 或设置 PYTHON 环境变量"
    exit 1
}

# ---------- 2. 探测 JDK (需 21+) ----------
if (-not $env:JAVA_HOME) {
    $javacObj = Get-Command javac -ErrorAction SilentlyContinue
    if ($javacObj) {
        $env:JAVA_HOME = Split-Path -Parent (Split-Path -Parent $javacObj.Source)
    } else {
        $searchDirs = @(
            "C:\Program Files\Java\jdk-21*",
            "C:\Program Files\Eclipse Adoptium\jdk-21*",
            "C:\Program Files\Microsoft\jdk-21*",
            "C:\Program Files\Zulu\zulu-21*",
            "C:\Program Files\BellSoft\LibericaJDK-21*",
            "C:\Program Files\Java\jdk-25*"
        )
        foreach ($pattern in $searchDirs) {
            $found = Get-Item $pattern -ErrorAction SilentlyContinue | Where-Object { Test-Path (Join-Path $_.FullName "bin\javac.exe") }
            if ($found) {
                $env:JAVA_HOME = $found[0].FullName
                break
            }
        }
    }
}
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME "bin\javac*"))) {
    Write-Error "[JARJS] 找不到 JDK 21+，请安装 JDK 并设置 JAVA_HOME 环境变量"
    exit 1
}
$env:PATH = (Join-Path $env:JAVA_HOME "bin") + [IO.Path]::PathSeparator + $env:PATH

# ---------- 3. 探测 Maven ----------
$MvnCmd = $null
if ($env:MVN -and (Test-Path $env:MVN)) {
    $MvnCmd = $env:MVN
} elseif ($env:MAVEN_HOME -and (Test-Path (Join-Path $env:MAVEN_HOME "bin\mvn.cmd"))) {
    $MvnCmd = Join-Path $env:MAVEN_HOME "bin\mvn.cmd"
} else {
    foreach ($cand in @("mvn.cmd", "mvn")) {
        $mObj = Get-Command $cand -ErrorAction SilentlyContinue
        if ($mObj) { $MvnCmd = $mObj.Source; break }
    }
}
if (-not $MvnCmd) {
    $mvnDirs = @("C:\Program Files\apache-maven*", "C:\apache-maven*")
    foreach ($pat in $mvnDirs) {
        $found = Get-Item $pat -ErrorAction SilentlyContinue | Where-Object { Test-Path (Join-Path $_.FullName "bin\mvn.cmd") }
        if ($found) { $MvnCmd = Join-Path $found[0].FullName "bin\mvn.cmd"; break }
    }
}
if (-not $MvnCmd) {
    Write-Error "[JARJS] 找不到 Maven (mvn)，请安装 Maven 并设置 PATH 或 MVN 环境变量"
    exit 1
}

# ---------- 4. 确保 shim 已编译 ----------
$ShimJar = Join-Path $JarjsHome "shim\target\shim-1.0.jar"
if (-not (Test-Path $ShimJar)) {
    Write-Host "[JARJS] 兼容层未构建，正在编译 shim..."
    Push-Location (Join-Path $JarjsHome "shim")
    & $MvnCmd -B -q clean package
    Pop-Location
}
& $MvnCmd -B -q install:install-file -Dfile="$ShimJar" -DgroupId=jarjs -DartifactId=shim -Dversion=1.0 -Dpackaging=jar | Out-Null

# ---------- 5. 校验输入与输出路径 ----------
if (-not (Test-Path $InputJar)) {
    Write-Error "[JARJS] 找不到输入 jar: $InputJar"
    exit 1
}
$InputAbs = (Resolve-Path $InputJar).Path
$OutputAbs = [System.IO.Path]::GetFullPath($Output)

# ---------- 6. 收集外部依赖 ----------
$ExtraJars = @()
if ($Libs) {
    if (-not (Test-Path $Libs)) {
        Write-Error "[JARJS] --libs 目录不存在: $Libs"
        exit 1
    }
    $ExtraJars += Get-ChildItem -Path $Libs -Filter "*.jar" | ForEach-Object { $_.FullName }
}
if ($Cp) {
    $parts = $Cp -split "[;:]"
    foreach ($p in $parts) {
        if ($p -and (Test-Path $p)) { $ExtraJars += (Resolve-Path $p).Path }
    }
}

$ExtraDepsXml = ""
$ExtraCpStr = ""
if ($ExtraJars.Count -gt 0) {
    Write-Host "[JARJS] 注入外部依赖 $($ExtraJars.Count) 个..."
    $idx = 0
    foreach ($j in $ExtraJars) {
        $idx++
        & $MvnCmd -B -q install:install-file -Dfile="$j" -DgroupId=jarjs.ext -DartifactId="lib$idx" -Dversion=1.0 -Dpackaging=jar | Out-Null
        $ExtraDepsXml += "<dependency><groupId>jarjs.ext</groupId><artifactId>lib$idx</artifactId><version>1.0</version></dependency>`n"
    }
    $ExtraCpStr = $ExtraJars -join [IO.Path]::PathSeparator
}

# ---------- 7. 静态报告模式 ----------
if ($Report) {
    Write-Host "[JARJS] 静态扫描全部符号引用（不执行转译）..."
    & $PyCmd (Join-Path $JarjsHome "bin\report.py") $InputAbs $ShimJar $Libs
    exit 0
}

# ---------- 8. 探测入口类 ----------
if (-not $MainClass) {
    $detectScript = @"
import zipfile, re
z = zipfile.ZipFile(r'$InputAbs')
if 'META-INF/MANIFEST.MF' in z.namelist():
    m = re.search(r'Main-Class:\s*([^\r\n]+)', z.read('META-INF/MANIFEST.MF').decode('utf-8', 'replace'))
    print(m.group(1).strip() if m else '')
"@
    $MainClass = (& $PyCmd -c $detectScript).Trim()
}
if (-not $MainClass) {
    Write-Error "[JARJS] 无法确定入口类，请使用 -MainClass 包名.类名 指定"
    exit 1
}
Write-Host "[JARJS] 入口类: $MainClass"

# ---------- 9. 临时工作目录 ----------
$Work = Join-Path ([System.IO.Path]::GetTempPath()) ("jarjs_" + [Guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Path $Work | Out-Null

try {
    Write-Host "[JARJS] 安装输入 jar..."
    & $MvnCmd -B -q install:install-file -Dfile="$InputAbs" -DgroupId=jarjs.input -DartifactId=app -Dversion=1.0 -Dpackaging=jar | Out-Null

    $ShadePom = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>jarjs.build</groupId><artifactId>build</artifactId><version>1.0</version><packaging>jar</packaging>
  <properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
  <dependencies>
    <dependency><groupId>jarjs.input</groupId><artifactId>app</artifactId><version>1.0</version></dependency>
    <dependency><groupId>jarjs</groupId><artifactId>shim</artifactId><version>1.0</version></dependency>
    <dependency><groupId>org.teavm</groupId><artifactId>teavm-classlib</artifactId><version>0.16.0</version><scope>provided</scope></dependency>
    <dependency><groupId>org.teavm</groupId><artifactId>teavm-jso-apis</artifactId><version>0.16.0</version><scope>provided</scope></dependency>
    $ExtraDepsXml
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId><artifactId>maven-shade-plugin</artifactId><version>3.5.1</version>
        <executions><execution><phase>package</phase><goals><goal>shade</goal></goals>
          <configuration>
            <createDependencyReducedPom>false</createDependencyReducedPom>
            <relocations>
              <relocation><pattern>org.lwjgl</pattern><shadedPattern>jarjs.lwjgl</shadedPattern></relocation>
              <relocation><pattern>javax.swing</pattern><shadedPattern>jarjs.swing</shadedPattern></relocation>
              <relocation><pattern>javax.imageio</pattern><shadedPattern>jarjs.imageio</shadedPattern></relocation>
              <relocation><pattern>javax.sound</pattern><shadedPattern>jarjs.sound</shadedPattern></relocation>
              <relocation><pattern>sun.misc.Unsafe</pattern><shadedPattern>jarjs.unsafe.Unsafe</shadedPattern></relocation>
              <relocation><pattern>java.awt</pattern><shadedPattern>jarjs.awt</shadedPattern></relocation>
              <relocation><pattern>java.util.concurrent.locks</pattern><shadedPattern>jarjs.juc.locks</shadedPattern></relocation>
              <relocation><pattern>java.util.concurrent.Executors</pattern><shadedPattern>jarjs.juc.Executors</shadedPattern></relocation>
              <relocation><pattern>java.util.concurrent.ExecutorService</pattern><shadedPattern>jarjs.juc.ExecutorService</shadedPattern></relocation>
              <relocation><pattern>java.util.concurrent.Future</pattern><shadedPattern>jarjs.juc.Future</shadedPattern></relocation>
              <relocation><pattern>java.util.concurrent.TimeUnit</pattern><shadedPattern>jarjs.juc.TimeUnit</shadedPattern></relocation>
            </relocations>
          </configuration>
        </execution></executions>
      </plugin>
    </plugins>
  </build>
</project>
"@
    Set-Content -Path (Join-Path $Work "pom.xml") -Value $ShadePom -Encoding utf8

    Push-Location $Work
    Write-Host "[JARJS] 重定位 + 打包..."
    & $MvnCmd -B -q package
    Pop-Location

    $Shaded = Join-Path $Work "target\build-1.0.jar"
    if (-not (Test-Path $Shaded)) {
        $cand = Get-ChildItem -Path (Join-Path $Work "target") -Filter "*.jar" | Where-Object { $_.Name -notmatch "original" } | Select-Object -First 1
        if ($cand) { $Shaded = $cand.FullName }
    }
    Write-Host "[JARJS] 安装重定位产物..."
    & $MvnCmd -B -q install:install-file -Dfile="$Shaded" -DgroupId=jarjs.relocated -DartifactId=app -Dversion=1.0 -Dpackaging=jar | Out-Null

    Write-Host "[JARJS] TeaVM 转译..."
    $ConvDir = Join-Path $Work "conv"
    New-Item -ItemType Directory -Path $ConvDir -Force | Out-Null
    New-Item -ItemType Directory -Path (Join-Path $ConvDir "src") -Force | Out-Null

    $ConvPom = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>jarjs.conv</groupId><artifactId>conv</artifactId><version>1.0</version><packaging>jar</packaging>
  <properties>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
  </properties>
  <dependencies>
    <dependency><groupId>jarjs.relocated</groupId><artifactId>app</artifactId><version>1.0</version></dependency>
    <dependency><groupId>jarjs</groupId><artifactId>shim</artifactId><version>1.0</version></dependency>
    <dependency><groupId>org.teavm</groupId><artifactId>teavm-classlib</artifactId><version>0.16.0</version><scope>provided</scope></dependency>
    <dependency><groupId>org.teavm</groupId><artifactId>teavm-jso-apis</artifactId><version>0.16.0</version><scope>provided</scope></dependency>
    $ExtraDepsXml
  </dependencies>
  <build><plugins><plugin>
    <groupId>org.teavm</groupId><artifactId>teavm-maven-plugin</artifactId><version>0.16.0</version>
    <executions><execution><id>main</id><phase>package</phase><goals><goal>compile</goal></goals>
      <configuration>
        <mainClass>$MainClass</mainClass>
        <targetDirectory>`${project.build.directory}/teavm</targetDirectory>
        <targetType>JAVASCRIPT</targetType>
        <optimizationLevel>ADVANCED</optimizationLevel>
        <debugInformationGenerated>false</debugInformationGenerated>
        <sourceMapsGenerated>false</sourceMapsGenerated>
      </configuration>
    </execution></executions>
  </plugin></plugins></build>
</project>
"@
    Set-Content -Path (Join-Path $ConvDir "pom.xml") -Value $ConvPom -Encoding utf8

    Push-Location $ConvDir
    & $MvnCmd -B -q package 2>&1 | Out-Null
    Pop-Location

    $JsFile = Join-Path $ConvDir "target\teavm\classes.js"
    if (-not (Test-Path $JsFile) -or (Get-Item $JsFile).Length -eq 0) {
        Write-Host "[JARJS] 转译未产出有效 JS（类库缺失）"
        if ($Auto) {
            Write-Host "[JARJS] 全自动补桩：递归补依赖类，循环到通过..."
            $fullCp = $Shaded + ($ExtraCpStr ? ([IO.Path]::PathSeparator + $ExtraCpStr) : "")
            $autoArgs = @((Join-Path $JarjsHome "bin\autostub.py"), $Work, $MainClass, "--cp", $fullCp)
            if ($Offline) { $autoArgs += "--offline" }
            & $PyCmd @autoArgs
        } elseif ($Lenient) {
            Write-Host "[JARJS] 宽松插桩模式：尝试补桩后重试..."
            if ($Offline) { $env:JARJS_OFFLINE = "1" }
            & $PyCmd (Join-Path $JarjsHome "bin\lenient.py") $Work $MainClass
        }
    }

    if (-not (Test-Path $JsFile) -or (Get-Item $JsFile).Length -eq 0) {
        Write-Error "[JARJS] 失败: 产物为空"
        exit 2
    }

    Write-Host "[JARJS] 扫描并内嵌资源（base64）..."
    $AssetsJs = Join-Path $Work "assets.js"
    & $PyCmd (Join-Path $JarjsHome "bin\pack_assets.py") $Shaded $AssetsJs

    Write-Host "[JARJS] 组装单文件 HTML..."
    & $PyCmd (Join-Path $JarjsHome "bin\assemble.py") $JsFile $OutputAbs $Width $Height $MainClass $AssetsJs

    $resFile = Get-Item $OutputAbs
    Write-Host "[JARJS] 完成: $($resFile.FullName) ($($resFile.Length) 字节)"
}
finally {
    if (Test-Path $Work) {
        Remove-Item -Path $Work -Recurse -Force -ErrorAction SilentlyContinue
    }
}

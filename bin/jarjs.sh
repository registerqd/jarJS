#!/usr/bin/env bash
# JARJS —— 把 Java jar 一键转成浏览器可运行的单文件 HTML
# 用法: jarjs build app.jar -o app.html [--main 包名.类名] [--lenient]
set -euo pipefail

JARJS_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# JDK：优先用环境变量，其次探测常见安装位置，最后用 PATH 里的 javac
if [ -z "${JAVA_HOME:-}" ] || [ ! -x "${JAVA_HOME}/bin/javac" ]; then
  if [ -x "/data/jdk-21.0.13+9/bin/javac" ]; then
    JAVA_HOME="/data/jdk-21.0.13+9"
  elif command -v javac >/dev/null 2>&1; then
    JAVA_HOME="$(dirname "$(dirname "$(readlink -f "$(command -v javac)")")")"
  else
    echo "[JARJS] 找不到 JDK 21+，请先安装并设置 JAVA_HOME" >&2
    exit 1
  fi
fi
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

# Maven：环境变量 > 常见位置 > PATH
if [ -z "${MVN:-}" ]; then
  if [ -x "/data/apache-maven-3.9.16/bin/mvn" ]; then
    MVN="/data/apache-maven-3.9.16/bin/mvn"
  elif command -v mvn >/dev/null 2>&1; then
    MVN="$(command -v mvn)"
  else
    echo "[JARJS] 找不到 Maven，请先安装并设置 MVN" >&2
    exit 1
  fi
fi

SHIM_JAR="$JARJS_HOME/shim/target/shim-1.0.jar"
if [ ! -f "$SHIM_JAR" ]; then
  echo "[JARJS] 兼容层未构建，正在编译 shim..."
  (cd "$JARJS_HOME/shim" && "$MVN" -B -q package)
fi

# 每次都把当前兼容层装进本地仓库，避免 shade 拿到旧版本
"$MVN" -B -q install:install-file -Dfile="$SHIM_JAR" \
  -DgroupId=jarjs -DartifactId=shim -Dversion=1.0 -Dpackaging=jar >/dev/null

cmd="${1:-}"; shift || true

if [ "$cmd" != "build" ]; then
  cat <<'USAGE'
JARJS v0.1 —— Java jar 到网页的一键转译器

用法:
  jarjs build app.jar -o app.html [选项]

选项:
  -o, --output <file>    输出文件（.html 单文件 或 .js）
  --main <class>         指定入口类（默认读取 jar 的 Main-Class）
  --lenient              宽松插桩：缺类自动补桩，不中断编译
  --width <px>           画布宽（默认 480）
  --height <px>          画布高（默认 480）
  -h, --help             显示帮助
USAGE
  exit 0
fi

INPUT=""; OUTPUT="app.html"; MAIN=""; LENIENT=0; W=480; H=480
while [ $# -gt 0 ]; do
  case "$1" in
    -o|--output) OUTPUT="$2"; shift 2 ;;
    --main) MAIN="$2"; shift 2 ;;
    --lenient) LENIENT=1; shift ;;
    --width) W="$2"; shift 2 ;;
    --height) H="$2"; shift 2 ;;
    -h|--help) exec "$0" ;;
    *) INPUT="$1"; shift ;;
  esac
done

[ -f "$INPUT" ] || { echo "[JARJS] 找不到输入 jar: $INPUT"; exit 1; }

# 输出路径绝对化（后面会 cd 到临时构建目录）
OUT_DIR="$(cd "$(dirname "$OUTPUT")" && pwd)"
OUTPUT="$OUT_DIR/$(basename "$OUTPUT")"

if [ -z "$MAIN" ]; then
  MAIN=$(unzip -p "$INPUT" META-INF/MANIFEST.MF 2>/dev/null | tr -d '\r' | awk -F': ' '/^Main-Class/{print $2; exit}')
fi
[ -n "$MAIN" ] || { echo "[JARJS] 无法确定入口类，请用 --main 指定"; exit 1; }
echo "[JARJS] 入口类: $MAIN"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

echo "[JARJS] 安装输入 jar..."
"$MVN" -B -q install:install-file -Dfile="$INPUT" \
  -DgroupId=jarjs.input -DartifactId=app -Dversion=1.0 -Dpackaging=jar >/dev/null

cat > "$WORK/pom.xml" <<POM
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>jarjs.build</groupId>
  <artifactId>build</artifactId>
  <version>1.0</version>
  <packaging>jar</packaging>
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
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.5.1</version>
        <executions><execution><phase>package</phase><goals><goal>shade</goal></goals>
          <configuration>
            <createDependencyReducedPom>false</createDependencyReducedPom>
            <relocations>
              <relocation><pattern>org.lwjgl</pattern><shadedPattern>jarjs.lwjgl</shadedPattern></relocation>
              <relocation><pattern>javax.swing</pattern><shadedPattern>jarjs.swing</shadedPattern></relocation>
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
POM

cd "$WORK"
echo "[JARJS] 重定位 + 打包..."
"$MVN" -B -q package >/dev/null 2>&1 || { echo "[JARJS] 打包失败"; exit 1; }

SHADED="$WORK/target/build-1.0.jar"
[ -f "$SHADED" ] || SHADED=$(ls "$WORK"/target/*.jar | grep -v original | head -1)
echo "[JARJS] 安装重定位产物: $(basename "$SHADED")"
"$MVN" -B -q install:install-file -Dfile="$SHADED" \
  -DgroupId=jarjs.relocated -DartifactId=app -Dversion=1.0 -Dpackaging=jar >/dev/null

echo "[JARJS] TeaVM 转译..."
mkdir -p conv
cat > conv-pom.xml <<POM
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
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
  </dependencies>
  <build><plugins><plugin>
    <groupId>org.teavm</groupId><artifactId>teavm-maven-plugin</artifactId><version>0.16.0</version>
    <executions><execution><id>main</id><phase>package</phase><goals><goal>compile</goal></goals>
      <configuration>
        <mainClass>${MAIN}</mainClass>
        <targetDirectory>\${project.build.directory}/teavm</targetDirectory>
        <targetType>JAVASCRIPT</targetType>
        <optimizationLevel>ADVANCED</optimizationLevel>
        <debugInformationGenerated>false</debugInformationGenerated>
        <sourceMapsGenerated>false</sourceMapsGenerated>
      </configuration>
    </execution></executions>
  </plugin></plugins></build>
</project>
POM
cp conv-pom.xml conv/pom.xml
mkdir -p conv/src
(cd conv && "$MVN" -B -q package 2>&1 | tail -25) || true
JS="$WORK/conv/target/teavm/classes.js"
if [ ! -s "$JS" ]; then
  echo "[JARJS] 转译未产出有效 JS（类库缺失）"
  if [ "$LENIENT" = "1" ]; then
    echo "[JARJS] 宽松插桩模式：尝试补桩后重试..."
    python3 "$JARJS_HOME/bin/lenient.py" "$WORK" "$MAIN" || true
  fi
  [ -s "$JS" ] || { echo "[JARJS] 失败：产物为空"; exit 2; }
fi

echo "[JARJS] 扫描并内嵌资源（base64）..."
ASSETS_JS="$WORK/assets.js"
python3 "$JARJS_HOME/bin/pack_assets.py" "$SHADED" "$ASSETS_JS"

echo "[JARJS] 组装单文件 HTML..."
python3 "$JARJS_HOME/bin/assemble.py" "$JS" "$OUTPUT" "$W" "$H" "$MAIN" "$ASSETS_JS"
echo "[JARJS] 完成: $OUTPUT ($(wc -c < "$OUTPUT") 字节)"

# jarJS v0.6

把 Java jar 一键转成浏览器可运行单文件 HTML 的现代化工具链。

支持 **双渲染管线**：既支持经典 OpenGL 1.1 固定管线（立即模式、矩阵栈、逐顶点光照），也支持现代 **OpenGL 2.0+ 可编程着色器管线**（GLSL 自定义顶点/片段着色器、VBO/IBO 顶点缓冲、动态 Uniform、FBO 离屏渲染）。

---

## v0.6：按真实工业应用实测的 386 条报错逐项修复

这一版不是猜的，是把一个真实大型 Java 3D 客户端跑出来的报错（去重 169 个符号）逐类补齐后的结果。

### 一、架构级修复（CLI）

| 问题 | 修复 |
|---|---|
| CLI 只接受单个 jar，几十个外部库喂不进去 | **`--libs <dir>` / `--cp a.jar:b.jar`**：自动 install 进 Maven 本地仓库并注入 shade/converter 两个 pom |
| TeaVM 遇错即停，永远看不到全貌 | **`--report`**：`bin/report.py` 直接解析 class 常量池，一次性输出全部缺失类，按包分组统计，不跑转译 |
| 单机游戏被迫带上整条网络/加密链 | **`--offline`**：一键桩掉 `java.nio.channels` / `java.net` / `java.security` / `io.netty` |
| 资源内嵌没进 build 流程 | `build` 末尾自动调用 `pack_assets.py`，产物直接内嵌（v0.4 已接，v0.6 起在 `--report` 之外全自动） |

```bash
# 真实大型项目的推荐跑法
bash bin/jarjs.sh build app.jar -o app.html --libs ./libs --offline --lenient

# 先看缺口，不浪费时间
bash bin/jarjs.sh build app.jar --report --libs ./libs
```

### 二、shim API 补齐（按报错逐条）

| 模块 | 补齐内容 |
|---|---|
| **GL11** | `glFog/glFogi/glFogf`、`glGetInteger/glGetFloat`、`glReadPixels`、`glGetTexImage`、`glMultMatrix`、`glTexGen/glTexGeni`、`glTexEnv/glTexEnvfv`、`glPolygonOffset`、`glLogicOp`、`glCopyTexSubImage2D`、`glClearAccum/glAccum`、`glStencilFunc/Op/Mask`、顶点数组 `Buffer` 重载 |
| **ARB/EXT** | 新增 `ARBFramebufferObject`、`EXTFramebufferObject`、`ARBShaderObjects`、`ARBVertexShader`、`ARBVertexBufferObject`、`ARBMultitexture`、`EXTBlendFuncSeparate` 七个兼容类 |
| **GL20** | `glUniform1/2/3/4` 的 `IntBuffer` / `FloatBuffer` 重载、`glUniform1fv/4fv(int,int,Buffer)` |
| **GL30** | `glGenRenderbuffers`、`glBindRenderbuffer`、`glRenderbufferStorage`、`glFramebufferRenderbuffer`、`glDeleteRenderbuffers` |
| **Unsafe** | `getIntVolatile`、`getLong/getLongVolatile`、`getObject/getObjectVolatile`、`putObjectVolatile`、`putOrderedInt/Long`、`throwException`、`allocateInstance` |
| **net** | `Proxy`（含 `NO_PROXY`）、`Authenticator`、`PasswordAuthentication`、`InetAddress`、`InetSocketAddress` |
| **awt** | 新增 `Image`（修掉 `Graphics.drawImage` 的悬空引用）、`Graphics.drawImage/dispose/create` |
| **sound.midi** | `Synthesizer`、`MidiChannel`、`ShortMessage`、`MidiDevice` |
| **Display / Mouse** | `getDesktopDisplayMode`、`setIcon`、`wasResized`、`isInsideWindow`、`getEventDWheel` |
| **imageio** | `setUseCache/getUseCache`、`write` |
| **ContextCapabilities** | 补 `GL_ARB_arrays_of_arrays`、`GL_ARB_texture_fog_distance` 等 12 个字段，全部预置 `true` |

### 三、已知边界（不吹牛）

- `java.util.concurrent` / `java.nio.channels` / `java.lang.Class` 反射等 **JDK 内部类无法用 shim 覆盖**（Java 模块系统禁止自定义 `java.*`），只能靠 `--offline` + `--lenient` 生成桩；桩能保证不崩，但不保证行为正确。
- 自动补桩必须先解析参数类型、递归补依赖类，只补方法签名会在下一次编译时炸（v0.6 已把这个约束写进 `lenient.py` 注释）。
- 自定义 GLSL 之外的桌面扩展（几何着色器、Compute Shader）不在射程内。

---


## 针对工业大作与复杂桌面游戏（Minecraft 1.8 / Netty / LibGDX 等）的四项基础设施更新 (v0.5)

| 核心痛点 | 桌面原生依赖 | jarJS 解决方案 |
|---|---|---|
| **网络层 (Netty)** | `sun.misc.Unsafe`（堆外直接内存分配）与 `Selector` / `SocketChannel`（系统 Socket） | **虚拟页表内存分配器 (`jarjs.unsafe.Unsafe`)**：纯 Java 实现 `allocateMemory` / `copyMemory` / `getByte` / `putByte`，解除 Netty PlatformDependent 崩溃；搭配浏览器原生 **WebSocket 二进制网络桥** |
| **音效系统 (Paulscode)** | `javax.sound.sampled.AudioFormat`、`Clip`、`AudioInputStream` 与 MIDI 背景音乐 | **Web Audio 直通 (`jarjs.sound.sampled.*`)**：JavaSound 与 Paulscode 指令直通 Web Audio API，包含 MIDI 兼容桩 |
| **贴图动态解码** | `javax.imageio.ImageIO` 与 `BufferedImage`（同步读取贴图） | **纯 Java 零依赖 PNG 解码器 (`jarjs.imageio.ImageIO`)**：直接在内存中基于 Inflater 解压解析 IHDR/IDAT，支持 `BufferedImage.getRGB` / `getSubimage` 同步读取 |
| **渲染扩展查询** | `GLContext.getCapabilities()`（检查 OpenGL 1.4~3.0 与 FBO/ARB 特性） | **特性常量全置 true (`ContextCapabilities`)**：预置 `OpenGL14`, `OpenGL20`, `GL_ARB_framebuffer_object` 等全部为 `true`，防止启动硬件检测直接闪退 |

---

## 核心设计理念：让代码始终保持高可读性

jarJS 从立项之初就坚持一个原则：**不做难以理解的黑盒混淆，输出结构规整、模块分明、人类可读的高质量工程代码。**

- **Java 侧 API 与 LWJGL / JDK 官方完全对齐**：所有方法签名、参数顺序、常量命名严格遵循规范，代码带有完整的 Javadoc 与参数解释。
- **纯增量、清晰的桥接分层**：
  - `shim/src/main/java/jarjs/lwjgl/opengl/`：GL11, GL13, GL14, GL15, GL20, GL30, GLContext；
  - `shim/src/main/java/jarjs/unsafe/`：纯 Java 实现的内存分配页表；
  - `shim/src/main/java/jarjs/imageio/`：纯 Java 同步贴图解码器；
  - `shim/src/main/java/jarjs/sound/`：JavaSound 到 Web Audio 直通；
  - `runtime/jarjs-gl.js` & `runtime/jarjs-media.js`：运行在浏览器的 WebGL 与 Web Audio 驱动内核。
- **资源自动化 Base64 流水线**：构建时 `bin/pack_assets.py` 扫描 jar 内所有贴图、音频、字体，转成 Base64 内嵌进 HTML，运行时纯内存读取，**零网络请求、零 Native 依赖**。

---

## 快速上手

### 环境要求
- **JDK 21+**
- **Maven 3.9+**
- **Python 3**

### 基本用法

```bash
# 1. 编译并转译你的 Java JAR
bash bin/jarjs.sh build mygame.jar -o mygame.html --width 800 --height 600

# 2. 指定入口主类与画布尺寸
bash bin/jarjs.sh build mygame.jar -o mygame.html --main com.demo.Main --width 760 --height 560

# 3. 启用宽松插桩（遇到未知第三方类库自动安全补桩，不中断转译）
bash bin/jarjs.sh build mygame.jar -o mygame.html --lenient
```

产物为一个**自包含的单文件 `.html`**，双击或部署到任意静态 Web 服务器即可运行。

---

## 在线演示 (Live Demos)

| 演示项目 | 核心技术 | 体验入口 |
|---|---|---|
| **VoxelCraft 3D 体素沙盒** | 第一人称 3D 漫游、体素地形、挖方块、放方块、OpenAL 音效 | [在线体验](docs/voxel-gl.html) |
| **GL20 等离子体动态着色器** | 自定义 GLSL Vertex/Fragment Shader、VBO 缓冲、动态 Uniform | [在线体验](docs/shader-gl.html) |
| **经典固定管线立方体** | 立即模式、三套矩阵栈、逐顶点 Blinn-Phong 光照 (8 盏灯) | [在线体验](docs/cube-gl.html) |
| **全栈多媒体演示** | Base64 资源内嵌、Web Audio 播放、Canvas2D 矢量文字直传 | [在线体验](docs/media-gl.html) |

---

## 目录结构

```
jarJS/
├── bin/
│   ├── jarjs.sh          # 主构建 CLI（自动探测环境，集成 Shade 重定位）
│   ├── assemble.py       # 将 TeaVM 输出与 WebGL/WebAudio 内核组装为单文件 HTML
│   ├── pack_assets.py    # 扫描 jar 并将资源打包为 Base64 内嵌表
│   └── lenient.py        # 宽松插桩引擎（两级缺失类/方法自动补桩）
├── shim/                 # 兼容层 Java 源码
│   └── src/main/java/jarjs/
│       ├── lwjgl/opengl/ # GL11, GL13, GL14, GL15, GL20, GL30, GLContext
│       ├── unsafe/       # sun.misc.Unsafe 内存分配页表
│       ├── imageio/      # ImageIO 与纯 Java PNG 同步解码器
│       ├── sound/        # javax.sound.sampled 与 MIDI 模块
│       ├── net/          # WebSocket 二进制网络桥
│       └── font/         # Canvas2D 矢量文字光栅化
├── runtime/
│   ├── jarjs-gl.js       # 双管线 WebGL 驱动内核
│   └── jarjs-media.js    # Web Audio、Image、Canvas 驱动
├── docs/                 # GitHub Pages 在线演示站点
├── demo-voxel/           # 3D 体素微型沙盒工程
├── demo-shader/          # 现代 GL20 着色器演示工程
├── demo-gl/              # 经典固定管线演示工程
├── demo-media/           # 多媒体系统演示工程
├── LICENSE               # MIT 开源协议
└── README.md
```

## 协议
本项目遵循 MIT 开源协议。

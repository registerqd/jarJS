# jarJS v0.3

把 Java jar 一键转成浏览器可运行单文件 HTML 的现代化工具链。

支持 **双渲染管线**：既支持经典 OpenGL 1.1 固定管线（立即模式、矩阵栈、逐顶点光照），也支持现代 **OpenGL 2.0+ 可编程着色器管线**（GLSL 自定义顶点/片段着色器、VBO/IBO 顶点缓冲、动态 Uniform、FBO 离屏渲染）。

---

## 核心设计理念：让代码始终保持高可读性

jarJS 从立项之初就坚持一个原则：**不做难以理解的黑盒混淆，输出结构规整、模块分明、人类可读的高质量工程代码。**

- **Java 侧 API 与 LWJGL 官方完全对齐**：所有方法签名、参数顺序、常量命名严格遵循 LWJGL 2.9.3 规范，代码带有完整的 Javadoc 与参数解释。无论是维护固定管线还是写 GLSL 着色器，代码逻辑清晰自然。
- **纯增量、清晰的桥接分层**：
  - `shim/src/main/java/jarjs/lwjgl/opengl/`：Java 侧统一 API 入口，提供类型安全和缓冲区打包；
  - `runtime/jarjs-gl.js`：运行在浏览器的 WebGL 调度内核，未做任何混淆压缩，每个功能段均有清晰的模块注释，随时可以阅读、二次开发和排查。
- **透明的自动降级预处理**：桌面游戏里的 GLSL（如 `#version 120`、缺少 `precision` 声明）在进入 WebGL 前自动完成合规性转换，保证运行效率的同时降低移植门槛。

---

## 架构

```
                   用户编写的 Java 桌面游戏 / 工具 (JAR)
                                     │
                 [maven-shade 重定位：org.lwjgl → jarjs.lwjgl]
                                     │
        ┌────────────────────────────┴────────────────────────────┐
        ▼                                                         ▼
【经典固定管线 (GL 1.1)】                                【现代着色器管线 (GL 2.0+)】
  - 矩阵栈 (ModelView / Proj / Tex)                        - 自定义 GLSL Vertex / Fragment Shader
  - 立即模式 (glBegin / glEnd)                             - VBO / IBO 顶点缓冲 (GL15)
  - 逐顶点 Blinn-Phong 光照 (8 盏灯)                       - 动态 Uniform 交互 (矩阵 / 浮点 / 向量)
  - 图元自动三角化 (QUADS / POLYGON)                        - 顶点属性指针 (glVertexAttribPointer)
  - 顶点数组与显示列表                                     - 多重纹理 (GL13) 与高级混合 (GL14)
        │                                                         │
        └────────────────────────────┬────────────────────────────┘
                                     ▼
                      GLBackend (@JSBody 跨语言直通桥)
                                     ▼
                   runtime/jarjs-gl.js (原生 WebGL 1.0 驱动)
                                     ▼
                    浏览器单文件 HTML (Canvas + WebGL)
```

---

## 快速上手

### 环境要求
- **JDK 21+**（编译支持 Java 21+ 现代语法与记录类型）
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

## 内置验证工程（Demos）

仓库内置了两个经过真实浏览器自动化回归测试的高质量工程：

### 1. 经典固定管线立方体 (`demo-gl`)
- **技术栈**：LWJGL 2.9.3、立即模式、光照、棋盘格生成纹理、透视投影矩阵、键盘移动相机。
- **源码**：`demo-gl/src/main/java/demo/CubeDemo.java`
- **运行效果**：三维光照立方体在棋盘地面上自由旋转，键盘实时操控，60 FPS。

### 2. 现代 GLSL 等离子体着色器 (`demo-shader`)
- **技术栈**：LWJGL 2.9.3、GL20 自定义顶点/片段着色器、GL15 顶点缓冲对象 (VBO)、动态 Uniform 传递时间与分辨率、全屏 Quad 渲染。
- **源码**：`demo-shader/src/main/java/demo/ShaderDemo.java`
- **运行效果**：全屏数学动态等离子体彩虹色轮流动，中央脉动发光光环，键盘空格暂停/继续，左右方向键变速。

---

## 支持的 API 矩阵

| 模块 | 核心 API / 常量 | 说明 |
|---|---|---|
| **GL20 (着色器)** | `glCreateShader`, `glShaderSource`, `glCompileShader`, `glCreateProgram`, `glAttachShader`, `glLinkProgram`, `glUseProgram`, `glGetUniformLocation`, `glUniform*`, `glGetAttribLocation`, `glEnableVertexAttribArray`, `glVertexAttribPointer` | 原生 WebGL 1:1 直通，带 GLSL 自动预处理 |
| **GL15 (缓冲对象)** | `glGenBuffers`, `glBindBuffer`, `glBufferData`, `glBufferSubData`, `glDeleteBuffers`, `GL_ARRAY_BUFFER`, `GL_ELEMENT_ARRAY_BUFFER` | 原生 WebGLBuffer 支持 |
| **GL14 (混合高级)** | `glBlendFuncSeparate`, `glBlendEquation`, `GL_FUNC_ADD`, `GL_FUNC_SUBTRACT` | 混合方程与通道分离 |
| **GL13 (多重纹理)** | `glActiveTexture`, `GL_TEXTURE0` ~ `GL_TEXTURE31` | 多纹理单元切换 |
| **GL11 (固定管线)** | `glBegin`, `glEnd`, `glVertex*`, `glColor*`, `glNormal*`, `glTexCoord*`, `glMatrixMode`, `glPushMatrix`, `glPopMatrix`, `glLight*`, `glMaterial*`, `glEnableClientState`, `glDrawArrays` | Java 侧完整固定管线仿真 |
| **GL30 (帧缓冲)** | `glGenFramebuffers`, `glBindFramebuffer`, `glFramebufferTexture2D`, `glCheckFramebufferStatus` | 离屏渲染与后处理支持 |
| **LWJGL 外壳** | `Display` (create/update/sync), `DisplayMode`, `Keyboard`, `Mouse`, `Timer`, `Sys`, `BufferUtils` | 桌面事件与时钟循环映射到浏览器事件循环 |

---

## 边界与演进方向

1. **GLSL 规范**：当前原生对标 WebGL 1.0（OpenGL ES 2.0）。高版本特有语法（如 3.0+ 的多重渲染目标 MRT、Compute Shader）不在本阶段射程内。
2. **大作适配路线**：
   - 经典固定管线游戏（Quake 2 / Jake2 风格）已完全具备运行条件；
   - 基于 libGDX / 自定义着色器的现代独立游戏，基础着色器管线已就绪，后续将逐步完善音频 OpenAL 桥接与输入系统。

---

## 目录结构

```
jarJS/
├── bin/
│   ├── jarjs.sh          # 主构建 CLI 入口（自动探测 JDK 21+ 与 Maven）
│   ├── assemble.py       # 将 TeaVM 输出与 WebGL 内核组装为单文件 HTML
│   └── lenient.py        # 宽松插桩引擎（两级缺失类/方法自动补桩）
├── shim/                 # 兼容层 Java 源码
│   └── src/main/java/jarjs/lwjgl/
│       ├── opengl/       # GL11, GL13, GL14, GL15, GL20, GL30 核心实现
│       ├── input/        # Keyboard, Mouse 键鼠事件模拟
│       └── util/         # GLU, Timer, BufferUtils 工具库
├── runtime/
│   └── jarjs-gl.js       # 浏览器 WebGL 运行时驱动（双管线核心，结构清晰可读）
├── demo-gl/              # 固定管线立方体验证工程
├── demo-shader/          # 现代可编程着色器验证工程
├── demo2/                # Swing 俄罗斯方块验证工程
├── LICENSE               # MIT 开源协议
└── README.md
```

## 协议
本项目遵循 MIT 开源协议。

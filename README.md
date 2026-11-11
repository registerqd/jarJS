# JARJS v0.2

把 Java jar 一键转成浏览器可运行单文件 HTML 的工具链。v0.2 新增 **OpenGL 1.x 固定管线 → WebGL** 完整兼容层，LWJGL 2 的桌面程序可以原样转译。

## 这是什么

TeaVM 的字节码解析器和 JS 生成器本身很强，卡人的是两处：**机制洁癖**（一处缺类就 0 字节罢工）和**类库贫血**（classlib 停在 Java 8，还硬啃多线程语义）。JARJS 不动编译器内核，只在外面做四层手术：JUC 直通、Swing/AWT 兼容层、OpenGL/WebGL 兼容层、宽松插桩。

## 用法

```bash
# 基本用法
bash bin/jarjs.sh build app.jar -o app.html

# 指定入口类 + 画布尺寸
bash bin/jarjs.sh build app.jar -o app.html --main com.demo.Main --width 640 --height 480

# 宽松插桩：缺类自动补桩，不中断整包编译
bash bin/jarjs.sh build app.jar -o app.html --lenient
```

依赖：JDK 21、Maven 3.9（路径可用 `JAVA_HOME` / `MVN` 环境变量覆盖）。

## OpenGL → WebGL 兼容层（v0.2 新增）

### 架构

```
用户 jar（org.lwjgl.* 调用）
   │  maven-shade 重定位：org.lwjgl → jarjs.lwjgl
   ▼
jarjs.lwjgl.opengl.GL11 / GLU / Display …（固定管线语义，Java 侧）
   │  矩阵、光照、图元展开全部在 Java 里算完
   ▼
GLBackend（@JSBody 桥）
   ▼
runtime/jarjs-gl.js（WebGL1：一个 program、交错顶点缓冲、纹理）
```

固定管线语义放在 Java 侧，是为了让「立即模式 + 矩阵栈 + 逐顶点光照」的 OpenGL 语义保持完整；JS 侧只做 WebGL 的搬运，不掺业务逻辑。

### 已实现

| 类别 | 内容 |
|---|---|
| 矩阵 | `glMatrixMode` / `LoadIdentity` / `PushMatrix` / `PopMatrix` / `Translate` / `Rotate` / `Scale` / `MultMatrix` / `LoadMatrix` / `Ortho` / `Frustum`，MODELVIEW / PROJECTION / TEXTURE 三套独立栈 |
| 立即模式 | `glBegin` / `glEnd` / `glVertex*` / `glColor*` / `glNormal*` / `glTexCoord*`，支持 POINTS / LINES / LINE_LOOP / LINE_STRIP / TRIANGLES / TRIANGLE_STRIP / TRIANGLE_FAN / QUADS / QUAD_STRIP / POLYGON |
| 光照 | `GL_LIGHT0`~`GL_LIGHT7` 开关，AMBIENT / DIFFUSE / SPECULAR / POSITION（方向光与点光）、常数/线性/二次衰减；材质 AMBIENT / DIFFUSE / SPECULAR / EMISSION / SHININESS；`GL_COLOR_MATERIAL`；逐顶点 Blinn-Phong |
| 纹理 | `glGenTextures` / `BindTexture` / `DeleteTextures` / `TexParameteri` / `TexImage2D`（RGBA / RGB / BGR(A) / LUMINANCE / ALPHA，`int[]`、`ByteBuffer`、`IntBuffer` 三种入口）/ 纹理矩阵 / REPEAT / CLAMP |
| 顶点数组 | `glEnableClientState` / `glVertexPointer` / `glColorPointer` / `glTexCoordPointer` / `glDrawArrays` / `glDrawElements` |
| 显示列表 | `glNewList` / `glEndList` / `glCallList` / `glDeleteLists` |
| 状态 | `glEnable/Disable`（深度、混合、剔除、光照、纹理、法线归一）、`glBlendFunc`、`glDepthFunc`、`glDepthMask`、`glCullFace`、`glFrontFace`、`glShadeModel`（平滑/平面）、`glPolygonMode`（填充/线框）、`glLineWidth`、`glPointSize`、`glPushAttrib/PopAttrib` |
| 其他 | `glClear` / `glClearColor` / `glViewport` / `glGetString` / `glGetError` / `glFlush` / `glHint` / `glPixelStorei` |
| GLU | `gluPerspective` / `gluLookAt` / `gluOrtho2D` / `gluErrorString` |
| LWJGL 外壳 | `Display`（create / update / swapBuffers / sync / isCloseRequested / destroy）、`DisplayMode`、`PixelFormat`、`Keyboard`（LWJGL 键码全集 + 事件队列）、`Mouse`、`Timer`、`Sys`、`BufferUtils`、`LWJGLException` |

### 帧循环为什么能跑

LWJGL 的写法是 `while(!Display.isCloseRequested()){ ... Display.update(); }`，浏览器不允许阻塞主线程。实测 TeaVM 下 `Thread.sleep` 会让出到事件循环（2 秒内 rAF 跑了 124 帧），所以 `Display.update()` 直接按 60 FPS 目标 sleep 剩余时间，浏览器在让出期间合成画面。**用户代码一行都不用改。**

### 验证

`demo-gl` 是一个标准 LWJGL 2.9.3 程序（固定管线、光照、纹理、键盘），未经任何修改直接转译：

- 产物 56 KB 单文件 HTML，760×560
- 非背景像素占 53.5%，68 种颜色（立方体六面 + 棋盘地面 + 旋转纹理平面）
- 61 FPS，键鼠事件（`isKeyDown` / 按下释放 / 鼠标按键）全部正确
- `glGetError()` 为 0，零 JS 异常

### 边界（不吹牛）

- WebGL1 没有固定管线，**着色器是内置的**：顶点色 × 纹理，不支持自定义 GLSL、多重纹理、着色器程序。
- 不支持模板缓冲、mipmap 链（纹理只上传 level 0）、多重采样（canvas 自带抗锯齿）、FBO、VBO 语义、`glReadPixels`。
- 光照只有 Blinn-Phong 近似，聚光灯（SPOT_*）参数接受但不参与计算。
- 立即模式每个 `glBegin/glEnd` 是一次 draw call，几何量很大的老程序（每帧上万次 begin/end）会明显掉帧；建议改用顶点数组或显示列表。
- 依赖桌面 OpenGL 之外的东西（音频 OpenAL、控制器 JInput、原生 LWJGL native）仍然不提供，`--lenient` 会补空桩。
- 3D 游戏可以跑，但「完整大作」级别的引擎（自定义着色器、后处理、骨骼动画）不在这一层的射程内。

## 其他兼容层

| 手术 | 状态 | 说明 |
|---|---|---|
| JUC 单线程直通 | 已实现 | `ReentrantLock` 只维护「持有者+重入计数」状态机；`LockSupport.park/unpark` 空转；`Executors.newVirtualThreadPerTaskExecutor()` 降级为立即执行的微任务队列 |
| Swing/AWT 兼容层 | 已实现 | `JFrame`→canvas、`JPanel`/`JLabel`、`Graphics`→Canvas2D、`Timer`→setInterval、键盘事件分发 |
| 宽松插桩 | 已实现（类级 + 方法级） | 从 TeaVM 错误解析缺失类和方法签名，自动生成桩类/桩方法并重试，最多 20 轮；桩被调用时打印 `[JARJS Stub]` 但不中断主流程 |

## 目录

```
bin/jarjs.sh        CLI 入口（含 org.lwjgl → jarjs.lwjgl 重定位）
bin/assemble.py     JS → 单文件 HTML（自动内联 WebGL 运行时）
bin/lenient.py      宽松插桩
shim/               Swing/AWT/JUC/OpenGL/LWJGL 兼容层源码
runtime/jarjs-gl.js WebGL 运行时
demo-gl/            LWJGL 2.9.3 立方体 demo（源码 + pom）
demo2/              Swing 俄罗斯方块
```

## 已知坑

1. **shim 必须先 install 到本地仓库**，否则 shade 会拿到旧版本（CLI 已自动处理）。
2. 重定位规则只能写 `org.lwjgl` 一条，不能拆成 `org.lwjgl.opengl` 等多条——Maven Shade 会把前一条重定位后的结果再匹配一次，产出 `jarjs.lwjgl.lwjgl.*`。
3. `GL_QUADS` 在 WebGL 里不存在，Java 侧统一展开成两个三角形；`GL_POLYGON` 展开成三角扇。
4. 背面剔除依赖顶点绕序，自己拼的四边形如果看不到，先 `glDisable(GL_CULL_FACE)` 验证。

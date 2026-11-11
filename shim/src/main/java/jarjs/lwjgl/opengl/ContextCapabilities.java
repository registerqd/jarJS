package jarjs.lwjgl.opengl;

/**
 * org.lwjgl.opengl.ContextCapabilities 兼容层。
 * 大型 Java 游戏（如 Minecraft 1.8 / Project Zomboid）在启动时会通过此类探测硬件特性。
 * 在 jarJS 环境下，将常用的核心 OpenGL 1.1~3.0 及常用扩展全部预置为 true，防止因特性探测失败直接抛出异常崩溃。
 */
public class ContextCapabilities {

    // 核心 OpenGL 版本标志
    public final boolean OpenGL11 = true;
    public final boolean OpenGL12 = true;
    public final boolean OpenGL13 = true;
    public final boolean OpenGL14 = true;
    public final boolean OpenGL15 = true;
    public final boolean OpenGL20 = true;
    public final boolean OpenGL21 = true;
    public final boolean OpenGL30 = true;
    public final boolean OpenGL31 = false;
    public final boolean OpenGL32 = false;

    // 核心缓冲区与着色器扩展
    public final boolean GL_ARB_vertex_buffer_object = true;
    public final boolean GL_ARB_shader_objects = true;
    public final boolean GL_ARB_vertex_shader = true;
    public final boolean GL_ARB_fragment_shader = true;
    public final boolean GL_ARB_shading_language_100 = true;

    // 帧缓冲 FBO 扩展（MC、Zomboid 渲染管线必查）
    public final boolean GL_ARB_framebuffer_object = true;
    public final boolean GL_EXT_framebuffer_object = true;
    public final boolean GL_EXT_framebuffer_blit = false;

    // 纹理与混合扩展
    public final boolean GL_ARB_multitexture = true;
    public final boolean GL_ARB_texture_non_power_of_two = true;
    public final boolean GL_EXT_blend_func_separate = true;
    public final boolean GL_EXT_blend_equation_separate = true;
    public final boolean GL_EXT_blend_color = true;
    public final boolean GL_EXT_blend_minmax = true;
    public final boolean GL_EXT_blend_subtract = true;

    // 顶点数组与矩阵
    public final boolean GL_ARB_vertex_array_object = true;
    public final boolean GL_ARB_matrix_palette = false;
    public final boolean GL_NV_fog_distance = false;

    // 遮挡查询与各向异性过滤
    public final boolean GL_ARB_occlusion_query = false;
    public final boolean GL_EXT_texture_filter_anisotropic = true;

    ContextCapabilities() {}
}

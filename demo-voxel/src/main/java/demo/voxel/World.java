package demo.voxel;

import org.lwjgl.opengl.GL11;

/**
 * 体素世界：管理 3D 体素网格、程序化起伏丘陵与树木生成、可见面剔除渲染。
 */
public class World {

    public static final int W = 28;
    public static final int H = 16;
    public static final int D = 28;

    private final byte[] blocks = new byte[W * H * D];

    public World() {
        generate();
    }

    public int getIndex(int x, int y, int z) {
        if (x < 0 || x >= W || y < 0 || y >= H || z < 0 || z >= D) return -1;
        return (y * D + z) * W + x;
    }

    public Block getBlock(int x, int y, int z) {
        int idx = getIndex(x, y, z);
        return idx < 0 ? Block.AIR : Block.fromId(blocks[idx]);
    }

    public void setBlock(int x, int y, int z, Block b) {
        int idx = getIndex(x, y, z);
        if (idx >= 0) blocks[idx] = (byte) b.id;
    }

    private void generate() {
        // 1. 起伏丘陵地形
        for (int x = 0; x < W; x++) {
            for (int z = 0; z < D; z++) {
                double hVal = 3.5 + 2.0 * Math.sin(x * 0.25) * Math.cos(z * 0.25)
                                  + 0.8 * Math.sin(x * 0.6 + z * 0.4);
                int groundH = Math.max(2, Math.min(H - 6, (int) Math.round(hVal)));

                for (int y = 0; y <= groundH; y++) {
                    if (y == groundH) {
                        setBlock(x, y, z, Block.GRASS);
                    } else if (y >= groundH - 2) {
                        setBlock(x, y, z, Block.DIRT);
                    } else {
                        setBlock(x, y, z, Block.STONE);
                    }
                }
            }
        }

        // 2. 种几棵橡树
        plantTree(7, getGroundHeight(7, 7) + 1, 7);
        plantTree(20, getGroundHeight(20, 8) + 1, 8);
        plantTree(8, getGroundHeight(8, 20) + 1, 20);
        plantTree(19, getGroundHeight(19, 19) + 1, 19);
    }

    private int getGroundHeight(int x, int z) {
        for (int y = H - 1; y >= 0; y--) {
            if (getBlock(x, y, z).isSolid()) return y;
        }
        return 2;
    }

    private void plantTree(int tx, int ty, int tz) {
        if (tx < 3 || tx >= W - 3 || tz < 3 || tz >= D - 3 || ty + 5 >= H) return;
        // 树干
        for (int y = 0; y < 4; y++) setBlock(tx, ty + y, tz, Block.WOOD);
        // 树叶 (3x3x2)
        for (int dy = 2; dy <= 4; dy++) {
            int rad = (dy == 4) ? 1 : 2;
            for (int dx = -rad; dx <= rad; dx++) {
                for (int dz = -rad; dz <= rad; dz++) {
                    if (dx == 0 && dz == 0 && dy < 4) continue;
                    int lx = tx + dx, ly = ty + dy, lz = tz + dz;
                    if (getBlock(lx, ly, lz) == Block.AIR) {
                        setBlock(lx, ly, lz, Block.LEAVES);
                    }
                }
            }
        }
    }

    /**
     * 渲染所有方块。核心：可见面剔除 (Face Culling) —— 邻居不是透明方块的面绝不提交，
     * 大幅降低 draw calls 与顶点量，保证 60 FPS 流畅满帧。
     */
    public void render(int atlasTextureId) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlasTextureId);

        final float step = 0.25f; // 4x4 图集每个格占 0.25 UV

        GL11.glBegin(GL11.GL_QUADS);

        for (int y = 0; y < H; y++) {
            for (int z = 0; z < D; z++) {
                for (int x = 0; x < W; x++) {
                    Block b = getBlock(x, y, z);
                    if (!b.isSolid()) continue;

                    // 面 0: TOP (+Y)
                    if (getBlock(x, y + 1, z).isTransparent()) {
                        int[] t = b.getTile(0);
                        float u0 = t[0] * step, v0 = t[1] * step;
                        float u1 = u0 + step, v1 = v0 + step;
                        GL11.glColor3f(1.0f, 1.0f, 1.0f); // 顶面全亮
                        GL11.glNormal3f(0, 1, 0);
                        GL11.glTexCoord2f(u0, v0); GL11.glVertex3f(x,     y + 1, z);
                        GL11.glTexCoord2f(u0, v1); GL11.glVertex3f(x,     y + 1, z + 1);
                        GL11.glTexCoord2f(u1, v1); GL11.glVertex3f(x + 1, y + 1, z + 1);
                        GL11.glTexCoord2f(u1, v0); GL11.glVertex3f(x + 1, y + 1, z);
                    }

                    // 面 1: BOTTOM (-Y)
                    if (y > 0 && getBlock(x, y - 1, z).isTransparent()) {
                        int[] t = b.getTile(1);
                        float u0 = t[0] * step, v0 = t[1] * step;
                        float u1 = u0 + step, v1 = v0 + step;
                        GL11.glColor3f(0.55f, 0.55f, 0.55f); // 底面暗阴影
                        GL11.glNormal3f(0, -1, 0);
                        GL11.glTexCoord2f(u0, v0); GL11.glVertex3f(x,     y, z + 1);
                        GL11.glTexCoord2f(u0, v1); GL11.glVertex3f(x,     y, z);
                        GL11.glTexCoord2f(u1, v1); GL11.glVertex3f(x + 1, y, z);
                        GL11.glTexCoord2f(u1, v0); GL11.glVertex3f(x + 1, y, z + 1);
                    }

                    // 面 2: NORTH (-Z)
                    if (getBlock(x, y, z - 1).isTransparent()) {
                        int[] t = b.getTile(2);
                        float u0 = t[0] * step, v0 = t[1] * step;
                        float u1 = u0 + step, v1 = v0 + step;
                        GL11.glColor3f(0.82f, 0.82f, 0.82f);
                        GL11.glNormal3f(0, 0, -1);
                        GL11.glTexCoord2f(u1, v1); GL11.glVertex3f(x,     y,     z);
                        GL11.glTexCoord2f(u1, v0); GL11.glVertex3f(x,     y + 1, z);
                        GL11.glTexCoord2f(u0, v0); GL11.glVertex3f(x + 1, y + 1, z);
                        GL11.glTexCoord2f(u0, v1); GL11.glVertex3f(x + 1, y,     z);
                    }

                    // 面 3: SOUTH (+Z)
                    if (getBlock(x, y, z + 1).isTransparent()) {
                        int[] t = b.getTile(3);
                        float u0 = t[0] * step, v0 = t[1] * step;
                        float u1 = u0 + step, v1 = v0 + step;
                        GL11.glColor3f(0.82f, 0.82f, 0.82f);
                        GL11.glNormal3f(0, 0, 1);
                        GL11.glTexCoord2f(u0, v1); GL11.glVertex3f(x,     y,     z + 1);
                        GL11.glTexCoord2f(u1, v1); GL11.glVertex3f(x + 1, y,     z + 1);
                        GL11.glTexCoord2f(u1, v0); GL11.glVertex3f(x + 1, y + 1, z + 1);
                        GL11.glTexCoord2f(u0, v0); GL11.glVertex3f(x,     y + 1, z + 1);
                    }

                    // 面 4: WEST (-X)
                    if (getBlock(x - 1, y, z).isTransparent()) {
                        int[] t = b.getTile(4);
                        float u0 = t[0] * step, v0 = t[1] * step;
                        float u1 = u0 + step, v1 = v0 + step;
                        GL11.glColor3f(0.72f, 0.72f, 0.72f);
                        GL11.glNormal3f(-1, 0, 0);
                        GL11.glTexCoord2f(u0, v1); GL11.glVertex3f(x, y,     z);
                        GL11.glTexCoord2f(u1, v1); GL11.glVertex3f(x, y,     z + 1);
                        GL11.glTexCoord2f(u1, v0); GL11.glVertex3f(x, y + 1, z + 1);
                        GL11.glTexCoord2f(u0, v0); GL11.glVertex3f(x, y + 1, z);
                    }

                    // 面 5: EAST (+X)
                    if (getBlock(x + 1, y, z).isTransparent()) {
                        int[] t = b.getTile(5);
                        float u0 = t[0] * step, v0 = t[1] * step;
                        float u1 = u0 + step, v1 = v0 + step;
                        GL11.glColor3f(0.72f, 0.72f, 0.72f);
                        GL11.glNormal3f(1, 0, 0);
                        GL11.glTexCoord2f(u1, v1); GL11.glVertex3f(x + 1, y,     z);
                        GL11.glTexCoord2f(u1, v0); GL11.glVertex3f(x + 1, y + 1, z);
                        GL11.glTexCoord2f(u0, v0); GL11.glVertex3f(x + 1, y + 1, z + 1);
                        GL11.glTexCoord2f(u0, v1); GL11.glVertex3f(x + 1, y,     z + 1);
                    }
                }
            }
        }

        GL11.glEnd();
    }
}

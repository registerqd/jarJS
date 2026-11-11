package demo.voxel;

/**
 * 方块类型与材质图集 UV 映射。
 * 图集为 4x4 网格 (64x64，每个贴图 16x16，步长 0.25)。
 */
public enum Block {
    AIR(0),
    GRASS(1),
    DIRT(2),
    STONE(3),
    WOOD(4),
    LEAVES(5),
    BRICK(6);

    public final int id;

    Block(int id) {
        this.id = id;
    }

    public static Block fromId(int id) {
        for (Block b : values()) {
            if (b.id == id) return b;
        }
        return AIR;
    }

    public boolean isSolid() {
        return this != AIR;
    }

    public boolean isTransparent() {
        return this == AIR || this == LEAVES;
    }

    /**
     * 获取指定面在 4x4 图集里的格子坐标 [tileX, tileY]。
     * face: 0=TOP(+Y), 1=BOTTOM(-Y), 2=NORTH(-Z), 3=SOUTH(+Z), 4=WEST(-X), 5=EAST(+X)
     */
    public int[] getTile(int face) {
        switch (this) {
            case GRASS:
                if (face == 0) return new int[]{0, 0};      // 草顶
                if (face == 1) return new int[]{2, 0};      // 泥土底
                return new int[]{1, 0};                     // 草侧面
            case DIRT:
                return new int[]{2, 0};
            case STONE:
                return new int[]{3, 0};
            case WOOD:
                if (face == 0 || face == 1) return new int[]{1, 1}; // 木头顶面年轮
                return new int[]{0, 1};                             // 木头侧面
            case LEAVES:
                return new int[]{2, 1};
            case BRICK:
                return new int[]{3, 1};
            default:
                return new int[]{0, 0};
        }
    }
}

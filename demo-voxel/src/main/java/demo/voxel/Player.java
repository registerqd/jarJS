package demo.voxel;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/**
 * 玩家第一人称控制器：WASD 移动、重力跳跃、鼠标视角、射线投射方块拾取。
 */
public class Player {

    public float x = 14.5f;
    public float y = 8.5f;
    public float z = 14.5f;

    public float yaw = 35f;
    public float pitch = -22f; // 俯视地面丘陵与树木

    private float vy = 0.0f;
    private boolean onGround = false;

    // 准星射线拾取结果
    public boolean hasTarget = false;
    public int targetX, targetY, targetZ;     // 命中的方块（破坏目标）
    public int placeX, placeY, placeZ;       // 紧邻的空气格（放置目标）

    public Block selectedBlock = Block.BRICK; // 默认手持砖块

    public void update(float dt, World world) {
        handleMouse();
        handleKeyboard(dt, world);
        updateRaycast(world);
    }

    private void handleMouse() {
        // 鼠标相对偏移量转动视角
        int dx = Mouse.getDX();
        int dy = Mouse.getDY();
        float sens = 0.22f;

        yaw = (yaw + dx * sens) % 360.0f;
        pitch = Math.max(-89.0f, Math.min(89.0f, pitch + dy * sens));
    }

    private void handleKeyboard(float dt, World world) {
        float forward = 0, strafe = 0;
        if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP)) forward += 1;
        if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN)) forward -= 1;
        if (Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT)) strafe -= 1;
        if (Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) strafe += 1;

        // 数字键切换手持方块
        if (Keyboard.isKeyDown(Keyboard.KEY_1)) selectedBlock = Block.GRASS;
        if (Keyboard.isKeyDown(Keyboard.KEY_2)) selectedBlock = Block.DIRT;
        if (Keyboard.isKeyDown(Keyboard.KEY_3)) selectedBlock = Block.STONE;
        if (Keyboard.isKeyDown(Keyboard.KEY_4)) selectedBlock = Block.WOOD;
        if (Keyboard.isKeyDown(Keyboard.KEY_5)) selectedBlock = Block.BRICK;
        if (Keyboard.isKeyDown(Keyboard.KEY_6)) selectedBlock = Block.LEAVES;

        // 跳跃
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && onGround) {
            vy = 7.0f;
            onGround = false;
        }

        // 水平速度分解
        double radYaw = Math.toRadians(yaw);
        float sinY = (float) Math.sin(radYaw);
        float cosY = (float) Math.cos(radYaw);

        float speed = 5.2f;
        float moveX = (forward * sinY + strafe * cosY) * speed * dt;
        float moveZ = (-forward * cosY + strafe * sinY) * speed * dt;

        // 简易水平移动碰撞
        float nx = x + moveX;
        float nz = z + moveZ;
        if (isPassable(nx, y, z, world)) x = nx;
        if (isPassable(x, y, nz, world)) z = nz;

        // 重力与下落碰撞
        vy -= 18.0f * dt;
        float ny = y + vy * dt;
        if (isPassable(x, ny, z, world)) {
            y = ny;
            onGround = false;
        } else {
            if (vy < 0) onGround = true;
            vy = 0;
        }

        // 掉出世界回弹
        if (y < 0) { y = 14; vy = 0; }
    }

    private boolean isPassable(float px, float py, float pz, World world) {
        int bx = (int) Math.floor(px);
        int by0 = (int) Math.floor(py);
        int by1 = (int) Math.floor(py + 1.5f);
        int bz = (int) Math.floor(pz);
        return !world.getBlock(bx, by0, bz).isSolid() && !world.getBlock(bx, by1, bz).isSolid();
    }

    /** 射线投射拾取方块（步长前进 6 格范围探测）。 */
    private void updateRaycast(World world) {
        hasTarget = false;
        double radYaw = Math.toRadians(yaw);
        double radPitch = Math.toRadians(pitch);

        float dirX = (float) (Math.sin(radYaw) * Math.cos(radPitch));
        float dirY = (float) Math.sin(radPitch);
        float dirZ = (float) (-Math.cos(radYaw) * Math.cos(radPitch));

        float eyeX = x;
        float eyeY = y + 1.6f;
        float eyeZ = z;

        float prevX = eyeX, prevY = eyeY, prevZ = eyeZ;
        float step = 0.08f;
        for (float d = 0; d <= 6.0f; d += step) {
            float cx = eyeX + dirX * d;
            float cy = eyeY + dirY * d;
            float cz = eyeZ + dirZ * d;

            int bx = (int) Math.floor(cx);
            int by = (int) Math.floor(cy);
            int bz = (int) Math.floor(cz);

            if (world.getBlock(bx, by, bz).isSolid()) {
                hasTarget = true;
                targetX = bx; targetY = by; targetZ = bz;
                placeX = (int) Math.floor(prevX);
                placeY = (int) Math.floor(prevY);
                placeZ = (int) Math.floor(prevZ);
                return;
            }
            prevX = cx; prevY = cy; prevZ = cz;
        }
    }
}

package dk.itu.mario.level;

import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.engine.sprites.Enemy;
import dk.itu.mario.engine.sprites.SpriteTemplate;
import dk.itu.mario.level.generator.LevelColumn;
import dk.itu.mario.level.generator.MapTiles;

import java.util.List;

public class BestPSOIndividualLevel extends Level implements LevelInterface {

    private GamePlay playerM;
    private List<LevelColumn> bestMap;

    public BestPSOIndividualLevel(int width, int height, GamePlay playerMetrics, List<LevelColumn> bestMap) {

        super(width, height);

        this.playerM = playerMetrics;
        this.bestMap = bestMap;

        creat();
    }

    private void creat() {

        int floor = height - 1;

        for(int i = 0; i < bestMap.size(); i++)
        {
            LevelColumn curLevelColumn = bestMap.get(i);

            // 生成地图
            for(int j = 0; j < height; j++){
                // 注意，马里奥主程序里的坐标系为从左上到右下从零递增，但在PSO程序里是从左下到右上从零递增，所以要注意变换

                int y = height - j - 1;
                // j为马里奥里面的纵坐标，y为PSO程序里的纵坐标

                char cellChar = curLevelColumn.getCellType(y);
                byte cellByte = SlicesManager.cellChar2Byte(cellChar);
                setBlock(i, j, cellByte);

                if(cellChar == MapTiles.SPIKE) {
                    setSpriteTemplate(i, j, new SpriteTemplate(Enemy.ENEMY_SPIKY, false));
                }
            }
        }

        // 修正地面方块类型
        fixWalls();


        // 出口
        xExit = width - 4;
        yExit = floor;
    }

    private void fixWalls()
    {
        boolean[][] blockMap = new boolean[width + 1][height + 1];

        for (int x = 0; x < width + 1; x++)
        {
            for (int y = 0; y < height + 1; y++)
            {
                int blocks = 0;
                for (int xx = x - 1; xx < x + 1; xx++)
                {
                    for (int yy = y - 1; yy < y + 1; yy++)
                    {
                        if (getBlockCapped(xx, yy) == GROUND){
                            blocks++;
                        }
                    }
                }
                if(getBlockCapped(x,y) == GROUND && x<width-1 && x!=0 && y!=0 && y<height-1){
                    boolean cond = true;
                    for (int yy = y-1; yy < y+1; yy++){
                        for (int xx = x-1; xx < x+2; xx++) {
                            if(yy==y && xx==x)continue;
                            if(getBlockCapped(xx, yy)==GROUND)cond = false;
                        }
                    }
                    if(cond) setBlock(x, y, HILL_TOP);
                }

                blockMap[x][y] = blocks == 4;
            }
        }
        blockify(this, blockMap, width + 1, height + 1);
    }

    /**
     * 根据预计算的块映射数据，调整关卡中每个方块的贴图以实现平滑地形过渡
     * （使用类似 Marching Squares 的算法处理地形边缘）
     *
     * @param level 要修改的关卡对象
     * @param blocks 布尔映射表，表示每个"顶点"是否被地面占据（true=地面）
     * @param width 映射表宽度（通常为关卡宽度+1）
     * @param height 映射表高度（通常为关卡高度+1）
     */
    private void blockify(Level level, boolean[][] blocks, int width, int height) {
        int to = 0; // 贴图偏移量（可用于切换主题，如雪地/沙漠等）

        // 临时存储当前处理的2x2顶点状态（用于判断相邻块关系）
        boolean[][] b = new boolean[2][2]; // [相对x][相对y]

        // 遍历关卡中的每个方块位置
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                /* 步骤1：收集当前方块对应的4个顶点状态 */
                for (int xx = x; xx <= x + 1; xx++) {
                    for (int yy = y; yy <= y + 1; yy++) {
                        // 处理边界情况（防止数组越界）
                        int _xx = Math.min(Math.max(xx, 0), width - 1);
                        int _yy = Math.min(Math.max(yy, 0), height - 1);

                        // 将四个顶点状态存入2x2数组（[0][0]=左上，[1][1]=右下）
                        b[xx - x][yy - y] = blocks[_xx][_yy];
                    }
                }

                /* 步骤2：根据顶点状态组合选择对应贴图 */
                // 情况1：左右列相同 且 上下行相同 → 形成水平或垂直统一区域
                if (b[0][0] == b[1][0] && b[0][1] == b[1][1]) {
                    if (b[0][0] == b[0][1]) { // 四个顶点全同
                        if (b[0][0]) {
                            // 全地面：设置完整方块（中间草地）
                            level.setBlock(x, y, (byte) (1 + 9 * 16 + to));
                        }
                    } else { // 上下两行不同 → 形成垂直斜坡
                        if (b[0][0]) {
                            // 上边是地面：下斜坡（顶部草地，下方泥土）
                            level.setBlock(x, y, (byte) (1 + 10 * 16 + to));
                        } else {
                            // 下边是地面：上斜坡（顶部泥土，下方草地）
                            level.setBlock(x, y, (byte) (1 + 8 * 16 + to));
                        }
                    }
                }
                // 情况2：上下行相同 且 左右列相同 → 形成垂直统一区域
                else if (b[0][0] == b[0][1] && b[1][0] == b[1][1]) {
                    if (b[0][0]) {
                        // 左侧是地面：右边缘草地（垂直右侧）
                        level.setBlock(x, y, (byte) (2 + 9 * 16 + to));
                    } else {
                        // 右侧是地面：左边缘草地（垂直左侧）
                        level.setBlock(x, y, (byte) (0 + 9 * 16 + to));
                    }
                }
                // 情况3：对角线相同 → 形成45度斜坡
                else if (b[0][0] == b[1][1] && b[0][1] == b[1][0]) {
                    // 统一设置为完整方块（可能用于内凹角落）
                    if(b[0][0]){
                        level.setBlock(x, y, HILL_TOP);
                    }else
                        level.setBlock(x, y, HILL_TOP);
                }
                // 情况4：左右列顶部相同 → 形成水平边缘
                else if (b[0][0] == b[1][0]) {
                    if (b[0][0]) {
                        // 顶部是地面：下凹处理
                        if (b[0][1]) {
                            // 右下凹槽（右下角缺口）
                            level.setBlock(x, y, (byte) (3 + 10 * 16 + to));
                        } else {
                            // 左下凹槽（左下角缺口）
                            level.setBlock(x, y, (byte) (3 + 11 * 16 + to));
                        }
                    } else {
                        // 顶部非地面：上边缘处理
                        if (b[0][1]) {
                            // 右上斜坡（右上边缘草地）
                            level.setBlock(x, y, (byte) (2 + 8 * 16 + to));
                        } else {
                            // 左上斜坡（左上边缘草地）
                            level.setBlock(x, y, (byte) (0 + 8 * 16 + to));
                        }
                    }
                }
                // 情况5：上下行右侧相同 → 形成垂直边缘
                else if (b[0][1] == b[1][1]) {
                    if (b[0][1]) {
                        if (b[0][0]) {
                            // 左口袋（左侧凹陷）
                            level.setBlock(x, y, (byte) (3 + 9 * 16 + to));
                        } else {
                            // 右口袋（右侧凹陷）
                            level.setBlock(x, y, (byte) (3 + 8 * 16 + to));
                        }
                    } else {
                        if (b[0][0]) {
                            // 右下斜坡（右下边缘草地）
                            level.setBlock(x, y, (byte) (2 + 10 * 16 + to));
                        } else {
                            // 左下斜坡（左下边缘草地）
                            level.setBlock(x, y, (byte) (0 + 10 * 16 + to));
                        }
                    }
                }
                // 情况6：无法归类的复杂形状 → 使用默认过渡块
                else {
                    // 设置小过渡块（可能用于复杂转角）
                    level.setBlock(x, y, (byte) (0 + 1 * 16 + to));
                }
            }
        }
    }

}
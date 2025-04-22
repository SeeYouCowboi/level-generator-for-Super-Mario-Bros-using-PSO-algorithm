package dk.itu.mario.level.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import dk.itu.mario.engine.sprites.Mario;
import dk.itu.mario.level.Level;


public class LevelParticle {
    
    private List<LevelColumn> curMap;
    private List<LevelColumn> personalBest;
    private boolean isFitnessUpdated;
    private int cachedFitness;
    private double fitness;
    private double bestFitness;
    private int generatedChunkStartIndex = 0;

    // PSO参数
    public double w;
    public double c1;
    public double c2;
    public double r1;
    public double r2;

    // 游戏物理常数
    private static final int spikePenalty = 20;
    private static int maxBlockJumpHeight = 2;
    private static float maxJumpHeight = 0.0f;
    private static float timeToWalkTile = 999.0f;
    private static boolean arePlayerMoveConstsCalculated = false;
    private final int chunkSize;


    // 首个区块的LevelParticle只有一个区块大小且只有一个平地的BaseChunk
    public LevelParticle(int chunkSize, int maxHeight, Random rand, boolean ifCreateBaseChunk, int baseChunkSize) {
        this.chunkSize = chunkSize;
        this.curMap = new ArrayList<LevelColumn>();

        if(ifCreateBaseChunk) {
            for(int i=0; i<baseChunkSize ;i++) {
                curMap.add(new LevelColumn(4,-1,-1));
            }
        }else {
            for (int i = 0; i < chunkSize; i++) {
                curMap.add(new LevelColumn(maxHeight, rand));
            }
        }

        evaluateFitness();
        this.personalBest = deepCopy(curMap);
    }

    // 先把baseChunk加入curMap，然后随机生成半个区块
    public LevelParticle(int chunkSize, int maxHeight, Random rand, List<LevelColumn> baseChunk) {
        this.chunkSize = chunkSize;
        if(!arePlayerMoveConstsCalculated) {
            calculatePlayerMoveConstants();
            arePlayerMoveConstsCalculated = true;
        }

        assert  (baseChunk.size() < chunkSize);
        this.curMap = new ArrayList<LevelColumn>();
        this.curMap.addAll(baseChunk); // 加入参考区块
        this.generatedChunkStartIndex = curMap.size();

        for (int i = 0; i < chunkSize/2; i++) {
            curMap.add(new LevelColumn(maxHeight, rand));
        } // 生成随机半区块

        evaluateFitness();
        this.personalBest = deepCopy(curMap);
        this.bestFitness = fitness;
    }



    private static void calculatePlayerMoveConstants() {

        // 计算最大跳跃高度（逐帧模拟）
        float currentHeight = 0;
        float currentVelocity = Mario.JUMP_TIME_GROUND; // 初始速度 ya = jumpTime * yJumpSpeed
        for (int t = Mario.JUMP_TIME_GROUND; t > 0; t--) {
            currentHeight += currentVelocity;
            currentVelocity = t * Mario.JUMP_FORCE_GROUND; // 每帧速度更新 ya = jumpTime * yJumpSpeed
            maxJumpHeight = Math.max(maxJumpHeight, currentHeight);
        }
        while (currentHeight > 0) {
            currentVelocity += Mario.GRAVITY; // 重力加速 ya += 3
            currentHeight += currentVelocity;
        }

        // 转换为方块单位高度
        maxBlockJumpHeight = (int) (maxJumpHeight / MapTiles.TILE_WIDTH);

        // 行走时间计算
        timeToWalkTile = MapTiles.TILE_WIDTH / Mario.RUN_SPEED;

        // 最远平地跳距离计算
        float ya = Mario.JUMP_FORCE_GROUND * Mario.JUMP_TIME_GROUND;
        int airFrames = Mario.JUMP_TIME_GROUND;
        float y = 0;
        while (y <= 0) {
            ya += Mario.GRAVITY;
            y += ya;
            airFrames++;
        }
        float xDistance = 0;
        float xSpeed = Mario.WALK_SPEED; // 按走路速度计算(本来是计算奔跑速度的，然后发现刺太多根本跑不起来)
        for (int t = 0; t < airFrames; t++) {
            xDistance += xSpeed;
            xSpeed *= Mario.AIR_INERTIA;
        }
        maxJumpStraightXDistance = (int) (xDistance / MapTiles.TILE_WIDTH);

        arePlayerMoveConstsCalculated = true;
    }

    private void evaluateFitness() {
        this.fitness = calculateFitness();
        // 精英保留策略：只有改进时才更新历史最优
        if (fitness > bestFitness) {
            bestFitness = fitness;
            personalBest = deepCopy(curMap);  // 深拷贝防止后续修改影响
        }
    }

    private int calculateFitness() {
        if(!arePlayerMoveConstsCalculated){
            calculatePlayerMoveConstants();
        }

        int fitness = 200 * curMap.size();  // 基础分
        // 地形结构评估（高度差/坑洞检测）
        fitness += getGroundFitness();
        // 尖刺分布评估（连续尖刺惩罚）
        fitness += getSpikeFitness();
        // 可跳跃性评估（玩家移动可行性）
        fitness += getSafeJumpFitness();

        return fitness;
    }
    private static int maxJumpStraightXDistance = 4;
    private static final float maxSpikes = 1.0f;
    private int getSpikeFitness() {
        int spikeFitness = 0;
        int spikeLength = 0;
        int totalSpikes = 0;

        for (LevelColumn col : curMap) {
            if (col.getHasSpike()) {
                totalSpikes++;
                spikeLength++;

                // 连续尖刺惩罚
                if (spikeLength > maxJumpStraightXDistance) {
                    spikeFitness -= spikeLength * spikePenalty;
                }
            } else {
                spikeLength = 0;
            }
        }

        // 尖刺密度惩罚
        if (totalSpikes > maxSpikes * curMap.size()) {
            spikeFitness -= (totalSpikes - (int)(maxSpikes * curMap.size())) * spikePenalty;
        }

        return spikeFitness;
    }

    private int getGroundFitness() {
        int groundFitness = 0;
        int holeLength = 0;

        for (int i = 1; i < curMap.size() - 1; i++) {
            LevelColumn prev = curMap.get(i-1);
            LevelColumn curr = curMap.get(i);

            // 空洞检测
            if (prev.getGroundHeight() == 0) {
                holeLength++;
                if (holeLength > maxJumpStraightXDistance) {
                    groundFitness -= 100;
                }
            } else {
                holeLength = 0;
            }

            // 高度差检测
            int heightDiff = Math.abs(prev.getGroundHeight() - curr.getGroundHeight());
            if (heightDiff > maxBlockJumpHeight) {
                groundFitness -= 100 * heightDiff;
            } else {
                groundFitness += 10 - (2 * heightDiff); // 鼓励平缓地形
            }
        }
        return groundFitness;
    }

    private int getSafeJumpFitness() {
        if (!arePlayerMoveConstsCalculated) {
            calculatePlayerMoveConstants();
        }

        int safeJumpFitness = 0;
        int contiguousHazards = 0;
        final int hazardPenalty = 50;

        // 从左向右扫描
        for (int i = 0; i < curMap.size(); ) {
            LevelColumn current = curMap.get(i);
            if (!current.isSafe) {
                contiguousHazards++;
                i++;
                continue;
            }

            // 找到下一个安全地块
            int nextSafeIndex = i + 1;
            if (nextSafeIndex >= curMap.size()) break;
            while (nextSafeIndex < curMap.size() && !curMap.get(nextSafeIndex).isSafe) {
                nextSafeIndex++;
            }

            if (!isJumpPossible(i, nextSafeIndex)) {
                if (nextSafeIndex >= curMap.size()) {
                    contiguousHazards = curMap.size() - i;
                }else{
                    contiguousHazards = nextSafeIndex - i;
                }
                if (contiguousHazards > maxJumpStraightXDistance) {
                    safeJumpFitness -= hazardPenalty * contiguousHazards;
                }
            }
            i = nextSafeIndex;
        }

        return safeJumpFitness;
    }

    private boolean isJumpPossible(int startIndex, int endIndex) {
        if (endIndex >= curMap.size()) return false;

        int xDist = Math.abs(endIndex - startIndex);
        float pixelDist = xDist * MapTiles.TILE_WIDTH;
        float maxH = maxJumpHeight;
        float parabolaCoeff = -4 * maxH / (pixelDist * pixelDist);

        for (int i = 1; i <= xDist; i++) {
            float xi = i * MapTiles.TILE_WIDTH;
            float yi = parabolaCoeff * xi * (xi - pixelDist);
            int maxAllowedHeight = (int)(yi / MapTiles.TILE_WIDTH) + curMap.get(startIndex).getGroundHeight();

            int currHeight = curMap.get(
                    startIndex + i
            ).getGroundHeight();

            if (maxAllowedHeight < currHeight) {
                return false;
            }
        }
        return true;
    }


    public List<LevelColumn> move(
            double w,
            List<LevelColumn> velocities,
            double r1, double c1,
            List<LevelColumn> personalBest,
            double r2, double c2,
            LevelParticle groupBest) {

        this.w = w;
        this.c1 = c1;
        this.c2 = c2;
        this.r1 = r1;
        this.r2 = r2;

        LevelColumn gco = null; // 当前地图遍历
        int i = generatedChunkStartIndex;
        LevelColumn gcv = null;
        LevelColumn gcp = null;
        LevelColumn gcg = null; // 全局最优地图遍历
        List<LevelColumn> resultVelocities = new ArrayList<>(velocities.size());

        if(!isFitnessUpdated)
            evaluateFitness();

        for(int j = 0 ; i < curMap.size(); i++, j++ ){
            gco = curMap.get(i);
            gcg = groupBest.curMap.get(i);
            gcv = velocities.get(j);
            gcp = personalBest.get(i);
            double newGroundHeight =
                    gco.d_groundHeight +
                            w * gcv.d_groundHeight +
                            r1 * c1 * (gcp.d_groundHeight - gco.d_groundHeight) +
                            r2 * c2 * (gcg.d_groundHeight - gco.d_groundHeight);

            double newHasSpike =
                    gco.d_hasSpike +
                            w * gcv.d_hasSpike +
                            r1 * c1 * (gcp.d_hasSpike - gco.d_hasSpike) +
                            r2 * c2 * (gcg.d_hasSpike - gco.d_hasSpike);



            // 限制状态值范围
            newGroundHeight = clamp(newGroundHeight, 0, 20.0);
            newHasSpike = clamp(newHasSpike, -10.0, 10.0);

            resultVelocities.add(new LevelColumn(newGroundHeight - gco.d_groundHeight,
                    newHasSpike - gco.d_hasSpike, -1));

            gco.d_groundHeight = newGroundHeight;
            gco.d_hasSpike = newHasSpike;
        }
        isFitnessUpdated = false;

        return resultVelocities;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }


    /**
     * 深拷贝关卡列集合（用于保存历史最优）
     */
    private List<LevelColumn> deepCopy(List<LevelColumn> original) {
        List<LevelColumn> copy = new ArrayList<>();
        for (LevelColumn col : original) {
            copy.add(new LevelColumn(col));  // 调用拷贝构造函数
        }
        return copy;
    }

    // Getters
    public double getFitness() {
        if(!isFitnessUpdated) {
            evaluateFitness();
        }
        return fitness;
    }
    public List<LevelColumn> getCurMap() { return curMap; }

    public List<LevelColumn> getGeneratedChunk() {
        int generatedChunkSize = chunkSize/2;
        return curMap.subList(curMap.size() - generatedChunkSize, curMap.size());
    }
}
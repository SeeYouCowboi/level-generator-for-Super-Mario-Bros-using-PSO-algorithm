package dk.itu.mario.level.generator;

import dk.itu.mario.MarioInterface.LevelGenerator;

import java.util.*;

public class PSOIterator  {
    // 地图元素类型定义
    public static final char BLANK = 'B';
    public static final char GROUND = 'G';
    public static final char SPIKE = 'S';
    public static final char CLOCK = 'C';

    // PSO参数
    private final double paramW = Global.Parameters.get("W");
    private final double paramCMin = Global.Parameters.get("Cmin");
    private final double paramCMax = Global.Parameters.get("Cmax");
    private final double paramRMin = Global.Parameters.get("Rmin");
    private final double paramRMax = Global.Parameters.get("Rmax");
    private final Random rand = Global.random;


    // 地图生成参数
    private final int populationSize;
    private final int maxIterations;
    private int chunkWidth;
    private int maxHeight;

    private List<LevelColumn> globalBestMap;
    private double globalBestFitness = Double.NEGATIVE_INFINITY;

    public PSOIterator(int populationSize, int maxIterations,
                       int chunkWidth, int maxHeight) {
        this.populationSize = populationSize;
        this.maxIterations = maxIterations;
        this.chunkWidth = chunkWidth;
        this.maxHeight = maxHeight;

    }

    public void generateLevel(int chunkNum) {
        // 生成初始区块
        LevelParticle baseParticle = new LevelParticle(chunkWidth, maxHeight, rand, true, chunkWidth);
        globalBestMap = baseParticle.getCurMap();

        for(int i=0; i<maxIterations; i++) {
            // 由于每次生成区块大小为半区块，再减去初始区块，因此一共生成chunkNum*2-2次区块
            for(int j=0; j<chunkNum*2-2; j++){
                // 需要生成popSize个mapParticle
                List<LevelParticle> population = new ArrayList<LevelParticle>(populationSize);
                for (int k = 0; k < populationSize; k++) {
                    population.add(new LevelParticle(chunkWidth, maxHeight, rand, getLastChunk(globalBestMap)));
                    // 注意生成的随机半区块宽度为maxChunkWidth/2
                }

                int xx=0;
                LevelParticle bestParticle;
                bestParticle = particleIterate(population); // 为每个粒子进行迭代并选出最佳粒子

                globalBestMap.addAll(bestParticle.getGeneratedChunk());
            }

        }

    }

    private List<LevelColumn> getLastChunk(List<LevelColumn> originChunk) {
        return originChunk.subList(originChunk.size()-chunkWidth, originChunk.size());
    }


    /**
     * 生成新区块
     */
    private LevelParticle particleIterate(List<LevelParticle> population) {

        List<List<LevelColumn>> velocities = new ArrayList<>(population.size());
        for (int i = 0; i < population.size(); i++) {
            velocities.add(createVelocities(chunkWidth/2));
        }

        List<List<LevelColumn>> personalBests = new ArrayList<>(population.size());
        for (LevelParticle levelParticle : population) {
            List<LevelColumn> pb = new ArrayList<>(levelParticle.getCurMap());
            personalBests.add(pb);
        }

        LevelParticle bestIndividual = null;

        bestIndividual = performIterations(population, velocities, personalBests, bestIndividual);

        for(LevelParticle individual : population) {
            if(individual.getFitness() > bestIndividual.getFitness()) {
                bestIndividual = individual;
            }
        }
        return bestIndividual;
    }

    private double lerp(double a, double b, double t) {
        t = Math.max(0.0, Math.min(1.0, t)); // Clamp t to [0, 1]
        return a + (b - a) * t;
    }

    private LevelParticle performIterations(
            List<LevelParticle> population,
            List<List<LevelColumn>> velocities,
            List<List<LevelColumn>> personalBests,
            LevelParticle bestIndividual
    ) {
        for (long i = 0; i < maxIterations; i++) {
            // 根据适应度排序
            population.sort(Comparator.comparingDouble(LevelParticle::getFitness));
            bestIndividual = population.get(population.size() - 1);

            double w = paramW * (i - maxIterations) / (maxIterations * maxIterations) + paramW;
            double c1 = lerp(paramCMax, paramCMin, (double) i / maxIterations);
            double c2 = lerp(paramCMin, paramCMax, (double) i / maxIterations);
            double r1 = lerp(paramRMin, paramRMax, Global.random.nextDouble());
            double r2 = lerp(paramRMin, paramRMax, Global.random.nextDouble());

            // Move individuals to new position; Move method also returns new velocity
            for (int j = 0; j < population.size(); j++) {
                double previousFitness = population.get(j).getFitness();

                velocities.set(j, population.get(j).move(w, velocities.get(j), r1, c1, personalBests.get(j), r2, c2, bestIndividual));

                if (population.get(j).getFitness() > previousFitness) {
                    personalBests.set(j, shallowCopyList(population.get(j).getCurMap()));
                }
            }
        }
        return bestIndividual;
    }

    private List<LevelColumn> createVelocities(int count) {
        List<LevelColumn> velocities = new ArrayList<LevelColumn>(count);
        for (int i=0; i<count; i++){
            LevelColumn gc = new LevelColumn(maxHeight, rand);
            gc.d_groundHeight -= 2;
            velocities.add(gc);
        }
        return velocities;
    }



    /**
     * 获取生成的最优地图
     */
    public List<LevelColumn> getGeneratedLevel() {
        return globalBestMap;
    }


    // 辅助方法
    static List<LevelColumn> deepCopyMap(List<LevelColumn> original) {
        List<LevelColumn> copy = new ArrayList<>();
        for (LevelColumn col : original) {
            copy.add(new LevelColumn(col));
        }
        return copy;
    }

    static <T> List<T> shallowCopyList(List<T> original) {
        return new ArrayList<>(original);
    }


    private void printProgress(int iteration) {
        if (iteration % 10 == 0) {
            System.out.printf("Iteration %d: Best Fitness = %.2f\n",
                    iteration, globalBestFitness);
        }
    }
}

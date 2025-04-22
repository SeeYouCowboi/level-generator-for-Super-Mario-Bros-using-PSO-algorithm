package dk.itu.mario.level.generator;

import java.util.List;
import java.util.Random;

import dk.itu.mario.MarioInterface.GamePlay;
import dk.itu.mario.MarioInterface.LevelGenerator;
import dk.itu.mario.MarioInterface.LevelInterface;
import dk.itu.mario.geneticAlgorithm.UniformGASuperMario;
import dk.itu.mario.level.BestGAIndividualLevel;
import dk.itu.mario.level.BestPSOIndividualLevel;

public class PSOLevelGenerator implements LevelGenerator {

    private static int chunkWidth = 40;
    private static int chunkNum = 5;
    private static int levelMaxHeight = 15;
    private static int psoGenerateMaxHeight = 12;

    public LevelInterface generateLevel(GamePlay playerMetrics) {
        PSOIterator pi = new PSOIterator(25, 30, chunkWidth, psoGenerateMaxHeight);
        pi.generateLevel(chunkNum);
        List<LevelColumn> psoMap = pi.getGeneratedLevel();

        int generatedChunkSize = chunkWidth/2;
        int levelWidth = chunkWidth + generatedChunkSize * (chunkNum-1)*2;
        assert (levelWidth != psoMap.size());
        LevelInterface level = new BestPSOIndividualLevel(levelWidth, levelMaxHeight, playerMetrics, psoMap);

        return level;
    }

    @Override
    public LevelInterface generateLevel(String detailedInfo) {

        // TODO Auto-generated method stub
        return null;
    }
}

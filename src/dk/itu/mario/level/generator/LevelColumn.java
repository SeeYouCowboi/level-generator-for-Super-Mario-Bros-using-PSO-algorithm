package dk.itu.mario.level.generator;

import java.util.Random;
import dk.itu.mario.level.Level;

public class LevelColumn {
        public double d_groundHeight;

        public int getGroundHeight() {
            return (int) Math.round(d_groundHeight);
        }
        public double d_hasSpike;
        public boolean getHasSpike() {
            return (int) this.d_groundHeight != 0 && d_hasSpike > 0;
        }

        public void setHasSpike(boolean hasSpike) {
            if(this.getGroundHeight() == 0 && hasSpike != this.getHasSpike()){
                d_hasSpike *= -1;
            } else {
                d_hasSpike *= (hasSpike ? 1 : -1);
            }
        }

    public double d_hasEnemy;
    public boolean getHasEnemy(){
        return (int) this.d_groundHeight != 0 && d_hasEnemy > 0;
    }

    public void setHasEnemy(boolean hasEnemy) {
        if(this.getGroundHeight() == 0 && this.getHasSpike() && hasEnemy != this.getHasEnemy()){
            d_hasEnemy *= -1;
        } else {
            d_hasEnemy *= (hasEnemy ? 1 : -1);
        }
    }

    public int obstacleHeight;
    public boolean isSafe;

    public LevelColumn(int maxHeight, Random rand) {
        this.d_groundHeight = rand.nextInt(maxHeight);
        this.d_hasSpike = rand.nextDouble() * 2 - 1;
        if(!this.getHasSpike())
            this.d_hasEnemy = rand.nextDouble() * 2 - 1; // 10%概率有敌人
        else this.d_hasEnemy = -1;
        this.obstacleHeight = (this.getGroundHeight() == 0) ? 0 : (int) (this.getGroundHeight() + (this.getHasSpike() ? 1 : 0));
        this.isSafe =  !(this.getHasSpike() || this.getGroundHeight() == 0 );
    }

    public LevelColumn(double groundHeight, double hasSpike, double hasEnemy)
    {
        this.d_groundHeight = groundHeight;
        this.d_hasSpike = hasSpike;
        this.d_hasEnemy = hasEnemy;
        this.obstacleHeight = (this.getGroundHeight() == 0) ? 0 : (int) (this.getGroundHeight() + (this.getHasSpike() ? 1 : 0));
        this.isSafe =  !(this.getHasSpike() || this.getGroundHeight() == 0 );
    }

    public LevelColumn(LevelColumn other) {
        this.d_groundHeight = other.d_groundHeight;
        this.d_hasSpike = other.d_hasSpike;
        this.d_hasEnemy = other.d_hasEnemy;
        this.obstacleHeight = other.obstacleHeight;
        this.isSafe = other.isSafe;
    }



    // PSO操作
    public LevelColumn add(LevelColumn velocity) {
        this.d_groundHeight += velocity.d_groundHeight;
        this.d_hasSpike += velocity.d_hasSpike;
        this.d_hasEnemy += velocity.d_hasEnemy;
        return this;
    }


    public LevelColumn multiply(double factor) {
        this.d_groundHeight *= factor;
        this.d_hasSpike *= factor;
        this.d_hasEnemy *= factor;
        return this;
    }

    public LevelColumn subtract(LevelColumn other) {
        this.d_groundHeight -= other.d_groundHeight;
        this.d_hasSpike -= other.d_hasSpike;
        this.d_hasEnemy -= other.d_hasEnemy;
        return this;
    }

    // 获取单元格类型
    public char getCellType(int y) {
        if (y < this.getGroundHeight()) return MapTiles.GROUND;
        if (y == this.getGroundHeight() && getHasSpike()) return MapTiles.SPIKE;
        if (y == this.getGroundHeight() && getHasEnemy()) return MapTiles.ENEMY;
        return MapTiles.BLANK;
    }


}
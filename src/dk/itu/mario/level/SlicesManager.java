package dk.itu.mario.level;

import dk.itu.mario.engine.sprites.Enemy;
import dk.itu.mario.level.generator.MapTiles;

public class SlicesManager extends Level {

	private static byte[] sliceA = 
	{
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE,
		HILL_TOP 
	}; 
	
	private static byte[] sliceB = 
	{
		EMPTY_SPACE,
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE,
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		BLOCK_COIN, 
		EMPTY_SPACE,
		EMPTY_SPACE, 
		EMPTY_SPACE,
		HILL_TOP 
	};
	
	private static byte[] sliceC = 
	{
		EMPTY_SPACE,
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE,
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE, 
		EMPTY_SPACE,
		EMPTY_SPACE, 
		EMPTY_SPACE,
		EMPTY_SPACE 
	};

	public static byte cellChar2Byte(char cellChar){
		switch (cellChar){
			case (MapTiles.BLANK): return EMPTY_SPACE;
			case (MapTiles.ENEMY): return EMPTY_SPACE;
			case (MapTiles.SPIKE): return EMPTY_SPACE;
			case (MapTiles.GROUND): return GROUND;
		}
		return -1;
	}

	private static byte[][] slices = {sliceA, sliceB, sliceC};
	
	public static byte[] getSlice(int sliceNumber)
	{
		return slices[sliceNumber];
	}
	
	public static int getSliceAmount()
	{
		return slices.length;
	}
}

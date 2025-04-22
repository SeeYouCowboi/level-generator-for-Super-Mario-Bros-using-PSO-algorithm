# level generator for Super Mario Bros using PSO algorithm

该代码根据[DEPONTES2022](https://www.sciencedirect.com/science/article/pii/S1875952122000209)中提到的粒子群优化算法(PSO)，然后使用了[GA-generator](https://github.com/lucasnfe/smb-genetic-level-generator/tree/master)的代码作为代码基础（关键原来的马里奥AI框架网址已经打不开了= =），做了一个使用PSO算法优化生成的马里奥关卡生成器。

然后由于我写到最后才发现这个马里奥AI框架没有尖刺陷阱，本来想自己写尖刺陷阱的逻辑进去，但是尖刺陷阱要算个方块属性的Enemy，好麻烦...就懒得写了。
所以本代码中的尖刺陷阱由我魔改的不会动的刺龟代替~

然后还有个问题是，原来的马里奥框架里没有单个方块山顶的材质，如果直接用原来的“不完善”的Marching Squares算法处理会导致单方块凸起材质直接是无碰撞体积的“土块”，所以在本代码里把单方块凸起统一改成了山顶中间方块，这下有碰撞体积了，虽然会让单方块凸起很难看，但...又不是不能用。

如果要修改原代码的贴图处理算法，我还得重新画TileMap，还不能用它原来的Marching Squaress算法，因为贴图的情况应该是2^8种（虽然其中会有些无效情况），但绝不是Marching Squaress算法的2^4种情况判断能处理的。
所以也懒得改了~(ˉ▽￣～) ~~

## 如何复现成果

如果要游玩随机生成的关卡，虽然不知道这是原来马里奥AI框架自带的还是上个遗传算法生成器作者写的ㄟ( ▔, ▔ )ㄏ，运行以下java文件

```
java dk.itu.mario.engine.Play
```

如果要游玩由PSO算法生成的关卡，运行以下java文件（修改CustomType至2可以改成遗传算法生成的关卡）

```
java dk.itu.mario.engine.PlayCustomized
```

进入游戏用键盘方向键控制方向，a键奔跑，s键跳跃。

For information about the Mario AI framework (2012), visit the following website:
https://sites.google.com/site/noormario/LevelGeneration/getting-started


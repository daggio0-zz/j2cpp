# j2cpp
=====

A Java to C++ converter, as an eclipse plug-in.

***

This plugins converts Java to C++ 11. It relies on both JDT and CDT.

***

I created this plug-in because I had a Java code base that I needed to translate to C++ for portability reasons (iOS not to name it). The purpose of this plug-in was to handle the tedious work of porting Java syntax to C++ syntax.
Note that the generated C++ code won't compile after conversion and that this converter only does the heavy lifting, you'll have of course to rework this generated code.

Here is an example conversion:

### BlockSituationEx.java:
```java
import java.util.HashMap;
import java.util.Map;

import me.pixodro.furiousblocks.core.panel.BlockState;
import me.pixodro.furiousblocks.core.panel.BlockType;
import me.pixodro.furiousblocks.core.situations.BlockSituation;
import me.pixodro.furiousblocks.core.tools.Point;

public class BlockSituationEx extends BlockSituation {
  private final Map<Integer, Integer> targetColumnCosts;
  private Point origin = null;
  private boolean chainReplacement = false;

  public BlockSituationEx(final BlockSituation bs) {
    super(bs.getId(), bs.getType(), bs.getState(), bs.getStateTick(), bs.getGarbageBlockType(), bs.getGarbageOwner(), bs.isCombo(), bs.hasJustLand(), bs.isFallingFromClearing(), bs.getPoppingIndex());
    targetColumnCosts = new HashMap<Integer, Integer>();
    chainReplacement = false;
  }

  public BlockSituationEx(final BlockSituationEx bsEx) {
    super(bsEx.getId(), bsEx.getType(), bsEx.getState(), bsEx.getStateTick(), bsEx.getGarbageBlockType(), bsEx.getGarbageOwner(), bsEx.isCombo(), bsEx.hasJustLand(), bsEx.isFallingFromClearing(), bsEx.getPoppingIndex());
    origin = bsEx.getOrigin();
    targetColumnCosts = bsEx.targetColumnCosts;
    chainReplacement = bsEx.chainReplacement;
  }

  private BlockSituationEx() {
    super(0, BlockType.INVISIBLE, BlockState.IDLE, 0, (byte) 0, 0, false, false, false, 0);
    targetColumnCosts = new HashMap<Integer, Integer>();
  }

  public final int getTargetColumnCost(final int targetColumn) {
    // TODO: this is a bug, target cost should never be null
    // return targetColumnCosts.get(targetColumn);
    final Integer cost = targetColumnCosts.get(targetColumn);
    return cost == null ? ComboProcessor.INFINITY : cost;
  }

  public final void addTargetCost(final int targetColumn, final int cost) {
    targetColumnCosts.put(targetColumn, cost);
  }

  public final Point getOrigin() {
    return origin;
  }

  public final void setOrigin(final Point position) {
    origin = position;
  }

  public final void reset() {
    origin = null;
    targetColumnCosts.clear();
    chainReplacement = false;
  }

  public static BlockSituationEx newInvisibleBlock() {
    return new BlockSituationEx();
  }

  @Override
  public String toString() {
    return "BlockSituationEx [origin=[" + (origin == null ? "null" : (origin.x + ":" + origin.y)) + "], isChainReplacement = " + isChainReplacement() + ", costs=" + targetColumnCosts + "]";
  }

  public void setChainReplacement(final boolean comboReplacement) {
    chainReplacement = comboReplacement;
  }

  public final boolean isChainReplacement() {
    return chainReplacement;
  }

  public final boolean isReplacement() {
    return !targetColumnCosts.isEmpty();
  }
}
```

Generated the 2 following files:

### BlockSituationEx.h:
```c++
#ifndef __BlockSituationEx_H_
#define __BlockSituationEx_H_

#include <cstdint>
#include <map>
#include "BlockSituation.h"
#include "String.h"
#include "BlockSituationEx.h"
#include "Point.h"

using namespace std;

class BlockSituationEx : BlockSituation
{
private:
    map<int32_t,int32_t> targetColumnCosts;
    Point* origin = nullptr;
    bool chainReplacement = false;
    BlockSituationEx();

protected:
public:
    BlockSituationEx(BlockSituation* bs);
    BlockSituationEx(BlockSituationEx* bsEx);
    int32_t getTargetColumnCost(int32_t targetColumn);
    void addTargetCost(int32_t targetColumn, int32_t cost);
    Point* getOrigin();
    void setOrigin(Point* position);
    void reset();
    static BlockSituationEx* newInvisibleBlock();
    void setChainReplacement(bool comboReplacement);
    bool isChainReplacement();
    bool isReplacement();
};

#endif //__BlockSituationEx_H_
```

### BlockSituationEx.cpp:
```c++

#include <cstdint>
#include <map>
#include "BlockSituation.h"
#include "String.h"
#include "BlockSituationEx.h"
#include "Point.h"
#include "BlockSituationEx.h"

using namespace std;

BlockSituationEx::BlockSituationEx(BlockSituation* bs)
{
    targetColumnCosts = new map<int32_t,int32_t>();
    chainReplacement = false;
}

BlockSituationEx::BlockSituationEx(BlockSituationEx* bsEx)
{
    origin = bsEx->getOrigin();
    targetColumnCosts = bsEx->targetColumnCosts;
    chainReplacement = bsEx->chainReplacement;
}

BlockSituationEx::BlockSituationEx()
{
    targetColumnCosts = new map<int32_t,int32_t>();
}

int32_t BlockSituationEx::getTargetColumnCost(int32_t targetColumn)
{
    int32_t cost = targetColumnCosts->get(targetColumn);
    return cost == nullptr ? ComboProcessor::INFINITY : cost;
}

void BlockSituationEx::addTargetCost(int32_t targetColumn, int32_t cost)
{
    targetColumnCosts->put(targetColumn, cost);
}

Point* BlockSituationEx::getOrigin()
{
    return origin;
}

void BlockSituationEx::setOrigin(Point* position)
{
    origin = position;
}

void BlockSituationEx::reset()
{
    origin = nullptr;
    targetColumnCosts->clear();
    chainReplacement = false;
}

BlockSituationEx* BlockSituationEx::newInvisibleBlock()
{
    return new BlockSituationEx();
}

void BlockSituationEx::setChainReplacement(bool comboReplacement)
{
    chainReplacement = comboReplacement;
}

bool BlockSituationEx::isChainReplacement()
{
    return chainReplacement;
}

bool BlockSituationEx::isReplacement()
{
    return !targetColumnCosts->isEmpty();
}
```

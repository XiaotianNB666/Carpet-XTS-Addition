# Carpet XTS Addition · 规则文档（简体中文）

> 所有规则默认均为原版行为，**只有显式开启后才会生效**。

---

## 分类说明

| 分类标签           | 含义                         |
|----------------|----------------------------|
| `XTS`          | Carpet XTS Addition分类      |
| `SURVIVAL`     | 生存玩法向，会改变生存模式下的可操作边界       |
| `EXPERIMENTAL` | 实验性特性，语义与实现可能随版本调整，生产服请先测试 |

---

## 规则列表

### （创造模式飞行）creativeFlight

允许生存模式玩家像创造模式一样飞行。

持有**烟花火箭**并穿着**鞘翅**即可起飞，燃料消耗由 `creativeFlightFuelBase` 决定；没有燃料或鞘翅时立即掉落。

- 类型：`boolean`
- 默认值：`false`
- 选项：`false`，`true`
- 分类：`XTS`，`SURVIVAL`

---

### （创造飞行燃料基数）creativeFlightFuelBase

一支数量为 N 的烟花火箭，可提供 `creativeFlightFuelBase × (2 + N)` 游戏刻（gt）的飞行燃料。

举例（默认基数 10）：

- 1 级烟花 → 10 × 3 = **30 gt**（约 1.5 秒）
- 3 星烟花 → 10 × 5 = **50 gt**（约 2.5 秒）

- 类型：`int`
- 默认值：`10`
- 分类：`XTS`，`SURVIVAL`

---

### （创造飞行结束摔落有伤害）creativeFlightFallHasDamage

> 并非是 **飞行状态下跌落免疫摔伤**

开启创造飞行后，从高空跌落是否仍按原版规则计算摔落伤害。

- `true`（默认）：创造飞行关闭后，受到摔落伤害
- `false`：关闭后不免疫摔伤，注意这并不会免疫其他伤害来源。

与 `creativeFlight` 搭配食用。

- 类型：`boolean`
- 默认值：`true`
- 选项：`false`，`true`
- 分类：`XTS`，`SURVIVAL`

---

### （假人操控）fakePlayerControl
> 从 [`player control`](https://github.com/XiaotianNB666/playercontrol) 模组移植而来

控制玩家是否能操控假人。

支持两种写法：

- **权限等级**：填 `0` / `1` / `2` / `3` / `4` / `ops`，只有权限等级 ≥ 该值的玩家可以操控假人
#### 开启后
`/player <player> control`可以控制假人（真人玩家控制有一点问题）

- 类型：`String`
- 默认值：`false`
- 选项：`false`，`0`，`1`，`2`，`3`，`4`，`ops`
- 分类：`XTS`，`EXPERIMENTAL`

---
### （TP 指令集合）tpCommandCollection

启用与tp有关指令，如`/tpa` `/home` `/sethome` `/delhome` `/back` *(也许完成了)*

- 类型：`String`
- 默认值：`false`
- 选项：`false`，`0`，`1`，`2`，`3`，`4`，`ops`
- 分类：`XTS`，`COMMAND`

---
### (更好的纹饰) `betterArmorTrims`
~~Make armor trims great again.~~

当纹饰材料为**金**时，猪灵不会攻击玩家

- 类型：`boolean`
- 默认值：`true`
- 选项：`false`，`true`
- 分类：`XTS`，`SURVIVAL`
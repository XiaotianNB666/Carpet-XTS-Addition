# Carpet XTS Addition

---

## 简介 / Introduction

> **English** — A [Fabric Carpet](https://github.com/gnembon/fabric-carpet) extension
> originally built for the **XTS Server**, and useful for any Minecraft server that wants
> a tighter leash on ops *and* a few genuinely handy player-side features.
>
> **中文** — 一个为 **XTS 服务器** 诞生的 [Fabric Carpet](https://github.com/gnembon/fabric-carpet)
> 附属包，同样适合任何想让管理更省心、又给玩家加点实用功能的 Minecraft 服务端。

**Defaults are fully vanilla. Nothing changes until you flip a rule.**
**默认全程原版行为，不手动开规则就不会有任何变化。**

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](./LICENSE)
![Category](https://img.shields.io/badge/carpet%20category-XTS-blue)
![Surface](https://img.shields.io/badge/surface-server%20side-lightgrey)

---

## 文档 / Docs

|                        |                                          |
|------------------------|------------------------------------------|
| 🇨🇳 简体中文规则文档          | [docs/lang/zh_cn.md](docs/lang/zh_cn.md) |
| 🇺🇸 English Rule Docs | [docs/lang/en_us.md](docs/lang/en_us.md) |

---

## 规则速查 / Quick Reference

| Rule                          | 中文名       | 类型      | 默认      | 分类                    |
|-------------------------------|-----------|---------|---------|-----------------------|
| `creativeFlight`              | 创造模式飞行    | boolean | `false` | `XTS`, `SURVIVAL`     |
| `creativeFlightFuelBase`      | 创造飞行燃料基数  | int     | `10`    | `XTS`, `SURVIVAL`     |
| `creativeFlightFallHasDamage` | 飞行结束摔落有伤害 | boolean | `true`  | `XTS`, `SURVIVAL`     |
| `fakePlayerControl`           | 假人操控      | String  | `false` | `XTS`, `EXPERIMENTAL` |
| `tpCommandCollection`         | TP 指令集合   | String  | `false` | `XTS`, `COMMAND`      |
| `betterArmorTrims`            | 更好的纹饰     | boolean | `false` | `XTS`, `SURVIVAL`     |

详细语义、选项与坑点见上方文档链接。
See the docs above for full semantics, options and caveats.

---

## License

This project is available under the MIT license. Feel free to learn from it and incorporate it in your own projects.
本项目基于 MIT 许可证发布，欢迎借鉴并用于你自己的项目。
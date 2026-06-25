# 智能断充（HyperSmartCharge）
为支持**电池充电保护**的设备添加自定义断充阈值设置，妈妈再也不用担心我的手机过充啦！

Add custom charge protect value setting for devices that support **battery charge protection**.

## Fork 说明

本仓库是 [buffcow/HyperSmartCharge](https://github.com/buffcow/HyperSmartCharge) 的 fork，感谢原作者 [@buffcow](https://github.com/buffcow) / qingyu 提供原始模块和适配工作。

本 fork 主要针对部分 HyperOS 2 设备上“启动后自定义断充阈值被系统覆盖”的情况做小幅修正。实际排查中发现，模块在安全服务启动阶段已经成功写入自定义阈值，例如 55% 对应的 `0x370011`，但随后小米安全服务或电池智能逻辑可能再次写入其它 `smart_chg` 值，导致界面看起来像是需要手动重新设置。

本 fork 的改动：

1. 检查当前 `smart_chg` 是否等于已保存的自定义阈值，而不是只在值为 `0` 时才补写。
2. 在 `com.miui.securitycenter.remote` 注册电池广播后延迟 5 秒做一次复查；如果发现阈值被系统覆盖，就按当前保存的阈值重写一次。
3. 保持阈值动态读取，用户仍可随时在界面中把 55% 改成其它支持范围内的值。

这个改动不保证所有设备都能真正停止充电；模块仍然依赖系统 `miui.util.IMiCharge` / 底层充电服务对 `smart_chg` 的实现。

> [!WARNING]  
> 本模块的使用所产生的所有后果，由使用者自行承担，项目组不承担任何责任，使用者应自行评估并承担相关风险。  
> 本项目不对任何任何衍生项目负责。

## 使用
1. 在 LSPosed 管理器中激活模块
2. 作用域勾选 安全服务(**`com.miui.securitycenter`**)
3. **重启手机**
4. 转到 **`省电与电池`** —— **`电池保护`** —— **`智能断充`** 设置阈值

## 注意
1. 需要设备本身支持电池保护功能并搭载 HyperOS 的系统
2. 支持调节范围为 20-100，推荐设置在 70-90 之间
3. 目前仅在 小米14(pro) 下测试通过

## 下载
[LSPosed 仓库](https://github.com/Xposed-Modules-Repo/cn.buffcow.hypersc/releases)

## 无效
请先检查设备是否支持电池保护功能，模块是否正常激活，并且作用域是否勾选。
<br>如果排查后仍有错误，请提交issue。或联系酷安[@buffcow](http://www.coolapk.com/u/1188320)(qingyu)

## 致谢
原项目由 [@buffcow](https://github.com/buffcow) / qingyu 开发与维护。

模块使用 [Yuki Hook API](https://github.com/fankes/YukiHookAPI) 构建。

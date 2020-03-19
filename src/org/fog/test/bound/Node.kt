package org.fog.test.bound

import kotlin.math.pow

data class PowerConsumption(val calcPower: Double, val transmitPower: Double)
data class Delay(val calcDelay: Double, val transmitDelay: Double)

class Node(
  // 最小运行频率
  minFrequency: Double,
  // 最大运行频率
  maxFrequency: Double,
  // 每时钟周期指令数
  private val alpha: Double,
  // 功耗系数，有CPU架构决定
  private val beta: Double,
  // 传输功率
  val transmitPower: Double,
  // 功耗模型
  powerModel: (node: Node) -> (task: Task, index: Int) -> PowerConsumption,
  // 延迟模型
  delayModel: (node: Node) -> (task: Task, index: Int) -> Delay,
  // 节点下接入孩子的个数，影响等待队列的因素
  val children: Int = 0
  ) {
  // 转换为Mips
  val frequencyToMips = { frequency: Double ->
    alpha * frequency
  }

  val mipsToPower = { mips: Double ->
    val frequency = mips / alpha
    when {
      frequency < minFrequency -> beta * minFrequency.pow(2)
      else -> beta * frequency.pow(2)
    }
  }

  val minMips = frequencyToMips(minFrequency)
  val maxMips = frequencyToMips(maxFrequency)

  val getPower = powerModel(this)
  val getDelay = delayModel(this)

}

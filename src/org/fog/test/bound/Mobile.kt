package org.fog.test.bound

import kotlin.math.log2

val wirelessTransmitBandwidth = {
  transmitPower: Double ->
  // 香农公式
  90 * log2(1 + 0.6 * transmitPower)
}

val mobilePowerModel = { node: Node ->
  { task: Task, index: Int ->
    val mips = task.subTaskMinMips(index)

    val calcPower = when {
      mips <= node.maxMips -> node.mipsToPower(mips) * task.subTaskDelay(index)

      else -> Double.MAX_VALUE
    }
    val transmitPower = task.size / wirelessTransmitBandwidth(node.transmitPower) * node.transmitPower

    PowerConsumption(calcPower, transmitPower)
  }
}

val mobileDelayModel = { node: Node ->
  { task: Task, index: Int ->
    val mips = task.subTaskMinMips(index)

    val calcDelay = when {
      mips <= node.maxMips -> task.subTaskDelay(index)

      else -> Double.MAX_VALUE
    }
    val transmitDelay = task.size / wirelessTransmitBandwidth(node.transmitPower)

    Delay(calcDelay, transmitDelay)
  }
}

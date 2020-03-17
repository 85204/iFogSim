package org.fog.test.bound

import kotlin.math.log2

val wireTransmitBandwidth = {
  transmitPower: Double ->
  // 香农公式
  0.99 * log2(1 + 0.95 * transmitPower)
}


val serverPowerModel = { node: Node ->
  { task: Task, index: Int ->
    val mi = task.length[index]

    val calcPower = mi / node.maxMips * node.mipsToPower(node.maxMips)
    val transmitPower = task.size / wireTransmitBandwidth(node.transmitPower) * node.transmitPower

    PowerConsumption(calcPower, transmitPower)
  }
}

val serverDelayModel = { node: Node ->
  { task: Task, index: Int ->
    val mi = task.length[index]

    val calcDelay = mi / node.maxMips
    val transmitDelay = task.size / wireTransmitBandwidth(node.transmitPower)

    Delay(calcDelay, transmitDelay)
  }
}

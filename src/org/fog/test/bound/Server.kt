package org.fog.test.bound

import kotlin.math.log2
import kotlin.random.Random

val wireTransmitBandwidth = { transmitPower: Double ->
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

var maxWaitDelay: Double = 18.0
val updateWaitDelay = {
  maxWaitDelay = Random.nextDouble(0.0, 20.0)
}

val serverDelayModel = { node: Node ->
  { task: Task, index: Int ->
    val mi = task.length[index]

    val waitDelay = node.children * maxWaitDelay
    val calcDelay = mi / node.maxMips
    val transmitDelay = task.size / wireTransmitBandwidth(node.transmitPower)

    Delay(waitDelay + calcDelay, transmitDelay)
  }
}

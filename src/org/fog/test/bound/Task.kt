package org.fog.test.bound

data class Task(
  // 传输量
  val size: Double,
  // 计算量
  val length: Array<Long>,
  // 最大延迟
  val maxDelay: Double
) {
  val sumLength: Long = this.length.reduce { acc, l ->  acc + l}
}

// 子任务容忍延迟
fun Task.subTaskDelay(index: Int): Double {
  return this.maxDelay * this.length[index] / this.sumLength
}

// 子任务的所需最小Mips，若不满足则会超过容忍延迟
fun Task.subTaskMinMips(index: Int): Double {
  return this.length[index] / this.subTaskDelay(index)
}

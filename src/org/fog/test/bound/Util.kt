package org.fog.test.bound

data class TreeNode(
  // 子任务序号
  var taskIndex: Int = -1,
  // 执行任务的设备
  val device: Int = -1,
  // 目前消耗的功耗数
  var currentPowerConsumption: Double = 0.0,
  // 目前已经花费的延迟
  var currentDelay: Double = 0.0,
  val children: ArrayList<TreeNode> = ArrayList(),
  // 已部署任务节点，部署后不需要计算传输功耗及延迟
  var taskDeployed: Int = 0
)

fun createTree(deep: Int, size: Int, root: TreeNode = TreeNode()): TreeNode {
  if (deep != 0) {
    root.children.addAll(Array(size) {
      val node = TreeNode(taskLength - deep, it)
      createTree(deep - 1, size, node)
    })
  }
  return root
}

data class Result(
  val power: Double,
  val delay: Double,
  val count: Long = 1
) {
  override fun toString(): String {
    return """ ${String.format("%9.2f", power)}, ${String.format("%9.2f", delay)}"""
  }
}

fun Result.acc(result: Result): Result {
  return Result(this.power + result.power, this.delay + result.delay, this.count + result.count)
}

fun Result.adv(): Result {
  return Result(this.power / this.count, this.delay / this.count)
}

const val ANSI_GREEN = "\u001B[32m"
const val ANSI_RED = "\u001b[31m"
const val ANSI_RESET = "\u001B[0m"

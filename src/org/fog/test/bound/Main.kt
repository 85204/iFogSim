package org.fog.test.bound

import kotlin.random.Random

data class TreeNode(
  // 子任务序号
  var taskIndex: Int = -1,
  // 目前消耗的功耗数
  var currentPowerConsumption: Double = 0.0,
  // 目前已经花费的延迟
  var currentDelay: Double = 0.0,
  val children: ArrayList<TreeNode> = ArrayList(),
  // 已部署任务节点，部署后不需要计算传输功耗及延迟
  var taskDeployed: Int = 0
)

// 更新节点数据
fun updateNode(task: Task, structure: Array<Node>, parentNode: TreeNode, taskIndex: Int, deviceIndex: Int, node: TreeNode) {
  // 传递已经部署任务的层
  node.taskDeployed = parentNode.taskDeployed

  node.currentPowerConsumption = parentNode.currentPowerConsumption + when (deviceIndex) {
    // 如果是本地计算
    0 -> structure[deviceIndex].getPower(task, taskIndex).calcPower
    // 其他情况检查是否为从Mobile到edge，只计算一次传输功耗，后续不计
    else -> {
      if (deviceIndex > node.taskDeployed && node.taskDeployed == 0)
        structure[0].getPower(task, taskIndex).transmitPower
      else
        0.0
    }
  }

  val delay = structure[deviceIndex].getDelay(task, taskIndex)
  node.currentDelay = parentNode.currentDelay + when (deviceIndex) {
    0 -> delay.calcDelay
    else -> {
      // 初始随机数来模拟等待队列
      val calcDelay = Random.nextDouble(0.0, 100.0) + structure[deviceIndex].getDelay(task, taskIndex).calcDelay
      // 若节点没有部署任务则需要增加传输延迟
      if (deviceIndex > node.taskDeployed) {
        // 可能需要跨层级连续传输任务，delay需要分别累加
        var sumDelay = calcDelay
        for (deep in node.taskDeployed until deviceIndex) {
          sumDelay += structure[deep].getDelay(task, taskIndex).transmitDelay
        }
        // 更新部署任务的节点层级
        node.taskDeployed = deviceIndex
        sumDelay
      } else
        calcDelay
    }
  }
}

fun branchAndBound(task: Task, structure: Array<Node>, root: TreeNode): Double {

  // 可行解
  var result = Double.MAX_VALUE
  fun rotate(node: TreeNode, taskIndex: Int = 0) {
    node.children.forEachIndexed { deviceIndex, it ->

      updateNode(task, structure, node, taskIndex, deviceIndex, it)

      // 值有效
      if (result > it.currentPowerConsumption && it.currentDelay <= task.maxDelay) {
        if (it.children.isEmpty())
        // 叶子节点，更新结果
          result = it.currentPowerConsumption
        else
        // 如果不是叶子节点说明任务还没有完成，并且延迟可以使用，继续递归
          rotate(it, taskIndex + 1)
      }
    }
  }

  rotate(root)
  return result
}

fun greedy(task: Task, structure: Array<Node>, root: TreeNode): Double {
  var result = 0.0

  fun rotate(node: TreeNode, taskIndex: Int = 0) {
    // 更新所有可能情况的数据
    node.children.forEachIndexed { deviceIndex, it ->
      updateNode(task, structure, node, taskIndex, deviceIndex, it)
    }

    // 贪婪的选择最优解
    var selectedNode = node.children[0]
    node.children.forEach {
      if (it.currentDelay < task.maxDelay && it.currentPowerConsumption < selectedNode.currentPowerConsumption)
        selectedNode = it
    }

    // 到叶子节点就输出结果，不到就递归求解
    if (selectedNode.children.size != 0)
      rotate(selectedNode, taskIndex + 1)
    else
      result = selectedNode.currentPowerConsumption
  }

  rotate(root)
  return result
}

const val taskLength = 3

fun createTree(deep: Int, size: Int, root: TreeNode = TreeNode()): TreeNode {
  if (deep != 0) {
    root.children.addAll(Array(size) {
      val node = TreeNode(taskLength - deep)
      createTree(deep - 1, size, node)
    })
  }
  return root
}

val mobile = Node(800.0, 2800.0, 10.0,
  0.01 / 8, 2.0, mobilePowerModel, mobileDelayModel)

val edge = Node(8000.0, 38000.0, 11.0,
  0.02, 4.0, serverPowerModel, serverDelayModel)

val cloud = Node(80000.0, 380000.0, 12.0,
  0.03, 4.0, serverPowerModel, serverDelayModel)

val task = Task(1000.0, arrayOf(2000, 3000, 4000), 100.0)

val structure = arrayOf(mobile, edge, cloud)

val devicesName = arrayOf("mobile", "edge", "cloud")

const val runTimes = 99000

fun main() {
  var root = createTree(task.length.size, structure.size)
  var result = 0.0
  for (i in 1..runTimes) {
    root = createTree(task.length.size, structure.size)
    result += branchAndBound(task, structure, root)
  }

  println("branch and bound result: ${String.format("%8.2f", result / runTimes)}")
  printTree(root)
  println("================================================================")

  var result2 = 0.0
  for (i in 1..runTimes) {
    root = createTree(task.length.size, structure.size)
    result2 += greedy(task, structure, root)
  }

  println("greedy result: ${String.format("%8.2f", result2 / runTimes)}")
  printTree(root)
}

const val ANSI_GREEN = "\u001B[32m"
const val ANSI_RESET = "\u001B[0m"

fun printTree(root: TreeNode, devicePath: Array<Int> = arrayOf()) {
  if (devicePath.isNotEmpty()) {
    val isLeaf = devicePath.size == 3 && root.currentPowerConsumption != 0.0
    println("""
      ${if (isLeaf) ANSI_GREEN else ""}
      device path: ${devicePath.map { devicesName[it] }}
      power: ${String.format("%8.2f", root.currentPowerConsumption)}
      delay: ${String.format("%8.2f", root.currentDelay)}
      ${if (isLeaf) ANSI_RESET else ""}""".trimIndent())
  }
  root.children.forEachIndexed { i, it ->
    printTree(it, arrayOf(*devicePath, i))
  }
}

package org.fog.test.bound

// 更新节点数据
fun updateNode(task: Task, structure: Array<Node>, parentNode: TreeNode, taskIndex: Int, deviceIndex: Int, node: TreeNode) {
  // 传递已经部署任务的层
  node.taskDeployed = parentNode.taskDeployed

//  if (taskIndex == 3 && deviceIndex == 2) {
//    println()
//  }

  node.currentPowerConsumption = parentNode.currentPowerConsumption + when (deviceIndex) {
    // 如果是本地计算
    0 -> structure[0].getPower(task, taskIndex).calcPower
    // 其他情况检查是否为从Mobile到edge，只计算一次传输功耗，后续不计
    else -> {
      if (deviceIndex > node.taskDeployed && node.taskDeployed == 0) {
        structure[0].getPower(task, 0).transmitPower
      } else
        0.0
    }
  }

  val delay = structure[deviceIndex].getDelay(task, taskIndex)
  node.currentDelay = parentNode.currentDelay + when (deviceIndex) {
    0 -> delay.calcDelay
    else -> {
      val calcDelay = structure[deviceIndex].getDelay(task, taskIndex).calcDelay
      // 若节点没有部署任务则需要增加传输延迟
      if (deviceIndex > node.taskDeployed) {
        // 可能需要跨层级连续传输任务，delay需要分别累加
        var sumDelay = calcDelay
        for (deep in node.taskDeployed until deviceIndex) {
          sumDelay += structure[deep].getDelay(task, 0).transmitDelay
        }
        // 更新部署任务的节点层级
        node.taskDeployed = deviceIndex
        sumDelay
      } else
        calcDelay
    }
  }
}

fun branchAndBound(task: Task, structure: Array<Node>, root: TreeNode): Result {

  // 可行解
  var result = Result(Double.MAX_VALUE, Double.MAX_VALUE)
  fun rotate(node: TreeNode, taskIndex: Int = 0) {
    node.children.forEachIndexed { deviceIndex, it ->

      updateNode(task, structure, node, taskIndex, deviceIndex, it)

      // 值有效
      if (result.power > it.currentPowerConsumption && it.currentDelay <= task.maxDelay) {
        if (it.children.isEmpty()) {
          // 叶子节点，更新结果
          result = Result(it.currentPowerConsumption, it.currentDelay)
        } else
        // 如果不是叶子节点说明任务还没有完成，并且延迟可以使用，继续递归
          rotate(it, taskIndex + 1)
      }
    }
  }

  rotate(root)
  if (result.power >= Double.MAX_VALUE) {
    throw Error("找不到可行解")
  }
  return result
}

fun greedy(task: Task, structure: Array<Node>, root: TreeNode): Result {
  var result = Result(0.0, 0.0)

  fun rotate(node: TreeNode, taskIndex: Int = 0): Boolean {
    // 更新所有可能情况的数据
    node.children.forEachIndexed { deviceIndex, it ->
      updateNode(task, structure, node, taskIndex, deviceIndex, it)
    }

    // 贪婪的选择最优解
    node.children.sortBy { it.currentPowerConsumption }

    val n = node.children.find {
      // 到叶子节点就输出结果，不到就递归求解
      if (it.children.size != 0) {
        return@find rotate(it, taskIndex + 1)
      } else if (it.currentDelay <= task.maxDelay) {
        result = Result(it.currentPowerConsumption, it.currentDelay)
        return@find true
      }
      false
    }
    return n != null
  }

  rotate(root)
//  if (result.delay > task.maxDelay) {
//    throw Error("找到的解超过最大延迟")
//  }
  return result
}

const val taskLength = 3

const val DEBUG = false

val runTimes: Int = if (DEBUG) 1 else 100

fun run(count: Int = 1, task: Task): Triple<Int, Result?, Result?> {
  val mobile = Node(800.0, 2800.0, 10.0,
    0.01 / 8, 2.0, mobilePowerModel, mobileDelayModel)

  val edge = Node(8000.0, 38000.0, 11.0,
    0.02, 4.0, serverPowerModel, serverDelayModel, 2)

  val cloud = Node(80000.0, 380000.0, 12.0,
    0.03, 4.0, serverPowerModel, serverDelayModel, count)

// 可变的量 层级数量，每一层的参数
  val structure = arrayOf(mobile, edge, cloud)

  val devicesName = arrayOf("mobile", "edge", "cloud")

  var root: TreeNode
  var result: Result? = null
  var result2: Result? = null

  fun printTree(root: TreeNode, devicePath: Array<Int> = arrayOf()) {
    if (devicePath.isNotEmpty()) {
      val isLeaf = devicePath.size == taskLength && root.currentPowerConsumption != 0.0
      val isLegal = root.currentDelay <= task.maxDelay
      println("""
      ${if (isLeaf) {
        if (isLegal) ANSI_GREEN else ANSI_RED
      } else ""}
      device path: ${devicePath.map { devicesName[it] }}
      power: ${String.format("%8.2f", root.currentPowerConsumption)}
      delay: ${String.format("%8.2f", root.currentDelay)}
      ${if (isLeaf) ANSI_RESET else ""}""".trimIndent())
    }
    root.children.forEachIndexed { _, it ->
      printTree(it, arrayOf(*devicePath, it.device))
    }
  }

  for (i in 1..runTimes) {
    try {
      root = createTree(task.length.size, structure.size)
      var newResult = greedy(task, structure, root)
      result2 = result2?.acc(newResult) ?: newResult
      if (DEBUG) {
        printTree(root)
        println("=======================================")
      }

      root = createTree(task.length.size, structure.size)
      newResult = branchAndBound(task, structure, root)
      result = result?.acc(newResult) ?: newResult
      if (DEBUG) printTree(root)

    } catch (e: Error) {
    }
    updateWaitDelay()
    Thread.sleep(3)
  }
//  println("mobile 数量: $mobileCount")
//  println("branch and bound: ${result ?: "找不到可行解"}")
//  println("greedy: ${result2 ?: "找到的解超过最大延迟"}")
//  println("$mobileCount,$result,$result2")
  return Triple(count, result, result2)
}

fun main() {
  val result = arrayOf(*Array(8) { Triple(it + 1, Result(0.0, 0.0), Result(0.0, 0.0)) })
  val paramRange = if (DEBUG) 1..1 else 1..8 step 1
  while (true) {
    for (param in paramRange) {
      // 可变的量 任务传输大小 任务计算量 子任务数量 容忍延迟
      val task = Task(1000.0, arrayOf(*Array(taskLength) { (it + 1).toLong() * 500000 }), 200.0)

      val (count, r1, r2) = run(task = task, count = param)
      if (DEBUG)
        println("$count, $r1, $r2")
      else {
        val updateValue = result[count - 1]
        result[count - 1] = Triple(count, updateValue.second.acc(r1!!), updateValue.third.acc(r2!!))
      }
    }
    if (DEBUG) break

    Cls.cls()
    println("          branchAndBound               greedy")
    println("num     power      delay      power     delay")
    result.forEach {
      val (c, b, g) = it
      println("$c,${b.adv()},${g.adv()}")
    }
  }
}

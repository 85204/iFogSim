package org.fog.test.bound

// 更新节点数据
fun updateNode(task: Task, structure: Array<Node>, parentNode: TreeNode, taskIndex: Int, deviceIndex: Int, node: TreeNode) {
  // 传递已经部署任务的层
  node.taskDeployed = parentNode.taskDeployed

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

fun greedy(task: Task, structure: Array<Node>, root: TreeNode, track: Boolean = true, sortByConsumption: Boolean = true): Result {
  var result: Result? = null

  fun rotate(node: TreeNode, taskIndex: Int = 0): Boolean {
    // 更新所有可能情况的数据
    node.children.forEachIndexed { deviceIndex, it ->
      updateNode(task, structure, node, taskIndex, deviceIndex, it)
    }

    // 贪婪的选择最优解
    if (sortByConsumption)
      node.children.sortBy { it.currentPowerConsumption }
    else
      node.children.sortBy { it.currentDelay }

    val n = node.children.find {
      when {
        // 到叶子节点就输出结果，不到就递归求解
        it.children.size != 0 -> rotate(it, taskIndex + 1)
        it.currentDelay <= task.maxDelay || !track -> {
          result = Result(it.currentPowerConsumption, it.currentDelay)
          true
        }
        else -> false
      }
    }
    return n != null
  }

  rotate(root)
  val r = result
  if (r == null) {
    throw Error("找不到可行解")
  } else {
    if (r.delay > task.maxDelay) {
      return r.punish()
    }
    return r
  }
}

const val taskLength = 5

const val DEBUG = false

val runTimes: Int = if (DEBUG) 1 else 100

fun run(count: Int = 1, task: Task): Array<Result?> {
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
  var branchAndBoundResult: Result? = null
  var greedyResult: Result? = null
  var greedyWithoutTrackResult: Result? = null
  var greedyWithoutTrackByDelayResult: Result? = null

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

  fun runPolicy(policy: (root: TreeNode) -> Result, oldResult: Result?, print: Boolean = false): Result {
    root = createTree(task.length.size, structure.size)
    val result = policy(root)
    val newResult = oldResult?.acc(result) ?: result
    if (DEBUG && print) {
      printTree(root)
      println("=======================================")
    }
    return newResult
  }

  for (i in 1..runTimes) {
    try {
      greedyWithoutTrackByDelayResult = runPolicy({
        greedy(task, structure, it, false, sortByConsumption = false)
      }, greedyWithoutTrackByDelayResult)

      greedyWithoutTrackResult = runPolicy({
        greedy(task, structure, it, track = false, sortByConsumption = true)
      }, greedyWithoutTrackResult, true)

      greedyResult = runPolicy({
        greedy(task, structure, it)
      }, greedyResult)

      branchAndBoundResult = runPolicy({
        branchAndBound(task, structure, it)
      }, branchAndBoundResult)

    } catch (e: Error) {
    }
    updateWaitDelay()
    Thread.sleep(3)
  }
  return arrayOf(branchAndBoundResult, greedyResult, greedyWithoutTrackResult, greedyWithoutTrackByDelayResult)
}

data class Results(
  val param: Int,
  val results: Array<Result>
)

fun Int.toRange(): IntRange {
  return this..this
}

fun main() {
  val result = arrayOf(*Array(10) { Results(it + 1, arrayOf(*Array(4) { Result(0.0, 0.0) })) })
  val paramRange = if (DEBUG) 4000.toRange() else 1000..9000 step 1000
  while (true) {
    for ((i, param) in paramRange.withIndex()) {
      // 可变的量 任务传输大小 任务计算量 子任务数量 容忍延迟
      val task = Task(param.toDouble(), arrayOf(*Array(taskLength) { (it + 1).toLong() * 5000 }), 200.0)

      val r = run(task = task, count = 2)
      if (DEBUG)
        println("$param, ${r.joinToString { it?.toString() ?: "" }}")
      else {
        val updateValue = result[i]
        result[i] = Results(param, updateValue.results.mapIndexed { ii, it -> it.acc(r[ii]!!) }.toTypedArray())
      }
    }
    if (DEBUG) break

    Cls.cls()
    println("             branchAndBound                 greedy     greedyWithoutTrack greedyWithoutTrackByDelay")
    println("param      power      delay        power     delay        power     delay        power     delay")
    result.forEach {
      println("${it.param},${it.results.map { r -> r.adv() }}")
    }
  }
}

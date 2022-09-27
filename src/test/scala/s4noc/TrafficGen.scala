package s4noc

import scala.collection.mutable
import scala.util.Random

/**
  *
  * @param n all cores (not n x n)
  */
class TrafficGen(n: Int) {

  val queuesSplit = Array.ofDim[mutable.Queue[Int]](n, n)
  val queues = Array.ofDim[mutable.Queue[(Int, Int)]](n)

  var injectionRate = 0.2 * n
  val r = new Random(0)
  private var countCycles = 0
  var inserted = 0

  def reset()  = {
    inserted = 0
    for (i <- 0 until n) {
      for (j <- 0 until n) {
        queuesSplit(i)(j) = new mutable.Queue[Int]()
      }
      queues(i) = new mutable.Queue[(Int, Int)]()
    }
  }


  def insert(from: Int, to: Int, data: Int): Unit = {
    queuesSplit(from)(to).enqueue(data)
    queues(from).enqueue((data, to))
  }

  def getValue(from: Int, to: Int): Int = {
    val q = queuesSplit(from)(to)
    if (q.isEmpty) {
      -1
    } else {
      q.dequeue()
    }
  }

  def getValueFromSingle(from: Int): (Int, Int) = {
    val q = queues(from)
    if (q.isEmpty) {
      (-1, 0)
    } else {
      q.dequeue()
    }
  }

  reset()

  // This would be a direct call back into the Chisel tester
  // def tick(inject: (Int, Int) => Unit): Unit = {

  /**
    * Execute this once per clock cycle and call back for packet injection
    */
  def tick(doInsert: Boolean): Unit = {
    // remember which source and destination was taken in a cycle
    // no doubles
    val fromSet = mutable.Set[Int]()
    val toSet = mutable.Set[Int]()

    def getOne(s: mutable.Set[Int]): Int = {
      var one = r.nextInt(n)
      var count = 0
      while (s.contains(one)) {
        one = r.nextInt(n)
        // Maybe just drop that one?
        // if (count > 100 * n) throw new Exception("Livelock in getOne()")
        if (count > 100 * n) return -1
        count += 1
      }
      s += one
      one
    }

    /*
    while (inserted.toDouble / (countCycles + 1) < injectionRate) {
      inserted += 1
      val from = getOne(fromSet)
      var to = getOne(toSet)
      if (to == from) to = getOne(toSet)
      // println(s"$countCycles $inserted: $from -> $to")
      if (to != -1 && from != -1) {
        insert(from, to, (from << 24) | (to << 16) | countCycles)
      } else {
        dropped += 1
      }
    }
     */
    if (doInsert) {
      for (from <- 0 until n) {
        val rnd = r.nextDouble()
        if (rnd < injectionRate) {
          inserted += 1
          var to = r.nextInt(n)
          while (to == from) {
            to = r.nextInt(n)
          }
          insert(from, to, (from << 24) | (to << 16) | countCycles)
        }
      }
    }

    countCycles += 1
  }
}

object TrafficGen extends App {

  val n = 4
  val t = new TrafficGen(n)

  for (i <- 0 until 10) {
    t.tick(true)
    for (i <- 0 until n) {
      for (j <- 0 until n) {
        val data = t.getValue(i, j)
        if (data != -1) {
          println(s"At $data $i -> $j")
        }
      }
    }
  }
}

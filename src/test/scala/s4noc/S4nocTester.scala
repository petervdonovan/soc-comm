/*
  Tester for the S4NOC.

  Author: Martin Schoeberl (martin@jopdesign.com)
  license see LICENSE
 */

package s4noc

import chisel3._
import chisel3.iotesters.PeekPokeTester
// import Chisel._

// class S4nocTester (dut: S4noc) extends Tester(dut) {
  class S4nocTester (dut: S4noc) extends PeekPokeTester(dut) {

  def read(): Int = {
    poke(dut.io.cpuPorts(3).rd, 1)
    poke(dut.io.cpuPorts(3).addr, 3)
    val status = peek(dut.io.cpuPorts(3).rdData)
    println("status="+status.toString)
    var ret = 0
    step(1)
    // FIXME: why is the return value earlier visible than status?
    // if (true || status == 1) {
    if (status == 1) {
      poke(dut.io.cpuPorts(3).rd, 1)
      poke(dut.io.cpuPorts(3).addr, 0)
      ret = peek(dut.io.cpuPorts(3).rdData).toInt
      println("ret="+ret.toString)
      step(1)
      poke(dut.io.cpuPorts(3).rd, 1)
      poke(dut.io.cpuPorts(3).addr, 1)
      val from = peek(dut.io.cpuPorts(3).rdData)
      step(1)
    }
    ret
  }

  poke(dut.io.cpuPorts(0).wrData, 0xcafebabe)
  poke(dut.io.cpuPorts(0).addr, 0)
  poke(dut.io.cpuPorts(0).wr, 1)
  step(1)
  poke(dut.io.cpuPorts(0).wrData, -1)
  poke(dut.io.cpuPorts(0).addr, -1)
  poke(dut.io.cpuPorts(0).wr, 0)
  var done = false
  for (i <- 0 until 14) {
    val ret = read()
    if (!done) done = ret == 0xcafebabe
  }
  if (!done) throw new Exception("Should have read in core 3 what core 0 wrote")
}

object S4nocTester {
  def main(args: Array[String]): Unit = {
    iotesters.Driver.execute(Array[String](), () => new S4noc(4, 2, 2, 32)) { c => new S4nocTester(c) }
  }
}

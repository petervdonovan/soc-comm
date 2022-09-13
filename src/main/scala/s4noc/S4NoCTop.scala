package s4noc

import chisel3._


/**
  * Top level of the S4NOC with a CPU interface.
  * But uses the combinational version
  *
  * Author: Martin Schoeberl (martin@jopdesign.com)
  * license see LICENSE
  * @param conf
  */
class S4NoCTop(conf: Config) extends Module  {
  val io = IO(new Bundle {
    val cpuPorts = Vec(conf.n, new CpuPortCombIO(conf.width))
    val cycCnt = Output(UInt(32.W))
  })

  val s4noc = Module(new S4NoC(conf))

  for (i <- 0 until conf.n) {
    val ci = Module(new CpuInterfaceComb(UInt(conf.width.W), conf.width))
    s4noc.io.networkPort(i) <> ci.io.networkPort
    io.cpuPorts(i) <> ci.io.cpuPort
  }

  val cntReg = RegInit(0.U(32.W))
  cntReg := cntReg + 1.U
  io.cycCnt := cntReg
}


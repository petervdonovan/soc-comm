package soc

import chisel3._
import chisel3.util._
import s4noc.Entry

/**
  * CPU interface to two ready/valid channels with 32-bit data.
  * TODO: used anywhere? No!
  * TODO: compare with Chisel book version
  *
  */
class CpuInterfaceRV[T <: Data](private val addrWidth: Int, private val dt: T) extends Module {
  val io = IO(new Bundle {
    val cpuPort = new MemoryMappedIO(addrWidth)
    val readyValidChannel = new ReadyValidChannelsIO(dt)
  })

  val cp = io.cpuPort
  val tx = io.readyValidChannel.tx
  val rx = io.readyValidChannel.rx

  val idle :: read :: writeWait :: Nil = Enum(3)
  val stateReg = RegInit(idle)

  val rdyReg = RegInit(false.B)
  val dataReg = Reg(UInt(32.W))

  cp.ack := rdyReg

  // some default values
  cp.rdData := 0.U
  rx.ready := false.B

  // write
  tx.valid := false.B
  tx.bits := Mux(stateReg === idle, cp.wrData, dataReg)
  tx.valid := cp.wr || stateReg === writeWait


  rdyReg := false.B
  switch(stateReg) {
    is(idle) {
      when(cp.wr) {
        when (tx.ready) {
          rdyReg := true.B
        } .otherwise {
          dataReg := cp.wrData
          stateReg := writeWait
        }
      }
    }
    is (read) {

    }
    is (writeWait) {
      when (tx.ready) {
        rdyReg := true.B
        stateReg := idle
      }
    }
  }
}




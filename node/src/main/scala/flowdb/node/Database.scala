package flowdb.node

import java.nio.ByteBuffer
import java.nio.file.Path

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.Behaviors
import flowdb.common.command.Database.{CloseDatabase, WriteProtocol, WriteToDatabase}
import flowdb.common.model.RawRecord
import org.rocksdb.{Options, RocksDB}
import cats.implicits._

object Database {
  private val SIZE_OF_LONG = 8
  private val SIZES_OF_IPV4 = 4
  private val RECORD_BUFFER_SIZE = 10000

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def behavior(path: Path): Behavior[WriteProtocol] = Behaviors.receive { (_, _) =>
    val keyBuffer = ByteBuffer.allocate(SIZE_OF_LONG + SIZES_OF_IPV4)
    val recordBuffer = Array.ofDim[RawRecord](RECORD_BUFFER_SIZE)
    val opts = new Options().setCreateIfMissing(true)
    val db = RocksDB.open(opts, path.toString)
    opened(db, keyBuffer, recordBuffer, 0)
  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def opened(db: RocksDB,
             keyBuffer: ByteBuffer,
             recordBuffer: Array[RawRecord],
             currentBufferIndex: Int): Behavior[WriteProtocol] = Behaviors.receive { (ctx, msg) =>
    msg match {
      case CloseDatabase =>
        db.close()
        Behaviors.stopped
      case WriteToDatabase(record) =>
        if (currentBufferIndex === RECORD_BUFFER_SIZE) {
          writeRecords(db, keyBuffer, recordBuffer)
          opened(db, keyBuffer, recordBuffer, 0)
        } else {
          recordBuffer.update(currentBufferIndex, record)
          opened(db, keyBuffer, recordBuffer, currentBufferIndex + 1)
        }
    }
  }

  def writeRecords(db: RocksDB, keyBuffer: ByteBuffer, recordBuffer: Array[RawRecord]): Unit = {
    recordBuffer.foreach { r =>
      makeKey(r, keyBuffer)
      db.put(keyBuffer.array(), r.value)
    }
  }

  @SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
  def makeKey(raw: RawRecord, buffer: ByteBuffer): Unit = {
    buffer.clear()
    buffer.putLong(raw.timestamp)
    buffer.put(raw.ip, SIZE_OF_LONG, raw.ip.length)
    ()
  }

}

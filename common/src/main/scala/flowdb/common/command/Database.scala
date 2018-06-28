package flowdb.common.command

import flowdb.common.model.RawRecord

object Database {
  sealed trait WriteProtocol
  final case object CloseDatabase extends WriteProtocol
  final case class WriteToDatabase(record: RawRecord) extends WriteProtocol
}

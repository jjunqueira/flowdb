package flowdb.common.command

import flowdb.common.model.RawRecord

object Writes {
  final case class Write(record: RawRecord)
}

package flowdb.common.model

@SuppressWarnings(Array("org.wartremover.warts.ArrayEquals"))
final case class RawRecord(timestamp: Long, ip: Array[Byte], value: Array[Byte])

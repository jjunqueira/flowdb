package flowdb.common.command

import com.comcast.ip4s.{Cidr, IpAddress}

import scala.concurrent.duration.Duration

object Reads {
  sealed trait Query {
    def startTimestamp: Long
    def endTimestamp: Long
    def maxRecords: Int
    def maxQueryTime: Duration
  }
  final case class CidrQuery(cidr: Cidr[IpAddress],
                             startTimestamp: Long,
                             endTimestamp: Long,
                             maxRecords: Int,
                             maxQueryTime: Duration)
      extends Query
  final case class IpQuery(ip: IpAddress,
                           startTimestamp: Long,
                           endTimestamp: Long,
                           maxRecords: Int,
                           maxQueryTime: Duration)
      extends Query
}

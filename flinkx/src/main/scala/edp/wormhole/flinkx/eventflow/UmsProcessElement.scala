package edp.wormhole.flinkx.eventflow

import edp.wormhole.common.json.{FieldInfo, JsonParseUtils}
import edp.wormhole.flinkx.common.ExceptionConfig
import edp.wormhole.flinkx.util.FlinkSchemaUtils
import edp.wormhole.ums.{UmsCommonUtils, UmsField, UmsProtocolUtils}
import edp.wormhole.ums.UmsProtocolType.UmsProtocolType
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala.OutputTag
import org.apache.flink.types.Row
import org.apache.flink.util.Collector
import org.apache.log4j.Logger
import org.joda.time.DateTime

import scala.collection.mutable.ArrayBuffer

class UmsProcessElement(sourceSchemaMap: Map[String, (TypeInformation[_], Int)], exceptionConfig: ExceptionConfig, jsonSourceParseMap: Map[(UmsProtocolType, String), (Seq[UmsField], Seq[FieldInfo], ArrayBuffer[(String, String)])], kafkaDataTag: OutputTag[String]) extends ProcessFunction[(String, String, String, Int, Long), Row]{
  //private val outputTag = OutputTag[String]("kafkaDataException")
  private lazy val logger = Logger.getLogger(this.getClass)
  override def processElement(value: (String, String, String, Int, Long), ctx: ProcessFunction[(String, String, String, Int, Long), Row]#Context, out: Collector[Row]): Unit = {
    logger.info("in UmsFlatMapper source data from kafka " + value._2)
    try {
      val (protocolType, namespace) = UmsCommonUtils.getTypeNamespaceFromKafkaKey(value._1)
      if (jsonSourceParseMap.contains((protocolType, namespace))) {
        val mapValue: (Seq[UmsField], Seq[FieldInfo], ArrayBuffer[(String, String)]) = jsonSourceParseMap((protocolType, namespace))
        val umsTuple = JsonParseUtils.dataParse(value._2, mapValue._2, mapValue._3)
        umsTuple.foreach(tuple => {
          createRow(tuple.tuple, protocolType.toString, out)
        })
      }
      else {
        val ums = UmsCommonUtils.json2Ums(value._2)
        logger.info("in UmsFlatMapper " + sourceSchemaMap.size)
        if (FlinkSchemaUtils.matchNamespace(ums.schema.namespace, exceptionConfig.sourceNamespace) && ums.payload.nonEmpty && ums.schema.fields.nonEmpty)
          ums.payload_get.foreach(tuple => {
            createRow(tuple.tuple, protocolType.toString, out)
          })
      }
    } catch {
      case ex: Throwable =>
<<<<<<< HEAD
        logger.error("in doFlinkSql table query", ex)
        //out.collect(new Row(0))
        ctx.output(kafkaDataTag, UmsProtocolUtils.feedbackFlowFlinkxError(exceptionConfig.sourceNamespace, exceptionConfig.streamId, exceptionConfig.flowId, exceptionConfig.sinkNamespace, new DateTime(), value._2, ex.getMessage))
=======
        ex.printStackTrace()
        out.collect(new Row(0))
<<<<<<< HEAD
        ctx.output(kafkaDataTag, UmsProtocolUtils.feedbackFlowFlinkxError(namespaceIdConfig.sourceNamespace, namespaceIdConfig.streamId, namespaceIdConfig.flowId, namespaceIdConfig.sinkNamespace, new DateTime(), value._2, ex.getMessage))
>>>>>>> print exception to log
=======
        ctx.output(kafkaDataTag, UmsProtocolUtils.feedbackFlowFlinkxError(exceptionConfig.sourceNamespace, exceptionConfig.streamId, exceptionConfig.flowId, exceptionConfig.sinkNamespace, new DateTime(), value._2, ex.getMessage))
>>>>>>> add exception process method
    }
  }

  def createRow(tuple: Seq[String], protocolType:String, out: Collector[Row]): Unit = {
    val row = new Row(tuple.size)
    for (i <- tuple.indices)
      row.setField(i, FlinkSchemaUtils.getRelValue(i, tuple(i), sourceSchemaMap))
    out.collect(row)
  }

}

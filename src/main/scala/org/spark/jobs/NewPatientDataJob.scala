

package org.spark.jobs

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.DStream
import org.spark.stream._

import scala.concurrent.duration.FiniteDuration

class NewPatientDataJob(config: JobConfig, source: KafkaDStreamSource) extends SparkStreamingApplication {

  override def sparkConfig: Map[String, String] = config.spark

  override def streamingBatchDuration: FiniteDuration = config.streamingBatchDuration

  override def streamingCheckpointDir: String = config.streamingCheckpointDir


  def start(): Unit = {

    def main(args: Array[String]): Unit = {
      withSparkStreamingContext { (sc, ssc) =>

        val kafkaStream: DStream[KafkaPayload] = source.createSource(ssc, "dbserver1.openmrs.obs")
        println("Job----------->")
        val stringCodec = sc.broadcast(KafkaPayloadStringCodec())
        val json = kafkaStream.map(record => (record.key, record.value))
        println(" json is  0------ 0" + json)



      }
    }

  }
}

  object NewPatientDataJob {

    def main(args: Array[String]): Unit = {
      val config = JobConfig()

      val streamingJob = new NewPatientDataJob(config, KafkaDStreamSource(config.sourceKafka))
      streamingJob.start()
    }

  }

  case class JobConfig(
                        inputTopic: String,
                        outputTopic: String,
                        windowDuration: FiniteDuration,
                        slideDuration: FiniteDuration,
                        spark: Map[String, String],
                        streamingBatchDuration: FiniteDuration,
                        streamingCheckpointDir: String,
                        sourceKafka: Map[String, String],
                        sinkKafka: Map[String, String]
                      ) extends Serializable

  object JobConfig {

    import com.typesafe.config.{Config, ConfigFactory}
    import net.ceedubs.ficus.Ficus._

    def apply(): JobConfig = apply(ConfigFactory.load)

    def apply(applicationConfig: Config): JobConfig = {

      val config = applicationConfig.getConfig("config")

      new JobConfig(
        config.as[String]("input.topic"),
        config.as[String]("output.topic"),
        config.as[FiniteDuration]("windowDuration"),
        config.as[FiniteDuration]("slideDuration"),
        config.as[Map[String, String]]("spark"),
        config.as[FiniteDuration]("streamingBatchDuration"),
        config.as[String]("streamingCheckpointDir"),
        config.as[Map[String, String]]("kafkaSource"),
        config.as[Map[String, String]]("kafkaSink")
      )
    }



}

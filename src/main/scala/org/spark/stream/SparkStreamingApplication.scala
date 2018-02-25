

package org.spark.stream

import scala.concurrent.duration.FiniteDuration

import org.apache.spark.SparkContext
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.StreamingContext

trait SparkStreamingApplication extends SparkApplication {

  def streamingBatchDuration: FiniteDuration

  def streamingCheckpointDir: String

  def withSparkStreamingContext(f: (SparkContext, StreamingContext) => Unit): Unit = {
    withSparkContext { sc =>
      val ssc = new StreamingContext(sc, Seconds(streamingBatchDuration.toSeconds))
      ssc.checkpoint(streamingCheckpointDir)

      f(sc, ssc)

      ssc.start()
      ssc.awaitTermination()
    }
  }

}

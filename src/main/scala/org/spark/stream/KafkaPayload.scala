

package org.spark.stream

case class KafkaPayload(key: Option[Array[Byte]], value: Array[Byte])

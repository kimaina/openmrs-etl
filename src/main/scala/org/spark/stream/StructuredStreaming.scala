package org.spark.stream

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StructField, _}

/**
  * Spark Streaming Kafka Cassandra
  */
object NetworkQualityStreamingJob extends App {

  val config = ConfigUtil.getConfig("spark")

  // spark config
  val spark: SparkSession = SparkSession.builder
    .appName(getClass.getSimpleName)
    .config("spark.jars.packages","org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.3")
    .config("spark.executor.memory", "10G")
    .config("spark.driver.memory", "10G")
    .config("spark.driver.maxResultSize", "10G")
    .getOrCreate()

  import spark.implicits._

  // define schema for json

val patient= StructType(ArrayType(
  StructField("patient_id", LongType, True),
  StructField("date_created", LongType, True),
  StructField("creator", LongType, True)
))

val schema= StructType(ArrayType(
                StructField("schema", StringType,
                StructField("payload", 
                           StructType(ArrayType(
                                StructField("before", StringType,
                                StructField("after", patient)
                           ))
                           )
))

  // create stream
  val df = spark
    .readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "dbserver1.openmrs.patient")
    .load()
    .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
    .select(from_json($"value", schema).alias("parsed_value"))
  //.withColumn("after", explode($"parsed_value.payload"))

  df.createOrReplaceTempView("patients")

  val sql =
    """
    SELECT
      WINDOW(FROM_UNIXTIME(parsed_value.payload.after.date_created/1000), "1 hour", "10 minutes") AS eventWindow,
      parsed_value.payload.after.creator AS creator,
      AVG(parsed_value.payload.after.patient_id) AS avgAge,
      MIN(parsed_value.payload.after.patient_id) AS minAge,
      MAX(parsed_value.payload.after.patient_id) AS maxAge
    FROM
      patients
    GROUP BY
      eventWindow,
      creator
    ORDER BY
      eventWindow,
      creator
  """
  val query = spark.sql(sql)

  // show results
  val result = query.writeStream
    .format("console")
    .outputMode("complete")
    .option("truncate", "false")
    .start()

  result.awaitTermination()

}
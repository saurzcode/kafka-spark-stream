package in.saurzcode.stream.reader

import in.saurzcode.stream.config.TwitterKafkaConfig
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{current_timestamp, from_csv, max}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

object KafkaStreamReader {

  def main(args: Array[String]): Unit = {
    val spark =
      SparkSession
        .builder()
        .appName("kafka-spark-stream")
        .master("local")
        .getOrCreate()

    val twitterUserSchema = StructType(
      Array(StructField("id", LongType),
            StructField("followersCount", IntegerType),
            StructField("name", StringType),
            StructField("screenName", StringType)))

    import spark.implicits._

    val streamDF =
      spark.readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", TwitterKafkaConfig.SERVERS)
        .option("subscribe", TwitterKafkaConfig.TOPIC)
        .load()

    val finalDF = streamDF
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
      .select(from_csv($"value", twitterUserSchema, Map("delimiter" -> ","))
        .as("twitter_user"))
      .select($"twitter_user.id",
              $"twitter_user.name",
              $"twitter_user.followersCount",
              $"twitter_user.screenName")
      .withColumn("event_time", current_timestamp())
      .withWatermark("event_time", "1 minutes")
      .groupBy("event_time")
      .agg(max($"followersCount"))

    val query = finalDF.writeStream
      .outputMode("complete")
      .format("console")
      .option("truncate", value = false)
      .trigger(Trigger.ProcessingTime("60 seconds"))
      .start()

    finalDF.printSchema()
    query.awaitTermination()

  }

}

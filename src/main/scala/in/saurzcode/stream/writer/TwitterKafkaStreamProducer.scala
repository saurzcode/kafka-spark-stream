package in.saurzcode.stream.writer

import java.util.Properties
import java.util.concurrent.LinkedBlockingQueue

import com.google.gson.Gson
import com.twitter.hbc.ClientBuilder
import com.twitter.hbc.core.Constants
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint
import com.twitter.hbc.core.processor.StringDelimitedProcessor
import com.twitter.hbc.httpclient.auth.OAuth1
import in.saurzcode.stream.config.TwitterKafkaConfig
import in.saurzcode.stream.model.Tweet
import org.apache.kafka.clients.producer.{
  KafkaProducer,
  ProducerConfig,
  ProducerRecord
}
import org.apache.kafka.common.serialization.{LongSerializer, StringSerializer}

import scala.collection.JavaConverters._

object TwitterKafkaStreamProducer {

  def main(args: Array[String]): Unit = {

    if (args.length != 5)
      throw new IllegalArgumentException(
        "Please Pass 5 arguments, in order - consumerKey, consumerSecret, token, secret, and term")

    //These should be passed in VM arguments for the application.
    // term on twitter on which you want to filter the results on.
    //These should be passed in VM arguments for the application.
    val consumerKey = args(0)
    val consumerSecret = args(1)
    val token = args(2)
    val secret = args(3)
    val term = args(4) // term on twitter on which you want to filter the results on.

    run(consumerKey, consumerSecret, token, secret, term)
  }

  private def run(consumerKey: String,
                  consumerSecret: String,
                  token: String,
                  secret: String,
                  term: String): Unit = {
    val queue = new LinkedBlockingQueue[String](10000)
    val endpoint = new StatusesFilterEndpoint
    endpoint.trackTerms(List(term).asJava)
    val auth = new OAuth1(consumerKey, consumerSecret, token, secret)
    val client = new ClientBuilder()
      .hosts(Constants.STREAM_HOST)
      .endpoint(endpoint)
      .authentication(auth)
      .processor(new StringDelimitedProcessor(queue))
      .build
    client.connect()
    val gson = new Gson()
    val producer = getProducer
    try {
      while (true) {
        val tweet = gson.fromJson(queue.take, classOf[Tweet])
        val key = tweet.getId
        val msg = tweet.toString
        println("Got Tweet " + key + "from User" + tweet.getUser.getId)
        val message =
          new ProducerRecord[Long, String](TwitterKafkaConfig.TOPIC, key, msg)
        producer.send(message)
      }
    } catch {
      case e: InterruptedException =>
        e.printStackTrace()
    } finally {
      client.stop()
      if (producer != null) producer.close()
    }
  }

  private def getProducer: KafkaProducer[Long, String] = {
    val properties = new Properties
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                   TwitterKafkaConfig.SERVERS)
    properties.put(ProducerConfig.ACKS_CONFIG, "1")
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                   classOf[LongSerializer].getName)
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                   classOf[StringSerializer].getName)
    new KafkaProducer[Long, String](properties)
  }

}

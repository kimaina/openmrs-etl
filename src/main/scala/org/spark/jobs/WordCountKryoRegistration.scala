

package org.spark.jobs

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator
import org.spark.stream.KafkaPayloadStringCodec

class WordCountKryoRegistration extends KryoRegistrator {

  override def registerClasses(kryo: Kryo): Unit = {
    kryo.register(classOf[KafkaPayloadStringCodec])

    //
    // register avro specific records using twitter chill
    //
    // kryo.register(classOf[MyAvroType], AvroSerializer.SpecificRecordBinarySerializer[MyAvroType])

  }

}

package flink.cep.test;

import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.util.serialization.DeserializationSchema;
import org.apache.flink.streaming.util.serialization.SerializationSchema;

public class ATMFraudSchema implements DeserializationSchema<ATMFraudEvent>, SerializationSchema<ATMFraudEvent> {

	@Override
	public byte[] serialize(ATMFraudEvent element) {
		return element.toString().getBytes();
	}

	@Override
	public ATMFraudEvent deserialize(byte[] message) {
		return ATMFraudEvent.fromString(new String(message));
	}

	@Override
	public boolean isEndOfStream(ATMFraudEvent nextElement) {
		return false;
	}

	@Override
	public TypeInformation<ATMFraudEvent> getProducedType() {
		return TypeExtractor.getForClass(ATMFraudEvent.class);
	}
}
package flink.cep.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.PatternSelectFunction;
import org.apache.flink.cep.PatternStream;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.IngestionTimeExtractor;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer08;

public class ATMCEPKafka {

	static final double HIGH_VALUE_TXN = 10000;
	static final long TXN_TIMESPAN_SEC = 20;


	public static void main(String[] args) throws Exception {

		List<String> allTxn = new ArrayList<String>();
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
		env.getConfig().setAutoWatermarkInterval(1000);


		// configure Kafka consumer
		Properties props = new Properties();
		props.setProperty("zookeeper.connect", "localhost:2181"); // Zookeeper default host:port
		props.setProperty("bootstrap.servers", "localhost:9092"); // Broker default host:port
		props.setProperty("group.id", "test-consumer-group");


		// create a Kafka consumer
		FlinkKafkaConsumer08<ATMFraudEvent> consumer = new FlinkKafkaConsumer08<ATMFraudEvent>(
				"ATMTXNS",                  // Topic
				new ATMFraudSchema(),       // Text to POJO Deserialiser for Kafka streams  
				props
				);


		DataStream<ATMFraudEvent> ATMTXNStream = env.addSource(consumer).assignTimestampsAndWatermarks(new IngestionTimeExtractor<>());


		// FraudAlert pattern: Two consecutive events > 10K ATM withdrawal 
		// appearing within a time interval of 20 seconds

		Pattern<ATMFraudEvent, ?> alertPattern = Pattern.<ATMFraudEvent>begin("first")
				.subtype(ATMFraudEvent.class)
				.where(
						new FilterFunction<ATMFraudEvent>() {
						    @Override
						    public boolean filter(ATMFraudEvent value) {
						        return true;
						    }
						})
				.where(evt -> evt.getTxnAmount() >= HIGH_VALUE_TXN && evt.getTxnType().equals("W/Draw"))
				.followedBy("second")
				.subtype(ATMFraudEvent.class)
				.where(evt -> evt.getTxnAmount() >= HIGH_VALUE_TXN && evt.getTxnType().equals("W/Draw"))
				.within(Time.seconds(TXN_TIMESPAN_SEC));
		

		// Events should be grouped by customerId, subsequent transactions for different customers isn't a issue here

		PatternStream<ATMFraudEvent> tempPatternStream = CEP.pattern(ATMTXNStream.rebalance().keyBy("customerId"), alertPattern);


		/*
		 * Java 8 lambdas didn't work due to JDT compiler issues,for Patternselectfunction
		 * refer https://ci.apache.org/projects/flink/flink-docs-master/dev/java8.html 
		 * 
		 */


		DataStream<ATMFraudAlert> alert = tempPatternStream.select(new PatternSelectFunction<ATMFraudEvent, ATMFraudAlert>(){
			private static final long serialVersionUID = 1L;
			@Override
			public ATMFraudAlert select(Map<String, ATMFraudEvent> pattern) {
				ATMFraudEvent first = (ATMFraudEvent) pattern.get("first");
				ATMFraudEvent second = (ATMFraudEvent) pattern.get("second");
				allTxn.clear();
				allTxn.add(first.getCustomerId() + " made a " + first.getTxnType() + " of " + first.getTxnAmount() + "/- at " + first.getTxnTimeStamp());
				allTxn.add(second.getCustomerId() + " made a " + second.getTxnType() + " of " + second.getTxnAmount() + "/- at " + second.getTxnTimeStamp());
				return new ATMFraudAlert(first.getAtmId(), allTxn);
			}
		});



		alert.print();
		env.execute("CEP monitoring job");
	}

}
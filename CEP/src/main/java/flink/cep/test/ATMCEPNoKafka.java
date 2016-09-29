package flink.cep.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

public class ATMCEPNoKafka {

	static final double HIGH_VALUE_TXN = 10000;
	static final long TXN_TIMESPAN_SEC = 20;

	private static final int atmId= 99999;
	private static final long PAUSE = 10000;
	private static long customerId = 222; 
	private static double txnAmount = 25000;
	private static String txnType = "W/Draw";


	public static void main(String[] args) throws Exception {

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
				"ATMTXNS", 
				new ATMFraudSchema(),
				props
				);


		DataStream<ATMFraudEvent> ATMTXNStream = env.addSource(consumer).assignTimestampsAndWatermarks(new IngestionTimeExtractor<>());


		//		ATMTXNStream
		//		.keyBy("customerId")
		//		.map ( s -> {
		//			ATMFraudEvent obj = (ATMFraudEvent) s;
		//			return obj.customerId + " - " + obj.txnAmount;
		//		})
		//		.print();




		//		// Input stream of monitoring events
		//		DataStream<ATMMonitoringEvent> inputEventStream = env
		//				.addSource(new MonitoringEventSource(
		//						atmId,
		//						PAUSE,
		//						customerId,
		//						txnAmount,
		//						txnType
		//						))
		//						.assignTimestampsAndWatermarks(new IngestionTimeExtractor<>());


		// Warning pattern: Two consecutive >10K txn events 
		// appearing within a time interval of 10 seconds

		Pattern<ATMFraudEvent, ?> warningPattern = Pattern.<ATMFraudEvent>begin("first")
				.subtype(ATMFraudEvent.class)
				.where(evt -> evt.getTxnAmount() >= HIGH_VALUE_TXN && evt.getTxnType().equals("W/Draw"))
				//.where(evt -> evt.getTxnAmount() >= HIGH_VALUE_TXN)
				.next("second")
				.subtype(ATMFraudEvent.class)
				.where(evt -> evt.getTxnAmount() >= HIGH_VALUE_TXN && evt.getTxnType().equals("W/Draw"))
				//.where(evt -> evt.getTxnAmount() >= HIGH_VALUE_TXN)
				.within(Time.seconds(TXN_TIMESPAN_SEC));



		// Create a pattern stream from our warning pattern
		PatternStream<ATMFraudEvent> tempPatternStream = CEP.pattern(ATMTXNStream.keyBy("customerId"), warningPattern);




		//		DataStream<ATMFraudWarning> warnings = tempPatternStream.select(
		//				(Map<String, ATMFraudEvent> pattern) -> {
		//					ATMFraudEvent first = (ATMFraudEvent) pattern.get("first");
		//					return new ATMFraudWarning(first.getAtmId(), first.getCustomerId() + " made a " + first.getTxnType() + " of " + first.getTxnAmount() + "/- at " + first.getTxnTimeStamp());
		//				});


		DataStream<ATMFraudAlert> warnings = tempPatternStream.select(new PatternSelectFunction<ATMFraudEvent, ATMFraudAlert>(){
			private static final long serialVersionUID = 1L;

			@Override
			public ATMFraudAlert select(Map<String, ATMFraudEvent> pattern) {
				ATMFraudEvent first = (ATMFraudEvent) pattern.get("first");
				List<String> allTxn = new ArrayList<String>();
				allTxn.add(first.getCustomerId() + " made a " + first.getTxnType() + " of " + first.getTxnAmount() + "/- at " + first.getTxnTimeStamp());
				return new ATMFraudAlert(first.getAtmId(), allTxn);

			}
		});



		warnings.print();
		System.out.print(warnings);

		env.execute("CEP monitoring job");
	}

}
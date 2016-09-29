package flink.cep.test;

import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

public class MonitoringEventSource extends RichParallelSourceFunction<ATMFraudEvent> {

	private boolean running = true;


	private long atmId;
	private long customerId;
	private double txnAmount;
	private String txnType;

	private final long pause;
	private Random random;

	public MonitoringEventSource(
			long atmId,
			long pause,
			long customerId,
			double txnAmount,
			String txnType
			) {
		this.atmId = atmId;
		this.pause = pause;
		this.customerId = customerId;
		this.txnAmount = txnAmount;
		this.txnType = txnType;
	}

	@Override
	public void open(Configuration configuration) {

		int numberTasks = getRuntimeContext().getNumberOfParallelSubtasks();
		int index = getRuntimeContext().getIndexOfThisSubtask();

		random = new Random();
	}

	public void run(SourceContext<ATMFraudEvent> sourceContext) throws Exception {
		ATMFraudEvent atmmonitoringEvent;
		Calendar calendar = Calendar.getInstance();
		long threadId = Thread.currentThread().getId();
		while (running) {
			customerId =  threadId;
			if (customerId % 2 ==0){
				txnAmount = 12000;
				System.out.println("customerid = "+ customerId +" amount = "+ txnAmount);
			}
			else{ 
				txnAmount = 5000;
				System.out.println("customerid = "+ customerId +" amount = "+ txnAmount);
				
			}

			atmmonitoringEvent = new ATMFraudEvent(atmId, customerId, txnAmount, txnType, new java.sql.Timestamp(calendar.getTime().getTime())+"");
			sourceContext.collect(atmmonitoringEvent);
			Thread.sleep(pause);
		}
	}

	public void cancel() {
		running = false;
	}
}
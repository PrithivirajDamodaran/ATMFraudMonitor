package flink.cep.test;


public class ATMFraudEvent {

	public long atmId;
	public long customerId;
	public  double txnAmount;
	public String txnType;
	public String txnTimeStamp;

	public ATMFraudEvent(){
		this(-1,-1,-1,"","");
		
	}

	public ATMFraudEvent(long atmId, long customerId, double txnAmount, String txnType, String txnTimeStamp) {
		this.atmId = atmId;
		this.customerId = customerId;
		this.txnAmount = txnAmount;
		this.txnType = txnType;
		this.txnTimeStamp = txnTimeStamp;
	}

	public double getTxnAmount() {
		return txnAmount;
	}

	public void setTxnAmount(double txnAmount) {
		this.txnAmount = txnAmount;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getTxnTimeStamp() {
		return txnTimeStamp;
	}

	public void setTxnTimeStamp(String txnTimeStamp) {
		this.txnTimeStamp = txnTimeStamp;
	}

	public static ATMFraudEvent fromString(String line) {

		String[] tokens = line.split(",");

		if (tokens.length != 5) {
			throw new RuntimeException("Invalid record: " + line);
		}

		ATMFraudEvent fraudevent = new ATMFraudEvent();

		fraudevent.atmId = Long.parseLong(tokens[0]);
		fraudevent.customerId = Long.parseLong(tokens[1]);
		fraudevent.txnAmount = Double.parseDouble(tokens[2]);
		fraudevent.txnType = tokens[3];
		fraudevent.txnTimeStamp = tokens[4];

		return fraudevent;
	}


	public long getAtmId() {
		return atmId;
	}


	public void setAtmId(long atmId) {
		this.atmId = atmId;
	}


	public long getCustomerId() {
		return customerId;
	}


	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
	
	
}

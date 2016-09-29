package flink.cep.test;

public  class ATMMonitoringEvent {

	public long atmId;
	public long customerId;

	public ATMMonitoringEvent() {
		this (-1,-1);
	}

	public ATMMonitoringEvent(long atmId, long customerId){
		this.atmId = atmId;
		this.customerId = customerId;

	}



	public long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}

	public long getAtmId() {
		return atmId;
	}

	public void setAtmId(long atmId) {
		this.atmId = atmId;
	}

}

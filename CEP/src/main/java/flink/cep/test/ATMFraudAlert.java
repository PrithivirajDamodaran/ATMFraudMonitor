package flink.cep.test;

import java.util.List;

public class ATMFraudAlert{

	private long atmId;
	private List<String> message;
	
	public ATMFraudAlert(long atmId, List<String> message) {
		this.atmId = atmId;
		this.message = message;
	}

	public long getAtmId() {
		return atmId;
	}

	public void setAtmId(long atmId) {
		this.atmId = atmId;
	}
	public List<String> getMessage() {
		return message;
	}



	public void setMessage(List<String> message) {
		this.message = message;
	}
	
	
	@Override
    public String toString() {
        return "FraudAlert(" +  atmId + ", " + message + ")";
    }




	
}

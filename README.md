# ATM Fraud Monitor - companion code for the blog post http://bytecontinnum.com/apache-flink-cep-library-part-1/
CEP for Banking systems powered by Apache Flink

Steps to test:

1. Install ZK locally and start the service. (if you are using already runnings service amend code accordingly)
2. Install Kafka locally and start the service.(if you are using already runnings service amend code accordingly)
3. Create topic "ATMTXNS"
4. Run ATMCEPKafka.java 
5. Use commandline or your own Kafka producer to ingest events in the below CSV format.
ATMID,CUSTOMERID,TXNAMT,TXNTYPE,TXNTIMESTAMP

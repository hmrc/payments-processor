
# payments-processor

### What is payments processor?

Payments processor is a microservice which allows for asynchronous execution of requests.  It does this via [work-item-repo] (https://github.com/hmrc/work-item-repo).
At the moment there is only one asynchronous flow available which sends a charge ref notification to ETMP via DES.  The DES endpoint is called, if there is a 5** failure 
the request is placed into the underlying work-item-repo implementation collection.  From here the request will be retried according to the values in the configuration:

queue.retryAfter = 120 seconds

queue.ttl = 24 hours

poller.interval = 30 seconds

If queue.enabled is set to false, failed requests will not be retried, failed requests will not be persisted in work-item-repo

If poller.enabled is set to false, failed requests will not be retried, failed requests will be persisted in work-item-repo


### Running Locally

You only need to run payments-processor: `sm --start PAYMENTS_PROCESSOR`

*make sure you have mongo running

---

### Test data
[Test data](https://confluence.tools.tax.service.gov.uk/display/OPS/Testing+work+item+repo)

---

### Further information
[See further information about this service on confluence](https://confluence.tools.tax.service.gov.uk/display/OPS/Payments+processor)

---
### Test suites
Has unit tests within the repo as well as:

https://github.com/hmrc/payments-processor-acceptance-tests

https://github.com/hmrc/payments-processor-perf-tests


---


### License     

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").

# Persevere

A library for performing actions with retries.

## How to use

* Specify the action you want to perform, by implementing [RetryableAction](src/main/java/com/github/cb372/persevere/action/RetryableAction.java):

    ````java
    RetryableAction<String> myAction = new RetryableAction<String> {
        public String execute(int retryCount) throws Exception {
            // might throw an IOException if it can't connect to server
            return httpClient.getResponseBody("http://api.my-service.com");
        }
    };
    ````

    Persevere will run the action, retrying if it throws an exception. 

    Note that the `retryCount` is passed as an argument, so you can change the behaviour of the action depending on how many times it has been retried.
    For example you might want to try connecting to a different server if the main server appears to be down.

* Specify how you want to pause between retries:

    ````java
    DelayStrategy delayStrategy = DelayStrategies.fixed(100);
    ````

* Decide how many times you want to retry before giving up:

    ````java
    int maxRetries = 3;
    ````

Pass all of this to Persevere and get back a `Future`:

````java
Future<ExecutionResult<String>> future = Persevere.persevere(myAction, delayStrategy, maxRetries);
````

Note that the trying and re-trying of the action is performed asynchronously (hence the `Future`) because it's likely to take a while, and you probably should be getting on with other work in the meantime, rather than blocking on the result.

### Failing fast

Some actions will fail in exactly the same way no matter how many times you retry it. In these cases, you may want to fail fast and not bother with any more retries.

You can do this by throwing a [GiveUp](src/main/java/com/github/cb372/persevere/action/GiveUp.java):

````java
RetryableAction<Integer> myAction = new RetryableAction<Integer> {
    public Integer execute(int retryCount) throws Exception {
        try {
            return (someLongRunningCalculation() / n);
        } catch (ArithmeticException e) {
            // oops! divided by zero!
            throw GiveUp.giveUpBecauseOf(e);
        }
    }
};
````

### Delay strategies

You can choose how you want to pause in between retries. The following strategies are provided:

* Retry immediately (no delay)
* Fixed delay
* Random delay (in between some minimum and maximum)
* Exponentially increasing delay

You can also easily write your own strategies.

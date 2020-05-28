## CSS Engineering Challenge Homework.

This CSS project is an engineering challenge homework.

### I) Development Environment.
The CSS project is implemented based on Java 11 / SpringBoot 2.2.7.RELEASE / Maven 3.6.3.
To run it up, please make sure your environment is fulfill/compatible with above conditions.

### II) How the system is designed.
There are two modules in the project: Order and Kitchen.
Each module has three packages 'model object', 'service interface' and 'service implementation'.

##### 1) Order module
 Order module provides two functions.
 
 1) Parses the order json file and convert the file content into  order objects.
 
 order.file.name property is used to define where's the json file.
 
 2)  An interface is defined for other system that need receive orders.
 
This order register/notify sub-system depends org.reactivestream and reactor. Because of using Flux, the system acquire the ability of reactive. Any system wants to receive the orders need to implements the 
interface IOrderObserver<Order>.

order.ingestion.rate property is used to control the rate of sending  orders to other system. 
 
##### 2) Kitchen module
This module is the core of the system. It has the functions to :

1) Receive orders.

KitchenOrderObserver implements IOrderObserver to receive orders from json file. 
    
2) Put order on right shelf based on a kind of algorithm.

StrategyOfPutOrderOnShelf implements the interface IShelfSelectStrategy to provide an algorithm which satisfy the requirement of this homework.

3) Clear expired orders (orderValue<0).

ExpiredOrderChecker implements IExpiredOrderCheckingService provides concrete method about how to clean the expired orders.

4) Use courier to delivery the cooked order.
In fact, the courier can be separated into another module. In order 
to simplify its implementation, it was added into the sub-package courier. But the interface ICourierService 
and ICookedOrderProvider are defined to make it can be refactored into separate module easily.

StrategyOfRandomPickCookedOrder implements ICookedOrderPickStrategy to provide a concrete implementation of how to pick up a cooked order for courier to delivery.

The class KitchenSystem glue all above services together. 

#### 3) About model objects
The model object Kitchen works as an entity. A shelf belongs to a Kitchen. The CookedOrder is used to model the received and processed Order in the Kitchen system. It includes the original Order and other properties related with kitchen process. The CookedOrder relates with a Shelf after cooked.


### III) How to Build & Test & Run the code.
unzip the css_dahui.zip in your preferred directory.

##### 1) Go to the project root dir.
cd css

##### 2) Build & Run all the test cases    
mvn clean verify

All the process needs about 3 minutes.  
Or you can run specific test case, such as:

mvn test -pl kitchen -Dtest=KitchenSystemIT

mvn test -pl kitchen -Dtest=OrderOnShelfTTLCalculatorTest

mvn test -pl order -Dtest=OrderFileScannerIT


##### 3) Run the kitchen system up.

Please change the order.json file path based on your environment.

###### Run by jar. ()

	java -jar ./kitchen/target/kitchen-0.0.1-SNAPSHOT-exec.jar \
	--order.file.name=/Users/dahui/tmp/orders.json \
	--order.ingestion.rate=2000 \
	--kitchen.order.checker.period=5000 \
	--courier.sleep.min=2000 --courier.sleep.max=6000 \
	--kitchen.shelf.capacity="{Hot: '10', Cold: '10', Frozen: '10', Any: '15'}"

###### Or you can run this application by Maven spring-boot plugin.

	mvn spring-boot:run -pl kitchen -Dspring-boot.run.arguments="\
	--order.file.name=/Users/dahui/tmp/orders.json \
	--order.ingestion.rate=2000 \
	--kitchen.order.checker.period=5000 \
	--courier.sleep.min=2000 --courier.sleep.max=6000" \
	--kitchen.shelf.capacity="{Hot: '10', Cold: '10', Frozen: '10', Any: '15'}"


##### 4) About Application Setting
All the configurable settings can be found in ./kitchen/src/main/resources/application.properties.
They're three kinds of setting for 'Order', 'Kitchen Process' and 'Courier Delivery'. 
You can override these values from command line as showed above.

    # ===== Order Setting ==============================
    
    # absolute path or filename in classpath.
    order.file.name=orders.json
    
    # millisecond
    order.ingestion.rate=2000
    
    # ===== Kitchen Setting ==============================
    
    # minute. The kitchen timeout if there is no orders coming.
    kitchen.order.poll.timeout=10
    
    # The number of orders per request the kitchen ask for.
    kitchen.order.amount.per.req=5
    
    # The shelf capacity based on temperature.
    kitchen.shelf.capacity={Hot: '10', Cold: '10', Frozen: '10', Any: '15'}
    kitchen.shelf.capacity.default=10
    
    # millisecond. The initial delay time of expired order checking service to work.
    kitchen.order.expire.checker.delay=2000
    # millisecond. The interval period of expired order checking service.
    kitchen.order.expire.checker.period=5000
    
    # ===== Courier Setting =====
    courier.worker.thread.pool.size=20
    # millisecond
    courier.sleep.min=2000
    # millisecond
    courier.sleep.max=6000
    

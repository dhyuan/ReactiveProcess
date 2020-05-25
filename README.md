## CSS Engineering Challenge Homework.

This CSS project is an engineering challenge homework.

### I) Development Environment.
The CSS project is implemented based on Java 14 / SpringBoot 2.2.7.RELEASE / Maven 3.6.3.
To run it up, please make sure your environment fulfill/compatible above conditions.

### II) How the system is designed.
There are two modules in the project: Order and Kitchen.
Each module' code is group as mo, service interface and service implementation.

##### 1) Order module
 Order module provides two functions.
 1) Parses the order json file and convert the file data as order objects.
 
 order.file.name property is used to specify where's the json file.
 
 2) Provides order object to outside system throw Flux.

order.ingestion.rate perperty is used to controll the ingestion rate to other system. And beause of using Flux, 
the system acquire the ability of reactive. Any system wants to receive the orders need to implements the 
interface IOrderObserver<Order>.
 
 
##### 2) Kitchen module
This module is the core of the system. It has the functions to 
1) Receive orders
    KitchenOrderObserver implements IOrderObserver. 
    
2) Put order on right shelf

3) Clear expired orders (orderValue<0).


4) Use courier to delivery the cooked order.
In fact, the courier can be separated into another module. In order 
to simplify its implementation was added in this module. But the interface ICourierService 
and ICookedOrderProvider are defined to make sure it can be refactored easier.

The class KitchenSystem glue all above services together. On the other hand, the model object Kitchen works as an entity.
The shelf belongs to Kitchen and CookedOrder can related a Shelf.
The CookedOrder is used to model the received and processed order in the Kitchen module. 


### III) How to Build & Test & Run the code.
unzip the css_dahui.zip to your preferred directory.

##### 1) Go to the project root dir.
cd css

##### 2) Build & Run all the test cases    
mvn clean verify

All the process needs about 3 minutes.  
Or you can run specific test case, such as:
mvn test -pl kitchen -Dtest=KitchenSystemIT
mvn test -pl kitchen -Dtest=OrderOnShelfTTLCalculatorTest
mvn test -pl order -Dtest=OrderFileScannerIT
...

##### 3) Run the kitchen system up.
##### Run by jar
java -jar ./kitchen/target/kitchen-0.0.1-SNAPSHOT-exec.jar \
--order.file.name=/Users/dahui/tmp/orders.json \
--order.ingestion.rate=2000

##### Or you can run this application by Maven spring-boot plugin.
mvn spring-boot:run -pl kitchen -Dspring-boot.run.arguments="\
--order.file.name=/Users/dahui/tmp/orders.json \
--order.ingestion.rate=2000 \
--kitchen.order.checker.period=5000 \
--courier.sleep.min=2000 --courier.sleep.max=6000"


##### 4) About Application Setting
All the configurable settings can be found in ./kitchen/src/main/resources/application.properties.
There three kinds of setting for 'Order', 'Kitchen Process' and 'Courier Delivery'. 
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
    courier.sleep.min=2000
    courier.sleep.max=6000
    

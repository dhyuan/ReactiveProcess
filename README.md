# css

This project is an engineering challenge homework.

### 1) Go to the project root dir.
cd THE_CSS_ROOT_DIR

### 2) Build and package
mvn clean package

### 3) Run the kitchen system up.
#### Run by jar
java -jar ./kitchen/target/kitchen-0.0.1-SNAPSHOT-exec.jar \
--order.file.name=/Users/dahui/tmp/orders.json \
--order.ingestion.rate=2000

#### Or you can run this application by Maven spring-boot plugin.
mvn spring-boot:run -pl kitchen -Dspring-boot.run.arguments="\
--order.file.name=/Users/dahui/tmp/orders.json \
--order.ingestion.rate=2000 \
--courier.sleep.min=2000 --courier.sleep.max=6000"

### 4) Setting
More config can be found/modified in ./kitchen/src/main/resources/application.properties


There are XXX modules in this project. 
1. Order module (com.ech.order).


2. Kitchen

3.  
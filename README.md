# How to compile and run

After filling in the `application.properties` file, you are ready to compile and run the app.

```bash
mvn clean package # or mvn clean package -DskipTests
java -jar SimpleCli-0.0.1.jar example --spring.config.location=target/classes/application.properties
```
> Note: you need to use `--spring.config.location` if the `application.properties` is not in the app's classpath.

# Spock Extension for Quarkus

### How to import

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.NetoDevel</groupId>
    <artifactId>quarkus-spock</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

```

### How to use

```groovy
@QuarkusSpec
class MySpec extends Specification {

   @Inject
   MySimpleBeanService service

   @Inject
   SimpleBean1 simple1

   def "Make Sure My Stuff Mocks"(){
       setup:
       simple1.get() >> "OK"
       expect:
       service.callSimple1sMethod() == "OK"
   }

   @Mocks 
   SimpleBean1Impl mock(){
       return Mock(SimpleBean1Impl)
   }

   @Mocks 
   SimpleBean2Impl mock(){
       return Stub(SimpleBean2Impl)
   }
```


### How to configure

Add this to your plugins in your Maven build

```xml
<plugin>
    <groupId>org.codehaus.gmavenplus</groupId>
    <artifactId>gmavenplus-plugin</artifactId>
    <version>1.6.2</version>
    <executions>
        <execution>
            <goals>
                <goal>addSources</goal>
                <goal>addTestSources</goal>
                <goal>generateStubs</goal>
                <goal>compile</goal>
                <goal>generateTestStubs</goal>
                <goal>compileTests</goal>
                <goal>removeStubs</goal>
                <goal>removeTestStubs</goal>
            </goals>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>org.codehaus.groovy</groupId>
            <artifactId>groovy-all</artifactId>
            <version>2.5.6</version>
            <scope>runtime</scope>
            <type>pom</type>
        </dependency>
    </dependencies>
</plugin>
```

You will also need to add Byte Buddy

```xml
<dependency>
    <groupId>net.bytebuddy</groupId>
    <artifactId>byte-buddy</artifactId>
    <version>1.9.12</version>
</dependency>
```
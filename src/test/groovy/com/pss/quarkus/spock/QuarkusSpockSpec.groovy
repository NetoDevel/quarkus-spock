package com.pss.quarkus.spock

import com.pss.quarkus.spock.annotations.Mocks
import com.pss.quarkus.spock.annotations.QuarkusSpec
import com.pss.quarkus.spock.exclude.AnotherBean
import com.pss.quarkus.spock.exclude.AnotherBeanImpl
import com.pss.quarkus.spock.exclude.Qualifying
import com.pss.quarkus.spock.exclude.SimpleBean
import spock.lang.Specification
import spock.lang.Stepwise

import javax.inject.Inject

@Stepwise
@QuarkusSpec
class QuarkusSpockSpec extends Specification {

    @Inject
    SimpleBean bean


    @Inject
    @Qualifying(Qualifying.Qualify.ANOTHER_QUALIFY)
    AnotherBean anotherBeanWithQualifier



    def "Test Injection"(){
        setup:
        bean.get() >> "MOCK"
        when:
        String result = bean.get()
        then:
        result == "MOCK"
    }


    def "Make Sure Mock Resets"(){
        when:
        String result = bean.get()
        then:
        result == null
    }

    def "Test Another Reset Injection"(){
        setup:
        bean.get() >> "SPOCK MOCK"
        when:
        String result = bean.get()
        then:
        result == "SPOCK MOCK"
    }

    def "Check qualifiers"(){
        expect:
        anotherBeanWithQualifier != null
    }


    @Mocks
    SimpleBean mock(){
        return Mock(SimpleBean)
    }

    @Mocks
    @Qualifying(Qualifying.Qualify.ANOTHER_QUALIFY)
    AnotherBeanImpl mockAnother(){
        return Mock(AnotherBeanImpl)
    }

}
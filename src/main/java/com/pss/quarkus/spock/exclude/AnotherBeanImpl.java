package com.pss.quarkus.spock.exclude;


import javax.enterprise.context.ApplicationScoped;

@Qualifying(Qualifying.Qualify.ANOTHER_QUALIFY)
@ApplicationScoped
public class AnotherBeanImpl implements AnotherBean {


    @Override
    public void doNothing() {

    }


}

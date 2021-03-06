/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pss.quarkus.spock;

import static com.pss.quarkus.spock.QuarkusSpockExtension.AbstractSpecificationInitializer.forJvm;
import static com.pss.quarkus.spock.QuarkusSpockExtension.AbstractSpecificationInitializer.forNative;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import io.quarkus.arc.Arc;

import org.jboss.logging.Logger;
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension;
import org.spockframework.runtime.model.SpecInfo;

import com.pss.quarkus.spock.annotations.Mocks;
import com.pss.quarkus.spock.annotations.QuarkusSpec;
import com.pss.quarkus.spock.inject.InjectionOverride;
import com.pss.quarkus.spock.inject.MockBeanSupplier;
import com.pss.quarkus.spock.repack.ArcTestResourceProvider;
import com.pss.quarkus.spock.repack.bootstrap.ContextBootstrapper;
import com.pss.quarkus.spock.state.SpecificationState;
import com.pss.quarkus.spock.util.CommonUtils;

import io.quarkus.test.common.NativeImageLauncher;
import io.quarkus.test.common.RestAssuredURLManager;
import io.quarkus.test.common.TestResourceManager;
import io.quarkus.test.common.http.TestHTTPResourceManager;
import spock.lang.Specification;

/**
 * Copy Pasta'd
 *
 *
 * todo: Add a switch to leave written classes, sometimes we want to see what happened from the processor output
 */
public class QuarkusSpockExtension extends AbstractAnnotationDrivenExtension<QuarkusSpec> {

    private static final Logger LOGGER = Logger.getLogger(QuarkusSpockExtension.class.getName());


    private static final String MESSAGE = "Assuming Test is running from %s and logging to %s, if this is incorrect " +
            "or if your compiled files are located in a different directory, please manually assign %s#logLocation()";

    @Override
    public void visitSpecAnnotation(QuarkusSpec annotation, SpecInfo spec) {

        final Class<?> specClass = spec.getReflection();
        Class introspected = specClass;
        while (introspected != null && !Specification.class.equals(introspected)) {
            for (Method m : introspected.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Mocks.class)) {
                    MockBeanSupplier supplier = MockBeanSupplier.from(m);
                    InjectionOverride.putBeanSupplier(supplier);
                }
            }

            introspected = introspected.getSuperclass();
        }
        final AbstractSpecificationInitializer initializer = annotation.substrate() ? forNative(specClass) : forJvm(specClass);

        // Log location
        String logLocation = Optional.of(annotation.logLocation())
                .filter(log -> !"".equals(log))
                .orElseGet(this::determineLogLocation);

        System.setProperty("quarkus.log.file.path", logLocation);

        spec.addSetupSpecInterceptor(invocation -> {
            SpecificationState.setSpecification(invocation.getInstance());
            initializer.initializeResources();
            initializer.start();
            invocation.proceed();
        });

        // Inject Dependencies
        spec.addInitializerInterceptor(invocation -> {

            SpecificationState.setSpecification(invocation.getInstance());
            TestHTTPResourceManager.inject(invocation.getInstance());
            ArcTestResourceProvider.inject(invocation.getInstance());
            RestAssuredURLManager.setURL();
            invocation.proceed();
        });

        spec.addSetupInterceptor(invocation -> {
            RestAssuredURLManager.setURL();
            CommonUtils.attachMocks(invocation);
            invocation.proceed();
        });

        // Clear URL
        spec.addCleanupInterceptor(invocation -> {
            RestAssuredURLManager.clearURL();
            CommonUtils.detachMocks(invocation);
            invocation.proceed();
        });

        // Shutdown Context and then Close Resources
        spec.addCleanupSpecInterceptor(invocation -> {
            initializer.shutdown();
            initializer.shutdownResources();
            invocation.proceed();
        });
    }

    private String determineLogLocation() {
        Path path = Paths.get(System.getProperty("user.dir"));
        boolean target = path.resolve("target").toFile().exists();
        boolean build = path.resolve("build").toFile().exists();
        if (target && build) {
            LOGGER.warn("Both a build/ directory and a target/ directory were detected, logging to target/");
            return "target/quarkus.log";
        } else if (build) {
            LOGGER.infof(MESSAGE, "Gradle", "build/quarkus.log", QuarkusSpec.class.getName());
            return "build/quarkus.log";
        } else {
            LOGGER.infof(MESSAGE, "Maven", "target/quarkus.log", QuarkusSpec.class.getName());
            return "target/quarkus.log";
        }
    }

    static abstract class AbstractSpecificationInitializer<T> {
        final TestResourceManager resourceManager;
        final T delegate;

        private AbstractSpecificationInitializer(TestResourceManager resourceManager, T delegate) {

            this.resourceManager = resourceManager;
            this.delegate = delegate;
        }

        public static AbstractSpecificationInitializer forJvm(Class clazz) {
            return new JvmSpecificationInitializer(new TestResourceManager(clazz), ContextBootstrapper.from(clazz));
        }

        public static AbstractSpecificationInitializer forNative(Class clazz) {
            return new NativeSpecificationInitializer(new TestResourceManager(clazz), new NativeImageLauncher(clazz));
        }

        final void initializeResources() {
            resourceManager.start();
        }

        final void shutdownResources() {
            resourceManager.stop();
        }

        abstract void start();

        abstract void shutdown();
    }

    private static class JvmSpecificationInitializer extends AbstractSpecificationInitializer<ContextBootstrapper> {

        private JvmSpecificationInitializer(TestResourceManager resourceManager, ContextBootstrapper delegate) {
            super(resourceManager, delegate);
        }

        @Override
        void start() {
            delegate.run();
            Arc.initialize();
        }

        @Override
        void shutdown() {
            delegate.shutdown();
        }
    }

    private static class NativeSpecificationInitializer extends AbstractSpecificationInitializer<NativeImageLauncher> {

        private NativeSpecificationInitializer(TestResourceManager resourceManager, NativeImageLauncher delegate) {
            super(resourceManager, delegate);
        }

        @Override
        void start() {
            try {
                delegate.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        void shutdown() {
            delegate.close();
        }
    }

}

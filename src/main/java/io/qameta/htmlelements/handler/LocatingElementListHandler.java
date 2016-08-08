package io.qameta.htmlelements.handler;

import io.qameta.htmlelements.proxy.Proxies;
import org.openqa.selenium.support.pagefactory.ElementLocator;
import io.qameta.htmlelements.context.WebElementContext;
import org.openqa.selenium.WebElement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import static io.qameta.htmlelements.util.ReflectionUtils.getAllMethods;

class LocatingElementListHandler extends ComplexHandler {

    private final WebElementContext context;

    LocatingElementListHandler(WebElementContext context) {
        this.context = context;
    }

    private WebElementContext getContext() {
        return context;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Class<?> proxyClass = List.class;

        if (getAllMethods(proxyClass).contains(method)) {
            return  invokeProxyMethod(getContext().getLocator(), method, args);
        }

        return super.invoke(proxy, method, args);
    }

    private Object invokeProxyMethod(ElementLocator locator, Method method, Object[] args) throws Throwable {
        List<WebElement> originalElements = locator.findElements();
        Class<?> returnedType = getContext().getWebElementClass();

        List<Object> wrappedElements = originalElements.stream()
                .map(element -> Proxies.simpleProxy(returnedType,
                        new LocatingElementHandler(from(element))))
                .collect(Collectors.toList());
        try {
            return method.invoke(wrappedElements, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static ElementLocator from(WebElement element) {
        return new ElementLocator() {
            @Override
            public WebElement findElement() {
                return element;
            }

            @Override
            public List<WebElement> findElements() {
                return null;
            }
        };
    }
}
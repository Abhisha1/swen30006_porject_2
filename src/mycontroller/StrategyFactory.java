package mycontroller;

import world.Car;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

public class StrategyFactory {
    public static IStrategy getStrategy(String className, Car car, ControllerContext context)  throws  ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException{

        Class<?> strategyClass = Class.forName("mycontroller." + className);
        Class[] classArgs = new Class[2];
        classArgs[0] = Car.class;
        classArgs[1] = ControllerContext.class;
        return (IStrategy) strategyClass.getDeclaredConstructor(classArgs).newInstance(car, context);
    }
}
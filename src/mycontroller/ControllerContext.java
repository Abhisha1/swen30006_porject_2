package mycontroller;

import controller.CarController;
import world.Car;

import java.util.HashMap;

public class ControllerContext {
    private HashMap<String, CarController> strategies = new HashMap<>();
    private CarController currentStrategy;
    private Car car;
    public ControllerContext(Car car) {
        this.car = car;
    }
    public void setCurrentStrategy(IStrategy strategy) {
        currentStrategy = (CarController)strategy;
    }

    public void update() {
        currentStrategy.update();
    }

    public void switchStrategy(String strategyName) {
       try {
           currentStrategy = (CarController) StrategyFactory.getStrategy(strategyName, car, this);
       }catch(Exception e) {
           System.err.println(e);
           System.exit(1);
       }
    }

    public void switchStrategy(String strategyName, Bundle strategyBundle) {
        try {
            IStrategy strategy = StrategyFactory.getStrategy(strategyName, car, this);
            strategy.parseBundle(strategyBundle);
            currentStrategy = (CarController) strategy;
        }catch(Exception e) {
            System.err.println(e);
            System.exit(1);
        }
    }
}

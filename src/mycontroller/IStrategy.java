package mycontroller;

public interface IStrategy {
    void switchStrategy(String strategyName);
    void parseBundle(Bundle strategyBundle);
}

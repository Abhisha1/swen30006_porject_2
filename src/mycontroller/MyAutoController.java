package mycontroller;

import controller.CarController;
import org.lwjgl.Sys;
import world.Car;




public class MyAutoController extends CarController {


		private IStrategy explorationStrategy;


		private ControllerContext context;
		public MyAutoController(Car car) {
			super(car);
			context = new ControllerContext(car);
			try {
				explorationStrategy = StrategyFactory.getStrategy("FuelFocus", car, context);
			}catch (Exception e) {
				System.out.println(e);
				System.exit(1);
			}

			context.setCurrentStrategy(explorationStrategy);


		}
		

		@Override
		public void update() {
			context.update();
		}
		
	}

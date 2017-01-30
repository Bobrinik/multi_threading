

public class Q1 {
	volatile public static Integer roterSpeeds[] = {0,0,0,0};
	volatile public static int roterCurrentSpeed[] = {0,0,0,0};

	public static void main(String[] args){
		System.out.println("X="+args[0]+" Y="+args[1]);
		Thread controller = createController(roterSpeeds,Integer.parseInt(args[0]));
		controller.start();
		for(int i = 0; i < 4; i++){
			createRoter(i,Integer.parseInt(args[1])).start();
		}
	}



	private static Thread createController(Integer rotorspeeds[], int sleepTime) {
		Thread controller = new Thread(new Runnable(){
			int dieIn = 10000;
			@Override
			public void run() {
				while(true){
					int roter = (int) (Math.random()*4);
					int speed = (int) (Math.random()*11);
					synchronized(roterSpeeds){
						roterSpeeds[roter] = speed;//we don't want roters to read at the same time as our controller writes
					}					
					try {
						Thread.sleep(sleepTime);
						dieIn = dieIn - sleepTime;
						if(dieIn <= 0){
							return;
						}
					} catch (Exception e) {
						System.err.println("Controller thread sleep: "+ e);
					}
				}				
			}

		});
		return controller;
	}

	private static Thread createRoter(int currentRotor, int speedCheckTime){

		int successFail[] = {0,0}; // 0-stores success 1-stores failures
		Thread roter = new Thread(new Runnable(){
			int currentSpeed;
			int dieIn = 10000;
			int max = 0;

			@Override
			public void run() {
				while(true){
					int possibleSpeed = getMaxPossibleSpeedForThisRoter();
					synchronized(roterSpeeds){
						if(possibleSpeed < roterSpeeds[currentRotor]){
							synchronized(roterCurrentSpeed){
								successFail[1]++;
								currentSpeed = possibleSpeed;
								roterCurrentSpeed[currentRotor] = currentSpeed;
							}
						}
						else{
							synchronized(roterCurrentSpeed){
								successFail[0] = successFail[0]+1;
								currentSpeed = roterSpeeds[currentRotor];
								roterCurrentSpeed[currentRotor] = currentSpeed;
							}
						}
					}

					if(max < currentSpeed){
						max = currentSpeed;
					}
					
					try {
						Thread.sleep(speedCheckTime);
						dieIn = dieIn - speedCheckTime;
						if(dieIn <= 0){
							System.out.println("Roter: "+currentRotor+" checks="+(successFail[0]+successFail[1])+", success rate="+successFail[0]+", max="+max);
							return;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			private int getMaxPossibleSpeedForThisRoter(){
				int totalSpeed = 0;
				synchronized(roterCurrentSpeed){
					for(int roter = 0; roter < 4; roter++){
						if(roter != currentRotor){
							totalSpeed = totalSpeed + roterCurrentSpeed[roter];
						}
					}
				}
				return 20 - totalSpeed;
			}
		});
		return roter;
	}

}

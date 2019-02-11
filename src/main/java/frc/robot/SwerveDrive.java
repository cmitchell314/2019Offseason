package frc.robot;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Encoder;

public class SwerveDrive {
	static AHRS ahrs;
	static double ahrsOffset;
	

	static SwerveModule frontRight = new SwerveModule(new Jaguar(RobotMap.DRIVE_FRONT_RIGHT_MOTOR), new Talon(RobotMap.STEER_FRONT_RIGHT_MOTOR),
		 new AbsoluteAnalogEncoder(RobotMap.ENCODER_FRONT_RIGHT), RobotMap.ENCODER_ZERO_VALUE_FRONT_RIGHT, 
		 new Encoder(RobotMap.DRIVE_ENCODER_FRONT_RIGHT_A, RobotMap.DRIVE_ENCODER_FRONT_RIGHT_B, false, Encoder.EncodingType.k2X), "FrontRight");
	static SwerveModule frontLeft = new SwerveModule(new Jaguar(RobotMap.DRIVE_FRONT_LEFT_MOTOR), new Talon(RobotMap.STEER_FRONT_LEFT_MOTOR),
		 new AbsoluteAnalogEncoder(RobotMap.ENCODER_FRONT_LEFT), RobotMap.ENCODER_ZERO_VALUE_FRONT_LEFT,
		 new Encoder(RobotMap.DRIVE_ENCODER_FRONT_LEFT_A, RobotMap.DRIVE_ENCODER_FRONT_LEFT_B, false, Encoder.EncodingType.k2X), "FrontLeft");
	static SwerveModule backLeft = new SwerveModule(new Jaguar(RobotMap.DRIVE_BACK_LEFT_MOTOR), new Talon(RobotMap.STEER_BACK_LEFT_MOTOR),
		 new AbsoluteAnalogEncoder(RobotMap.ENCODER_BACK_LEFT), RobotMap.ENCODER_ZERO_VALUE_BACK_LEFT, 
		 new Encoder(RobotMap.DRIVE_ENCODER_BACK_LEFT_A, RobotMap.DRIVE_ENCODER_BACK_LEFT_B, false, Encoder.EncodingType.k2X), "BackLeft");
	static SwerveModule backRight = new SwerveModule(new Jaguar(RobotMap.DRIVE_BACK_RIGHT_MOTOR), new Talon(RobotMap.STEER_BACK_RIGHT_MOTOR),
		 new AbsoluteAnalogEncoder(RobotMap.ENCODER_BACK_RIGHT), RobotMap.ENCODER_ZERO_VALUE_BACK_RIGHT,  
		 new Encoder(RobotMap.DRIVE_ENCODER_BACK_RIGHT_A, RobotMap.DRIVE_ENCODER_BACK_RIGHT_B, false, Encoder.EncodingType.k2X), "BackRight");

	static boolean driveStraight = false;
	static double translationAngle;
	static double lastJSX;

	static void swerveInit(){
		ahrs = new AHRS(SerialPort.Port.kUSB);
		ahrs.reset();
		ahrsOffset = ahrs.getAngle();
	}	

	static void runSwerve(Joystick left, Joystick right, JoystickButton rightButton1, JoystickButton rightButton7, JoystickButton frb, JoystickButton flb, JoystickButton blb, JoystickButton brb) {

		Joystick leftStick = left;
		Joystick rightStick = right;
		JoystickButton rightTrigger = rightButton1;
		JoystickButton tuningActivation = rightButton7;

		int driveMode = 0;

		double speed = Math.pow(leftStick.getMagnitude(), 2);
		double leftDirection = leftStick.getDirectionDegrees() * -1;
		double leftX = leftStick.getX();
		double leftY = leftStick.getY();

		double rightDirection = rightStick.getDirectionDegrees();
		double rightMagnitude = rightStick.getMagnitude();
		double twist = rightStick.getTwist();
		

		if (twist < 0) {
			twist = -Math.pow(twist, 2);
		} else {
			twist = Math.pow(twist, 2);
		}

		if (tuningActivation.get() == true) {
			driveMode = 1;
		} else {
			driveMode = 0;
		} 


		switch (driveMode) {
			case 0:
				//the 0s are temporary replacements for the robot relative joysticks. remember to find the opposite of the y value
				translateAndRotate(leftX, -leftY, twist, ahrs.getAngle() - ahrsOffset, rightDirection, rightMagnitude, 0, 0);
				break;

			case 1:
				tuningMode(frb, flb, blb, brb);
				break;

			case 2:

				break;
		
			default:
				break;
		}
		
		if (rightTrigger.get() == true) {
			ahrsOffset = ahrs.getAngle();
			driveStraight = false;
		}
			
			  
		SmartDashboard.putNumber("ahrs angle", ahrs.getAngle() - ahrsOffset);
		SmartDashboard.putNumber("Joystick output", leftDirection);
		SmartDashboard.putNumber("Joystick output speed", speed);
		
	}



	static void translateAndRotate(double driveFieldTranslationX, double driveFieldTranslationY, double unregulatedTurning, double gyroReading, double fieldRelativeRobotDirection, double absoluteTurnMagnitude, double driveRobotTranslationX, double driveRobotTranslationY) {

		//turns the gyro into a 0-360 range -- easier to work with
		double gyroValue = (Math.abs(((int)(gyroReading)) * 360) + gyroReading) % 360;

		//initializing the main variables
		double fieldRelativeX = driveFieldTranslationX;
		double fieldRelativeY = driveFieldTranslationY;
		double robotRelativeX = driveRobotTranslationX;
		double robotRelativeY = driveRobotTranslationY;
		double unregulatedRotationValue = unregulatedTurning;
		double absoluteRotationMagnitude = absoluteTurnMagnitude;
		double absoluteFieldRelativeDirection = fieldRelativeRobotDirection;

		//Rotation Modes -- absolute, unregulated, and none
		double rotationMagnitude;
		if (absoluteRotationMagnitude > RobotMap.ABSOLUTE_ROTATION_DEADZONE) {
			if (absoluteFieldRelativeDirection < 0) absoluteFieldRelativeDirection += 360;

			if (absoluteFieldRelativeDirection > 337.5 || absoluteFieldRelativeDirection <= 22.5) {
				absoluteFieldRelativeDirection = 0;
			} else if (absoluteFieldRelativeDirection > 22.5 && absoluteFieldRelativeDirection <= 67.5) {
				absoluteFieldRelativeDirection = 45;
			} else if (absoluteFieldRelativeDirection > 67.5 && absoluteFieldRelativeDirection <= 110.5) {
				absoluteFieldRelativeDirection = 90;
			} else if (absoluteFieldRelativeDirection > 110.5 && absoluteFieldRelativeDirection <= 157.5) {
				absoluteFieldRelativeDirection = 135;
			} else if (absoluteFieldRelativeDirection > 157.5 && absoluteFieldRelativeDirection <= 202.5) {
				absoluteFieldRelativeDirection = 180;
			} else if (absoluteFieldRelativeDirection > 202.5 && absoluteFieldRelativeDirection <= 247.5) {
				absoluteFieldRelativeDirection = 225;
			} else if (absoluteFieldRelativeDirection > 247.5 && absoluteFieldRelativeDirection <= 292.5) {
				absoluteFieldRelativeDirection = 270;
			} else {
				absoluteFieldRelativeDirection = 315;
			}
			rotationMagnitude = ((absoluteFieldRelativeDirection - gyroValue) / 100) * Math.pow((absoluteRotationMagnitude / 1.5), 2);
			if (Math.abs(absoluteFieldRelativeDirection - gyroValue) > 180) rotationMagnitude *= -1;
			driveStraight = false;
		} else if (unregulatedRotationValue > RobotMap.ROTATION_DEADZONE) {
			rotationMagnitude = unregulatedRotationValue;
			driveStraight = false;
		} else {
			if (driveStraight == false) {
				driveStraight = true;
				translationAngle = gyroValue;
			}
			rotationMagnitude = 0.01 * (translationAngle - gyroValue);
		}
		if (rotationMagnitude > 1) rotationMagnitude = 1;
		if (rotationMagnitude < -1) rotationMagnitude = -1;


		//Translation Modes -- field relative or robot relative
		double jsMag = Math.sqrt(Math.pow(fieldRelativeX, 2) + Math.pow(fieldRelativeY, 2));
		if (jsMag < RobotMap.TRANSLATION_DEADZONE) jsMag = 0;
		if (jsMag != 0) {
			double initialAngle;

			if (fieldRelativeX == 0) {
				initialAngle = 90;
			} else {
				initialAngle = Math.toDegrees(Math.atan(fieldRelativeY / fieldRelativeX));
			}
	
			if (fieldRelativeX < 0) {
				if (fieldRelativeY > 0) {
					initialAngle += 180;
				} else {
					initialAngle -= 180;
				}
			}
	
			double processedAngle = initialAngle + gyroValue;
			robotRelativeX = jsMag * Math.cos(Math.toRadians(processedAngle));	
			robotRelativeY = jsMag * Math.sin(Math.toRadians(processedAngle));
		} else if (Math.sqrt(Math.pow(robotRelativeX, 2) + Math.pow(robotRelativeY, 2)) < RobotMap.TRANSLATION_DEADZONE) {
			//if the field relative code didn't run, robot rel will still be set from its declaration, this rules out deadzones
			robotRelativeX = 0;
			robotRelativeY = 0;
		}

		//Vector math to combine the translation and the rotation values
		//adding the various cartesian points for the end of the vectors
		double xWithRotation = robotRelativeX + rotationMagnitude;
		double xWithoutRotation = robotRelativeX - rotationMagnitude;
		double yWithRotation = robotRelativeY + rotationMagnitude;
		double yWithoutRotation = robotRelativeY - rotationMagnitude;

		//Constructing the arrays to be used to determine outcomes for each wheel
		double wheelX[] = new double[4]; //the x value of the wheels vector
		double wheelY[] = new double[4]; //the y value of the wheels vector

		double wheelSpeed[] = new double[4]; //the speed that will be assigned to the wheels output
		double wheelAngle[] = new double[4]; // the angle that will be assigned to the modules output
	
		//individually processes each wheel -- determines speed and angle
		for (int i = 0 ; i < 4 ; i ++) {

			//for each module, the turn vectors will extend in a different direction
			if(i == 0 || i == 1) {
				wheelX[i] = xWithRotation;
			} else {
				wheelX[i] = xWithoutRotation;
			}

			if(i == 0 || i == 3) {
				wheelY[i] = yWithoutRotation;
			} else {
				wheelY[i] = yWithRotation;
			}

			//the wheels speed is just the distance from the end of its added vectors and the wheels center
			wheelSpeed[i] = Math.sqrt(Math.pow(wheelX[i], 2) + Math.pow(wheelY[i], 2));
			//the angle is the interior angle of the formed triangle
			wheelAngle[i] = Math.toDegrees(Math.atan(wheelX[i] / wheelY[i]));

			//The math only allows for directions in 2 quadrants, have to reassign values to gain the 2nd and 3rd quadrants
			if(wheelX[i] >= 0) {
				if (wheelY[i] >= 0) {
					//already in Q1
				} else {
					//shift to Q4
					wheelAngle[i] += 180;
				}
			} else {
				if (wheelY[i] >= 0) {
					//shift to Q2
				} else {
					//shift to Q3
					wheelAngle[i] -= 180;
				}
			}

			//math is done assuming clockwise, wheel outputs are counterclockwise
			wheelAngle[i] *= -1;

			//makes all angles positive -- if negative will make it a positive co-terminal angle
			if (wheelAngle[i] < 0) {
				wheelAngle[i] += 360;
			}
		}
	
		//assures that no wheel is given a speed higher than 1 -- if so, will divide all speeds by the highest speed
		double maxSpeed = wheelSpeed[0];
		if (wheelSpeed[1] > maxSpeed) {maxSpeed = wheelSpeed[1];}
		if (wheelSpeed[2] > maxSpeed) {maxSpeed = wheelSpeed[2];}
		if (wheelSpeed[3] > maxSpeed) {maxSpeed = wheelSpeed[3];}
		if (maxSpeed > 1) {
			for (int i = 0 ; i < 4 ; i ++) {
				wheelSpeed[i] /= maxSpeed;
			}
		}
		
		//sets all modules to the calculated speed and angle
		frontRight.control(wheelSpeed[0], wheelAngle[0]);
		frontLeft.control(wheelSpeed[1], wheelAngle[1]);
		backLeft.control(wheelSpeed[2], wheelAngle[2]);
		backRight.control(wheelSpeed[3], wheelAngle[3]);

		//reads out the raw angles, processed angles, speed, and gyro
		SmartDashboard.putNumber("FR raw angle", frontRight.getAngle());
		SmartDashboard.putNumber("FL raw angle", frontLeft.getAngle());
		SmartDashboard.putNumber("BL raw angle", backLeft.getAngle());
		SmartDashboard.putNumber("BR raw angle", backRight.getAngle());

		SmartDashboard.putNumber("FR Speed", wheelSpeed[0]);
		SmartDashboard.putNumber("FL Speed", wheelSpeed[1]);
		SmartDashboard.putNumber("BL Speed", wheelSpeed[2]);
		SmartDashboard.putNumber("BR Speed", wheelSpeed[3]);

		SmartDashboard.putNumber("FR Angle", wheelAngle[0]);
		SmartDashboard.putNumber("FL Angle", wheelAngle[1]);
		SmartDashboard.putNumber("BL Angle", wheelAngle[2]);
		SmartDashboard.putNumber("BR Angle", wheelAngle[3]);

		SmartDashboard.putNumber("Gyro 0-360", gyroValue);
	}

	static void parkPosition() {
		//can be activated to give the robot increased traction when stopped
		frontRight.control(0, -45);
		frontLeft.control(0, 45);
		backLeft.control(0, -45);
		backRight.control(0, 45);
	}

	static void tuningMode(JoystickButton buttonFR, JoystickButton buttonFL, JoystickButton buttonBL, JoystickButton buttonBR) {
		//used to tune the modules and their zero values
		if (buttonFR.get()) {
			frontRight.control(0.1, 0);
		} else {
			frontRight.stop();
		}
		if (buttonFL.get()) {
			frontLeft.control(0.1, 0);
		} else {
			frontLeft.stop();
		}
		if (buttonBR.get()) {
			backRight.control(0.1, 0);
		} else {
			backRight.stop();
		}
		if (buttonBL.get()) {
			backLeft.control(0.1, 0);
		} else {
			backLeft.stop();
		}
		SmartDashboard.putNumber("FR raw angle", frontRight.getAngle());
		SmartDashboard.putNumber("FL raw angle", frontLeft.getAngle());
		SmartDashboard.putNumber("BL raw angle", backLeft.getAngle());
		SmartDashboard.putNumber("BR raw angle", backRight.getAngle());
	}

	static void syncroDrive(double driveSpeed, double driveAngle, double twist, double gyroReading) {
		//not field relative yet -- sitll needs work
		driveAngle += gyroReading;

		if (Math.abs(twist) > 0.5) {
			if (twist > 0) {
				twist = (twist - 0.5)*2;
			} else if (twist < 0) {
				twist = (twist + 0.5)*2;
			}
			frontRight.control(-twist, 45);
			frontLeft.control(twist, 315);
			backRight.control(-twist, 315);
			backLeft.control(twist, 45);
		} else {
			frontRight.control(driveSpeed, driveAngle);
			frontLeft.control(driveSpeed, driveAngle);
			backRight.control(driveSpeed, driveAngle);
			backLeft.control(driveSpeed, driveAngle);
		}

		SmartDashboard.putNumber("front right encoder: ", frontRight.getAngle());
		SmartDashboard.putNumber("front left encoder: ", frontLeft.getAngle());
		SmartDashboard.putNumber("back right encoder: ", backRight.getAngle());
		SmartDashboard.putNumber("back left encoder: ", backLeft.getAngle());

		SmartDashboard.putNumber("Corrected angle FR", frontRight.convertToRobotRelative(frontRight.getAngle()));
		SmartDashboard.putNumber("Corrected angle FL", frontLeft.convertToRobotRelative(frontLeft.getAngle()));
		SmartDashboard.putNumber("Corrected angle BR", backRight.convertToRobotRelative(backRight.getAngle()));
		SmartDashboard.putNumber("Corrected angle BL", backLeft.convertToRobotRelative(backLeft.getAngle()));
	}

	static void individualModuleControl(boolean buttonfr, boolean buttonfl, boolean buttonbr, boolean buttonbl) {
		if (buttonfr) {
			frontRight.control(0.6, 0);
		} else {
			frontRight.stop();
		}
		if (buttonfl) {
			frontLeft.control(0.6, 0);
		} else {
			frontLeft.stop();
		}
		if (buttonbr) {
			backRight.control(0.6, 0);
		} else {
			backRight.stop();
		}
		if (buttonbl) {
			backLeft.control(0.6, 0);
		} else {
			backLeft.stop();
		}
		SmartDashboard.putNumber("front right encoder: ", frontRight.getAngle());
		SmartDashboard.putNumber("front left encoder: ", frontLeft.getAngle());
		SmartDashboard.putNumber("back right encoder: ", backRight.getAngle());
		SmartDashboard.putNumber("back left encoder: ", backLeft.getAngle());

		SmartDashboard.putNumber("Corrected angle FR", frontRight.convertToRobotRelative(frontRight.getAngle()));
		SmartDashboard.putNumber("Corrected angle FL", frontLeft.convertToRobotRelative(frontLeft.getAngle()));
		SmartDashboard.putNumber("Corrected angle BR", backRight.convertToRobotRelative(backRight.getAngle()));
		SmartDashboard.putNumber("Corrected angle BL", backLeft.convertToRobotRelative(backLeft.getAngle()));
	}
}
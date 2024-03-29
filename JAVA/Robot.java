
//~~~~~~~//
// Qubit //
//~~~~~~~//

package org.firstinspires.ftc.teamcode;
import android.util.Log;

import com.disnodeteam.dogecv.CameraViewDisplay;
import com.disnodeteam.dogecv.DogeCV;
import com.disnodeteam.dogecv.Dogeforia;
import com.disnodeteam.dogecv.detectors.roverrukus.GoldDetector;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.YZX;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;
import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.BACK;
import static org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection.FRONT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

final public class Robot {

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
//  Declar private toate componentele electronice(hardwareMap), modul de operare(telemetry)si gamepad-urile//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private HardwareMap hardwareMap;

    private Telemetry telemetry;

    private Gamepad gamepad1;

    private Gamepad gamepad2;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
//Declar private motoarele dandu-le si un nume sugestiv pentru a fi mai usor de lucrat//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private DcMotor leftFrontMotor;

    private DcMotor rightFrontMotor;

    private DcMotor leftBackMotor;

    private DcMotor rightBackMotor;

    private DcMotor armElbowMotor;

    private DcMotor armLatching;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Declar private servourile dandu-le si un nume sugestiv pentru a fi mai usor de lucrat//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private CRServo suctionServo;

    private Servo dropServo;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Declar private senzorii dandu-le si un nume sugestiv pentru a fi mai usor de lucrat//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private TouchSensor digitalTouch;

    private BNO055IMU imu;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// In continuare voi avea nevoie de o constanta mmPerInch pentru a calcula distanta//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private static final float mmPerInch        = 25.4f;


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Declaratiile de care am nevoie pentru autonoma//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    private DistanceSensor boxSensor;

    private double timeNeededToMoveColectorArmToParkingPosition;
    private double timeLeftToMoveColectorArmToParkingPosition;
    private ElapsedTime currentTimeToMoveCollectArm = new ElapsedTime();
    private boolean timerReset = false;
    private boolean armMoveDone = false;

//~~~~~~~~~~~~~~~//
// Constructorii //
//~~~~~~~~~~~~~~~//

    public Robot(HardwareMap hardwareMap, Telemetry telemetry, Gamepad gamepad1, Gamepad gamepad2) {
        this.hardwareMap = hardwareMap;

        this.telemetry = telemetry;

        this.gamepad1 = gamepad1;

        this.gamepad2 = gamepad2;

        this.configureAllHardware();
    }

    public Robot(HardwareMap hardwareMap, Telemetry telemetry) {
        this.hardwareMap = hardwareMap;

        this.telemetry = telemetry;

        this.configureAllHardware();
    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Modul operational al robotului: teleMode este controlarea robotului cu gamepad-uri //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    void teleMode() {
        //Functia pentru miscarea robotului
        this.moveRobot();
        //Functia pentru colectarea deseurilor
        this.moveSuction();
        //Functia pentru miscarea bratului de colectare
        this.moveCollectorArm();
        //Functia pentru ridicarea sistemului cu recipient
        this.moveLanderArm();
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Aici voi declara 2 variabile x si y deoarece urmeaza sa colectez date de la imagini prin pixeli si //
// a le transpune intr-un sistem xOy                                                                  //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    private double x = 0;
    private double y = 0;
    boolean trashCollected =false;

    public void autonomous() throws IOException, JSONException {
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Verific camera pana detectez un deseu //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        while(x == 0 && y == 0) {
            getTrashCoord();
            telemetry.addData("X: ", x);
            telemetry.addData("Y: ", y);
            telemetry.addLine("SEARCHING FOR TRASH");
            telemetry.update();

        }
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Imi calculez distanta pana la deseu pentru a stii ce valoare sa dau encoder-elor//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        this.driveUsingEncoder(0.5, distanceToTrash(), false, false, -1.0);

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Se verifica daca deseul a fost colectat printr-un senzor din cutia de colectat iar daca nu servoul de colectat isi continua rotatia //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        while(!trashCollected) {
            if (boxSensor.getDistance(DistanceUnit.CM) <= 20) {
                telemetry.update();
                collectTrash();
                trashCollected = true;
            }else{
                suctionServo.setPower(-1.0);
            }
        }


        while(boxSensor.getDistance(DistanceUnit.CM) <= 20) {
            collectTrash();
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Transform json-ul in 2 variabile double//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private void getTrashCoord() throws IOException, JSONException {
        String data = readJsonUrl().replaceAll("[{} ]", "");
        data = data.replace("]","");
        data = data.replace("[","");
        data = data.replace("\"","");
        data = data.replace("\"","");

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Sparg vectorul ca sa creem 2 valori pentru X si Y//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

        String[] coord = data.split(",", 0);

        if(coord.length != 1) {
            if (coord[0] != null && coord[1] != null) {
                x = Double.parseDouble(coord[0]);
                y = Double.parseDouble(coord[1]);
            } else {
                x = 0;
                y = 0;
            }
        }
    }

    private void collectTrash() {
        this.armElbowMotor.setPower(-1.0);
        this.suctionServo.setPower(-1.0);
        try {
            Thread.sleep(3700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.armElbowMotor.setPower(0.0);

        this.dropServo.setPosition(0.7);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.dropServo.setPosition(1.0);

        this.armElbowMotor.setPower(1.0);
        try {
            Thread.sleep(3700);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.armElbowMotor.setPower(0.0);
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Ma folosesc de distanta focala si de inaltimea camerei fata de sol          //
// pentru a calcula cu teorema lu Pitagora "ipotenuza", distanta pana la deseu //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    private double focalDistance = 377;
    private double cameraHeigh = 41;

    private double degreeToTrash(){
        return ((Math.atan((200-x)/distanceToTrash()))*180)/Math.PI;
    }

    private double distanceToTrash(){
        return ((cameraHeigh*focalDistance)/Math.abs(y));
    }

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Crez si ma folosesc de un server local pentru a scrie coordonatele cu ajutorul rapsberry-ului //
//ca mai apoi sa le accesez de pe robot                                                          //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    private String readJsonUrl() throws IOException, JSONException {
        URL url;
        StringBuffer response = new StringBuffer();
        try {
            url = new URL("http://192.168.43.208:5000/coord");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url");
        }

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Aici e stringul cu cele 2 coordonate
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

            String responseJSON = response.toString();
            if(responseJSON != null) {
                return responseJSON;
            }else{
                responseJSON = "0,0";
                return responseJSON;
            }
        }
    }


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Functiile pe care le-am creat//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//


    public void stopAllMovements() {
        this.leftFrontMotor.setPower(0);
        this.rightFrontMotor.setPower(0);
        this.leftBackMotor.setPower(0);
        this.rightBackMotor.setPower(0);
        this.armElbowMotor.setPower(0.0);
        this.armLatching.setPower(0.0);
        this.suctionServo.setPower(0.0);
    }

//~~~~~~~~~~~~~~~~~~~~//
// Miscarea robotului //
//~~~~~~~~~~~~~~~~~~~~//

    private void moveRobot() {
        this.leftFrontMotor.setPower(this.gamepad1.left_stick_y - this.gamepad1.right_stick_x);
        this.leftBackMotor.setPower(this.gamepad1.left_stick_y - this.gamepad1.right_stick_x);
        this.rightFrontMotor.setPower(this.gamepad1.left_stick_y + this.gamepad1.right_stick_x);
        this.rightBackMotor.setPower(this.gamepad1.left_stick_y + this.gamepad1.right_stick_x);

    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Miscarea sistemului de colectat//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private void moveSuction() {
        if(this.gamepad1.right_bumper && !this.gamepad1.left_bumper) {
            this.suctionServo.setPower(-1.0);
        } else if(this.gamepad1.left_bumper && !this.gamepad1.right_bumper) {
            this.suctionServo.setPower(1.0);
        } else {
            this.suctionServo.setPower(0.0);
        }

        if(this.gamepad2.x){
            this.dropServo.setPosition(0.7);
            this.telemetry.addLine("AM APASAT X");
            this.telemetry.update();
        }

        if(!this.gamepad2.x){
            this.dropServo.setPosition(1.0);
        }
    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Miscarea bratului de colectat//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private void moveCollectorArm() {
        this.armElbowMotor.setPower(-this.gamepad2.right_stick_y * 1.0);
    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Miscarea bratului ce ridica recipientrul de colectare//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private void moveLanderArm() {
        this.telemetry.addData("armLatching: ", this.armLatching.getCurrentPosition());
        this.telemetry.addData("y: ", this.gamepad1.y);
        this.telemetry.addData("a: ", this.gamepad1.a);
        if(this.digitalTouch.isPressed() && this.digitalTouch.isPressed() && this.gamepad1.a && !this.gamepad1.y) {
            this.armLatching.setPower(0.0);
        } else if(this.digitalTouch.isPressed() && this.gamepad1.y && !this.gamepad1.a) {
            this.armLatching.setPower(-1.0);
        } else {
            if (this.gamepad1.y && !this.gamepad1.a) {
                this.armLatching.setPower(-1.0);
            } else if (this.gamepad1.a && !this.gamepad1.y) {
                this.armLatching.setPower(1.0);
            } else {
                this.armLatching.setPower(0.0);
            }
        }

        this.telemetry.addData("armLatchingPower: ", this.armLatching.getPower());

        this.telemetry.update();
    }

//~~~~~~~~~~~~~~~~~~~~~~//
// Functia de autonomie //
//~~~~~~~~~~~~~~~~~~~~~~//

    public void initAutonomous() {
        this.configureLatchingMotorForAuto();

        this.telemetry.addLine("1");
        this.telemetry.update();

        this.initializeIMU();

        this.telemetry.addLine("2");
        this.telemetry.update();

        //this.initializeCamera();

        this.telemetry.addLine("3");
        this.telemetry.update();

    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// IMU-ul folosit pentru orientarea in spatiu //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    public void initializeIMU() {
        BNO055IMU.Parameters parameter = new BNO055IMU.Parameters();
        parameter.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameter.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        this.imu.initialize(parameter);
        //DistanceSensor-ul folosit in interiorul cutiei
        boxSensor = hardwareMap.get(DistanceSensor.class, "sensor_range");
        Rev2mDistanceSensor sensorTimeOfFlight = (Rev2mDistanceSensor)boxSensor;
    }

    private double getAbsoluteHeading() {
        return this.imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES).firstAngle;
    }
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Variabilele numerice pentru motoare //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    static final double COUNTS_PER_MOTOR_REV = 1120;
    static final double DRIVE_GEAR_REDUCTION = 1.0;
    static final double WHEEL_DIAMETER_CM    = 4.0 * 2.54;
    static final double COUNTS_PER_CM        = ((COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) / (WHEEL_DIAMETER_CM  * 3.1415));

    private void driveUsingEncoder(double speed, double distance, boolean moveArmLatching, boolean moveArmCollect , double suctionDirection) {
        this.telemetry.addData("leftFrontPosInit: ",this.leftFrontMotor.getCurrentPosition());
        this.telemetry.addData("leftBackPosInit: ",this.leftBackMotor.getCurrentPosition());
        this.telemetry.addData("rightFrontPosInit: ",this.rightFrontMotor.getCurrentPosition());
        this.telemetry.addData("rightBackPosInit: ",this.rightBackMotor.getCurrentPosition());

        this.telemetry.update();

        double distanceWithMarginError = distance / 1.1;

        this.configureTractionMotorsForAuto();


        int leftFront = this.leftFrontMotor.getCurrentPosition() + (int)(distanceWithMarginError * COUNTS_PER_CM);
        int leftBack = this.leftBackMotor.getCurrentPosition() + (int)(distanceWithMarginError * COUNTS_PER_CM);
        int rightFront = this.rightFrontMotor.getCurrentPosition() + (int)(distanceWithMarginError * COUNTS_PER_CM);
        int rightBack = this.rightBackMotor.getCurrentPosition() + (int)(distanceWithMarginError * COUNTS_PER_CM);

        this.leftFrontMotor.setTargetPosition(leftFront);
        this.rightBackMotor.setTargetPosition(rightBack);
        this.leftBackMotor.setTargetPosition(leftBack);
        this.rightFrontMotor.setTargetPosition(rightFront);


        this.armLatching.setTargetPosition(5);

        while (this.leftFrontMotor.isBusy() || this.rightFrontMotor.isBusy() || this.leftBackMotor.isBusy() || this.rightBackMotor.isBusy()) {
            this.telemetry.addData("leftFrontPos: ",this.leftFrontMotor.getCurrentPosition());
            this.telemetry.addData("leftBackPos: ",this.leftBackMotor.getCurrentPosition());
            this.telemetry.addData("rightFrontPos: ",this.rightFrontMotor.getCurrentPosition());
            this.telemetry.addData("rightBackPos: ",this.rightBackMotor.getCurrentPosition());

            this.telemetry.update();

            this.leftFrontMotor.setPower(Math.abs(speed));
            this.rightFrontMotor.setPower(Math.abs(speed));
            this.leftBackMotor.setPower(Math.abs(speed));
            this.rightBackMotor.setPower(Math.abs(speed));


            if(moveArmLatching) {
                if (!this.digitalTouch.isPressed()) {
                    this.armLatching.setPower(1.0);

                    if (this.digitalTouch.isPressed()) {
                        this.armLatching.setPower(0.0);
                    }
                }

            }

            this.suctionServo.setPower(suctionDirection);

            if(moveArmCollect && !armMoveDone){

                if(!timerReset) {
                    currentTimeToMoveCollectArm.reset();
                    timerReset = true;
                }

                this.armElbowMotor.setPower(-1.0);
                this.timeLeftToMoveColectorArmToParkingPosition = (timeNeededToMoveColectorArmToParkingPosition - currentTimeToMoveCollectArm.seconds());

                if (timeLeftToMoveColectorArmToParkingPosition <= 0.0) {
                    this.armElbowMotor.setPower(0.0);
                    armMoveDone = true;
                }

            }

            if(leftFront < 0 && leftBack < 0 && rightFront < 0  && rightBack < 0) {
                if (leftFrontMotor.getCurrentPosition() < leftFront
                        || leftBackMotor.getCurrentPosition() < leftBack
                        || rightFrontMotor.getCurrentPosition() < rightFront
                        || rightBackMotor.getCurrentPosition() < rightBack)
                    break;
            } else {
                if (leftFrontMotor.getCurrentPosition() > leftFront
                        || leftBackMotor.getCurrentPosition() > leftBack
                        || rightFrontMotor.getCurrentPosition() > rightFront
                        || rightBackMotor.getCurrentPosition() > rightBack)
                    break;
            }
        }

        this.telemetry.addData("leftFrontPos: ",this.leftFrontMotor.getCurrentPosition());
        this.telemetry.addData("leftBackPos: ",this.leftBackMotor.getCurrentPosition());
        this.telemetry.addData("rightFrontPos: ",this.rightFrontMotor.getCurrentPosition());
        this.telemetry.addData("rightBackPos: ",this.rightBackMotor.getCurrentPosition());

        this.telemetry.update();


        this.stopAllMovements();
    }


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Functia pentru miscare la orice unghi doresc //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    public void rotateToTarget(double angleTarget) {
        this.configureTractionMotorsForTele();

        double currentAngle;

        boolean targetReached = false;

        currentAngle = this.getAbsoluteHeading();

        double minim = (angleTarget - currentAngle + 540) % 360 - 180;

        double power = -Math.signum(minim) * 0.25;

        while(!targetReached) {
            telemetry.addData("getAbsoluteHeading(): ", this.getAbsoluteHeading());
            telemetry.addData("angleTarget: ", angleTarget);
            telemetry.update();

            this.turn(power);

            if(angleTarget < 0) {
                if(angleTarget < 0) {
                    if(this.getAbsoluteHeading() < angleTarget) {
                        targetReached = true;
                    }
                } else {
                    if(this.getAbsoluteHeading() > angleTarget) {
                        targetReached = true;
                    }
                }
            } else {
                if(angleTarget < 0) {
                    if(this.getAbsoluteHeading() < angleTarget) {
                        targetReached = true;
                    }
                } else {
                    if(this.getAbsoluteHeading() > angleTarget) {
                        targetReached = true;
                    }
                }
            }

            telemetry.update();
        }

        this.stopAllMovements();
    }

    public void rotateCertainDegrees(double angle) {

        this.configureTractionMotorsForTele();

        double target = this.getAbsoluteHeading() + angle;

        boolean targetReached = false;

        telemetry.addData("getAbsoluteHeading(): ", this.getAbsoluteHeading());
        telemetry.addData("Target", target);
        telemetry.update();

        while(!targetReached) {
            this.turn(-Math.signum(angle) * 0.25);

            telemetry.addData("this.getAbsoluteHeading(): ", this.getAbsoluteHeading());

            if(target < 0) {
                if(angle < 0) {
                    if(this.getAbsoluteHeading() < target + 0) {
                        targetReached = true;
                    }
                } else {
                    if(this.getAbsoluteHeading() > target - 0) {
                        targetReached = true;
                    }
                }
            } else {
                if(angle < 0) {
                    if(this.getAbsoluteHeading() < target + 0) {
                        targetReached = true;
                    }
                } else {
                    if(this.getAbsoluteHeading() > target - 0) {
                        targetReached = true;
                    }
                }
            }

            telemetry.update();
        }

        this.stopAllMovements();
    }

    private void turn(double sPower) {
        this.leftFrontMotor.setPower(-sPower);
        this.leftBackMotor.setPower(-sPower);
        this.rightFrontMotor.setPower(sPower);
        this.rightFrontMotor.setPower(sPower);
    }

    public double computeDistanceWithPadding(double distance) {
        return distance / 1.3;
    }


//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Declararea tuturor pieselor electrice folosite //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

    private void configureAllHardware() {
        this.configureTractionMotors();
        this.configureOtherMotors();
        this.configureServos();
        this.configureSensors();
    }

    private void configureSensors() {
        this.digitalTouch = hardwareMap.get(TouchSensor.class, "stop");
        this.imu = hardwareMap.get(BNO055IMU.class, "IMU");
    }

    private void configureServos() {
        this.suctionServo = hardwareMap.get(CRServo.class, "suctionServo");
        this.dropServo = hardwareMap.get(Servo.class, "palletServo");
    }

    public void configureOtherMotors() {
        this.armElbowMotor = hardwareMap.get(DcMotor.class, "Brat2");
        this.armLatching = hardwareMap.get(DcMotor.class, "motorUp");
    }

    private void configureLatchingMotorForAuto() {
        this.armLatching.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.armLatching.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    private void configureTractionMotors() {
        this.leftFrontMotor = hardwareMap.get(DcMotor.class, "leftFront");
        this.rightFrontMotor = hardwareMap.get(DcMotor.class, "rightFront");
        this.leftBackMotor = hardwareMap.get(DcMotor.class, "leftBack");
        this.rightBackMotor = hardwareMap.get(DcMotor.class, "rightBack");

        this.leftBackMotor.setDirection(DcMotor.Direction.REVERSE);
        this.leftFrontMotor.setDirection(DcMotor.Direction.REVERSE);

        this.leftFrontMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.rightFrontMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.leftBackMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.rightBackMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private void configureTractionMotorsForAuto() {
        this.leftFrontMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.leftBackMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.rightFrontMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.rightBackMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        this.leftFrontMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        this.leftBackMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        this.rightFrontMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        this.rightBackMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    }

    private void configureTractionMotorsForTele() {
        this.leftFrontMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.leftBackMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.rightFrontMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        this.rightBackMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        this.leftFrontMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.rightFrontMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.leftBackMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        this.rightBackMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Signed by Qubit (Preda Bogdan)//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
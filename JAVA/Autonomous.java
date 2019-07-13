

//~~~~~~~//
// Qubit //
//~~~~~~~//


package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.json.JSONException;

import java.io.IOException;

@com.qualcomm.robotcore.eventloop.opmode.Autonomous
public class Autonomous extends LinearOpMode {

    private Robot robot;
    private boolean autonomousFinished = false;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Se initalizeaza toate modulele si componentele electrice //
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
    public void initialize() {

        this.robot = new Robot(this.hardwareMap, this.telemetry);
        this.robot.initAutonomous();
    }

    @Override
    public void runOpMode() {
        this.initialize();

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
//  Dupa ce s-au initializat componentele se asteapta start-ul utilizatorului//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
        while(isStarted() == false && isStopRequested() == false){
            this.telemetry.addData("Status: ", "waiting for start command... time = %.02f",getRuntime());
            this.telemetry.update();
            sleep(200);

        }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Dupa ce se selecteaza programul robotul incepe sa colecteze deseuri//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//

        while(opModeIsActive()){
// Functiile folosite sunt mostenite de la clasa Robot pentru un program mult mai simplu de scris
            try {
                this.robot.autonomous();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
// Signed by Qubit (Preda Bogdan)//
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~//
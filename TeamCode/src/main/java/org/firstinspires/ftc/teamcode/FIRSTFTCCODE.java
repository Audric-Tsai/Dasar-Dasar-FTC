package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.Range;

@TeleOp(name = "HelloFTC", group = "Competition")
public class FIRSTFTCCODE extends LinearOpMode {

    // ---- Tuning constants ----------------------------------------------
    // Change behaviour here, not scattered through the loop below.

    private static final double FLYWHEEL_TARGET_TPS = 1800;  // ticks/sec — TUNE THIS
    private static final double FLYWHEEL_TOLERANCE  = 100;   // ticks/sec, for "at speed"
    private static final double FEEDER_POWER        = 1.0;
    private static final double SLOW_MODE_FACTOR    = 0.35;

    // ---- Hardware ------------------------------------------------------

    private DcMotor    leftMotor;
    private DcMotor    rightMotor;
    private DcMotor    intake;
    private DcMotorEx  launching;      // Ex = velocity control available
    private DcMotor    launchControl;

    @Override
    public void runOpMode() {

        // ---- Init ------------------------------------------------------

        leftMotor     = hardwareMap.get(DcMotor.class,   "left_drive");
        rightMotor    = hardwareMap.get(DcMotor.class,   "right_drive");
        intake        = hardwareMap.get(DcMotor.class,   "intake");
        launching     = hardwareMap.get(DcMotorEx.class, "launching");
        launchControl = hardwareMap.get(DcMotor.class,   "launchControl");

        // Set direction once here, then use POSITIVE power everywhere below.
        // If a mechanism runs backwards, flip it here — never by negating power.
        leftMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        rightMotor.setDirection(DcMotorSimple.Direction.FORWARD);
        intake.setDirection(DcMotorSimple.Direction.REVERSE);
        launching.setDirection(DcMotorSimple.Direction.FORWARD);
        launchControl.setDirection(DcMotorSimple.Direction.FORWARD);

        // Brake instead of coast — much more precise for lining up shots.
        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Closed-loop velocity on the flywheel. This is the important one:
        // it holds RPM as the battery sags, so late-match shots land the same
        // as first-match shots.
        launching.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        launching.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launching.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        boolean flywheelOn = false;
        boolean feederOn   = false;

        waitForStart();

        // ---- Main loop -------------------------------------------------

        while (opModeIsActive()) {

            // Drive — arcade. left_stick_y is negated because the gamepad
            // reports forward as negative.
            double drive = -gamepad1.left_stick_y;
            double turn  =  gamepad1.right_stick_x;

            // Squaring preserves sign but compresses the low end, giving
            // much finer control when creeping into position.
            drive = Math.copySign(drive * drive, drive);
            turn  = Math.copySign(turn  * turn,  turn);

            double scale = gamepad1.left_stick_button ? SLOW_MODE_FACTOR : 1.0;

            double leftPower  = Range.clip((drive + turn) * scale, -1.0, 1.0);
            double rightPower = Range.clip((drive - turn) * scale, -1.0, 1.0);

            leftMotor.setPower(leftPower);
            rightMotor.setPower(rightPower);

            // Flywheel — A on, B off
            if (gamepad1.a) flywheelOn = true;
            if (gamepad1.b) flywheelOn = false;

            launching.setVelocity(flywheelOn ? FLYWHEEL_TARGET_TPS : 0);

            double actualVelocity = launching.getVelocity();
            boolean atSpeed = flywheelOn
                    && Math.abs(actualVelocity - FLYWHEEL_TARGET_TPS) < FLYWHEEL_TOLERANCE;

            // Feeder — Triangle on, Square off. Interlocked: it will not run
            // unless the flywheel is actually up to speed, which prevents
            // jams and the classic weak first shot.
            if (gamepad1.y) feederOn = true;
            if (gamepad1.x) feederOn = false;

            launchControl.setPower((feederOn && atSpeed) ? FEEDER_POWER : 0);

            // Intake — triggers give variable speed in both directions
            intake.setPower(gamepad1.right_trigger - gamepad1.left_trigger);

            // Rumble the controller when the shot is ready, so the driver can
            // keep their eyes on the field instead of the telemetry screen.
            if (atSpeed) {
                gamepad1.rumble(150);
            }

            // ---- Telemetry ---------------------------------------------

            telemetry.addData("FLYWHEEL", flywheelOn
                    ? (atSpeed ? ">>> READY <<<" : "spooling...")
                    : "off");
            telemetry.addData("  velocity", "%.0f / %.0f", actualVelocity, FLYWHEEL_TARGET_TPS);
            telemetry.addData("FEEDER", feederOn ? (atSpeed ? "running" : "waiting") : "off");
            telemetry.addLine();
            telemetry.addData("Drive", "L %.2f  R %.2f", leftPower, rightPower);
            telemetry.addData("Mode", scale < 1.0 ? "SLOW" : "normal");
            telemetry.addData("Intake", "%.2f", intake.getPower());
            telemetry.update();
        }

        // ---- Cleanup ---------------------------------------------------

        launching.setVelocity(0);
        launchControl.setPower(0);
        intake.setPower(0);
        leftMotor.setPower(0);
        rightMotor.setPower(0);
    }
}

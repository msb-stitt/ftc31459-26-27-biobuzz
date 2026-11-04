# Launcher troubleshooting

If the launcher stutters rather than holding its speed, run **Launcher Check**, which
[README.md](./README.md) describes, and try these in order:

- Run it at 1250, the full target. At a fifth, 37523's feedforward asks for 0.145 of full speed,
  where an F of 13.75 alone would ask for 0.105. Too little, and the wheels stick and surge.
- At 0.2, `launcher/measured_tps` is positive and near 250. Negative, or 0 while the wheel turns,
  means the motor or its encoder is wired backwards, or the encoder is not in the port beside its
  motor.
- In the Robot Configuration, `launcher` is a goBILDA 5203 series 6000 rpm motor.
- With the robot off, the launcher wheels spin freely by hand, nothing rubs the frame, and the
  belt does not slip.
- Step from 0.2 to 1.0. A stutter only at low fractions points to the feedforward; at every
  fraction, to the wiring or the gains.
- If the speed swings around the target, lower `launcher_P_ctrlUnitsPerTickPerS` in the robot's
  constants file (pedro/Bot*.java); if it never reaches it, raise
  `launcher_kV_powerFractionPerTickPerS`. The motor controller's feedforward is a static part,
  `launcher_kS_powerFraction`, plus `launcher_kV_powerFractionPerTickPerS` for each tick per second
  of the target, so the launcher is given a new F each time its target changes. 37523's are P 40,
  0.05 and 0.00037963, which give its F of 13.75 at 1250 ticks per second; goBILDA's example used
  `(40, 0, 0, 12.5)`, and its DECODE StarterBot `(300, 0, 0, 10)`.

package com.Jessy1237.DwarfCraft;

public class TrainSkillSchedule implements Runnable {

	private final DwarfTrainer trainer;
	private final DCPlayer dcplayer;
	
	public TrainSkillSchedule(DwarfTrainer trainer, DCPlayer dcplayer) {
		this.trainer = trainer;
		this.dcplayer = dcplayer;
	}

	@Override
	public void run() {
		trainer.trainSkill(dcplayer);
	}

}
